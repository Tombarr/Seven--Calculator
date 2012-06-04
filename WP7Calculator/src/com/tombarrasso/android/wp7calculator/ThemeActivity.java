package com.tombarrasso.android.wp7calculator;

/*
 * ThemeActivity.java
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

// UI Packages
import com.tombarrasso.android.wp7calculator.R;
import com.tombarrasso.android.wp7ui.app.WPActivity;
import com.tombarrasso.android.wp7ui.statusbar.StatusBarView;
import com.tombarrasso.android.wp7ui.WPTheme;
import com.tombarrasso.android.wp7ui.widget.WPThemeView;
import com.tombarrasso.android.wp7ui.widget.WPThemeView.OnAccentSelectedListener;

// Build Packages
import com.tombarrasso.android.wp7bar.AndroidUtils;

// Android Packages
import android.view.ViewStub;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.graphics.Color;
import android.view.animation.AccelerateInterpolator;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.os.Process;
import android.app.ActivityManager;

/**
 * Activity for setting this applications theme {@link WPTheme.themeColor}.
 * Don't forget to declare this Activity in your Manifest file!
 *
 * @author      Thomas James Barrasso <contact @ tombarrasso.com>
 * @version     2.0
 * @since       06-02-2012
 * @category	{@link Activity}
 */

public final class ThemeActivity extends WPActivity
	implements OnAccentSelectedListener
{
	public static final String TAG = ThemeActivity.class.getSimpleName();
		
	private WPThemeView mThemeView;
	private boolean mStatusBar, mShouldAnimate, mShouldKeepWake, mThemeDark;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
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
		
		// Localize accent colors.
		WPTheme.setThemeColorNames(getResources().
			getStringArray(R.array.color_names));

		// Get miscellaneous settings.
		final SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mStatusBar = mPrefs.getBoolean(HomeActivity.STATUS_KEY, mStatusBar);
		mShouldAnimate = mPrefs.getBoolean(HomeActivity.ANIMATE_KEY, mShouldAnimate);
		mShouldKeepWake = mPrefs.getBoolean(HomeActivity.WAKE_KEY, mShouldKeepWake);
		mThemeDark = mPrefs.getBoolean(HomeActivity.THEME_KEY, mThemeDark);
		
		// Disable IME for this application
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
        					 WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES);
        
        // Be hardware accelerated if possible.
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		}

		// Set out accent color.
		WPTheme.setAccentColorUnsynchronized(mPrefs.getInt(
			HomeActivity.ACCENT_KEY, WPTheme.getAccentColor()));

		// Load the status bar if set to do so.
		updateWindowFlags();
		
		setContentView(R.layout.theme);
		
		// Since we are caching large views, we want
        // to keep their cache between each animation.
        try {
			((ViewGroup) getWindow().getDecorView()).setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
			((ViewGroup) getWindow().getDecorView()).setDrawingCacheEnabled(true);
		} catch (Throwable t) { /* This shouldn't happen. */ }
		
		updateThemeColor();
		
		// Inflate the status bar if possible.
		final View mStatusView = findViewById(R.id.statusbarstub);
		if (mStatusBar && mStatusView != null && mStatusView instanceof ViewStub)
			((ViewStub) mStatusView).inflate();
        
        // Get ThemeView.
		mThemeView = (WPThemeView) findViewById(R.id.themeview);
		mThemeView.setOnAccentSelectedListener(this);
		mThemeView.setAnimate(mShouldAnimate);
	}
	
	// Update the theme color of this Activity.
	private final void updateThemeColor()
	{
		final SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mThemeDark = mPrefs.getBoolean(HomeActivity.THEME_KEY, mThemeDark);
		
		final View mStatusBar = findViewById(R.id.statusbarview);
		if (mStatusBar != null && (mStatusBar instanceof StatusBarView))
		{
			((StatusBarView) mStatusBar).setAllColors(((mThemeDark) ? Color.WHITE : Color.BLACK));
		}
		
		// Update layout and colors.
		if (mThemeView != null)
		{
			final View mContainer = mThemeView.getRootView();
			if (mContainer == null) return;
			
			if (mThemeDark)
			{
				mContainer.setBackgroundColor(WPTheme.defMenuBackground);
			}
			else
			{
				mContainer.setBackgroundColor(Color.WHITE);
			}
		}
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
    
    @Override
   	protected void onStart()
    {
    	// Get a few values from settings.
		final SharedPreferences mPrefs = PreferenceManager
			.getDefaultSharedPreferences(getApplicationContext());
		if (mPrefs != null) {
			mStatusBar = mPrefs.getBoolean(HomeActivity.STATUS_KEY, mStatusBar);
			mShouldKeepWake = mPrefs.getBoolean(HomeActivity.WAKE_KEY, mShouldKeepWake);
			mShouldAnimate = mPrefs.getBoolean(HomeActivity.ANIMATE_KEY, mShouldAnimate);
			
			updateThemeColor();
			updateWindowFlags();
			
			if (mThemeView != null) mThemeView.setAnimate(mShouldAnimate);
		}
		
    	super.onStart();
    }
	
	// Handle all key presses.
	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (mThemeView == null) return super.dispatchKeyEvent(event);
		
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
					mThemeView.animateClose();
					return true;
				}
			}
		}
		
		return super.dispatchKeyEvent(event);
	}
    
    // Collapse menu if open and back pressed.
    @Override
    public void onBackPressed() {
    	if (mThemeView != null) {
    		mThemeView.animateClose();
    		return;
    	}
    
    	super.onBackPressed();
    }
    
    @Override
	public void finish()
	{
		if (mThemeView != null) {
    		mThemeView.animateClose();
    		return;
    	}
	
		super.finish();	
	}
	
	@Override
	public void finishNow()
	{
		super.finishNow();
	
		// Disable default Activity animations.
		overridePendingTransition(0, 0);	
	}

	// Finish this Activity when a new
	// accent color has been choosen.
	@Override
	public void onAccentSelected(int mColor)
	{
		// This shouldn't happen but if by accident or
		// through reflection the color is set to zero.
		if (mColor != 0)
		{
			// Store in shared preferences.
			final SharedPreferences mPrefs =
				PreferenceManager.getDefaultSharedPreferences(this);
			final Editor mEditor = mPrefs.edit();
			mEditor.putInt(HomeActivity.ACCENT_KEY, mColor);
			mEditor.commit();
			
			WPTheme.setAccentColorUnsynchronized(mColor);
		}
		
		finishNow();
	}
}
