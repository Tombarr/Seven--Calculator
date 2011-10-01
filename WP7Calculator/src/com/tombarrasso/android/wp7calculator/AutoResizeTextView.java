package com.tombarrasso.android.wp7calculator;

// Java Packages
import java.text.NumberFormat;
import java.util.Locale;

// App Packages
import com.tombarrasso.android.wp7ui.WPFonts;

// Android Packages
import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * Text view that auto adjusts text size to fit within the view.
 * If the text size equals the minimum text size and still does not
 * fit, append with an ellipsis. 
 * 
 * @author Chase Colburn
 * @since Apr 4, 2011
 */
public class AutoResizeTextView extends TextView
{
	public static final String TAG = AutoResizeTextView.class.getSimpleName();
	
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
    private float mSpacingMult = 1.1f;

    // Text view additional line spacing
    private float mSpacingAdd = -1.0f;

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
    	setTypeface(WPFonts.regular);
    };
    
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
    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        mNeedsResize = true;
        // Since this view may be reused, it is good to reset the text size
        resetTextSize();
    }

    /**
     * If the text view size changed, set the force resize flag to true
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            mNeedsResize = true;
        }
    }

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
        float targetTextSize = mMaxTextSize > 0 ? Math.min(mTextSize, mMaxTextSize) : mTextSize;

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