/**
 * @brief x lib is the library which includes the commonly used functions in 3 Sided Cube Android applications
 *
 * @author Callum Taylor
 **/
package com.cube.storm.content.lib;

import android.util.Log;

import java.util.Collection;

/**
 * @brief This static class is for debugging
 * @todo Add debug outputs for more data types
 */
public class D
{
	private final static String LOG_TAG = "DEBUG";
	private static boolean DEBUG = true;

	/**
	 * Sets if the app is in debug mode.
	 *
	 * @param inDebug
	 *            If set to true then outputs will be made, else they wont
	 */
	public static void setDebugMode(boolean inDebug)
	{
		DEBUG = inDebug;
	}

	private static String getCallingMethodInfo()
	{
		Throwable fakeException = new Throwable();
		StackTraceElement[] stackTrace = fakeException.getStackTrace();

		if (stackTrace != null && stackTrace.length >= 2)
		{
			StackTraceElement s = stackTrace[2];
			if (s != null)
			{
				return s.getFileName() + "(" + s.getMethodName() + ":" + s.getLineNumber() + "):";
			}
		}

		return null;
	}

	private static void longInfo(String str)
	{
		if (str == null) str = "[null]";

		if (str.length() > 4000)
		{
			Log.e(LOG_TAG, str.substring(0, 4000));
			longInfo(str.substring(4000));
		}
		else
		{
			Log.e(LOG_TAG, str);
		}
	}

	public static void out(Collection args)
	{
		if (!DEBUG) return;

		String output = "";

		if (args != null)
		{
			for (Object o : args)
			{
				if (o == null)
				{
					output += "[null]";
					continue;
				}
				output += o.toString() + ", ";
			}
		}

		longInfo(getCallingMethodInfo() + " " + output);
	}

	public static void out(Object... args)
	{
		if (!DEBUG) return;

		String output = "";

		if (args != null)
		{
			for (Object o : args)
			{
				if (o == null)
				{
					output += "[null]";
					continue;
				}
				output += o.toString() + ", ";
			}
		}

		longInfo(getCallingMethodInfo() + " " + output);
	}

	public static void out(String str, Object... args)
	{
		if (!DEBUG) return;

		longInfo(getCallingMethodInfo() + " " + String.format(str, args));
	}

	public static void out(Object obj)
	{
		if (!DEBUG) return;

		if (obj == null)
		{
			obj = "[null]";
		}

		longInfo(getCallingMethodInfo() + " " + obj.toString());
	}

	public static void out(Exception e)
	{
		if (!DEBUG) return;

		e.printStackTrace();
	}
}
