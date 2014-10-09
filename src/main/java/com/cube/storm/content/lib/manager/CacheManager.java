package com.cube.storm.content.lib.manager;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.cube.storm.ContentSettings;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import lombok.Getter;
import lombok.Setter;

public class CacheManager
{
	@Setter @Getter private static String cachePath;
	private static CacheManager instance;

	public static CacheManager getInstance(Context context)
	{
		if (instance == null)
		{
			synchronized (CacheManager.class)
			{
				if (instance == null)
				{
					instance = new CacheManager(context);
				}
			}
		}

		return instance;
	}

	public CacheManager(Context context)
	{
		if (ContentSettings.getInstance().isUseExternalCache())
		{
			cachePath = context.getExternalCacheDir().getAbsolutePath();
		}
		else if (!TextUtils.isEmpty(ContentSettings.getInstance().getCachePath()))
		{
			cachePath = ContentSettings.getInstance().getCachePath();
		}
		else
		{
			throw new NullPointerException("No cache path given");
		}
	}

	public long getFileAge(String fileName)
	{
		if (!TextUtils.isEmpty(fileName))
		{
			return System.currentTimeMillis() - new File(cachePath, fileName).lastModified();
		}

		return 0;
	}

	/**
	 * Check if file exists in cache
	 * @param fileName Name of file
	 * @return {@code true} if file exists
	 */
	public boolean fileExists(String fileName)
	{
		return !TextUtils.isEmpty(fileName)
			&& !TextUtils.isEmpty(cachePath)
			&& new File(cachePath, fileName).exists();
	}

	/**
	 * Read file from cache and return as byte array
	 * @param fileName Name of file
	 * @return Byte array of file contents
	 */
	public byte[] readFile(String fileName)
	{
		return readFile(new File(cachePath, fileName));
	}

	/**
	 * Read file from cache and return as String representation of contents
	 * @param fileName Name of file
	 * @return String of file contents
	 */
	public String readFileAsString(String fileName)
	{
		return readFileAsString(new File(cachePath, fileName));
	}

	/**
	 * Read file from cache and return contents as a String
	 * @param fileName File to read
	 * @return String of file contents
	 */
	public String readFileAsString(File fileName)
	{
		if (fileExists(fileName.getName()))
		{
			return new String(readFile(fileName));
		}

		return null;
	}

	/**
	 * Read file and return contents as JSON
	 * @param fileName Name of file
	 * @return JSON element of file contents
	 */
	public JsonElement readFileAsJson(String fileName)
	{
		return readFileAsJson(new File(cachePath, fileName));
	}

	/**
	 * Read file and return contents as JSON
	 * @param fileName File to read
	 * @return JSON element of file contents
	 */
	public JsonElement readFileAsJson(File fileName)
	{
		if (fileExists(fileName.getName()))
		{
			return new JsonParser().parse(readFileAsString(fileName));
		}

		return null;
	}

	/**
	 * Read file and return contents as byte array
	 * @param fileName File to read
	 * @return Byte array of file contents
	 */
	public byte[] readFile(File fileName)
	{
		try
		{
			if (fileExists(fileName.getName()))
			{
				return readFile(new FileInputStream(fileName));
			}
			return null;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Read file from cache and return it as a byte array
	 * @param input InputStream to read from
	 * @return Byte array of the contents of file
	 */
	public byte[] readFile(InputStream input)
	{
		if (isExternalStorageReadable())
		{
			ByteArrayOutputStream bos = null;

			try
			{
				bos = new ByteArrayOutputStream(8192);

				int bufferSize = 1024;
				byte[] buffer = new byte[bufferSize];

				int len = 0;
				while ((len = input.read(buffer)) > 0)
				{
					bos.write(buffer, 0, len);
				}

				return bos.toByteArray();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					input.close();

					if (bos != null)
					{
						bos.close();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	/**
	 * Write file to cache
	 * @param fileName Name of file
	 * @param contents Contents of file
	 */
	public void writeFile(String fileName, Serializable contents)
	{
		byte[] c = serializeObject(contents);
		writeFile(fileName, c);
	}

	/**
	 * Write file to cache
	 * @param fileName Name of file
	 * @param contents Contents of file
	 */
	public void writeFile(String fileName, byte[] contents)
	{
		if (isExternalStorageWritable())
		{
			FileOutputStream fos = null;
			try
			{
				File f = new File(cachePath + "/" + fileName);
				fos = new FileOutputStream(f);
				fos.write(contents);
				fos.flush();
				fos.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			Log.d(CacheManager.class.getSimpleName(), "Cache is not writable. Is it mounted?");
		}
	}

	public boolean removeFile(String fileName)
	{
		if (isExternalStorageWritable())
		{
			File f = new File(cachePath + "/" + fileName);
			return f.delete();
		}

		return false;
	}

	/**
	 * Serializes data into bytes
	 *
	 * @param data The data to be serialized
	 * @return The serialized data in a byte array
	 */
	public static byte[] serializeObject(Object data)
	{
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(data);

			return bos.toByteArray();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets the file's hash
	 * @param filename
	 * @return
	 */
	public String getFileHash(String filename)
	{
		InputStream is = null;
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			is = new FileInputStream(cachePath + "/" + filename);
			is = new DigestInputStream(is, md);

			byte[] contents = readFile(filename);

			StringBuilder signature = new StringBuilder();
			byte[] messageDigest = md.digest(contents);

			for (int i = 0; i < messageDigest.length; i++)
			{
				String hex = Integer.toHexString(0xFF & messageDigest[i]);
				if (hex.length() == 1)
				{
					signature.append('0');
				}

				signature.append(hex);
			}

			return signature.toString();
		}
		catch (Exception ignored){}
		finally
		{
			try
			{
				if (is != null)
				{
					is.close();
				}
			}
			catch (Exception ignored){}
		}

		return "";
	}

	/**
	 * Deserailizes data into an object
	 *
	 * @param data
	 *            The byte array to be deserialized
	 * @return The data as an object
	 */
	public static Object desterializeObject(byte[] data)
	{
		try
		{
			ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(data));
			Object objectData = input.readObject();
			input.close();

			return objectData;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Check the availability of the external storage for writing
	 * @return {@code true} if storage media is available
	 */
	private boolean isExternalStorageWritable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state))
		{
			return true;
		}
		return false;
	}

	/**
	 * Check the availability of the external storage for reading
	 * @return {@code true} if storage media is available
	 */
	private boolean isExternalStorageReadable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
			Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
		{
			return true;
		}
		return false;
	}
}
