package ICUP.App;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;


public class MapperOverlay extends ItemizedOverlay {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	Context mContext; 
	
	/**
	 * Constructs a new mapper overlay 
	 * @param defaultMarker
	 */
	public MapperOverlay(Drawable defaultMarker) {
		//Bounds need to be defined. 
		//Setting center-point at bottom of image to be point attached to map coordinates
		super(boundCenterBottom(defaultMarker));
		// TODO Auto-generated constructor stub
	}
	
	public MapperOverlay(Drawable defaultMarker, Context context){
		super(defaultMarker);
		mContext = context; 
	}

	/*
	 * Creates a new map overlay item
	 *  (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#createItem(int)
	 */
	@Override
	protected OverlayItem createItem(int arg0) {
		// TODO Auto-generated method stub
		return mOverlays.get(arg0);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mOverlays.size();
	}
	
	@Override
	protected boolean onTap (int index){
		/*
		OverlayItem item = mOverlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet()); 
		dialog.show();
		*/
		return true;
	}
	
	
	/**
	 * Method to add new OverlayItems to ArrayList
	 * @param overlay
	 */
	public void addOverlay(OverlayItem overlay){
		mOverlays.add(overlay);
		populate();
	}
	

}
