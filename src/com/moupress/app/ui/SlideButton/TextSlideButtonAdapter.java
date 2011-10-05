package  com.moupress.app.ui.SlideButton;



import com.moupress.app.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class TextSlideButtonAdapter extends AbstractSlideButtonAdapter{
	
	private String[] items;
	private LayoutInflater inflator;
	private Context ctx;
	
	
	public TextSlideButtonAdapter(String[] items,Context ctx)
	{
		this.items = items;
		this.ctx = ctx;
		inflator = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getItem(int index, View convertView, ViewGroup parent) {
		
		if(convertView == null)
		{
			convertView = inflator.inflate(R.layout.weekday,parent, false);
			
		}
		TextView tv = (TextView)convertView.findViewById(R.id.wkday);
		//TextView tv = new TextView(ctx);
		tv.setTextColor(ctx.getResources().getColor(R.color.black));
		tv.setText(items[index]);
		
		return tv;
	}
	

	@Override
	public int getItemsCount() {
		
		return items.length;
	}

}
