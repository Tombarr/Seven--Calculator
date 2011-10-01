package com.tombarrasso.android.wp7calculator;

/*
 * ThemeActivity.java
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
import com.tombarrasso.android.wp7calculator.R;
import com.tombarrasso.android.wp7ui.app.WPActivity;
import com.tombarrasso.android.wp7ui.widget.WPThemeView;
import com.tombarrasso.android.wp7ui.widget.WPThemeView.OnAccentSelectedListener;

// Android Packages
import android.view.ViewStub;
import android.view.View;
import android.os.Bundle;
import android.view.animation.AccelerateInterpolator;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Activity for setting this applications theme {@link WPTheme.themeColor}.
 * Don't forget to declare this Activity in your Manifest file.
 *
 * @author      Thomas James Barrasso <contact @ tombarrasso.com>
 * @version     1.0
 * @since       2011-07-09
 * @category	Activity
 */

public class ThemeActivity extends WPActivity implements OnAccentSelectedListener
{
	public static final String TAG = ThemeActivity.class.getSimpleName();
	private boolean mStatusBar, mShouldAnimate;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Get miscellaneous settings.
		final SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mStatusBar = mPrefs.getBoolean(HomeActivity.STATUS_KEY, mStatusBar);
		mShouldAnimate = mPrefs.getBoolean(HomeActivity.ANIMATE_KEY, mShouldAnimate);

		// Load the status bar if set to do so.
		if (mStatusBar) makeFullscreen();
		setContentView(R.layout.theme);
		
		// Inflate the status bar if possible.
		final View mStatusView = findViewById(R.id.statusbarstub);
		if (mStatusBar && mStatusView != null && mStatusView instanceof ViewStub)
			((ViewStub) mStatusView).inflate();
        

		final WPThemeView mThemeView = (WPThemeView) findViewById(R.id.themeview);
		mThemeView.setOnAccentSelectedListener(this);
		
		final int mDuration = getResources().getInteger(R.integer.flyout_duration);
		if (mDuration > 0 && mShouldAnimate)
		{
			// Fly in if we are returning from a finished Activity.
			final Flip3DAnimation mAnimIn = new Flip3DAnimation(-90, 0, 0, mThemeView.getHeight() / 2),
								  mAnimOut = new Flip3DAnimation(0, -90, 0, mThemeView.getHeight() / 2);
			mAnimIn.setInterpolator(new AccelerateInterpolator());
			mAnimOut.setInterpolator(new AccelerateInterpolator());
			mAnimIn.setDuration(mDuration);
			mAnimOut.setDuration(mDuration);
			mAnimIn.setFillAfter(true);
			mAnimOut.setFillAfter(true);
			setActivityAnimation(mAnimIn, mAnimOut);
		}
	}

	// Finish this Activity when a new
	// accent color has been choosen.
	@Override
	public void onAccentSelected()
	{
		finish();
	}
}
