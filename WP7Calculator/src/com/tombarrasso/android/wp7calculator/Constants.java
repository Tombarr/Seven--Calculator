package com.tombarrasso.android.wp7calculator;

/*
 * Constants.java
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

/**
 * This class is meant to contain constant variables, set statically
 * once for performance considerations and easy access. Getters/ setters
 * are unnecessary because these values cannot be modified.
 * 
 * @author		Thomas James Barrasso <contact @ tombarrasso.com>
 * @since		07-19-2011
 * @version		1.0
 * @category	Helper
 */

public final class Constants
{
	public static final String XMLNS = "http://schema.tombarrasso.com/wp7calculator";

	public static final class Tags
	{
		public static final int CLEAR = 1,
					MEMCLEAR = 2,
					MEMRECALL = 3,
					MEMPLUS = 4,
					DELETE = 5,
					PLUSMINUS = 6,
					PERCENT = 7,
					DIVIDE = 8,
					SEVEN = 9, 
					EIGHT = 10,
					NINE = 11,
					MULTIPLY = 12,
					FOUR = 13,
					FIVE = 14,
					SIX = 15,
					MINUS = 16,
					ONE = 17,
					TWO = 18,
					THREE = 19,
					PLUS = 20,
					ZERO = 21,
					PERIOD = 22,
					EQUALS = 23, 
					PAREN_OPEN = 24,
					PAREN_CLOSE = 25,
					PI = 26,
					DEGREE = 27,
					RADIAN = 28,
					GRADIAN = 29,
					SQRT = 30,
					SIN = 31, 
					COS = 32,
					TAN = 33,
					NATURAL_LOG = 34,
					LOG = 35,
					EXPONENTIAL_TEN = 36,
					FACTORIAL = 37,
					SQUARED = 38,
					EXPONENTIAL = 39;
	}
	
	public static final PrintfFormat FUN_SIN_RAD = new PrintfFormat("sin(%s)"),
					 FUN_COS_RAD = new PrintfFormat("cos(%s)"),
					 FUN_TAN_RAD = new PrintfFormat("tan(%s)"),
					 FUN_SIN_DEG = new PrintfFormat("sind(%s)"),
					 FUN_COS_DEG = new PrintfFormat("cosd(%s)"),
					 FUN_TAN_DEG = new PrintfFormat("tand(%s)"),
					 FUN_SIN_GRAD = new PrintfFormat("sing(%s)"),
					 FUN_COS_GRAD = new PrintfFormat("cosg(%s)"),
					 FUN_TAN_GRAD = new PrintfFormat("tang(%s)"),
					 FUN_LOG = new PrintfFormat("log(%s)"),
					 FUN_LN = new PrintfFormat("ln(%s)"),
					 FUN_SQRT = new PrintfFormat("sqrt(%s)"),
					 FUN_SQUARED = new PrintfFormat("(%s)^2"),
					 FUN_EXPONENTIAL_TEN = new PrintfFormat("10^(%s)"),
					 FUN_FACTORIAL = new PrintfFormat("(%s)!");
	
	public static final int MAX_LEN = 16;
	
	public static final class Type
	{
		public static final int OPERATOR = 1, NUMBER = 2, PERCENT_OP = -3, FUNCTION = 5,
								PAREN = 6, NOTYPE = -1, SOLUTION = 4;
	}
	
	public static final class TrigMode
	{
		public static final int DEGREE = -3, RADIAN = -4, GRADIAN = -5;
	}
	
	public static final class ButtonColors
	{
		public static final int COLOR_ACCENT = 1,
								COLOR_LIGHT = 2,
								COLOR_DARK = 3,
								COLOR_TRIG = 4;
	}
	
	public static final class Chars
	{
		public static final char comma = ',', period = '.', zero = '0';
		 // The various characters necessary
	    // to handle keyboard meta state.
	    public static final char[] EQUALS_CHAR = { '=' },
	    						   STAR_CHAR = { '*' },
	    						   PARENOPEN_CHAR = { '(' },
	    						   PARENCLOSE_CHAR = { ')' },
	    						   FACTORIAL_CHAR = { '!' },
	    						   PERCENT_CHAR = { '%' },
	    						   MINUS_CHAR = { '-' },
	    						   PLUS_CHAR = { '+' },
	    						   NUMBERS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	    public static final int NUMBERS_LEN = NUMBERS.length;
	}
}
