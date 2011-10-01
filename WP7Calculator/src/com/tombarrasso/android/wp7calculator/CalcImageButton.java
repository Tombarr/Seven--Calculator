package com.tombarrasso.android.wp7calculator;

/*
 * CalcImageButton.java
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

// Android Packages
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;

/**
 * Button used to display an image button.
 *
 * <u>Change log:</u>
 * <b>Version 1.01</b>
 * <ul>
 *	<li>Vibrate onDown instead of onClick.</li>
 * </ul>
 *
 * @author		Thomas James Barrasso <contact @ tombarrasso.com>
 * @since		2011
 * @version		1.01
 * @category	View
 */

public class CalcImageButton extends ImageView implements OnTouchListener
{
	public static final String TAG = CalcImageButton.class.getSimpleName();
	private int COLOR = ButtonColors.COLOR_LIGHT;
	private int mUpId, mDownId;
	private int mFunction = Constants.Tags.CLEAR;
	private boolean mShouldListen = true,
					mShouldVibrate = false;
	private static Vibrator mVibrator;
	private final int mVibrationDuration;
	
	// ====================
	//    Construction
	// ====================
	
	public CalcImageButton(Context context)
	{
		super(context);
		mVibrationDuration = context.getResources().getInteger(R.integer.vibration_duration);
		init();
	}
	
	public CalcImageButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mVibrationDuration = context.getResources().getInteger(R.integer.vibration_duration);
		setAttrs(attrs);
		init();
	}
		
	public CalcImageButton(Context context, AttributeSet attrs, int defStyle)
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
			setColor(attr.getColor(R.styleable.Calc_color, COLOR));
			setSrcDown(attr.getResourceId(R.styleable.Calc_srcdown, -1));
			setSrcUp(attr.getResourceId(R.styleable.Calc_src, -1));
			
			// If none of the attributes were used do not
			// use the custom OnTouchListener.
			if (attr.getColor(R.styleable.Calc_color, Integer.MAX_VALUE) == Integer.MAX_VALUE &&
				attr.getResourceId(R.styleable.Calc_src, Integer.MAX_VALUE) == Integer.MAX_VALUE &&
				attr.getResourceId(R.styleable.Calc_srcdown, Integer.MAX_VALUE) == Integer.MAX_VALUE)
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
		default:
			COLOR = color;
			setBackgroundColor(COLOR);
			break;
		}
	}
	
	/**
	 * Set the XML Resource id for the
	 * image to be used when the button
	 * is pressed down.
	 * 
	 * @param id
	 */
	private void setSrcDown(int id)
	{
		mDownId = id;
	}
	
	/**
	 * Set the XML Resource id for the
	 * image to be used when the button
	 * is pressed up.
	 * 
	 * @param id
	 */
	private void setSrcUp(int id)
	{
		mUpId = id;
		setImageResource(mUpId);
	}

	/**
	 * Gets the current background color.
	 */
	public int getColor()
	{
		return COLOR;
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
            	setBackgroundColor(Color.WHITE);
            	setImageResource(mDownId);

				// Vibrate for the given time if set to do so.
				if (mShouldVibrate && mVibrator != null &&
					mVibrationDuration > 0)
						mVibrator.vibrate(mVibrationDuration);

            	break;
            }
            
            case MotionEvent.ACTION_CANCEL:
            {
            	setColor(COLOR);
            	setImageResource(mUpId);
                break;
            }

            case MotionEvent.ACTION_UP:
            {
            	setColor(COLOR);
            	setImageResource(mUpId);
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
        	setBackgroundColor(Color.WHITE);
		}
		else
		{
			setColor(COLOR);
		}
	}
}
