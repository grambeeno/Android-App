package ICUP.App;


import java.sql.Time;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * 
 * @author ICUP
 *
 */

/**
 * CountdownView: Custom textView to act as our countdown timer
 */
public class CountdownView extends TextView {
	
	Context context;
	private long endTime = 0;
	int pad = 5;
	String TAG = getClass().getName();
	String time;	// holds the time from the XML args
	
	private Time timeRemaining;		// time remaining
	private CountDownTimer countdown;
	
	private int color;
	public CountdownView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		color = 0xFF629642;
		
		Log.i(TAG, "Attributes...");
		
		// parse the XML attributes passed to the object
		for(int i=0; i<attrs.getAttributeCount(); i++){
			Log.i(TAG, attrs.getAttributeName(i));
			/*
			 * Read value of custom attributes
			 */

		}
		this.time = attrs.getAttributeValue("http://schemas.android.com/apk/res/ICUP.App", "setEndTime");
		Log.i(TAG, "String time="+time);
		// Grab the end time from the XML file and convert it into a long
		this.endTime = Long.parseLong(attrs.getAttributeValue("http://schemas.android.com/apk/res/ICUP.App", "setEndTime"));
		timeRemaining = new Time(endTime - System.currentTimeMillis() ); // compute time remaining
		
		init();
		
		
	}

	// extra constructor incase we don't get attr's
	public CountdownView(Context context){
		super(context);
		this.context = context;
		Log.i(TAG, "MyTextView(ctxt)");
		init();
	}

	@Override
	protected void onDraw(Canvas canvas){
		Paint paint = new Paint();
		paint.setColor(color);
		RectF rect = new RectF(this.getWidth()/2-55,0,(this.getWidth()/2+55),this.getHeight());
		canvas.drawRoundRect(rect, 10, 10, paint);
		super.onDraw(canvas);
		
	}
	
	private void init(){
		setPadding(3, 3, 3, 3);		// set the padding around the textView
		// set Size
		this.setTextSize(18);
		Log.i(TAG, "FINAL COUNTDOWN"+ Long.toString( System.currentTimeMillis()));
		countdown =  new CountDownTimer(endTime - System.currentTimeMillis(), 1000) {
			// overrided onTick
			@Override
			public void onTick(long millisUntilFinished) {
		    	 timeRemaining.setTime(millisUntilFinished); //get the time remaining
		    	 updateText(timeRemaining.toString()); 	// convert the time to "hh:mm:ss"
		     }

			@Override
			public void onFinish() {
				 Log.i(TAG, "Finished");
				 updateText("Deal has Expired");
			}
		  }.start();
		
	}
	
	// needed to update the textview from inside the countdown class
	public void updateText(String newText){
		this.setText(timeRemaining.toString()+"\nRemains");	
	}
	
	public void setColor(int newColor){
		this.color= newColor;
		invalidate();
	}

	// Method to set the time from the JSON string
	public void setEndTime(long endTime) {
		// TODO Auto-generated method stub
		countdown.cancel();
		this.endTime = endTime;
		// re-compute the time remaining and restart the timer
		init();
	}
	
	public void countdownEnabled(boolean enabled){
		if(enabled){
			countdown.start();
		}
		else{
			countdown.cancel();
		}
	
	}
}

