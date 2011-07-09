package lokalite.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.TextView;

/**
 * 
 * @author Jonathan Tsui
 *
 */

/**
 * LokaliteActivity: Sub-class for all of Lokalite that implements common features
 */
public class LokalitePreferenceActivity extends PreferenceActivity  {
	
	private static final int SWIPE_MIN_DISTANCE = 50;
	private static final int SWIPE_MAX_OFF_PATH = 100;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	private Intent leftIntent;
	private Intent rightIntent;
	
	protected final int defaultIntentCode = 0;
	protected TextView lokaliteTitle;
	
	/* 
	 * Creates a new preference activity
	 * Adds a custome gesture listener 
	 * (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		
		gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        };
	}
	
    /* 
     * Creates OptionsMenu when MENU button is pressed
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
     * a button on the Options Menu is pressed
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
        case R.id.featuredevent:
        	Intent featuredEvent = new Intent(this, FeaturedEvent.class);
        	startActivityIfNeeded(featuredEvent, defaultIntentCode);
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
     * @param newLeftIntent
     * @param newRightIntent
     */
    public void enableSwipe (Intent newLeftIntent, Intent newRightIntent)
    {
    	gestureDetector = new GestureDetector(new MyGestureDetector());
    	leftIntent = newLeftIntent;
    	rightIntent = newRightIntent;
    }
    
    
    /**
     *Disables swiping by de allocating the gesture detector.  
     */
    public void disableSwipe ()
    {
    	// Gesture detector for swiping
        gestureDetector = null;
    }
    
   
    /**
     * Custom gesture detector to handle swipe events 
     */
    class MyGestureDetector extends SimpleOnGestureListener {
        /*
         * Handles "fling" events A.K.A swiping accross the screen
         *  (non-Javadoc)
         * @see android.view.GestureDetector.SimpleOnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    // When a right to left swipe is detected then call slideLeft
                	slideLeft(leftIntent);
                	return true;
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    // When a left to right swipe is detected then call slideRight
                	slideRight(rightIntent);
                	return true;
                }
            
            return false;
        }
    }
    
    
    /* 
     * Method gets called when user touches screen
     * (non-Javadoc)
     * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if (gestureDetector != null)
    	{
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
     * @param nextClass
     */
    public void slideLeft (Intent nextClass) {
        startActivityIfNeeded(nextClass, defaultIntentCode);
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
    }
    
   
   
    /**
     * Method gets called when user swipes left to right
     * Activity on the left gets started
     * @param nextClass
     */
    public void slideRight (Intent nextClass) {
        startActivityIfNeeded(nextClass, defaultIntentCode);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }
    
    /* 
     * Handles button events from the phone 
     * Handles search button presses
     * (non-Javadoc)
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH)
		{
    		return true;
		}

		return super.onKeyDown(keyCode, event);    
	}
}
