package ICUP.App;

import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public class BusinessCat extends ICUPListActivity {
	private ArrayList<String> myCats = null;
    private ArrayAdapter<String> myAdapter;
	
    /**
     * Sets the alarm manager, and initializes swiping intents. 
     * sets the onItemClickListener to start the BusinessTabHost passing the 
     * category so that the data will reflect the correct category.
     */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.businesscat);
	    
	    am = (AlarmManager) getSystemService(ALARM_SERVICE);
	    	    
        // For swiping
        // You MUST set the intent to the left and right of this current activity/view
        // Forgetting to do so will cause the application to crash.
	    Intent leftIntent = new Intent(this, FavoritesTabHost.class);
        Intent rightIntent = new Intent(this, FeaturedEvent.class);
        enableSwipe(leftIntent, rightIntent);
        
        // Initialize the list of business items
		myCats = new ArrayList<String>(Categories.businessCategories);
        

        myAdapter = new CategoryListAdapter(this, this, R.layout.catitem, myCats);
        setListAdapter(myAdapter);
        
        // Handle category clicks
        this.getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
        		Intent intent = new Intent(v.getContext(), BusinessTabHost.class);
            	intent.putExtra("cat", (String) getListView().getItemAtPosition(position));
            	PendingIntent pendingIntent = PendingIntent.getBroadcast(v.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            	startActivityIfNeeded(intent, defaultIntentCode);
            	overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
        });
	}
    /**
     * sets the OnTouchListener to allow for swiping within categories.
     */
    @Override
    public void onContentChanged()
    {
    	super.onContentChanged();
    	getListView().setOnTouchListener(gestureListener);
    }
}
