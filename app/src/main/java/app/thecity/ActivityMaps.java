package app.thecity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.HashMap;
import java.util.List;

import app.thecity.data.Constant;
import app.thecity.data.DatabaseHandler;
import app.thecity.model.Category;
import app.thecity.model.Place;
import app.thecity.utils.PermissionUtil;
import app.thecity.utils.Tools;

public class ActivityMaps extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_OBJ = "key.EXTRA_OBJ";

    private GoogleMap mMap;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private DatabaseHandler db;
    private ClusterManager<Place> mClusterManager;
    private View parent_view;
    private int cat[];
    private PlaceMarkerRenderer placeMarkerRenderer;

    // for single place
    private Place ext_place = null;
    private boolean isSinglePlace;
    HashMap<String, Place> hashMapPlaces = new HashMap<>();

    // id category
    private int cat_id = -1;

    private Category cur_category;

    // view for custom marker
    private ImageView icon, marker_bg;
    private View marker_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        parent_view = findViewById(android.R.id.content);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        marker_view = inflater.inflate(R.layout.maps_marker, null);
        icon = (ImageView) marker_view.findViewById(R.id.marker_icon);
        marker_bg = (ImageView) marker_view.findViewById(R.id.marker_bg);

        ext_place = (Place) getIntent().getSerializableExtra(EXTRA_OBJ);
        isSinglePlace = (ext_place != null);

        db = new DatabaseHandler(this);
        initMapFragment();
        initToolbar();

        cat = getResources().getIntArray(R.array.id_category);

        // for system bar in lollipop
        Tools.systemBarLolipop(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = Tools.configActivityMaps(googleMap);
        CameraUpdate location;
        if (isSinglePlace) {
            marker_bg.setColorFilter(getResources().getColor(R.color.marker_secondary));
            MarkerOptions markerOptions = new MarkerOptions().title(ext_place.name).position(ext_place.getPosition());
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(Tools.createBitmapFromView(ActivityMaps.this, marker_view)));
            mMap.addMarker(markerOptions);
            location = CameraUpdateFactory.newLatLngZoom(ext_place.getPosition(), 12);
            actionBar.setTitle(ext_place.name);
        } else {
            location = CameraUpdateFactory.newLatLngZoom(new LatLng(Constant.city_lat, Constant.city_lng), 9);
            mClusterManager = new ClusterManager<>(this, mMap);
            placeMarkerRenderer = new PlaceMarkerRenderer(this, mMap, mClusterManager);
            mClusterManager.setRenderer(placeMarkerRenderer);
            mMap.setOnCameraChangeListener(mClusterManager);
            loadClusterManager(db.getAllPlace());
        }
        mMap.animateCamera(location);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Place place;
                if (hashMapPlaces.get(marker.getId()) != null) {
                    place = (Place) hashMapPlaces.get(marker.getId());
                } else {
                    place = ext_place;
                }
                ActivityPlaceDetail.navigate(ActivityMaps.this, parent_view, place);
            }
        });

        showMyLocation();
    }

    private void showMyLocation() {
        if (PermissionUtil.isLocationGranted(this)) {
            // Enable / Disable my location button
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    try {
                        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            showAlertDialogGps();
                        } else {
                            Location loc = Tools.getLastKnownLocation(ActivityMaps.this);
                            CameraUpdate myCam = CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 12);
                            mMap.animateCamera(myCam);
                        }
                    } catch (Exception e) {
                    }
                    return true;
                }
            });
        }
    }

    private void loadClusterManager(List<Place> places) {
        mClusterManager.clearItems();
        mClusterManager.addItems(places);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.activity_title_maps);
        Tools.setActionBarColor(this, actionBar);
    }

    private void initMapFragment() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    private class PlaceMarkerRenderer extends DefaultClusterRenderer<Place> {
        public PlaceMarkerRenderer(Context context, GoogleMap map, ClusterManager<Place> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(Place item, MarkerOptions markerOptions) {
            if (cat_id == -1) { // all place
                icon.setImageResource(R.drawable.round_shape);
            } else {
                icon.setImageResource(cur_category.icon);
            }
            marker_bg.setColorFilter(getResources().getColor(R.color.marker_primary));
            markerOptions.title(item.name);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(Tools.createBitmapFromView(ActivityMaps.this, marker_view)));
            if (ext_place != null && ext_place.place_id == item.place_id) {
                markerOptions.visible(false);
            }
        }

        @Override
        protected void onClusterItemRendered(Place item, Marker marker) {
            hashMapPlaces.put(marker.getId(), item);
            super.onClusterItemRendered(item, marker);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        } else {
            String category_text;
            if (item.getItemId() != R.id.menu_category) {
                category_text = item.getTitle().toString();
                switch (item.getItemId()) {
                    case R.id.nav_all:
                        cat_id = -1;
                        break;
                    case R.id.nav_featured:
                        cat_id = cat[10];
                        break;
                    case R.id.nav_tour:
                        cat_id = cat[0];
                        break;
                    case R.id.nav_food:
                        cat_id = cat[1];
                        break;
                    case R.id.nav_hotels:
                        cat_id = cat[2];
                        break;
                    case R.id.nav_ent:
                        cat_id = cat[3];
                        break;
                    case R.id.nav_sport:
                        cat_id = cat[4];
                        break;
                    case R.id.nav_shop:
                        cat_id = cat[5];
                        break;
                    case R.id.nav_transport:
                        cat_id = cat[6];
                        break;
                    case R.id.nav_religion:
                        cat_id = cat[7];
                        break;
                    case R.id.nav_public:
                        cat_id = cat[8];
                        break;
                    case R.id.nav_money:
                        cat_id = cat[9];
                        break;
                }

                // get category object when menu click
                cur_category = db.getCategory(cat_id);

                if (isSinglePlace) {
                    isSinglePlace = false;
                    mClusterManager = new ClusterManager<>(this, mMap);
                    mMap.setOnCameraChangeListener(mClusterManager);
                }

                List<Place> places = db.getAllPlaceByCategory(cat_id);
                loadClusterManager(places);
                if (places.size() == 0) {
                    Snackbar.make(parent_view, getString(R.string.no_item_at) + " " + item.getTitle().toString(), Snackbar.LENGTH_LONG).show();
                }
                placeMarkerRenderer = new PlaceMarkerRenderer(this, mMap, mClusterManager);
                mClusterManager.setRenderer(placeMarkerRenderer);

                actionBar.setTitle(category_text);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAlertDialogGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_content_gps);
        builder.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.NO, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

}
