package com.tombarrasso.android.wp7calculator;

/*
 * HomeActivity.java
 *
 * Copyright (C) 2012 Thomas James Barrasso
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
import java.nio.charset.Charset;

// Arity Packages
import org.javia.arity.Symbols;
import org.javia.arity.SyntaxException;

// App Packages
import com.tombarrasso.android.wp7ui.extras.Changelog;
import com.tombarrasso.android.wp7calculator.CalcTask.OnCalculationListener;
import com.tombarrasso.android.wp7calculator.Constants.ButtonColors;
import com.tombarrasso.android.wp7calculator.History;
import com.tombarrasso.android.wp7ui.view.WPMenuItem;
import com.tombarrasso.android.wp7ui.app.WPActivity;
import com.tombarrasso.android.wp7ui.app.WPDialog;
import com.tombarrasso.android.wp7ui.statusbar.StatusBarView;
import com.tombarrasso.android.wp7ui.widget.WPRadioButton;
import com.tombarrasso.android.wp7ui.widget.WPTextView;
import com.tombarrasso.android.wp7ui.widget.ScrollView;
import com.tombarrasso.android.wp7ui.widget.WPToast;
import com.tombarrasso.android.wp7ui.widget.WPToggleSwitch;
import com.tombarrasso.android.wp7ui.WPTheme;

// Android Packages
import android.app.Dialog;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewConfiguration;
import android.view.View.OnLongClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.format.Time;
import android.preference.PreferenceManager;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation;
import android.text.TextUtils;
import android.util.Log;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Checkable;
import android.widget.RelativeLayout;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.ViewAnimator;
import android.widget.AdapterView;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.content.pm.PackageManager;
import android.text.Html;
import android.view.Menu;
import android.net.Uri;
import android.os.IBinder;
import android.os.Process;
import android.app.ActivityManager;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.res.Configuration;

// Build Packages
import com.tombarrasso.android.wp7bar.AndroidUtils;

// Flip Packages
import com.tekle.oss.android.animation.AnimationFactory;
import com.tekle.oss.android.animation.AnimationFactory.FlipDirection;

// Billing Packages
import com.tombarrasso.android.wp7bar.billing.*;
import com.tombarrasso.android.wp7bar.billing.BillingService.RequestPurchase;
import com.tombarrasso.android.wp7bar.billing.BillingService.RestoreTransactions;
import com.tombarrasso.android.wp7bar.billing.Consts.PurchaseState;
import com.tombarrasso.android.wp7bar.billing.Consts.ResponseCode;

// Miscwidgets Packages
import org.miscwidgets.widget.Panel;

// Animation Packages
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.nineoldandroids.animation.TypeEvaluator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.view.animation.AnimatorProxy;

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
 * <b>Version 2.0</b>
 * <ul>
 *	<li>Added NFC/ Android Beam support!</li>
 *	<li>Fixed decimal on solution bug.</li>
 * </ul>
 * <b>Version 2.1</b>
 * <ul>
 *	<li>New WP7-style menus.</li>
 *	<li>Support for light/ dark themes.</li>
 * </ul>
 *
 * @author		Thomas James Barrasso <contact @ tombarrasso.com>
 * @since		06-03-2012
 * @version		2.1
 * @category	{@link Activity}
 */

public final class HomeActivity extends WPActivity
	implements OnClickListener, OnCalculationListener
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
							   ACCENT_KEY = "Accent_Color",
							   WAKE_KEY = "App_Keep_Screen_On",
							   THEME_KEY = "Theme_Dark";
	
	// UIViews.
	private ListView mHistoryList;
	private AutoResizeTextView mResult;
	private WPTextView mEquation, mMemoryDisplay;
	private ViewGroup mCalcTable;
	private ViewAnimator mFlipContainer;
	// private Checkable mVibToggle, mStatToggle, mAnimToggle, mKeepWakeToggle;
	
	// NFC stuff.
	private NfcAdapter mNfcAdapter;
    private static final int MESSAGE_SENT = 1;
    
    private static final int MENU_NORMAL = 0,
    						 MENU_HISTORY = 1;
	
	// George Orwell would be proud.
	private static final int DIALOG_CHANGELOG = 1984;;
	
	// Resources
	private History.HistoryAdapter mHistoryAdapter;
	private History mHistory;
	private RadioGroup mGroup;
	private Resources mResources;
	private int mLen = 0, mParenCount = 0,
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
	
	private boolean mShouldVibrate = false,
					mStatusBar = false,
					mShouldAnimate = true,
					mShouldKeepWake = false,
					mThemeDark = true;
	
	/**
	 * Recursively set the text of all {@link TextView}s.
	 */
	public static final void setTextColor(View mGroup, int color)
	{
		if (mGroup == null) return;

		if (mGroup instanceof ViewGroup)
		{
			// Don't edit the theme chooser.
			if (mGroup.getId() == R.id.linearlayout_theme_background ||
				mGroup.getId() == R.id.linearlayout_theme_background_selected ||
				mGroup.getId() == R.id.linearlayout_theme_accent) return;
		
			final ViewGroup mVGroup = (ViewGroup) mGroup;
			for (int i = 0, e = mVGroup.getChildCount(); i < e; ++i)
				setTextColor(mVGroup.getChildAt(i), color);
		}
		else if ((mGroup instanceof TextView) && !(mGroup instanceof EditText))
		{
			final TextView mTV = (TextView) mGroup;
			final int mTextColor = mTV.getCurrentTextColor();
			
			// Only apply this to white/ black text.
			if (mTextColor == Color.BLACK ||
				mTextColor == Color.WHITE)
				mTV.setTextColor(color);
		}
	}
	
	/**
	 * Recursively set the {@link OnLongClickListener} of all Views.
	 */
	public static final void setLongClickListener(View mGroup)
	{
		if (mGroup == null) return;

		if (mGroup instanceof ViewGroup)
		{
			final ViewGroup mVGroup = (ViewGroup) mGroup;
			for (int i = 0, e = mVGroup.getChildCount(); i < e; ++i)
				setLongClickListener(mVGroup.getChildAt(i));
		}
		else if (mGroup instanceof View)
		{
			final String mDesc = (String) mGroup.getContentDescription();
			if (mDesc != null && mDesc.length() > 0)
			{				
				mGroup.setOnLongClickListener(mLongClickListener);
			}
		}
	}
	
	private static final LongClickListener mLongClickListener = new LongClickListener();
	
	private static final class LongClickListener
		implements OnLongClickListener
	{
		@Override
		public boolean onLongClick(View v)
		{
			// Check if the View has a contentDescription.
			final String mDesc = (String) v.getContentDescription();
			if (mDesc != null && mDesc.length() > 0)
				Toast.makeText(v.getContext().getApplicationContext(), mDesc, Toast.LENGTH_LONG).show();
			
			// Consume the event, YUM!
			return true;
		}
	}
	
	@Override
	protected void onPause()
	{		
		// Get Preferences and Editor.
		final SharedPreferences mPrefs = PreferenceManager
			.getDefaultSharedPreferences(getApplicationContext());
		final Editor mEditor = mPrefs.edit();

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
		
		if (mResult != null && mResult.getText() != null)
			if (mResult != null) mEditor.putString(RESULT_KEY, mResult.getText().toString());
		if (mEquation != null && mEquation.getText() != null)
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
    	
    	// Check NFC for ICS.
    	if (android.os.Build.VERSION.SDK_INT >= 14) {
			// Check to see that the Activity started due to an Android Beam
			if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
				processIntent(getIntent());
			}
		}
    }
    
    private NfcStuff mNfcStuff = null;
    
    // Internal class for NFC stuff.
    private final class NfcStuff implements
    	CreateNdefMessageCallback, OnNdefPushCompleteCallback
    {
    	/**
		 * Creates a custom MIME type encapsulated in an NDEF record
		 *
		 * @param mimeType
		 */
		public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
			byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
			NdefRecord mimeRecord = new NdefRecord(
					NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
			return mimeRecord;
		}
		
		/**
		 * Implementation for the CreateNdefMessageCallback interface
		 */
		@Override
		public NdefMessage createNdefMessage(NfcEvent event) {
			final Time time = new Time();
			time.setToNow();
			final String text = mResult.getText().toString();
			final NdefMessage msg = new NdefMessage(
					new NdefRecord[] { createMimeRecord(
							"application/com.example.android.beam", text.getBytes())
			 /**
			  * The Android Application Record (AAR) is commented out. When a device
			  * receives a push with an AAR in it, the application specified in the AAR
			  * is guaranteed to run. The AAR overrides the tag dispatch system.
			  * You can add it back in to guarantee that this
			  * activity starts when receiving a beamed message. For now, this code
			  * uses the tag dispatch system.
			  */
			  //,NdefRecord.createApplicationRecord("com.example.android.beam")
			});
			
			return msg;
		}
	
		/**
		 * Implementation for the OnNdefPushCompleteCallback interface
		 */
		@Override
		public void onNdefPushComplete(NfcEvent arg0) {
			// A handler is needed to send messages to the activity when this
			// callback occurs, because it happens from a binder thread
			mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
		}
	}
    
    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }
    
    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    private void processIntent(Intent intent) {
        final Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        final NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        final String mResultTextNfc = new String(
        	msg.getRecords()[0].getPayload());
        	
        // Update our last entry as a number.
		mLastEntry.type = Constants.Type.SOLUTION;
		mLastEntry.num = mResultTextNfc;
		mLastEntry.isFloat = mLastEntry.num.contains(".");
        mResult.setText(mResultTextNfc);
    }
    		
	// App launch.
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        // Check some basic parameters.
		final boolean isEmulator = AndroidUtils.isEmulator();
		final boolean isRelease = AndroidUtils.isRelease(getApplicationContext());
		final boolean isRestricted = isRestricted();
		
		// Don't bother running if this is not signed properly or
		// if we are running this on the Android Emulator. Also
		// check if this Context is restricted (loaded from another
		// application's process). If any of these things are true
		// stop running immediately.
		if (isEmulator || !isRelease || isRestricted) 
		{
			final ActivityManager mManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			if (android.os.Build.VERSION.SDK_INT >= 8) {
				mManager.killBackgroundProcesses(getPackageName());
			} else {
				mManager.restartPackage(getPackageName());
			}
			Process.killProcess(Process.myPid());
			super.finishNow();
			
			return;
		}
	
		mResources = getResources();
    	
    	// This is for localization.
        WPTheme.setThemeColorNames(mResources.getStringArray(R.array.color_names));
	
		super.shouldRemoveStatusBarListeners(false);
        
        // Set this class as the listener for calculation events.
        // If this is not set, calculations will never display.
        CalcTask.setCalculationListener(this);
        
        super.onCreate(savedInstanceState);
        
        // Disable IME for this application
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
        					 WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES);
        
        // Be hardware accelerated if possible.
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		}

		// Get a few values from settings.
		final SharedPreferences mPrefs = PreferenceManager
			.getDefaultSharedPreferences(getApplicationContext());
		mStatusBar = mPrefs.getBoolean(STATUS_KEY, mStatusBar);
		mShouldKeepWake = mPrefs.getBoolean(WAKE_KEY, mShouldKeepWake);
		mShouldAnimate = mPrefs.getBoolean(ANIMATE_KEY, mShouldAnimate);
		mThemeDark = mPrefs.getBoolean(THEME_KEY, mThemeDark);
		
		WPTheme.setThemeDarkUnsynchronized(mThemeDark);
		WPTheme.setAccentColorUnsynchronized(mPrefs.getInt(ACCENT_KEY, WPTheme.getAccentColor()));
		
		updateWindowFlags();
		
		final int mRotation = getRotation();
		// Check which layout to use in landscape.
		setContentView(((mRotation == Surface.ROTATION_270)
			? R.layout.home_two : R.layout.home));
		
		// Since we are caching large views, we want
        // to keep their cache between each animation.
        try {
			((ViewGroup) getWindow().getDecorView()).setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
			((ViewGroup) getWindow().getDecorView()).setDrawingCacheEnabled(true);
		} catch (Throwable t) { /* This shouldn't happen. */ }
		
		// All long clicking for additional information.
		setLongClickListener(getWindow().getDecorView());
		
		// Check NFC for ICS.
		if (android.os.Build.VERSION.SDK_INT >= 14) {	
			try {
				mNfcStuff = new NfcStuff();
			
				// Check for available NFC Adapter
				mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
				
				// This means that the device has NFC.
				if (mNfcAdapter != null)
				{
					// Register callback to set NDEF message
					mNfcAdapter.setNdefPushMessageCallback(mNfcStuff, this);
					// Register callback to listen for message-sent success
					mNfcAdapter.setOnNdefPushCompleteCallback(mNfcStuff, this);
				}
			} catch (Throwable e) {
				Log.w(TAG, "An error occured with NFC Beam, oh well!", e);
			}
		}

		// Inflate the status bar if possible.
		final View mStatusView = findViewById(R.id.statusbarstub);
		if (mStatusBar && mStatusView != null && mStatusView instanceof ViewStub)
			((ViewStub) mStatusView).inflate();
			
		updateThemeColor();
        
        // Fetch UI Views.
        mMemoryDisplay = (WPTextView) findViewById(R.id.memory);
        mEquation = (WPTextView) findViewById(R.id.equation);
        mResult = (AutoResizeTextView) findViewById(R.id.result);
        mCalcTable = (ViewGroup) findViewById(R.id.buttons);
        mHistoryList = (ListView) findViewById(R.id.history_list);
        mFlipContainer = (ViewAnimator) findViewById(R.id.flip_container);
        
        // Gingerbread overscroll glow removal.
        if (android.os.Build.VERSION.SDK_INT >= 9) {
        	mHistoryList.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        
        // Create Fake Menu button for ICS.
        if (android.os.Build.VERSION.SDK_INT >= 14) {
        	if (!ViewConfiguration.get(
        		getApplicationContext()).hasPermanentMenuKey()) {
            	createFakeMenu();
            }
        }
        
        // Move the Result text slightly up to accommodate margin issues.
        final int mUp = mResources.getDimensionPixelSize(R.dimen.vertical_translation);
        final AnimatorProxy mUpProxy = AnimatorProxy.wrap(mResult);
    	mUpProxy.setY(-mUp);
                
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
		
		// Set our adapter and OnItemClickListener.
        if (mHistoryList != null) {
			mHistoryList.setEmptyView(findViewById(R.id.no_history));
			mHistoryList.setAdapter(mHistoryAdapter);
			mHistoryList.setOnItemClickListener(mHistoryClickListener);
		}
	        
        // Setup menu with inverted drawables.
        // Commented out because it was replaced.
        // addMenuItems(ids, drawables, drawablesDown, captions);
        
        setupMenu();
        
        // Fly in if we are returning from a finished Activity.
		final int mDuration = mResources.getInteger(R.integer.flyout_duration);
		if (mDuration > 0 && mShouldAnimate) createActivityAnims();
		
		// Display Change Log.
		final Changelog mChangelog = new Changelog(this);
		if (mChangelog.firstRun())
		{
			// Attach listener to show vibration dialog.
			Log.i(TAG, "Displaying change log.");
			showDialogSafely(DIALOG_CHANGELOG);
		}
    }
    
    private boolean hasCreatedAnims = false;
    
    private final void createActivityAnims()
    {
    	if (mResources == null) return;
    	final int mDuration = mResources.getInteger(R.integer.flyout_duration);
    	if (hasCreatedAnims || mDuration <= 0) return;
    	
    	final int mHalfHeight = (int) (getResources().getDisplayMetrics().heightPixels / 2);
		final Flip3DAnimation mAnimIn = new Flip3DAnimation(120, 0, 0, mHalfHeight),
							  mAnimOut = new Flip3DAnimation(0, 120, 0, mHalfHeight);
		mAnimIn.setInterpolator(mAccelInt);
		mAnimOut.setInterpolator(mAccelInt);
		mAnimIn.setDuration(mDuration);
		mAnimOut.setDuration(mDuration);
		mAnimIn.setFillAfter(true);
		mAnimOut.setFillAfter(true);
		setActivityAnimation(mAnimIn, mAnimOut);
		
		hasCreatedAnims = true;
    }
    
    private static final int mScreenFlags =
    	(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | 
		WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
		WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
		WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
		WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING);
    
    private final void updateWindowFlags()
    {
    	// Check if we should keep the screen on, turn it on,
		// remove the lockscreen... whatever we have to do!
		if (mShouldKeepWake && getWindow() != null) {
			getWindow().addFlags(mScreenFlags);
		} else {
			getWindow().clearFlags(mScreenFlags);
		}
		
		final View mStatusBarView = findViewById(R.id.statusbarview);
		if (mStatusBarView != null) mStatusBarView.setVisibility(
			((mStatusBar) ? View.VISIBLE : View.GONE));

		// Load the status bar if set to do so.
		if (mStatusBar) {
			if (getWindow() != null) {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
				makeFullscreen();
			}
			
			// Inflate the status bar if possible.
			final View mStatusView = findViewById(R.id.statusbarstub);
			if (mStatusView != null && mStatusView instanceof ViewStub)
				((ViewStub) mStatusView).inflate();
		} else {
			if (getWindow() != null) {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
									 WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			}
		}
    }
    
    @Override
    public void onLowMemory()
    {
    	super.onLowMemory();
    	mShouldAnimate = false;
    }
    
    // Update the theme color of this Activity.
	private final void updateThemeColor()
	{
		final SharedPreferences mPrefs = PreferenceManager
			.getDefaultSharedPreferences(getApplicationContext());
		mThemeDark = mPrefs.getBoolean(HomeActivity.THEME_KEY, mThemeDark);
		
		final View mStatusBar = findViewById(R.id.statusbarview);
		if (mStatusBar != null && (mStatusBar instanceof StatusBarView))
		{
			((StatusBarView) mStatusBar).setAllColors(((mThemeDark) ? Color.WHITE : Color.BLACK));
		}
		
		// Make the Memory indicator a solid block behind it.
		if (mMemoryDisplay != null)
			mMemoryDisplay.setBackgroundColor(((mThemeDark) ? Color.BLACK : Color.WHITE));
		
		final View[] mRoots =  {
			getWindow().getDecorView().getRootView(),
			findViewById(R.id.root)
		};
		
		// Update layout and colors.
		if (mRoots != null)
		{
			if (mThemeDark)
			{
				for (final View mRoot : mRoots)
					if (mRoot != null)
						mRoot.setBackgroundColor(Color.BLACK);
				HomeActivity.setTextColor(mRoots[0], Color.WHITE);
			}
			else
			{
				for (final View mRoot : mRoots)
					if (mRoot != null)
						mRoot.setBackgroundColor(Color.WHITE);
				HomeActivity.setTextColor(mRoots[0], Color.BLACK);
			}
		}
	}
    
    private final AccelerateInterpolator mAccelInt = new AccelerateInterpolator();
    
    private final Flip3DAnimation getStartAnimation()
    {
    	// Fly in if we are returning from a finished Activity.
		final int mDuration = mResources.getInteger(R.integer.flyout_duration);
		
		if (mDuration <= 0) return null;
		
    	final Flip3DAnimation mAnimIn = new Flip3DAnimation(120, 0, 0, getResources().getDisplayMetrics().heightPixels / 2);
		mAnimIn.setInterpolator(mAccelInt);
		mAnimIn.setDuration(mDuration);
		mAnimIn.setFillAfter(true);
		return mAnimIn;
    }
    
    // Handle all key presses.
	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{		
		// Make sure we don't handle repeat clicks,
		// ie. holding a button down.
		if (event.getRepeatCount() == 0 &&
				event.getAction() == KeyEvent.ACTION_DOWN)
		{
			switch(event.getKeyCode())
			{
				// Hide menus when back button is pressed.
				case KeyEvent.KEYCODE_BACK:
				{
					// If the menu is opened, close it.
					final Panel mMenu = (Panel) findViewById(R.id.wpmenu);
					if (mMenu.isOpen()) {
						mMenu.setOpen(false, true);
						return true;
					}
					
					// If we are in our history table.
					if (mCalcTable.getVisibility() == View.GONE) {
						flipHistory();
						return true;
					}
				}
			}
		}
		
		return super.dispatchKeyEvent(event);
	}
    
    // Collapse menu if open and back pressed.
    @Override
    public void onBackPressed()
    {
    	// If the menu is opened, close it.
    	if (mMenu == null) mMenu = (Panel) findViewById(R.id.wpmenu);
    	if (mMenu.isOpen()) {
    		mMenu.setOpen(false, true);
    		return;
    	}
    	
    	// If we are in our history table.
		if (mCalcTable.getVisibility() == View.GONE) {
			flipHistory();
			return;
		}
    
    	super.onBackPressed();
    }
    
    private Panel mMenu;
    
    // Toggle the WP menu when MENU button pressed.
    @Override
	public boolean onPrepareOptionsMenu(Menu menu) 
	{
		if (mMenu == null) mMenu = (Panel) findViewById(R.id.wpmenu);
		mMenu.setOpen(!mMenu.isOpen(), true);
		return super.onPrepareOptionsMenu(menu);
	}
	
	/**
	 * @return The orientation of the device, one of the
	 * constants in {@link Surface}.
	 */
	public final int getRotation()
	{
		final WindowManager mWM = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		final Display mDisplay = mWM.getDefaultDisplay();
		
		// Froyo and up use getRotation().
		if (android.os.Build.VERSION.SDK_INT >= 8)
		{
			return mDisplay.getRotation();
		}
		
		return mDisplay.getOrientation();
	}
	
	/**
	 * @return An {@link Intent} to share this application.
	 */
	private final Intent getShareIntent()
	{
		final Intent mIntent = new Intent(android.content.Intent.ACTION_SEND);  
    	mIntent.setType("text/plain");  
    	mIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));  
		mIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_text));  
      
   		return Intent.createChooser(mIntent, getString(R.string.share_title));  
	}
	
	/**
	 * Opacity of the WP7-style AppBar/ menu.
	 */
	private static final int MENU_OPACITY = (int) (0.75f * 255);
    
    // Setup the bottom WP7 menu.
    private final void setupMenu()
    {    
    	final View mMoreDots = findViewById(R.id.menu_more),
    			   mMenuContent = findViewById(R.id.panelContent),
    			   mMenuHandle = findViewById(R.id.panelHandle);
    	
    	if (mMenu == null) mMenu = (Panel) findViewById(R.id.wpmenu);
    	
    	// Set menu background color.
    	if (mMenu != null) {
    		final int mMenuBackColor = getResources().getColor(
    			((mThemeDark) ? R.color.menu : R.color.menu_light));
    			
    		// This lets the menu know what to keep track of.
    		mMenu.setBackgroundColors(mMenuBackColor);
    		// mMenuHandle.setBackgroundColor(mMenuBackColor);
    		// mMenuContent.setBackgroundColor(mMenuBackColor);
    	}
    	
    	// Set the dots to light.
    	if (mMoreDots != null) {
    		((ImageView) mMoreDots).setImageResource(((mThemeDark) ?
    			R.drawable.points_white : R.drawable.points_black));
    			
    		// Check if we have rotated.
    		final int mRotation = getRotation();
    		if (mRotation == Surface.ROTATION_90 ||
    			mRotation == Surface.ROTATION_270)
    		{
    			final AnimatorProxy mAnimProxy = AnimatorProxy.wrap(mMoreDots);
    			mAnimProxy.setRotation(90);
    		}
    	}
    	
    	updateMenu(MENU_NORMAL);
    }
        
    private final MenuClickWrapper mHistoryMenuListener =
    	new MenuClickWrapper(new View.OnClickListener()
    {
    	@Override
    	public void onClick(View mView)
    	{
    		flipHistory();
    	}
    });
    
    private final void flipHistory()
    {
    	final View mHistList = (View) mHistoryList.getParent();
    	
    	if (mShouldAnimate) {
			// Save some drawing cache for better performance.
			((ViewGroup) mHistList).setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
			((ViewGroup) mHistList).setDrawingCacheEnabled(true);
			mCalcTable.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
			mCalcTable.setDrawingCacheEnabled(true);
		}
			
		final boolean isCalcVisible = (mCalcTable.getVisibility() == View.VISIBLE);
		
		if (mShouldAnimate) {
			AnimationFactory.flipTransition(mFlipContainer, FlipDirection.LEFT_RIGHT);
		} else {
			// Just toggle visibility if we shouldn't animate.
			mCalcTable.setVisibility(((isCalcVisible) ? View.GONE : View.VISIBLE));
			mHistList.setVisibility(((isCalcVisible) ? View.VISIBLE : View.GONE));
		}
		updateMenu(((isCalcVisible) ? MENU_HISTORY : MENU_NORMAL));
    }
    
    private final void updateMenu(int menu)
    {
    	// Get all of our menu items.
    	final TextView mSettings = (TextView) findViewById(R.id.settings_button),
				   	   mHistory = (TextView) findViewById(R.id.history),
				   	   mShare = (TextView) findViewById(R.id.share_button),
				   	   mRate = (TextView) findViewById(R.id.rate_button);
    
    	switch(menu)
    	{
    		case MENU_NORMAL:
    		{		
				// Make items visible.   
				mSettings.setVisibility(View.VISIBLE);
				mShare.setVisibility(View.VISIBLE);
				mRate.setVisibility(View.VISIBLE);
				mHistory.setVisibility(View.VISIBLE);
				
				// Update text.
				mSettings.setText(R.string.settings);
				mShare.setText(R.string.share);
						   
    			// Set click listener for rate button.
				mRate.setOnClickListener((OnClickListener) new MenuClickWrapper((OnClickListener)
					new UrlClickListener(this, getString(R.string.rateuri))));
					
				// Set click listener for share button.
				mShare.setOnClickListener((OnClickListener) new MenuClickWrapper((OnClickListener)
					new LaunchIntentListener(getShareIntent(), this)));
					
				// Set click listener for settings button.
				mSettings.setOnClickListener((OnClickListener) new MenuClickWrapper((OnClickListener)
					new LaunchClickListener(AdvancedActivity.class, this)));
					
				mHistory.setOnClickListener(mHistoryMenuListener);
					
    			break;
    		}
    		case MENU_HISTORY:
    		{
    			// Make items visible/ hidden.   
				mSettings.setVisibility(View.VISIBLE);
				mShare.setVisibility(View.VISIBLE);
				mRate.setVisibility(View.GONE);
				mHistory.setVisibility(View.GONE);
				
				// Update text.
				mSettings.setText(R.string.history_back);
				mShare.setText(R.string.history_clear);
				
				// Update click listeners.
				mSettings.setOnClickListener(mHistoryMenuListener);
				mShare.setOnClickListener(mClearMenuListener);
    		
    			break;
    		}
    	}
    }
    
    private final MenuClickWrapper mClearMenuListener =
    	new MenuClickWrapper(new View.OnClickListener()
    {
    	@Override
    	public void onClick(View mView)
    	{
    		mHistory.clear();
    	}
    });
        
    /**
     * Wrapper class that closes the menu when pressed.
     */
    private final class MenuClickWrapper
    	implements View.OnClickListener
    {
    	private final View.OnClickListener mListener;
    
    	public MenuClickWrapper(View.OnClickListener mListener)
    	{
    		this.mListener = mListener;
    	}
    
    	@Override
    	public void onClick(View mView)
    	{
    		if (mListener != null) mListener.onClick(mView);
    		
    		// If the menu is opened, close it.
			if (mMenu == null) mMenu = (Panel)
				HomeActivity.this.findViewById(R.id.wpmenu);
			if (mMenu != null && mMenu.isOpen()) {
				mMenu.setOpen(false, false);
			}
    	}
    };
    
    private static final int REQUEST_CODE = 0x7;
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	super.onWindowFocusChanged(hasFocus);
    	
    	// if (hasFocus) startAnim();
    	
    	Log.v(TAG, "Window focus: " + Boolean.toString(hasFocus) + ".");
    }
    
    private final void startAnim() {
    	final Flip3DAnimation mAnim = getStartAnimation();
			 
		 if (mAnim == null) return;
		 
		 final View mView = getWindow().getDecorView().findViewById(Window.ID_ANDROID_CONTENT);
		 if (mView != null) mView.startAnimation(mAnim);
    }
    
    // Restart animation on failure.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    
		 if (resultCode == RESULT_CANCELED && mShouldAnimate) {
			 startAnim();
			 
			 Log.v(TAG, "Activity canceled.");
		}
    }
    
    /**
     * Show a dialog but avoid the slight possibility of
     * an FC if we are exiting.
     */
    private final void showDialogSafely(int mDialog)
    {
    	if (!isFinishing()) showDialog(mDialog);
    }

    /** This handler receives a message from onNdefPushComplete */
    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
				case MESSAGE_SENT:
				{
					WPToast.makeText(getApplicationContext(),
						getString(R.string.message_sent), WPToast.LENGTH_LONG).show();
					break;
				}
            }
        }
    };
    
    // Click handler for menu button.
    private final View.OnClickListener mMenuClickListener =
    	new View.OnClickListener()
    {
    	@Override
    	public void onClick(View mView)
    	{
    		switch(mView.getId()) {
    			case R.id.overflow_menu: {
    				toggleMenu();
    				break;
    			}
    		}
    	}
    };
    
    /**
     * Create the fake menu button and make it work!
     */
    private final void createFakeMenu()
    {
    	Log.v(TAG, "ICS w/o physical menu button.");
    }
    
    /**
     * Handle the opening/ closing
     * of a WPDialog box.
     */
    public Dialog onCreateDialog(int id)
    {
    	final WPDialog mDialog = new WPDialog(this);
		if (mStatusBar)
			mDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	
    	switch(id)
    	{
			case DIALOG_CHANGELOG:
			{
				// Get the dialog for the change log.
				final Changelog mChangeLog = new Changelog(this);
				return mChangeLog.getLogDialog();
			}
    	}
    	return mDialog;
    }
    
    // Dismiss dialog click listener.
	private final DialogInterface.OnClickListener mDismissListener =
		new DialogInterface.OnClickListener()
	{	
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			dialog.dismiss();
		}
	};
    
    /**
     * Called when this activity becomes visible.
     */
    @Override
    protected void onStart() {
        
        // Get a few values from settings.
		final SharedPreferences mPrefs = PreferenceManager
			.getDefaultSharedPreferences(getApplicationContext());
			
		if (mPrefs != null) {
			mStatusBar = mPrefs.getBoolean(STATUS_KEY, mStatusBar);
			mShouldKeepWake = mPrefs.getBoolean(WAKE_KEY, mShouldKeepWake);
			mShouldAnimate = mPrefs.getBoolean(ANIMATE_KEY, mShouldAnimate);
			mThemeDark = mPrefs.getBoolean(THEME_KEY, mThemeDark);
			
			updateWindowFlags();
		}
		
		if (mShouldAnimate) createActivityAnims();
		
		// Stop animations if necessary.
		setAnimate(mShouldAnimate);
		
		updateThemeColor();
		setupMenu();
		
		super.onStart();
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
			flipHistory();
	  	 }
    };

	/**
     * Restart the application to allow the newly
     * chosen theme to take affect. <em>Note:</em>
     * this clears all Activities, only call this
     * method at the appropriate time when all data
     * has been saved.
     */
    public final void restart()
    {
    	final Intent mIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
		mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		super.finishNow();
		startActivity(mIntent);
    }
    
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
    
    /*@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		// Use better API for Honeycomb+
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			recreate();
			return;
		}
		
		startActivity(getIntent());
		super.finishNow();
	}*/

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
		else if (mLastEntry.type == Constants.Type.NUMBER ||
				 mLastEntry.type == Constants.Type.SOLUTION)
		{
    		// Only add a period if there is not one already.
    		// Also make sure that there is room for another period.
    		if (mLen < Constants.MAX_LEN-1 && !mLastEntry.isFloat)
    		{
    			if (mResult != null) mResult.setText(mResult.getText() + string(R.string.period));
    			mLastEntry.isFloat = true;
    			mLastEntry.type = Constants.Type.NUMBER;
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
    
    // Listener for when the button that takes the
	// user to the Activity to hide/ show icons is clicked.
	public static final class LaunchClickListener implements View.OnClickListener
	{
		private final Class mClass;
		private final Context mContext;

		public LaunchClickListener(Class mClass, Context mContext)
		{
			this.mClass = mClass;
			this.mContext = mContext;
		}

		@Override
		public void onClick(View mView)
		{
			// Launch the Activity.
			if (mContext instanceof Activity) {
				((Activity) mContext).startActivityForResult(new Intent(mContext, mClass), REQUEST_CODE);
			} else {
				mContext.startActivity(new Intent(mContext, mClass));
			}
			
			// Disable default Activity animations.
			try {
				if (mContext instanceof Activity) {
					((Activity) mContext).overridePendingTransition(0, 0);
				}
			} catch (Throwable t) { }
		}
	};
	
	// Listener for when the button that takes the
	// user to the Activity to hide/ show icons is clicked.
	public static final class UrlClickListener implements View.OnClickListener
	{
		private final String url;
		private final Context mContext;

		public UrlClickListener(Context mContext, String url)
		{
			this.url = url;
			this.mContext = mContext;
		}

		@Override
		public void onClick(View mView)
		{
			try
			{
				final Intent intent = new Intent(Intent.ACTION_VIEW);
				final Uri mUri = Uri.parse(url);
				intent.setData(mUri);
				
				// Launch the Activity.
				if (mContext instanceof Activity) {
					((Activity) mContext).startActivityForResult(intent, REQUEST_CODE);
				} else {
					mContext.startActivity(intent);
				}
			}
			catch (Throwable e)
			{
				Log.e(TAG, "Error loading webpage.", e);
			}
			
			// Disable default Activity animations.
			try {
				if (mContext instanceof Activity) {
					((Activity) mContext).overridePendingTransition(0, 0);
				}
			} catch (Throwable t) { }
		}
	};
	
	// Listener for when the button that takes the
	// user to the Activity to hide/ show icons is clicked.
	public static final class LaunchComponentListener implements View.OnClickListener
	{
		private final String mAction;
		private final Context mContext;

		public LaunchComponentListener(String mAction, Context mContext)
		{
			this.mAction = mAction;
			this.mContext = mContext;
		}

		@Override
		public void onClick(View mView)
		{
			// Disable default Activity animations.
			try {
				// Launch the Activity.
				if (mContext instanceof Activity) {
					((Activity) mContext).startActivityForResult(new Intent(mAction), REQUEST_CODE);
				} else {
					mContext.startActivity(new Intent(mAction));
				}
			
				if (mContext instanceof Activity) {
					((Activity) mContext).overridePendingTransition(0, 0);
				}
			} catch (Throwable t) { }
		}
	};
	
	// Listener for when the button that takes the
	// user to the Activity to hide/ show icons is clicked.
	public static final class LaunchIntentListener implements View.OnClickListener
	{
		private final Intent mIntent;
		private final Context mContext;

		public LaunchIntentListener(Intent mIntent, Context mContext)
		{
			this.mIntent = mIntent;
			this.mContext = mContext;
		}

		@Override
		public void onClick(View mView)
		{
			// Disable default Activity animations.
			try {
				// Launch the Activity.
				if (mContext instanceof Activity) {
					((Activity) mContext).startActivityForResult(mIntent, REQUEST_CODE);
				} else {
					mContext.startActivity(mIntent);
				}
			
				if (mContext instanceof Activity) {
					((Activity) mContext).overridePendingTransition(0, 0);
				}
			} catch (Throwable t) { }
		}
	};
}
