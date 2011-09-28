package com.moupress.app.TTS;

import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

public class CalendarTask {

	private static String CALENDAR_RES_21 = "content://calendar/calendars";
	private static String CALENDAR_RES_22 = "content://com.android.calendar";
	private static final String DEBUG_TAG = "CalendarTask";
	
	private static long MILSEC_PER_DAY = 1000*60*60*24;
	
	private Activity activity;
	
	public CalendarTask(Activity activity) {
		this.activity = activity;
	}
	
	/**
     * Return Events Summary For All Active Calendar
     * Dictionary Keys: "_id", "title", "dtstart"
     */
	public Hashtable[] getCalendarTasks() {
		
		int[] calIds = getSelectedCalendars();
		Hashtable[] events = new Hashtable[calIds.length];
		for (int i = 0; i < calIds.length; i++) {
			events[i] = getTDayCalEventSumary(calIds[i]);
		}
		
		return events;
	}
	
	/**
	 * Get All Active Calendar
	 * @return
	 */
    private int[] getSelectedCalendars() {
        String[] projection = new String[] { "_id", "name" };
        String selection = "selected=1";
        String path = "calendars";

        Cursor managedCursor = getCalendarManagedCursor(projection, selection, path);

        String[] calNames = new String[managedCursor.getCount()];
		int[] calIds = new int[managedCursor.getCount()];
        
        if (managedCursor != null && managedCursor.moveToFirst()) {

            int nameColumn = managedCursor.getColumnIndex("name");
            int idColumn = managedCursor.getColumnIndex("_id");

            for (int i = 0; i < calNames.length; i++) {
    		    calIds[i] = managedCursor.getInt( idColumn );
    		    calNames[i] = managedCursor.getString(nameColumn);
    		    Log.i(DEBUG_TAG, "Found Calendar '" + calNames[i]+ "' (ID=" + calIds[i] + ")");
    		    managedCursor.moveToNext();
    		}
            
        } else {
            Log.i(DEBUG_TAG, "No Calendars");
        }

        return calIds;

    }

    /**
     * Return Events Summary in a Single Calendar
     * Dictionary Keys: "_id", "title", "dtstart"
     */
    private Hashtable<String, String> getTDayCalEventSumary(int calID) {
    	String[] projection = new String[] { "_id", "title", "dtstart" };
    	String selection = "Calendars._id="+calID;
    	String path = "instances/when";
        
		Uri.Builder builder; 
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR) builder = Uri.parse(CALENDAR_RES_21 + path).buildUpon();
		else 													 builder = Uri.parse(CALENDAR_RES_22 + path).buildUpon();
        long now = new Date().getTime();
        ContentUris.appendId(builder, now );
        ContentUris.appendId(builder, now + MILSEC_PER_DAY);
        
        ContentResolver contentResolver = activity.getContentResolver();
        Cursor eventCursor = contentResolver.query(builder.build(), projection, selection, null, "startDay ASC, startMinute ASC"); 
        Hashtable<String, String> eventSummary = new Hashtable<String, String>();
        while (eventCursor.moveToNext()) {
            int number = eventCursor.getColumnCount();
            for (int i = 0; i < number; i++) {
            	String colName = eventCursor.getColumnName(i);
            	String colValue = eventCursor.getString(i);
            	eventSummary.put(colName, colValue);
            }
        }
        
        return eventSummary;
    }
    
    private void ListAllCalendarDetails() {
        Cursor managedCursor = getCalendarManagedCursor(null, null, "calendars");

        if (managedCursor != null && managedCursor.moveToFirst()) {

            do {
            	int number = managedCursor.getColumnCount();
                for (int i = 0; i < number; i++) {
                	String colName = managedCursor.getColumnName(i);
                	String colValue = managedCursor.getString(i);
                    Log.i(DEBUG_TAG, colName + "=" + colValue);
                }
            } while (managedCursor.moveToNext());
        } else {
            Log.i(DEBUG_TAG, "No Calendars");
        }

    }

    private void ListAllCalendarEntries(int calID) {

        Cursor managedCursor = getCalendarManagedCursor(null, "calendar_id="
                + calID, "events");

        if (managedCursor != null && managedCursor.moveToFirst()) {

            do {
            	int number = managedCursor.getColumnCount();
                for (int i = 0; i < number; i++) {
                	String colName = managedCursor.getColumnName(i);
                	String colValue = managedCursor.getString(i);
                	Log.i(DEBUG_TAG, colName + "=" + colValue);
                }
            } while (managedCursor.moveToNext());
        } else {
            Log.i(DEBUG_TAG, "No Calendars");
        }

    }

    private void ListCalendarEntry(int eventId) {
        Cursor managedCursor = getCalendarManagedCursor(null, null, "events/" + eventId);
    
        if (managedCursor != null && managedCursor.moveToFirst()) {

            do {
                for (int i = 0; i < managedCursor.getColumnCount(); i++) {
                    Log.i(DEBUG_TAG, managedCursor.getColumnName(i) + "=" + managedCursor.getString(i));
                }
            } while (managedCursor.moveToNext());
        } else {
            Log.i(DEBUG_TAG, "No Calendar Entry");
        }

    }

    private void ListCalendarEntrySummary(int eventId) {
        String[] projection = new String[] { "_id", "title", "dtstart" };
        Cursor managedCursor = getCalendarManagedCursor(projection,
                null, "events/" + eventId);

        if (managedCursor != null && managedCursor.moveToFirst()) {

            do {
                for (int i = 0; i < managedCursor.getColumnCount(); i++) {
                    Log.i(DEBUG_TAG, managedCursor.getColumnName(i) + "=" + managedCursor.getString(i));
                }
            } while (managedCursor.moveToNext());
        } else {
            Log.i(DEBUG_TAG, "No Calendar Entry");
        }

    }

    /**
     * Determines if it's a pre 2.1 or a 2.2 calendar Uri, and returns the Cursor
     * @param projection
     * @param selection
     * @param path
     * @return
     */
    private Cursor getCalendarManagedCursor(String[] projection, String selection, String path) {
        Uri calendars = Uri.parse("content://calendar/" + path);

        Cursor managedCursor = null;
        try {
            managedCursor = activity.managedQuery(calendars, projection, selection,
                    null, null);
        } catch (IllegalArgumentException e) {
            Log.w(DEBUG_TAG, "Failed to get provider at ["
                    + calendars.toString() + "]");
        }

        if (managedCursor == null) {
            // try again
            calendars = Uri.parse("content://com.android.calendar/" + path);
            try {
                managedCursor = activity.managedQuery(calendars, projection, selection, null, null);
            } catch (IllegalArgumentException e) {
                Log.w(DEBUG_TAG, "Failed to get provider at ["
                        + calendars.toString() + "]");
            }
        }
        return managedCursor;
    }

    
}
