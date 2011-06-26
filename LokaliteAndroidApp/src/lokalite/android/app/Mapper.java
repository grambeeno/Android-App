package lokalite.android.app;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class Mapper extends MapActivity {

	//To find longitude and latitude
	private LocationManager lm; 
	private LocationListener locationListener; 
	protected final int defaultIntentCode = 0;  
	private MapController mc; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mapview);
	    
	    Bundle extras = getIntent().getExtras();
	    
	    //use LocationManager class to obtain GPS location 
	    lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    locationListener = new MyLocationListener();
	    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,locationListener); 
	    

	    MapView mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    mc = mapView.getController();
	
	    //Overlay Stuff here
	    List<Overlay> mapOverlays = mapView.getOverlays();
	    Drawable drawable = this.getResources().getDrawable(R.drawable.map_pin);
	    MapperOverlay itemizedoverlay =  new MapperOverlay(drawable);
	    
	    double lat= extras.getDouble("lat")*(double)1E6;
	    double lon= extras.getDouble("lon")*(double )1E6;
	    
	    GeoPoint point = new GeoPoint(new Double(lat).intValue(), new Double(lon).intValue());
	    OverlayItem overlayitem = new OverlayItem(point,"Hello", "Here");
	    
	    mc.animateTo(point);
	    mc.setZoom(14);
	    Log.i("mapper", "Mapping location:" + extras.getDouble("lat") + " " + extras.getDouble("lon"));
	    
	    itemizedoverlay.addOverlay(overlayitem);
	    mapOverlays.add(itemizedoverlay);
	    
	    
	}
	
	
	   @Override
	    // Creates OptionsMenu when MENU button is pressed
	    public boolean onCreateOptionsMenu(Menu menu) {
	    	
	        MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.mainmenu, menu);
	        return true;
	    }
	    
	    @Override
	    // Brings the proper activity into the current focus when
	    // a button on the Options Menu is pressed
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	        case R.id.main:
	        	Intent mainPage = new Intent(this, LokaliteAndroidMain.class);
	        	startActivityIfNeeded(mainPage, defaultIntentCode);
	            return true;
	        case R.id.featuredevent:
	        	Intent featuredEvent = new Intent(this, FeaturedEvent.class);
	        	startActivityIfNeeded(featuredEvent, defaultIntentCode);
	            return true;
	        case R.id.events:
	        	Intent events = new Intent(this, EventsCat.class);
	        	  startActivityIfNeeded(events, defaultIntentCode);
	            return true;
	        case R.id.organizations:
	        	Intent organizations = new Intent(this, BusinessCat.class);
	        	  startActivityIfNeeded(organizations, defaultIntentCode);
	            return true;
	        case R.id.favorites:
	        	Intent favorites = new Intent(this, FavoritesTabHost.class);
	        	  startActivityIfNeeded(favorites, defaultIntentCode);
	            return true;
	        case R.id.settings:
	        	Intent settings = new Intent(this, Settings.class);
	        	  startActivityIfNeeded(settings, defaultIntentCode);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	        }
	    }
	
	
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}
	
	//Updates location once coordinates have changed
	// This is required to have GPS working
	private class MyLocationListener implements LocationListener{
		public void onLocationChanged (Location loc){
			if (loc != null) { 
/*
				GeoPoint p = new GeoPoint(
						(int) (loc.getLatitude() * 1E6), 
						(int) (loc.getLongitude() * 1E6));
						(int)(loc.getLatitude()), 
						(int)(loc.getLongitude()));
				mc.animateTo(p); 
				mc.setZoom(16); 
				mapView.invalidate();
*/				
			}
		}
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        public void onStatusChanged(String provider, int status, 
            Bundle extras) {
            // TODO Auto-generated method stub
        }
	}



}
