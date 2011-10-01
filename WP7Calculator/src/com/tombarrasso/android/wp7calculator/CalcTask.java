package com.tombarrasso.android.wp7calculator;

/*
 * CalcTask.java
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

// Android Packages
import android.os.AsyncTask;
import android.util.Log;

// Arity Packages
import org.javia.arity.Symbols;
import org.javia.arity.SyntaxException;

/**
 * Handles arithmetic string evaluations on
 * a separate thread and notifies a listener
 * when a computation has finished or errored.
 * 
 * @author		Thomas Barrasso
 * @version		1.0
 * @since		7-12-2011
 * @category	Task
 */

public class CalcTask extends AsyncTask<String, Void, Double>
{
	public static final String TAG = CalcTask.class.getSimpleName();
	
	public static interface OnCalculationListener
	{
		public void onCalculate(double result);
		public void onError();
	}
	private static OnCalculationListener mCalcListener;
	
	/**
	 * Setter for the listener to be notified
	 * when a calculation is complete.
	 */
	public static final void setCalculationListener(OnCalculationListener calcListener)
	{
		mCalcListener = calcListener;
	}
	
	// Arithmetic evaluation.
	private static final Symbols MathFactory = new Symbols();
	static
	{
		try
		{
			// Define Trigonometric Gradian functions.
			MathFactory.define(MathFactory.compileWithName("sing(x) = sind((9*x)/10)"));
			MathFactory.define(MathFactory.compileWithName("cosg(x) = cosd((9*x)/10)"));
			MathFactory.define(MathFactory.compileWithName("tang(x) = tand((9*x)/10)"));
		}
		catch (SyntaxException e)
		{
			Log.w(TAG, "Could not define gradian trig functions.");
			e.printStackTrace();
		}
	};

	/**
	 * Get the Symbols for Math processing.
	 */
	public static Symbols getMathFactory()
	{
		return MathFactory;
	}

	// Evaluate the string equation.
	@Override
	protected Double doInBackground(String... equations)
	{
		try
		{
			return MathFactory.eval(equations[0]);
		}
		catch (SyntaxException e)
		{
			Log.w(TAG, "An error occured while trying to process math statement: (" + equations[0] + ")");
			e.printStackTrace();
		}
		
		return null;
	}
	
	// Notify the listener that a calculation is complete.
	@Override
    protected void onPostExecute(Double result)
	{
		if (result == null) mCalcListener.onError();
		else mCalcListener.onCalculate(result);
	}
}
