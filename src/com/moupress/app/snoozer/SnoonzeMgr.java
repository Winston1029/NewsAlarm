package com.moupress.app.snoozer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import com.moupress.app.R;
import com.moupress.app.ui.SlideButton.OnChangeListener;
import com.moupress.app.ui.SlideButton.SlideButton;

public class SnoonzeMgr {

	private SensorEventListener sensorEventListener;
	private OnGesturePerformedListener gesturePerformedListener;
	private GestureLibrary gestureLib;
	private long lastUpdate;
	private Context context;
	
	private GestureOverlayView gestures;
	private SlideButton dismissSlide;
	
	private SnoozeListener snoozeListener;
	
	private Boolean flipMotionDetector = false;
	
	private Boolean swingMotionDetector = false;
	
	private final int FACE_DOWN = 1;
	private final int FACE_UP = 2;
	
	private int flipSide, preFlipSide;
	
//	private Handler snoozeHandler;
	
	public SnoonzeMgr(Context context)
	{
		this.context = context;
		initListeners();
		
		//1 For Sensor, 2 for Gesture
		//registerListener(GESTURE_SNOOZE_TYPE, snoozeListener);
	}
	
	public SnoonzeMgr(Context context, SnoozeListener listener)
	{
		this.context = context;
		this.snoozeListener = listener;
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		initListeners();
		
		// to change this in actual production ****
		gestureLib = GestureLibraries.fromRawResource(context, R.raw.spells);
		//gestures = (GestureOverlayView) ((Activity) context).findViewById(R.id.gestures);
		if (!gestureLib.load()) {
        	((Activity) context).finish();
        	//System.out.println("Library Loading has not finished!");
        }
		
		lastUpdate = System.currentTimeMillis();
		
		this.isDismissed = false;
		this.isSnoozed = false;
	}
	
	private void initListeners() {
		gesturePerformedListener = new OnGesturePerformedListener(){

			@Override
			public void onGesturePerformed(GestureOverlayView overlay,
					Gesture gesture) {
				ArrayList<Prediction> predictions = gestureLib.recognize(gesture);

				// We want at least one prediction
				if (predictions.size() > 0) {
					Prediction prediction = predictions.get(0);
					// We want at least some confidence in the result
					if (prediction.score > 0.5) {
						// Show the spell
						//snoozeListener.onSnoozed();
						snoozeTriggered();
						System.out.println("Gesture Snoozed !");
					}
				}
			}};
			
		sensorEventListener = new SensorEventListener() {

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}

			@Override
			public void onSensorChanged(SensorEvent event) {
				if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
				{
					float[] values = event.values;
					
					float x = values[0];
					float y = values[1];
					float z = values[2];
					
					if(swingMotionDetector == true)
					{
						float accelationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
						long actualTime = System.currentTimeMillis();
						
						if(accelationSquareRoot>2)
						{
							if (actualTime - lastUpdate < 200) {
								return;
							}
							lastUpdate = actualTime;
							//snoozeListener.onSnoozed();
							snoozeTriggered();
							System.out.println("Swing Snoozed !");
							isSnoozed = true;
						}
					}
					
					if(flipMotionDetector == true)
					{
							flipSide = (z>=0)?FACE_UP:FACE_DOWN;
							if(preFlipSide != 0)
							{
								if(flipSide != preFlipSide)
								{
									//snoozeListener.onSnoozed();
									snoozeTriggered();
									System.out.println("Flip Snoozed");
									isSnoozed = true;
								}
							}
							preFlipSide = flipSide;
					}
				}
			}
		};
	}
	
	public void setSnoozeListener(SnoozeListener listener) {
		snoozeListener = listener;
	}
	
	public void setGestureLib (GestureLibrary lib) {
		gestureLib = lib;
	}
	
	public void setGestureOverlayView(View v) {
		gestures = (GestureOverlayView)v;
	}
	
//	private Runnable alarmTriggerTask = new Runnable()
//	{
//
//		@Override
//		public void run() {
//			
//			//System.out.println("Snoozed Again!");
//			Toast.makeText(context, "Snoozed", 1000);
//			snoozeListener.onSnoozedAgain();
//		}
//		
//	};
	public void snoozeTriggered()
	{
		this.snoozeListener.onSnoozed();
		//this.snoozeHandler = new Handler();
		//snoozeHandler.postDelayed(alarmTriggerTask, Const.SNOOZE_DUR);
		
	}
	
	
	public static final int  GESTURE_SNOOZE_TYPE = 0;
	public static final int FLIP_SNOOZE_TYPE = 1;
	public static final int  SWING_SNOOZE_TYPE = 2;
	
	private SensorManager sensorManager;
	public void unRegisterListener(int snoozeType) {
		// TODO Auto-generated method stub
		if(snoozeType==SWING_SNOOZE_TYPE||snoozeType==FLIP_SNOOZE_TYPE)
		{
			if(snoozeType == FLIP_SNOOZE_TYPE)
				this.flipMotionDetector = false;
			if(snoozeType == SWING_SNOOZE_TYPE)
				this.swingMotionDetector = false;
			
			sensorManager.unregisterListener(sensorEventListener);
		} else if (snoozeType == GESTURE_SNOOZE_TYPE)
		{
			System.out.println("Unregister Gesture Listener!");
			gestures.removeAllOnGesturePerformedListeners();
			gestures.removeAllOnGestureListeners();
			gestures.removeAllOnGesturingListeners();
		}
	}
	
	private boolean isSnoozed,isDismissed;
	public void registerListener(int snoozeType, SnoozeListener listener) {
		isSnoozed = false;
		isDismissed = false;
		if (listener != null) {
			snoozeListener = listener;
		} // else use default snooze lister, which is nothing but print line
		
		if(snoozeType==FLIP_SNOOZE_TYPE||snoozeType == SWING_SNOOZE_TYPE)
		{
			//gestures.removeAllOnGestureListeners();
			if(snoozeType == FLIP_SNOOZE_TYPE)
			{
				this.flipMotionDetector = true;
				flipSide = 0;
				preFlipSide = 0;
			}
			if(snoozeType == SWING_SNOOZE_TYPE)
				this.swingMotionDetector = true;
			
			sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		}
		else if(snoozeType == GESTURE_SNOOZE_TYPE)
		{
			System.out.println("Register Gesture");
			//sensorManager.unregisterListener(snoozeSensorEventListener);
		    this.unRegisterListener(GESTURE_SNOOZE_TYPE);
			gestures.addOnGesturePerformedListener(gesturePerformedListener);
		}
	}

	public void setDismissSlide(SlideButton dismissSlide) {
		// TODO Auto-generated method stub
		this.dismissSlide = dismissSlide;
		this.dismissSlide.setOnChangedListener(new OnChangeListener()
	    {

	    	public void OnChanged(int weekdayPos,boolean direction,View v) {
	    		
	    		if(direction == true && weekdayPos == 2)
	    		{
	    			snoozeListener.onDismissed();
	    			isSnoozed = false;
	    			isDismissed = true;
	    		}
	    	}

			@Override
			public void OnSelected(int weekdayPos,  View v, int mode) {
				
				//dismissViewAdapter.testTxtSwitcher();
			}
	    });
	}
}
