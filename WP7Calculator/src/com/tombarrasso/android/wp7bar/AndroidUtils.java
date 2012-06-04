package com.tombarrasso.android.wp7bar;

/*
 * AndroidUtils.java
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
 
// Java Packages
import java.io.File;
import java.io.IOException;
 
// Android Packages
import android.os.IInterface;
import android.os.Build;
import android.os.Binder;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.content.res.Resources;
import android.content.Intent;
import android.util.Log;
import android.os.SystemClock;
import android.content.pm.Signature;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * Simple helper class for obtaing info about this application and
 * the operating system that it is running on.
 *
 * @author		Thomas James Barrasso <contact @ tombarrasso.com>
 * @since		06-03-2012
 * @version		1.1
 * @category	Helper
 */

public final class AndroidUtils
{
	public static final String TAG = AndroidUtils.class.getSimpleName(),
							   PACKAGE = AndroidUtils.class.getPackage().getName();
	
	/**
	 * The signature used for the release of this application.
	 * Used with {@link isRelease}. Dump mSig.toCharsString()
	 * to the log to obtain this value.
	 */
	private static final String SIG_RELEASE = "308203763082025ea00302010202044e681467300d06092a864886f70d0101050500307d310b30090603550406130255533110300e06035504081307556e6b6e6f776e3110300e06035504071307556e6b6e6f776e31173015060355040a130e536576656e2b2050726f6a65637431173015060355040b130e536576656e2b2050726f6a656374311830160603550403130f54686f6d617320426172726173736f301e170d3131303930383031303333355a170d3339303132343031303333355a307d310b30090603550406130255533110300e06035504081307556e6b6e6f776e3110300e06035504071307556e6b6e6f776e31173015060355040a130e536576656e2b2050726f6a65637431173015060355040b130e536576656e2b2050726f6a656374311830160603550403130f54686f6d617320426172726173736f30820122300d06092a864886f70d01010105000382010f003082010a0282010100aba5e9768535017832a78d15b69abd92f75d19de07fddde8b8ab695b6532ed9c2680e54ee4aa6bb9220743aae485f77a5d41ee16d1a8f4a4903ef4c69c9dc422c174ceea761efa49560b488fd560c232e401b513cf54831c5a1e936280458e142dfeced2029d343f970406599a06ded3de081c3f8fa417468de85d5983a2c39177a6ef20ada3f4dc7aed6cbf21d964f67f02ad4436e2baea78c65fa930bc9caa6760a4b5974ed56a2f3c7ccaae729a63b17da4cf4bf585a1c12a19826e25d6c335e5ab4991be1f8711833af8d6e0da1e505553070a9335bbe4d357fa429d87280018c6b021b622e06df285c896d490357d879415beee62b9a80da524195547670203010001300d06092a864886f70d0101050500038201010015242a322630375ff858986cd14949bbd084dc6e7cf845391084d8c567f605dd76a1e1a6714f8d8bcd3d54ff82d81a2245fce7c5b43249fe5239528dbf18d70b12cd292a95c73e8a23e1230ad035aac6a59931f73a3607d20f59c288cd0938fbae93939af45c42057e13ad33dcddcf1ecdedd51a0f07b585d4775fda5e5175c886a6e992e8eeec84b8a6fafa1727e8ebddeee3721964a2028118a653ea6981f2860ec2256e18f9b64b2336211b6a6c1a99855f14dad3ceda83643d38f99fb8ed4b7106b5d4b75bbdfde1858b5eaa4a76a3bba9e77a7d7a740ffe2805f3fe3bb4806361b5fc4701fd3ca5167e11b7db01e3d46fb779c29aac5d3b6c1ca9f97faa";
	
	/**
	 * @return True if this application is being run on
	 * the Android emulator (or a device who reports its
	 * build information as an emulator).
	 */
	public static final boolean isEmulator()
	{
		if (Build.MODEL == null) return false;
		return (Build.MODEL.equals("sdk") || Build.MODEL.equals("google_sdk"));
	}
	
	/**
	 * @return True if this is a release build of this application,
	 * false if otherwise. This is detected by checking the signature
	 * with which this application was signed.
	 */
	public static final boolean isRelease(Context mContext)
	{
		try
		{
			final int mPkgSig = mContext.getPackageManager()
				.checkSignatures(android.os.Process.myUid(),
								 android.os.Process.myUid());
			
			// Check some basic parameters first.
			// They need to both be signed because they are the
			// same app, and they must match.
			if (mPkgSig == PackageManager.SIGNATURE_FIRST_NOT_SIGNED ||
				mPkgSig == PackageManager.SIGNATURE_SECOND_NOT_SIGNED ||
				mPkgSig == PackageManager.SIGNATURE_NEITHER_SIGNED ||
				mPkgSig == PackageManager.SIGNATURE_NO_MATCH)
			{
				Log.d(TAG, "App not signed properly!");
				return false;
			}
		
			final Signature[] mSigs = mContext.getPackageManager()
				.getPackageInfo(mContext.getPackageName(),
				PackageManager.GET_SIGNATURES).signatures;
				
			if (mSigs == null || mSigs.length <= 0) return false;
			
			// Loop through every signature.
			for (final Signature mSig : mSigs)
			{			
				// Let's test our signatures.
				if (SIG_RELEASE.equals(mSig.toCharsString()))
				{
					return true;
				}
			}
			
			return true;
		}
		catch (Throwable t)
		{
			Log.e(TAG, "Could not detect build signature.", t);
		}
		
		return false;
	}
}