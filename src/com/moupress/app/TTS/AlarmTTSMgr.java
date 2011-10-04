package com.moupress.app.TTS;

import java.util.Hashtable;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.moupress.app.Const;

public class AlarmTTSMgr {

	private Button btnPlay;
	private Button btnPause;
	private Button btnShutdown;
	
	private Context context;
	private AlarmTTS talker;
    private Activity activity;

	public AlarmTTSMgr(Context context, final Button btnPlay, Button btnPause, Button btnShutDown) {
		this.btnPause = btnPause;
		this.btnPlay = btnPlay;
		this.btnShutdown = btnShutDown;
		
		this.context = context;
		talker = new AlarmTTS(context);
		setTalkerMsg("test");
		
		btnPlay.setOnClickListener(new OnClickListener() {

            public void onClick(View v)
            {
                ttsPlayOrResume();
                btnPlay.setText("Play");
            }

            
        });
        btnPause.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v)
            {
                ttsPause();
                btnPlay.setText("Resume");
            }
            
            
        });
       
        btnShutdown.setOnClickListener(new OnClickListener() {

            public void onClick(View v)
            {
                talker.ShutDown();
            }
            
            
        });
	}
	
	public AlarmTTSMgr(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        talker = new AlarmTTS(activity);
        setDefaultMsg();
    }
	
	private void setDefaultMsg()
	{
	    CalendarTask calenderTask = new CalendarTask(activity);
	    Hashtable[] events = calenderTask.getCalendarTasks();
	    if (events == null)
	    {
	        talker.AddMsgToSpeak("There is no content to read. There is no content to read. There is no content to read.");
            return;
	    }

	    StringBuilder sBuilder = new StringBuilder();
	    for (int i = 0; i < events.length; i++)
        { 
	        Hashtable hashtable = events[i];
	        //Add Calender Events Index
	        sBuilder.append("Number ").append((i+1)).append(".");
	        //Add Title
	        sBuilder.append(hashtable.get(Const.CALENDER_TITLE)).append(".");
	        //Add Date
	        sBuilder.append("Starting Date is ").append(hashtable.get(Const.CALENDER_STARTDATE)).append(".");
	        
	        //The break between events
	        sBuilder.append("                         .");
        }
	    
	    talker.AddMsgToSpeak(sBuilder.toString());
	}
	
	public void ttsPlayOrResume()
    {
        talker.PlayOrResumeSpeak();
    }
	
	public void ttsPause()
    {
        talker.Pause();
    }
	
	public void ttsShutDown() 
	{
	    talker.ShutDown();
	}
	public void setTalkerMsg(String message) {
	    talker.AddMsgToSpeak(message);
//		talker.AddMsgToSpeak("I have, myself, full confidence that if all do their duty, if nothing is neglected, and if the best arrangements are made, as they are being made, we shall prove ourselves once again able to defend our Island home, to ride out the storm of war, and to outlive the menace of tyranny, if necessary for years, if necessary alone." +
//        		"At any rate, that is what we are going to try to do. That is the resolve of His Majesty¡¯s Government-every man of them. That is the will of Parliament and the nation." +
//        		"The British Empire and the French Republic, linked together in their cause and in their need, will defend to the death their native soil, aiding each other like good comrades to the utmost of their strength." +
//        		"Even though large tracts of Europe and many old and famous States have fallen or may fall into the grip of the Gestapo and all the odious apparatus of Nazi rule, we shall not flag or fail." +
//        		"We shall go on to the end, we shall fight in France," +
//        		"we shall fight on the seas and oceans," +
//        		"we shall fight with growing confidence and growing strength in the air, we shall defend our Island, whatever the cost may be," +
//        		"we shall fight on the beaches," +
//        		"we shall fight on the landing grounds," +
//        		"we shall fight in the fields and in the streets," +
//        		"we shall fight in the hills;" +
//        		"we shall never surrender, and even if, which I do not for a moment believe, this Island or a large part of it were subjugated and starving, then our Empire beyond the seas, armed and guarded by the British Fleet, would carry on the struggle, until, in God¡¯s good time, the New World, with all its power and might, steps forth to the rescue and the liberation of the old.");
        
	}

   
}
