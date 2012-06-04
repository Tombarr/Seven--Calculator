package com.tombarrasso.android.wp7calculator;

/*
 * AccentReceiver.java
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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// UI Packages
import com.tombarrasso.android.wp7ui.WPTheme;
import com.tombarrasso.android.wp7ui.widget.WPThemeView;

/**
 * This receiver is notified when the user chooses a new accent
 * color in another application and we want to know about it (
 * and it possible syncrhonize with it).
 * 
 * @author		Thomas James Barrasso <contact @ tombarrasso.com>
 * @since		06-03-2012
 * @version		1.0
 * @category	{@link BroadcastReceiver}
 */

public final class AccentReceiver extends BroadcastReceiver
{
	public static final String TAG = AccentReceiver.class.getSimpleName(),
							   PACKAGE = AccentReceiver.class.getPackage().getName();

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// Get be safe.
		if (intent == null) return;
		final String mAction = intent.getAction();
		if (mAction == null) return;

		// If the accent color has changed, update it.
		if (mAction.equals(WPThemeView.ACTION_ACCENT_CHANGED))
		{
			final int mAccent = intent.getIntExtra(
				WPThemeView.EXTRA_ACCENT_NEW, Integer.MAX_VALUE);
				
			// Check our accent color.
			if (mAccent != Integer.MAX_VALUE)
			{
				final boolean mWithinDefaults = WPTheme.isAccentWithinDefaults(mAccent);
				final String mAccentHex = String.format("%06X", (0xFFFFFF & mAccent));
		
				Log.v(TAG, "Accent color: #" + mAccentHex + ", Default: " + Boolean.toString(mWithinDefaults) + ".");
			
				// Finally, make sure that we all support
				// this particular accent color.
				if (mWithinDefaults)
				{
					// Update the color in our preferences.
					// Store in shared preferences.
					final SharedPreferences mPrefs =
						PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
					final Editor mEditor = mPrefs.edit();
					mEditor.putInt(HomeActivity.ACCENT_KEY, mAccent);
					mEditor.commit();
					
					WPTheme.setAccentColorUnsynchronized(mAccent);
				}
			}
		}
	}
}
