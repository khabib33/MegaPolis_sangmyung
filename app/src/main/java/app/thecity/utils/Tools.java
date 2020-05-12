package app.thecity.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.URI;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import app.thecity.ActivityMain;
import app.thecity.ActivitySplash;
import app.thecity.R;
import app.thecity.data.AppConfig;
import app.thecity.data.SharedPref;
import app.thecity.data.ThisApplication;
import app.thecity.model.DeviceInfo;
import app.thecity.model.NewsInfo;
import app.thecity.model.Place;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class Tools {

    public static boolean needRequestPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public static boolean isLolipopOrHigher() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    public static void systemBarLolipop(Activity act) {
        if (isLolipopOrHigher()) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Tools.colorDarker(new SharedPref(act).getThemeColorInt()));
        }
    }

    public static boolean cekConnection(Context context) {
        ConnectionDetector conn = new ConnectionDetector(context);
        if (conn.isConnectingToInternet()) {
            return true;
        } else {
            return false;
        }
    }

    public static void displayImage(Context ctx, ImageView img, String url) {
        try {
            Glide.with(ctx.getApplicationContext()).load(url)
                    .transition(withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static void displayImageThumb(Context ctx, ImageView img, String url, float thumb) {
        try {
            Glide.with(ctx.getApplicationContext()).load(url)
                    .transition(withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .thumbnail(thumb)
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static void clearImageCacheOnBackground(final Context ctx) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Glide.get(ctx).clearDiskCache();
                }
            }).start();
        } catch (Exception e) {
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE + "";
    }

    public static int getGridSpanCount(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float screenWidth = displayMetrics.widthPixels;
        float cellWidth = activity.getResources().getDimension(R.dimen.item_place_width);
        return Math.round(screenWidth / cellWidth);
    }

    public static GoogleMap configStaticMap(Activity act, GoogleMap googleMap, Place place) {
        // set map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Enable / Disable zooming controls
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        // Enable / Disable my location button
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        // Enable / Disable Compass icon
        googleMap.getUiSettings().setCompassEnabled(false);
        // Enable / Disable Rotate gesture
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        // Enable / Disable zooming functionality
        googleMap.getUiSettings().setZoomGesturesEnabled(false);
        // enable traffic layer
        googleMap.isTrafficEnabled();
        googleMap.setTrafficEnabled(false);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        LayoutInflater inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View marker_view = inflater.inflate(R.layout.maps_marker, null);
        ((ImageView) marker_view.findViewById(R.id.marker_bg)).setColorFilter(act.getResources().getColor(R.color.marker_secondary));

        CameraPosition cameraPosition = new CameraPosition.Builder().target(place.getPosition()).zoom(12).build();
        MarkerOptions markerOptions = new MarkerOptions().position(place.getPosition());
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(Tools.createBitmapFromView(act, marker_view)));
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        return googleMap;
    }

    public static GoogleMap configActivityMaps(GoogleMap googleMap) {
        // set map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Enable / Disable zooming controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Enable / Disable Compass icon
        googleMap.getUiSettings().setCompassEnabled(true);
        // Enable / Disable Rotate gesture
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        // Enable / Disable zooming functionality
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);

        return googleMap;
    }

    public static void rateAction(Activity activity) {
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        }
    }

    private static String getPlayStoreUrl(Activity act) {
        return "http://play.google.com/store/apps/details?id=" + act.getPackageName();
    }

    public static void aboutAction(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.dialog_about_title));
        builder.setMessage(activity.getString(R.string.about_text));
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    public static void dialNumber(Context ctx, String phone) {
        try {
            Intent i = new Intent(Intent.ACTION_DIAL);
            i.setData(Uri.parse("tel:" + phone));
            ctx.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(ctx, "Cannot dial number", Toast.LENGTH_SHORT);
        }
    }

    public static void directUrl(Context ctx, String website) {
        String url = website;
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            url = "http://" + url;
        }
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        ctx.startActivity(i);
    }

    public static void methodShare(Activity act, Place p) {

        // string to share
        String shareBody = "View good place \'" + p.name + "\'"
                + "\n" + "located at : " + p.address + "\n\n"
                + "Using app : " + getPlayStoreUrl(act);

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, act.getString(R.string.app_name));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        act.startActivity(Intent.createChooser(sharingIntent, "Share Using"));
    }

    public static void methodShareNews(Activity act, NewsInfo n) {

        // string to share
        String shareBody = n.title + "\n\n" + getPlayStoreUrl(act);

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, act.getString(R.string.app_name));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        act.startActivity(Intent.createChooser(sharingIntent, "Share Using"));
    }

    public static Bitmap createBitmapFromView(Activity act, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        act.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public static void setActionBarColor(Context ctx, ActionBar actionbar) {
        ColorDrawable colordrw = new ColorDrawable(new SharedPref(ctx).getThemeColorInt());
        actionbar.setBackgroundDrawable(colordrw);
    }

    public static int colorDarker(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    public static int colorBrighter(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] /= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    public static void restartApplication(Activity activity) {
        activity.finish();
        ActivityMain.getInstance().finish();
        Intent i = new Intent(activity, ActivitySplash.class);
        activity.startActivity(i);
    }

    private static float calculateDistance(LatLng from, LatLng to) {
        Location start = new Location("");
        start.setLatitude(from.latitude);
        start.setLongitude(from.longitude);

        Location end = new Location("");
        end.setLatitude(to.latitude);
        end.setLongitude(to.longitude);

        float distInMeters = start.distanceTo(end);
        float resultDist = 0;
        if (AppConfig.DISTANCE_METRIC_CODE.equals("KILOMETER")) {
            resultDist = distInMeters / 1000;
        } else {
            resultDist = (float) (distInMeters * 0.000621371192);
        }
        return resultDist;
    }

    public static List<Place> filterItemsWithDistance(Activity act, List<Place> items) {
        if (AppConfig.SORT_BY_DISTANCE) { // checking for distance sorting
            LatLng curLoc = Tools.getCurLocation(act);
            if (curLoc != null) {
                return Tools.getSortedDistanceList(items, curLoc);
            }
        }
        return items;
    }

    public static List<Place> itemsWithDistance(Context ctx, List<Place> items) {
        if (AppConfig.SORT_BY_DISTANCE) { // checking for distance sorting
            LatLng curLoc = Tools.getCurLocation(ctx);
            if (curLoc != null) {
                return Tools.getDistanceList(items, curLoc);
            }
        }
        return items;
    }

    public static List<Place> getDistanceList(List<Place> places, LatLng curLoc) {
        if (places.size() > 0) {
            for (Place p : places) {
                p.distance = calculateDistance(curLoc, p.getPosition());
            }
        }
        return places;
    }

    public static List<Place> getSortedDistanceList(List<Place> places, LatLng curLoc) {
        List<Place> result = new ArrayList<>();
        if (places.size() > 0) {
            for (int i = 0; i < places.size(); i++) {
                Place p = places.get(i);
                p.distance = calculateDistance(curLoc, p.getPosition());
                result.add(p);
            }
            Collections.sort(result, new Comparator<Place>() {
                @Override
                public int compare(final Place p1, final Place p2) {
                    return Float.compare(p1.distance, p2.distance);
                }
            });
        } else {
            return places;
        }
        return result;
    }

    public static LatLng getCurLocation(Context ctx) {
        if (PermissionUtil.isLocationGranted(ctx)) {
            LocationManager manager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location loc = ThisApplication.getInstance().getLocation();
                if (loc == null) {
                    loc = getLastKnownLocation(ctx);
                    ThisApplication.getInstance().setLocation(loc);
                }
                if (loc != null) {
                    return new LatLng(loc.getLatitude(), loc.getLongitude());
                }
            }
        }
        return null;
    }

    public static Location getLastKnownLocation(Context ctx) {
        // add location listener
        setLocationListener(ctx);

        LocationManager mLocationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = Tools.requestLocationUpdate(mLocationManager);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        mLocationManager.removeUpdates(locationListener);
        return bestLocation;
    }

    private static LocationListener requestLocationUpdate(LocationManager manager) {
        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        for (String provider : manager.getAllProviders()){
            manager.requestLocationUpdates(provider, 0, 0, locationListener);
        }
        return locationListener;
    }

    private static LocationCallback locationCallback = null;
    private static FusedLocationProviderClient locationProviderClient = null;
    private static void setLocationListener(Context ctx) {
        if(locationCallback != null) return;
        if(locationProviderClient == null){
            locationProviderClient = LocationServices.getFusedLocationProviderClient(ctx);
        }
        // for getting the current location update after every 2 seconds with high accuracy
        final LocationRequest locationRequest = new LocationRequest()
                .setInterval(500).setFastestInterval(500)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                locationProviderClient.removeLocationUpdates(locationCallback);
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        ThisApplication.getInstance().setLocation(location);
                    }
                }
            }
        };
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    public static String getFormatedDistance(float distance) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(1);
        return df.format(distance) + " " + AppConfig.DISTANCE_METRIC_STR;
    }

    public static DeviceInfo getDeviceInfo(Context context) {
        String phoneID = Build.SERIAL;
        try {
            phoneID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
        }
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDevice(Tools.getDeviceName());
        deviceInfo.setEmail(phoneID);
        deviceInfo.setVersion(Tools.getAndroidVersion());
        deviceInfo.setRegid(new SharedPref(context).getFcmRegId());
        deviceInfo.setDate_create(System.currentTimeMillis());

        return deviceInfo;
    }

    public static String getFormattedDateSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MMM dd, yyyy");
        return newFormat.format(new Date(dateTime));
    }

    public static String getFormattedDate(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy hh:mm");
        return newFormat.format(new Date(dateTime));
    }


    public interface CallbackRegId {
        void onSuccess(DeviceInfo result);

        void onError();
    }

    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static void directLinkToBrowser(Activity activity, String url) {
        url = appendQuery(url, "t=" + System.currentTimeMillis());
        if (!URLUtil.isValidUrl(url)) {
            Toast.makeText(activity, "Ops, Cannot open url", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(intent);
    }

    private static String appendQuery(String uri, String appendQuery) {
        try {
            URI oldUri = new URI(uri);
            String newQuery = oldUri.getQuery();
            if (newQuery == null) {
                newQuery = appendQuery;
            } else {
                newQuery += "&" + appendQuery;
            }
            URI newUri = new URI(
                    oldUri.getScheme(),
                    oldUri.getAuthority(),
                    oldUri.getPath(), newQuery, oldUri.getFragment()
            );
            return newUri.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return uri;
        }
    }

}
