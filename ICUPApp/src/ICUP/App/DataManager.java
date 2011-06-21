package ICUP.App;

import java.io.IOException;
import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import android.net.ConnectivityManager;
import android.util.Log;
import android.content.Context;
//import android.database.sqlite.SQLiteDatabase;

import com.blueduck.collegepedia.dto.EventDTO;
import com.blueduck.collegepedia.dto.OrganizationDTO;


/**
 * 
 * @author ICUP
 */

/**
 * DataManager: Manages calls for ICUPDB and HTTPConnect
 * acts as a wrapper around all data fetching functionality. 
 */

public class DataManager {
	//DataManager data members 
	private static final String TAG = "DataManager";
	private final ICUPDb db;
	//private final SQLiteDatabase dbHolder;
	private Context ctx;
	
	/**
	 * Default constructor
	 * @param c
	 */
	public DataManager(Context c){
		db = new ICUPDb(c);
		//dbHolder = db.getmDb();
		ctx = c;
		//db.createUpdateTimeEvents(cat, 0);
	}
	
	/**
	 * Checks whether there is internet connectivity
	 * sets isOnline to true/false if there is/isn't internet connectivity 
	 * 
	 */
    public boolean isOnline() {
  	   ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
  	  if (cm.getActiveNetworkInfo() != null
  	            && cm.getActiveNetworkInfo().isAvailable()
  	            && cm.getActiveNetworkInfo().isConnected()) {
  	        return true;
  	    } else {
  	    	Log.i(TAG,"No internet connectivity");
  	        return false;
  	    }
    }
	
	/**
	 * Makes a call to the server to get the featured content and 
	 * inserts the featured content into the database
	 * @param url
	 * @return ParentDTO
	 */
    public EventDTO getFeaturedContent(String url){
    	
    	//Setting default featured DTO 
    	EventDTO featured = new EventDTO();
    	featured.setDescription("Sorry, could not connect to Lokalite's server"); 
    	
    	InputStream JSONString = null;
    
    	//Checking for internet connectivity 
    	if (!isOnline()){
    		return featured;
    	}
    	
    	
    	//Connecting to server
    	//Note: Catches IOExceptions and returns default featured DTO 
		try {
			JSONString = HTTPConnect.loadManySerialized(url);
			
			//Serializing JSON string from server to DTOs     	
			featured = HTTPConnect.getObject(JSONString, EventDTO.class);
		} catch (JsonParseException e) {
			Log.i(TAG, "ERROR: JSON string is not valid from server");
			e.printStackTrace();
			return featured;
		} catch (JsonMappingException e) {
			Log.i(TAG, "ERROR: Cannot parse JSON string to DTO");
			e.printStackTrace();
			return featured;
		} catch (IOException e) {
			Log.i(TAG, "ERROR: Cannot parse JSON string (IOException)");
			e.printStackTrace();
			return featured;
		}

		

		String featuredJSON = new String ("");
		ObjectMapper mapper = new ObjectMapper();
		try {
			featuredJSON = mapper.writeValueAsString(featured);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
		if (db.createEvent(featured, featuredJSON) < 0){
			Log.i(TAG, "Featured Content couldn't be added to the database");
		}
		

		return featured;	

    }
 
/**
 * Retrieves the Organization List from the server
 * and makes calls to add the Organization list to the database 
 * @param <T>
 * @param url
 * @param o
 * @return
 */
	public ArrayList<OrganizationDTO> getOrganizationsList(String cat, String url,TypeReference<ArrayList<OrganizationDTO>> o){
    	ArrayList<OrganizationDTO> t = new ArrayList<OrganizationDTO>();
    	InputStream JSONString = null;
    	String newUrl = new String(url.toString());
    	//Log.i(TAG, "url is " + url);
    	//Checking for Internet connectivity 
    	if (!isOnline()){
    		Log.i(TAG, "Cannot retrieve Event/Organization list from server");
    		t = db.fetchAllOrganizations(url);
    		Log.i(TAG, "Retrieving URL from the DB: " + url  );
    		return t;
    	}
    	Date updateTime = new Date();
    	Format formatter;
        formatter = new SimpleDateFormat("yyyyMMdd");
    	String ut;
    	ut = new String();
        ut = formatter.format(updateTime);
        ut+=("T");
        formatter = new SimpleDateFormat("HHmmss");
        ut += formatter.format(updateTime);

    	String lastUpdated = db.fetchUpdateTimeOrg(cat);
    	if ((lastUpdated == null) || (lastUpdated == ""))
    	{
        	//Log.i(TAG, "last updated was null ");
        	// check the format of the time
    		db.createUpdateTimeOrg(ut, cat);
    	}
    	else
    	{
        	//Log.i(TAG, "last updated was " + lastUpdated);
            String timeStamp = "&lastUpdated=";
            newUrl += timeStamp;
            newUrl += lastUpdated;
            // check the format of the time
    		db.createUpdateTimeOrg(ut, cat);
    	}
    	 
    	//Connecting to server
    	//Note: Catches IOExceptions and returns empty list
    	try {
    		if(!db.isEmpty(cat))
    		{
    			//The database has data for the category	
    			Log.i(TAG, "not Empty");
        		JSONString = HTTPConnect.loadManySerialized(newUrl);
        		//check if input stream is empty
        		//Log.i(TAG, "Available bytes = " + String.valueOf(JSONString.available()));
    			if(JSONString.available()==0){
    				Log.i(TAG, "Retrieving URL from the DB: " + url  );
    				t = db.fetchAllOrganizations(url);
    				JSONString.close();
    				return t;
    			}
	    		else
	    		{
	    			Log.i(TAG, "Fetching orgs from the server URL: " + url);
	    			//Serializing JSON string from server to DTOs
	    			t = HTTPConnect.getMultipleObjects(JSONString, o);
	    			//REFACTOR LATER
	    			if(t != null){
	    				db.createRows(t, JSONString);
	    			}
	    			JSONString.close();
	    		}
    		}
    		else
    		{
    			Log.i(TAG, "Empty");
        		JSONString = HTTPConnect.loadManySerialized(url);
    			Log.i(TAG, "Fetching orgs from the server (DB is empty)");
    			//Serializing JSON string from server to DTOs
    			t = HTTPConnect.getMultipleObjects(JSONString, o);
    			//REFACTOR LATER
    			if(t != null){
    				Log.i(TAG, "creating rows for businesses");
    				db.createRows(t, JSONString);
    			}
    			JSONString.close();
    		}

		} catch (JsonParseException e) {
			Log.i(TAG, "ERROR: JSON string is not valid from server");
			e.printStackTrace();
			
			t = db.fetchAllOrganizations(url);
			
			return t;
		} catch (JsonMappingException e) {
			Log.i(TAG, "ERROR: Cannot parse JSON string to DTO");
			e.printStackTrace();
			
			t = db.fetchAllOrganizations(url);
			
			return t;
		} catch (IOException e) {
			Log.i(TAG, "ERROR: Cannot parse JSON string (IOException)");
			e.printStackTrace();
			
			t = db.fetchAllOrganizations(url);
			
			return t;
		}
		return t;	
    }
    
    /**
     * First removes any old events from the database.
     * Then retrieves the event List from the server,
     * and makes calls to add the Events list to the database 
     * @param url
     * @param o
     * @return
     */
    		
    public ArrayList<EventDTO> getEventsList(String url,TypeReference<ArrayList<EventDTO>> o, String cat, boolean b){
    	//Calls updateDBEvents which makes a check to see if the app needs to go to the server.
    	updateDBEvents(url, o, cat, b);
    	db.databaseClean();
   		ArrayList<EventDTO> events = db.fetchAllEvents(url);
   		return events;
    }
    
    /**
     * Wrapper function for getFavEventDTOs()
     * Makes a call to the server to populate database with Events List
     * @return
     */
    public ArrayList<EventDTO> getFavEventList(){
    	
    	ArrayList<EventDTO> t = new ArrayList<EventDTO>();
    	
    	//Calls getFavEventDTOs to populate the database
    	//Note: Handles JsonParseException, JsonMappingException, & IOException 
		//		and returns empty list
    	try {
    		ArrayList<EventDTO> fe =db.getFavEventDTOs();
    		
			return fe;
		} catch (JsonParseException e) {
			Log.i(TAG, "ERROR: JSON string is not valid from server");
			e.printStackTrace();
			
			return t;
		} catch (JsonMappingException e) {
			Log.i(TAG, "ERROR: Cannot parse JSON string to DTO");
			e.printStackTrace();
			
			return t;
		} catch (IOException e) {
			Log.i(TAG, "ERROR: Cannot parse JSON string (IOException)");
			e.printStackTrace();
			
			return t;
		}
    }
    
    /**
     * Wrapper function for getFavOrganizationDTOs() 
     * Makes a call to the server to populate database with Organization List
     * @return
     */
    public ArrayList<OrganizationDTO> getFavOrganizationList(){
    	
    	ArrayList<OrganizationDTO> t = new ArrayList<OrganizationDTO>();
    	
    	//Calls getFavOrganizationDTOs() to populate the database
    	//Note: Handles JsonParseException, JsonMappingException, & IOException 
		//		and returns empty list
    	try {
    		ArrayList<OrganizationDTO> orgs = db.getFavOrganizationDTOs();
			
    		return orgs;
		} catch (JsonParseException e) {
			Log.i(TAG, "ERROR: JSON string is not valid from server");
			e.printStackTrace();
			
			return t;
		} catch (JsonMappingException e) {
			Log.i(TAG, "ERROR: Cannot parse JSON string to DTO");
			e.printStackTrace();
			
			return t;
		} catch (IOException e) {
			Log.i(TAG, "ERROR: Cannot parse JSON string (IOException)");
			e.printStackTrace();
			
			return t;
		}
		
    }
    
    /**
     * Wrapper function: adds an organization to the favorites table in the database
     * @param OrganizationDTO  
     * @return boolean 
     */
    public boolean addToFavorites(OrganizationDTO b){
    	
    	boolean dbResult = db.createFavOrganization(b);
    	if(!dbResult){
    		//Log.i(TAG, "Org is already in Favorites");
    		
    		return false;
    	}
    	else{
    		//Log.i(TAG, "Organization is in Favorites");
    		
    		return true;
    	}
    }
    
    /**
     * Wrapper function: adds an event to the favorites table in the database 
     * @param EventDTO
     * @return boolean
     */
    public boolean addToFavorites(EventDTO e){
    	
    	boolean dbResult = db.createFavEvent(e);
    	if(!dbResult){
    		Log.i(TAG, "Event is already in Favorites");
    		
    		return false;
    	}
    	else{
    		Log.i(TAG, "Event added to Favorites");
    		
    		return true;
    	}
    }

    /**
     * Wrapper function: removes an event from the favorites table in the database
     * @param EventDTO
     * @return boolean
     */
	public boolean removeFavEvent(EventDTO e) {
		
		if (!db.dbRemoveFavEvent(e)){
			Log.i(TAG, "ERROR: Deletion of event from database failed");
			
			return false;
    	}
    	else{
    		Log.i(TAG, "Event is removed from Favorites");
    		
    		return true;
		}
	}

	/**
	 * Wrapper function: removes an organization from the favorites table in the database
	 * @param OrganizationDTO
	 * @return boolean
	 */
	public boolean removeFavBusiness(OrganizationDTO e) {
		
		if (!db.dbRemoveFavOrganization(e)){
			Log.i(TAG, "ERROR: Deletion of organization from database failed");
    		
			return false;
    	}
    	else{
    		Log.i(TAG, "Organization is removed from Favorites");
    		
    		return true;
		}
	}
	
	/**
	 * Makes a call to the server to search for Events given criteria
	 * Returns a list of the Events searched 
	 * @param String category, String name
	 * @return ArrayList<EventDTO>
	 */
    public ArrayList<EventDTO> searchEvent(String category, String name, String orgName, Date start, Date end){
    	
    	ArrayList<EventDTO> e = new ArrayList<EventDTO>();
    	//check for null or other error from the server
    	//separate method for all
    	
    	//Checking for internet connectivity 
    	if (!isOnline()){
    		e = db.searchEvents(category,name,orgName, start, end);
    		
    		return e;
    	}
    	
    	// Get the events list data from the server
    	//TypeReference<ArrayList<EventDTO>> eventsRef = new TypeReference<ArrayList<EventDTO>>(){};
		//e = getDTOs("http://174.136.5.67:8080/collegepedia/api/events.json?category="+category+"&name="+name, eventsRef);
		//e = getEventsList("http://174.136.5.67:8080/collegepedia/api/events.json?category="+category, eventsRef);
		e = db.searchEvents(category,name,orgName, start, end);
    	
		return e;
    }
    
    /**
	 * Makes a call to the server to search for Organizations given criteria 
	 * Returns a list of the Organizations searched 
	 * @param String category, String name
	 * @return ArrayList<OrganizationDTO>
	 */
    public ArrayList<OrganizationDTO> searchOrganization(String category, String name, String eventName){
    	
    	ArrayList<OrganizationDTO> o = new ArrayList<OrganizationDTO>();
    	//check for null or other error from the server
    	//separate method for all
    	
    	//Checking for internet connectivity 
    	if (!isOnline()){
    		o = db.searchOrgs(category, name, eventName);
    		
    		return o;
    	}
    	
    	// Get the events list data from the server
    	//TypeReference<ArrayList<OrganizationDTO>> orgsRef = new TypeReference<ArrayList<OrganizationDTO>>(){};
		//o = getDTOs("http://174.136.5.67:8080/collegepedia/api/organizations.json?category="+category+"&name="+name, eventsRef);
		//o = getOrganizationsList("http://174.136.5.67:8080/collegepedia/api/organizations.json?category="+category, orgsRef);
		o = db.searchOrgs(category, name, eventName);
		
    	return o;
    }
    
	/**
	 * Returns a list of the favorite events searched 
	 * @param String category, String name
	 * @return ArrayList<EventDTO>
	 */
    public ArrayList<EventDTO> searchFavEvents(String name, String orgName, Date start, Date end){
    	
    	ArrayList<EventDTO> e = new ArrayList<EventDTO>();
    	e = db.searchFavEvents(name,orgName, start, end);
    	return e;
    }

    /**
	 * Returns a list of the favorite organizations searched 
	 * @param String category, String name
	 * @return ArrayList<OrganizationDTO>
	 */
    public ArrayList<OrganizationDTO> searchFavOrgs(String name, String eventName){
    	
    	ArrayList<OrganizationDTO> o = new ArrayList<OrganizationDTO>();
    	o = db.searchFavOrgs(name, eventName);
    	return o;
    }
    
    /**
     * Updates the Event table in the database with the content from the server 
     * only if it's been at least 30 minutes since the last time the table has been updated
     * @param url
     * @param o
     */
    public void updateDBEvents(String url, TypeReference<ArrayList<EventDTO>> o, String cat, boolean goToServer){
    	ArrayList<EventDTO> t = new ArrayList<EventDTO>();
    	InputStream JSONString = null;
    	Date currentTime = new Date();
    	long storedTime = db.fetchUpdateTimeEvents(cat);
    	
    	Log.i(TAG, "DataManager: storedTime in DB " + String.valueOf(storedTime));
    	Log.i(TAG, "DataManager: time difference " + String.valueOf(currentTime.getTime() - storedTime));
    	
		//Go to server and update DB if it has been 5 minutes
		if (storedTime == 0 || (currentTime.getTime() - storedTime) >= 1800000 || goToServer == true){
			Log.i(TAG, "DataManager: going to server");
		
			//Go to server and update DB if it has been 5 minutes	
        	try{	
    	    	//Get the DTO's from the server
        		JSONString = HTTPConnect.loadManySerialized(url);
    				
    			//Serializing JSON string from server to DTOs 		
    			t = HTTPConnect.getMultipleObjects(JSONString, o);  
    			
    			//Inserting DTOs into database
    			if(t != null){
    				long row_id = db.createRows(t, JSONString);
    			}
    			JSONString.close();
    		} 
        	catch (JsonParseException e) {
    			Log.i(TAG, "ERROR: JSON string is not valid from server");
    			e.printStackTrace();
    			t = db.fetchAllEvents(url);
    		} catch (JsonMappingException e) {
    			Log.i(TAG, "ERROR: Cannot parse JSON string to DTO");
    			e.printStackTrace();
    			t = db.fetchAllEvents(url);
    		} catch (IOException e) {
    			Log.i(TAG, "ERROR: Cannot parse JSON string (IOException)");
    			e.printStackTrace();
    			t = db.fetchAllEvents(url);
    		}
    		
    		if(t != null){
    			long row_id = db.createRows(t, JSONString);
    		}
    		//update the stored time in db
    		db.createUpdateTimeEvents(cat, currentTime.getTime());
    	}
    }
    
    
    /**
     * wrapper around db close. Makes sure we don't double 
     * close or double open the DB
     */
    public void closeDB(){
    	db.myClose();
    }
    
    /**
     * wrapper around db close. Makes sure we don't double 
     * close or double open the DB
     */
    public void openDB(){
    	db.myOpen();
    }


}
