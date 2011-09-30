package com.moupress.app.weather;


import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
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
import com.moupress.app.util.NetworkConnection;
import com.moupress.app.weather.Weather.Temperature;
import com.moupress.app.weather.Weather.UnitSystem;
import com.moupress.app.weather.Weather.WeatherCondition;

public class WeatherMgr implements Runnable {

	
	private static final String TAG = "NewsAlarm";
	private static final String DEFAULT_LOCATION = "Singapore";
	
	private Context context;

	private NetworkConnection nc;
		
	public WeatherMgr(Context context) {
		this.context = context;
		//Connect Check
		this.nc = new NetworkConnection(Const.HOST_WEATHER_SERVICE,context);
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
        
        if(nc.checkInternetConnection()==true)
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
		
		if(nc.checkInternetConnection()==true)
		try {
			weather.query(location, Locale.getDefault());
		} catch (WeatherException e) {
			e.printStackTrace();
		}
	}
	
	public Hashtable<String, String> getCurrentWeather() {
		weather = new GoogleWeather();
		
		boolean autoLocation = true;
		Location location = null;
		if (autoLocation) {
			location = queryLocation();
		} else {
			location = new Location(DEFAULT_LOCATION);
		}
		
		if(nc.checkInternetConnection()==true)
		try {
			weather.query(location, Locale.getDefault());
		} catch (WeatherException e) {
			e.printStackTrace();
		}
		
		Hashtable<String, String> weatherDetail = new Hashtable<String, String>();
		String sCondition;
		if (weather.getConditions().size() <= 0) {
			sCondition = "";
        }
		else {
			WeatherCondition currentCondition = weather.getConditions().get(0);
		
			if (currentCondition.temperature.unit.name() == "US")
				sCondition = currentCondition.getConditionText() + " " + currentCondition.temperature.getCurrent() + " F";
			else  sCondition = currentCondition.getConditionText() + " " + currentCondition.temperature.getCurrent() + " C";
		}
		
		weatherDetail.put(Const.WEATHERINFO_CURRENT, sCondition);
		Toast.makeText(context, "Weather Loaded successfully", Toast.LENGTH_SHORT).show();
		return weatherDetail;
	}
	
	public Hashtable<String, String> getWeatherDetails() {
		Hashtable<String, String> weatherDetail = new Hashtable<String, String>();
		weatherDetail.put(Const.WEATHERINFO_WINDHUMIDITY, getWindHumidity());
		weatherDetail.put(Const.WEATHERINFO_FORCAST, getForecasts());
		return weatherDetail;
	}
	
	private String getForecasts() {
        String forecastsStr = new String();
        for (int i = 1; i < 4; i++) {
            if (weather.getConditions().size() <= i) {
                break;
            }
            WeatherCondition forecastCondition = weather.getConditions().get(i);
            Temperature forecastTemp = forecastCondition.getTemperature(forecastCondition.temperature.unit);
            
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(weather.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, i);
            Date day = calendar.getTime();
            
            forecastsStr = forecastsStr + day.toString() + Const.WEATHERINFO_SEPARATOR + forecastCondition.getConditionText() + Const.WEATHERINFO_SEPARATOR;
            forecastsStr = forecastsStr + forecastTemp.getHigh() + Const.WEATHERINFO_SEPARATOR + forecastTemp.getLow() + Const.WEATHERINFO_SEPARATOR;
        }
        return forecastsStr;
    }
	
	private String getWindHumidity() {
		WeatherCondition currentCondition = weather.getConditions().get(0);
        return currentCondition.getHumidityText() + Const.WEATHERINFO_SEPARATOR + currentCondition.getWindText();
    }
}
