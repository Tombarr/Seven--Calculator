package com.tombarrasso.android.wp7calculator;

/*
 * Memory.java
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

// Java Packages
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

// Android Packages
import android.content.Context;
import android.os.Environment;
import android.util.Log;

// App Packages
import com.tombarrasso.android.wp7ui.WPTheme;

/**
 * Static helper for storing memory on user's SD card.
 * <br /><br />
 * <u>Change log:</u>
 * <b>Version 1.01</b>
 * <ul>
 * 	<li>Using ThemeTask for performance.</li>
 * </ul>
 * <b>Version 1.02</b>
 * <ul>
 * 	<li>Using UTF-8 to encode files.</li>
 * </ul>
 * 
 * @author		Thomas James Barrasso
 * @since		2011
 * @version		1.02
 * @category		Helper
 */

public final class Memory
{
	public static final String TAG = Memory.class.getSimpleName(),
							   PACKAGE = Memory.class.getPackage().getName(),
							   DATA_DIR = File.separator + "Android" + File.separator + "data"
								+ File.separator + PACKAGE + File.separator + "files" + File.separator;
	
	/**
	 * @return The directory for storing data,
	 * or null if none is available.
	 */
	public static final File getDataDir()
	{
		if (WPTheme.isReadable())
		{
			final File dataDir = new File(Environment.getExternalStorageDirectory(), DATA_DIR);
			if (dataDir != null && !dataDir.exists()) dataDir.mkdirs();
			return dataDir;
		}
		
		return null;
	}
	
	/**
	 * @return The location of the color file, or
	 * it is created if it does not already exist.
	 * @throws IOException 
	 */
	public static final File getDataFile(String filename) throws IOException
	{
		final File colorFile = new File(getDataDir(), filename);
		if (colorFile != null && !colorFile.exists()) colorFile.createNewFile();
		return colorFile;
	}

	public static final File getInternalDataFile(String filename, Context mContext) throws IOException
	{
		final File colorFile = new File(mContext.getFilesDir(), filename);
		if (colorFile != null && !colorFile.exists()) colorFile.createNewFile();
		return colorFile;
	}

	public static final boolean storeInInternalMemory(String filename, String str, Context mContext)
	{	
		try
		{
			// Try to store number in file.
			final WPTheme.Operation mOp = new WPTheme.Operation(getInternalDataFile(filename, mContext), str);
			(new WPTheme.ThemeTask()).execute(mOp);
			return true;
		}
		catch (IOException e)
		{
			Log.w(TAG, "Could not store (" + str + ") in \"" + filename + "\"");
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Stores the number in a given file.
	 * 
	 * @param filename The filename.
	 * @param number The number to store.
	 * @return Whether or not the number
	 * was successfully stored.
	 */
	public static final boolean storeInMemory(String filename, int number)
	{
		return storeInMemory(filename, "" + number);
	}
		
	public static final boolean storeInMemory(String filename, String str)
	{
		if (!WPTheme.isWritable()) return false;
		
		// Try to store number in file.
		try
		{
			final WPTheme.Operation mOp = new WPTheme.Operation(getDataFile(filename), str);
			(new WPTheme.ThemeTask()).execute(mOp);
			return true;
		}
		catch (IOException e)
		{
			Log.w(TAG, "Could not store (" + str + ") in \"" + filename + "\"");
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Gets the string stored in memory.
	 * 
	 * @param filename The filename to retrieve.
	 * @return The string stored in filename.
	 * @throws IOException If the file was unavailable.
	 */
	public static final String getStringFromMemory(String filename) throws IOException
	{
		final File mNumFile = getDataFile(filename);
		if (mNumFile == null) throw new IOException("Could not retrieve file \"" + filename + "\"");
			
		String strContent = "";
		final BufferedReader colorIS = new BufferedReader( new InputStreamReader( new FileInputStream(mNumFile), "UTF-8" ) );
 
		String str;
		while ((str = colorIS.readLine()) != null) {
		    strContent += str + "\n";
		}
	 
		colorIS.close();

		return strContent;
	}

	/**
	 * Gets the string stored in internal memory.
	 * 
	 * @param filename The filename to retrieve.
	 * @return The string stored in filename.
	 * @throws IOException If the file was unavailable.
	 */
	public static final String getStringFromInternalMemory(String filename, Context mContext) throws IOException
	{
		final File mNumFile = getInternalDataFile(filename, mContext);
		if (mNumFile == null) throw new IOException("Could not retrieve file \"" + filename + "\"");
			
		String strContent = "";
		final BufferedReader colorIS = new BufferedReader( new InputStreamReader( new FileInputStream(mNumFile), "UTF-8" ) );
 
		String str;
		while ((str = colorIS.readLine()) != null) {
		    strContent += str + "\n";
		}
	 
		colorIS.close();

		return strContent;
	}

	/**
	 * Gets the number stored in memory.
	 * 
	 * @param filename The filename to retrieve.
	 * @return The number stored in filename.
	 * @throws IOException If the file was unavailable.
	 * @throws NumberFormatException If number is not formatted properly.
	 */
	public static final int getFromMemory(String filename) throws IOException, NumberFormatException
	{
		return Integer.parseInt(getStringFromMemory(filename));
	}
}
