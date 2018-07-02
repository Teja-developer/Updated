package com.example.sudhaseshu.login;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.maps.android.SphericalUtil;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.sudhaseshu.login.Bluetooth.connected;
import static com.example.sudhaseshu.login.Bluetooth.myThreadConnected;
import static com.example.sudhaseshu.login.DirectionParser.dir;
import static com.example.sudhaseshu.login.DirectionParser.dist;
import static com.example.sudhaseshu.login.DirectionParser.latLngs;
import static com.example.sudhaseshu.login.DirectionParser.simp_dir;

public class Nav extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private LatLngBounds bounds;
    private Bundle saved;
    private String place;
    private boolean dir_click = false;
    private static final String TAG = "MapActivity";
    private AutoCompleteTextView mSearchText;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private Polyline polyline;
    private PlaceInfo mPlace;
    private String current_mode="mode=driving";
    private com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton actionButton;
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    final String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int REQUEST_CODE = 123;
    private static final long MIN_TIME = 2;
    private static final float MIN_DISTANCE = 30;
    private static final float DEFAULT_ZOOM = 17f;

    private Boolean mLocationPermissionGranted = false;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    public Location currentLocation;
    public LatLng des, northSide, southSide;

    private AutocompleteFilter filter;
    public TextView directions, usrnm, emailid;
    public ImageView profilep;
    public DrawerLayout drawer;
    private FloatingActionButton currentl;
    FloatingActionButton direction;
    public Button nav;

    public TextView primary, addr, phn, latln;
    LinearLayout layoutBottomSheet;
    BottomSheetBehavior sheetBehavior;

    public ImageButton voicesrc;

    double lon;
    double lat;
    private boolean voice;

    @Override
    protected void onStart() {
        saved = new Bundle();
        super.onStart();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);

        //For displaying the bluetooth icon when the device is connected
        ImageView blue = (ImageView) findViewById(R.id.blue);
        if(connected)
        {
            blue.setVisibility(View.VISIBLE);
        }

        if (savedInstanceState != null) {
            Log.i(TAG, " " + savedInstanceState.get("directions") + " MainBUndle");
        } else {
            Log.i(TAG, "empty");
        }

        mSearchText = findViewById(R.id.input_search);

        //Persistent Bottom Sheet
        primary = findViewById(R.id.mainnm);
        addr = findViewById(R.id.address);
        phn = findViewById(R.id.phno);
        btmsheet();

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        //Current Location
        currentl = findViewById(R.id.curl);
        currentl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });

        //Directions
        direction = findViewById(R.id.dir);
        direction.setVisibility(View.GONE);
        direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSearchText.length() < 1) {
                    Toast.makeText(getApplicationContext(), "Please enter the destination address", Toast.LENGTH_SHORT).show();
                } else {
                    destination(v);
                }
            }
       });

        nav = findViewById(R.id.nav);
        nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionButton.setVisibility(View.VISIBLE);
                destination(v);
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        usrnm = findViewById(R.id.userName);
        emailid = findViewById(R.id.emailId);
        profilep = findViewById(R.id.profilePic);
        directions = findViewById(R.id.direct);

        //DrawerActions
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                hideSoftKeyboard();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                actionButton.setVisibility(View.GONE);
            }

            public void onDrawerClosed(View view){
                actionButton.setVisibility(View.VISIBLE);
            }
        };
        drawer.addDrawerListener(toggle);

        toggle.syncState();

        //Navigation View
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Floating action menu for switching between vehicle mode and walking mode
        //Step1

        ImageView icon = new ImageView(this); // Create an icon
        icon.setImageResource(R.drawable.bluetooth);


        actionButton = new com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton.Builder(this)
                .setContentView(icon)
                .build();

        actionButton.setVisibility(View.GONE);
        ((com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton.LayoutParams) actionButton.getLayoutParams()).setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.margin_right), getResources().getDimensionPixelSize(R.dimen.margin_bottom));
        //Step2
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);

        ImageView itemIcon = new ImageView(this);
        itemIcon.setImageResource(R.drawable.bluetooth);
        SubActionButton button1 = itemBuilder.setContentView(itemIcon).build();

        ImageView itemIcon2 = new ImageView(this);
        itemIcon2.setImageResource(R.drawable.bluetooth);
        SubActionButton button2 = itemBuilder.setContentView(itemIcon2).build();

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Walking",Toast.LENGTH_SHORT).show();
                current_mode = "mode=walking";
                polyline.remove();
                destination(v);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Driving",Toast.LENGTH_SHORT).show();
                current_mode = "mode=driving";
                polyline.remove();
                destination(v);
            }
        });

        //Step3
        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .setStartAngle(180)
                .setEndAngle(225)
                .addSubActionView(button1)
                .addSubActionView(button2)
                // ...
                .attachTo(actionButton)
                .build();


//        final FloatingActionButton fab = findViewById(R.id.fab);
//
//
//        //attach menu to fab

        /* Request Permissions */
        getLocationPermission();
        if (mLocationPermissionGranted) {
            getDeviceLocation();
        }

        //voice search
        voicesrc = findViewById(R.id.voicesrch);
        voicesrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Voice Search");
                voice_search();
            }
        });
        init();
    }

    private void voice_search() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);

            if (matches != null) {
                if (matches.size() > 0) {
                    place = matches.get(0);
                    mSearchText.setText(place, true);
                    voice = true;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void hideNavigationBar() {
        this.getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
    }

    public void btmsheet() {
        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }


    //Initialize the map
    private void initMap() {
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                try {
                    // Customise the styling of the base map using a JSON object defined
                    // in a raw resource file.
                    boolean success = googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    getApplicationContext(), R.raw.blackm));

                    if (!success) {
                        Log.i("Map", "Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    Log.i("Map", "Can't find style. Error: ", e);

                }
                getDeviceLocation();
                Log.i("Map", "Entered initMap");
                Toast.makeText(getApplicationContext(), "Attack", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        //this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // Get the current location
    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {

            final com.google.android.gms.tasks.Task<Location> location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<Location> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        currentLocation = location.getResult();
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM);
                        mMap.animateCamera(cameraUpdate);
                        mMap.setMyLocationEnabled(true);
                        mMap.setBuildingsEnabled(true);
                        //For disabling location button
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);

                        northSide = SphericalUtil.computeOffset(latLng, 100000, 0);
                        southSide = SphericalUtil.computeOffset(latLng, 100000, 180);

                        bounds = LatLngBounds.builder()
                                .include(northSide)
                                .include(southSide)
                                .build();
                        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(getApplicationContext(), mGoogleApiClient,
                                bounds, filter);
                        mSearchText.setAdapter(mPlaceAutocompleteAdapter);
                    }
                }
            });

        } catch (SecurityException e) {
            Log.i("Map", "Security Exception" + e);
        }
    }

    //Search View with PlacesAutoComplete
    private void init() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        filter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();

        Log.i(TAG, " " + filter.toString());
        Log.i(TAG, " " + bounds);
        mSearchText.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                bounds, filter);
        mSearchText.setAdapter(mPlaceAutocompleteAdapter);
//        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
//                if(actionId == EditorInfo.IME_ACTION_SEARCH
//                        || actionId == EditorInfo.IME_ACTION_DONE
//                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
//                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
//
//                    //execute our method for searching
//                }
//
//                return false;
//            }
//        });
    }


    //Camera Zoom
    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);
        //For disabling location button
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }


    //Location Permissions
    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    //Open Map
                    initMap();
                }
            }
        }
    }

    //For marking the destination address
    public void destination(View v) {
        //For getting the direction
        String url = requestUrl(des);
        Nav.TaskRequestDirections taskRequestDirections = new Nav.TaskRequestDirections();
        taskRequestDirections.execute(url);
    }

    public void destMarker() {
        lat = des.latitude;
        lon = des.longitude;
        goToLocation(des.latitude, des.longitude, 18);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(lat, lon));
        mMap.addMarker(markerOptions);
    }

    //Moves the camera to the location which is selected by the user
    public void goToLocation(double latitude, double longitude, int zoom) {
        LatLng latLng1 = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng1, zoom);
        mMap.animateCamera(cameraUpdate);
    }


    //Url for getting the directions,etc
    private String requestUrl(LatLng des) {
        //Taking default origin location temporarily...

        String origin = "origin=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude();
        String des1 = "destination=" + des.latitude + "," + des.longitude;
        String sensor = "sensor=false";
        String mode = current_mode;
        Toast.makeText(getApplicationContext(),current_mode,Toast.LENGTH_SHORT).show();
        String param = origin + "&" + des1 + "&" + sensor + "&" + mode;
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
        Log.i("Map", url);
        return url;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class TaskRequestDirections extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String responseString = null;

            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Nav.TaskParser taskParser = new Nav.TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject object = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                object = new JSONObject(strings[0]);
                DirectionParser directionsParser = new DirectionParser();
                routes = directionsParser.parse(object);
                Log.i("Map", "Reached TaskParser");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            super.onPostExecute(lists);

            ArrayList points;
            PolylineOptions options;
            options = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                options = new PolylineOptions();

                for (HashMap<String, String> point : path) {

                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat, lon));
                }
                double longitude = currentLocation.getLongitude();
                double latitude = currentLocation.getLatitude();

                //Adding current location to head of the latLngs
                latLngs.add(0, new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));

                Log.i("Map", "" + longitude + " " + latitude);
                Log.i("Map", "" + latLngs.get(0).longitude + " " + latLngs.get(0).latitude);

                for (int i = 0; i < dir.length; i++) {
                    if (longitude == latLngs.get(i).longitude && latitude == latLngs.get(i).latitude) {
                        Log.i("Mafps", "Equalss....................................." + latLngs.size() + " " + dist.length + " " + dir.length);
                        //Need to continue
                    }
                }

                //For updating the location
                LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                LocationListener mLocationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        Log.d("Map", "onLocationChanged() callback received");
                        double longitude = (location.getLongitude());
                        double latitude = (location.getLatitude());

                        Log.d("Map", "longitude is: " + longitude);
                        Log.d("Map", "latitude is: " + latitude);

                        //if(steps.getDuration - (Longitude.getDIrection(end,start,kdla,alskd)<=10)
                        for (int i = 0; i < dir.length; i++) {
//                            if(longitude==latLngs.get(i).longitude && latitude==latLngs.get(i).latitude) {
//                                directions.setText(""+dir[i]);
//                                Log.i("Maps",""+dir[i]);
//                                //Need to continue
//                            }

                            Location temp = new Location("temp");
                            temp.setLatitude(latLngs.get(i + 1).latitude);
                            temp.setLongitude(latLngs.get(i + 1).longitude);
                            Log.i("Map", "" + location.getLatitude() + " " + location.getLongitude() + "\n " + dist[i] + " " + location.distanceTo(temp));

                            if (dist[i] - location.distanceTo(temp) < 50 && dist[i] - location.distanceTo(temp) > 0) {
                                Log.d("Map", "Reached!");
                                directions.setText(simp_dir[i]);
                                if (dist[i] - location.distanceTo(temp) < 5 && dist[i] - location.distanceTo(temp) > 0){
                                    if (myThreadConnected != null) {
                                        byte[] bytesToSend;
                                        try {
                                            bytesToSend = "0".getBytes();
                                        } catch (ArrayIndexOutOfBoundsException e) {
                                            bytesToSend = "0".getBytes();
                                        }
                                        myThreadConnected.write(bytesToSend);
                                    }
                                }else {
                                    if (myThreadConnected != null) {
                                        byte[] bytesToSend ;
                                        try {
                                            if(simp_dir[i-1]=="turn-left" || simp_dir[i-1]=="turn-slight-left" || simp_dir[i-1]=="turn-sharp-left"){
                                                Log.d("Send", "Turn left");
                                                bytesToSend = "1".getBytes();
                                            }
                                            else if(simp_dir[i-1]=="turn-right" || simp_dir[i-1]=="turn-slight-right" || simp_dir[i-1]=="turn-sharp-right"){
                                                bytesToSend = "2".getBytes();
                                                Log.d("Send" , "Turn Right");
                                            }
                                            else{
                                                bytesToSend = "0".getBytes();
                                            }
                                        } catch (ArrayIndexOutOfBoundsException e) {
                                            bytesToSend = simp_dir[i].getBytes();
                                        }
                                        myThreadConnected.write(bytesToSend);
                                    }
                                }
                                //Log.i("Map","This is from locationListener");
                            }
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        // Log statements to help you debug your app.
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        Log.i("Map", "onProviderDisabled() callback received. Provider: " + provider);
                    }

                };

                // This is the permission check to access (fine) location.
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions((Activity) getApplicationContext(),
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
                    return;
                }
                Log.i("Map", "End");
                mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
                options.addAll(points);
                options.width(12);
                options.color(ContextCompat.getColor(getApplicationContext(), R.color.mgold));
                options.geodesic(true);
            }
            if (options != null) {
                polyline = mMap.addPolyline(options);
                if (dir_click == false)
                    dir_click = true;
                else
                    dir_click = false;
            } else
                Toast.makeText(getApplicationContext(), "Directions not found :(", Toast.LENGTH_SHORT).show();
        }
    }

    private String requestDirection(String reqUrl) throws IOException {

        String responseString = null;
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;

        try {
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line = "";
            StringBuffer stringBuffer = new StringBuffer();

            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            responseString = stringBuffer.toString();
            inputStreamReader.close();
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null)
                inputStream.close();
            httpURLConnection.disconnect();
        }
        return responseString;
    }


    @Override
    public void onBackPressed() {
        Log.i(TAG, " " + dir_click + " " + item_clicked);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (dir_click) {
            //TODO: remove marker when back button is pressed when directions are showing!! else it should quit the app( or double tap to quit the app
            polyline.remove();
            dir_click = false;
        }
        else if(item_clicked) {
            actionButton.setVisibility(View.VISIBLE);
            getDeviceLocation();
            direction.setVisibility(View.GONE);
            layoutBottomSheet.setVisibility(View.GONE);
            item_clicked = false;
        }
        else
        super.onBackPressed();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.connect) {
            Intent intent = new Intent(Nav.this, Bluetooth.class);
            startActivity(intent);
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            if(mMap.isTrafficEnabled() == true){
                mMap.setTrafficEnabled(false);
            }else {
                mMap.setTrafficEnabled(true);
            }
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }else if(id == R.id.signot) {
            FirebaseAuth.getInstance().signOut();
            finish();
            startActivity(new Intent(this , SignIn.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

      /*
        --------------------------- google places API autocomplete suggestions -----------------
     */

    private boolean item_clicked = false;
    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            direction.setVisibility(View.GONE);
            hideSoftKeyboard();
            item_clicked = true;
            direction.setVisibility(View.VISIBLE);
            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();
            place = placeId;

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Toast.makeText(getApplicationContext(), "Pressed", Toast.LENGTH_SHORT).show();
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            try {
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                Log.d(TAG, "onResult: name: " + place.getName());
                mPlace.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: address: " + place.getAddress());
//                mPlace.setAttributions(place.getAttributions().toString());
//                Log.d(TAG, "onResult: attributions: " + place.getAttributions());
                mPlace.setId(place.getId());
                Log.d(TAG, "onResult: id:" + place.getId());
                mPlace.setLatlng(place.getLatLng());
                Log.d(TAG, "onResult: latlng: " + place.getLatLng());
                des = place.getLatLng();
                mPlace.setRating(place.getRating());
                Log.d(TAG, "onResult: rating: " + place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: phone number: " + place.getPhoneNumber());
                mPlace.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "onResult: website uri: " + place.getWebsiteUri());

                Log.d(TAG, "onResult: place: " + mPlace.toString());
                primary.setText(mPlace.getName());
                addr.setText(mPlace.getAddress());
                phn.setText(mPlace.getPhoneNumber());
                latln.setText(mPlace.getLatlng().toString());

            } catch (NullPointerException e) {
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage());
            }

            //moveCamera(new LatLng(place.getViewport().getCenter().latitude,
            //      place.getViewport().getCenter().longitude), DEFAULT_ZOOM, mPlace.getName());
            destMarker();
            places.release();
            LinearLayout btmpg = findViewById(R.id.bottom_sheet);
            btmpg.setVisibility(View.VISIBLE);
            ImageView dirs = findViewById(R.id.dirs);
            dirs.setVisibility(View.VISIBLE);
            TextView text = findViewById(R.id.direct);
            text.setVisibility(View.VISIBLE);
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        onSaveInstanceState(new Bundle());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("direction", dir_click);
        outState.putString("place", place);
        Log.i(TAG, " " + outState.get("direction"));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.i(TAG, "Entered REsume");
    }

}
