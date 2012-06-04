package com.tombarrasso.android.wp7calculator;

/*
 * CalcApplication.java
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
import android.app.Application;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

// ACRA Packages
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey = "",
				mailTo = "",
				formUri = "http://www.bugsense.com/api/acra?api_key=07df7e42") 
public final class CalcApplication extends Application
{
	public static final String BUGSENSE_API_KEY = "07df7e42";

	@Override
	public void onCreate() {
	
		// Get a few values from settings.
		final SharedPreferences mPrefs = PreferenceManager
			.getDefaultSharedPreferences(getApplicationContext());
		final boolean canReport = mPrefs.getBoolean(ACRA.PREF_ENABLE_ACRA, true);
		
		// Make sure that the user is OK with crash reporting.
		// ACRA handles this preference live, but this is just
		// to save resources on the user's behalf.
		if (canReport)
		{
			// The following line triggers the initialization of ACRA
			ACRA.init(this);
		}
		
        super.onCreate();
	}
}