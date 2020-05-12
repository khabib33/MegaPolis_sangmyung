package app.thecity.data;

public class AppConfig {

    // flag for display ads
    public static final boolean ADS_MAIN_BANNER = true;
    public static final boolean ADS_MAIN_INTERSTITIAL = true;
    public static final long DELAY_NEXT_INTERSTITIAL = 60; // in second
    public static final boolean ADS_PLACE_DETAILS_BANNER = true;
    public static final boolean ADS_NEWS_DETAILS_BANNER = true;

    // if you not use ads you can set this to false
    public static final boolean ENABLE_GDPR = true;

    // this flag if you want to hide menu news info
    public static final boolean ENABLE_NEWS_INFO = true;

    // flag for save image offline
    public static final boolean IMAGE_CACHE = true;

    // if you place data more than 200 items please set TRUE
    public static final boolean LAZY_LOAD = false;

    // flag for tracking analytics
    public static final boolean ENABLE_ANALYTICS = true;

    // clear image cache when receive push notifications
    public static final boolean REFRESH_IMG_NOTIF = true;


    // when user enable gps, places will sort by distance
    public static final boolean SORT_BY_DISTANCE = true;

    // distance metric, fill with KILOMETER or MILE only
    public static final String DISTANCE_METRIC_CODE = "KILOMETER";

    // related to UI display string
    public static final String DISTANCE_METRIC_STR = "Km";

    // flag for enable disable theme color chooser, in Setting
    public static final boolean THEME_COLOR = true;

}
