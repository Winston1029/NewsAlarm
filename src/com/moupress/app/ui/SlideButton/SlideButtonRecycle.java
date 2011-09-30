package  com.moupress.app.ui.SlideButton;

import java.util.LinkedList;
import java.util.List;
import android.view.View;
import android.widget.LinearLayout;

public class SlideButtonRecycle {
	
	//Cached ListItems
	private List<View> items;
	
	public SlideButtonRecycle()
	{
		items = new LinkedList<View>();
	}
	
	/**
	 * Recycles items from specified layout.
	 * 
	 * @param layout the layout containing items to be cached
	 * @param firstItem the number of first item in layout
	 * @param range the range of current wheel items 
	 * @return the new value of first item number
	 */
	public int recycleItem(LinearLayout layout, int first, int count)
	{
		for(int i=first;i<first+count;i++)
		{
			items.add(layout.getChildAt(i));
			layout.removeViewAt(i);
		}
		
		return first;
	}
	
	public void clearAll()
	{
		if(items != null)
		{
			items.clear();
		}
	}
	
	/**
	 * Gets item view
	 * @return the cached view
	 */
	public View getItem() {
		return getCachedView(items);
	}
	
	/**
	 * Gets view from specified cache.
	 * @param cache the cache
	 * @return the first view from cache.
	 */
	private View getCachedView(List<View> cache) {
		if (cache != null && cache.size() > 0) {
			View view = cache.get(0);
			cache.remove(0);
			return view;
		}
		return null;
	}
	
}
