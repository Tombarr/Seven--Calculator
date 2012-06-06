package com.tombarrasso.android.wp7calculator;

/*
 * History.java
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
import com.tombarrasso.android.wp7calculator.R;

// Java Packages
import java.util.ArrayList;
import java.io.IOException;

// Android Packages
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.graphics.Color;

// App Packages
import com.tombarrasso.android.wp7ui.WPTheme;

/**
 * Maintain a history of all entries, being composed
 * of equations and results. It has a limit and is
 * synchronized by storing data on the file system.
 * History manages {@link HistoryAdapter} via {@link getAdapter},
 * whereby the data set will be notified of changes.
 *
 * @author		Thomas James Barrasso
 * @since		06-05-2012
 * @version		1.1
 * @category	Persistance
 */

public final class History
{
	private static final String ENTRY_SEPARATOR = "\n",
				    PART_SEPARATOR = ",",
				    TAG = History.class.getSimpleName(),
				    EXTENSION = "data",
				    FILENAME = TAG + "." + EXTENSION;
	public static final int LIMIT = 100;

	public static interface OnHistoryChangeListener
	{
		public void onHistoryChange();
	}

	public History(Context mContext)
	{
		this.mContext = mContext;
		this.mHistory = readFromFile();
	}

	public static final class HistoryEntry
	{
		public String mEquation, mResult;

		// Trim the strings to consistant display.
		public HistoryEntry(String mEquation, String mResult)
		{
			this.mEquation = (mEquation != null) ? mEquation.trim() : mEquation;
			this.mResult = (mResult != null) ? mResult.trim() : mResult;
		}

		public HistoryEntry(String mEquation, double mResult)
		{
			this(mEquation, Double.toString(mResult));
		}
	}
	
	private static OnHistoryChangeListener mChangeListener;
	private HistoryAdapter mAdapter;
	private ArrayList<HistoryEntry> mHistory;
	private Context mContext;

        public void setHistoryList(ArrayList<HistoryEntry> mHistory)
	{
		this.mHistory = mHistory;
	}

	public ArrayList<HistoryEntry> getHistoryList()
	{
		return this.mHistory;
	}

	public History(Context mContext, ArrayList<HistoryEntry> mHistory)
	{
		this.mContext = mContext;
		this.mHistory = mHistory;
	}

	/**
	 * Set the listener to be notified when the History has changed.
	 */
	public static void setOnHistoryChangeListener(OnHistoryChangeListener mListener)
	{
		mChangeListener = mListener;
	}
	
	public void addEntry(HistoryEntry mEntry)
	{
		// Pop from the beginning to conform to a limit.
		if (mHistory.size() >= LIMIT) mHistory.remove(0);
		mHistory.add(mEntry);
		if (mAdapter != null) mAdapter.notifyDataSetChanged();
		if (mChangeListener != null) mChangeListener.onHistoryChange();
	}

	public void removeEntry(int index)
	{
		mHistory.remove(index);
		if (mAdapter != null) mAdapter.notifyDataSetChanged();
		if (mChangeListener != null) mChangeListener.onHistoryChange();
	}

	public void removeEntry(HistoryEntry mEntry)
	{
		mHistory.remove(mEntry);
		if (mAdapter != null) mAdapter.notifyDataSetChanged();
		if (mChangeListener != null) mChangeListener.onHistoryChange();
	}

	public int size()
	{
		return mHistory.size();
	}

	public void clear()
	{
		mHistory.clear();
		if (mAdapter != null) mAdapter.notifyDataSetChanged();
		if (mChangeListener != null) mChangeListener.onHistoryChange();
	}

	public void writeToFile()
	{
		String mHistoryString = "";
		for (int i = 0, e = mHistory.size(); i < e; ++i)
			mHistoryString += mHistory.get(i).mEquation + PART_SEPARATOR
					+ mHistory.get(i).mResult + ENTRY_SEPARATOR;
		Memory.storeInInternalMemory(FILENAME, mHistoryString, mContext);
	}

	public ArrayList<HistoryEntry> readFromFile()
	{
		final ArrayList<HistoryEntry> mHistory = new ArrayList<HistoryEntry>();
		String mHistoryString = "";
		try
		{
			mHistoryString = Memory.getStringFromInternalMemory(FILENAME, mContext);
		}
		catch (IOException e)
		{
			return mHistory;
		}
		
		if (mHistoryString != null && !mHistoryString.equals(""))
		{
			// STOP: Log.i(TAG, mHistoryString);
			final String[] mEntries = mHistoryString.split(ENTRY_SEPARATOR);
			if (mEntries == null) return mHistory;
			for (int i = 0, e = mEntries.length; i < e; ++i)
			{
				final String[] mParts = mEntries[i].split(PART_SEPARATOR);
				if (mParts != null && mParts.length == 2)
					mHistory.add(new HistoryEntry(mParts[0], mParts[1]));
			}
		}

		return mHistory;
	}

	/**
	 * Get a {@link HistoryAdapter} for current history.
	 */
	public HistoryAdapter getAdapter(int mResId)
	{
		if (mAdapter == null) mAdapter = new HistoryAdapter(mContext, mResId, this.mHistory);
		return mAdapter;
	}

	/**
	 * Adapter for a ListView that displays History.
	 */
	public static final class HistoryAdapter extends ArrayAdapter<HistoryEntry>
	{
		private int mResId;
		private final LayoutInflater mInflater;

		public static final class HistoryHolder
		{
			TextView mEquation;
			TextView mResult;
		}

		public HistoryAdapter(Context mContext, int mResId, ArrayList<HistoryEntry> mHistory)
		{
			super(mContext, mResId, mHistory);
			this.mResId = mResId;
			mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			final boolean mConvertNull = (convertView == null);
			final ViewGroup mView = (ViewGroup) ((mConvertNull) ? mInflater.inflate(mResId, null) : convertView);
						
			final HistoryEntry mEntry = getItem(position);
			final HistoryHolder mHolder;

			if (mConvertNull)
			{
				mHolder = new HistoryHolder();
				mHolder.mEquation = (TextView) mView.findViewById(R.id.equation);
				mHolder.mResult = (TextView) mView.findViewById(R.id.result);
				
				// Update text color.
				final int mTextColor = ((WPTheme.isThemeDark()) ? Color.WHITE : Color.BLACK);
				mHolder.mEquation.setTextColor(mTextColor);
				mHolder.mResult.setTextColor(mTextColor);
				
				mView.setTag(mHolder);
			}
			else
			{
				mHolder = (HistoryHolder) mView.getTag();
			}

			// Update text content.
			mHolder.mEquation.setText(mEntry.mEquation);
			mHolder.mResult.setText(mEntry.mResult);

			return mView;
		}
	}
}
