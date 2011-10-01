package com.tombarrasso.android.wp7calculator;

/*
 * HomeActivity.java
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

// App Packages
import java.util.List;
import java.util.ArrayList;

// Arity Packages
import org.javia.arity.Symbols;
import org.javia.arity.SyntaxException;

// App Packages
import com.tombarrasso.android.wp7ui.extras.Changelog;
import com.tombarrasso.android.wp7calculator.CalcTask.OnCalculationListener;
import com.tombarrasso.android.wp7calculator.Constants.ButtonColors;
import com.tombarrasso.android.wp7calculator.History;
import com.tombarrasso.android.wp7ui.WPMenuItem;
import com.tombarrasso.android.wp7ui.app.WPActivity;
import com.tombarrasso.android.wp7ui.app.WPDialog;
import com.tombarrasso.android.wp7ui.widget.WPRadioButton;
import com.tombarrasso.android.wp7ui.widget.WPTextView;
import com.tombarrasso.android.wp7ui.widget.WPToggleSwitch;
import com.tombarrasso.android.wp7ui.WPTheme;

// Android Packages
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.widget.RadioGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Checkable;
import android.view.LayoutInflater;

/**
 * This {@link Activity} contains the logic behind the calculator.
 * It records user input in the form of strings and passes it to
 * {@link arity} for expression evaluation. The current equation
 * presented to the user above the result, and below that contains
 * the buttons for user interaction.
 * <br /><br />
 * <u>Change log:</u><br />
 * <b>Version 1.01</b>
 * <ul>
 * 	<li>Store Memory to SD.</li>
 * </ul>
 * <b>Version 1.02</b>
 * <ul>
 * 	<li>Function implementation.</li>
 * </ul>
 * <b>Version 1.03</b>
 * <ul>
 * 	<li>Parenthesis implementation.</li>
 *	<li>Fly out Activity Animations</li>
 * </ul>
 * <b>Version 1.04</b>
 * <ul>
 * 	<li>Keyboard support</li>
 * </ul>
 * <b>Version 1.05</b>
 * <ul>
 * 	<li>Fixed localization bug</li>
 * 	<li>Added more languages</li>
 * 	<li>Fixed decimal removal bug</li>
 * </ul>
 * <b>Version 1.06</b>
 * <ul>
 * 	<li>Dialog/ Menu performance and repeated displaying</li>
 * 	<li>Theme engine</li>
 * </ul>
 * <b>Version 1.07</b>
 * <ul>
 * 	<li>Dialog and shake for vibration setting</li>
 * 	<li>Storing all values in {@link SharedPreferences}</li>
 * 	<li>Bug fixes for delete button</li>
 * <ul>
 * <b>Version 1.08</b>
 * <ul>
 * 	<li>Allow change of sign in solutions</li>
 *	<li>Show Vibration setting after change log</li>
 * 	<li>History</li>
 * </ul> 
 *
 * @author		Thomas James Barrasso <contact @ tombarrasso.com>
 * @since		08-30-2011
 * @version		1.09
 * @category	Activity
 */

public class HomeActivity extends WPActivity implements OnClickListener, OnCalculationListener
{
	public static final String TAG = HomeActivity.class.getName(),
							   PACKAGE = HomeActivity.class.getPackage().getName(),
							   VIBRATE_KEY = "Should_Vibrate",
							   MEMORY_KEY = "Memory_Value",
							   TRIG_KEY = "Trig_Mode",
							   RESULT_KEY = "Result_Value",
							   EQUATION_KEY = "Equation_Value",
							   STATUS_KEY = "Status_Show",
							   ANIMATE_KEY = "Animation_Mode",
							   ACCENT_KEY = "Accent_Color";
	
	// UIViews.
	private static AutoResizeTextView mResult;
	private static WPTextView mEquation, mMemoryDisplay;
	private static ViewGroup mCalcTable;
	private static Checkable mVibToggle, mStatToggle, mAnimToggle;
	
	// Menu items.
	private static final int DIALOG_CHANGELOG = 1,
							 DIALOG_VIBRATE = 2,
							 DIALOG_HISTORY = 3;

	private static final int MENU_ITEM_1 = 1, MENU_ITEM_2 = 2, MENU_ITEM_3 = 3;
	private static final int[] ids = { MENU_ITEM_1, MENU_ITEM_2, MENU_ITEM_3 };
	private static final int[] drawables =
		{ R.drawable.theme, R.drawable.settings, R.drawable.history };
	private static final int[] drawablesDown = { R.drawable.theme_inverted, R.drawable.settings_inverted, R.drawable.history_inverted };
	
	// Resources
	private History.HistoryAdapter mHistoryAdapter;
	private History mHistory;
	private RadioGroup mGroup;
	private static Resources mResources;
	private static int mLen = 0, mParenCount = 0,
					   mTrigMode = Constants.TrigMode.DEGREE;
	
    // Data that is contained during
    // a configuration change.
    public static final class RotationData
    {
    	String mResultText, mEquationText;
    	State superstate;
		ArrayList<History.HistoryEntry> mHistory;
    }
	
	// Entries
	public static final class Entry
	{
		public String num;
		public int type;
		public boolean isSet;
		public boolean isFloat;
		public boolean isNegative;
	}
	private static String mLastOperator = "",
						  mLastNum = "",
						  mLastEquation = "";
	private static final Entry mLastEntry = new Entry(),
							   mMemory = new Entry();
	
	private Vibrator mVibrator;
	private boolean mShouldVibrate = false,
					mStatusBar = false,
					mShouldAnimate = true;
	
	@Override
	protected void onPause()
	{		
		// Get Preferences and Editor.
		final SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		final Editor mEditor = mPrefs.edit();
		
		// Store whether or not to vibrate.
		mEditor.putBoolean(VIBRATE_KEY, mShouldVibrate);
		mEditor.putBoolean(STATUS_KEY, mStatusBar);
		mEditor.putBoolean(ANIMATE_KEY, mShouldAnimate);
		mEditor.putInt(ACCENT_KEY, WPTheme.getThemeColor());

		int mMemVal = 0;
		try
		{
			if (mMemory.num != null && !mMemory.num.equals(""))
				mMemVal = Integer.parseInt(mMemory.num, 10);
		}
		catch (NumberFormatException e)
		{
			Log.w(TAG, "Could not save memory to preferences");
		}
		mEditor.putInt(MEMORY_KEY, (mMemory.isSet) ? mMemVal : 0);
		mEditor.putInt(TRIG_KEY, mTrigMode);
		if (mResult != null) mEditor.putString(RESULT_KEY, mResult.getText().toString());
		if (mEquation != null) mEditor.putString(EQUATION_KEY, mEquation.getText().toString());
		mEditor.commit();

		// Write history contents to file.
		mHistory.writeToFile();

		super.onPause();
	}

    // Retrieve memory from SD onResume.
    @Override
    protected void onResume()
    {
		getMemory();
    	super.onResume();
    }
		
	// App launch.
    @Override
    public void onCreate(Bundle savedInstanceState)
    {	
    	mResources = getResources();
    	// This is for localization.
        WPTheme.setThemeColorNames(mResources.getStringArray(R.array.color_names));
        
        // Set this class as the listener for calculation events.
        // If this is not set, calculations will never display.
        CalcTask.setCalculationListener(this);
        
        super.onCreate(savedInstanceState);

		// Get a few values from settings.
		final SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mStatusBar = mPrefs.getBoolean(STATUS_KEY, mStatusBar);
		mShouldAnimate = mPrefs.getBoolean(ANIMATE_KEY, mShouldAnimate);
		WPTheme.setThemeColor(mPrefs.getInt(ACCENT_KEY, WPTheme.defThemeColor));

		// Load the status bar if set to do so.
		if (mStatusBar) makeFullscreen();
		    setContentView(R.layout.home);

		// Inflate the status bar if possible.
		final View mStatusView = findViewById(R.id.statusbarstub);
		if (mStatusBar && mStatusView != null && mStatusView instanceof ViewStub)
			((ViewStub) mStatusView).inflate();
        
        // Fetch UI Views.
        mMemoryDisplay = (WPTextView) findViewById(R.id.memory);
        mEquation = (WPTextView) findViewById(R.id.equation);
        mResult = (AutoResizeTextView) findViewById(R.id.result);
        mCalcTable = (ViewGroup) findViewById(R.id.buttons);
                
        // Attaches an onClickListener to every CalcButton.
        if (mCalcTable != null) attachClickListeners(mCalcTable, this);
        if (mMemoryDisplay != null) mMemoryDisplay.setText(R.string.m);
        
        // Load from last configuration.
        final RotationData mData = (RotationData) getLastNonConfigurationInstance();
        if (mData != null)
        {
		// Create History either from saved data during rotation,
		// or by initializing it from the saved data file.
        	super.restoreFromState(mData.superstate);
		mHistory = new History(this, mData.mHistory);
        	if (mResult != null) mResult.setText(mData.mResultText);
        	if (mEquation != null) mEquation.setText(mData.mEquationText);
        }
        else
        {
		mHistory = new History(this);

	        // Initialize last entry
	        mLastEntry.num = string(R.string.zero);
	        mLastEntry.isSet = true;
	        mLastEntry.type = Constants.Type.NUMBER;
	        mLastEntry.isFloat = false;
	        mLastEntry.isNegative = false;
	        if (mResult != null) mResult.setText(R.string.zero);
        }

		// Adapter for history.
		mHistoryAdapter = mHistory.getAdapter(R.layout.history);
	        
        // Setup menu with inverted drawables.
        addMenuItems(ids, drawables, drawablesDown);
        
        // Fly in if we are returning from a finished Activity.
		final int mDuration = mResources.getInteger(R.integer.flyout_duration);
		if (mDuration > 0 && mShouldAnimate)
		{
			final Flip3DAnimation mAnimIn = new Flip3DAnimation(120, 0, 0, getResources().getDisplayMetrics().heightPixels / 2),
								  mAnimOut = new Flip3DAnimation(0, 120, 0, getResources().getDisplayMetrics().heightPixels / 2);
			mAnimIn.setInterpolator(new AccelerateInterpolator());
			mAnimOut.setInterpolator(new AccelerateInterpolator());
			mAnimIn.setDuration(mDuration);
			mAnimOut.setDuration(mDuration);
			mAnimIn.setFillAfter(true);
			mAnimOut.setFillAfter(true);
			setActivityAnimation(mAnimIn, mAnimOut);
		}
		
		// Display Change Log.
		final Changelog mChangelog = new Changelog(this);
		if (mChangelog.firstRun())
		{
			// Attach listener to show vibration dialog.
			Changelog.setOnChangelogDismissedListener(mChangelogListener);
			Log.i(TAG, "Displaying change log...");
			showDialog(DIALOG_CHANGELOG);
		}
    }
    
    // Go to theme selection activity when settings menu item is clicked.
    @Override
    public void onOptionsItemSelected(WPMenuItem item)
    {
    	switch(item.getItemId())
    	{
			case MENU_ITEM_1:
			{
				startActivity(new Intent(HomeActivity.this,
					ThemeActivity.class));
				break;
			}
			case MENU_ITEM_2:
			{
				showDialog(DIALOG_VIBRATE);
				break;
			}
			case MENU_ITEM_3:
			{
				showDialog(DIALOG_HISTORY);
				break;
			}
    	}
    }
    
    /**
     * Handle the opening/ closing
     * of a WPDialog box.
     */
    public Dialog onCreateDialog(int id)
    {
    	final WPDialog mDialog = new WPDialog(this);
		if (mStatusBar)
			mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	
    	switch(id)
    	{
    	case DIALOG_CHANGELOG:
    	{
    		// Get the dialog for the change log.
    		final Changelog mChangeLog = new Changelog(this);
    		return mChangeLog.getLogDialog();
    	}
    	case DIALOG_VIBRATE:
    	{
    		mDialog.setTitle(R.string.settings);
			final LinearLayout mContainer = new LinearLayout(this);
			final LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			final ViewGroup mVibration = (ViewGroup) mInflater.inflate(R.layout.settings_item, null),
					mStatus = (ViewGroup) mInflater.inflate(R.layout.settings_item, null),
					mAnimate = (ViewGroup) mInflater.inflate(R.layout.settings_item, null);
			mContainer.setOrientation(LinearLayout.VERTICAL);
			mVibration.setId(R.id.settings_vibration);
			mStatus.setId(R.id.settings_status);

			// Get the checkable of various settings.
			mVibToggle = (Checkable) mVibration.findViewById(R.id.setting_toggle);
			mStatToggle = (Checkable) mStatus.findViewById(R.id.setting_toggle);
			mAnimToggle = (Checkable) mAnimate.findViewById(R.id.setting_toggle);

			// Set text of settings.
			((TextView) mStatus.findViewById(R.id.setting_desc)).setText(R.string.status);
			((TextView) mVibration.findViewById(R.id.setting_desc)).setText(R.string.vibrate);
			((TextView) mAnimate.findViewById(R.id.setting_desc)).setText(R.string.animate);

			mContainer.addView(mVibration);
			mContainer.addView(mStatus);
			mContainer.addView(mAnimate);
			mDialog.setMessageView(mContainer);

			// Set enabled/ disabled.
			if (mShouldVibrate) mVibToggle.setChecked(mShouldVibrate);
			if (mStatusBar) mStatToggle.setChecked(mStatusBar);
			if (mShouldAnimate) mAnimToggle.setChecked(mShouldAnimate);

				mDialog.setPositiveButton(R.string.change, mVibrateListener);
				mDialog.setNegativeButton(R.string.cancel, mVibrateListener);
				break;
			}
		case DIALOG_HISTORY:
		{
			mDialog.setTitle(R.string.history);
			mDialog.setFullScreen(true);
			final ListView mList = new ListView(this);
			// Remove background color, cache color hint,
			// scrolling edge, divider, etc.
			mList.setBackgroundColor(Color.TRANSPARENT);
			mList.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			mList.setSelector(R.drawable.history_item);
			mList.setDividerHeight(0);
			mList.setCacheColorHint(Color.TRANSPARENT);
			mList.setAdapter(mHistoryAdapter);
			mList.setOnItemClickListener(mHistoryClickListener);
			mList.setVerticalFadingEdgeEnabled(false);
			mDialog.setMessageView(mList);
			mDialog.setPositiveButton(R.string.history_close, mHistoryListener);
			mDialog.setNegativeButton(R.string.history_clear, mHistoryListener);
			break;
		}
    	}
    	return mDialog;
    }

    // Handle a history item being clicked.
    private final ListView.OnItemClickListener mHistoryClickListener =
    new ListView.OnItemClickListener()
    {
	   	@Override
	   	public void onItemClick(AdapterView<?> adapter, View view, int position, long arg)
		{
			// Restore screen from history.
		  		final History.HistoryEntry mEntry = mHistoryAdapter.getItem(position);
			mEquation.setText(mEntry.mEquation);
			mResult.setText(mEntry.mResult);
			mLastEntry.type = Constants.Type.SOLUTION;
			mLastEntry.num = mEntry.mResult;

			// Determine if the number contains a period.
			mLastEntry.isFloat = mLastEntry.num.contains(".");
			dismissDialog(DIALOG_HISTORY);
	  	 }
    };
    
    /**
     * Click handler for the "yes" or "no" vibration buttons.
     */
    private final DialogInterface.OnClickListener mVibrateListener =
    	new DialogInterface.OnClickListener()
    {
    	@Override
    	public void onClick(DialogInterface dialog, int which)
    	{
			// If the change button was pressed, then
			// change settings and restart app.
    		if (which == WPDialog.POSITIVE_BUTTON)
			{
				mStatusBar = mStatToggle.isChecked();
				mShouldVibrate = mVibToggle.isChecked();
				mShouldAnimate = mAnimToggle.isChecked();
				restart();
			}

    		dismissDialog(DIALOG_VIBRATE);
    	}
    };

	/**
     * Restart the application to allow the newly
     * chosen theme to take affect. <em>Note:</em>
     * this clears all Activities, only call this
     * method at the appropriate time when all data
     * has been saved.
     */
    public void restart()
    {
    	final Intent mIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
		mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(mIntent);
    }

    /**
     * Click handler for dismissing the history dialog.
     */
    private DialogInterface.OnClickListener mHistoryListener =
    	new DialogInterface.OnClickListener()
    {
    	@Override
    	public void onClick(DialogInterface dialog, int which)
    	{
			// If the negative button was clicked...
			if (which != WPDialog.POSITIVE_BUTTON)
			{
				// Clear the history.
				mHistory.clear();
			}
	
			// Dismiss the dialog either way.
			dismissDialog(DIALOG_HISTORY);
		}
    };
    
    /**
     * Attaches an {@link OnClickListener} to every {@link View} of
     * a particular type with a given layout; recursively.
     * @param mLayout The layout to traverse.
     * @param mListener The listener to attach.
     */
    private void attachClickListeners(ViewGroup mLayout, OnClickListener mListener)
    {
    	// Set the click handler of all buttons.
        for (int i = 0, e = mLayout.getChildCount(); i < e; ++i)
        {
        	final View mChild = mLayout.getChildAt(i);
        	if (mChild instanceof ViewGroup)
        	{
        		attachClickListeners((ViewGroup) mChild, mListener);
        	}
        	else if (mChild instanceof CalcButton || mChild instanceof CalcImageButton)
        	{
        		mChild.setOnClickListener(mListener);
        		setTextForButton(mChild);
        	}
        }
    }
        
    /**
     * Set the text of for the supplied child.
     * @param mChild
     */
    private void setTextForButton(View mChild)
    {
    	if (mChild instanceof CalcButton)
    	{
    		final int mTag = ((CalcButton) mChild).getFunction();
    		int mStrId = 0;
    		switch(mTag)
        	{
        	case Constants.Tags.CLEAR:
        		mStrId = R.string.clear;
	        	break;
        	case Constants.Tags.MEMCLEAR:
        		mStrId = R.string.memclear;
        		break;
        	case Constants.Tags.MEMRECALL:
        		mStrId = R.string.memrecall;
	        	break;
        	case Constants.Tags.MEMPLUS:
        		mStrId = R.string.memplus;
        		break;
        	case Constants.Tags.PLUSMINUS:
        		mStrId = R.string.plusminus;
        		break;
        	case Constants.Tags.PERCENT:
        		mStrId = R.string.percent;
        		break;
        	case Constants.Tags.DIVIDE:
        		mStrId = R.string.divide;
        		break;
        	case Constants.Tags.SEVEN:
        		mStrId = R.string.seven;
        		break;
        	case Constants.Tags.EIGHT:
        		mStrId = R.string.eight;
        		break;
        	case Constants.Tags.NINE:
        		mStrId = R.string.nine;
        		break;
        	case Constants.Tags.MULTIPLY:
        		mStrId = R.string.multiply;
        		break;
        	case Constants.Tags.FOUR:
        		mStrId = R.string.four;
        		break;
        	case Constants.Tags.FIVE:
        		mStrId = R.string.five;
        		break;
        	case Constants.Tags.SIX:
        		mStrId = R.string.six;
        		break;
        	case Constants.Tags.MINUS:
        		mStrId = R.string.minus;
        		break;
        	case Constants.Tags.ONE:
        		mStrId = R.string.one;
        		break;
        	case Constants.Tags.TWO:
        		mStrId = R.string.two;
        		break;
        	case Constants.Tags.THREE:
        		mStrId = R.string.three;
        		break;
        	case Constants.Tags.PLUS:
        		mStrId = R.string.plus;
        		break;
        	case Constants.Tags.ZERO:
        		mStrId = R.string.zero;
        		break;
        	case Constants.Tags.PERIOD:
        		mStrId = R.string.period;
        		break;
        	case Constants.Tags.PAREN_OPEN:
        		mStrId = R.string.paren_open;
        		break;
        	case Constants.Tags.PAREN_CLOSE:
        		mStrId = R.string.paren_close;
        		break;
        	case Constants.Tags.PI:
        		mStrId = R.string.pi;
        		break;
        	case Constants.Tags.DEGREE:
        		mStrId = R.string.degree;
        		break;
        	case Constants.Tags.RADIAN:
        		mStrId = R.string.radian;
        		break;
        	case Constants.Tags.GRADIAN:
        		mStrId = R.string.grad;
        		break;
        	case Constants.Tags.SQRT:
        		mStrId = R.string.sqrt;
        		break;
        	case Constants.Tags.SIN:
        		mStrId = R.string.sin;
        		break;
        	case Constants.Tags.COS:
        		mStrId = R.string.cos;
        		break;
        	case Constants.Tags.TAN:
        		mStrId = R.string.tan;
        		break;
        	case Constants.Tags.NATURAL_LOG:
        		mStrId = R.string.ln;
        		break;
        	case Constants.Tags.LOG:
        		mStrId = R.string.log;
        		break;
        	case Constants.Tags.EXPONENTIAL_TEN:
        		mStrId = R.string.exponential_ten;
        		break;
        	case Constants.Tags.FACTORIAL:
        		mStrId = R.string.factorial;
        		break;
        	case Constants.Tags.SQUARED:
        		mStrId = R.string.squared;
        		break;
        	case Constants.Tags.EXPONENTIAL:
        		mStrId = R.string.exponential;
        		break;
        	case Constants.Tags.EQUALS:
        		mStrId = R.string.equals;
        		break;
        	}
    		
    		if (mStrId != 0) ((CalcButton) mChild).setText(mStrId);
    	}
    }
    
    /**
     * Updates the color of the three trig type buttons.
     * The currently used one is given a different color
     * than every other one.
     */
    private void attachTrigColors(ViewGroup mLayout, int mode)
    {
    	mTrigMode = mode;
    	
    	final int mTrigTag = (mTrigMode == Constants.TrigMode.GRADIAN) ? Constants.Tags.GRADIAN :
			((mTrigMode == Constants.TrigMode.DEGREE) ? Constants.Tags.DEGREE : Constants.Tags.RADIAN);
    	
    	// Prevent NullPointerException.
    	if (mLayout == null) return;
    	
    	// Set the click handler of all buttons.
        for (int i = 0, e = mLayout.getChildCount(); i < e; ++i)
        {
        	final View mChild = mLayout.getChildAt(i);
        	
        	int mTag = -1;
        	if (mChild instanceof ViewGroup)
        	{
        		attachTrigColors((ViewGroup) mChild, mode);
        	}
        	else if (mChild instanceof CalcButton)
        	{
        		mTag = ((CalcButton) mChild).getFunction();
        		
        		final boolean isTrigButton = (mTag == Constants.Tags.GRADIAN ||
    					mTag == Constants.Tags.RADIAN || mTag == Constants.Tags.DEGREE);
    			
    			// Update color of trig button.
    			if (mTrigTag == mTag && isTrigButton)
    			{
    				((CalcButton) mChild).setColor(ButtonColors.COLOR_TRIG);
    			}
    			else if (mTrigTag != mTag && isTrigButton)
    			{
    				((CalcButton) mChild).setColor(ButtonColors.COLOR_LIGHT);
    			}
        	}
        	else if (mChild instanceof CalcImageButton)
        	{
        		mTag = ((CalcImageButton) mChild).getFunction();
        		
        		final boolean isTrigButton = (mTag == Constants.Tags.GRADIAN ||
    					mTag == Constants.Tags.RADIAN || mTag == Constants.Tags.DEGREE);
    			
    			// Update color of trig button.
    			if (mTrigTag == mTag && isTrigButton)
    			{
    				((CalcImageButton) mChild).setColor(ButtonColors.COLOR_TRIG);
    			}
    			else if (mTrigTag != mTag && isTrigButton)
    			{
    				((CalcImageButton) mChild).setColor(ButtonColors.COLOR_LIGHT);
    			}
        	}
		}
    }
    
    @Override
    public Object onRetainNonConfigurationInstance()
    {
    	final RotationData mData = new RotationData();
    	
    	// Prevent NullPointerException.
    	if (mResult != null)
    		mData.mResultText = mResult.getText().toString();
    	if (mEquation != null)
    		mData.mEquationText = mEquation.getText().toString();
    	
	mData.mHistory = mHistory.getHistoryList();
    	mData.superstate = (State) super.onRetainNonConfigurationInstance();
    	return mData;
    }
    
    // Updates memory based on SD storage.
    private void getMemory()
    {
    	final SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

    	// Load whether to vibrate or not.
    	mShouldVibrate = mPrefs.getBoolean(VIBRATE_KEY, mShouldVibrate);
		mStatusBar = mPrefs.getBoolean(STATUS_KEY, mStatusBar);
    	
    	// Load memory value.
    	final int mMemValue = mPrefs.getInt(MEMORY_KEY, 0);
    	if (mMemValue != 0)
    	{
    		mMemory.num = mMemValue + "";
    		mMemory.type = Constants.Type.NUMBER;
			mMemory.isSet = true;
			if (mMemoryDisplay != null)
				mMemoryDisplay.setVisibility(View.VISIBLE);
    	}
    	
    	// Save the last shown result and equation.
    	if (mResult != null) mResult.setText(mPrefs.getString(RESULT_KEY, string(R.string.zero)));
    	if (mEquation != null) mEquation.setText(mPrefs.getString(EQUATION_KEY, ""));
    	
    	// Attempt to load the default trig mode
    	// from file, if not use default.
    	final int mMemTrig = mPrefs.getInt(TRIG_KEY, mTrigMode);
    	
    	// Get trig mode.
		handleTrigMode(mMemTrig);
    }
    
    /**
     * Gets the string from a resource id.
     */
    private String string(int id)
    {
    	return mResources.getString(id);
    }
    
    /**
     * Remove all instances of a
     * character in a given string.
     */
    public static final String removeChar(String s, char c)
    {
    	if (s == null) return null;
		String mWithoutChar = "";
		final int e = s.length();
		for (int i = 0; i < e; i++)
		{
			final char cur = s.charAt(i);
			if (cur != c) mWithoutChar += cur;
		}
		return mWithoutChar;
    }
    
    /**
     * Update the trig mode.
     */
    private void handleTrigMode(int mode)
    {
    	mTrigMode = mode;
    	
    	attachTrigColors(mCalcTable, mode);
    }
    
    // Clears the calculator screen & equation.
    public void clear()
    {
    	if (mEquation != null) mEquation.setText("");
		mLastOperator = "";
		if (mResult != null) mResult.setText(string(R.string.zero));
		mLastEntry.num = string(R.string.zero);
		mLastEntry.isFloat = false;
		mLastEntry.type = Constants.Type.NUMBER;
    }
    
    // Deletes the last entry, or handles appropriately.
    public void delete()
    {
    	final int mLen = mLastEntry.num.length();
		
		// Allow percentages to delete.
		if (mLastEntry.type == Constants.Type.PERCENT_OP)
			mLastEntry.type = Constants.Type.NUMBER;
		
		// If we have 2+ Constants.Type.NUMBERs, remove the last.
		if (mLastEntry.type == Constants.Type.NUMBER && mLen > 1)
		{
			// The last character is a period.
			final String mText = (mResult == null) ? "" : mResult.getText().toString();
			final boolean lastPeriod = (mText.lastIndexOf(string(R.string.period)) == mText.length() - 1),
						  hasPeriod = mText.contains(string(R.string.period));
			
			if (lastPeriod && mResult != null)
			{
				mResult.setText(mText.substring(0, mText.length() - 1));
				mLastEntry.num = mResult.getText().toString();
				mLastEntry.isFloat = false;
				return;
			}
			
	    	boolean addZeros = false;
    		if (mLastEntry.num.charAt(mLen - 2) == Constants.Chars.zero)
    			addZeros = true;
			
    		if (mResult != null)
    		{
				mResult.setCommaSeparatedText(mLastEntry.num.substring(0, mLen - 1));
				mLastEntry.num = mResult.getText().toString();
    		}
						
			// Determine if the Constants.Type.NUMBER contains a period.
			mLastEntry.isFloat = mLastEntry.num.contains(string(R.string.period));
			
			// Because of the Constants.Type.NUMBERFormat used we
			// have to re-append a period.
			if (!lastPeriod && !mLastEntry.isFloat && hasPeriod && mResult != null)
			{
				mResult.setText(mResult.getText() + string(R.string.period));
				mLastEntry.num = mResult.getText().toString();
				mLastEntry.isFloat = true;
			}
			
			// Add zeros back that were trimmed.
    		if (addZeros)
    		{
    			for (int i = 0, j = (mLen - 1) - mLastEntry.num.length(); i < j; ++i)
    				mLastEntry.num += Constants.Chars.zero;
    			if (mResult != null) mResult.setText(mLastEntry.num);
    		}
		}
		// If we have one Constants.Type.NUMBER, go to zero.
		else if (mLastEntry.type == Constants.Type.NUMBER && mLen == 1)
		{
			// Determine if the number contains a period.
			final String mText = (mResult == null) ? "" : mResult.getText().toString();
			final boolean isFloat = mText.contains(string(R.string.period));
			if (isFloat && mResult != null)
			{
				mResult.setText(mText.substring(0, 1));
				mLastEntry.num = mResult.getText().toString();
				mLastEntry.isFloat = false;
			}
			else if (!isFloat && mResult != null)
			{
				mResult.setText(string(R.string.zero));
				mLastEntry.num = string(R.string.zero);
				mLastEntry.isFloat = false;
			}
		}
		else if (mLastEntry.type == Constants.Type.FUNCTION)
		{
			// Clear result.
			if (mResult != null) mResult.setText(string(R.string.zero));
			mLastEntry.num = string(R.string.zero);
			mLastEntry.isFloat = false;
			mLastEntry.type = Constants.Type.NUMBER;
		}
    }

    public Changelog.OnChangelogDismissedListener mChangelogListener =
        new Changelog.OnChangelogDismissedListener()
    {
        public void onChangelogDismissed()
        {
	    showDialog(DIALOG_VIBRATE);
        }
    };
    
    // Handle a percent operation.
    public void percent()
    {
    	// Only take the percentage of a Constants.Type.NUMBER.
		if (mLastEntry.type == Constants.Type.NUMBER)
		{
			// Put percent in result.
			if (mResult != null)
			{
				mLastEntry.num = mResult.getText().toString() + string(R.string.percent);
				mResult.setText(mLastEntry.num);
			}
			mLastEntry.type = Constants.Type.PERCENT_OP;
		}
    }
    
    // Handle adding a decimal.
    public void period()
    {
    	// If an operation just happened and period is
		// clicked, display 0. as to begin forming a fraction.
		if (mLastEntry.type == Constants.Type.OPERATOR)
		{
			if (mResult != null)
			{
				mResult.setText(string(R.string.zero) + string(R.string.period));
				mLastEntry.num = mResult.getText().toString();
			}
			mLastEntry.type = Constants.Type.NUMBER;
			mLastEntry.isFloat = true;
		}
		else if (mLastEntry.type == Constants.Type.NUMBER)
		{
    		// Only add a period if there is not one already.
    		// Also make sure that there is room for another period.
    		if (mLen < Constants.MAX_LEN-1 && !mLastEntry.isFloat)
    		{
    			if (mResult != null) mResult.setText(mResult.getText() + string(R.string.period));
    			mLastEntry.isFloat = true;
    		}
		}
    }
    
    // Handle parenthesis operation.
    public void handleParen(int paren)
    {
    	switch(paren)
    	{
    	case Constants.Tags.PAREN_OPEN:
    	{
    		if (mLastEntry.type == Constants.Type.FUNCTION || mLastEntry.type == Constants.Type.PERCENT_OP
    				|| mLastEntry.type == Constants.Type.OPERATOR || mLastEntry.type == Constants.Type.NUMBER
    				|| mLastEntry.type == Constants.Type.PAREN)
    		{
	    		mLastEntry.num = string(R.string.paren_open);
	    		mLastEntry.type = Constants.Type.PAREN;
	    		if (mEquation != null) mEquation.setText(mEquation.getText().toString() + " " + mLastEntry.num);
	    		mParenCount++;
    		}
    		break;
    	}
    	case Constants.Tags.PAREN_CLOSE:
    	{
    		if ((mLastEntry.type == Constants.Type.NUMBER || mLastEntry.type == Constants.Type.SOLUTION ||
    				mLastEntry.type == Constants.Type.FUNCTION || mLastEntry.type == Constants.Type.PERCENT_OP
    				|| mLastEntry.type == Constants.Type.PAREN) && mParenCount > 0)
    		{
	    		mLastEntry.num = string(R.string.paren_close);
	    		mLastEntry.type = Constants.Type.PAREN;

    			if (mEquation != null && mResult != null)
    			{
		    		final String mText = removeChar(mResult.getText().toString(), Constants.Chars.comma);
	    			mEquation.setText(mEquation.getText().toString() +
	    					" " + mText + mLastEntry.num);
	    			mResult.setText(string(R.string.zero));
    			}
	    		
	    		mParenCount--;
    		}
    		break;
    	}
    	}
    }
    
    // Handle determining a solution.
    public void equals()
    {
    	// This might look excessive but it keeps this extensible.
		if ((mLastEntry.type == Constants.Type.FUNCTION || mLastEntry.type == Constants.Type.SOLUTION ||
				mLastEntry.type == Constants.Type.NUMBER || mLastEntry.type == Constants.Type.OPERATOR ||
				mLastEntry.type == Constants.Type.PERCENT_OP || mLastEntry.type == Constants.Type.PAREN)
				&& (mResult != null && mEquation != null))
		{
			final String mText = removeChar(mResult.getText().toString(),
					Constants.Chars.comma), equation;
			final CalcTask mTask = new CalcTask();
			
			// Attempt to calculate the result of the equation.
			// If the last button clicked was an operation,
			// apply it to the currently displayed Constants.Type.NUMBER.
			
			// If the last button was equals,
			// use the last Constants.Type.OPERATOR on the result.
			if (mLastEntry.type == Constants.Type.SOLUTION && !mLastOperator.equals(""))
				equation = mText + " " +  mLastOperator + " " + mLastNum;
			
			// If the last button pressed was an Constants.Type.OPERATOR,
			// use that Constants.Type.OPERATOR on the result.
			else if (mLastEntry.type == Constants.Type.OPERATOR)
				equation = mText + " " + mLastEntry.num + " " + mText;
			
			// If the last button pressed was a Constants.Type.PARENthesis,
			// use the equation (the result is irrelevant.
			else if (mLastEntry.type == Constants.Type.PAREN)
				equation = mEquation.getText().toString();
			
			// If the last button pressed was a Constants.Type.NUMBER,
			// add that Constants.Type.NUMBER to the current equation and calculate.
			else
				equation = mEquation.getText().toString() + " " + mText;

			mLastEquation = removeChar(equation, Constants.Chars.comma);
			
			// Run the background task to do the computation.
			mTask.execute(removeChar(equation, Constants.Chars.comma));
		}
    }
    
	// Triggered when a calculation has finished.
	@Override
	public void onCalculate(double result)
	{
		if (mResult != null && mEquation != null)
		{
			mResult.setCommaSeparatedText(result);
			mEquation.setText("");
			
			mLastEntry.type = Constants.Type.SOLUTION;
			mLastEntry.isFloat = false;
			mLastEntry.num = removeChar(mResult.getText().toString(), Constants.Chars.comma);

			// Store result in history.
			mHistory.addEntry(new History.HistoryEntry(mLastEquation, mLastEntry.num));
		}
		
		// Determine if the Constants.Type.NUMBER contains a period.
		mLastEntry.isFloat = mLastEntry.num.contains(".");
	}

	// Triggered if a calculation could not be completed.
	@Override
	public void onError()
	{
		if (mResult != null) mResult.setText("Syntax Error");
	}
    
    /**
     * Handle button clicks.
     */
    @Override
    public void onClick(View view)
    {
    	// Get the behavior of the button.
    	final int tag;
    	if (view instanceof CalcButton)
    		tag = ((CalcButton) view).getFunction();
    	else if (view instanceof CalcImageButton)
    		tag = ((CalcImageButton) view).getFunction();
    	else
    		tag = -1; // Nothing...
    	
    	switch(tag)
    	{
    	case Constants.Tags.CLEAR:
    	{
    		clear();
    		break;
    	}
    	case Constants.Tags.MEMCLEAR:
    	{
    		mMemory.isSet = false;
    		mMemory.num = string(R.string.zero);
    		mMemory.type = Constants.Type.NOTYPE;
    		if (mMemoryDisplay != null) mMemoryDisplay.setVisibility(View.GONE);
    		break;
    	}
    	case Constants.Tags.MEMRECALL:
    	{
    		if (mMemory.isSet)
    		{
    			if (mResult != null) mResult.setCommaSeparatedText(mMemory.num);
    			mLastEntry.num = mMemory.num;
    		}
    		else
    		{
    			if (mResult != null) mResult.setText(string(R.string.zero));
    			mLastEntry.num = string(R.string.zero);
    		}
    		mLastEntry.type = Constants.Type.NUMBER;
    		break;
    	}
    	case Constants.Tags.MEMPLUS:
    	{
    		// Only allow saving into memory for these values.
    		if (mLastEntry.type == Constants.Type.NUMBER ||
    				mLastEntry.type == Constants.Type.SOLUTION ||
    				mLastEntry.type == Constants.Type.PAREN)
    		{
	    		final String mText = (mResult == null) ? "" :
	    			removeChar(mResult.getText().toString(), Constants.Chars.comma);
	    		
	    		// Only save Constants.Type.NUMBER if it is not zero.
	    		if (!mText.equals(string(R.string.zero)))
	    		{
				if (mMemory.isSet)
				{
					try
					{
						final Symbols MathFactory = CalcTask.getMathFactory();
						final double mResult = MathFactory.eval(mMemory.num + " + " + mText);
						mMemory.num = ""+mResult;
					}
					catch(SyntaxException e)
					{
						Log.w(TAG, "An error occured while trying to process math statement: (" + mText + ")");
						e.printStackTrace();
					}
				}
				else
				{
			    		mMemory.isSet = true;
			    		mMemory.num = mText;
			    		mMemory.type = Constants.Type.NUMBER;
				    		if (mMemoryDisplay != null) 
					mMemoryDisplay.setVisibility(View.VISIBLE);
				}
	    		}
    		}
    		break;
    	}
    	case Constants.Tags.DELETE:
    	{
    		delete();
    		break;
    	}
    	case Constants.Tags.PLUSMINUS:
    	{
    		if (mLastEntry.type == Constants.Type.NUMBER || mLastEntry.type == Constants.Type.FUNCTION || mLastEntry.type == Constants.Type.SOLUTION)
    		{
    			final String mText = (mResult == null) ? "" : mResult.getText().toString();
    			final int mLen = mText.length();
    			if (mLen > 0)
    			{
    				// Add/ remove minus based on whether
    				// or not it is already there.
    				if (mText.charAt(0) != (char) '-' && mResult != null)
    					mResult.setText(string(R.string.minus) + mText);
    				else if (mResult != null)
    					mResult.setText(mText.substring(1, mLen));
    				if (mResult != null) mLastEntry.num = mResult.getText().toString();
    			}
    		}
    		break;
    	}
    	case Constants.Tags.PERCENT:
    	{
    		percent();
    		break;
    	}
    	case Constants.Tags.DIVIDE:
    		handleOperation(Constants.Tags.DIVIDE);
    		break;
    	case Constants.Tags.SEVEN:
    		handleNumber(7);
    		break;
    	case Constants.Tags.EIGHT:
    		handleNumber(8);
    		break;
    	case Constants.Tags.NINE:
    		handleNumber(9);
    		break;
    	case Constants.Tags.MULTIPLY:
    		handleOperation(Constants.Tags.MULTIPLY);
    		break;
    	case Constants.Tags.FOUR:
    		handleNumber(4);
    		break;
    	case Constants.Tags.FIVE:
    		handleNumber(5);
    		break;
    	case Constants.Tags.SIX:
    		handleNumber(6);
    		break;
    	case Constants.Tags.MINUS:
    		handleOperation(Constants.Tags.MINUS);
    		break;
    	case Constants.Tags.ONE:
    		handleNumber(1);
    		break;
    	case Constants.Tags.TWO:
    		handleNumber(2);
    		break;
    	case Constants.Tags.THREE:
    		handleNumber(3);
    		break;
    	case Constants.Tags.PLUS:
    		handleOperation(Constants.Tags.PLUS);
    		break;
    	case Constants.Tags.ZERO:
    		handleNumber(0);
    		break;
    	case Constants.Tags.PERIOD:
    		period();
    		break;
    	case Constants.Tags.PAREN_OPEN:
    		handleParen(Constants.Tags.PAREN_OPEN);
    		break;
    	case Constants.Tags.PAREN_CLOSE:
    		handleParen(Constants.Tags.PAREN_CLOSE);
    		break;
    	case Constants.Tags.PI:
    		if (mResult != null) mResult.setText("" + Math.PI);
    		if (mResult != null) mLastEntry.num = mLastNum = mResult.getText().toString();
        	mLastEntry.type = Constants.Type.NUMBER;
        	mLastEntry.isFloat = true;
    		break;
    	case Constants.Tags.DEGREE:
    		handleTrigMode(Constants.TrigMode.DEGREE);
    		break;
    	case Constants.Tags.RADIAN:
    		handleTrigMode(Constants.TrigMode.RADIAN);
    		break;
    	case Constants.Tags.GRADIAN:
    		handleTrigMode(Constants.TrigMode.GRADIAN);
    		break;
    	case Constants.Tags.SQRT:
    		handleFunction(Constants.Tags.SQRT);
    		break;
    	case Constants.Tags.SIN:
    		handleFunction(Constants.Tags.SIN);
    		break;
    	case Constants.Tags.COS:
    		handleFunction(Constants.Tags.COS);
    		break;
    	case Constants.Tags.TAN:
    		handleFunction(Constants.Tags.TAN);
    		break;
    	case Constants.Tags.NATURAL_LOG:
    		handleFunction(Constants.Tags.NATURAL_LOG);
    		break;
    	case Constants.Tags.LOG:
    		handleFunction(Constants.Tags.LOG);
    		break;
    	case Constants.Tags.EXPONENTIAL_TEN:
    		handleFunction(Constants.Tags.EXPONENTIAL_TEN);
    		break;
    	case Constants.Tags.FACTORIAL:
    		handleFunction(Constants.Tags.FACTORIAL);
    		break;
    	case Constants.Tags.SQUARED:
    		handleFunction(Constants.Tags.SQUARED);
    		break;
    	case Constants.Tags.EXPONENTIAL:
    		handleOperation(Constants.Tags.EXPONENTIAL);
    		break;
    	case Constants.Tags.EQUALS:
    		equals();
    		break;
    	}
    	
    	if (mResult != null) mLen = mResult.getText().length();
    }
    
    /**
     * Handle operation buttons being pressed.
     */
    private void handleOperation(int operation)
    {    		
		int mOpId = 0;
    	switch(operation)
    	{
    	case Constants.Tags.PLUS:
    		mOpId = R.string.plus;
    		break;
    	case Constants.Tags.MULTIPLY:
    		mOpId = R.string.multiply;
    		break;
    	case Constants.Tags.MINUS:
    		mOpId = R.string.minus;
    		break;
    	case Constants.Tags.DIVIDE:
    		mOpId = R.string.divide;
    		break;
    	case Constants.Tags.EXPONENTIAL:
    		mOpId = R.string.power;
    		break;
    	}
    	
    	final String mEqText = (mEquation == null) ? "" :
    		removeChar(mEquation.getText().toString(), Constants.Chars.comma);
    	
    	// Determine if the last character in the equation
    	// is a Constants.Type.PARENthesis, but avoid a StringIndexOutOfBoundsException.
    	final int mLastIndex = mEqText.length() - 1;
    	boolean mLastParen = false;
    	if (mLastIndex > 0)
    		mLastParen = mEqText.substring(mLastIndex).equals(string(R.string.paren_open));
    	
    	// Don't add space if the Constants.Type.OPERATOR is ^ or (.
    	final String mSpace = (operation == Constants.Tags.EXPONENTIAL) ? "" : " ",
    				 mSpacePrev = (mLastOperator.equals(string(R.string.power))
    					|| mLastParen) ? "" : " ";
    	
    	if (mLastEntry.type == Constants.Type.SOLUTION || mLastEntry.type == Constants.Type.FUNCTION)
    	{
    		// Take the Constants.Type.SOLUTION and add it to a new equation.
    		if (mEquation != null) mEquation.setText(removeChar(mResult.getText().toString(),
    				Constants.Chars.comma) + mSpace + string(mOpId));
    		if (mResult != null)  mResult.setText(string(R.string.zero));
    	}
    	// We cannot operate on an operation.
    	else if (mLastEntry.type == Constants.Type.OPERATOR)
    	{
    		// So we change the operation.
    		final int mLen = mEqText.length();
    		if (mLen > 1 && mEquation != null)
    			mEquation.setText(mEqText.substring(0, mLen - 1) + string(mOpId));
    		return;
    	}
    	else if (mLastEntry.type == Constants.Type.PAREN)
    	{
    		final String mText = (mResult == null) ? "" :
    			removeChar(mResult.getText().toString(), Constants.Chars.comma);
    		if (mEquation != null) mEquation.setText(mEquation.getText().toString() +
    				mSpacePrev + ((mLastEntry.num.equals(string(R.string.paren_open))) ?
    						mText : "") + mSpace + string(mOpId));
    	}
    	else if (mLastEntry.type == Constants.Type.NUMBER || mLastEntry.type == Constants.Type.PERCENT_OP)
    	{
    		if (mEquation != null) mEquation.setText(mEqText +
    				mSpacePrev + removeChar(mLastEntry.num, Constants.Chars.comma) + mSpace + string(mOpId));
    	}
    	mLastEntry.num = mLastOperator = string(mOpId);
		mLastEntry.type = Constants.Type.OPERATOR;
		mLastEntry.isFloat = false;
    }
    
    /**
     * Handle a Constants.Type.NUMBER button being pressed.
     */
    private void handleNumber(int num)
    {
    	if (mLen >= Constants.MAX_LEN && mLastEntry.type == Constants.Type.NUMBER) return;
    	if (mLastEntry.type == Constants.Type.PERCENT_OP) return;
    	if (mLastEntry.type == Constants.Type.FUNCTION) return;
    	
    	// Reset text if the last button clicked
    	// was an Constants.Type.OPERATOR, Constants.Type.PARENthesis, or Constants.Type.SOLUTION.
    	if ((mLastEntry.type == Constants.Type.OPERATOR || mLastEntry.type == Constants.Type.SOLUTION
    			|| mLastEntry.type == Constants.Type.PAREN) && mResult != null)
    		mResult.setText("");
    	
    	int mNumId = 0;
    	switch(num)
    	{
    	case 0:
    	{
    		mNumId = R.string.zero;
    		break;
    	}
    	case 1:
    	{
    		mNumId = R.string.one;
    		break;
    	}
    	case 2:
    	{
    		mNumId = R.string.two;
    		break;
    	}
    	case 3:
    	{
    		mNumId = R.string.three;
    		break;
    	}
    	case 4:
    	{
    		mNumId = R.string.four;
    		break;
    	}
    	case 5:
    	{
    		mNumId = R.string.five;
    		break;
    	}
    	case 6:
    	{
    		mNumId = R.string.six;
    		break;
    	}
    	case 7:
    	{
    		mNumId = R.string.seven;
    		break;
    	}
    	case 8:
    	{
    		mNumId = R.string.eight;
    		break;
    	}
    	case 9:
    	{
    		mNumId = R.string.nine;
    		break;
    	}
    	}
    	
    	// Add the Constants.Type.NUMBER to the display.
    	if (num == 0 && mLastEntry.isFloat)
    	{
    		// If the Constants.Type.NUMBER was zero and a decimal is displaying
    		// then just allow zero to be added.
    		if (mResult != null) mResult.setText(mResult.getText() + string(mNumId));
    	}
    	else
    	{
    		// For all other cases just add the Constants.Type.NUMBER to the display
    		// and calculate whether or not commas formatting.
    		if (mResult != null) mResult.setCommaSeparatedText(mResult.getText() + string(mNumId));
    	}
    	mLastEntry.num = mLastNum = (mResult == null) ? "" : mResult.getText().toString();
    	mLastEntry.type = Constants.Type.NUMBER;
    }
    
    /**
     * Handle when a Constants.Type.FUNCTION button is clicked.
     */
    private void handleFunction(int func)
    {
    	PrintfFormat mFun = null;
    	switch(func)
    	{
    	case Constants.Tags.SQRT:
    		mFun = Constants.FUN_SQRT;
    		break;
    	case Constants.Tags.SIN:
    		if (mTrigMode == Constants.TrigMode.GRADIAN)
    			mFun = Constants.FUN_SIN_GRAD;
    		else if (mTrigMode == Constants.TrigMode.DEGREE)
    			mFun = Constants.FUN_SIN_DEG;
    		else if (mTrigMode == Constants.TrigMode.RADIAN)
    			mFun = Constants.FUN_SIN_RAD;
    		break;
    	case Constants.Tags.COS:
    		if (mTrigMode == Constants.TrigMode.GRADIAN)
    			mFun = Constants.FUN_COS_GRAD;
    		else if (mTrigMode == Constants.TrigMode.DEGREE)
    			mFun = Constants.FUN_COS_DEG;
    		else if (mTrigMode == Constants.TrigMode.RADIAN)
    			mFun = Constants.FUN_COS_RAD;
    		break;
    	case Constants.Tags.TAN:
    		if (mTrigMode == Constants.TrigMode.GRADIAN)
    			mFun = Constants.FUN_TAN_GRAD;
    		else if (mTrigMode == Constants.TrigMode.DEGREE)
    			mFun = Constants.FUN_TAN_DEG;
    		else if (mTrigMode == Constants.TrigMode.RADIAN)
    			mFun = Constants.FUN_TAN_RAD;
    		break;
    	case Constants.Tags.NATURAL_LOG:
    		mFun = Constants.FUN_LN;
    		break;
    	case Constants.Tags.LOG:
    		mFun = Constants.FUN_LOG;
    		break;
    	case Constants.Tags.EXPONENTIAL_TEN:
    		mFun = Constants.FUN_EXPONENTIAL_TEN;
    		break;
    	case Constants.Tags.FACTORIAL:
    		mFun = Constants.FUN_FACTORIAL;
    		break;
    	case Constants.Tags.SQUARED:
    		mFun = Constants.FUN_SQUARED;
    	}
    	
    	if (mFun != null)
    	{
    		final String mText = (mResult == null) ? "" : mResult.getText().toString();
	    	mLastEntry.num = mFun.sprintf(mText);
	    	if (mResult != null) mResult.setText(mLastEntry.num);
	    	mLastEntry.type = Constants.Type.FUNCTION;
    	}
    }
    
    // Handle key events.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
	{
    	// Don't allow repetition.
        if (event.getRepeatCount() == 0)
        {
        	final int mMetaState = event.getMetaState();
        	
        	// Handle various meta keyboard states.
        	if (event.getMatch(Constants.Chars.EQUALS_CHAR, mMetaState) == Constants.Chars.EQUALS_CHAR[0])
        	{
        		equals();
        		return true;
        	}
        	else if (event.getMatch(Constants.Chars.FACTORIAL_CHAR, mMetaState) == Constants.Chars.FACTORIAL_CHAR[0])
        	{
        		handleFunction(Constants.Tags.FACTORIAL);
        		return true;
        	}
        	else if (event.getMatch(Constants.Chars.STAR_CHAR, mMetaState) == Constants.Chars.STAR_CHAR[0])
        	{
        		handleOperation(Constants.Tags.MULTIPLY);
        		return true;
        	}
        	else if (event.getMatch(Constants.Chars.PARENOPEN_CHAR, mMetaState)
        			== Constants.Chars.PARENOPEN_CHAR[0])
        	{
        		handleParen(Constants.Tags.PAREN_OPEN);
        		return true;
        	}
        	else if (event.getMatch(Constants.Chars.PARENCLOSE_CHAR, mMetaState) 
        		== Constants.Chars.PARENCLOSE_CHAR[0])
        	{
        		handleParen(Constants.Tags.PAREN_CLOSE);
        		return true;
        	}
        	else if (event.getMatch(Constants.Chars.PERCENT_CHAR, mMetaState)
        			== Constants.Chars.PERCENT_CHAR[0])
        	{
        		percent();
        		return true;
        	}
        	else if (event.getMatch(Constants.Chars.MINUS_CHAR, mMetaState)
        			== Constants.Chars.MINUS_CHAR[0])
        	{
        		handleOperation(Constants.Tags.MINUS);
        		return true;
        	}
        	else if (event.getMatch(Constants.Chars.PLUS_CHAR, mMetaState)
        			== Constants.Chars.PLUS_CHAR[0])
        	{
        		handleOperation(Constants.Tags.PLUS);
        		return true;
        	}
        	
        	final char mNum = event.getMatch(Constants.Chars.NUMBERS, mMetaState);
        	
        	// Handle a Constants.Type.NUMBER being pressed.
        	if (mNum != 0)
        	{
        		for (int i = 0; i < Constants.Chars.NUMBERS_LEN; ++i)
        		{
        			if (mNum == Constants.Chars.NUMBERS[i])
        			{
        				handleNumber(i);
        				return true;
        			}
        		}
        	}
        	
        	// Handle all other keys missed.
        	switch(keyCode)
        	{
        	case KeyEvent.KEYCODE_DEL:
        		delete();
        		break;
        	case KeyEvent.KEYCODE_CLEAR:
        		clear();
        		break;
        	case KeyEvent.KEYCODE_PERIOD:
        		period();
        		break;
        	case KeyEvent.KEYCODE_SLASH:
        		handleOperation(Constants.Tags.DIVIDE);
        		break;
        	case KeyEvent.KEYCODE_PLUS:
        		handleOperation(Constants.Tags.PLUS);
        		break;
        	case KeyEvent.KEYCODE_ENTER:
        		equals();
        		break;
        	case KeyEvent.KEYCODE_LEFT_BRACKET:
        		handleParen(Constants.Tags.PAREN_OPEN);
        		break;
        	case KeyEvent.KEYCODE_RIGHT_BRACKET:
        		handleParen(Constants.Tags.PAREN_CLOSE);
        		break;
        	case KeyEvent.KEYCODE_MINUS:
        		handleOperation(Constants.Tags.MINUS);
        		break;
        	case KeyEvent.KEYCODE_STAR:
        		handleOperation(Constants.Tags.MULTIPLY);
        		break;
        	case KeyEvent.KEYCODE_0:
        		handleNumber(0);
        		break;
        	case KeyEvent.KEYCODE_1:
        		handleNumber(1);
        		break;
        	case KeyEvent.KEYCODE_2:
        		handleNumber(2);
        		break;
        	case KeyEvent.KEYCODE_3:
        		handleNumber(3);
        		break;
        	case KeyEvent.KEYCODE_4:
        		handleNumber(4);
        		break;
        	case KeyEvent.KEYCODE_5:
        		handleNumber(5);
        		break;
        	case KeyEvent.KEYCODE_6:
        		handleNumber(6);
        		break;
        	case KeyEvent.KEYCODE_7:
        		handleNumber(7);
        		break;
        	case KeyEvent.KEYCODE_8:
        		handleNumber(8);
        		break;
        	case KeyEvent.KEYCODE_9:
        		handleNumber(9);
        		break;
        	}
        }
        
        return super.onKeyDown(keyCode, event);
    }
}
