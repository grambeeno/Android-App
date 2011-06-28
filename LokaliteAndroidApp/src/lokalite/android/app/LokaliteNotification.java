package lokalite.android.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LokaliteNotification extends BroadcastReceiver {
	SharedPreferences preferences;
	NotificationManager myNotificationManager;

	/*
	 * Handles receiving notification events from the phone. 
	 *  (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String notifType = preferences.getString("notifTypePref", "both");
		myNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		String section = intent.getStringExtra("section");
		CharSequence title = intent.getCharSequenceExtra("title");
		CharSequence details = intent.getCharSequenceExtra("details");
		long alerttime = intent.getLongExtra("alerttime", System.currentTimeMillis());
		
		
		if (title != null && details != null)
		{
			PendingIntent contentIntent;
			// This is who should be launched if the user selects our notification.
			if (section == "event")
			{
				contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, EventsCat.class), 0);
			}
			else
			{
				contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, OrganizationCat.class), 0);
			}

			Notification notification = new Notification(
					R.drawable.events,       // the icon for the status bar
					title,                      // the text to display in the ticker
					alerttime); 				// the timestamp for the notification

			notification.setLatestEventInfo(
					context,                    // the context to use
					title,						// the title for the notification
					details,                    // the details to display in the notification
					contentIntent);             // the contentIntent (see above)

			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			
			// The type of alert
			// DEFAULT_ALL		Use all default values (where applicable).
			// DEFAULT_LIGHTS 	Use the default notification lights.
			// DEFAULT_SOUND 	Use the default notification sound.
			// DEFAULT_VIBRATE	Use the default notification vibrate.
	        if (notifType.equals("vibrate"))
	        {
	        	notification.defaults = Notification.DEFAULT_VIBRATE;
	        }
	        else if (notifType.equals("sound"))
	        {
	        	notification.defaults = Notification.DEFAULT_SOUND;
	        }
	        else
	        {
	        	notification.defaults = Notification.DEFAULT_ALL;
	        }

			//myNotificationManager.notify(1, notification);
		}
	}
}