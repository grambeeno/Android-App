package lokalite.android.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.blueduck.collegepedia.dto.EventDTO;

/** Featured Event: Shows the Featured Event **/
public class FeaturedEvent extends LokaliteActivity {
	String TAG = getClass().getName();
	private EventDTO featured;
	
   
    /* 
     * Called when the FeaturedEvent activity is first created.
     * Enables swiping and connectts to the server to get data. 
     * (non-Javadoc)
     * @see lokalite.android.app.LokaliteActivity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.featuredevent);
        featured = null;
        //dm = new DataManager(this);
        if(dm != null){
        	  new getFeaturedEventTask().execute("http://174.136.5.67:8080/collegepedia/api/featured.json"); 
        }
        
        // For swiping
        // You MUST set the intent to the left and right of this current activity/view
        // Forgetting to do so will cause the application to crash.
        Intent leftIntent = new Intent(this, OrganizationCat.class);
    	Intent rightIntent = new Intent(this, EventsCat.class);
        enableSwipe(leftIntent, rightIntent);
        
        // For swiping in complex views
        findViewById(R.id.FeaturedEventScrollView).setOnTouchListener(gestureListener);

        enableUI(false);
    }
    
    
    /* 
     * Catch onPause events
     * (non-Javadoc)
     * @see lokalite.android.app.LokaliteActivity#onPause()
     */
    @Override
	public void onPause(){
    	super.onPause();
    }
    
    
    /* 
     * Catch onResume Events
     * (non-Javadoc)
     * @see lokalite.android.app.LokaliteActivity#onResume()
     */
    @Override
	public void onResume(){
    	super.onResume();
    }
    
    
    /**
     * Handles button clicks from the map button. 
     * Starts a new activity centered at the location of the event
     * @param v
     */
    public void onMap(View v){
        Intent map = new Intent(this, Mapper.class);
    	if(featured != null){
    		map.putExtra("lat", featured.getLat());
    		map.putExtra("lon", featured.getLon());
    		startActivityIfNeeded(map, defaultIntentCode);	
    	}    	
    }
    
    /**
     * Adds the event to the users favotires list. 
     * @param v
     */
    public void onAddToFavs(View v){
		boolean add = dm.addToFavorites(featured);
		if(add ==  true){
			AlertDialog.Builder adb = new AlertDialog.Builder(FeaturedEvent.this);  
            adb.setMessage("Added "+featured.getName()+" to favorites");  
            adb.setPositiveButton("Ok", null);
            adb.show();    
		}
		if (add == false){
			AlertDialog.Builder adb = new AlertDialog.Builder(FeaturedEvent.this);  
            adb.setMessage(featured.getName()+" is already in favorites");  
            adb.setPositiveButton("Ok", null);
            adb.show();    	
		}	
    
    }
    
    /**
     * populateUI: populate the UI given the info in a FeaturedEventDTO
     * @param currentEvent
     */
    public void populateUI(EventDTO currentEvent){
    	//Set description text 
    	if (currentEvent.getName() != null)
    	{
    		TextView description = (TextView)findViewById(R.id.description);
    		description.setText(currentEvent.getName());
    	}
    	else{
    		TextView description = (TextView)findViewById(R.id.description);
    		
    		//description.setText("No Featured Event");
    		description.setText("Featured Event Coming Soon");
    	}
    	
    	//Set finePrint text
    	if (currentEvent.getDescription() != null)
    	{
    		TextView finePrint = (TextView)findViewById(R.id.finePrint);
    		finePrint.setText(currentEvent.getDescription().replace("\r", ""));
    	}
    	
    	//Convert byte array to image
    	if (currentEvent.getImage() != null)
    	{
    		Bitmap image;
    		image = BitmapFactory.decodeByteArray(currentEvent.getImage(), 0, currentEvent.getImage().length);
    		
    		//Set featuredEventLogo image
    		ImageView featuredEventLogo = (ImageView)findViewById(R.id.featuredEventLogo);
    		featuredEventLogo.setImageBitmap(image);
    	}
    	else{
    		//Set featuredEventLogo image
    		ImageView featuredEventLogo = (ImageView)findViewById(R.id.featuredEventLogo);
    		featuredEventLogo.setImageResource(R.drawable.nofeatured);
    	}
    	
    	if (currentEvent.getLocation() != null)
    	{
    		TextView location = (TextView)findViewById(R.id.featuredLocation);
    		location.setText(currentEvent.getLocation());
    	}
    	
    	if (currentEvent.getStartDatetime() != null)
    	{
    		TextView startDate =(TextView)findViewById(R.id.featuredDate);
    		startDate.setText(currentEvent.getStartDatetime().toLocaleString());
    	}
    
    	if(currentEvent.getDescription() != "Sorry, could not connect to Lokalite's server" 
    		&& currentEvent.getDescription() != null){
    		enableUI(true);
    	}

    	
    }
     
    /**
     * Custom async task to fetch data from the server in its own thread
     * So we do not block the UI
     */
    private class getFeaturedEventTask extends AsyncTask<String, Void, EventDTO>{

		@Override
		// Gets run in a background thread so that we don't block the UI
		protected EventDTO doInBackground(String... params) {
			
			EventDTO curEvent = dm.getFeaturedContent(params[0]);
			return curEvent;
		}
		

		@Override
		// on completion of the activity we should update the ui.
		protected void onPostExecute(EventDTO result) {
			// If an exception gets thrown or the connection is canceled then 
			// result == null 
			if(result != null){
				featured = result;
				populateUI(result);
			}
	     }

    }  
    
    /**
     * Enables or disables the UI depending on if we have information 
     * @param enabled
     */
    public void enableUI(boolean enabled){
        ImageButton mapButton = (ImageButton)findViewById(R.id.mapButton);
        mapButton.setClickable(enabled);
        mapButton.setEnabled(enabled);
    	
        ImageButton addToFavs = (ImageButton)findViewById(R.id.addToFavButton);
        addToFavs.setClickable(enabled);
        addToFavs.setEnabled(enabled);	
    }
}
