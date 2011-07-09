package lokalite.android.app;

import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.view.Window;
import android.widget.TextView;

/**
 * 
 * @author Jonathan Tsui
 *
 */

/**
 * Settings: Allows the user to modify Lokalite settings 
 */
public class Settings extends LokalitePreferenceActivity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        // Set the title layout and text
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.lokaliteheader);
        lokaliteTitle = (TextView) findViewById(R.id.lokalitetitle);
        this.lokaliteTitle.setText("Settings");
        
        PreferenceCategory myCategory = (PreferenceCategory) findPreference("notifications");
        myCategory.removePreference(findPreference("lastUpdated"));
        
        // No swiping from Settings
        disableSwipe();
    }
}
