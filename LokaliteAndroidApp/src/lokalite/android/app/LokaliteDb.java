package lokalite.android.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.blueduck.collegepedia.dto.EventDTO;
import com.blueduck.collegepedia.dto.OrganizationDTO;
import com.blueduck.collegepedia.dto.ParentDTO;


public class LokaliteDb extends SQLiteOpenHelper {
	public static final String KEY_ID = "id";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_JSON_DTO = "dto_string";
    public static final String KEY_NAME = "name";
    public static final String KEY_BUSINESS_ID = "business_id";
    public static final String KEY_BUSINESS_NAME = "business_name";
    public static final String KEY_START_DATE = "start_date";
    public static final String KEY_END_DATE = "end_date";
    public static final String KEY_ACCESS_DATE = "access_date";
	public static final String KEY_LOCATION = "location";
	public static final String KEY_LON = "longitude";
	public static final String KEY_LAT = "latitude";
	public static final String KEY_CATEGORY = "category";
	public static final String KEY_BUSINESS_UPDATE_TIME = "business_update_time";
	public static final String KEY_EVENTS_UPDATE_TIME = "events_update_time";
	public static final String KEY_FEATURED_UPDATE_TIME = "featured_update_time";
	

    private static final String TAG = "LokaliteDb";
    private static SQLiteDatabase mDb;

    /**
     * Table creation statements
     */
    private static final String TABLE_CREATE_BUSINESS =
    	"create table business (_id integer primary key autoincrement, "
        + "name text not null, longitude text not null, latitude text not null," 
        + " category text not null, dto_string string not null, id text not null);";
    
    private static final String TABLE_CREATE_EVENTS =
    	"create table events (_id integer primary key autoincrement, "
        + "business_name text not null, name text not null," 
        + "start_date bigint not null, end_date bigint not null,"
        + "longitude text not null, latitude text not null," 
		+ " category text not null, dto_string text not null, id text not null);";
    
    private static final String TABLE_CREATE_FEATURED =
    	"create table featured (_id integer primary key autoincrement, access_date text not null,"
    	+ "dto_string text not null);";
    
    private static final String TABLE_CREATE_FAVBUSINESS =
    	"create table favbusiness (_id integer primary key autoincrement,"
    	+ "dto_string text not null, name text not null," 
    	+ "id text not null);";
    
    private static final String TABLE_CREATE_FAVEVENTS =
    	"create table favevents (_id integer primary key autoincrement,"
    	+ "dto_string text not null, id text not null,"
    	+ "business_name text not null, name text not null," 
    	+ "start_date bigint not null, end_date bigint not null);";
    
    private static final String TABLE_CREATE_UPDATEEVENTS =
    	"create table updateevents (_id integer primary key autoincrement, "
        + "events_update_time bigint not null,"
        + "category text not null);";
    
    private static final String TABLE_CREATE_UPDATEORG =
    	"create table updateorg (_id integer primary key autoincrement, "
        + "business_update_time bigint not null,"
        + "category text not null);";
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        TABLE_CREATE_EVENTS +";"+ TABLE_CREATE_BUSINESS + ";" + TABLE_CREATE_FEATURED + ";" + TABLE_CREATE_FAVBUSINESS + ";"
        + TABLE_CREATE_FAVEVENTS + ";"+ TABLE_CREATE_UPDATEEVENTS + ";" + TABLE_CREATE_UPDATEORG + ";" ;

    private static final String DATABASE_NAME = "data";
    private static final String TABLE_EVENTS = "events";
    private static final String TABLE_BUSINESS = "business";
    private static final String TABLE_FEATURED = "featured";
    private static final String TABLE_FAVBUSINESS = "favBusiness";
    private static final String TABLE_FAVEVENTS = "favEvents";
    private static final String TABLE_UPDATEEVENTS = "updateevents";
    private static final String TABLE_UPDATEORG = "updateorg";
    private static final int DATABASE_VERSION = 114;

   
    private final Context mCtx;

        @Override
        /**
         * Called by the Android OS when LokaliteDb is created
         * Uses the constants defined above to create new tables
         * any changes needed to the DB will require updating the DATABASE_VERSION
         */
        public void onCreate(SQLiteDatabase db) {

            try{
            	db.execSQL(TABLE_CREATE_EVENTS);
            	db.execSQL(TABLE_CREATE_BUSINESS);
            	db.execSQL(TABLE_CREATE_FEATURED);
            	db.execSQL(TABLE_CREATE_FAVBUSINESS);
            	db.execSQL(TABLE_CREATE_FAVEVENTS);
            	db.execSQL(TABLE_CREATE_UPDATEEVENTS);
            	db.execSQL(TABLE_CREATE_UPDATEORG);
            	Log.i(TAG, "All Tables Created");

            	}
            catch (SQLException e){
            	Log.i(TAG, "DB Create failed");
            }
        }

        @Override
        /**
         * This Method is called when the DATABASE_VERSION constant 
         * has been incremented. All tables will be dropped, along with the
         * data in them.
         */
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from  " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS events");
            db.execSQL("DROP TABLE IF EXISTS business");
            db.execSQL("DROP TABLE IF EXISTS featured");
            db.execSQL("DROP TABLE IF EXISTS favBusiness");
            db.execSQL("DROP TABLE IF EXISTS favEvents");
            db.execSQL("DROP TABLE IF EXISTS updateevents");
            db.execSQL("DROP TABLE IF EXISTS updateorg");
            onCreate(db);
        }
    

    public LokaliteDb(Context ctx) {
    	
    	super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
          
        try{open();} //Opens or creates the database  
        catch(Exception e1)
        {
      	  Log.i(TAG, "DB open EXCEPTION");  
      	  e1.printStackTrace();  
        }

    	this.mCtx = ctx;
    }

    /**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * @return 
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public void open() throws SQLException {
        setmDb(getWritableDatabase());

    }
    /**
     * Close the database.
     */
    public void close() {
       getmDb().close();
    }
    
    /**
 	* Open the database only after checking that the database has not been previously 
 	* opened.  
 	*/ 
 	public void myOpen() { 
	 	if(getmDb().isOpen()){//if the database is already open do nothing 
	 		return; 
	 	} 
	 	else{//if the database is not open then open it 
	 		open(); 
	 		return; 
	 	} 
 	} 
 	/** 
 	* Close the database only after checking that the database has not been previously 
 	* closed.  
 	*/ 
 	public void myClose(){ 
	 	if(getmDb().isOpen()){//if the db is open then you can close it 
	 		close(); 
	 	} 
	 	else{//if the db is already closed do nothing 
	 		return; 
	 	} 
 	} 
 	/**
 	 *  Gets the favored events from the database. passing null for the search param returns the entire table.  
 	 * @return ArrayList<EventDTO> t 
 	 * @throws JsonParseException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 */
    public ArrayList<EventDTO> getFavEventDTOs() throws JsonParseException, JsonMappingException, IOException{
    	
    	ArrayList<EventDTO> t = new ArrayList<EventDTO>();
        Cursor mCursor =
            getmDb().query(false, TABLE_FAVEVENTS, new String[] {KEY_JSON_DTO}, null, null,
                    null, null, null, null); 
        
        if (mCursor != null && mCursor.moveToFirst()) {
            do{
            	String JSON = new String("");
            	JSON = mCursor.getString(0);
				t.add(HTTPConnect.getObject(JSON, EventDTO.class));
		
            }while(mCursor.moveToNext());   	
            mCursor.close();
        }
    	return t;
    }
    
    /**
     * Gets the favored orgs from the database. passing null for the search param returns the entire table.
     * @return ArrayList<OrganizationDTO> t 
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public ArrayList<OrganizationDTO> getFavOrganizationDTOs() throws JsonParseException, JsonMappingException, IOException{
    	ArrayList<OrganizationDTO> t = new ArrayList<OrganizationDTO>();
    	//OrganizationDTO org;
        Cursor mCursor =
            getmDb().query(false, TABLE_FAVBUSINESS, new String[] {KEY_JSON_DTO}, null, null,
                    null, null, null, null); 
        if (mCursor != null && mCursor.moveToFirst()) {
            do{
            	String JSON = new String("");
            	JSON = mCursor.getString(0);
				t.add( HTTPConnect.getObject(JSON, OrganizationDTO.class));
            		
            }while(mCursor.moveToNext());   	
            mCursor.close();
        }
    	return t;
    }
    

    /**
     * Create a list of events using the title and body provided. If the events is
     * successfully created return the last rowId for the last event, otherwise return
     * a -1 to indicate failure.
     * @param <T>
     * @return rowId or -1 if failed
     */
    public <T extends ParentDTO> long createRows(ArrayList<T> list, InputStream JSONString) throws SQLException {
    	long rowID = -1;
        String currentjson = new String("");
        ObjectMapper mapper = new ObjectMapper();
        
        for(int i =0; i< list.size(); i++){
        try {
			currentjson = mapper.writeValueAsString(list.get(i));
        }
		catch (IOException e) {
			e.printStackTrace();
		}
	        if(list.get(i) instanceof EventDTO){
	        	rowID = createEvent((EventDTO)list.get(i), currentjson);
	        }
	        else
	        {
	        	rowID = createBusiness((OrganizationDTO)list.get(i), currentjson);
	        }
        }
 
       
    	return rowID;
    }



    /**
     * Create a new event using the title and body provided. If the event is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * @return rowId or -1 if failed
     */
    public long createEvent(EventDTO e, String JSONString) throws SQLException {
    	Long rowID = rowExists(TABLE_EVENTS, e);
    	if(rowID == -1){
    		ContentValues initialValues = new ContentValues();
    		if(e.getCategories()!=null){
    			//initialValues.put(KEY_CATEGORY, e.getCategories());
    		}
    		
    		initialValues.put(KEY_CATEGORY, "\"" + e.getCategories().toString().replace('[',' ').replace(']',' ')+"\"");
    		initialValues.put(KEY_BUSINESS_NAME, e.getOrganizationName());
    		initialValues.put(KEY_NAME, e.getName());
    		initialValues.put(KEY_LON, String.valueOf(e.getLon())); 
    		initialValues.put(KEY_LAT, String.valueOf(e.getLat()));
    		initialValues.put(KEY_START_DATE, e.getStartDatetime().getTime());
    		initialValues.put(KEY_END_DATE, e.getStopDatetime().getTime());
    		//Note: need to escape all \n and \r otherwise JSONMapping FAILS
    		initialValues.put(KEY_JSON_DTO, JSONString.replace("\n", "\\n").replace("\r", "\\r"));
    		initialValues.put(KEY_ID, String.valueOf(e.getId()));
    		//Log.w(TAG, "CREATED NEW ROW FOR TABLE EVENTS");
    		rowID = getmDb().insert(TABLE_EVENTS, null, initialValues);
    		//Log.i(TAG, String.valueOf(rowID));
    	}
    	return rowID;

        
    }
    /**
     * Create a new business using the title and body provided. If the business is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * @return rowId or -1 if failed
     */
    public long createBusiness(OrganizationDTO b, String JSONString) throws SQLException {
    	Long rowID = rowExists(TABLE_BUSINESS, b);
    	if(rowID == -1){
    		ContentValues initialValues = new ContentValues();
    		initialValues.put(KEY_NAME, b.getName());
    		initialValues.put(KEY_LON, String.valueOf(b.getLon()));
    		initialValues.put(KEY_LAT, String.valueOf(b.getLat()));
    		initialValues.put(KEY_CATEGORY, "\"" + b.getCategories().toString().replace('[', ' ').replace(']',' ') + "\"");
    		initialValues.put(KEY_ID, String.valueOf(b.getId()));
    		//Note: need to escape all \n and \r otherwise JSONMapping FAILS
    		JSONString = JSONString.replace("\n", "\\n").replace("\r", "");
    		initialValues.put(KEY_JSON_DTO, JSONString);
    		rowID = getmDb().insert(TABLE_BUSINESS, null, initialValues);
    	}
    	return rowID;

    }
    /**
     * Create a new featured content using the title and body provided. If the featured content is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * @return true if featured event was added and false if featured event was not added
     */
  
    public boolean createFeatured (String JSONString, EventDTO f) throws SQLException {
    	if(JSONString == "{}"){
    		return false;
    	}
    	
    	Long rowID = new Long(-1);
    	if(rowID == -1)
    	{
    		ContentValues initialValues = new ContentValues();
    		initialValues.put(KEY_JSON_DTO, JSONString);
    		initialValues.put(KEY_ACCESS_DATE, String.valueOf(System.currentTimeMillis()));
    		getmDb().insert(TABLE_FEATURED, null, initialValues);
    		createEvent(f, JSONString);
    	}
    	
    	if (rowID >= 0){
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    
    /**
     * Create a new featured content using the title and body provided. If the featured content is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * @return rowId or -1 if failed
     */
 
    public boolean createFavOrganization (OrganizationDTO b) throws SQLException {
    	Long rowID = rowExists(TABLE_FAVBUSINESS, b);
    	if(rowID >= 0){
    		return false;
    	}
    	else{
    		String JSONString = new String("");
    		JSONString = fetchFavBusinessJSON(b);
    		ContentValues initialValues = new ContentValues();
    		initialValues.put(KEY_JSON_DTO, JSONString);
    		initialValues.put(KEY_NAME, b.getName());
    		initialValues.put(KEY_ID, b.getId());
    		Log.w(TAG, "Fav Organization was created");
    		rowID = getmDb().insert(TABLE_FAVBUSINESS, null, initialValues);
    		if (rowID >= 0){
    			return true;
    		}
    		else{
    			throw new SQLException();
    		}
    	}
    	

    }
    /**
     * takes an EventDTO and inserts it into the database if it doesn't already exist
     * @param e
     * @return
     * @throws SQLException
     */

    public boolean createFavEvent (EventDTO e) throws SQLException {
    	long rowID = rowExists(TABLE_FAVEVENTS,e);
    	if(rowID > 0){
    		return false;
    	}
    	else{//the row is not in the database
    		String JSONString = new String("");
    		JSONString = fetchFavEventJSON(e);
    		ContentValues initialValues = new ContentValues();
    		initialValues.put(KEY_BUSINESS_NAME, e.getOrganizationName());
    		initialValues.put(KEY_NAME, e.getName());
    		initialValues.put(KEY_START_DATE, e.getStartDatetime().getTime());
    		initialValues.put(KEY_END_DATE, e.getStopDatetime().getTime());
    		initialValues.put(KEY_JSON_DTO, JSONString);
    		initialValues.put(KEY_ID, e.getId());
    		Log.w(TAG, "Fav EVENT WAS CREATED");
    		rowID = getmDb().insert(TABLE_FAVEVENTS, null, initialValues);
    		if (rowID >= 0){
    			return true;
    		}
    		else{
    			throw new SQLException();
    		}
    	}

    }

    //Create update time
    //Create update time or update it for Orgs
    public boolean createUpdateTimeOrg (String cat, long time) throws SQLException {
    	Cursor mCursor = null;
    	mCursor = getmDb().query(true, TABLE_UPDATEORG, new String[] {KEY_ROWID},  KEY_CATEGORY + "=" + "'" + cat + "'",  
    				null, null, null, null, null); 
    	//Entry exists update it in table
    	if (mCursor != null && mCursor.moveToFirst()){
        	ContentValues inputValue = new ContentValues();
        	inputValue.put(KEY_BUSINESS_UPDATE_TIME, time);
        	
        	int row = getmDb().update(TABLE_UPDATEORG, inputValue, KEY_CATEGORY + "=" + "'" + cat + "'", null);
 			mCursor.close();
 			mCursor.deactivate();
        	if (row >= 0){
        		return true;
        	}
        	else{
        		throw new SQLException();
        	}
    	}
    	//Entry does not exist
    	else {
    		long rowID;
        	ContentValues inputValue = new ContentValues();
        	inputValue.put(KEY_BUSINESS_UPDATE_TIME, time);
        	inputValue.put(KEY_CATEGORY, cat);
        	rowID = getmDb().insert(TABLE_UPDATEORG, null, inputValue);
 			mCursor.close();
 			mCursor.deactivate();
        	if (rowID >= 0){
        		return true;
        	}
        	else{
        		throw new SQLException();
        	}
    	}
    }
    /**
     * Used to fetch the update time in the database. This function is called 
     * from DataManager when to check whether or not the database needs to be updated
     * by the server
     * @return String 
     */
     public long fetchUpdateTimeOrg(String cat){
     	long time;
     	Cursor mCursor =
     		getmDb().query(false, TABLE_UPDATEORG, new String[] {KEY_BUSINESS_UPDATE_TIME}, KEY_CATEGORY + "=" + "'" + cat + "'"
     				, null,null, null, null, null); 
     	if (mCursor != null && mCursor.moveToFirst() ) { 		
 			time = mCursor.getLong(mCursor.getColumnIndex(KEY_BUSINESS_UPDATE_TIME));
 			mCursor.close();
 			mCursor.deactivate();
 			return time;	
     	}
     	mCursor.close();
     	mCursor.deactivate();
     	//Returns -1 if failed
     	return -1;
     }
    
     /**
      * Method to create or update the timestamp for Events
      * @param cat, time
      * @return
      * @throws SQLException
      */
    public boolean createUpdateTimeEvents (String cat, long time) throws SQLException {
    	Cursor mCursor = null;
    	mCursor = getmDb().query(true, TABLE_UPDATEEVENTS, new String[] {KEY_ROWID},  KEY_CATEGORY + "=" + "'" + cat + "'", 
    				null, null, null, null, null); 
    	//Entry exists update it in table
    	if (mCursor != null && mCursor.moveToFirst()){
    		ContentValues inputValue = new ContentValues();
    		inputValue.put(KEY_EVENTS_UPDATE_TIME , time);

    		int row = getmDb().update(TABLE_UPDATEEVENTS, inputValue, KEY_CATEGORY + "=" + "'" + cat + "'", null);
    		mCursor.close();
    		mCursor.deactivate();
    		if (row >= 0){
    			return true;
    		}
    		else{
    			throw new SQLException();
    		}
    	}
    	//Entry does not exist
    	else {
    		long rowID;
    		ContentValues inputValue = new ContentValues();
    		inputValue.put(KEY_EVENTS_UPDATE_TIME, time);
    		inputValue.put(KEY_CATEGORY, cat);
    		rowID = getmDb().insert(TABLE_UPDATEEVENTS, null, inputValue);
    		mCursor.close();
    		mCursor.deactivate();
    		if (rowID >= 0){
    			return true;
    		}
    		else{
    			throw new SQLException();
    		}
    	}
    }
    /**
     * Takes a DTO and removes it from the favorites table if it is in there
     * @param b
     * @return
     * @throws SQLException
     */
    
    public boolean dbRemoveFavOrganization (ParentDTO b) throws SQLException {
    	Long rowID = rowExists(TABLE_FAVBUSINESS, b);
    	if(rowID != -1){
    		int deleted = getmDb().delete(TABLE_FAVBUSINESS, KEY_ID + "=" + b.getId() ,null);
    		if(deleted == 1){
    			return true;
    		}
    		return false;
    	}
    	return false;

    }
    /**
     * Takes a DTO and removes it from the favorites table if it is in there
     * @param b
     * @return
     * @throws SQLException
     */
    public boolean dbRemoveFavEvent (ParentDTO b) throws SQLException {
    	Long rowID = rowExists(TABLE_FAVEVENTS, b);
    	if(rowID != -1){
    		int deleted = getmDb().delete(TABLE_FAVEVENTS, KEY_ID + "=" + b.getId() ,null);
    		if(deleted == 1){
    			return true;
    		}
    		return false;
    	}
    	return false;

    }
    
    /**
     * Gets the JSON String from the database for the EventDTO based on the ID
     * @param OrganizationDTO
     * @return JSONString
     * @throws SQLException
     */
    public String fetchFavEventJSON(EventDTO e) throws SQLException {

    	Cursor mCursor =
    		getmDb().query(true, TABLE_EVENTS, new String[] {KEY_JSON_DTO},  KEY_ID +"=" + String.valueOf(e.getId()) , null,
    				null, null, null, null); 
    	if (mCursor != null && mCursor.moveToFirst()) {
			String JSON = mCursor.getString(mCursor.getColumnIndex(KEY_JSON_DTO));
			mCursor.close();
			mCursor.deactivate();
			return JSON;
    	}
    	mCursor.close();
    	mCursor.deactivate();
    	return null;
    }
    /**
     * Gets the JSON String from the database for the OrganizationDTO based on the ID
     * @param OrganizationDTO
     * @return JSONString
     * @throws SQLException
     */
    public String fetchFavBusinessJSON(OrganizationDTO e) throws SQLException {
    	Cursor mCursor =
    		getmDb().query(false, TABLE_BUSINESS, new String[] {KEY_JSON_DTO}, KEY_ID + "=" + e.getId(), null,
    				null, null, null, null); 
    	if (mCursor != null && mCursor.moveToFirst() ) { 		
			String JSON = mCursor.getString(mCursor.getColumnIndex(KEY_JSON_DTO));
			mCursor.close();
			mCursor.deactivate();
			return JSON;	
    	}
    	mCursor.close();
    	mCursor.deactivate();
    	return null; 
    }	
    /**
     * Method to check if a DTO is already being stored in the database
     * Used to manage the size of the database and prevent duplicated entries
     * @param table
     * @param p
     * @return
     */
    public long rowExists(String table, ParentDTO p ){
    	Cursor mCursor =
    		getmDb().query(true, table, new String[] {KEY_ROWID},  KEY_ID +"=" + String.valueOf(p.getId()) , null,
    				null, null, null, null); 
    	if (mCursor != null && 	mCursor.moveToFirst()) {
    		if(mCursor.getCount()>0){
    			
    			long id = mCursor.getLong(mCursor.getColumnIndex(KEY_ROWID));
    			mCursor.close();
    			mCursor.deactivate();
    			return id;
    		}
    	}
		mCursor.close();
		mCursor.deactivate();
    	return -1;
    }
    /**
     * Used to fetch all the orgs in the database, this function is called 
     * from DataManager when there is no connection
     * @return ArrayList<OrganizationDTO> all the orgs currently in the database
     */
    public ArrayList<OrganizationDTO> fetchAllOrganizations(String url){
    	ArrayList<OrganizationDTO> orgs = new ArrayList<OrganizationDTO>();
    	String JSON = new String();
    	String category = new String(" ");
    	int catIndex = url.indexOf("category=");
    	
    	if(catIndex != -1){
    		category = url.substring(catIndex+9);
    	}
    	if (category.equals(" ")||category.matches("All")){
    		category = "%";
    	}	
    	Log.i("LokaliteDb", "category is: "+ category);
    	Cursor mCursor;
    	if (category.equals(new String("%"))){
    		mCursor =getmDb().query(false, TABLE_BUSINESS, new String[] {KEY_JSON_DTO},
        			null , null, null, null, null, "14"); 
    	}
    	else{
    		mCursor =getmDb().query(false, TABLE_BUSINESS, new String[] {KEY_JSON_DTO},
        	 KEY_CATEGORY + " like " + "'%" +category + "%'"
        			, null, null, null, null, "14"); 
    	}
    	
    	if (mCursor != null && 	mCursor.moveToFirst()) {
    		int count = mCursor.getCount();
    		for(int i = 0; i< count; i++){
    			//JSON = mCursor.getString(mCursor.getColumnIndex(KEY_JSON_DTO)).replace("\\n", "\n");
    			JSON = mCursor.getString(mCursor.getColumnIndex(KEY_JSON_DTO));
				try {
					orgs.add(HTTPConnect.getObject(JSON, OrganizationDTO.class));
				} catch (IOException e) {
					Log.i("LokaliteDb", "IOException when getting OrgDTO's from database from fetchAllOrganizations");
					e.printStackTrace();
				}

    			mCursor.moveToNext();
    		}
    	}
		mCursor.close();
		mCursor.deactivate();
		return orgs;
    	
    }
    /**
     * Used to fetch all the events in the database, this function is called 
     * from DataManager when there is no connection
     * @return ArrayList<EventDTO> all the events currently in the database
     */
    public ArrayList<EventDTO> fetchAllEvents(String url) {
    	databaseClean();
    	ArrayList<EventDTO> events = new ArrayList<EventDTO>();
    	String JSON = new String();
    	
    	String category = new String(" ");
    	int catIndex = url.indexOf("category=");
    	int endIndex = url.indexOf("&limit=15");
    	
    	if(catIndex != -1){
    		category = url.substring(catIndex+9, endIndex);
    	}
    	if (category.equals(" ")){
    		category = "%";
    	}	
    	Cursor mCursor;
    	if (category.equals(new String("%"))){
    		mCursor =getmDb().query(false, TABLE_EVENTS, new String[] {KEY_JSON_DTO},
        			KEY_CATEGORY + " like " + "'" + category + "'" 
        			, null, null, null, null, null); 
    	}
    	else{
    		mCursor =getmDb().query(false, TABLE_EVENTS, new String[] {KEY_JSON_DTO},
        	 KEY_CATEGORY + " like " + "'%" +category + "%'"
        			, null, null, null, null, null); 
    	}
    	if (mCursor != null && 	mCursor.moveToFirst()) {
    		int count = mCursor.getCount();
    		for(int i = 0; i< count; i++){
    			JSON = mCursor.getString(mCursor.getColumnIndex(KEY_JSON_DTO));

				try {

					events.add(HTTPConnect.getObject(JSON, EventDTO.class));
				} catch (IOException e) {
					Log.i("LokaliteDb", "IOException when getting EventDTO's from database in fetchAllEvents()");
					e.printStackTrace();
				}

    			mCursor.moveToNext();
    		}
    	}
		mCursor.close();
		return events;
    	
    }
    
    /**
     * Used to fetch the update time in the database. This function is called 
     * from DataManager when to check whether or not the database needs to be updated
     * by the server
     * @return long 
     */
     public long fetchUpdateTimeEvents(String cat){     	
     	long time;
     	Cursor mCursor =
     		getmDb().query(false, TABLE_UPDATEEVENTS, new String[] {KEY_EVENTS_UPDATE_TIME}, KEY_CATEGORY + "=" + "'" + cat + "'"
     				, null,null, null, null, null); 
     	if (mCursor != null && mCursor.moveToFirst() ) { 		
 			time = mCursor.getLong(mCursor.getColumnIndex(KEY_EVENTS_UPDATE_TIME));
 			mCursor.close();
 			mCursor.deactivate();
 			return time;	
     	}
     	mCursor.close();
     	mCursor.deactivate();
     	//Returns -1 if failed
     	return -1;
     }
     
    /**
     * counts the number of rows in a table to see if we need to go to the server to 
     * update the data
     * @return number of rows in the table
     */
    public int tableCount(String table){
    	int total= 0;
    	//Query the events table for all the events
    	Cursor mCursor =getmDb().query(false, table, new String[] {KEY_ROWID},null, null,
				null, null, null, null);  
    	if (mCursor != null && 	mCursor.moveToFirst()) {
    		total = mCursor.getCount();
    		}
    		// When we have gotten to the end of the events stored on the phone close the cursor
    		mCursor.close();
    		return total;
    }	
    
    /**
     * Remove all events whose start date has passed from the database
     * if a parse exception is thrown while parsing the date, the event will be deleted
     * under the assumption that it must be in error. 
     */

    public void databaseClean(){
    	Date start = new Date();
    	
    	//add 5 hours to the present time so that we don't delete any thing that the user may want
    	start.setHours(start.getHours());
    	
    		
    	//Query the events table for all the events
    	Cursor mCursor =getmDb().query(false, TABLE_EVENTS, new String[] {KEY_ROWID, KEY_END_DATE}, KEY_END_DATE + "<" + start.getTime() , null,
				null, null, null, null);  
    
    	
    	if (mCursor != null && 	mCursor.moveToFirst()) {
    		int count = mCursor.getCount();
    		Log.i("LokaliteDb", "Number of events removed DBClean: " + String.valueOf(count));
    		for(int i = 0; i< count; i++){
    			Log.i("LokaliteDb", "Current time: " + start.getTime() + "End Date: " + mCursor.getLong(mCursor.getColumnIndex(KEY_END_DATE)) 
    					+ "Row ID: " + mCursor.getInt(mCursor.getColumnIndex(KEY_ROWID)));
				int deleted = getmDb().delete(TABLE_EVENTS, KEY_ROWID + "==" + mCursor.getInt(mCursor.getColumnIndex(KEY_ROWID)), null);  
			    
				getmDb().delete(TABLE_EVENTS, KEY_ROWID + "=" + mCursor.getInt(mCursor.getColumnIndex(KEY_ROWID)), null);
				mCursor.moveToNext();

				}

    		}
    		// When we have gotten to the end of the events stored on the phone close the cursor
    		mCursor.close();
    	
    }
    /**
     * Used to search for events in the database, this function is called 
     * from DataManager when there is no connection
     * @return ArrayList<EventDTO> all the events 
     * in the database that match the search options
     */
    public ArrayList<EventDTO> searchEvents(String category,String name,String orgName, Date start, Date end) {
    	databaseClean();
    	ArrayList<EventDTO> events = new ArrayList<EventDTO>();
    	String JSON = new String();
    	if(name == "Event Title" | name.equals(null)| name.equals(new String(" ")) | name.equals(new String(""))){
    		name = new String("%");
    	}
    	else {
    		name = "%"+name+"%";
    	}
    	if(orgName == "Organization Name" | orgName.equals(null)| orgName.equals(new String(" ")) | orgName.equals(new String(""))){
    		orgName = new String("%");
    	}
    	else {
    		orgName = "%"+orgName+"%";
    	}
    	if(category == null){
    		category = new String("%");
    	}
    	Cursor mCursor;
    	if (category.equals(new String("%"))){
    		mCursor =getmDb().query(false, TABLE_EVENTS, new String[] {KEY_JSON_DTO},
        			KEY_START_DATE +" <= "+"'"+end.getTime()+"'"+ " and "
        			+ KEY_END_DATE + " >= " + "'" + start.getTime()+"'" 
        			+ " and " + KEY_NAME + " like " + "'" + name + "'" 
        			+ " and " + KEY_CATEGORY + " like " + "'" + category + "'"
        			+ " and " + KEY_BUSINESS_NAME + " like " + "'" + orgName + "';" 
        			, null, null, null, null, null); 
    	}
    	else{
    		mCursor =getmDb().query(false, TABLE_EVENTS, new String[] {KEY_JSON_DTO},
        			KEY_START_DATE +" BETWEEN "+"'"+start.getTime()+"'"+ " and "+"'"+ end.getTime()+"'" 
        			+ " and " + KEY_NAME + " like " + "'" + name + "'" 
        			+ " and " + KEY_CATEGORY + " like " + "'%" +category + "%'"
        			+ " and " + KEY_BUSINESS_NAME + " like " + "'" + orgName + "'" + ";" 
        			, null, null, null, null, null); 
    	}
    	
    	
    	if (mCursor != null && 	mCursor.moveToFirst()) {
    		int count = mCursor.getCount();
    		for(int i = 0; i< count; i++){
    			JSON = mCursor.getString(mCursor.getColumnIndex(KEY_JSON_DTO));
    			Log.i("LokaliteDb", "Events found within date range =" + String.valueOf(count));
				try {

					events.add(HTTPConnect.getObject(JSON, EventDTO.class));
				} catch (IOException e) {
					Log.i("LokaliteDb", "IOException when getting EventDTO's from database from searchEvents()");
					e.printStackTrace();
				}

    			mCursor.moveToNext();
    		}
    	}
		mCursor.close();
		return events;    	
    }
    /**
     * Used to search for organization in the database, this function is called 
     * from DataManager when there is no connection
     * @return ArrayList<OrganizationDTO> all the organizations
     * in the database that match the search options
     */
    public ArrayList<OrganizationDTO> searchOrgs(String category, String orgName,String hostedEventName ) {
    	databaseClean();
    	ArrayList<OrganizationDTO> orgs = new ArrayList<OrganizationDTO>();
    	String orgEventSearch = new String("");
    	String JSON = new String();
    	if(orgName == "Organization Name" | orgName.equals(null)| orgName.equals(new String(""))){
    		orgName = new String("%");
    	}
    	else {
    		orgName = "%"+orgName+"%";
    	}
    	if(hostedEventName == "Event Name"  | hostedEventName.equals(null)| hostedEventName.equals(new String(""))){
    		hostedEventName = new String("%");
    	}
    	else {
    		hostedEventName = "%"+hostedEventName+"%";
    	}
    	if(category == null){
    		category = new String("%");
    	}

    	else{
    		Cursor eventCursor =getmDb().query(false, TABLE_EVENTS, new String[] {KEY_BUSINESS_NAME},KEY_NAME + " like " +"\"" + hostedEventName + "\"", null,
    				null, null, null, null); 
        	
        	if (eventCursor != null && eventCursor.moveToFirst()) {
        		String bName;
        		int count = eventCursor.getCount();
        		for(int i = 0; i< count; i++){
        			bName = eventCursor.getString(eventCursor.getColumnIndex(KEY_BUSINESS_NAME));
        			orgEventSearch = orgEventSearch + " " + bName + " ";
        			eventCursor.moveToNext();
        		}
        	}
    		eventCursor.close();
    	}
    	if(orgEventSearch.equals("") | orgEventSearch.equals(null)){
    		orgEventSearch = new String("%");
    	}
    	else{
    		orgEventSearch = "\"" + orgEventSearch + "\"";
    	}
    	Cursor mCursor;
    	if (category.equals(new String("%"))){
    		if(orgEventSearch.equals(new String("%"))){
    			mCursor =getmDb().query(false, TABLE_BUSINESS, new String[] {KEY_JSON_DTO},
            			KEY_NAME + " like " + "'" + orgName + "';" 
            			+ " and " + KEY_CATEGORY + " like " + "'" +category + "'" 
            			+ " and " + KEY_NAME + " like " + "'" + orgEventSearch +"'"
            			, null, null, null, null, null);
    		}
    		else
    		{
    			mCursor =getmDb().query(false, TABLE_BUSINESS, new String[] {KEY_JSON_DTO},
            			KEY_NAME + " like " + "'" + orgName + "'" 
            			+ " and " + KEY_CATEGORY + " like " + "'" +category + "'"
            			+ " and " + orgEventSearch + " like " + "'%' || " + KEY_NAME + " || '%';"
            			, null, null, null, null, null);
    		}
    	}
    	else{
    		if(orgEventSearch.equals(new String("%"))){
        		mCursor =getmDb().query(false, TABLE_BUSINESS, new String[] {KEY_JSON_DTO},
            			KEY_NAME + " like " + "'" + orgName + "'" 
            			+ " and " + KEY_CATEGORY + " like " + "'%" +category + "%'"
            			+ " and " + KEY_NAME + " like " + "'" + orgEventSearch + "'"
            			, null, null, null, null, null);
    		}
    		else
    		{
        		mCursor =getmDb().query(false, TABLE_BUSINESS, new String[] {KEY_JSON_DTO},
            			KEY_NAME + " like " + "'" + orgName + "'" 
            			+ " and " + KEY_CATEGORY + " like " + "'%" +category + "%'"
            			+ " and " + orgEventSearch + " like " + "'%' || " + KEY_NAME + " || '%';"
            			, null, null, null, null, null);
    		}
    	}
    
    	 

    	if (mCursor != null && 	mCursor.moveToFirst()) {
    		int count = mCursor.getCount();
    		for(int i = 0; i< count; i++){
    			JSON = mCursor.getString(mCursor.getColumnIndex(KEY_JSON_DTO));

				try {

					orgs.add(HTTPConnect.getObject(JSON, OrganizationDTO.class));
				} catch (IOException e) {
					Log.i("LokaliteDb", "IOException when getting EventDTO's from database from searchOrgs");
					e.printStackTrace();
				}

    			mCursor.moveToNext();
    		}
    	}
		mCursor.close();
		return orgs;    	
    }
	//add 5 hours to the present time so that we don't delete any thing that the user may want
	//presentTime.setHours(presentTime.getHours()+5);
	/**
	 * @return the mDb
	 */
	private static void setmDb(SQLiteDatabase database) {
		mDb = database;
	}
	/**
	 * @return the mDb
	 */
	public SQLiteDatabase getmDb() {
		return mDb;
	}
	// Returns True if Table t is empty
	public Boolean isEmpty(String t)

	{		Log.i("LokaliteDb", "isEmpty() category"+ t);
	Cursor orgCursor;
	if(t.matches("All")){
		orgCursor =getmDb().query(false, TABLE_BUSINESS,new String[] {KEY_NAME} ,null, null,
				null, null, null, null);
	}
	else{
		orgCursor =getmDb().query(false, TABLE_BUSINESS,new String[] {KEY_NAME} ,KEY_CATEGORY + " like " + "'%" + t + "%'", null,
				null, null, null, null);
	}
	Log.i("LokaliteDb", "isEmpty() number of rows"+ String.valueOf(orgCursor.getCount()));
	if(orgCursor.getCount() > 0){
		Log.i("LokaliteDb", "isEmpty() number of rows"+ String.valueOf(orgCursor.getCount()));
		orgCursor.close();
		orgCursor.deactivate();
		return false;
	}
	orgCursor.close();
	orgCursor.deactivate();
	return true;
	}
	/**
     * Used to search for favorite events in the database, this function is called 
     * from DataManager when there is no connection
     * @return ArrayList<EventDTO> all the favorites events 
     * in the database that match the search options
     */
    public ArrayList<EventDTO> searchFavEvents(String name,String orgName, Date start, Date end) {
    	databaseClean();
    	ArrayList<EventDTO> events = new ArrayList<EventDTO>();
    	String JSON = new String();
    	if(name == "Event Title" | name.equals(null)| name.equals(new String(" ")) | name.equals(new String(""))){
    		name = new String("%");
    	}
    	else {
    		name = "%"+name+"%";
    	}
    	if(orgName == "Organization Name" | orgName.equals(null)| orgName.equals(new String(" ")) | orgName.equals(new String(""))){
    		orgName = new String("%");
    	}
    	else {
    		orgName = "%"+orgName+"%";
    	}
    	Cursor mCursor;
    	mCursor =getmDb().query(false, TABLE_FAVEVENTS, new String[] {KEY_JSON_DTO},
        			KEY_START_DATE +" BETWEEN "+"'"+start.getTime()+"'"+ " and "+"'"+ end.getTime()+"'" 
        			+ " and " + KEY_NAME + " like " + "'" + name + "'" 
        			+ " and " + KEY_BUSINESS_NAME + " like " + "'" + orgName + "'" 
        			, null, null, null, null, null); 
    	
    	if (mCursor != null && 	mCursor.moveToFirst()) {
    		int count = mCursor.getCount();
    		for(int i = 0; i< count; i++){
    			JSON = mCursor.getString(mCursor.getColumnIndex(KEY_JSON_DTO));
    			Log.i("LokaliteDb", "Events found within date range =" + String.valueOf(count));
				try {

					events.add(HTTPConnect.getObject(JSON, EventDTO.class));
				} catch (IOException e) {
					Log.i("LokaliteDb", "IOException when getting EventDTO's from database from searchEvents()");
					e.printStackTrace();
				}

    			mCursor.moveToNext();
    		}
    	}
		mCursor.close();
		return events;    	
    }
    /**
     * Used to search for favorite organization in the database, this function is called 
     * from DataManager
     * @return ArrayList<OrganizationDTO> all the favorite organizations
     * in the database that match the search options
     */
    public ArrayList<OrganizationDTO> searchFavOrgs(String orgName,String hostedEventName ) {
    	databaseClean();
    	ArrayList<OrganizationDTO> orgs = new ArrayList<OrganizationDTO>();
    	String orgEventSearch = new String("");
    	String JSON = new String();
    	if(orgName == "Organization Name" | orgName.equals(null)| orgName.equals(new String(""))){
    		orgName = new String("%");
    	}
    	else {
    		orgName = "%"+orgName+"%";
    	}
    	if(hostedEventName == "Event Name"  | hostedEventName.equals(null)| hostedEventName.equals(new String(""))){
    		hostedEventName = new String("%");
    	}
    	else {
    		hostedEventName = "%"+hostedEventName+"%";
    	}
    	if(hostedEventName.equals(new String("%"))){
        	
    	}
    	else{
    		Cursor eventCursor =getmDb().query(false, TABLE_EVENTS, new String[] {KEY_BUSINESS_NAME},KEY_NAME + " like " +"\"" + hostedEventName + "\"", null,
    				null, null, null, null); 
        	
        	if (eventCursor != null && eventCursor.moveToFirst()) {
        		String bName;
        		int count = eventCursor.getCount();
        		for(int i = 0; i< count; i++){
        			bName = eventCursor.getString(eventCursor.getColumnIndex(KEY_BUSINESS_NAME));
        			orgEventSearch = orgEventSearch + " " + bName + " ";
        			eventCursor.moveToNext();
        		}
        	}
    		eventCursor.close();
    	}
    	if(orgEventSearch.equals("") | orgEventSearch.equals(null)){
    		orgEventSearch = new String("%");
    	}
    	else{
    		orgEventSearch = "\"" + orgEventSearch + "\"";
    	}
    	Cursor mCursor;
    	if(orgEventSearch.equals(new String("%"))){
    		mCursor =getmDb().query(false, TABLE_FAVBUSINESS, new String[] {KEY_JSON_DTO},
            		KEY_NAME + " like " + "'" + orgName + "'" 
            		+ " and " + KEY_NAME + " like " + "'" + orgEventSearch +"'"
            		, null, null, null, null, null);
    	}
    	else
    	{
    		mCursor =getmDb().query(false, TABLE_FAVBUSINESS, new String[] {KEY_JSON_DTO},
           			KEY_NAME + " like " + "'" + orgName + "'" 
           			+ " and " + orgEventSearch + " like " + "'%' || " + KEY_NAME + " || '%'"
           			, null, null, null, null, null);
    	}
    	 
    	if (mCursor != null && 	mCursor.moveToFirst()) {
    		int count = mCursor.getCount();
    		for(int i = 0; i< count; i++){
    			JSON = mCursor.getString(mCursor.getColumnIndex(KEY_JSON_DTO));

				try {

					orgs.add(HTTPConnect.getObject(JSON, OrganizationDTO.class));
				} catch (IOException e) {
					Log.i("LokaliteDb", "IOException when getting EventDTO's from database from searchOrgs");
					e.printStackTrace();
				}

    			mCursor.moveToNext();
    		}
    	}
		mCursor.close();
		return orgs;    	
    }
    
}
	

