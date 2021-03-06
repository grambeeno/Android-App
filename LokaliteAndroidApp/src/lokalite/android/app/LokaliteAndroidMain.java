package lokalite.android.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

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
public class LokaliteAndroidMain extends LokaliteActivity {
    
    /* 
     *  Called when the activity is first created.
     * (non-Javadoc)
     * @see lokalite.android.app.LokaliteActivity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        
        // Set the title layout and text
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.lokaliteheader);
        lokaliteTitle = (TextView) findViewById(R.id.lokalitetitle);
        this.lokaliteTitle.setText("Lokalite");
        
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));
        
        // Grid positions go:
        // 0 1 2
        // 3 4 5
        // 6 7 8
        // etc.
        //  
        // 0 - Event Tab Host
        // 1 - Business Tab Host
        // 2 - Favorites
        // 3 - Settings
        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            	int myReqCode = 0;
            	
            	switch (position)
            	{
            	case 0:
            		Intent eventTabHost = new Intent(v.getContext(), EventsCat.class);
                	startActivityIfNeeded(eventTabHost, myReqCode);
            		break;
            	case 1:
            		Intent business = new Intent(v.getContext(), OrganizationCat.class);
                	startActivityIfNeeded(business, myReqCode);
            		break;
            	case 2:
            		Intent favorites = new Intent(v.getContext(), ProfileTabHost.class);
                	startActivityIfNeeded(favorites, myReqCode);
            		break;
            	case 3:
            		Intent settings = new Intent(v.getContext(), Settings.class);
                	startActivityIfNeeded(settings, myReqCode);
            		break;
            	}
            	
            }
        });
        
        // For disabling swiping (just to be safe)
        disableSwipe();
    }
    

    /**
     * Custom image adapter so we can display icons 
     *
     */
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            // if it's not recycled, initialize some attributes
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mThumbIds[position]);
            return imageView;
        }
        

        // references to our images
        private Integer[] mThumbIds = {
                R.drawable.events, R.drawable.organizations,
                R.drawable.favorites, R.drawable.settings,
        };
    }
}
