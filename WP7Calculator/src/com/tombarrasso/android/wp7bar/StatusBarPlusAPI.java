package com.tombarrasso.android.wp7bar;

/*
 * StatusBarPlusAPI.java
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
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent;
import android.app.Activity;
import android.util.Log;
 
/**
 * This is the public API for StatusBar+ (com.tombarrasso.android.wp7bar).
 * It uses an ordered broadcast to send information and receive status
 * updates back. Applications can only integrate with StatusBar+ while they
 * are in the foreground with an {@link Activity}, and will automatically be
 * released for you when your app exits or is backgrounded.
 *
 * @version 1.0
 * @since	06-05-2012
 * @author	Thomas James Barrasso <contact @ tombarrasso.com>
 */
public final class StatusBarPlusAPI
{
	public static final String TAG = StatusBarPlusAPI.class.getSimpleName();
	
	/**
	 * The version of this API. All API versions are backwards-compatible
	 * and designed such that new features to not interfere with old ones.
	 * Nonetheless it is recommended that you use the latest version.
	 */
	public static final float VERSION = 1.0f;
	
	/**
	 * Sticky {@link Intent} used to notify other applications
	 * when StatusBar+ has been enabled and disabled.
	 */
	public static final String ACTION_ENABLED = "com.tombarrasso.android.wp7bar.intent.action.ENABLED",
							   ACTION_DISABLED = "com.tombarrasso.android.wp7bar.intent.action.DISABLED";

	// Intents for enabled/ disabled actions.
	private static final Intent ENABLED_INTENT = new Intent(ACTION_ENABLED),
								DISABLED_INTENT = new Intent(ACTION_DISABLED);
	
	/**
	 * {@link Intent} action for third-party apps to integrate with StatusBar+.
	 * Use extras below to configure integration.
	 */
	public static final String ACTION_INTEGRATE = "com.tombarrasso.android.wp7bar.intent.INTEGRATE";
	
	/**
	 * {@link Intent} extra with a {@link Boolean} value. Set false
	 * to hide the {@link WPDigitalClock}.
	 */
	public static final String EXTRA_CLOCK_VISIBLE = "clock_visible";
	
	/**
	 * {@link Intent} extra with a {@link Boolean} value. Set true
	 * for StatusBar+ to have a transparent background.
	 */
	public static final String EXTRA_TRANSPARENT = "transparent";
	
	/**
	 * {@link Intent} extra with a {@link Integer} value. Set to
	 * the {@link Color} desired.
	 */
	public static final String EXTRA_BACKGROUND = "background";
	
	/**
	 * {@link Intent} extra with a {@link Float} value. Set to
	 * the API version of the requesting app or StatusBar+ depending
	 * on if this is in response {@link Intent}.
	 */
	public static final String EXTRA_API_VERSION = "api_version";
	
	/**
	 * {@link Intent} extra with a {@link Boolean} value. Set true
	 * to restore all functionality and release any integration with
	 * StatusBar+. Set false does nothing.
	 */
	public static final String EXTRA_RESTORE = "restore";
	
	/**
	 * {@link Intent} extra with a {@link Boolean} value. This is
	 * the light/ dark WP7 theme. True for dark, false for light.<br />
	 * <em>Note</em>: in future releases of StatusBar+ this may
	 * not take effect depending on the theme chosen.
	 */
	public static final String EXTRA_THEME = "theme_dark";
	
	/**
	 * {@link Intent} extra with a {@link Integer} value. Set to
	 * the {@link Color} value desired for status bar icons.<br />
	 * <em>Note</em>: in future releases of StatusBar+ this may
	 * not take effect depending on the theme chosen.
	 */
	public static final String EXTRA_COLOR = "icon_color";
	
	/**
	 * {@link Intent} extra with a {@link Boolean} value. Set true
	 * and StatusBar+ will ignore that your app is full screen and
	 * not automatically hide. Use with {@link EXTRA_TRANSPARENT}
	 * and make your {@link Activity} full screen with a padding equal
	 * to the height of StatusBar+ for an immersive, WP7 experience.
	 */
	public static final String EXTRA_COMMANDO = "commando";
	
	/**
	 * {@link Intent} extra with a {@link String} value. Set to
	 * the desired title and StatusBar+ will display the text in
	 * the top-left until the user taps the status bar. If the
	 * status bar is not in "Tap to drop" mode then this request
	 * will sadly be ignored and {@link ERROR_CODE_TITLE_IGNORED}
	 * will be provided.
	 */
	public static final String EXTRA_TITLE = "title";
	
	/**
	 * {@link Intent} extra with a {@link Integer} value. Corresponds
	 * to one of the many ERROR_CODE values.
	 */
	public static final String EXTRA_ERROR_CODE = "error_code";
	
	public static final String EXTRA_PID = "pid";
	public static final String EXTRA_UID = "uid";
	public static final String EXTRA_PACKAGE = "package_name";
	
	/**
	 * String array of extras that correspond to various API settings.
	 */
	public static final String[] EXTRAS =
	{
		EXTRA_CLOCK_VISIBLE, EXTRA_TRANSPARENT, EXTRA_BACKGROUND,
		EXTRA_RESTORE, EXTRA_THEME, EXTRA_COLOR, EXTRA_COMMANDO, EXTRA_TITLE
	};
	
	/* ====== ERROR CODES ====== */
	
	/**
	 * Horray, no error has occurred!
	 */
	public static final int ERROR_CODE_PEACHY_KEEN = 0;
	
	/**
	 * The app requesting to integrate with StatusBar+ does not hold
	 * the {@link android.Manifest.permission.EXPAND_STATUS_BAR} permission.
	 * You shall not pass!
	 */
	public static final int ERROR_CODE_NO_PERMISSION = 1;
	
	/**
	 * The {@link EXTRA_TITLE} was sadly ignored because
	 * "Tap to drop" was not enabled.
	 */
	public static final int ERROR_CODE_TITLE_IGNORED = 2;
	
	/**
	 * This error occurs when your app is not the foreground
	 * {@link Activity}. <em>Note</em>: you cannot use this
	 * API from a background {@link Service} or other component.
	 */
	public static final int ERROR_CODE_NOT_FOREGROUND = 3;
	
	/**
	 * This error occurs when an option specified during integration
	 * could not be used properly. In future updates to StatusBar+ 
	 * support for third-party themes will make some of these options
	 * no longer functional.
	 */
	public static final int ERROR_CODE_UNSUPPORTED = 4;
	
	/**
	 * This error occurs when no options, or invalid options,
	 * are specified. It should not occur unless you are
	 * trying to manipulate the API in an unsupported way.
	 */
	public static final int ERROR_CODE_BAD_REQUEST = 5;
	
	/**
	 * Listener to be notified when integration
	 * completed and an error code (or {@link ERROR_CODE_PEACHY_KEEN}
	 * is returned.
	 */
	public static interface OnIntegrateResponse
	{
		/**
		 * Notified when integration is complete.
		 *
		 * @param errorCode One of many ERROR_CODE values.
		 */
		public void onIntegrate(int errorCode);
	}
	
	// Response receiver.
	public final BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// Notify the listener if one exists.
			if (mListener != null)
				mListener.onIntegrate(intent.getIntExtra(
					EXTRA_ERROR_CODE, ERROR_CODE_PEACHY_KEEN));
		}
	};
	
	private static StatusBarPlusAPI mInstance;
	
	private final Context mContext;
	private OnIntegrateResponse mListener;
	
	/**
	 * Lazy-loaded singleton constructor. If you access the
	 * API from numerous application components this may
	 * be preferable.
	 */
	public static synchronized final StatusBarPlusAPI getInstance(Context mContext)
	{
		if (mInstance == null)
			mInstance = new StatusBarPlusAPI(mContext);
		
		return mInstance;
	}
	
	/**
	 * Basic API constructor.
	 */
	public StatusBarPlusAPI(Context mContext)
	{
		this.mContext = mContext;
	}
	
	/**
	 * Set the {@link OnIntegrateResponse} to be notified when
	 * integration has occurred. This is not necessary but may
	 * be useful for debugging purposes to understand if requests
	 * are successfully processed or understanding why not.
	 */
	public final void setOnIntegrateResponse(
		OnIntegrateResponse mListener)
	{
		this.mListener = mListener;
	}
	
	/**
	 * @return True is StatusBar+ is enabled and running.
	 * It is fine to make integration calls without checking
	 * this method, they will just not go through.
	 * <em>Note</em>: this is not the most accurate test
	 * because other applications can also broadcast this
	 * action. If your app holds the {@link android.Manifest.permission.GET_TASKS}
	 * permission you can use {@link PackageManager} to check
	 * if the {@link Service} or {@link AccessibilityService}
	 * is running.
	 */
	public final boolean isStatusBarPlusEnabled()
	{
		return (mContext.registerReceiver(
			null, new IntentFilter(ACTION_ENABLED)) != null);
	}
	
	/**
	 * @see {@link goCommando}
	 * 
	 * @param theme True if dark WP7 theme.
	 */
	public final void goCommando(boolean theme)
	{
		final Intent mIntent = new Intent(ACTION_INTEGRATE);
		mIntent.putExtra(EXTRA_COMMANDO, true);
		mIntent.putExtra(EXTRA_TRANSPARENT, true);
		mIntent.putExtra(EXTRA_THEME, theme);
		integrate(mIntent);
	}
	
	/**
	 * Integrate with StatusBar+ making it transparent. It will
	 * also ignore full screen apps, so it will display above
	 * your application. Be sure to accomodate it with padding.
	 */
	public final void goCommando()
	{
		final Intent mIntent = new Intent(ACTION_INTEGRATE);
		mIntent.putExtra(EXTRA_COMMANDO, true);
		mIntent.putExtra(EXTRA_TRANSPARENT, true);
		integrate(mIntent);
	}
	
	/**
	 * Restore StatusBar+ functionality. This removes all
	 * integration and returns StatusBar+ to its original
	 * state. <em>Note</em>: this happens automatically for
	 * you when your app is backgrounded, use only when you
	 * need to specifically release integration.
	 */
	public final void restore()
	{
		final Intent mIntent = new Intent(ACTION_INTEGRATE);
		mIntent.putExtra(EXTRA_RESTORE, true);
		integrate(mIntent);
	}
	
	/**
	 * Integrate with StatusBar+!
	 * Don't forget to call {@link setOnIntegrateResponse} prior
	 * or no result will be returned.
	 */
	public final void integrate(Intent mIntent)
	{
		// Make sure that the action is right.
		if (mIntent.getAction() == null ||
			!mIntent.getAction().equals(ACTION_INTEGRATE)) return;
			
		// Send specifically to StatusBar+
		mIntent.setPackage("com.tombarrasso.android.wp7bar");
		
		// Inform StatusBar+ of the API which originated this request.
		// Does not alter the outcome of the request.
		mIntent.putExtra(EXTRA_API_VERSION, VERSION);
		
		// Don't try anything funny here, it's just not worth it.
		// You'll also need these parameters for the top activity
		// otherwise you'll get {@link ERROR_CODE_NOT_FOREGROUND}
		// so just don't be mean.
		mIntent.putExtra(EXTRA_PID, android.os.Process.myPid());
		mIntent.putExtra(EXTRA_UID, android.os.Process.myUid());
		mIntent.putExtra(EXTRA_PACKAGE, mContext.getPackageName());
		
		// Send the ordered broadcast.
		mContext.sendOrderedBroadcast(mIntent, null,
			mReceiver, null, Activity.RESULT_OK, null, null);
	}
}