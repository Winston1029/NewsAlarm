package  com.moupress.app.ui.SlideButton;

import android.view.View;
import android.widget.TextView;

public interface OnChangeListener {
	 abstract void OnChanged(int weekdayPos, boolean direction, View textView);  
	 abstract void OnSelected(int weekdayPos,  View textView,int mode); 
}
