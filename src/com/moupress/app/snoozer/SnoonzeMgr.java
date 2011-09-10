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

public class SnoonzeMgr {

	private SensorEventListener sensorEventListener;
	private OnGesturePerformedListener gesturePerformedListener;
	private GestureLibrary gestureLib;
	private long lastUpdate;
	private Context context;
	
	private GestureOverlayView gestures;
	
	private SnoozeListener snoozeListener;
	
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
					if (prediction.score > 1.0) {
						// Show the spell
						snoozeListener.onSnoozed();
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
					
					float accelationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
					long actualTime = System.currentTimeMillis();
					
					if(accelationSquareRoot>2)
					{
						if (actualTime - lastUpdate < 200) {
							return;
						}
						lastUpdate = actualTime;
						snoozeListener.onSnoozed();
						isSnoozed = true;
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
	
	public static final int  SENSOR_SNOOZE_TYPE = 1;
	public static final int  GESTURE_SNOOZE_TYPE = 2;
	public static final int SWING_SNOOZE_TYPE = 3;
	
	private SensorManager sensorManager;
	public void unRegisterListener(int snoozeType) {
		// TODO Auto-generated method stub
		if(snoozeType==SENSOR_SNOOZE_TYPE)
		{
			sensorManager.unregisterListener(sensorEventListener);
		} else if (snoozeType == GESTURE_SNOOZE_TYPE)
		{
			gestures.removeAllOnGestureListeners();
		}
	}
	
	private boolean isSnoozed;
	public void registerListener(int snoozeType, SnoozeListener listener) {
		isSnoozed = false;
		if (listener != null) {
			snoozeListener = listener;
		} // else use default snooze lister, which is nothing but print line
		
		if(snoozeType==SENSOR_SNOOZE_TYPE)
		{
			//gestures.removeAllOnGestureListeners();
			sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		}
		else if(snoozeType == GESTURE_SNOOZE_TYPE)
		{
			//sensorManager.unregisterListener(snoozeSensorEventListener);
			gestures.addOnGesturePerformedListener(gesturePerformedListener);
		}
	}
}
