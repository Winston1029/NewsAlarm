package com.moupress.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;


public class NewsAlarmActivity extends Activity {
    
	private PubSub pubsub;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (Const.ISDEBUG) {
        	setContentView(R.layout.main);
        }  else {
        	setContentView(R.layout.news_alarm_ui);
        }
        
        
        pubsub = new PubSub(getBaseContext(), this);
    }
    
    
}