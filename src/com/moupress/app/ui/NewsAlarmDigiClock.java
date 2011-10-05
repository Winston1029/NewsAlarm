package com.moupress.app.ui;

import java.util.Calendar;
import com.moupress.app.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;



public class NewsAlarmDigiClock extends TextView{

	
	Calendar mCalendar;
    private final static String m12 = "h:mm a";
    private final static String m24 = "k:mm";
    private final static String dt ="dd,MMM";
    private final static String ww = "EEEE";
    private FormatChangeObserver mFormatChangeObserver;

    private Runnable mTicker;
    private Handler mHandler;
    private String contentDisplay;

    private boolean mTickerStopped = false;

    private String mFormat;

    public NewsAlarmDigiClock(Context context) {
        super(context);
        initClock(context);
        System.out.println("Non Attr Constructor! ");
    }

    public NewsAlarmDigiClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NewsAlarmDigiClock);
        contentDisplay = a.getString(R.styleable.NewsAlarmDigiClock_content);
        a.recycle();
        initClock(context);
        //System.out.println("Non Custom Attr Constructor! "+contentDisplay);
    }
    

    private void initClock(Context context) {
        //Resources r = mContext.getResources();

        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }

        mFormatChangeObserver = new FormatChangeObserver();
        getContext().getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver);

        setFormat();
    }

    @Override
    protected void onAttachedToWindow() {
        mTickerStopped = false;
        super.onAttachedToWindow();
        mHandler = new Handler();

        /**
         * requests a tick on the next hard-second boundary
         */
        mTicker = new Runnable() {
                public void run() {
                    if (mTickerStopped) return;
                    mCalendar.setTimeInMillis(System.currentTimeMillis());
                    setText(DateFormat.format(mFormat, mCalendar));
                    invalidate();
                    long now = SystemClock.uptimeMillis();
                    long next = now + (1000 - now % 1000);
                    mHandler.postAtTime(mTicker, next);
                }
            };
        mTicker.run();
    }

    @Override
    protected void onDetachedFromWindow() {
     super.onDetachedFromWindow();
     mTickerStopped = true;
 }

 /**
  * Pulls 12/24 mode from system settings
  */
 private boolean get24HourMode() {
     return android.text.format.DateFormat.is24HourFormat(getContext());
 }

 private void setFormat() {
	 if(contentDisplay.equals("time"))
	 {
	     if (get24HourMode()) {
	         mFormat = m24;
	     } else {
	         mFormat = m12;
	     }
     }
	 else if(contentDisplay.equals("date"))
	 {
		 mFormat = dt;
	 }
	 else if(contentDisplay.equals("weekday"))
	 {
		 mFormat = ww;
	 }
 }
 
 public int getWeekDayRank()
 {
	 if(contentDisplay.equals("weekday"))
	 {
		 return mCalendar.get(Calendar.DAY_OF_WEEK);
	 }
	return 0;
 }

 private class FormatChangeObserver extends ContentObserver {
     public FormatChangeObserver() {
         super(new Handler());
     }

     @Override
     public void onChange(boolean selfChange) {
         setFormat();
     }
 }
 


}
