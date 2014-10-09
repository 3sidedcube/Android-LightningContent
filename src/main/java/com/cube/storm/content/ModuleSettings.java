package com.cube.storm.content;

import lombok.Getter;

public class ModuleSettings
{
	public enum Environment
	{
		TEST("test"),
		LIVE("live");

		@Getter private String environmentLabel;
		private Environment(String str)
		{
			this.environmentLabel = str;
		}
	}

	public static final String VERSION = "v2.0";

	public static String APP_ID = "STORM-1-1";

	public static String CONTENT_BASE_URL = "";
	public static String CONTENT_VERSION = "";
	public static String CONTENT_AUTH_TOKEN = "";
	public static Environment CONTENT_ENVIRONMENT = Environment.TEST;
}
