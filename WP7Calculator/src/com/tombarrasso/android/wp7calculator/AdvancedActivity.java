package com.tombarrasso.android.wp7calculator;

/*
 * AdvancedActivity.java
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

// Android Packages
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.View;
import android.util.Log;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.app.Dialog;
import android.os.IBinder;
import android.os.Handler;
import android.os.Process;
import android.os.RemoteException;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.text.method.LinkMovementMethod;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManager;
import android.view.Window;
import android.view.MotionEvent;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.EditText;
import android.widget.Spinner;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.provider.Settings;
import android.view.ViewStub;
import android.text.Html;
import android.net.Uri;
import android.view.Window.Callback;
import android.view.View.OnLongClickListener;
import android.os.AsyncTask;
import android.graphics.Point;
import android.view.Display;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.util.TypedValue;
import android.app.ActivityManager;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

// Java Packages
import java.lang.IllegalArgumentException;
import java.util.Locale;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

// Build Packages
import com.tombarrasso.android.wp7bar.AndroidUtils;

// UI Packages
import com.tombarrasso.android.wp7ui.extras.Changelog;
import com.tombarrasso.android.wp7ui.app.WPDialog;
import com.tombarrasso.android.wp7ui.app.WPActivity;
import com.tombarrasso.android.wp7ui.statusbar.StatusBarView;
import com.tombarrasso.android.wp7ui.WPTheme;
import com.tombarrasso.android.wp7ui.WPFonts;
import com.tombarrasso.android.wp7ui.widget.WPToast;
import com.tombarrasso.android.wp7ui.widget.WPButtonView;
import com.tombarrasso.android.wp7ui.widget.WPTextView;
import com.tombarrasso.android.wp7ui.widget.WPThemeView;
import com.tombarrasso.android.wp7ui.widget.ScrollView;
import com.tombarrasso.android.wp7ui.widget.WPToggleSwitch;
import com.tombarrasso.android.wp7ui.widget.WPPivotControl;

// Billing Packages
import com.tombarrasso.android.wp7bar.billing.*;
import com.tombarrasso.android.wp7bar.billing.BillingService.RequestPurchase;
import com.tombarrasso.android.wp7bar.billing.BillingService.RestoreTransactions;
import com.tombarrasso.android.wp7bar.billing.Consts.PurchaseState;
import com.tombarrasso.android.wp7bar.billing.Consts.ResponseCode;

// ACRA Packages
import org.acra.*;
import org.acra.annotation.*;

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
 * This {@link Activity} manages the preferences for the
 * applications and talks to the GUI. It handles click
 * events and starts the corresponding service or change in UI.<br />
 * This is quite a mess and should be cleaned, but its not the
 * focus of the application. Just be sure there are no FCs.
 *
 * @author		Thomas James Barrasso <contact @ tombarrasso.com>
 * @since		06-03-2012
 * @version		2.0
 * @category	{@link Activity}
 */

public final class AdvancedActivity extends WPActivity
{
	public static final String TAG = AdvancedActivity.class.getSimpleName(),
							   PACKAGE = AdvancedActivity.class.getPackage().getName();

	// George Orwell would be proud.
	private static final int DIALOG_ANONYMOUS_REPORTING = 1984,
							 DIALOG_CHANGELOG = DIALOG_ANONYMOUS_REPORTING * 2,
							 DIALOG_RESTART = DIALOG_CHANGELOG * 2,
							 DIALOG_CANNOT_CONNECT_ID = DIALOG_RESTART * 2,
							 DIALOG_DONATION_PURCHASED = DIALOG_CANNOT_CONNECT_ID * 2,
							 DIALOG_BILLING_NOT_SUPPORTED_ID = DIALOG_DONATION_PURCHASED * 2,
							 DIALOG_USER_CANCELLED = DIALOG_BILLING_NOT_SUPPORTED_ID * 2;

	// Views for settings and such.
	private View mReporting,
				 mAppAnims,
				 mRoot,
				 mKeepScreen,
				 mVibrate,
				 mCustomStatus;
				 
	// Pivot screen iDs.
	private static final int SCREEN_ONE = 0,
							 SCREEN_TWO = 1;

	private WPPivotControl mPivot;
	
	private boolean mShouldVibrate = false,
					mStatusBar = false,
					mShouldAnimate = true,
					mShouldKeepWake = false,
					mThemeDark = true,
					mNeedsRestart = false;
					
	private SharedPreferences mPrefs;
	
	private static final class LongClickListener
		implements OnLongClickListener
	{
		@Override
		public boolean onLongClick(View v)
		{
			// Check if the View has a contentDescription.
			final String mDesc = (String) v.getContentDescription();
			if (mDesc != null && mDesc.length() > 0)
				Toast.makeText(v.getContext(), mDesc, Toast.LENGTH_LONG).show();
			
			// Consume the event, YUM!
			return true;
		}
	}
	
	/** This handler receives a message from onNdefPushComplete */
    private final Handler mHandler = new Handler();
	
	private static final class CatalogEntry {
        public String sku;
        public int nameId;
        public Managed managed;

        public CatalogEntry(String sku, int nameId, Managed managed) {
            this.sku = sku;
            this.nameId = nameId;
            this.managed = managed;
        }
    }
    
    /**
     * Each product in the catalog is either MANAGED or UNMANAGED.  MANAGED
     * means that the product can be purchased only once per user (such as a new
     * level in a game). The purchase is remembered by Android Market and
     * can be restored if this application is uninstalled and then
     * re-installed. UNMANAGED is used for products that can be used up and
     * purchased multiple times (such as poker chips). It is up to the
     * application to keep track of UNMANAGED products for the user.
     */
    private enum Managed { MANAGED, UNMANAGED }
    
    /**
     * A {@link PurchaseObserver} is used to get callbacks when Android Market sends
     * messages to this application so that we can update the UI.
     */
    private final class BarPurchaseObserver extends PurchaseObserver {
        public BarPurchaseObserver(Handler handler) {
            super(AdvancedActivity.this, handler);
        }

        @Override
        public void onBillingSupported(boolean supported) {
            if (Consts.DEBUG) {
                Log.i(TAG, "supported: " + supported);
            }
            
            // Show the button if billing is supported.
            /*if (supported && mBuyButton != null) {
                mBuyButton.setVisibility(View.VISIBLE);
                mBuyButton.setEnabled(true);
            }*/
        }

        @Override
        public void onPurchaseStateChange(PurchaseState purchaseState, String itemId,
                int quantity, long purchaseTime, String developerPayload) {
            if (Consts.DEBUG) {
                Log.i(TAG, "onPurchaseStateChange() itemId: " + itemId + " " + purchaseState);
            }
        }

        @Override
        public void onRequestPurchaseResponse(RequestPurchase request,
                ResponseCode responseCode) {
            if (Consts.DEBUG) {
                Log.d(TAG, request.mProductId + ": " + responseCode);
            }
            if (responseCode == ResponseCode.RESULT_OK) {
                if (Consts.DEBUG) {
                    Log.i(TAG, "purchase was successfully sent to server");
                }
                
                showDialogSafely(DIALOG_DONATION_PURCHASED);
            } else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
                if (Consts.DEBUG) {
                    Log.i(TAG, "user canceled purchase");
                }
                
                showDialogSafely(DIALOG_USER_CANCELLED);
            } else {
                if (Consts.DEBUG) {
                    Log.i(TAG, "purchase failed");
                }
                
                showDialogSafely(DIALOG_CANNOT_CONNECT_ID);
            }
        }

        @Override
        public void onRestoreTransactionsResponse(RestoreTransactions request,
                ResponseCode responseCode) {
            if (responseCode == ResponseCode.RESULT_OK) {
                if (Consts.DEBUG) {
                    Log.d(TAG, "completed RestoreTransactions request");
                }
            } else {
                if (Consts.DEBUG) {
                    Log.d(TAG, "RestoreTransactions error: " + responseCode);
                }
            }
        }
    }
    
    /**
	 * Recursively set the {@link OnLongClickListener} of all Views.
	 */
	public static final void updateButtons(View mGroup)
	{
		if (mGroup == null) return;

		if (mGroup instanceof ViewGroup)
		{
			final ViewGroup mVGroup = (ViewGroup) mGroup;
			for (int i = 0, e = mVGroup.getChildCount(); i < e; ++i)
				updateButtons(mVGroup.getChildAt(i));
		}
		else if (mGroup instanceof WPButtonView)
		{
			mGroup.onWindowFocusChanged(true);
		}
	}
    
    private BillingService mBillingService;
	private BarPurchaseObserver mBarPurchaseObserver;
    
    /**
     * The developer payload that is sent with subsequent
     * purchase requests.
     */
    private String mPayloadContents = "";
    
    private final String getPayload() {
    	final String mId = Secure.getString(getContentResolver(),
                        Secure.ANDROID_ID); // could be null!
        if (mId == null) return mPayloadContents;
        
        return mId;
    }
    
    // Donation item for purchase.
    private static final CatalogEntry DONATE = new CatalogEntry(
    	"donate", R.string.donate, Managed.UNMANAGED);
    	
    private String mItemName = "donate";
    private String mSku = mItemName;

    /** Called when the activity is first created. */
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
	
		super.shouldRemoveStatusBarListeners(false);
		
		super.onCreate(savedInstanceState);
		
		// Be hardware accelerated if possible.
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		}

		// Get a few values from settings.
		mPrefs = PreferenceManager
			.getDefaultSharedPreferences(getApplicationContext());
		mStatusBar = mPrefs.getBoolean(HomeActivity.STATUS_KEY, mStatusBar);
		mShouldKeepWake = mPrefs.getBoolean(HomeActivity.WAKE_KEY, mShouldKeepWake);
		mShouldAnimate = mPrefs.getBoolean(HomeActivity.ANIMATE_KEY, mShouldAnimate);
		mThemeDark = mPrefs.getBoolean(HomeActivity.THEME_KEY, mThemeDark);
		mShouldVibrate = mPrefs.getBoolean(HomeActivity.VIBRATE_KEY, mShouldVibrate);
		final int mAccentColor = mPrefs.getInt(HomeActivity.ACCENT_KEY, WPTheme.getAccentColor());
		final String mAccentHex = String.format("%06X", (0xFFFFFF & mAccentColor));
		
		Log.v(TAG, "CREATE/ Theme: " + Boolean.toString(mThemeDark) + ", Accent: #" + mAccentHex + ".");
		
		// This is for localization.
        WPTheme.setThemeColorNames(getResources().getStringArray(R.array.color_names));
		
		WPTheme.setThemeDarkUnsynchronized(mThemeDark);
		WPTheme.setAccentColorUnsynchronized(mAccentColor);
				
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        updateWindowFlags();

        setContentView(R.layout.advanced_config);
        
        // Inflate the status bar if possible.
		final View mStatusView = findViewById(R.id.statusbarstub);
		if (mStatusBar && mStatusView != null && mStatusView instanceof ViewStub)
			((ViewStub) mStatusView).inflate();
        
        // Set up the pivot.
		mPivot = (WPPivotControl) findViewById(R.id.homePivot);
		
		// Call these BEFORE setTab!!!
		mPivot.setTextSize(11);
		mPivot.setFont(WPTextView.BOLD);
		
		mPivot.setTab(SCREEN_ONE, R.string.settings)
			  .setTab(SCREEN_TWO, R.string.theme_norm);

		// Set light/ dark based on the theme.
		mRoot = findViewById(R.id.root);
		updateThemeColor();
		
		// Set long click listener for all Views.
		HomeActivity.setLongClickListener(mRoot);
		
		// Make sure that we should run animations.	  
		if (mShouldAnimate)
		{
			// For Honeycomb and above, animate!
			if (android.os.Build.VERSION.SDK_INT >= 11)
			{
				mPivot.setRotationY(90.0f);
				mPivot.setPivotY(0);
			}
			else
			{
				// Prevent the horrible!
				try {
					// Use the wrapped API functionality.
					final AnimatorProxy mProxy = AnimatorProxy.wrap(mPivot);
					if (mProxy != null)
					{
						mProxy.setPivotY(0);
						mProxy.setRotationY(90.0f);
					}
				} catch (Throwable t) { }
			}
		}
		
		// Setup Android Market Billing.
        mBarPurchaseObserver = new BarPurchaseObserver(mHandler);
        mBillingService = new BillingService();
        mBillingService.setContext(getApplicationContext());

        // Check if billing is supported.
        ResponseHandler.register(mBarPurchaseObserver);
        
        if (!mBillingService.checkBillingSupported()) {
            Log.d(TAG, "Billing is not supported.");
        }
        
        // Add donate button.
		final View mDonate = findViewById(R.id.donate);
		if (mDonate != null) mDonate.setOnClickListener(mBillClick);
		
		// Since we are caching large views, we want
        // to keep their cache between each animation.
        try {
        	((ViewGroup) mRoot).setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
        } catch (Throwable t) { /* This shouldn't happen. */ }

		// Find the toggle for the status bar.
		mReporting = findViewById(R.id.reporting);
		mAppAnims = findViewById(R.id.app_anims);
		mKeepScreen = findViewById(R.id.keep_wake);
		mVibrate = findViewById(R.id.vibrate);
		mCustomStatus = findViewById(R.id.custom_status);
		
		// Set up the PayPal click handler.
		final View mPaypal = findViewById(R.id.paypal);
		if (mPaypal != null) mPaypal.setOnClickListener(
			new HomeActivity.UrlClickListener(this, getString(R.string.paypal_uri)));
		
		// SignalSense question mark.
		View mQMark = findViewById(R.id.reporting_more);
		if (mQMark != null)
		{
			mQMark.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View mView)
				{
					showDialogSafely(DIALOG_ANONYMOUS_REPORTING);
				}
			});
		}
		
		// Handle the click of the app icon.
		final View mHomeIcon = findViewById(R.id.icon);
		if (mHomeIcon != null) mHomeIcon.setOnClickListener(mHomeListener);
		
		// Since we are caching large views, we want
        // to keep their cache between each animation.
        try {
        	((ViewGroup) mRoot).setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
        } catch (Throwable t) { /* This shouldn't happen. */ }
		
		setupThemeChooser();
			
		if (mReporting instanceof Checkable)
			((Checkable) mReporting).setChecked(mPrefs.getBoolean(ACRA.PREF_ENABLE_ACRA, true));
		
		// If it is a check box listen for its changes.
		if (mReporting instanceof CompoundButton)
			((CompoundButton) mReporting).setOnCheckedChangeListener(mReportingListener);
			
		if (mAppAnims instanceof Checkable)
			((Checkable) mAppAnims).setChecked(mShouldAnimate);
			
		if (mAppAnims instanceof CompoundButton)
			((CompoundButton) mAppAnims).setOnCheckedChangeListener(mAppAnimListener);
			
		if (mKeepScreen instanceof Checkable)
			((Checkable) mKeepScreen).setChecked(mShouldKeepWake);
			
		if (mKeepScreen instanceof CompoundButton)
			((CompoundButton) mKeepScreen).setOnCheckedChangeListener(mWakeListener);
		
		if (mVibrate instanceof Checkable)
			((Checkable) mVibrate).setChecked(mShouldVibrate);
			
		if (mVibrate instanceof CompoundButton)
			((CompoundButton) mVibrate).setOnCheckedChangeListener(mVibrateListener);
			
		if (mCustomStatus instanceof Checkable)
			((Checkable) mCustomStatus).setChecked(mStatusBar);
			
		if (mCustomStatus instanceof CompoundButton)
			((CompoundButton) mCustomStatus).setOnCheckedChangeListener(mBarListener);
			
		final View mChangeLog = findViewById(R.id.changelog);
		// When clicked, display the change log.
		if (mChangeLog != null) {
			mChangeLog.setOnClickListener(
				new View.OnClickListener()
				{
					@Override
					public void onClick(View mView)
					{
						showDialogSafely(DIALOG_CHANGELOG);
					}			
				}
			);
		}
	}
	
	// Update the theme color of this Activity.
	private final void updateThemeColor()
	{
		mThemeDark = mPrefs.getBoolean(HomeActivity.THEME_KEY, mThemeDark);
		
		final View mStatusBar = findViewById(R.id.statusbarview);
		if (mStatusBar != null && (mStatusBar instanceof StatusBarView))
		{
			((StatusBarView) mStatusBar).setAllColors(((mThemeDark) ? Color.WHITE : Color.BLACK));
		}
		
		// Update layout and colors.
		if (mRoot != null)
		{
			final View mContainer = mRoot.getRootView();
			if (mThemeDark)
			{
				mContainer.setBackgroundColor(Color.BLACK);
				HomeActivity.setTextColor(mContainer, Color.WHITE);
			}
			else if (mRoot != null)
			{
				mContainer.setBackgroundColor(Color.WHITE);
				HomeActivity.setTextColor(mContainer, Color.BLACK);
			}
		}
		
		// Update accent stuff.
		if (mAccentView != null) mAccentView.setBackgroundColor(WPTheme.getAccentColor());
		if (mAccentText != null) mAccentText.setText(WPTheme.getAccentColorName());
	}
	
	// Go home when clicked.
	private final View.OnClickListener mHomeListener
		= new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			finish();
		}
	};
	
	private TextView mThemeSelected,
					 mThemeDarkView,
					 mThemeLight,
					 mAccentText;
	private View mThemeContainer,
				 mThemeContainerSelected,
				 mAccentView,
				 mBatteryWarning;
	
	// Setup everything related to the choosing of
	// a theme preference (light vs. dark).
	private final void setupThemeChooser()
	{
		final Resources mRes = getResources();
		
		// Localize accent colors.
		WPTheme.setThemeColorNames(mRes.getStringArray(R.array.color_names));
	
		mThemeDark = mPrefs.getBoolean(HomeActivity.THEME_KEY, mThemeDark);
		final int mAccentColor = WPTheme.getAccentColor();
		final String mAccentHex = String.format("%06X", (0xFFFFFF & mAccentColor));
		
		Log.v(TAG, "Accent color: #" + mAccentHex +
			", theme: " + Boolean.toString(mThemeDark) + ".");
	
		final String mAccentStr = "<font color='#" + mAccentHex + "'>" +
			mRes.getString(R.string.accentcolor).toLowerCase() + "</font>";
		
		final TextView mThemeDescView = (TextView) findViewById(R.id.text_theme_description);
		mThemeDescView.setText(Html.fromHtml(String.format(
			mRes.getString(R.string.theme_description), mAccentStr)));
			
		// Battery warning display?
		if (mBatteryWarning == null) mBatteryWarning = findViewById(R.id.light_battery_warning);
		if (!mThemeDark) mBatteryWarning.setVisibility(View.VISIBLE);
			
		// Update the small accent color indicator.
		if (mAccentView == null) mAccentView = findViewById(R.id.view_accent_selected);
		mAccentView.setBackgroundColor(mAccentColor);
		
		// Set our accent color.
		if (mAccentText == null) mAccentText = (TextView) findViewById(R.id.text_accent_selected);
		mAccentText.setText(WPTheme.getAccentColorName());
		
		// Set the selected theme.
		if (mThemeSelected == null) mThemeSelected = (TextView) findViewById(R.id.text_theme_selected);
		if (mThemeDarkView == null) mThemeDarkView = (TextView) findViewById(R.id.text_theme_dark);
		if (mThemeLight == null) mThemeLight = (TextView) findViewById(R.id.text_theme_light);
		
		// Add click listener for accent activity.
		final View mAccentLayout = findViewById(R.id.linearlayout_theme_accent);
		mAccentLayout.setOnClickListener(new HomeActivity.LaunchClickListener(
			ThemeActivity.class, AdvancedActivity.this));
		
		setThemeText();

		// Get the containers.		
		if (mThemeContainer == null)
			mThemeContainer = findViewById(R.id.linearlayout_theme_background);
		if (mThemeContainerSelected == null)
			mThemeContainerSelected = findViewById(R.id.linearlayout_theme_background_selected);
		
		// Toggle selected visibility.
		mThemeSelected.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View mView)
			{	
				mThemeContainer.setVisibility(View.GONE);
				mThemeContainerSelected.setVisibility(View.VISIBLE);
			}
		});
		
		mRoot.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View mView, MotionEvent event)
			{
				if (event.getAction() != MotionEvent.ACTION_DOWN) return false;
								
				// Check if the accent color is open.
				if (mThemeContainer.getVisibility() != View.VISIBLE)
				{
					mThemeContainer.setVisibility(View.VISIBLE);
					mThemeContainerSelected.setVisibility(View.GONE);
				}
				
				return false;
			}
		});
		
		// Toggle selected visibility.
		mThemeDarkView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View mView)
			{
				mThemeContainer.setVisibility(View.VISIBLE);
				mThemeContainerSelected.setVisibility(View.GONE);
			}
		});
		mThemeLight.setOnClickListener(mThemeListener);
	}
	
	// Update the text of the theme preferences.
	private final void setThemeText()
	{
		mThemeDark = mPrefs.getBoolean(HomeActivity.THEME_KEY, mThemeDark);
	
		// If we are light, swap everything!
		if (mThemeDark) {
			mThemeSelected.setText(R.string.dark);
			mThemeDarkView.setText(R.string.dark);
			mThemeLight.setText(R.string.light);
			mBatteryWarning.setVisibility(View.GONE);
		} else {
			mThemeSelected.setText(R.string.light);
			mThemeDarkView.setText(R.string.light);
			mThemeLight.setText(R.string.dark);
			mBatteryWarning.setVisibility(View.VISIBLE);
		}
		
		// Make the selected one the accent color.
		mThemeDarkView.setTextColor(WPTheme.getAccentColor());
	}
	
	// Listener for when a theme is clicked.
	private final View.OnClickListener mThemeListener =
		new View.OnClickListener()
	{
		@Override
		public void onClick(View mView)
		{
			mThemeDark = !mPrefs.getBoolean(HomeActivity.THEME_KEY, mThemeDark);
			
			// Toggle the theme dark/ light and reset stuff.
			final Editor mEditor = mPrefs.edit();
			mEditor.putBoolean(HomeActivity.THEME_KEY, mThemeDark);
			mEditor.commit();
			WPTheme.setThemeDarkUnsynchronized(mThemeDark);
			
			setupThemeChooser();
			updateThemeColor();
			
			setThemeText();
			
			// Toggle the visibility.
			mThemeContainer.setVisibility(View.VISIBLE);
			mThemeContainerSelected.setVisibility(View.GONE);
			
			// Update buttons.
			updateButtons(mRoot);
		}
	};
	
	/**
     * Called when this activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        ResponseHandler.register(mBarPurchaseObserver);
        
        // Get a few values from settings.
		final SharedPreferences mPrefs = PreferenceManager
			.getDefaultSharedPreferences(getApplicationContext());
		mStatusBar = mPrefs.getBoolean(HomeActivity.STATUS_KEY, mStatusBar);
		mShouldKeepWake = mPrefs.getBoolean(HomeActivity.WAKE_KEY, mShouldKeepWake);
		mShouldAnimate = mPrefs.getBoolean(HomeActivity.ANIMATE_KEY, mShouldAnimate);
		mThemeDark = mPrefs.getBoolean(HomeActivity.THEME_KEY, mThemeDark);
		
		updateWindowFlags();
        
        setupThemeChooser();
        updateThemeColor();
    }
    
    /**
     * Called when this activity is no longer visible.
     */
    @Override
    protected void onStop() {
        super.onStop();
        ResponseHandler.unregister(mBarPurchaseObserver);
    }
    
    @Override
    protected void onDestroy() {
        mBillingService.unbind();
        super.onDestroy();
    }
    
    private final View.OnClickListener mBillClick = new View.OnClickListener() {
		/**
		 * Called when a button is pressed.
		 */
		public void onClick(View v) {
			if (v.getId() == R.id.donate) {
				if (Consts.DEBUG) {
					Log.d(TAG, "buying: " + mItemName + " sku: " + mSku);
				}
				
				if (mBillingService == null) return;
				
				if (!mBillingService.requestPurchase(mSku, getPayload())) {
					showDialogSafely(DIALOG_BILLING_NOT_SUPPORTED_ID);
				}
			}
		}
	};
	
	private boolean shouldAnimate = true,
    				animating = false,
    				finishing = false;
    
    /**
	 * When overriding this method make sure
	 * to call the superclass's method. This
	 * removes custom Activity animations if
	 * memory is low.
	 */
	@Override
	public void onLowMemory()
	{
		shouldAnimate = false;
		super.onLowMemory();
	}
	
	/**
	 * Finish right now! No animation.
	 */	
	@Override
	public void finish()
	{
		// Don't do anything if an
		// Animation is in progress.
		if (animating || isFinishing()) return;
		
		/*if (mNeedsRestart) {
			showDialogSafely(DIALOG_RESTART);
			return;
		}*/
		
		finishing = true;
		
		// Fly in if we are returning from a finished Activity.
		final int mDuration = getResources().getInteger(R.integer.flyout_duration);
		
		// Don't bother if we no custom
		// animations have been set.
		if (!shouldAnimate || mPrefs == null || !mShouldAnimate || mDuration <= 0)
		{
			super.finishNow();
			return;
		}
		
		// For Honeycomb and above, animate!
		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
			mPivot.setRotationY(0.0f);
			mPivot.setPivotY(0);
		}
		else
		{
			// Prevent the horrible!
			try {
				// Use the wrapped API functionality.
				final AnimatorProxy mProxy = AnimatorProxy.wrap(mPivot);
				if (mProxy != null)
				{
					mProxy.setPivotY(0);
					mProxy.setRotationY(0.0f);
				}
			} catch (Throwable t) { }
		}

		// Create and run our animation!
		final ObjectAnimator mAnim = ObjectAnimator.ofFloat(mPivot, "rotationY", 0.0f, -90.0f);
		
		// Create and run our animation!
		final ObjectAnimator mAnimFade = ObjectAnimator.ofFloat(
			findViewById(R.id.fauxActionBar), "alpha", 1.0f, 0.0f);
		final ObjectAnimator mAnimFade3 = ObjectAnimator.ofFloat
			(findViewById(R.id.divider), "alpha", 1.0f, 0.0f);
		final ObjectAnimator mAnimFade2 = ObjectAnimator.ofFloat(mPivot, "alpha", 1.0f, 0.0f);
		
		// Attach our local listener.
		mAnim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator anim) {
				animating = false;
				mPivot.setVisibility(View.GONE);
				AdvancedActivity.this.finishNow();
			}
		});
		
		// Actually run the animations.
		final AnimatorSet mSet = new AnimatorSet();
		mSet.setDuration(mDuration);
		mSet.setInterpolator(new AccelerateInterpolator());
		mSet.playTogether(mAnimFade3, mAnimFade2, mAnimFade, mAnim);
		mSet.start();
		animating = true;
	}
	
	private final void updateWindowFlags()
    {		
		final View mStatusBarView = findViewById(R.id.statusbarview);
		if (mStatusBarView != null) mStatusBarView.setVisibility(
			((mStatusBar) ? View.VISIBLE : View.GONE));

		// Load the status bar if set to do so.
		if (mStatusBar) {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			makeFullscreen();
			
			// Inflate the status bar if possible.
			final View mStatusView = findViewById(R.id.statusbarstub);
			if (mStatusView != null && mStatusView instanceof ViewStub)
				((ViewStub) mStatusView).inflate();
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    		 	 WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}
    }
	
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
    	if (mIntent != null) {
			mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			super.finishNow();
			startActivity(mIntent);
		}
    }
	
	/**
	 * Finish right now! No animation.
	 */
	@Override
	public void finishNow()
	{
		super.finishNow();
		
		// Disable default Activity animations.
		overridePendingTransition(0, 0);
	}
	
	@Override
	public void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		
		// Fly in if we are returning from a finished Activity.
		final int mDuration = getResources().getInteger(R.integer.flyout_duration);
		
		// Check to make sure we should run our animations.
		if (mPrefs == null || !mShouldAnimate || mDuration <= 0) return;
		
		// Create and run our animation!
		final ObjectAnimator mAnim = ObjectAnimator.ofFloat(mPivot, "rotationY", 90.0f, 0.0f);
		
		// Create and run our animation!
		final ObjectAnimator mAnimFade = ObjectAnimator.ofFloat(mPivot, "alpha", 0.0f, 1.0f);
		
		// Actually run the animations.
		final AnimatorSet mSet = new AnimatorSet();
		mSet.setDuration(mDuration);
		mSet.setInterpolator(new DecelerateInterpolator());
		mSet.playTogether(mAnimFade, mAnim);
		mSet.start();
    }
    
    // Create dialog boxes!
    @Override
	protected Dialog onCreateDialog(int id)
    {
    	final WPDialog mDialog = new WPDialog(this);
    	
    	switch(id)
    	{
    		case DIALOG_CHANGELOG:
			{
				// Get the dialog for the change log.
				final Changelog mChangeLog = new Changelog(this);
				return mChangeLog.getLogDialog();
			}
			case DIALOG_ANONYMOUS_REPORTING:
			{
				mDialog.setTitle(R.string.reporting);
				mDialog.setFullScreen(true);
				final ScrollView mContainer = new ScrollView(getApplicationContext());
				mContainer.setFillViewport(true);
				mContainer.setBackgroundColor(Color.TRANSPARENT);
				final WPTextView mTV = new WPTextView(getApplicationContext());
				mTV.setText(R.string.reporting_description);
				mTV.setTextColor(Color.WHITE);
				mTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18); // 18dp
				mContainer.addView(mTV);
				mDialog.setMessageView(mContainer);
				mDialog.setPositiveButton(R.string.changelog_ok, mDismissListener);
				break;
			}
			case DIALOG_RESTART:
			{
				mDialog.setTitle(R.string.restart);
				final ScrollView mContainer = new ScrollView(getApplicationContext());
				mContainer.setFillViewport(true);
				mContainer.setBackgroundColor(Color.TRANSPARENT);
				final WPTextView mTV = new WPTextView(getApplicationContext());
				mTV.setText(R.string.restart_description);
				mTV.setTextColor(Color.WHITE);
				mTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18); // 18dp
				mContainer.addView(mTV);
				mDialog.setMessageView(mContainer);
				mDialog.setPositiveButton(R.string.restart, mRestartListener);
				mDialog.setNegativeButton(R.string.cancel, mDismissListener);
				break;
			}
			case DIALOG_CANNOT_CONNECT_ID:
			{
				mDialog.setTitle(getString(R.string.cannot_connect_title));
				mDialog.setPositiveButton(getString(R.string.dismiss), mDismissListener)
					   .setMessageText(getString(R.string.cannot_connect_message));
				break;
			}
			case DIALOG_BILLING_NOT_SUPPORTED_ID:
			{
				mDialog.setTitle(getString(R.string.billing_not_supported_title));
				mDialog.setPositiveButton(getString(R.string.thanks), mDismissListener)
					   .setMessageText(getString(R.string.billing_not_supported_message));
				break;
			}
			case DIALOG_DONATION_PURCHASED:
			{
				mDialog.setTitle(getString(R.string.donation_complete_title));
				mDialog.setPositiveButton(getString(R.string.thanks), mDismissListener)
					   .setMessageText(getString(R.string.donation_complete));
				break;
			}
			case DIALOG_USER_CANCELLED:
			{
				mDialog.setTitle(getString(R.string.donation_cancelled_title));
				mDialog.setPositiveButton(getString(R.string.thanks), mDismissListener)
					   .setMessageText(getString(R.string.donation_cancelled));
				break;
			}
		}

		return mDialog;
	}
    
    /**
     * @return The screen width.
     */
    public final int getScreenWidth()
    {
    	final Display mDisplay = getWindowManager().getDefaultDisplay();
    	
    	// Use Honeycomb method if available.
    	if (android.os.Build.VERSION.SDK_INT >= 13)
    	{
			final Point mSize = new Point();
			mDisplay.getSize(mSize);
			return mSize.x;
		}
		
		return mDisplay.getWidth();
    }
        
    @Override
    public void onWindowFocusChanged(boolean focus) {
    	if (focus) {
    		setupThemeChooser();
       		updateThemeColor();
    	}
    	
    	super.onWindowFocusChanged(focus);
    }
    
	// Dismiss dialog click listener.
	private DialogInterface.OnClickListener mDismissListener =
		new DialogInterface.OnClickListener()
	{	
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			dialog.dismiss();
		}
	};
	
	private DialogInterface.OnClickListener mRestartListener =
		new DialogInterface.OnClickListener()
	{	
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			restart();
		}
	};

	private final OnCheckedChangeListener mReportingListener =
		new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(
			CompoundButton buttonView, boolean isChecked)
		{
			// Get a few values from settings.
			final Editor mEditor = mPrefs.edit();
			mEditor.putBoolean(ACRA.PREF_ENABLE_ACRA, isChecked);
			mEditor.commit();
			
			mNeedsRestart = true;
		}
	};
	
	private final OnCheckedChangeListener mAppAnimListener =
		new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(
			CompoundButton buttonView, boolean isChecked)
		{
			mShouldAnimate = isChecked;
			
			// Get a few values from settings.
			final Editor mEditor = mPrefs.edit();
			mEditor.putBoolean(HomeActivity.ANIMATE_KEY, isChecked);
			mEditor.commit();
			
			mNeedsRestart = true;
		}
	};
	
	private final OnCheckedChangeListener mWakeListener =
		new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(
			CompoundButton buttonView, boolean isChecked)
		{
			mShouldKeepWake = isChecked;		
			
			// Get a few values from settings.
			final Editor mEditor = mPrefs.edit();
			mEditor.putBoolean(HomeActivity.WAKE_KEY, isChecked);
			mEditor.commit();
			
			mNeedsRestart = true;		
		}
	};
	
	private final OnCheckedChangeListener mBarListener =
		new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(
			CompoundButton buttonView, boolean isChecked)
		{
			mStatusBar = isChecked;
			
			// Get a few values from settings.
			final Editor mEditor = mPrefs.edit();
			mEditor.putBoolean(HomeActivity.STATUS_KEY, isChecked);
			mEditor.commit();
			
			updateWindowFlags();
			
			mNeedsRestart = true;	
		}
	};
	
	private final OnCheckedChangeListener mVibrateListener =
		new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(
			CompoundButton buttonView, boolean isChecked)
		{
			mShouldVibrate = isChecked;
			
			// Get a few values from settings.
			final Editor mEditor = mPrefs.edit();
			mEditor.putBoolean(HomeActivity.VIBRATE_KEY, isChecked);
			mEditor.commit();
		}
	};
	
	/**
	 * Show a dialog safely calling {@link showDialog}.
	 */
	public final void showDialogSafely(int mDialog)
	{
		if (!isFinishing()) {
			showDialog(mDialog);
		}
	}
}
