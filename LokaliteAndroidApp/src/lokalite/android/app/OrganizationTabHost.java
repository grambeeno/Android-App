package lokalite.android.app;


import java.util.ArrayList;

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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.blueduck.collegepedia.dto.OrganizationDTO;

public class OrganizationTabHost extends LokaliteListActivity {
	
	//ArrayLists to hold businesses and search results 
    private ArrayList<OrganizationDTO> myBusinesses = null;
    private ArrayList<OrganizationDTO> mySearchBusinesses = null;
    
    private Runnable viewBusinesses;
    private boolean advancedSearchVisible;
    private final String extra = "?category=";
//    private final String timeStamp = "&lastUpdated=";
    private String cat = "";
    private final String base = "http://174.136.5.67:8080/collegepedia/api/organizations.json";
    private String url = base;
    private boolean searchOn = false;

    private OrganizationDTO curSelection;
       
    private static String TAG = "BusinessTabHost";
    
    
    /*
     *      Runnable object to update the ListView via the adapter "myAdapter"
     *		Content generated from myBusinesses
     *		This also removes the context/pop-up window
     */

    /**
     * Thread to load businesses from server 
     */
    private Runnable returnRes = new Runnable()
    {
        public void run() {
        	updateList(myBusinesses);
        	TextView emptyView = (TextView) findViewById(android.R.id.empty);
    	    emptyView.setText("Could not get data from the server");
        }
    };

    /* 
     * Creates a new BusinessTabHost including swiping and fetching data 
     * from the server. 
     * (non-Javadoc)
     * @see lokalite.android.app.LokaliteListActivity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.businesshost);
        
        // Set the title layout and text
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.lokaliteheader);
        lokaliteTitle = (TextView) findViewById(R.id.lokalitetitle);
        this.lokaliteTitle.setText("Organizations");
        
        Bundle extras = getIntent().getExtras();
        
        // Get the category passed from BusinessCat
        cat = extras.getString("cat");
          
        TabHost tabHost = (TabHost) this.findViewById(R.id.businesstabhost);  // The activity TabHost
        doTabSetup(tabHost);

        tabHost.setCurrentTab(0);
     
        // Initialize the list of business items
        myBusinesses = new ArrayList<OrganizationDTO>();
        myAdapter = new OrganizationAdapter(this, this, R.layout.businessitem, myBusinesses, false);
        setListAdapter(myAdapter);
        // Create the viewEvents Runnable object.
        // Make it Runnable so it can be run again in case events want to be updated, etc. 
        viewBusinesses = new Runnable(){
            public void run() {
                getBusinesses();
            }
        };
        
        // Create and start the thread to get and load the Events
        
        thread =  new Thread(null, viewBusinesses, "OrganizationsList");
        thread.start();
        
        // For swiping
        // You MUST set the intent to the left and right of this current activity/view
        // Forgetting to do so will cause the application to crash.
        Intent leftIntent = new Intent(this, FavoritesTabHost.class);
        Intent rightIntent = new Intent(this, EventsCat.class);
        enableSwipe(leftIntent, rightIntent);
        
        
        
        // For swiping in complex views
        findViewById(R.id.businesstabhost).setOnTouchListener(gestureListener);
        tabHost.setOnTabChangedListener(new OnTabChangeListener(){
            public void onTabChanged(String tabId) {
            	if (searchOn == true)
        		{
        			searchOn = false;
        			updateList(myBusinesses);
        		}
                getCurrentFocus().setOnTouchListener(gestureListener);
            }
        });
        
        // Handle business profile clicks
        this.getListView().setOnItemClickListener(new OnItemClickListener() {
            
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.businesspopup, null);
                
                int popupHeight = (int) (getApplicationContext().getResources().getDisplayMetrics().heightPixels) - 25;
                int popupWidth = (int) (getApplicationContext().getResources().getDisplayMetrics().widthPixels);
                
                TextView popupName = (TextView) v.findViewById(R.id.businessPopupName);
                TextView popupDesc = (TextView) v.findViewById(R.id.businessPopupDesc);
                TextView popupLocation = (TextView) v.findViewById(R.id.businessPopupLocation);
                TextView hiddenBusinessID = (TextView)v.findViewById(R.id.hiddenBusinessId);
                ImageView image = (ImageView)v.findViewById(R.id.businessPopupImage);
                OrganizationDTO o = (OrganizationDTO) getListView().getItemAtPosition(position);
                curSelection = o;

                if (popupName != null && o.getName() != null)
                {
                    popupName.setText(o.getName());
                }
                if (popupDesc != null && o.getDescription() != null)
                {
                    popupDesc.setText(o.getDescription().replace("\r", ""));
                }
                if (popupLocation != null && o.getAddress() != null)
                {
                    popupLocation.setText(o.getAddress());
                }
                if (image != null && o.getImage() != null)
                {
                	Bitmap pic;
            		pic = BitmapFactory.decodeByteArray(o.getImage(), 0, o.getImage().length);
            		image.setImageBitmap(pic);
                }
        		if (hiddenBusinessID != null && o.getId() != null)
        		{
        			hiddenBusinessID.setText(o.getId().toString());
        		}
        		if(isOnline()){
        			enableMapButton(v, true);
        		}
        		if(!isOnline()){
        			enableMapButton(v, false);
        		}
                popup = new PopupWindow(v, popupWidth, popupHeight, false);
                popup.setOutsideTouchable(false);
                
                popup.showAtLocation(findViewById(R.id.businessdirtab), Gravity.CENTER, 0, 0);
                
            }
        });
        
        //disable the advanced search layout
        findViewById(R.id.businessAdvancedSearch).setVisibility(View.GONE);
        advancedSearchVisible = false;
  	   	this.setTitle("Organization List - " + cat);
    }
    
    /* 
     * 
     * (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause(){
    	super.onPause();
    }
    
    /*
     * Set up the tabs in the tab host. Loads the correct view for each tab
     */
	private void doTabSetup(TabHost tHost){
        Resources res = getResources(); // Resource object to get Drawables
        
        TabHost.TabSpec spec;  // Reusable TabSpec for each tab
        tHost.setup();
        
        // Set the tabs to change views when clicked
        spec = tHost.newTabSpec("businessdir");
        spec.setIndicator("Organization Directory", res.getDrawable(R.drawable.orgs_tab));
        spec.setContent(R.id.businessdirtab);
        tHost.addTab(spec);
        
        spec = tHost.newTabSpec("search");
        spec.setIndicator("Search", res.getDrawable(R.drawable.search));
        spec.setContent(R.id.businesssearch);
        tHost.addTab(spec);	
		
	}
	
    /*
     * Catches events for the physical back button on the phone
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_SEARCH)
		{
    		TabHost tabHost = (TabHost) this.findViewById(R.id.businesstabhost);
    		tabHost.setCurrentTab(1);
    		return true;
		}

    	return super.onKeyDown(keyCode, event);
    }
    
    /* 
     * Handles content changed events such as the phone rotating or keyboard events
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
     * Handle button events from the advanced search button.
     * Enables the rest of the search UI.
     * @param v
     */
    public void businessAdvancedButtonClicked(View v){
        if(advancedSearchVisible){
            findViewById(R.id.businessAdvancedSearch).setVisibility(View.GONE);
            advancedSearchVisible = false;
        }
        else{
            findViewById(R.id.businessAdvancedSearch).setVisibility(View.VISIBLE);
    		RadioButton categorySearch = (RadioButton)findViewById(R.id.currentOrgCheck);
    		if (cat.contentEquals("All"))
    		{
    			findViewById(R.id.allCheck).setVisibility(View.GONE);
    		}
    		else
    		{
    			findViewById(R.id.allCheck).setVisibility(View.VISIBLE);
    			findViewById(R.id.currentOrgCheck).setVisibility(View.VISIBLE);
    		}
    		categorySearch.setText(cat);
            advancedSearchVisible = true;
        }
    }
    
    /**
     * Handle button events from search button. 
     * Issues a request to data manager to do searching on the TB and server. 
     * @param v
     */
    public void businessSearchButtonClicked(View v){
    	EditText nameQuery = (EditText)findViewById(R.id.businessSearchText);
    	RadioButton checkCurrent = (RadioButton)findViewById(R.id.currentOrgCheck);
    	RadioButton checkAll = (RadioButton) findViewById(R.id.allCheck);
    	EditText eventQuery = (EditText)findViewById(R.id.eventSearchText);
    	
    	String orgName = null;
    	String category = null;
    	String eventName = null;

    	//setting organization name
    	orgName = nameQuery.getText().toString();
    	Log.i(TAG, "Organization Name "+ orgName);
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
    	Log.i(TAG, "Category is " + category);
    	//setting event name
    	eventName = eventQuery.getText().toString();
    	Log.i(TAG, "Event Name "+ eventName);
    	
    	// do the search
    	ArrayList<OrganizationDTO> o = dm.searchOrganization(category, orgName, eventName);
    	Log.i(TAG, "SEARCH Result "+ o.size());
    	
    	// update the UI
    	mySearchBusinesses = o;
    	updateList(mySearchBusinesses);
    	TabHost tabHost = (TabHost) this.findViewById(R.id.businesstabhost);  // The activity TabHost
    	tabHost.setCurrentTab(0);
    	searchOn = true;
    }
    
    /*
     * Get the multiple events and store them into the array myEvents.
     * Loop through the events and ensure that nothing important is null
     */
    private void getBusinesses()
    {
        int i;
        
        // Get the events list data from the server
        TypeReference<ArrayList<OrganizationDTO>> businessRef = new TypeReference<ArrayList<OrganizationDTO>>(){};
       
        if(!cat.equals("All")){
        	url += extra + cat;
        }
 
        myBusinesses = dm.getOrganizationsList(cat, url, businessRef);
        
        if(myBusinesses.isEmpty()){
              Log.i(TAG, "myBusinesses is null");
              runOnUiThread(returnRes);
            return;
        } 
        
        // Put placeholders instead of null for any important information
        // This is IMPORTANT. It will crash the application if there is a null value in any of the elements of the Event item
        // See res/layout/eventitem.xml for details on what these elements are and/or change them.
        for (i = 0; i < myBusinesses.size(); i++)
        {
            if (myBusinesses.get(i).getName() == null)
            {
                myBusinesses.get(i).setName("Unknown Name");
            }            
            if (myBusinesses.get(i).getDescription() == null)
            {
                myBusinesses.get(i).setDescription("No description");
            }
            if (myBusinesses.get(i).getId() == null)
            {
                myBusinesses.get(i).setId(new Long(1));
            }
        }
               
        // See returnRes at the top for more information on why this is called here
        runOnUiThread(returnRes);
    }
    
    /*
     * Handle adding organizations to the users favorites list.
     */
    public void addButtonClick(View v)
    {
        
        LinearLayout vwParentRow = (LinearLayout)v.getParent(); 
        TextView id = (TextView)vwParentRow.findViewById(R.id.businessId);
        TextView desc;  
        
        for(OrganizationDTO e : myBusinesses){
      
            if(e != null && (e.getId() == Long.parseLong(id.getText().toString()))){
                AlertDialog.Builder adb = new AlertDialog.Builder(OrganizationTabHost.this);  
                adb.setTitle("OrganizationsList");
                boolean add = dm.addToFavorites(e);
                if(add ==  true){
                	 desc = (TextView)vwParentRow.findViewById(R.id.businessname);
                     adb.setMessage("Added "+desc.getText()+" to favorites");  
                     adb.setPositiveButton("Ok", null);
                     adb.show();    
                }
                if (add == false){
    				desc = (TextView)vwParentRow.findViewById(R.id.businessname);
    				adb.setMessage(desc.getText()+" is already in favorites");  
    				adb.setPositiveButton("Ok", null);
    				adb.show();	
    			}
                
            }
        }
    }
    
    /*
     * Handle add to favorites events generated while in a popup
     */
    public void onBusinessAddToFavs(View v){
        v = popup.getContentView();
        TextView businessID = (TextView)v.findViewById(R.id.hiddenBusinessId);
      
        for(int i = 0; i< myBusinesses.size() ; i++    ){
            if(myBusinesses.get(i) != null && (myBusinesses.get(i).getId() == Long.parseLong(businessID.getText().toString()))){
                boolean add = dm.addToFavorites(myBusinesses.get(i));
                if(add ==  true){
                	AlertDialog.Builder adb = new AlertDialog.Builder(OrganizationTabHost.this);  
                	adb.setMessage("Added "+myBusinesses.get(i).getName()+" to favorites");  
                	adb.setPositiveButton("Ok", null);
                	adb.show(); 
                }
                else if (add == false){
                	AlertDialog.Builder adb = new AlertDialog.Builder(OrganizationTabHost.this);  
                    adb.setMessage(myBusinesses.get(i).getName()+" is already in favorites");  
                    adb.setPositiveButton("Ok", null);
                    adb.show();    	
                }
            }
        }
        
        popup.dismiss();
    }
    
    /*
     * Closes the popup 
     */
    public void onBusinessesCloseClicked(View v){
        popup.dismiss();
    }
    
    /*
     * Start the map activity when the user wants to map the location of an event
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
     * Update the content of the list by going to the datamanager. 
     */
    public void update(){
        TextView emptyView = (TextView) findViewById(android.R.id.empty);
 	   emptyView.setText("Loading Organizations...");
 	    
 	  myBusinesses.clear();
  	   myAdapter.notifyDataSetChanged();
  	   
  	   url = base;
        // Create and start the thread to get and load the Events
        Thread thread =  new Thread(null, viewBusinesses, "OrganizationsList");
        thread.start();
     }
    
    /*
     * Overridden method that handles swipe events from the user
     */
    @Override 
    public void slideLeft (Intent nextClass){
 	   int index = Categories.businessCategories.indexOf(cat);
 	   index = (index+1) % (Categories.businessCategories.size());
 	   if(index < 0 ){ 
 		   index = Categories.businessCategories.size() -1; 
 	   }
 	   
 	   cat = Categories.businessCategories.get(index);
 	   this.setTitle("Organization List - " + cat);
 	   if(thread.isAlive()){
 		   thread.stop();
 	   }
 	   update();
    }
        
    /*
     * Overridden method that handles swipe events from the user
     */
    @Override
    public void slideRight (Intent nextClass){
 	   int index = Categories.businessCategories.indexOf(cat);
 	   index = (index-1) % (Categories.businessCategories.size());
 	   if(index < 0 ){ 
 		   index = Categories.businessCategories.size() -1; 
 	   }
 	   
 	   cat = Categories.businessCategories.get(index);
 	   this.setTitle("Organization List - " + cat);
 	   
 	   if(thread.isAlive()){
 		   thread.stop();
 	   }
 	   
 	   update();
    }
    
    /**
     * Enable the map button if we have valid data. 
     * @param v
     * @param enabled
     */
    public void enableMapButton(View v, boolean enabled){
		ImageButton mapButton = (ImageButton)v.findViewById(R.id.mapButton);
		mapButton.setClickable(enabled);
		mapButton.setEnabled(enabled);
    }
}