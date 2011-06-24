package ICUP.App;

import java.util.ArrayList;
import java.util.Date;

import org.codehaus.jackson.type.TypeReference;

import android.app.AlertDialog;
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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;

import com.blueduck.collegepedia.dto.EventDTO;

public class EventTabHost extends ICUPListActivity {
	private ArrayList<EventDTO> myEvents = null;
	private ArrayList<EventDTO> mySearchEvents = null;
	private Runnable viewEvents;
	private boolean advancedSearchVisible;
	private String cat = "";
	private final String extra = "category=";
	private final String firstArg = "?";
	private final String nArg = "&";
	private final String base = "http://174.136.5.67:8080/api/events.json";
	private String url = base;
	private EventDTO curSelection;
	private String limitString = "limit=";
	private int limit = 15;
	private boolean searchOn = false;
	private Date end = new Date();
	private Date start = new Date();
	private DatePicker endDate ;
	private TimePicker eventTime;
	//Debug w/ log file
	private static String TAG = "EventsTabHost";


	/**
	 * 	Runnable object to update the ListView via the adapter "myAdapter"
	 * Content generated from myEvents
	 * This also removes the context/pop-up window
	 */
	private Runnable returnRes = new Runnable()
	{
		public void run() {
			updateList(myEvents);
			TextView emptyView = (TextView) findViewById(android.R.id.empty);
			emptyView.setText("Could not get data from the server");
		}

	};

	/* 
	 * Creates a new EventTabHost. Sets up swiping and gesture listeners 
	 * Goes to the DB and server to populate lists. 
	 * (non-Javadoc)
	 * @see ICUP.App.ICUPListActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.eventhost);

		curSelection = null;

		// Get the category passed from EventsCat
		Bundle extras = getIntent().getExtras();
		cat = extras.getString("cat");


		TabHost tabHost = (TabHost) this.findViewById(R.id.eventstabhost);  // The activity TabHost
		doTabSetup(tabHost);
		tabHost.setCurrentTab(0);

		//Initialize the dates for searching to be 2 days apart
		DatePicker endDate = (DatePicker)findViewById(R.id.searchEndDate);
		TimePicker eventTime = (TimePicker)findViewById(R.id.searchTime);
		DatePicker startDate = (DatePicker)findViewById(R.id.searchStartDate);
		endDate = (DatePicker)findViewById(R.id.searchEndDate);
		eventTime = (TimePicker)findViewById(R.id.searchTime);

		//setting start date - starting it at 2010
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
		endDate.updateDate(end.getYear()+1900, end.getMonth(), end.getDate());



		// Initialize the list of event items
		myEvents = new ArrayList<EventDTO>();
		myAdapter = new EventAdapter(this, this, R.layout.eventitem, myEvents, EventAdapter.adapterType.EVENT);
		setListAdapter(myAdapter);

		// Create the viewEvents Runnable object.
		// Make it Runnable so it can be run again in case events want to be updated, etc. 
		viewEvents = new Runnable(){
			public void run() {
				getEvents();
			}
		};

		// Create and start the thread to get and load the Events
		thread =  new Thread(null, viewEvents, "EventsList");
		thread.start();

		// For swiping
		// You MUST set the intent to the left and right of this current activity/view
		// Forgetting to do so will cause the application to crash.
		Intent leftIntent = new Intent(this, FeaturedEvent.class);
		Intent rightIntent = new Intent(this, FavoritesTabHost.class);
		enableSwipe(leftIntent, rightIntent);


		// For swiping in complex views
		findViewById(R.id.eventstabhost).setOnTouchListener(gestureListener);
		tabHost.setOnTabChangedListener(new OnTabChangeListener(){
			public void onTabChanged(String tabId) {
				if (searchOn == true)
				{
					searchOn = false;
					updateList(myEvents);
				}
				getCurrentFocus().setOnTouchListener(gestureListener);
			}
		});

		
		// setup a custom onItemClickListener to handle click events o each list item
		this.getListView().setOnItemClickListener(new OnItemClickListener(){


			public void onItemClick(AdapterView<?> a, View v, int position,long id) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.eventpopup, null);

				int popupHeight = (int) (getApplicationContext().getResources().getDisplayMetrics().heightPixels) - 25;
				int popupWidth = (int) (getApplicationContext().getResources().getDisplayMetrics().widthPixels);

				TextView popupTitle = (TextView) v.findViewById(R.id.eventPopupTitle);
				TextView popupHostName = (TextView) v.findViewById(R.id.eventPopupHostName);
				TextView popupDesc = (TextView) v.findViewById(R.id.eventPopupDesc);
				TextView popupStartTime = (TextView) v.findViewById(R.id.eventPopupStartDatetime);
				TextView popupEndTime = (TextView) v.findViewById(R.id.eventPopupEndDatetime);
				TextView popupLocation = (TextView) v.findViewById(R.id.eventPopupLocation);
				TextView hiddenEventID = (TextView)v.findViewById(R.id.hiddenEventId);
				ImageView image = (ImageView)v.findViewById(R.id.eventPopupImage);

				EventDTO o = (EventDTO) getListView().getItemAtPosition(position);

				if(o.getId() == -1){ // got a request for more events
					if(!cat.equals("All")){
						// -2 because one event is the "load more. .." and size = indexed from 1 not 0
						url = base+firstArg + extra + cat+nArg+"offset="+(myEvents.size()-2)+nArg+limitString+limit;
						Log.i("Event Tab Host: ", "URL="+url);

					}
					else{
						url = base+firstArg +limitString+limit+nArg+"offset="+(myEvents.size()-2);
						Log.i("Event Tab Host1: ", "URL="+url);
						//Log.i("EVENTTABHOST: ", String.valueOf(myEvents.size()-2));
					}
					//myEvents.remove(myEvents.size()-1);
					TypeReference<ArrayList<EventDTO>> eventsRef = new TypeReference<ArrayList<EventDTO>>(){};
					myEvents.addAll( myEvents.size()-2, dm.getEventsList(url, eventsRef, cat, true));
					updateList(myEvents);  
					return;
				}
				if (popupTitle != null && o.getName() != null)
				{
					popupTitle.setText(o.getName());
				}
				if (popupHostName != null && o.getOrganizationName() != null)
				{
					popupHostName.setText(o.getOrganizationName() + " Presents: ");
				}
				if (popupDesc != null && o.getDescription() != null)
				{
					popupDesc.setText(o.getDescription().replace("\r", ""));
				}
				if (popupLocation != null && o.getLocation() != null)
				{
					popupLocation.setText(o.getLocation());
				}
				if (popupStartTime != null && o.getStartDatetime() != null)
				{
					popupStartTime.setText("Start Time: "+o.getStartDatetime().toLocaleString());
				}
				if (popupEndTime != null && o.getStopDatetime() != null)
				{
					popupEndTime.setText("End Time: "+ o.getStopDatetime().toLocaleString());
				}
				if (image != null && o.getImage() != null)
				{
					Bitmap pic;
					pic = BitmapFactory.decodeByteArray(o.getImage(), 0, o.getImage().length);
					image.setImageBitmap(pic);
				}
				if (hiddenEventID != null && o.getId() != null)
				{
					hiddenEventID.setText(o.getId().toString());
				}

				if(isOnline()){
					enableMapButton(v, true);
				}
				if(!isOnline()){
					enableMapButton(v,false);
				}
				popup = new PopupWindow(v, popupWidth, popupHeight, false);
				popup.setOutsideTouchable(false);
				popup.showAtLocation(findViewById(R.id.eventlisttab), Gravity.CENTER, 0, 0);
				curSelection = o;
			}

		}); 


		//disable the advanced search 
		findViewById(R.id.eventAdvancedSearch).setVisibility(View.GONE);
		advancedSearchVisible = false;
		this.setTitle("Event List - " + cat);
	}

	/*
	 * Handle events from the phones physical back button
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH)
		{
			TabHost tabHost = (TabHost) this.findViewById(R.id.eventstabhost);
			tabHost.setCurrentTab(1);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	/*
	 * Setup the tabs in the tabhost
	 */
	private void doTabSetup(TabHost tHost){
		Resources res = getResources(); // Resource object to get Drawables

		TabHost.TabSpec spec;  // Reusable TabSpec for each tab
		tHost.setup();

		// Create an Intent to launch an Activity for the tab (to be reused)
		spec = tHost.newTabSpec("eventslist");
		spec.setIndicator("Events List", res.getDrawable(R.drawable.events_tab));
		spec.setContent(R.id.eventlisttab);
		tHost.addTab(spec);

		spec = tHost.newTabSpec("search");
		spec.setIndicator("Search", res.getDrawable(R.drawable.search));
		spec.setContent(R.id.eventsearch);
		tHost.addTab(spec);

	}

	/*
	 * Get the multiple events and store them into the array myEvents.
	 * Loop through the events and ensure that nothing important is null
	 */
	private void getEvents()
	{

		// Local variables for checking for program-terminating errors.
		int i;  
		Date date = new java.util.Date();

		if(!cat.equals("All")){
			url += firstArg + extra + cat+nArg+limitString+limit;
		}
		else{
			url += firstArg +limitString+limit;
		}

		// Get the events list data from the server
		TypeReference<ArrayList<EventDTO>> eventsRef = new TypeReference<ArrayList<EventDTO>>(){};
		myEvents = dm.getEventsList(url, eventsRef, cat, false);
		//myEvents = HTTPConnectDummy.getMultipleObjects("events", eventsRef);
		//myEvents = mDbHelper.getDTOs("events", eventsRef);
		if(myEvents.isEmpty()){
			Log.i(TAG, "myEvents is null");
			runOnUiThread(returnRes);
			return;
		} 

		if(myEvents.size() == limit){
			EventDTO loadMore = new EventDTO();
			loadMore.setId(new Long(-1));
			loadMore.setDescription("Click To Load More Events");
			myEvents.add(loadMore);
		}

		// Put placeholders instead of null for any important information
		// This is IMPORTANT. It will crash the application if there is a null value in any of the elements of the Event item
		// See res/layout/eventitem.xml for details on what these elements are and/or change them.
		for (i = 0; i < myEvents.size(); i++)
		{
			if (myEvents.get(i) == null)
			{
				myEvents.get(i).setName("Unknown Event");
			}			
			if (myEvents.get(i).getDescription() == null)
			{
				myEvents.get(i).setDescription("No description");
			}
			if (myEvents.get(i).getStartDatetime() == null)
			{
				myEvents.get(i).setStartDatetime(date);
			}
		}

		// See returnRes at the top for more information on why this is called here
		runOnUiThread(returnRes);
	}

	/**
	 * Updates the content in the list by starting the thread to go 
	 * to the server and get the most recent info. 
	 */
	public void update(){
		TextView emptyView = (TextView) findViewById(android.R.id.empty);
		emptyView.setText("Loading Events...");

		myEvents.clear();
		myAdapter.notifyDataSetChanged();

		url = base;
		// Create and start the thread to get and load the Events
		Thread thread =  new Thread(null, viewEvents, "EventsList");
		thread.start();
	}


	/* 
	 * Handle onPauseEvents
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause(){
		super.onPause();
	}

	/*
	 *  For swiping after tab content has changed (mainly lists)
	 * @see android.app.ListActivity#onContentChanged()
	 */
	@Override
	public void onContentChanged()
	{
		super.onContentChanged();
		getListView().setOnTouchListener(gestureListener);
	}
	/*
	 * Handle adding events to the users favorites list
	 */
	public void addButtonClick(View v)
	{
		LinearLayout vwParentRow = (LinearLayout)v.getParent();
		TextView id = (TextView)vwParentRow.findViewById(R.id.eventId);
		TextView desc;        
		for(EventDTO e : myEvents){
			if(e != null && (e.getId() == Long.parseLong(id.getText().toString()))){
				AlertDialog.Builder adb = new AlertDialog.Builder(EventTabHost.this);  
				adb.setTitle("EventsList");
				boolean add = dm.addToFavorites(e);
				if(add ==  true){
					desc = (TextView)vwParentRow.findViewById(R.id.eventtitle);
					adb.setMessage("Added "+desc.getText()+" to favorites");  
					adb.setPositiveButton("Ok", null);
					adb.show();	
				}
				if (add == false){
					desc = (TextView)vwParentRow.findViewById(R.id.eventtitle);
					adb.setMessage(desc.getText()+" is already in favorites");  
					adb.setPositiveButton("Ok", null);
					adb.show();	
				}
			}
		}
	}

	/*
	 * Expand the search UI when the user clicks the advanced search 
	 * button.
	 */
	public void advancedButtonClicked(View v){
		if(advancedSearchVisible){
			findViewById(R.id.eventAdvancedSearch).setVisibility(View.GONE);	
			advancedSearchVisible = false;
		}
		else{
			findViewById(R.id.eventAdvancedSearch).setVisibility(View.VISIBLE);
			RadioButton categorySearch = (RadioButton)findViewById(R.id.currentCheck);
			if (cat.contentEquals("All"))
			{
				findViewById(R.id.allCheck).setVisibility(View.GONE);
			}
			else
			{
				findViewById(R.id.allCheck).setVisibility(View.VISIBLE);
				findViewById(R.id.currentCheck).setVisibility(View.VISIBLE);
			}
			categorySearch.setText(cat);
			advancedSearchVisible = true;
		}

	}

	/*
	 * Search events using the info in the search UI
	 */
	public void searchButtonClicked(View v){
		EditText nameQuery = (EditText)findViewById(R.id.eventSearchText);
		RadioButton checkCurrent = (RadioButton)findViewById(R.id.currentCheck);
		RadioButton checkAll = (RadioButton) findViewById(R.id.allCheck);
		EditText orgQuery = (EditText)findViewById(R.id.searchOrg);

		String eventName = null;
		String category = null;
		String orgName = null;
		//setting event name
		eventName = nameQuery.getText().toString();
		//setting category
		if (checkCurrent.isChecked()){
			category = cat;
		}
		if (checkAll.isChecked()){
			category = "All";
		}
		if((category.equals(new String("ALL"))) | (category.equals(new String("All"))))
		{
			category = null;
		}
		//setting organization name
		orgName = orgQuery.getText().toString();

		// Getting the times off of the input fields
		DatePicker endDate = (DatePicker)findViewById(R.id.searchEndDate);
		TimePicker eventTime = (TimePicker)findViewById(R.id.searchTime);
		DatePicker startDate = (DatePicker)findViewById(R.id.searchStartDate);
		endDate = (DatePicker)findViewById(R.id.searchEndDate);
		eventTime = (TimePicker)findViewById(R.id.searchTime);

		// getting start date
		start.setYear(startDate.getYear()-1900);
		start.setMonth(startDate.getMonth());
		start.setDate(startDate.getDayOfMonth());
		start.setHours(eventTime.getCurrentHour());
		start.setMinutes(eventTime.getCurrentMinute());
		// getting end date
		end.setYear(endDate.getYear()-1900);
		end.setMonth(endDate.getMonth());
		end.setDate(endDate.getDayOfMonth());
		end.setHours(eventTime.getCurrentHour());
		end.setMinutes(eventTime.getCurrentMinute());

		// Verify that this is a valid time period for search before searching
		if (start.after(end))
		{
			Toast.makeText(this, "The start time must be before the end time", Toast.LENGTH_SHORT).show();
		}
		else
		{
			//Only a basic search - set date to be 5 days after current date
			//    	if (advancedSearchVisible == false){
			//    		end.setDate(endDate.getDayOfMonth() + 2);
			//    	}
			// do the search
			ArrayList<EventDTO> e = dm.searchEvent(category, eventName, orgName, start, end);
			
			// update the UI
			mySearchEvents = e;
			updateList(mySearchEvents);
			TabHost tabHost = (TabHost) this.findViewById(R.id.eventstabhost);  // The activity TabHost
			tabHost.setCurrentTab(0);
			searchOn = true;
		}
	}

	/*
	 * Handle adding an event to favorites when the user is in the popup
	 */
	public void onAddToFavsClicked(View v){
		//get the popup view that was previously created in onItemClick()
		v = popup.getContentView();
		TextView eventID = (TextView)v.findViewById(R.id.hiddenEventId);

		for(int i = 0; i< myEvents.size() ; i++	){
			if(myEvents.get(i) != null && (myEvents.get(i).getId() == Long.parseLong(eventID.getText().toString()))){
				boolean add = dm.addToFavorites(myEvents.get(i));
				if(add ==  true){
					AlertDialog.Builder adb = new AlertDialog.Builder(EventTabHost.this);  
					adb.setMessage("Added "+myEvents.get(i).getName()+" to favorites");  
					adb.setPositiveButton("Ok", null);
					adb.show();    
				}
				if (add == false){
					AlertDialog.Builder adb = new AlertDialog.Builder(EventTabHost.this);  
					adb.setMessage(myEvents.get(i).getName()+" is already in favorites");  
					adb.setPositiveButton("Ok", null);
					adb.show();    	
				}
			}
		}
		popup.dismiss();


	}

	/*
	 * User closed the popup
	 */
	public void onEventsCloseClicked(View v){
		popup.dismiss();
	}

	/*
	 * Start the map activity when the map button is pressed
	 */
	public void onEventsMap(View v){
		Intent map = new Intent(this, Mapper.class);
		if(curSelection != null){
			map.putExtra("lat", curSelection.getLat());
			map.putExtra("lon", curSelection.getLon());
			startActivityIfNeeded(map, defaultIntentCode);
		}
	}

	/*
	 * Handle swipe events generated by the user and change categories 
	 * @see ICUP.App.ICUPListActivity#slideLeft(android.content.Intent)
	 */
	@Override
	public void slideLeft (Intent nextClass){
		int index = Categories.eventCategories.indexOf(cat);
		index = (index+1) % (Categories.eventCategories.size());
		if(index < 0 ){ 
			index = Categories.eventCategories.size() -1; 
		}

		cat = Categories.eventCategories.get(index);
		//Temporary place for this code, should be put in an updater function at some point
		// updates the category for the check box in advanced search
		RadioButton categorySearch = (RadioButton)findViewById(R.id.currentCheck);
		categorySearch.setText(cat);
		this.setTitle("Event List - " + cat);

		if(thread.isAlive()){
			thread.stop();

		}

		update();
	}

	/*
	 * Handle swipe events generated by the user and change categories 
	 * @see ICUP.App.ICUPListActivity#slideRight(android.content.Intent)
	 */
	@Override
	public void slideRight (Intent nextClass){
		int index = Categories.eventCategories.indexOf(cat);
		index = (index-1) % (Categories.eventCategories.size());
		if(index < 0 ){ 
			index = Categories.eventCategories.size() -1; 
		}

		cat = Categories.eventCategories.get(index);
		//Temporary place for this code, should be put in an updater function at some point
		// updates the category for the check box in advanced search
		RadioButton categorySearch = (RadioButton)findViewById(R.id.currentCheck);
		categorySearch.setText(cat);
		this.setTitle("Event List - " + cat);

		if(thread.isAlive()){
			thread.stop();

		}

		update();
	}

	/**
	 * Enables the map button ui once we have valid info 
	 * @param v
	 * @param enabled
	 */
	public void enableMapButton(View v, boolean enabled){
		ImageButton mapButton = (ImageButton)v.findViewById(R.id.mapButton);
		mapButton.setClickable(enabled);
		mapButton.setEnabled(enabled);
	}

}
