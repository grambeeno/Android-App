package lokalite.android.app;

import android.os.Bundle;
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
}
