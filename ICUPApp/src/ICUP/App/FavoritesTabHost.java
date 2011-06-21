package ICUP.App;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TimePicker;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.blueduck.collegepedia.dto.EventDTO;
import com.blueduck.collegepedia.dto.OrganizationDTO;
import com.blueduck.collegepedia.dto.ParentDTO;

public class FavoritesTabHost extends ICUPListActivity {
	
	// array lists to hold all of our events and orgs
	private ArrayList<EventDTO> favEvents = null;
	private ArrayList<EventDTO> favEventsSearch = null;

	private ArrayList<OrganizationDTO> favOrgs = null;
	private ArrayList<OrganizationDTO> favOrgsSearch = null;

	private Runnable viewFavorites;

	private EventAdapter myEventAdapter;
	private BusinessAdapter myBusinessAdapter;


	private TabHost tabHost;
    private boolean advancedSearchVisible;
	private ParentDTO curSelection;
	private boolean searchOn = false;

	//Buttons 
	private RadioButton eventsRB; 
	private RadioButton orgsRB;
	private boolean eventSearch = true;
	private static String TAG = "FavoritesTabHost";

	/**
	 * Runnable object to update the ListView via the adapter "myAdapter"
	 * Content generated from myEvents
	 * This also removes the context/pop-up window
	 */
	private Runnable returnRes = new Runnable()
	{
		public void run() {
			if(tabHost.getCurrentTab() == 0)
			{
				myEventAdapter.clear();
				if(favEvents != null && favEvents.size() > 0){
					myEventAdapter.notifyDataSetChanged();
					for(int i = 0; i < favEvents.size(); i++)
						myEventAdapter.add(favEvents.get(i));
				}
				myEventAdapter.notifyDataSetChanged();
			}
			else if(tabHost.getCurrentTab() == 1)
			{
				myBusinessAdapter.clear();
				if(myBusinessAdapter != null && favOrgs.size() > 0){
					myBusinessAdapter.notifyDataSetChanged();
					for(int i = 0; i < favOrgs.size(); i++)
						myBusinessAdapter.add(favOrgs.get(i));
				}
				myBusinessAdapter.notifyDataSetChanged();            	
			}
		}
	};

	/* 
	 * Creates a new Favorites tab host. Sets up swiping and loads
	 * information from the data base. Also configures our pop up handler
	 * via a onItemClick listener
	 * (non-Javadoc)
	 * @see ICUP.App.ICUPListActivity#onCreate(android.os.Bundle)
	 */
	/* (non-Javadoc)
	 * @see ICUP.App.ICUPListActivity#onCreate(android.os.Bundle)
	 */
	/* (non-Javadoc)
	 * @see ICUP.App.ICUPListActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favoriteshost);
		
		//Setting Radio Buttons 
		eventsRB = (RadioButton)findViewById(R.id.faveventsRB);
		orgsRB = (RadioButton)findViewById(R.id.favorgsRB);

		tabHost = (TabHost) this.findViewById(R.id.favoritestabhost);  // The activity TabHost
		doTabSetup(tabHost);
		tabHost.setCurrentTab(0);
		
		// Initialize the list of event items
		favEvents = new ArrayList<EventDTO>();
		myEventAdapter = new EventAdapter(this,this, R.layout.faveventitem, favEvents, EventAdapter.adapterType.FAVORITE);

		// Initialize the list of business items
		favOrgs = new ArrayList<OrganizationDTO>();
		myBusinessAdapter = new BusinessAdapter(this, this, R.layout.businessitem, favOrgs, true);
		
		// Create the viewEvents Runnable object.
		// Make it Runnable so it can be run again in case events want to be updated, etc. 
		viewFavorites = new Runnable(){
			public void run() {
				if(tabHost.getCurrentTab() == 0)
				{
					setListAdapter(myEventAdapter);
					getEvents();
				}
				else if (tabHost.getCurrentTab() == 1)
				{
					setListAdapter(myBusinessAdapter);
					getBusinesses();
				}
			}
		};

		// Create and start the thread to get and load the Events
		final Thread thread =  new Thread(null, viewFavorites, "FavoritesList");
		thread.start();

		// For swiping
		// You MUST set the intent to the left and right of this current activity/view
		// Forgetting to do so will cause the application to crash.
		
		Intent leftIntent = new Intent(this, EventsCat.class);
		Intent rightIntent = new Intent(this, BusinessCat.class);
		enableSwipe(leftIntent, rightIntent);
		
		
		// For swiping in complex views
		findViewById(R.id.favoritestabhost).setOnTouchListener(gestureListener);
		tabHost.setOnTabChangedListener(new OnTabChangeListener(){
			public void onTabChanged(String tabId) {
				if (searchOn)
        		{
        			searchOn = false;
        			updateEventList(favEvents);
        			updateOrgList(favOrgs);
        		}
				getCurrentFocus().setOnTouchListener(gestureListener);
				thread.run();
			}
		});

		// custom on click listener that creates a pop up when a given list item is clicked
		this.getListView().setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> a, View v, int position,long id) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				TextView popupTitle = null;
				TextView popupDesc = null;
				TextView popupLocation = null;
				ImageView popupImage = null;
				if(tabHost.getCurrentTab() == 0)
				{
					v = inflater.inflate(R.layout.faveventpopup, null);
					popupTitle = (TextView) v.findViewById(R.id.faveventPopupTitle);
					popupDesc = (TextView) v.findViewById(R.id.faveventPopupDesc);
					TextView popupStartTime = (TextView) v.findViewById(R.id.faveventPopupStartDatetime);
	        		TextView popupEndTime = (TextView) v.findViewById(R.id.faveventPopupEndDatetime);
					popupLocation = (TextView) v.findViewById(R.id.faveventPopupLocation);
	                popupImage = (ImageView)v.findViewById(R.id.faveventPopupImage);
					EventDTO o = (EventDTO) getListView().getItemAtPosition(position);
					curSelection = o;
					if (popupTitle != null)
					{
						popupTitle.setText(o.getName());
					}
					if (popupDesc != null)
					{
						popupDesc.setText(o.getDescription().replace("\r", ""));
					}
					if (popupStartTime != null && o.getStartDatetime() != null)
	                {
	                	popupStartTime.setText("Start Time: "+o.getStartDatetime().toLocaleString());
	                }
	                if (popupEndTime != null && o.getStopDatetime() != null)
	                {
	                	popupEndTime.setText("End Time: "+ o.getStopDatetime().toLocaleString());
	                }
					if (popupLocation != null)
					{
						popupLocation.setText(o.getLocation());
					}
					if (popupImage != null && o.getImage() != null)
	                {
	                	Bitmap pic;
	            		pic = BitmapFactory.decodeByteArray(o.getImage(), 0, o.getImage().length);
	            		popupImage.setImageBitmap(pic);
	                }
				}
				else if(tabHost.getCurrentTab() == 1)
				{
					v = inflater.inflate(R.layout.favbusinesspopup, null);
					popupTitle = (TextView) v.findViewById(R.id.favbusinessPopupName);
					popupDesc = (TextView) v.findViewById(R.id.favbusinessPopupDesc);
					popupLocation = (TextView) v.findViewById(R.id.favbusinessPopupLocation);
	                popupImage = (ImageView)v.findViewById(R.id.favbusinessPopupImage);

					OrganizationDTO o = (OrganizationDTO) getListView().getItemAtPosition(position);
					curSelection = o;
					if (popupTitle != null)
					{
						popupTitle.setText(o.getName());
					}
					if (popupDesc != null)
					{
						popupDesc.setText(o.getDescription().replace("\r", ""));
					}
					if (popupLocation != null)
					{
						popupLocation.setText(o.getAddress());
					}
					if (popupImage != null && o.getImage() != null)
	                {
	                	Bitmap pic;
	            		pic = BitmapFactory.decodeByteArray(o.getImage(), 0, o.getImage().length);
	            		popupImage.setImageBitmap(pic);
	                }
				}


				int popupHeight = (int) (getApplicationContext().getResources().getDisplayMetrics().heightPixels) - 25;
				int popupWidth = (int) (getApplicationContext().getResources().getDisplayMetrics().widthPixels);
				
				popup = new PopupWindow(v, popupWidth, popupHeight, false);
				popup.setOutsideTouchable(false);
				popup.showAtLocation(findViewById(R.id.favoriteslisttab), Gravity.CENTER, 0, 0);
			}
		}); 
		//disable the advanced search 
        findViewById(R.id.favoritesEventsAdvancedSearch).setVisibility(View.GONE);
        findViewById(R.id.favoritesOrgsAdvancedSearch).setVisibility(View.GONE);
        advancedSearchVisible = false;
	}

	/* 
	 * handles search button events from the phone
	 * (non-Javadoc)
	 * @see ICUP.App.ICUPListActivity#onKeyDown(int, android.view.KeyEvent)
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_SEARCH)
		{
    		TabHost tabHost = (TabHost) this.findViewById(R.id.favoritestabhost);
    		tabHost.setCurrentTab(2);
    		return true;
		}

    	return super.onKeyDown(keyCode, event);
    }

	/**
	 * Sets up the tab host and assigns views for each tab
	 * @param tHost
	 */
	private void doTabSetup(TabHost tHost){
		Resources res = getResources(); // Resource object to get Drawables

		TabHost.TabSpec spec;  // Reusable TabSpec for each tab
		TabHost.TabSpec specTest;  // Reusable TabSpec for each tab
		tHost.setup();

		// Create an Intent to launch an Activity for the tab (to be reused)
		specTest = tHost.newTabSpec("favevents");
		specTest.setIndicator("My Events", res.getDrawable(R.drawable.fav_events));
		specTest.setContent(R.id.favoriteslisttab);
		
		spec = tHost.newTabSpec("favbusinesses");
		spec.setIndicator("My Orgs", res.getDrawable(R.drawable.fav_organizations));
		spec.setContent(R.id.favoriteslisttab);
		
		// The setContent affects which tab the list will appear to be on.
		// For this reason,we have to add the tabs in this weird way and AFTER both contents have been set.
		tHost.addTab(specTest);
		tHost.addTab(spec);

		spec = tHost.newTabSpec("search");
		spec.setIndicator("Search", res.getDrawable(R.drawable.search));
		spec.setContent(R.id.favoritessearch);
		tHost.addTab(spec);
	}

	/**
	 * Gets all events from the DB
	 */
	private void getEvents()
	{
	
		int i;  
		Date date = new java.util.Date();
		favEvents = null;
		// Get the events list data from the server
		favEvents = dm.getFavEventList();

		if(favEvents == null){
			Log.i(TAG, "favEvents is null");
			return;
		} 
	
		// Put placeholders instead of null for any important information
		// This is IMPORTANT. It will crash the application if there is a null value in any of the elements of the Event item
		// See res/layout/eventitem.xml for details on what these elements are and/or change them.

		for (i = 0; i < favEvents.size(); i++)
		{
			if (favEvents.get(i).getName() == null)
			{
				favEvents.get(i).setName("Unknown Event");
			}			
			if (favEvents.get(i).getDescription() == null)
			{
				favEvents.get(i).setDescription("No description");
			}
			if (favEvents.get(i).getStopDatetime() == null)
			{
				favEvents.get(i).setStopDatetime(date);
			}
		}

		// See returnRes at the top for more information on why this is called here
		runOnUiThread(returnRes);
	}

	/**
	 * Gets all businesses from the db
	 */
	private void getBusinesses()
	{
		// Local variables for checking for program-terminating errors.
		int i;  

		favOrgs = null;
		favOrgs = dm.getFavOrganizationList();

		if(favOrgs == null){
			Log.i(TAG, "favOrgs is null");
			return;
		} 

		// Put placeholders instead of null for any important information
		// This is IMPORTANT. It will crash the application if there is a null value in any of the elements of the Event item
		// See res/layout/eventitem.xml for details on what these elements are and/or change them.

		for (i = 0; i < favOrgs.size(); i++)
		{
			if (favOrgs.get(i).getName() == null)
			{
				favOrgs.get(i).setName("Unknown Business");
			}			
			if (favOrgs.get(i).getDescription() == null)
			{
				favOrgs.get(i).setDescription("No description");
			}
			if (favOrgs.get(i).getId() == null)
			{
				favOrgs.get(i).setId(new Long(i));
			}
		}

		// See returnRes at the top for more information on why this is called here
		runOnUiThread(returnRes);
	}	
	
	
	
	/* 
	 * For swiping after tab content has changed (mainly lists)
	 * (non-Javadoc)
	 * @see android.app.ListActivity#onContentChanged()
	 */
	@Override
	public void onContentChanged()
	{
		super.onContentChanged();
		getListView().setOnTouchListener(gestureListener);
	}

	/**
	 * Removes a Event item from the list when the delete button is pressed
	 * @param v
	 */
	public void removeEventButtonClick(View v)
	{
		LinearLayout vwParentRow = (LinearLayout)v.getParent();
		TextView id = (TextView)vwParentRow.findViewById(R.id.eventId);
		for(EventDTO e : favEvents){
			if(e != null && (e.getId() == Long.parseLong(id.getText().toString()))){
				if(dm.removeFavEvent(e)){
					myEventAdapter.remove(e);
					myEventAdapter.notifyDataSetChanged();
				}
			}
		}
	}
	
	/**
	 * Removes a Business item from the list when the delete button is pressed
	 * @param v
	 */
	public void removeBusinessButtonClick(View v){
		LinearLayout vwParentRow = (LinearLayout)v.getParent();
		TextView id = (TextView)vwParentRow.findViewById(R.id.businessId);
		for(OrganizationDTO e : favOrgs){
			if(e != null && (e.getId() == Long.parseLong(id.getText().toString()))){
				if(dm.removeFavBusiness(e)){
					myBusinessAdapter.remove(e);
					myBusinessAdapter.notifyDataSetChanged();
				}
			}
		}
	}
	
	/**
	 * Closes the popup when close button is pressed.
	 * @param v
	 */
	public void onBusinessesCloseClicked(View v){
        popup.dismiss();
    }
	
	/**
	 * Closes the popup when close button is pressed.
	 * @param v
	 */
	public void onEventsCloseClicked(View v){
    	popup.dismiss();
    }
	
	/**
	 * handles button events from the map button. Creates a new map activity
	 * and centers it at the events location.
	 * @param v
	 */
	public void onEventsMap(View v){
        Intent map = new Intent(this, Mapper.class);
        if(curSelection != null){
        	map.putExtra("lat", curSelection.getLat());
        	map.putExtra("lon", curSelection.getLon());
        	startActivityIfNeeded(map, defaultIntentCode);
        }
	}
	
	/**
	 * Handles button events from the map button in Businesses 
	 * @param v
	 */
	public void onBusinessMap(View v){
        Intent map = new Intent(this, Mapper.class);
        if(curSelection != null){
        	map.putExtra("lat", curSelection.getLat());
        	map.putExtra("lon", curSelection.getLon());
        	startActivityIfNeeded(map, defaultIntentCode);
        }
	}
	
	/**
	 * Sets the UI depending on the state of the orgs radio buttons
	 * @param v
	 */
	public void favorgsRBClicked(View v){
		if (advancedSearchVisible){
			findViewById(R.id.favoritesEventsAdvancedSearch).setVisibility(View.GONE);
			findViewById(R.id.favoritesOrgsAdvancedSearch).setVisibility(View.VISIBLE);
			findViewById(R.id.favoritesAdvancedButtonExtra).setVisibility(View.GONE);
		}
		eventSearch = false;
	}
	
	/**
	 * Sets the UI depending on the state of the events radio buttons
	 * @param v
	 */
	public void faveventsRBClicked(View v){
		if (advancedSearchVisible){
			findViewById(R.id.favoritesEventsAdvancedSearch).setVisibility(View.VISIBLE);
			findViewById(R.id.favoritesOrgsAdvancedSearch).setVisibility(View.GONE);
			findViewById(R.id.favoritesAdvancedButtonExtra).setVisibility(View.GONE);
		}
		eventSearch = true;	
	}
	
	
	/*
     * Expand the search UI when the user clicks the advanced search 
     * button.
     */
    public void advancedButtonClicked(View v){
    	//Basic search view 
    	if(advancedSearchVisible){
    		findViewById(R.id.favoritesEventsAdvancedSearch).setVisibility(View.GONE);	
    		findViewById(R.id.favoritesOrgsAdvancedSearch).setVisibility(View.GONE);
    		findViewById(R.id.favoritesAdvancedButtonExtra).setVisibility(View.VISIBLE);
    		advancedSearchVisible = false;
    	}
    	
    	
    	//Advanced search view
    	else{
    		//Show Events Advanced Search 
    		if(eventsRB.isChecked() == true){
    			findViewById(R.id.favoritesEventsAdvancedSearch).setVisibility(View.VISIBLE);
    			findViewById(R.id.favoritesOrgsAdvancedSearch).setVisibility(View.GONE);
    			findViewById(R.id.favoritesAdvancedButtonExtra).setVisibility(View.GONE);
    		}
    			
    		//Show Organizations Advanced Search
    		if(orgsRB.isChecked() == true){
    			findViewById(R.id.favoritesEventsAdvancedSearch).setVisibility(View.GONE);
    			findViewById(R.id.favoritesOrgsAdvancedSearch).setVisibility(View.VISIBLE);
    			findViewById(R.id.favoritesAdvancedButtonExtra).setVisibility(View.GONE);
    		}
    		advancedSearchVisible = true;
    	}
   
    }
    
    /*
     * Search events using the info in the search UI
     */
    public void searchButtonClicked(View v){
    	if(eventSearch)
    	{
	    	EditText nameQuery = (EditText)findViewById(R.id.favoritesSearchText);
	    	EditText orgQuery = (EditText)findViewById(R.id.searchOrg);
	    	DatePicker startDate = (DatePicker)findViewById(R.id.searchStartDate);
	    	DatePicker endDate = (DatePicker)findViewById(R.id.searchEndDate);
	    	TimePicker eventTime = (TimePicker)findViewById(R.id.searchTime);
	    	String eventName = null;
	    	String orgName = null;
	    	Date start = new Date();
	    	Date end = new Date();
	    	//setting event name
	    	eventName = nameQuery.getText().toString();
	    	Log.i(TAG, "Event Name "+ eventName);
	    	//setting organization name
	    	orgName = orgQuery.getText().toString();
	    	Log.i(TAG, "Organization Name "+ orgName);
	    	//setting start date
	    	start.setYear(startDate.getYear()-1900);
	    	start.setMonth(startDate.getMonth());
	    	start.setDate(startDate.getDayOfMonth());
	    	start.setHours(eventTime.getCurrentHour());
	    	start.setMinutes(eventTime.getCurrentMinute());
	    	Log.i(TAG, "Start Date " + start.toLocaleString());
	    	//setting end date
	    	end.setYear(endDate.getYear()-1900);
	    	end.setMonth(endDate.getMonth());
	    	end.setDate(endDate.getDayOfMonth()+5);
	    	end.setHours(eventTime.getCurrentHour());
	    	end.setMinutes(eventTime.getCurrentMinute());
	    	Log.i(TAG, "End Date " + end.toLocaleString());
	    	
	    	//Only a basic search - set date to be 5 days after current date
	    	if (advancedSearchVisible == false){
	    		end.setDate(endDate.getDayOfMonth() + 2);
	    	}
	    	// do the search
	    	ArrayList<EventDTO> e = dm.searchFavEvents(eventName, orgName, start, end);
	    	Log.i(TAG, "SEARCH Result "+ e.size());
	    	
	    	// update the UI
	    	favEventsSearch = e;
			tabHost = (TabHost) this.findViewById(R.id.favoritestabhost);  // The activity TabHost
	    	tabHost.setCurrentTab(0);
	    	updateList(favEventsSearch);
	    	searchOn = true;
    	}
    	else
    	{
    		EditText nameQuery = (EditText)findViewById(R.id.favoritesSearchText);
        	EditText eventQuery = (EditText)findViewById(R.id.searchEvent);
        	
        	String orgName = null;
        	String eventName = null;

        	//setting organization name
        	orgName = nameQuery.getText().toString();
        	Log.i(TAG, "Organization Name "+ orgName);
        	//setting event name
        	eventName = eventQuery.getText().toString();
        	Log.i(TAG, "Event Name "+ eventName);
        	
        	// do the search
        	ArrayList<OrganizationDTO> o = dm.searchFavOrgs(orgName, eventName);
        	Log.i(TAG, "SEARCH Result "+ o.size());
        	
        	// update the UI
        	favOrgsSearch = o;
        	tabHost = (TabHost) this.findViewById(R.id.favoritestabhost);  // The activity TabHost
        	tabHost.setCurrentTab(1);
        	updateList(favOrgsSearch);
        	searchOn = true;
    	}
    }
    
    
	/*
	 * Updates the list view given an array of new list items 
	 *  (non-Javadoc)
	 * @see ICUP.App.ICUPListActivity#updateList(java.util.ArrayList)
	 */
	public <T extends ParentDTO> void updateList(ArrayList<T> newList) {
		if(tabHost.getCurrentTab() == 0)
		{
			myEventAdapter.clear();
			if(favEventsSearch != null && favEventsSearch.size() > 0){
				myEventAdapter.notifyDataSetChanged();
				for(int i = 0; i < favEventsSearch.size(); i++)
					myEventAdapter.add(favEventsSearch.get(i));
			}
			myEventAdapter.notifyDataSetChanged();
		}
		else if(tabHost.getCurrentTab() == 1)
		{
			myBusinessAdapter.clear();
			if(myBusinessAdapter != null && favOrgsSearch.size() > 0){
				myBusinessAdapter.notifyDataSetChanged();
				for(int i = 0; i < favOrgsSearch.size(); i++)
					myBusinessAdapter.add(favOrgsSearch.get(i));
			}
			myBusinessAdapter.notifyDataSetChanged();            	
		}
	}
	
	
	/**
	 * Updates the favorites orgs list. 
	 * @param newList
	 */
	public void updateOrgList(ArrayList<OrganizationDTO> newList) {
		myBusinessAdapter.clear();
		if (newList != null && newList.size() > 0) {
		for (int i = 0; i < newList.size(); i++)
			myBusinessAdapter.add(newList.get(i));
		}
		myBusinessAdapter.notifyDataSetChanged();
	}
	
	/**
	 * Updates the events list 
	 * @param newList
	 */
	public void updateEventList(ArrayList<EventDTO> newList) {
		myEventAdapter.clear();
		if (newList != null && newList.size() > 0) {
		for (int i = 0; i < newList.size(); i++)
			myEventAdapter.add(newList.get(i));
		}
		myEventAdapter.notifyDataSetChanged();
	}
}
