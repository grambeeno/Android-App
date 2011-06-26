package lokalite.android.app;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

// The magic-maker. This adapter is the communication channel between the class and the view
// Any changes 
public class CategoryListAdapter extends ArrayAdapter<String>
{
	// Local variable to hold all the event items
    private ArrayList<String> items;
    private Activity parentActivity;
   
    /**
     * Creates a new categoryListAdapter. Used for populating eventscat and buisnesscat.
     * @param parent
     * @param context
     * @param textViewResourceId
     * @param items
     */
    public CategoryListAdapter(Activity parent, Context context, int textViewResourceId, ArrayList<String> items) {
            super(context, textViewResourceId, items);
            this.items = items;
            this.parentActivity = parent;
    }
    
    
    /* 
     * This function actually places the content from the Event Item objects onto the ListView.
     * (non-Javadoc)
     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.catitem, null);
            }
            String o = items.get(position);
            if (o != null) {
                    TextView title = (TextView) v.findViewById(R.id.catName);
                    //TextView id = (TextView)v.findViewById(R.id.catId);
                    if (title != null) {
                    	title.setText(o);
                    }
            } 
            return v;
    }
}