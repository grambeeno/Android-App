package lokalite.android.app;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.blueduck.collegepedia.dto.OrganizationDTO;

/*
 * Serves as a link between our custom list view and the lists containing items.
 */
public class OrganizationAdapter extends ArrayAdapter<OrganizationDTO>
{
    // Local variable to hold all the event items
    private ArrayList<OrganizationDTO> items;
	private Activity parentActivity;
    private boolean isFavs;
    

    public OrganizationAdapter(Activity parent,Context context, int textViewResourceId, ArrayList<OrganizationDTO> items, boolean isFavorites) {
            super(context, textViewResourceId, items);
            this.items = items;
            this.parentActivity = parent;
            this.isFavs = isFavorites;
    }
    

   
    /**
     * Place the content from the organization item into objects in the list view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if(this.isFavs == true){
                	v = vi.inflate(R.layout.profilebusinessitem, null);	
                }
                else{
                	v = vi.inflate(R.layout.organizationitem, null);
                }                    
            }
            OrganizationDTO o = items.get(position);
            if (o != null) {
                    TextView name = (TextView) v.findViewById(R.id.businessname);
                    TextView id = (TextView)v.findViewById(R.id.businessId);
                    
                    if (name != null) {
                        name.setText(o.getName());
                    }
                    if(id != null){
                        id.setText(o.getId().toString());
                    }
                    
                    if(this.isFavs == true)
                    {
                    	TextView desc = (TextView) v.findViewById(R.id.businessdesc);
                    	if(desc != null){
                            desc.setText("Description: " + o.getDescription());
                        }
                    }
            }
            
            return v;
    }
}