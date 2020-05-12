package app.thecity.data;

public class Constant {

    /**
     * -------------------- EDIT THIS WITH YOURS ---------------------------------------------------
     */

    // Edit WEB_URL with your url. Make sure you have backslash('/') in the end url
    public static String WEB_URL = "http://services.dream-space.web.id/the_city/";

    // for map zoom
    public static final double city_lat = -6.9174639;
    public static final double city_lng = 107.6191228;

    /**
     * ------------------- DON'T EDIT THIS ---------------------------------------------------------
     */

    // image file url
    public static String getURLimgPlace(String file_name) {
        return WEB_URL + "uploads/place/" + file_name;
    }
    public static String getURLimgNews(String file_name) {
        return WEB_URL + "uploads/news/" + file_name;
    }

    // this limit value used for give pagination (request and display) to decrease payload
    public static final int LIMIT_PLACE_REQUEST = 40;
    public static final int LIMIT_LOADMORE = 40;

    public static final int LIMIT_NEWS_REQUEST = 40;

    // retry load image notification
    public static int LOAD_IMAGE_NOTIF_RETRY = 3;

    // for search logs Tag
    public static final String LOG_TAG = "CITY_LOG";

    // Google analytics event category
    public enum Event {
        FAVORITES,
        THEME,
        NOTIFICATION,
        REFRESH
    }

}
