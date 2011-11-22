package com.moupress.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class NewsAlarmService extends Service {

	private static final String TAG = "NewsAlarmService";
	private PubSub pubsub;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		 super.onStartCommand(intent, flags, startId);
		 return START_STICKY;
	}



	@Override
	public void onCreate() {
		//Toast.makeText(this, "NewsAlarm Service Created", Toast.LENGTH_SHORT).show();
		System.out.println("Service is created!");
		pubsub = new PubSub(getBaseContext(), this);
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onDestroy() {
		//Toast.makeText(this, "NewsAlarm Service Stopped", Toast.LENGTH_SHORT).show();
		//startService(new Intent(this, NewsAlarmService.class));
		Log.d(TAG, "onDestroy");
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		//Toast.makeText(this, "NewsAlarm Service Started", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onStart");
	}

}
