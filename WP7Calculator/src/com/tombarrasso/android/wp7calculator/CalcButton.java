package com.tombarrasso.android.wp7calculator;

/*
 * CalcButton.java
 *
 * Copyright (C) Thomas James Barrasso
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// UI Packages
import com.tombarrasso.android.wp7calculator.Constants.ButtonColors;
import com.tombarrasso.android.wp7ui.WPTheme;
import com.tombarrasso.android.wp7ui.widget.WPTextView;

// Android Packages
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;

/**
 * Button used in the calculator for numbers/ symbols.
 *
 * <br /><br />
 * <u>Change Log:</u>
 * <b>Version 1.01</b>
 * <ul>
 * 	<li>COLOR_TRIG</li>
 * </ul>
 * <b>Version 1.02</b>
 * <ul>
 *	<li>Vibrate onDown instead of onClick.</li>
 * </ul>
 * 
 * @author		Thomas James Barrasso <contact @ tombarrasso.com>
 * @since		2011
 * @version		1.02
 * @category	View
 */

public class CalcButton extends WPTextView implements OnTouchListener
{
	public static final String TAG = CalcButton.class.getSimpleName();
	public static final int BOLD 	= 1,
							ITALIC 	= 4,
							MEDIUM 	= 3,
							REGULAR = 0,
							LIGHT 	= 2;
	private int COLOR, mColor, mColorDown;
	private int mFunction = Constants.Tags.CLEAR;
	private boolean mShouldListen = true,
					mShouldVibrate = false;
	private static Vibrator mVibrator;
	private final int mVibrationDuration;
	
	// ====================
	//    Construction
	// ====================
	
	public CalcButton(Context context)
	{
		super(context);
		mVibrationDuration = context.getResources().getInteger(R.integer.vibration_duration);
		init();
	}
	
	public CalcButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mVibrationDuration = context.getResources().getInteger(R.integer.vibration_duration);
		setAttrs(attrs);
		init();
	}
		
	public CalcButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		mVibrationDuration = context.getResources().getInteger(R.integer.vibration_duration);
		setAttrs(attrs);
		init();
	}
	
	private void setAttrs(AttributeSet attrs)
	{
		// Fetch object for fetching attribute values.
		final TypedArray attr = getContext().obtainStyledAttributes(attrs, R.styleable.Calc);
		
		try
		{
			setFunction(attr.getInt(R.styleable.Calc_function, mFunction));
			setColor(attr.getColor(R.styleable.Calc_color, ButtonColors.COLOR_DARK));
			setTextColorDown(attr.getColor(R.styleable.Calc_textColorDown, Color.BLACK));
			setFont(attr.getInt(R.styleable.Calc_font, REGULAR));
			mColor = attr.getColor(R.styleable.Calc_textColor, Color.WHITE);
			setTextColor(mColor);
			
			// If none of the attributes were used do not
			// use the custom OnTouchListener.
			if (attr.getColor(R.styleable.Calc_color, Integer.MAX_VALUE) == Integer.MAX_VALUE &&
				attr.getColor(R.styleable.Calc_textColor, Integer.MAX_VALUE) == Integer.MAX_VALUE &&
				attr.getColor(R.styleable.Calc_textColorDown, Integer.MAX_VALUE) == Integer.MAX_VALUE)
				mShouldListen = false;
		}
		finally
		{
			attr.recycle();
		}
	}
	
	// ====================
	//    Initialization
	// ====================
	
	private void init()
	{
		// Get vibrator instance.
		if (mVibrator == null)
			mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

		// Get a few values from settings.
		final SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		mShouldVibrate = mPrefs.getBoolean(HomeActivity.VIBRATE_KEY, mShouldVibrate);

		// Set styles.
		if (mShouldListen)
			setOnTouchListener(this);
	}
	
	// ====================
	//    Public Methods
	// ====================
	
	/**
	 * Sets the background color of this view.
	 */
	public void setColor(int color)
	{
		switch (color)
		{
		case ButtonColors.COLOR_ACCENT:
			COLOR = ButtonColors.COLOR_ACCENT;
			setBackgroundColor(WPTheme.getThemeColor());
			break;
		case ButtonColors.COLOR_LIGHT:
			COLOR = ButtonColors.COLOR_LIGHT;
			setBackgroundColor(WPTheme.calcLight);
			break;
		case ButtonColors.COLOR_DARK:
			COLOR = ButtonColors.COLOR_DARK;
			setBackgroundColor(WPTheme.defMenuBackground);
			break;
		case ButtonColors.COLOR_TRIG:
			COLOR = ButtonColors.COLOR_TRIG;
			setBackgroundColor(WPTheme.calcTrigMode);
			break;
		default:
			COLOR = color;
			setBackgroundColor(COLOR);
			break;
		}
		
		setTextColor(mColor);
	}
	
	/**
	 * Gets the current background color.
	 */
	public int getColor()
	{
		return COLOR;
	}
	
	/**
	 * Set the text color to be used when
	 * the button is pressed down.
	 * 
	 * @param color
	 */
	public void setTextColorDown(int color)
	{
		mColorDown = color;
	}
	
	/**
	 * @return The text color to be used
	 * when the button is pressed down.
	 */
	public int getTextColorDown()
	{
		return mColorDown;
	}
	
	// Update the color of this SeekBar
	// to the current theme color.
	
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus)
	{
		super.onWindowFocusChanged(hasWindowFocus);
		if (COLOR == ButtonColors.COLOR_ACCENT)
			setBackgroundColor(WPTheme.getThemeColor());
	}
	
	@Override
	protected void onWindowVisibilityChanged(int visibility)
	{
		super.onWindowVisibilityChanged(visibility);
		if (COLOR == ButtonColors.COLOR_ACCENT)
			setBackgroundColor(WPTheme.getThemeColor());;
	}

	/**
	 * Handle touch events to set background color
	 * to white and text color to black.
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event)
	{
		switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
            	setTextColor(mColorDown);
            	setBackgroundColor(Color.WHITE);
		
				// Vibrate for the given time if set to do so.
				if (mShouldVibrate && mVibrator != null &&
					mVibrationDuration > 0)
						mVibrator.vibrate(mVibrationDuration);

            	break;
            }
            
            case MotionEvent.ACTION_CANCEL:
            {
            	setColor(COLOR);
                break;
            }

            case MotionEvent.ACTION_UP:
            {
            	setColor(COLOR);
                break;
            }
        }
		
		return false;
	}
	
	/**
	 * Set the function in which the
	 * button should do when clicked.
	 * @param function
	 */
	public void setFunction(int function)
	{
		mFunction = function;
	}
	
	/**
	 * @return The function in which the
	 * button should do when clicked.
	 */
	public int getFunction()
	{
		return mFunction;
	}
	
	// Listen for a focus change.
	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect)
	{
		if (gainFocus)
		{
			setTextColor(Color.BLACK);
        	setBackgroundColor(Color.WHITE);
		}
		else
		{
			setColor(COLOR);
		}
	}
}
