package lokalite.android.app;

import android.os.Bundle;
import android.preference.PreferenceCategory;

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
        
        PreferenceCategory myCategory = (PreferenceCategory) findPreference("notifications");
        myCategory.removePreference(findPreference("lastUpdated"));
        
        // No swiping from Settings
        disableSwipe();
    }
}
