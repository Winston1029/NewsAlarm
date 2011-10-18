package  com.moupress.app.ui.SlideButton;



import com.moupress.app.Const;
import com.moupress.app.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SlideButton extends View implements OnTouchListener{
	
	private float downX,currentX=-1;
	
	private Bitmap slip_btn; 
	
	private OnChangeListener chgLsn;
	
	//Items layout
	private LinearLayout itemsLayout;
	
	// View adapter
	private SlideButtonAdapter viewAdapter;
	
	private SlideButtonRecycle recycle = new SlideButtonRecycle();
	
	private int[] itemsPostions;
	
	private float censorPos;
	
	private float preCensorPos;
	
	private int slidePos;
	
	private boolean isSliding = false;
	
	private int slideDir = 0;
	private int preSlideDir = 0;
	
	/** Left and right padding value */
	private static final int PADDING = 7;
	
	private int startIndex;
	
	private int thumb;
	
	private Context ctx;
	
	//Sliding stopped mode
	private int mode;
	
	private final int ORIGINAL = 0;
	private final int DISCRETE = 1;
	private final int CONTINUOUS = 2;
	

	public SlideButton(Context context)
	{
		super(context);
		ctx = context;
		//init();
	}
	
	private void init() {
		///slip_btn = BitmapFactory.decodeResource(getResources(), R.drawable.slide_thumb);
		slip_btn = BitmapFactory.decodeResource(getResources(), Const.thumbImg[thumb]);
		
		setOnTouchListener(this);
	}
	
	public OnChangeListener getOnChangeListener()
	{
		return chgLsn;
	}
	
	public void setSlidePosition(int index)
	{
		startIndex = index;
		
		//System.out.println("CurrentX Position "+ currentX+" Index "+ index);
		currentX = -1;
		invalidate();
	}
	
	
	
	/**
	 * Draws items
	 * @param canvas the canvas for drawing
	 */
	private void drawItems(Canvas canvas) {
		canvas.save();
		
		//int top = (currentItem - firstItem) * getItemHeight() + (getItemHeight() - getHeight()) / 2;
		canvas.translate(PADDING, PADDING);
		
		itemsLayout.draw(canvas);
		//System.out.println("Top Value is "+top);

		canvas.restore();
		
	}
	
	public void iniSlipBtnPos()
	{	
		if(startIndex > 0)
		{
			if(itemsPostions!=null && itemsLayout.getChildCount()>startIndex)
			{
				currentX = itemsPostions[startIndex]+PADDING;
				if(chgLsn != null)
				chgLsn.OnSelected(startIndex, (TextView)itemsLayout.getChildAt(startIndex),1);
			}
		}
		else
		{
			currentX = 0;
		}
	}
	
	private void getItemsPositions()
	{
		
		for(int i=0;i<itemsLayout.getChildCount();i++)
		{
			itemsPostions[i]= ((View)itemsLayout.getChildAt(i)).getLeft()+((View)itemsLayout.getChildAt(i)).getWidth()/2;
			//itemsPostions[i]= ((View)itemsLayout.getChildAt(i)).getLeft();
			//System.out.println("Child Position "+i+" " + itemsPostions[i]);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//super.onDraw(canvas);
		//itemsLayout.draw(canvas);
		drawItems(canvas);
		getItemsPositions();
		
		//First Time Loading
		if(currentX == -1)
		{
			this.iniSlipBtnPos();
		}
		
		 //Matrix matrix = new Matrix();  
		Paint paint = new Paint(); 
		 
		 float x;
		 
		 if(currentX >= this.getWidth()-slip_btn.getWidth()/2) 
		 {
			 x = this.getWidth()-slip_btn.getWidth();
		 }
		 else
		 {
			 x = currentX - slip_btn.getWidth()/2;
		 }
			
		 if(x<=0)
		 {
			 x = 0;
		 }
		 canvas.drawBitmap(slip_btn,x, 1, paint);

		 //censorPos = (int) currentX;
		 
		 if(isSliding == true)
		 comparePosition();
	}
	
	
	
	private void comparePosition() {
		//System.out.println("censor position "+ censorPos);
		if(censorPos>=preCensorPos)
		{
			slideDir = 1;
		}
		else
		{
			slideDir =2;
		}
		
		if(preSlideDir != slideDir && preSlideDir != 0)
		{
			if((slideDir==1 && preCensorPos < downX)||(slideDir==2 && preCensorPos > downX))
			downX = preCensorPos;
		}
		
		//System.out.println("Direction "+ slideDir + " previous Location " + preCensorPos+" Down Location "+downX);
		
		for(int i = 0;i<itemsPostions.length;i++)
		{
			if(itemsPostions[i]>=preCensorPos && itemsPostions[i]<=censorPos && itemsPostions[i] >= (downX-slip_btn.getWidth()/2))
			{
				if(chgLsn != null)
				chgLsn.OnChanged(i,true,(TextView)itemsLayout.getChildAt(i));
				slidePos = i;
			}
			 else if(itemsPostions[i]<=preCensorPos && itemsPostions[i]>=censorPos && itemsPostions[i]<= (downX+slip_btn.getWidth()/2))
			{
				 if(chgLsn != null)
				chgLsn.OnChanged(i,false,(TextView)itemsLayout.getChildAt(i));
				
				slidePos = i-1;
			}
		}
		
		preCensorPos = censorPos;
		preSlideDir = slideDir;
	}
	/**
	 * Calculates control width and creates text layouts
	 * @param widthSize the input layout width
	 * @param mode the layout mode
	 * @return the calculated control width
	 */
	private int calculateLayoutWidth(int widthSize, int mode) {
		//this.setBackgroundResource(R.drawable.slide_bg);
		itemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		itemsLayout.setGravity(Gravity.CENTER_HORIZONTAL);
		itemsLayout.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED), 
	                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		int width = itemsLayout.getMeasuredWidth();
		
		if (mode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else {
			//width += 2 * PADDING;
			// Check against our minimum width
			width = Math.max(width, getSuggestedMinimumWidth());

			if (mode == MeasureSpec.AT_MOST && widthSize < width) {
				width = widthSize;
			}
		}
		
        itemsLayout.measure(MeasureSpec.makeMeasureSpec(width , MeasureSpec.EXACTLY), 
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		return width;

	}
	
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		layout(right,bottom);
	}
	private void layout(int width, int height) {
		// TODO Auto-generated method stub
		int itemsWidth = width-6*PADDING;
		itemsLayout.layout(0, 0, itemsWidth, height);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		//int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		//int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		buildViewForMeasuring();
		
		calculateLayoutWidth(widthSize,widthMode);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
	}
	
	/**
	 * Builds view for measuring
	 */
	public void buildViewForMeasuring()
	{
		if(itemsLayout == null)
		createItemsLayout();
		itemsLayout.removeAllViews();
		if(viewAdapter != null)
		{
			int count = viewAdapter.getItemsCount();
			for(int i = 0; i< count; i++)
			{
				//addViewItem(i);
				if(chgLsn != null)
			  	chgLsn.OnSelected(i, addViewItem(i), 3);
			}
		}
	}
	
	public void reLoadViews()
	{
		if(viewAdapter != null)
		{
			int count = viewAdapter.getItemsCount();
			for(int i = 0; i< count; i++)
			{
				//addViewItem(i);
				if(chgLsn != null)
			  	chgLsn.OnSelected(i, itemsLayout.getChildAt(i), 3);
			}
		}
		
	}
	/**
	 * Adds view for item to items layout
	 * @param index the item index
	 * @param first the flag indicates if view should be first
	 * @return true if corresponding item exists and is added
	 */
	private View addViewItem(int index) {
		View view = getItemView(index);
		if (view != null) {
			
			//try{
				itemsLayout.addView(view);
				//}catch(Exception e)
				///{
				//	System.out.println("Exception e "+e.toString());
				//}
				//return view;
			}
		return view;
	}
	
	/**
	 * Creates item layouts if necessary
	 */
	private void createItemsLayout() {
		if (itemsLayout == null) {
			itemsLayout = new LinearLayout(getContext());
			itemsLayout.setOrientation(LinearLayout.HORIZONTAL);
		}
	}
	
	
	public SlideButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx= context;
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlideButton);
		thumb = a.getInt(R.styleable.SlideButton_thumb,0);
		mode = a.getInt(R.styleable.SlideButton_mode, 0);
		//System.out.println("Slide Button Constructor ");
		init();
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		 switch(event.getAction())
		 {	
		 case MotionEvent.ACTION_MOVE:
			 currentX = event.getX();
			 isSliding = true;
			 break;
		 case MotionEvent.ACTION_DOWN:
			 slideDir = preSlideDir = 0;
			 downX = event.getX();
			 currentX = downX;
			 break;
		 case MotionEvent.ACTION_UP:
			 
			 if(mode == this.DISCRETE)
			 {
				 if(isSliding == true)
				 {
					 if(slidePos>=0)
					 //currentX= itemsPostions[slidePos] + slip_btn.getWidth()/2+PADDING;
						 currentX= itemsPostions[slidePos]+PADDING;
					 else
				     currentX = 0;
					 isSliding = false;
				 }
				 else
				 {
					 int i = getClickLocation(event.getX());
					 currentX=itemsPostions[i]+PADDING;
					 
					 if(chgLsn != null)
					 chgLsn.OnSelected(i, (TextView)itemsLayout.getChildAt(i),0);
				 }
			 }
			 else if(mode == this.ORIGINAL)
				 currentX = 0;
			 
			 break; 
		 default: 
			 break;
		 }
		 censorPos = event.getX();
		 invalidate();
		return true;
	}
	
	 public void setOnChangedListener(OnChangeListener onChgLsn){
		 chgLsn = onChgLsn; 
	 }
	 
	 
	 public int getClickLocation(float currentPos)
	 {
		for(int i = 0;i<viewAdapter.getItemsCount()-1;i++)
		{
			if(currentPos>=itemsPostions[i] && currentPos<=itemsPostions[i+1])
			{
				if(Math.abs(currentPos-itemsPostions[i])<=Math.abs(currentPos-itemsPostions[i+1]))
				{
					return i;
				}
				else 
					return i +1;
			}
			
		}
		
		if(currentPos<=itemsPostions[0])
			return 0;
		else if(currentPos>=itemsPostions[viewAdapter.getItemsCount()-1])
		    return viewAdapter.getItemsCount()-1;
		else
		    return 0;
	 }
	 
	 /**
		 * Returns view for specified item
		 * @param index the item index
		 * @return item view or empty view if index is out of bounds
		 */
	    public View getItemView(int index) {
			if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
				return null;
			}
			
			if (!isValidItemIndex(index)) {
				return null;
			}
			
			return viewAdapter.getItem(index, recycle.getItem(), itemsLayout);
		}
	    
	    /**
		 * Checks whether iterm index is valid
		 * @param index the item index
		 * @return true if item index is not out of bounds or the wheel is cyclic
		 */
		private boolean isValidItemIndex(int index) {
		    return viewAdapter != null && viewAdapter.getItemsCount() > 0 &&( index >= 0 && index < viewAdapter.getItemsCount());
		}
		
		// Adapter listener
	    private DataSetObserver dataObserver = new DataSetObserver() {
	        @Override
	        public void onChanged() {
	            invalidateSlideButton(false);
	        }

	        @Override
	        public void onInvalidated() {
	            invalidateSlideButton(true);
	        }
	    };
		
		/**
		 * Sets view adapter. Usually new adapters contain different views, so
		 * it needs to rebuild view by calling measure().
		 *  
		 * @param viewAdapter the view adapter
		 */
		public void setViewAdapter(SlideButtonAdapter viewAdapter) {
		    if (this.viewAdapter != null) {
		        this.viewAdapter.unregisterDataSetObserver(dataObserver);
		    }
	        this.viewAdapter = viewAdapter;
	        if (this.viewAdapter != null) {
	            this.viewAdapter.registerDataSetObserver(dataObserver);
	        }
	       
	        invalidateSlideButton(true);
	        itemsPostions = new int[viewAdapter.getItemsCount()];
		}
		/**
		 * Invalidates SlideButton
		 * @param clearCaches if true then cached views will be clear
		 */
		private void invalidateSlideButton(boolean clearCaches) {
			// TODO Auto-generated method stub
			if (clearCaches) {
	            recycle.clearAll();
	            if (itemsLayout != null) {
	                itemsLayout.removeAllViews();
	            }
	        } else if (itemsLayout != null) {
	            // cache all items
		        recycle.recycleItem(itemsLayout, 0, viewAdapter.getItemsCount());         
	        }
	        invalidate();
		}
}
