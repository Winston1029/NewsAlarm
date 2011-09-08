package com.moupress.app.weather;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.moupress.app.Const;
import com.moupress.app.weather.Weather.WeatherCondition;

public class WeatherMgr extends Service implements Runnable {

	
	private static final String TAG = "NewsAlarm";
	private static final String DEFAULT_LOCATION = "Singapore";
	
	private Context context;
	
	private ImageButton refreshButton;
	private TextView txv_wind;
	private TextView txv_humidity;
	private TextView txv_updatetime;
	private TextView txv_location;
	
	public WeatherMgr(Context context, ImageButton refreshBtn, TextView wind, TextView humidity, TextView updatetime, TextView location) {
		this.context = context;
		this.refreshButton = refreshBtn;
		this.txv_wind = wind;
		this.txv_humidity = humidity;
		this.txv_updatetime = updatetime;
		this.txv_location = location;
		
		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
        });
	}
	
	public WeatherMgr(Context context) {
		this.context = context;
	}

	private Location queryLocation() {
		LocationManager	manager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		
        	
        android.location.Location androidLocation = 
                manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        
        if (androidLocation == null) {
        	return new Location(DEFAULT_LOCATION);
        }
        Geocoder coder = new Geocoder(context);
        List<Address> addresses = null;
        try {
            addresses = coder.getFromLocation(androidLocation.getLatitude(),
                    androidLocation.getLongitude(), 1);
        } catch (IOException e) {
            Log.w(TAG, "cannot decode location", e);
        }
        if (addresses == null || addresses.size() == 0) {
            return new AndroidGoogleLocation(androidLocation);
        }
        
        return new AndroidGoogleLocation(androidLocation, addresses.get(0));
        //return new Location(DEFAULT_LOCATION);
    }

	GoogleWeather weather;
	@Override
	public void run() {
		weather = new GoogleWeather();
		
		boolean autoLocation = false;
		Location location = null;
		if (autoLocation) {
			location = queryLocation();
		} else {
			location = new Location(DEFAULT_LOCATION);
		}
		
		try {
			weather.query(location, Locale.getDefault());
		} catch (WeatherException e) {
			e.printStackTrace();
		}
		updateUI();
	}
	
	public void getWeather() {
		weather = new GoogleWeather();
		
		boolean autoLocation = true;
		Location location = null;
		if (autoLocation) {
			location = queryLocation();
		} else {
			location = new Location(DEFAULT_LOCATION);
		}
		
		try {
			weather.query(location, Locale.getDefault());
		} catch (WeatherException e) {
			e.printStackTrace();
		}
		updateUI();
		Toast.makeText(context, "Weather Loaded successfully", Toast.LENGTH_SHORT).show();
	}

	private void updateUI() {
		if (Const.ISDEBUG) {
			// location & time
			txv_location.setText(weather.getLocation().getText());
			txv_updatetime.setText(weather.getTime().toLocaleString());
			
			//humidity & wind
			if (weather.getConditions().size() <= 0) {
	            return;
	        }
			WeatherCondition currentCondition = weather.getConditions().get(0);
			txv_humidity.setText(currentCondition.getHumidityText());
			txv_wind.setText(currentCondition.getWindText());
		}		
	}

	private boolean isExpired(long timestamp) {
		return false;
    }

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
