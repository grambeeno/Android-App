package lokalite.android.app;


import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

/** About: Lists general information about the Lokalite Android Application **/
public class About extends LokaliteActivity{
	private boolean tutorialVisible; 
	private Gallery myThumbnails; 
	private ImageView myImgView; 
	private ImageView myLogo;

	//Setting images for screenshots
	private Integer[] ImgID = {
			R.drawable.intro_about, 
			R.drawable.mainmenu_about,
			R.drawable.options_menu_about,
			R.drawable.navigation_about,
			R.drawable.maps_about,
			R.drawable.navtoevents_about,
			R.drawable.eventscat_about,
			R.drawable.eventslist_about,
			R.drawable.eventspopup_about,
			R.drawable.eventssearch_about,
			R.drawable.eventsadvsearch_about,
			R.drawable.navtoorgs_about,
			R.drawable.orgscat_about,
			R.drawable.orgslist_about,
			R.drawable.orgspopup_about, 
			R.drawable.orgssearch_about,
			R.drawable.orgsadvsearch_about,
			R.drawable.navtofavs_about,
			R.drawable.favs_about,
			R.drawable.favssearch_about,
			R.drawable.favsadvsearchevents_about,
			R.drawable.favsadvsearchorgs_about,
			R.drawable.navtosettings_about,
			R.drawable.settings_about
	};

	//Setting images for thumbnails
	private Integer[] thumbnailID = {
			R.drawable.intro_thumbnail, 
			R.drawable.main_menu_thumbnail,
			R.drawable.options_menu_thumbnail,
			R.drawable.navigation_thumbnail,
			R.drawable.map_thumbnail,
			R.drawable.events_thumbnail,
			R.drawable.events_thumbnail,
			R.drawable.events_thumbnail,
			R.drawable.events_thumbnail,
			R.drawable.events_thumbnail,
			R.drawable.events_thumbnail,
			R.drawable.orgs_thumbnail,
			R.drawable.orgs_thumbnail,
			R.drawable.orgs_thumbnail,
			R.drawable.orgs_thumbnail,
			R.drawable.orgs_thumbnail,
			R.drawable.orgs_thumbnail,
			R.drawable.favs_thumbnail,
			R.drawable.favs_thumbnail,
			R.drawable.favs_thumbnail,
			R.drawable.favs_thumbnail,
			R.drawable.favs_thumbnail,
			R.drawable.settings_thumbnail,
			R.drawable.settings_thumbnail
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.abouthost);

		// Set the title layout and text
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.lokaliteheader);
        lokaliteTitle = (TextView) findViewById(R.id.lokalitetitle);
        this.lokaliteTitle.setText("About");

		//Disable tutorial view
		findViewById(R.id.thumbnailgallery).setVisibility(View.GONE);
		findViewById(R.id.ImageView01).setVisibility(View.GONE);
		myLogo = (ImageView) findViewById(R.id.imageLogo);
		myLogo.setImageResource(R.drawable.lokalite_logo);
		tutorialVisible = false;


		//Setting up Tutorial images
		//Main Image
		myImgView = (ImageView) findViewById(R.id.ImageView01);
		myImgView.setImageResource(ImgID[0]);
		myImgView.setScaleType(ImageView.ScaleType.FIT_START);
		this.setTitle("Help/About - About App");

		//Image thumbnails on top
		myThumbnails = (Gallery)findViewById(R.id.thumbnailgallery);
		myThumbnails.setAdapter(new AddImgAdp(this)); 

		myThumbnails.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View v, int position, long id){
				myImgView.setImageResource(ImgID[position]);
			}
		});

		// Overridden below
		// For swiping
		// You MUST set the intent to the left and right of this current activity/view
		// Forgetting to do so will cause the application to crash.
		Intent leftIntent = new Intent(this, About.class);
		Intent rightIntent = new Intent(this, About.class);
		enableSwipe(leftIntent, rightIntent);
		findViewById(R.id.AboutScrollView).setOnTouchListener(gestureListener);
	}

	/**
	 * This image adapter allows us to easily insert the array of images 
	 * defined at the top of this JAVA class into the UI
	 */
	public class AddImgAdp extends android.widget.BaseAdapter {
		int GalItemBg;
		private Context cont;

		public AddImgAdp(Context c) {
			cont = c;
			TypedArray typArray = obtainStyledAttributes(R.styleable.TutorialGallery);
			GalItemBg = typArray.getResourceId(R.styleable.TutorialGallery_android_galleryItemBackground, 0);
			typArray.recycle();
		}

		public int getCount() {
			return ImgID.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imgView = new ImageView(cont);

			imgView.setImageResource(thumbnailID[position]);
			imgView.setLayoutParams(new Gallery.LayoutParams(80, 70));
			imgView.setScaleType(ImageView.ScaleType.FIT_XY);
			imgView.setBackgroundResource(GalItemBg);

			return imgView;
		}
	}
	
	/**
	 * Starts the tutorial when the button is clicked
	 * @param v
	 */
	public void startTutorialButtonClicked(View v){
		toggleTutorial();
	}

	/**
	 * 
	 * Set UI for tutorial
	 */
	public void toggleTutorial (){
		//Welcome page 
		if(tutorialVisible){
			findViewById(R.id.thumbnailgallery).setVisibility(View.GONE);
			findViewById(R.id.ImageView01).setVisibility(View.GONE);
			findViewById(R.id.lokaliteWelcome).setVisibility(View.VISIBLE);
			findViewById(R.id.tutorialButton).setVisibility(View.VISIBLE);
			findViewById(R.id.lokaliteInfo).setVisibility(View.VISIBLE);
			findViewById(R.id.imageLogo).setVisibility(View.VISIBLE);
			this.setTitle("Help/About - About App");
			tutorialVisible = false;
		}
		//Tutorial page
		else{
			findViewById(R.id.thumbnailgallery).setVisibility(View.VISIBLE);
			findViewById(R.id.ImageView01).setVisibility(View.VISIBLE);
			findViewById(R.id.lokaliteWelcome).setVisibility(View.GONE);
			findViewById(R.id.tutorialButton).setVisibility(View.GONE);
			findViewById(R.id.lokaliteInfo).setVisibility(View.GONE);
			findViewById(R.id.imageLogo).setVisibility(View.GONE);
			this.setTitle("Help/About - Tutorial");
			tutorialVisible = true;
		}
	}

	/**
	 * Opens the Lokalite website when its URL is clicked
	 * @param v
	 */
	public void onLinkClicked(View v){
		Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.lokalite.com/"));
		startActivity(myIntent); 
	}
	
	/**
	 * Back button (on device) handler 
	 * Brings user back to the home About page
	 * @param keyCode, event
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (tutorialVisible == true){
				toggleTutorial();
				return true;
			}	
		}
		return super.onKeyDown(keyCode, event);
	}

	//SWIPING
	/**
	 * Overridden method that handles swipe events from the user
	 * Allows the user to swipe between tutorial images
	 * @param nextClass
	 */
	@Override 
	public void slideLeft (Intent nextClass){
		if (tutorialVisible == true){
			if (myThumbnails.getSelectedItemPosition() < (myThumbnails.getCount()-1))
			{
				myImgView.setImageResource(ImgID[myThumbnails.getSelectedItemPosition()+1]);
				myThumbnails.setSelection(myThumbnails.getSelectedItemPosition()+1);
			}
		}
	}

	/**
	 * Overridden method that handles swipe events from the user
	 * Allows the user to swipe between tutorial images
	 * @param nextClass
	 */
	@Override
	public void slideRight (Intent nextClass){
		if (tutorialVisible == true){
			if (myThumbnails.getSelectedItemPosition() > 0)
			{
				myImgView.setImageResource(ImgID[myThumbnails.getSelectedItemPosition()-1]);
				myThumbnails.setSelection(myThumbnails.getSelectedItemPosition()-1);
			}
		}
	}
}
