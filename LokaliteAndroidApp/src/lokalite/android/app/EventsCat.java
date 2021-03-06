
package lokalite.android.app;

import java.util.ArrayList;

import android.app.AlarmManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class EventsCat extends LokaliteListActivity {
	private ArrayList<String> myCats = null;
	private ArrayAdapter<String> myAdapter;

	/*
	 * Creates a new EventCat activity including swiping
	 * 
	 * @see lokalite.android.app.LokaliteListActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eventscat);
		
		// Set the title layout and text
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.lokaliteheader);
        lokaliteTitle = (TextView) findViewById(R.id.lokalitetitle);
        this.lokaliteTitle.setText("Events");

		am = (AlarmManager) getSystemService(ALARM_SERVICE);

		// For swiping
		// You MUST set the intent to the left and right of this current activity/view
		// Forgetting to do so will cause the application to crash.
		Intent leftIntent = new Intent(this, OrganizationCat.class);
        Intent rightIntent = new Intent(this, ProfileTabHost.class);
        enableSwipe(leftIntent, rightIntent);
        
        
		// Initialize the list of event items
		myCats = new ArrayList<String>(Categories.eventCategories);


		myAdapter = new CategoryListAdapter(this, this, R.layout.eventitem, myCats);
		setListAdapter(myAdapter);

		// Handle category clicks
		this.getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				Intent intent = new Intent(v.getContext(), EventTabHost.class);
				intent.putExtra("cat", (String) getListView().getItemAtPosition(position));
				//PendingIntent pendingIntent = PendingIntent.getBroadcast(v.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				startActivityIfNeeded(intent, defaultIntentCode);
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
	}

	/* 
	 * Handles "Content changes' such as rotating the device or deploying the keyboard
	 * 
	 * @see android.app.ListActivity#onContentChanged()
	 */
	@Override
	public void onContentChanged()
	{
		super.onContentChanged();
		getListView().setOnTouchListener(gestureListener);
	}
}
