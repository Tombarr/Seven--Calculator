package com.tombarrasso.android.wp7calculator;

// Java Packages
import java.text.NumberFormat;
import java.util.Locale;
import java.lang.reflect.Method;

// App Packages
import com.tombarrasso.android.wp7ui.WPFonts;
import com.tombarrasso.android.wp7ui.widget.WPToast;

// Android Packages
import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout.Alignment;
import android.content.res.Resources;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.view.HapticFeedbackConstants;
import android.util.Log;

/**
 * Text view that auto adjusts text size to fit within the view.
 * If the text size equals the minimum text size and still does not
 * fit, append with an ellipsis.<br />
 * Modified by Thomas Barrasso on June 6, 2012 to support {@link ClipboardManager}
 * integration, automatic comma separation, and more.
 * 
 * @see http://stackoverflow.com/questions/5033012/auto-scale-textview-text-to-fit-within-bounds
 * @author Chase Colburn
 * @since  Apr 4, 2011
 * 
 * @version 2.0
 */
public final class AutoResizeTextView
	extends TextView
{
	public static final String TAG = AutoResizeTextView.class.getSimpleName();
	
	private String[] mMenuItemsStrings;
    
    private static final Class<TextView> mViewClass = TextView.class;
    
    private static Method mMethod;
    
    public static final boolean setCustomSelectionActionModeCallback(
    	View mView, ActionMode.Callback mCallback)
    {
    	// Cache the Method for performance.
		if (mMethod == null)
		{
			try
			{
				// Check to see if an overscroll method exists.
				mMethod = mViewClass.getMethod("setCustomSelectionActionModeCallback",
					new Class[] { ActionMode.Callback.class });
			}
			catch(NoSuchMethodException e)
			{
				return false;
			}
		}
		
		// Call the method if it exists.
		// It is bad practice to catch all exceptions
		// but Reflection has so many, all with the
		// same meaning that no method was called.
		try
		{
			mMethod.invoke(mView,
				new Object[] { mCallback });
		}
		catch(Exception e)
		{
			return false;
		}

		return true;
    }
    
    private static final int CUT = 0;
    private static final int COPY = 1;
    private static final int PASTE = 2;
	
    // Minimum text size for this text view
    public static final float MIN_TEXT_SIZE = 2;

    // Interface for resize notifications
    public interface OnTextResizeListener {
        public void onTextResize(TextView textView, float oldSize, float newSize);
    }

    // Off screen canvas for text size rendering
    private static final Canvas sTextResizeCanvas = new Canvas();

    // Our ellipse string
    private static final String mEllipsis = "...";

    // Registered resize listener
    private OnTextResizeListener mTextResizeListener;

    // Flag for text and/or size changes to force a resize
    private boolean mNeedsResize = false;

    // Text size that is set from code. This acts as a starting point for resizing
    private float mTextSize;

    // Temporary upper bounds on the starting text size
    private float mMaxTextSize = 0;

    // Lower bounds for text size
    private float mMinTextSize = MIN_TEXT_SIZE;

    // Text view line spacing multiplier
    private float mSpacingMult = 1.0f;

    // Text view additional line spacing
    private float mSpacingAdd = -2.0f;

    // Add ellipsis to text that overflows at the smallest text size
    private boolean mAddEllipsis = false;

    // Default constructor override
    public AutoResizeTextView(Context context) {
        this(context, null);
        init();
    }

    // Default constructor when inflating from XML file
    public AutoResizeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    // Default constructor override
    public AutoResizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        mTextSize = getTextSize();
    }
    
    private void init()
    {
    	WPFonts.setDefaultFonts(getContext().getResources().getAssets());
    	setTypeface(WPFonts.getFontSet().getRegular());
    	
    	//max size defaults to the initially specified text size unless it is too small
        mMaxTextSize = this.getTextSize();
    	
    	// Only bother if the API might be available.
    	if (android.os.Build.VERSION.SDK_INT >= 11)
    	{
    		try
    		{
	    		setCustomSelectionActionModeCallback(
	    			this, new NoTextSelectionMode());
	    	}
	    	catch (Throwable e) { }
	    }
    };
    
    /* Re size the font so the specified text fits in the text box
	 * assuming the text box is the specified width.
	 */
	//TODO binary search
	private void refitText(String text, int textWidth) {
		if (textWidth > 0) {
			int availableWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
			float trySize = mMaxTextSize;
	
			this.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
			while ((trySize > mMinTextSize) && (this.getPaint().measureText(text) > availableWidth)) {
				trySize -= 1;
				if (trySize <= mMinTextSize) {
					trySize = mMinTextSize;
					break;
				}
				this.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
			}
			this.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
		}
	}
	
	@Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        mNeedsResize = true;
        refitText(text.toString(), this.getWidth());
    }
    
    private int mWidth;

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		mNeedsResize = true;
		mWidth = w;
		refitText(this.getText().toString(), this.getWidth());
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        
        //int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        // refitText(this.getText().toString(), parentWidth);
    }
    
    // Custom setText methods to handle adding commas
    // and general number formatting issues.
    
    private static final NumberFormat nf = NumberFormat.getInstance(Locale.US);
    static {
    	// Let the program handle where to truncate.
    	nf.setMaximumFractionDigits(999); // Arbitrary large digit.
    };
    
    /**
     * Separates text with commas after every three chars.
     */
    public void setCommaSeparatedText(double num)
    {		
    	try
    	{
	    	super.setText(nf.format(num));
    	}
    	catch (NumberFormatException e)
    	{
    		super.setText(Double.toString(num));
    	}
    }
    
    /**
     * Separates text with commas after every three chars.
     */
    public void setCommaSeparatedText(String text)
    {
    	try
    	{
    		setCommaSeparatedText(Double.parseDouble(HomeActivity.removeChar(text, Constants.Chars.comma)));
    	}
    	catch (NumberFormatException e)
    	{
    		super.setText(text);
    	}
    }

    /**
     * When text changes, set the force resize flag to true and reset the text size.
     */
    /*@Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        mNeedsResize = true;
        // Since this view may be reused, it is good to reset the text size
        resetTextSize();
    }*/

    /**
     * If the text view size changed, set the force resize flag to true
     */
    /*@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            mNeedsResize = true;
        }
    }/*

    /**
     * Register listener to receive resize notifications
     * @param listener
     */
    public void setOnResizeListener(OnTextResizeListener listener) {
        mTextResizeListener = listener;
    }

    /**
     * Override the set text size to update our internal reference values
     */
    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        mTextSize = getTextSize();
    }

    /**
     * Override the set text size to update our internal reference values
     */
    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        mTextSize = getTextSize();
    }

    /**
     * Override the set line spacing to update our internal reference values
     */
    @Override
    public void setLineSpacing(float add, float mult) {
        super.setLineSpacing(add, mult);
        mSpacingMult = mult;
        mSpacingAdd = add;
    }

    /**
     * Set the upper text size limit and invalidate the view
     * @param maxTextSize
     */
    public void setMaxTextSize(float maxTextSize) {
        mMaxTextSize = maxTextSize;
        requestLayout();
        invalidate();
    }

    /**
     * Return upper text size limit
     * @return
     */
    public float getMaxTextSize() {
        return mMaxTextSize;
    }

    /**
     * Set the lower text size limit and invalidate the view
     * @param minTextSize
     */
    public void setMinTextSize(float minTextSize) {
        mMinTextSize = minTextSize;
        requestLayout();
        invalidate();
    }

    /**
     * Return lower text size limit
     * @return
     */
    public float getMinTextSize() {
        return mMinTextSize;
    }

    /**
     * Set flag to add ellipsis to text that overflows at the smallest text size
     * @param addEllipsis
     */
    public void setAddEllipsis(boolean addEllipsis) {
        mAddEllipsis = addEllipsis;
    }

    /**
     * Return flag to add ellipsis to text that overflows at the smallest text size
     * @return
     */
    public boolean getAddEllipsis() {
        return mAddEllipsis;
    }

    /**
     * Reset the text to the original size
     */
    public void resetTextSize() {
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        mMaxTextSize = mTextSize;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
       if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            // Hack to prevent keyboard and insertion handle from showing.
           cancelLongPress();
        }
        return super.onTouchEvent(event);
    }
    
    @Override
    public boolean performLongClick() {
    
    	final SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
    	final boolean mShouldVibrate = mPrefs.getBoolean(HomeActivity.VIBRATE_KEY, false);
    	
    	// Cause a small vibration
    	if (mShouldVibrate) {
    		performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
						HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING |
						HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
    	}
    
        showContextMenu();
        return true;
    }
    
    private final class MenuHandler implements MenuItem.OnMenuItemClickListener {
    	@Override
        public boolean onMenuItemClick(MenuItem item) {
            return onTextContextMenuItem(item.getTitle());
        }
    }

    public boolean onTextContextMenuItem(CharSequence title) {
        boolean handled = false;
        if (TextUtils.equals(title, mMenuItemsStrings[CUT])) {
            cutContent();
            handled = true;
        } else if (TextUtils.equals(title,  mMenuItemsStrings[COPY])) {
            copyContent();
            handled = true;
        } else if (TextUtils.equals(title,  mMenuItemsStrings[PASTE])) {
            pasteContent();
            handled = true;
        }
        return handled;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu) {
        MenuHandler handler = new MenuHandler();
        if (mMenuItemsStrings == null) {
            Resources resources = getResources();
            mMenuItemsStrings = new String[3];
            mMenuItemsStrings[CUT] = resources.getString(android.R.string.cut);
            mMenuItemsStrings[COPY] = resources.getString(android.R.string.copy);
            mMenuItemsStrings[PASTE] = resources.getString(android.R.string.paste);
        }
        for (int i = 0; i < mMenuItemsStrings.length; i++) {
            menu.add(Menu.NONE, i, i, mMenuItemsStrings[i]).setOnMenuItemClickListener(handler);
        }
        if (getText().length() == 0) {
            menu.getItem(CUT).setVisible(false);
            menu.getItem(COPY).setVisible(false);
        }
        CharSequence primaryClip = getClipText();
        if (primaryClip == null || !canPaste(primaryClip)) {
            menu.getItem(PASTE).setVisible(false);
        }
    }

    private void setClipText(CharSequence clip) {
        final ClipboardManager clipboard = (ClipboardManager) getContext().
                getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(clip);
    }

    private void copyContent() {
        final String text = (String) getText();
        int textLength = text.length();
        final ClipboardManager clipboard = (ClipboardManager)
        	getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(text);
        
        // Toast to having been copied.
        WPToast.makeText(getContext().getApplicationContext(),
        	getContext().getString(R.string.text_copied_toast),
        	WPToast.LENGTH_SHORT).show();
    }

    private void cutContent() {
        final String text = (String) getText();
        int textLength = text.length();
        setClipText(text);
        setText(R.string.zero);
    }

    private CharSequence getClipText() {
        final ClipboardManager clipboard = (ClipboardManager)
        	getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        return clipboard.getText();
    }

    private void pasteContent() {
        CharSequence clip = getClipText();
        if (clip != null) {
			if (canPaste(clip)) {
				setText(clip);
			}
		}
    }

    private boolean canPaste(CharSequence paste) {
        boolean canPaste = true;
        try {
            Float.parseFloat(paste.toString());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error turning string to integer. Ignoring paste.", e);
            canPaste = false;
        }
        return canPaste;
    }

    private final class NoTextSelectionMode
    	implements ActionMode.Callback
    {
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            copyContent();
            // Prevents the selection action mode on double tap.
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {}

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    }

    /**
     * Override drawing and resize text if necessary
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if(mNeedsResize) {
            resizeText(getWidth(), getHeight());
        }
        
        super.onDraw(canvas);
    }

    /**
     * Resize the text size with default width and height
     */
    public void resizeText() {
        int heightLimit = getHeight() - getPaddingBottom() - getPaddingTop();
        int widthLimit = getWidth() - getPaddingLeft() - getPaddingRight();
        resizeText(widthLimit, heightLimit);
    }

    /**
     * Resize the text size with specified width and height
     * @param width
     * @param height
     */
    public void resizeText(int width, int height) {
        final CharSequence text = getText();
        // Do not resize if the view does not have dimensions or there is no text
        if(text == null || text.length() == 0 || height <= 0 || width <= 0) {
            return;
        }

        // Get the text view's paint object
        final TextPaint textPaint = getPaint();

        // Store the current text size
        float oldTextSize = textPaint.getTextSize();
        // If there is a max text size set, use the lesser of that and the default text size
        float targetTextSize = (mMaxTextSize > 0) ? Math.min(mTextSize, mMaxTextSize) : mTextSize;

        // Get the required text height
        int textHeight = getTextHeight(text, textPaint, width, targetTextSize);

        // Until we either fit within our text view or we had reached our min text size, incrementally try smaller sizes
        while(textHeight > height && targetTextSize > mMinTextSize) {
            targetTextSize = Math.max(targetTextSize - 2, mMinTextSize);
            textHeight = getTextHeight(text, textPaint, width, targetTextSize);
        }

        // If we had reached our minimum text size and still don't fit, append an ellipsis
        if(mAddEllipsis && targetTextSize == mMinTextSize && textHeight > height) {
            // Draw using a static layout
            StaticLayout layout = new StaticLayout(text, textPaint, width, Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, false);
            layout.draw(sTextResizeCanvas);
            int lastLine = layout.getLineForVertical(height) - 1;
            int start = layout.getLineStart(lastLine);
            int end = layout.getLineEnd(lastLine);
            float lineWidth = layout.getLineWidth(lastLine);
            float ellipseWidth = textPaint.measureText(mEllipsis);

            // Trim characters off until we have enough room to draw the ellipsis
            while(width < lineWidth + ellipseWidth) {
                lineWidth = textPaint.measureText(text.subSequence(start, --end + 1).toString());   
            }
            setText(text.subSequence(0, end) + mEllipsis);

        }

        // Some devices try to auto adjust line spacing, so force default line spacing 
        // and invalidate the layout as a side effect
        textPaint.setTextSize(targetTextSize);
        setLineSpacing(mSpacingAdd, mSpacingMult);

        // Notify the listener if registered
        if(mTextResizeListener != null) {
            mTextResizeListener.onTextResize(this, oldTextSize, targetTextSize);
        }

        // Reset force resize flag
        mNeedsResize = false;
    }

    // Set the text size of the text paint object and use a static layout to render text off screen before measuring
    private int getTextHeight(CharSequence source, TextPaint paint, int width, float textSize) {
        // Update the text paint object
        paint.setTextSize(textSize);
        // Draw using a static layout
        StaticLayout layout = new StaticLayout(source, paint, width, Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, false);
        layout.draw(sTextResizeCanvas);
        return layout.getHeight();
    }

}