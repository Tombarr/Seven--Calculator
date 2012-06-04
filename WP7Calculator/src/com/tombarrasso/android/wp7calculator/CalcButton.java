package com.tombarrasso.android.wp7calculator;

/*
 * CalcButton.java
 *
 * Copyright 2012 (C) Thomas James Barrasso
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
import android.view.HapticFeedbackConstants;

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
 * <b>Version 1.03</b>
 * <ul>
 *	<li>Vibrate now done using Haptic Feedback features.</li>
 * </ul>
 * 
 * @author		Thomas James Barrasso <contact @ tombarrasso.com>
 * @since		2012
 * @version		1.03
 * @category	{@link View}
 */

public final class CalcButton extends WPTextView implements OnTouchListener
{
	public static final String TAG = CalcButton.class.getSimpleName();
	public static final int BOLD 	= 1,
							ITALIC 	= 4,
							MEDIUM 	= 3,
							REGULAR = 0,
							LIGHT 	= 2;
	private int COLOR, mColor, mColorDown;
	private final SharedPreferences mPrefs;
	private int mFunction = Constants.Tags.CLEAR;
	private boolean mShouldListen = true,
					mShouldVibrate = false;
	private final int mVibrationDuration;
	
	// ====================
	//    Construction
	// ====================
	
	public CalcButton(Context context)
	{
		super(context);
		mVibrationDuration = context.getResources().getInteger(R.integer.vibration_duration);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		init();
	}
	
	public CalcButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mVibrationDuration = context.getResources().getInteger(R.integer.vibration_duration);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		setAttrs(attrs);
		init();
	}
		
	public CalcButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		mVibrationDuration = context.getResources().getInteger(R.integer.vibration_duration);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
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
			setTextColorDown(attr.getColor(R.styleable.Calc_textColorDown,
				((WPTheme.isThemeDark()) ? Color.BLACK : Color.WHITE)));
			setFont(attr.getInt(R.styleable.Calc_font, REGULAR));
			mColor = attr.getColor(R.styleable.Calc_textColor, ((WPTheme.isThemeDark()) ? Color.WHITE : Color.BLACK));
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
		setHapticFeedbackEnabled(true);
		
		// Get a few values from settings.
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
		final boolean isDark = WPTheme.isThemeDark();
		
		switch (color)
		{
			case ButtonColors.COLOR_ACCENT:
				COLOR = ButtonColors.COLOR_ACCENT;
				setBackgroundColor(WPTheme.getThemeColor());
				break;
			case ButtonColors.COLOR_LIGHT:
				COLOR = ButtonColors.COLOR_LIGHT;
				setBackgroundColor(((isDark) ? WPTheme.calcLight : Constants.LIGHT_BUTTON_COLOR_DARK));
				break;
			case ButtonColors.COLOR_DARK:
				COLOR = ButtonColors.COLOR_DARK;
				setBackgroundColor(((isDark) ? WPTheme.defMenuBackground : Constants.LIGHT_BUTTON_COLOR_LIGHT));
				break;
			case ButtonColors.COLOR_TRIG:
				COLOR = ButtonColors.COLOR_TRIG;
				setBackgroundColor(((isDark) ? WPTheme.calcTrigMode : Constants.LIGHT_BUTTON_COLOR_TRIG));
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
		updateValues();
	}
	
	@Override
	protected void onWindowVisibilityChanged(int visibility)
	{
		super.onWindowVisibilityChanged(visibility);
		updateValues();
	}
	
	private final void updateValues()
	{			
		setColor(getColor());
		
		setTextColorDown(((WPTheme.isThemeDark()) ? Color.BLACK : Color.WHITE));
		mColor = ((WPTheme.isThemeDark()) ? Color.WHITE : Color.BLACK);
		setTextColor(mColor);
			
		mShouldVibrate = mPrefs.getBoolean(HomeActivity.VIBRATE_KEY, mShouldVibrate);
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
            	setBackgroundColor(((WPTheme.isThemeDark()) ? Color.WHITE : Color.BLACK));
		
				// Vibrate for the given time if set to do so.
				if (mShouldVibrate)
					performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
						HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING |
						HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);

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
			setTextColor(((WPTheme.isThemeDark()) ? Color.BLACK : Color.WHITE));
        	setBackgroundColor(((WPTheme.isThemeDark()) ? Color.WHITE : Color.BLACK));
		}
		else
		{
			setColor(COLOR);
		}
	}
}
