package com.moupress.app.ui;


import com.moupress.app.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

public class NewsAlarmSlidingUpPanel extends LinearLayout{

	private int speed=300;
	private boolean isOpen=false;
	private Cursor currFavor;
	
	public NewsAlarmSlidingUpPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a= context.obtainStyledAttributes(attrs,R.styleable.SlidingPanel,0, 0);
		speed=a.getInt(R.styleable.SlidingPanel_speed, 300);
		a.recycle();
	}
	
	public void toggle() {
		TranslateAnimation anim=null;
		
		isOpen=!isOpen;
		
		if (isOpen) {
			setVisibility(View.VISIBLE);
			anim=new TranslateAnimation(0.0f, 0.0f,getHeight(),0.0f);
		}
		else {
			anim=new TranslateAnimation(0.0f, 0.0f, 0.0f,getHeight());
			anim.setAnimationListener(collapseListener);
		}
		
		anim.setDuration(speed);
		anim.setInterpolator(new AccelerateInterpolator(1.0f));
		//v.startAnimation(anim);
		startAnimation(anim);
	}
	
	Animation.AnimationListener collapseListener=new Animation.AnimationListener() {
		public void onAnimationEnd(Animation animation) {
			setVisibility(View.GONE);
		}
		
		public void onAnimationRepeat(Animation animation) {
			// not needed
		}
		
		public void onAnimationStart(Animation animation) {
			// not needed
		}
	};
	
}