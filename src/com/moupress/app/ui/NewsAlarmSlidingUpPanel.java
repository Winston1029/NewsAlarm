package com.moupress.app.ui;


import com.moupress.app.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

public class NewsAlarmSlidingUpPanel extends LinearLayout{

	private int speed=300;
	private boolean isOpen=false;
	//private Cursor currFavor;
	private PanelSlidingListener panelSlidingListener;
	
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
			anim.setAnimationListener(popUpListener);
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
	
	Animation.AnimationListener popUpListener=new Animation.AnimationListener() {
		public void onAnimationEnd(Animation animation) {
			panelSlidingListener.onSlidingUpEnd();
		}
		
		public void onAnimationRepeat(Animation animation) {
			// not needed
		}
		
		public void onAnimationStart(Animation animation) {
			
		}
	};
	Animation.AnimationListener collapseListener=new Animation.AnimationListener() {
		public void onAnimationEnd(Animation animation) {
			setVisibility(View.GONE);
			panelSlidingListener.onSlidingDownEnd();
		}
		
		public void onAnimationRepeat(Animation animation) {
			// not needed
		}
		
		public void onAnimationStart(Animation animation) {
			// not needed
		}
	};
	
	public void setPanelSlidingListener(PanelSlidingListener panelSlidingListener)
	{
		this.panelSlidingListener = panelSlidingListener;
	}
	
	public interface PanelSlidingListener{
		
		public void onSlidingUpEnd();
		public void onSlidingDownEnd();
	}

	public void setOpen(boolean open) {
		// TODO Auto-generated method stub
		this.isOpen = open;
	}
}
