package lokalite.android.app;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blueduck.collegepedia.dto.ParentDTO;

/**
 * 
 * @author Jonathan Tsui, Matthew Ripley
 *
 */

/**
 * LokaliteActivity: Sub-class for all of Lokalite that implements common features
 */
public class LokaliteListActivity extends ListActivity {
	
	// For storing preferences
	SharedPreferences preferences;
	Location myLocation = new Location(LOCATION_SERVICE);
	
	private static final int SWIPE_MIN_DISTANCE = 50;
	private static final int SWIPE_MAX_OFF_PATH = 100;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	private Intent leftIntent;
	private Intent rightIntent;
	
	protected ArrayAdapter myAdapter;
	protected final int defaultIntentCode = 0;
	protected boolean connected;
	protected AlarmManager am;
	protected DataManager dm;
	protected PopupWindow popup;
	protected Thread thread;
	protected TextView lokaliteTitle;
	
	/* 
	 * Catches configuration changes from the phone such as keyboard and rotation events
	 * (non-Javadoc)
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (popup != null) {
			if (popup.isShowing()) {
				int popupHeight = (int) (getApplicationContext().getResources().getDisplayMetrics().heightPixels) - 25;
				int popupWidth = (int) (getApplicationContext().getResources().getDisplayMetrics().widthPixels);

				popup.update(0, 0, popupWidth, popupHeight);
			}
		}
	}

	/*
	 * Initializes a new LokaliteListActivity. Loads preferences, and user location. 
	 * Checks to see if the device is online and if so initializes the data manager
	 * 
	 *  (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dm = new DataManager(this);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.lokaliteheader);
		
		am = (AlarmManager) getSystemService(ALARM_SERVICE);

		// Initialize preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		// Get the preferences that affect events and orgs
		String locCityPref = preferences.getString("locCityPref", "Boulder, CO");
		Geocoder gc = new Geocoder(this, Locale.getDefault());
		if(isOnline()){
			try {
				List<Address> addresses = gc.getFromLocationName(locCityPref, 1);

				if (!addresses.isEmpty()) {
					Address address = addresses.get(0);
					myLocation.setLatitude(address.getLatitude());
					myLocation.setLongitude(address.getLongitude());
				}
			} catch (Exception e) {
			}
		}
		else
		{
			LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Criteria crit = new Criteria();
			crit.setAccuracy(Criteria.ACCURACY_FINE);
			String provider = lm.getBestProvider(crit, true);
			Location myLocation = lm.getLastKnownLocation(provider);
			
			
			// If the user has no LastKnownLocations and is not online, default to Boulder, CO
			if (myLocation == null)
			{
				myLocation = new Location(LOCATION_SERVICE);
				myLocation.setLatitude(40);
				myLocation.setLongitude(-105);
			}
		}

		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		};

		Intent extras = getIntent();
		connected = extras.getBooleanExtra(
				ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
	}

	/* 
	 * Creates OptionsMenu when MENU button is pressed
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	/* 
	 * Brings the proper activity into the current focus when
	// a button on the Options Menu is pressed
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.main:
			Intent mainPage = new Intent(this, LokaliteAndroidMain.class);
			startActivityIfNeeded(mainPage, defaultIntentCode);
			return true;
		case R.id.events:
			Intent events = new Intent(this, EventsCat.class);
			startActivityIfNeeded(events, defaultIntentCode);
			return true;
		case R.id.organizations:
			Intent organizations = new Intent(this, OrganizationCat.class);
			startActivityIfNeeded(organizations, defaultIntentCode);
			return true;
		case R.id.favorites:
			Intent favorites = new Intent(this, FavoritesTabHost.class);
			startActivityIfNeeded(favorites, defaultIntentCode);
			return true;
		case R.id.settings:
			Intent settings = new Intent(this, Settings.class);
			startActivityIfNeeded(settings, defaultIntentCode);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	
	/**
	 * Enables swiping and sets activities' intents left and right
	 * 
	 * @param newLeftIntent
	 * @param newRightIntent
	 */
	public void enableSwipe(Intent newLeftIntent, Intent newRightIntent) {
		gestureDetector = new GestureDetector(new MyGestureDetector());
		leftIntent = newLeftIntent;
		rightIntent = newRightIntent;
	}

	
	/**
	 * Disables swiping be removing the gesture detector.
	 */
	public void disableSwipe() {
		// Gesture detector for swiping
		gestureDetector = null;
	}

	
	/**
	 * Custom gesture detector that handles swiping and other gestures 
	 *
	 */
	class MyGestureDetector extends SimpleOnGestureListener {
		/*
		 * Handles fling events (swiping)
		 * 
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
		 */
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				// When a right to left swipe is detected then call slideLeft
				slideLeft(leftIntent);
				return true;
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				// When a left to right swipe is detected then call slideRight
				slideRight(rightIntent);
				return true;
			}

			return false;
		}
	}

	/* 
	 * Method gets called when user touches screen
	 * Passes all events to the gesture detector.  
	 */

	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector != null) {
			if (gestureDetector.onTouchEvent(event))
				return true;
			else
				return false;
		}

		return false;
	}


	/**
	 * Method gets called when user swipes right to left
	 * Activity on the right gets started
	 *  
	 * @param nextClass
	 */
	public void slideLeft(Intent nextClass) {
		startActivityIfNeeded(nextClass, defaultIntentCode);
		overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
	}


	/**
	 * Method gets called when user swipes left to right
	 * Activity on the left gets started
	 * @param nextClass
	 */
	public void slideRight(Intent nextClass) {
		startActivityIfNeeded(nextClass, defaultIntentCode);
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
	}

	/**
	 * 
	 * Sets a alarm to notify the user. 
	 * @param section
	 * @param title
	 * @param details
	 * @param alerttime
	 */
	public void setAlarm(String section, CharSequence title,
			CharSequence details, Date alerttime) {
		Intent intent = new Intent(this, LokaliteNotification.class);
		intent.putExtra("section", section);
		intent.putExtra("title", title);
		intent.putExtra("details", details);
		intent.putExtra("alerttime", alerttime.getTime());
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000,
				pendingIntent);
	}

	/**
	 * Returns true if the phone is online otherwise false.
	 * @return
	 */
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isAvailable()
				&& cm.getActiveNetworkInfo().isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Updates the current list by clearing all items 
	 * and re adding them.
	 * @param <T>
	 * @param newList
	 */
	public <T extends ParentDTO> void updateList(ArrayList<T> newList) {
		// Get the preferences that affect events and orgs
		int locCityPref = Integer.parseInt(preferences.getString("LocRadiusPref", "20"));
		float[] results = new float[3];
		
		myAdapter.clear();
		if (newList != null && newList.size() > 0) {
			int i;
			for (i = 0; i < newList.size(); i++)
			{	
				if (newList.get(i).getLat() != null && newList.get(i).getLon() != null)
				{
					Location.distanceBetween(myLocation.getLatitude(), myLocation.getLongitude(), newList.get(i).getLat(), newList.get(i).getLon(), results);

					// Convert meters to miles
					results[0] *= 0.000621371192;

					if (results != null)
					{
						if (results[0] < locCityPref)
						{
							myAdapter.add(newList.get(i));
						}
					}
				}
				else
				{
					myAdapter.add(newList.get(i));
				}
			}
			
			Comparator locationDistComparator = new Comparator<T>() {
				@Override
				public int compare(T locationOne, T locationTwo) {
					float[] resultsOne = new float[3];
					float[] resultsTwo = new float[3];
					
					if (locationOne.getLat() != null && locationOne.getLon() != null && locationTwo.getLat() != null && locationTwo.getLon() != null)  
					{
						Location.distanceBetween(myLocation.getLatitude(), myLocation.getLongitude(), locationOne.getLat(), locationOne.getLon(), resultsOne);
						Location.distanceBetween(myLocation.getLatitude(), myLocation.getLongitude(), locationTwo.getLat(), locationTwo.getLon(), resultsTwo);
						
						if (resultsOne != null && resultsTwo != null)
						{
							if (resultsOne[0] < resultsTwo[0])
							{
								return -1;
							}
							else if (resultsOne[0] > resultsTwo[0])
							{
								return 1;
							}
							else 
							{
								return 0;
							}
						}
						else if (resultsOne != null && resultsTwo == null)
						{
							return 1;
						}
						else
						{
							return -1;
						}
					}
					else if (locationOne.getLat() != null && locationOne.getLon() != null)
					{
						return 1;
					}
					else if (locationTwo.getLat() != null && locationTwo.getLon() != null)
					{
						return -1;
					}
					
					return 0;
				}
			};
			myAdapter.sort(locationDistComparator);
		}
		
		myAdapter.notifyDataSetChanged();
	}

	/**
	 * Adds a new item to the end of the list.
	 * Sets notifyChanged to true so the list updates
	 * @param <T>
	 * @param item
	 */
	public <T extends ParentDTO> void appendList(T item) {
		myAdapter.add(item);
		myAdapter.notifyDataSetChanged();
	}

	/* 
	 * Handles key events from the phone. 
	 * (non-Javadoc)
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (popup != null) {
				if (popup.isShowing()) {
					popup.dismiss();
					return true;
				}
			}
		}

		return super.onKeyDown(keyCode, event);
	}
}
