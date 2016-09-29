package com.cube.storm.content.lib;

public class Constants
{
	// Storm content endpoints
	public static final String API_CONTENT_UPDATE = "apps/%s/update?timestamp=%s&environment=%s";
	public static final String API_BUNDLE = "apps/%s/bundle?&environment=%s";

	// Storm URI protocols
	public static final String URI_CACHE = "cache";
	public static final String URI_NATIVE = "app";

	// File and folder names
	public static final String FOLDER_PAGES = "pages";
	public static final String FOLDER_CONTENT = "content";
	public static final String FOLDER_LANGUAGES = "languages";
	public static final String FOLDER_DATA = "data";
	public static final String FILE_MANIFEST = "manifest.json";
	public static final String FILE_ENTRY_POINT = "app.json";
}
