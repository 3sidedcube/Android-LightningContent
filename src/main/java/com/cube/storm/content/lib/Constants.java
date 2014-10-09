package com.cube.storm.content.lib;

import android.content.Context;
import android.util.DisplayMetrics;

import com.cube.storm.content.ModuleSettings;

import lombok.Getter;

public class Constants
{
	// API calls, stubs and endpoints
	public static final String API_CONTENT_URL = ModuleSettings.CONTENT_BASE_URL;
	public static final String API_VERSION = ModuleSettings.CONTENT_VERSION;
	public static final String API_CONTENT_UPDATE = API_VERSION + "apps/%s/update?timestamp=%s&density=%s&environment=%s";
	public static final String API_BUNDLE = API_VERSION + "apps/%s/bundle?density=%s&environment=%s";

	// Storm URI Schemes
	public static final String URI_CACHE = "cache://";
	public static final String URI_NATIVE = "app://";

	// File and folder names
	public static final String FOLDER_PAGES = "pages";
	public static final String FOLDER_CONTENT = "content";
	public static final String FOLDER_LANGUAGES = "languages";
	public static final String FOLDER_DATA = "data";
	public static final String FILE_MANIFEST = "manifest.json";
	public static final String FILE_ENTRY_POINT = "app.json";

	// Extras keys
	public static final String EXTRA_QUIZ = "quiz";
	public static final String EXTRA_QUIZ_STATES = "quiz_states";
	public static final String EXTRA_FILE_NAME = "file_name";
	public static final String EXTRA_START_PAGE = "start_page";
	public static final String EXTRA_SLIDE_MENU_ENABLED = "slide_menu_enabled";
	public static final String EXTRA_TITLE = "title";
	public static final String EXTRA_CLASS_NAME = "class_name";
	public static final String EXTRA_PAGE = "payload";
	public static final String EXTRA_VIDEOS = "videos";

	// Class names for Storm Views
	public static final String CLASS_APP = "App";
	public static final String CLASS_TABBED_COLLECTION = "TabbedPageCollection";
	public static final String CLASS_PAGE_COLLECTION = "PageCollection";
	public static final String CLASS_QUIZ_PAGE = "QuizPage";
	public static final String CLASS_LIST_PAGE = "ListPage";
	public static final String CLASS_GRID_PAGE = "GridPage";
	public static final String CLASS_NATIVE_PAGE = "NativePage";
	public static final String CLASS_CONTENT = "_content";
	public static final String CLASS_QUIZ_FINISH_PAGE = "QuizFinishFragment";
	public static final String CLASS_LOCATOR_FRAGMENT_PAGE= "LocatorFragmentClass";
	public static final String CLASS_LOCATOR_DETAIL_FRAGMENT_CLASS = "LocatorDetailFragmentClass";

	// Key names for preferences
	public static final String PREFS_QUIZ = "quiz_achievements";
	public static final String PREFS_IN_DEVELOPER_MODE = "in_developer_mode";
	public static final String PREFS_TOKEN = "access_token";
	public static final String PREFS_TOKEN_TIMEOUT = "token_timeout";

	// Service actions
	public static final String ACTION_WON_BADGE = "com.cube.storm.quiz.win";
	public static final String ACTION_UPDATE_REFRESH = "com.cube.storm.content.refresh";

	public enum ContentDensity
	{
		x0_75(0.75, "x0.75"),
		x1_00(1, "x1.0"),
		x1_50(1.5, "x1.5"),
		x2_00(2, "x2.0");

		@Getter double maxPixels;
		@Getter String density;

		private ContentDensity(double maxPixels, String densityStr)
		{
			this.maxPixels = maxPixels;
			this.density = densityStr;
		}

		public static ContentDensity getDensityForSize(Context c)
		{
			Dimension d = new Dimension(c);
			int densityName = d.getDensityName();

			if (densityName >= DisplayMetrics.DENSITY_LOW && densityName < DisplayMetrics.DENSITY_MEDIUM)
			{
				return ContentDensity.x0_75;
			}
			else if (densityName >= DisplayMetrics.DENSITY_MEDIUM && densityName < DisplayMetrics.DENSITY_TV)
			{
				return ContentDensity.x1_00;
			}
			else if (densityName >= DisplayMetrics.DENSITY_TV && densityName < DisplayMetrics.DENSITY_XHIGH)
			{
				return ContentDensity.x1_50;
			}
			else if (densityName >= DisplayMetrics.DENSITY_XHIGH)
			{
				return ContentDensity.x2_00;
			}

			return x1_00;
		}
	}
}
