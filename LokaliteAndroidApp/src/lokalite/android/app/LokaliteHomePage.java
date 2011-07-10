package lokalite.android.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

/**
 * 
 * @author Jonathan Tsui
 *
 */

/**
 * LokaliteAndroidMain: Starting page of Lokalite's Android Application
 */
/**
 * @author matthew
 *
 */
public class LokaliteHomePage extends LokaliteActivity {

	/* 
	 *  Called when the activity is first created.
	 * (non-Javadoc)
	 * @see lokalite.android.app.LokaliteActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.homepage);

		// For disabling swiping (just to be safe)
		disableSwipe();
	}

	public void signInButtonPressed(View v){
		Intent about = new Intent(v.getContext(), LokaliteAndroidMain.class); 
		startActivityIfNeeded(about, 0);
	}
	
	public void signUpButtonPressed(View v){
		Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.lokalite.com/signup"));
		startActivity(myIntent);
	}
	
	public void learnMoreButtonPressed(View v){
		Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://lokalite.com/static/about"));
		startActivity(myIntent);
	}
	
	public void helpButtonPressed(View v){
		Intent about = new Intent(v.getContext(), About.class); 
		startActivityIfNeeded(about, 0);
	}
}
