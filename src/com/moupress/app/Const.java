package com.moupress.app;

public class Const {
	public final static boolean ISDEBUG =  false;
	

	//==============Alarm Sound ================================
	public static final int ALARMSOUND_BBC = 0;
	public static final int ALARMSOUND_MEDIACORP_933 = 1;
	public static final int ALARMSOUND_REMINDER = 2;
	public static final String BBC_WORLD_SERVICE = "http://www.bbc.co.uk/worldservice/meta/tx/nb/live/eneuk.pls";
	public static final String MEDIACORP_938 = "http://www.mediacorpradio.sg/radioliveplayer/asx/938live/fm938TO.asx";
	public static final String MEDIACORP_938_MMS = "mms://a1109.l11459635108.c114596.g.lm.akamaistream.net/D/1109/114596/v0001/reflector:35108";
	public static final String DEFAULT_RIGNTONE = "Default Ringtone";

	//==============Weather================================
	public static final String HOST_WEATHER_SERVICE = "www.google.com";
	public static final String NON_WEATHER_MSG = "No Weather Info";
	public static final String WEATHERINFO_SEPARATOR = ";";
	public static final String WEATHERINFO_WINDHUMIDITY = "windhumidity";
	public static final String WEATHERINFO_FORCAST = "focast";
	public static final String WEATHERINFO_CURRENT = "current";
	
	// larger means longer waiting time at the beginning
	public static final int INTIAL_KB_BUFFER = 160 * 10 / 8;

	public static enum SCREENS {
		HomeUI, SnoozeUI, AlarmTimeUI, AlarmSoundUI,OnSnoozedUI
	};
	
	//==============DBHelper================================
    public static final String sPName = "NewsAlarm";
    
    public static final String  DefString = "";
    public static final Boolean DefBool = false;
    public static final int DefNum = -1;
    
    public static final String ALARM = "Alarm_";
    public static final String SelectedDay = "Alarm_Selected_Day_";
    public static final String Hours = "Alarm_Hours_";
    public static final String Mins = "Alarm_Mins_";
    public static final String ISALARMSET = "IsAlarmSet_";
    public static final String ALARMSOUNDE ="Alarm_Sound_";
    public static final String SNOOZEMODE = "Snooze_";
    public static final String GESTURE = "Gesture";
    public static final String Limit = ";";
    public static final boolean[] DaySelected = new boolean[]{false,false,false,false,false,false,false};
    public static final String StrDaySelected = "T";
    public static final String StrDayNotSelected = "F";
    //===============Calendar Event==========================
    public static final String CALENDER_ID = "_id";
    public static final String CALENDER_TITLE = "title";
    public static final String CALENDER_STARTDATE = "dtstart";
    
    //===============Slide Button============================
    public static final int[] thumbImg = {R.drawable.slide_thumb,R.drawable.dismiss_thumb_small};
    
    //===============Snooze Timer ===============
    public static final int SNOOZE_DUR = 1*60*1000;

}
