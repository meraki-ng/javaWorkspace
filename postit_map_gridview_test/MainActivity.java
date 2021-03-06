
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private Button Button_search;
    private EditText EditText_text;
    private Geocoder geocoder;
    List<Address> addresses;
    String address;
    List<Location> locationList;
    Iterator<Address> locations;
    private GoogleMap mGoogleMap = null;
    private Marker currentMarker = null;
    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1???
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5???
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // ?????? ?????????
    Location mCurrentLocatiion;
    LatLng currentPosition;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    private View mLayout;  // Snackbar ???????????? ???????????? View??? ???????????????.

    int oncreateOne = 0;
    String getCurrentAddressString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        /*final TextView txtVw = findViewById(R.id.placeName);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                txtVw.setText(place.getName());
            }
            @Override
            public void onError(Status status) {
                txtVw.setText(status.toString());
            }
        });*/

        mLayout = findViewById(R.id.layout_main);
        Button_search = findViewById(R.id.Button_search);
        EditText_text = findViewById(R.id.EditText_text);
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());
                getCurrentAddressString = getCurrentAddress(currentPosition);
                String markerSnippet = getCurrentAddress(currentPosition);
                setCurrentLocation(location , markerSnippet);
                mCurrentLocatiion = location;
            }
        }
    };


    private void startLocationUpdates() {
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            if (checkPermission())
                mGoogleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mGoogleMap = googleMap;
        geocoder = new Geocoder(this);
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                MarkerOptions mOptions = new MarkerOptions();
                String str = EditText_text.getText().toString();
                List<Address> addressList = null;
                try {
                    addressList = geocoder.getFromLocationName(str, 1); // ?????? / ?????? ?????? ?????? ??????
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String[] splitStr = addressList.get(0).toString().split(",");
                address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1, splitStr[0].length() - 2); // ??????
                mOptions.title(str);
                mOptions.position(point);
                Double latitude = point.latitude; // ??????
                Double longitude = point.longitude; // ??????
                mOptions.snippet(getCurrentAddress(point));
                getCurrentAddressString = getCurrentAddress(point);
                mOptions.position(new LatLng(latitude, longitude));
                googleMap.addMarker(mOptions);
            }
        });
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(MainActivity.this, UserClickHomeActivity.class);
                intent.putExtra("address", getCurrentAddressString);
                Log.d("????????????",getCurrentAddressString);
                startActivity(intent);
                return false;
            }
        });
        Button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = EditText_text.getText().toString();
                oncreateOne = 1;
                if (str == null || str.equals("")) {
                    Toast.makeText(MainActivity.this, "????????? ?????? ???????????????, \n\t ?????? ??????????????????.", Toast.LENGTH_SHORT).show();
                } else {
                    List<Address> addressList = null;
                    try {
                        addressList = geocoder.getFromLocationName(str, 1); // ?????? / ?????? ?????? ?????? ??????
                        locations = addressList.iterator();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        String[] splitStr = addressList.get(0).toString().split(",");
                        address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1, splitStr[0].length() - 2); // ??????
                        String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // ??????
                        String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // ??????
                        LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        MarkerOptions mOptions2 = new MarkerOptions();
                        mOptions2.title(str);
                        mOptions2.getSnippet();
                        mOptions2.snippet(getCurrentAddress(point));
                        getCurrentAddressString = getCurrentAddress(point);
                        mOptions2.position(point);
                        mGoogleMap.addMarker(mOptions2);
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "???????????? ????????? ?????? ???????????????, \n\t ?????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    }


                }

            }
        });
        setDefaultLocation();
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates(); // 3. ?????? ???????????? ??????
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                Snackbar.make(mLayout, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.",
                        Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (checkPermission()) {

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            if (mGoogleMap != null)
                mGoogleMap.setMyLocationEnabled(true);

        }
    }


    @Override
    protected void onStop() {

        super.onStop();

        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public String getCurrentAddress(LatLng latlng) {

        //????????????... GPS??? ????????? ??????
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //???????????? ??????
            Toast.makeText(this, "???????????? ????????? ????????????", Toast.LENGTH_LONG).show();
            return "???????????? ????????? ????????????";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "????????? GPS ??????", Toast.LENGTH_LONG).show();
            return "????????? GPS ??????";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "?????? ?????????", Toast.LENGTH_LONG).show();
            return "?????? ?????????";

        } else {

            Address address = addresses.get(0);
            return address.getAddressLine(0);
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location, String markerSnippet) {
        if (oncreateOne == 0) {
            if (currentMarker != null) currentMarker.remove();
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLatLng);
            markerOptions.snippet(markerSnippet);
            markerOptions.draggable(true);
            currentMarker = mGoogleMap.addMarker(markerOptions);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
            mGoogleMap.moveCamera(cameraUpdate);
        }
    }

    public void setDefaultLocation() {


        //????????? ??????, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "???????????? ??????????????? ?????????";
        String markerSnippet = "?????? ????????? GPS ?????? ?????? ???????????????";


        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mGoogleMap.moveCamera(cameraUpdate);

    }


    //??????????????? ????????? ????????? ????????? ?????? ????????????
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;

    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ??????????????????

            boolean check_result = true;


            // ?????? ???????????? ??????????????? ???????????????.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                // ???????????? ??????????????? ?????? ??????????????? ???????????????.
                startLocationUpdates();
            } else {
                // ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ?????? ???????????????.2 ?????? ????????? ????????????.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {


                    // ???????????? ????????? ????????? ???????????? ?????? ?????? ???????????? ????????? ???????????? ?????? ????????? ??? ????????????.
                    Snackbar.make(mLayout, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();

                } else {


                    // "?????? ?????? ??????"??? ???????????? ???????????? ????????? ????????? ???????????? ??????(??? ??????)?????? ???????????? ???????????? ?????? ????????? ??? ????????????.
                    Snackbar.make(mLayout, "???????????? ?????????????????????. ???????????? ?????? ??? ??????????????????.",
                            Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }
    }


    //??????????????? GPS ???????????? ?????? ????????????
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                + "?????? ????????? ?????????????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS ????????? ?????????");


                        needRequest = true;

                        return;
                    }
                }
                break;
        }
    }
}
