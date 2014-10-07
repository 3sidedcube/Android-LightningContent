package com.cube.storm.content.lib.manager;

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
	@Setter @Getter private static String cachePath = "";
	private static CacheManager instance;

	public static CacheManager getInstance()
	{
		if (instance == null)
		{
			synchronized (CacheManager.class)
			{
				if (instance == null)
				{
					instance = new CacheManager();
				}
			}
		}

		return instance;
	}

	public long getFileAge(String fileName)
	{
		return System.currentTimeMillis() - new File(cachePath, fileName).lastModified();
	}

	public boolean fileExists(String fileName)
	{
		return new File(cachePath, fileName).exists();
	}

	public boolean fileExists(String path, String fileName)
	{
		return new File(path, fileName).exists();
	}

	public byte[] readFile(String fileName)
	{
		return readFile(cachePath, fileName);
	}

	public byte[] readFile(String folderPath, String fileName)
	{
		return readFile(new File(folderPath, fileName));
	}

	public String readFileAsString(String fileName)
	{
		return readFileAsString(new File(cachePath, fileName));
	}

	public String readFileAsString(String path, String fileName)
	{
		return readFileAsString(new File(path, fileName));
	}

	public String readFileAsString(File fileName)
	{
		return new String(readFile(fileName));
	}

	public JsonElement readFileAsJson(String fileName)
	{
		return readFileAsJson(new File(cachePath, fileName));
	}

	public JsonElement readFileAsJson(String path, String fileName)
	{
		return readFileAsJson(new File(path, fileName));
	}

	public JsonElement readFileAsJson(File fileName)
	{
		return new JsonParser().parse(readFileAsString(fileName));
	}

	public byte[] readFile(File fileName)
	{
		try
		{
			return readFile(new FileInputStream(fileName));
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public byte[] readFile(InputStream input)
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

		return null;
	}

	public void writeFile(String fileName, byte[] contents)
	{
		writeFile(cachePath, fileName, contents);
	}

	public void writeFile(String fileName, Serializable contents)
	{
		byte[] c = serializeObject(contents);
		writeFile(cachePath, fileName, c);
	}

	public void writeFile(String folderPath, String fileName, Serializable contents)
	{
		byte[] c = serializeObject(contents);
		writeFile(folderPath, fileName, c);
	}

	public void writeFile(String folderPath, String fileName, byte[] contents)
	{
		FileOutputStream fos = null;
		try
		{
			File f = new File(folderPath + "/" + fileName);
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

	public boolean removeFile(String fileName)
	{
		return removeFile(cachePath, fileName);
	}

	public boolean removeFile(String folderPath, String fileName)
	{
		File f = new File(folderPath + "/" + fileName);
		return f.delete();
	}

	/**
	 * Serializes data into bytes
	 *
	 * @param data
	 *            The data to be serailized
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
	 * Gets the files
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
}
