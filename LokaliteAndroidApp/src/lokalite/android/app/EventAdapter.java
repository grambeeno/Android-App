package lokalite.android.app;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.blueduck.collegepedia.dto.EventDTO;

// This adapter is the communication channel between the class and the view
// Any changes 
public class EventAdapter extends ArrayAdapter<EventDTO>
{
	// Local variable to hold all the event items
    private ArrayList<EventDTO> items;
    private Activity parentActivity;
    //boolean isFavEvent; 
    public enum adapterType {EVENT, FAVORITE, LOAD};
    
    adapterType curType;
   
    /**
     * Constructs a new EventAdapter including the adapter type.
     * Either a regular list item or a add more list item 
     * @param parent 
     * @param context
     * @param textViewResourceId
     * @param items
     * @param t
     */
    public EventAdapter(Activity parent, Context context, int textViewResourceId, ArrayList<EventDTO> items, adapterType t) {
            super(context, textViewResourceId, items);
            this.items = items;
            this.parentActivity = parent;
            //this.isFavEvent = isFavorites; 
            curType = t;
    }
    
    
    /* 
     * This function actually places the content from the Event Item objects onto the ListView.
     * (non-Javadoc)
     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            EventDTO o = items.get(position);
            LayoutInflater vi = (LayoutInflater)parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                
                switch(curType){
                	case EVENT:
                		v = vi.inflate(R.layout.eventitem, null);
                		break;
                	case FAVORITE:
                		v = vi.inflate(R.layout.faveventitem, null);
                		break;
                }      
            
           
            if(o.getDescription() == "Click To Load More Events"){
            	v =vi.inflate(R.layout.loaditem, null);
            	
            }
            
            if (o != null) {
                    TextView title = (TextView) v.findViewById(R.id.eventtitle);
                    TextView time = (TextView) v.findViewById(R.id.eventstarttime);
                    TextView id = (TextView)v.findViewById(R.id.eventId);
                    
                    if (title != null) {
                    	title.setText(o.getName());
                    }
                    if (time != null) {
                    	time.setText("Start Time: " + o.getStartDatetime().toLocaleString());
                    }
                    if(id != null){
                    	id.setText(o.getId().toString());
                    }
                    
                    if (curType == adapterType.FAVORITE)
                    {
                    	TextView desc = (TextView) v.findViewById(R.id.eventdesc);
                    	if(desc != null){
                        	desc.setText(o.getDescription());
                        }
                    }
            }
                           
            return v;
    }
}
    
