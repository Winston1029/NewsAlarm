package  com.moupress.app.ui.SlideButton;



import com.moupress.app.R;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class TextSwitcherSlideButtonAdapter extends AbstractSlideButtonAdapter{
	
	private String[] items;
	private LayoutInflater inflator;
	private Context ctx;
	private TextSwitcher[] ts;
	
	Animation in,out;
	
	
	public TextSwitcherSlideButtonAdapter(String[] items,Context ctx)
	{
		this.items = items;
		this.ctx = ctx;
		inflator = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		in = AnimationUtils.loadAnimation(ctx,android.R.anim.fade_in);
		out = AnimationUtils.loadAnimation(ctx,android.R.anim.fade_out);
		ts = new  TextSwitcher[items.length];
	}
	
	@Override
	public View getItem(int index, View convertView, ViewGroup parent) {
		
		if(convertView == null)
		{
			convertView = inflator.inflate(R.layout.dismisstextswitcher,parent, false);
			
		}
		TextSwitcher tv = (TextSwitcher)convertView.findViewById(R.id.weekdayswitcher);
		tv.setInAnimation(in);
		tv.setOutAnimation(out);
		tv.setFactory(new ViewSwitcher.ViewFactory(){

			@Override
			public View makeView() {
				TextView t = new TextView(ctx);
				t.setTextSize(20);
				
				t.setGravity( Gravity.CENTER_HORIZONTAL);
				//t.setText("D");
				return t;
			}});
		//TextView tv = new TextView(ctx);
		//tv.setTextColor(ctx.getResources().getColor(R.color.black));
		//TextView t = (TextView) (tv.getNextView().equals(tv.getChildAt(0))? tv.getChildAt(1) : tv.getChildAt(0));
		TextView t = (TextView) (tv.getCurrentView());
		t.setTextColor(ctx.getResources().getColor(R.color.black));
		tv.setText(items[index]);
		return t;
	}
	
	public void testTxtSwitcher(int i)
	{
		ts[1].setText("Text i "+i);
	}

	@Override
	public int getItemsCount() {
		
		return items.length;
	}

}
