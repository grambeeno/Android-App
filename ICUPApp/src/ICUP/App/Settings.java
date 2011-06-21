package ICUP.App;

import android.os.Bundle;
import android.preference.PreferenceCategory;

/**
 * 
 * @author ICUP
 *
 */

/**
 * Settings: Allows the user to modify ICUP settings 
 */
public class Settings extends ICUPPreferenceActivity
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
