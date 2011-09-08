package com.moupress.app.TTS;

import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;

public class AlarmTTS implements OnInitListener, OnUtteranceCompletedListener
{
    private static final String TAG = "TextToSpeech";
    Context mContext;
    private TextToSpeech mTts;
    private String[] loveArray;   
    private int lastUtterance = -1;
    private boolean isPaused = false;
    private static final String STORE_NAME = "preferenceFile"; 
    private HashMap<String, String> params = new HashMap<String, String>();  
    
    public AlarmTTS(Context context)
    {
        this.mContext = context;
        this.mTts = new TextToSpeech(this.mContext, this);
    }
    
    public void AddMsgToSpeak(String msg)
    {
        StringTokenizer loveTokens = new StringTokenizer(msg ,",.");  
        int i = 0;  
        loveArray = new String[loveTokens.countTokens()];  
        while(loveTokens.hasMoreTokens())  
        {  
            loveArray[i++] = loveTokens.nextToken();  
        }  
    }
    
    public void PlayOrResumeSpeak()  
    {  
        this.isPaused = false;
        lastUtterance++;  
        if(lastUtterance >= loveArray.length)  
        {  
            lastUtterance = 0;  
        }  
        Log.v(TAG, "the begin utterance is " + lastUtterance);  
        for(int i = lastUtterance; i < loveArray.length; i++)  
        {  
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(i));  
            mTts.speak(loveArray[i], TextToSpeech.QUEUE_ADD, params);  
        }  
    }  
    public void ShutDown()
    {
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        SharedPreferences settings = mContext.getSharedPreferences(STORE_NAME, 0);  
        SharedPreferences.Editor editor = settings.edit();  
        editor.putInt("lastUtterance", lastUtterance);  
        editor.commit();  
        Log.v(TAG, "the stored lastUtterance is " + lastUtterance);  
    }
    
    public void onUtteranceCompleted(String utteranceId) {  
        Log.v(TAG, "Get completed message for the utteranceId " + utteranceId);  
        lastUtterance = Integer.parseInt(utteranceId);  
        if(this.isPaused)
        {
            mTts.stop();
        }
    }  
    public void onInit(int status)
    {
        if (status == TextToSpeech.SUCCESS)
        {
             int result = mTts.setLanguage(Locale.US);

             if (result == TextToSpeech.LANG_MISSING_DATA ||
                 result == TextToSpeech.LANG_NOT_SUPPORTED) 
             {
                 Log.e(TAG, "Language is not available.");
                 return;
             }
             mTts.setOnUtteranceCompletedListener(this);  
        }
    }

    public void Pause()
    {
        this.isPaused = true;
    }
    
}
