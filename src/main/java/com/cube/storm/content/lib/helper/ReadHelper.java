package com.cube.storm.content.lib.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

public class ReadHelper
{
	public static JsonElement readJsonFromFileStream(FileInputStream is)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			byte[] buffer = new byte[8192];
			int len = 0;

			while ((len = is.read(buffer)) > -1)
			{
				bos.write(buffer, 0, len);
			}

			return new JsonParser().parse(new String(bos.toByteArray()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				bos.close();
				bos.flush();
			}
			catch (Exception e){}
		}

		return null;
	}
}
