package com.example.pickhere;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.LocationDataSourceHERE;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.mapping.MapState;
import com.here.android.mpa.mapping.SupportMapFragment;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.RoutingError;
import com.here.android.positioning.StatusListener;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends AppCompatActivity implements Map.OnTransformListener {
    private ImageButton imageButton;
    private static MapRoute Maproute = null;
    private MapMarker  mapMarker,parking1,parking2,recordmarkercliked;
    private Button mRideStatus;
    private LinearLayout LayPoiInfo;
    private Map map = null;
    private Button btn,btncloselay;
    private SupportMapFragment mapFragment = null;
    private PositioningManager mPositioningManager;
    private LocationDataSourceHERE mHereLocation;
    // flag that indicates whether maps is being transformed
    private boolean mTransforming;
    private Runnable mPendingUpdate;
    private Boolean paused;
    private Button btnReportIncident;
    private double latitude,longitude,allincidentlat,allincidentlong;
    private GeoCoordinate pickupLocation;

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    private List<MapObject> m_mapObjectList = new ArrayList<>();
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private String markerIdcliked;
    private ImageView imageView;
    private TextView timestamp;
    private TextView address;
    private int status = 0;
    private GeoCoordinate destinationLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        btnReportIncident = findViewById(R.id.reportincident);
        btn=findViewById(R.id.button);
        LayPoiInfo = findViewById(R.id.Poibuyinfo);
        btncloselay=findViewById(R.id.close);
        imageView=findViewById(R.id.incidentpics);
        timestamp=findViewById(R.id.timedate);
        address=findViewById(R.id.loc);
        mRideStatus =findViewById(R.id.pickBike);
        imageButton=findViewById(R.id.imageButton);
        imageButton.setVisibility(View.VISIBLE);
        checkPermissions();
       /* if (map!=null) {
            addAllIncidentMarkers();
        }*/

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAllIncidentMarkers();
            }
        });

        btncloselay.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View view) {
                                               LayPoiInfo.setVisibility(View.INVISIBLE);
                                               btnReportIncident.setVisibility(View.VISIBLE);
                                               imageButton.setVisibility(View.VISIBLE);

                                           }
    });

                btnReportIncident.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        erasePolylines();
                        removeMarkers();
                        getLiveCoordinates();
                        Opendashboard();
                    }
                });

        LayPoiInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayPoiInfo.setVisibility(View.INVISIBLE);
                btnReportIncident.setVisibility(View.VISIBLE);
            }
        });


mRideStatus.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        LayPoiInfo.setVisibility(View.INVISIBLE);
        btnReportIncident.setVisibility(View.VISIBLE);
        getAssignedCustomerDestination();

    }
});

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Opendash();
            }
        });



    }

    public void Opendash(){
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }



    private void getRouteToMarker(GeoCoordinate pickupOrDestination) {
        GeoPosition geoPosition;
        GeoCoordinate coordinate;
        if (mPositioningManager != null) {
            geoPosition = mPositioningManager.getPosition();
            coordinate = geoPosition.getCoordinate();
// 2. Initialize RouteManager
            CoreRouter router = new CoreRouter();

            // 3. Select routing options
            RoutePlan routePlan = new RoutePlan();
            routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(coordinate.getLatitude(), coordinate.getLongitude())));
            routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(pickupOrDestination)));
            // Create the RouteOptions and set its transport mode & routing type
            RouteOptions routeOptions = new RouteOptions();
            routeOptions.setTransportMode(RouteOptions.TransportMode.SCOOTER);
            routeOptions.setRouteType(RouteOptions.Type.FASTEST);
            routePlan.setRouteOptions(routeOptions);
            router.calculateRoute(routePlan, routeManagerListener);
        }

    }
    private CoreRouter.Listener routeManagerListener = new CoreRouter.Listener() {
        @Override
        public void onCalculateRouteFinished(List<RouteResult> list, RoutingError routingError) {
            if (routingError == RoutingError.NONE && list.get(0).getRoute() != null) {
// Render the route on the map
                Maproute = new MapRoute(list.get(0).getRoute());
                map.addMapObject(Maproute);
                // Get the bounding box containing the route and zoom in (no animation)
                GeoBoundingBox gbb = list.get(0).getRoute().getBoundingBox();
                map.zoomTo(gbb, Map.Animation.NONE, Map.MOVE_PRESERVE_ORIENTATION);
            } else {
// Display a message indicating route calculation failure
                Toast.makeText(MapActivity.this, "Route calculation failed: %s" + routingError.toString(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onProgress(int i) {

        }
    };
    private void getAssignedCustomerDestination()
    {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference DestinationParking1 = FirebaseDatabase.getInstance().getReference().child("ParkingZone");

        DestinationParking1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    java.util.Map<String, Object> map = (java.util.Map<String, Object>) dataSnapshot.getValue();

                    Double destinationLat = 0.0;
                    Double destinationLng = 0.0;
                    if(map.get("Parkinglat1") != null){
                        destinationLat = Double.valueOf(map.get("Parkinglat1").toString());
                    }
                    if(map.get("Parkinglong1") != null){
                        destinationLng = Double.valueOf(map.get("Parkinglong1").toString());
                        destinationLatLng = new GeoCoordinate(destinationLat, destinationLng);
                    }
                }
                if (destinationLatLng.getLatitude() != 0.0 && destinationLatLng.getLongitude() != 0.0) {
                    getRouteToMarker(destinationLatLng);
                    //DestinationMarker = new MapMarker();
                    //DestinationMarker.setCoordinate(destinationLatLng);
                    //map.addMapObject(DestinationMarker);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    private MapGesture.OnGestureListener onGestureListenernew = new
            MapGesture.OnGestureListener() {

                @Override
                public void onPanStart() {
                }

                @Override
                public void onPanEnd() {
                }

                @Override
                public void onMultiFingerManipulationStart() {
                }

                @Override
                public void onMultiFingerManipulationEnd() {
                }

                @Override
                public boolean onMapObjectsSelected(final List<ViewObject> list) {
                    for (ViewObject viewObject : list) {
                        if (viewObject.getBaseType() == ViewObject.Type.USER_OBJECT) {
                            MapObject mapObject = (MapObject) viewObject;

                            if (mapObject.getType() == MapObject.Type.MARKER) {
                                new AsyncCaller(mapObject).execute();
                            }
                        }
                    }
                    return false;
                }

                @Override
                public boolean onTapEvent(PointF pointF) {
                    return false;
                }

                @Override
                public boolean onDoubleTapEvent(PointF pointF) {
                    return false;
                }

                @Override
                public void onPinchLocked() {
                }

                @Override
                public boolean onPinchZoomEvent(float v, PointF pointF) {
                    return false;
                }

                @Override
                public void onRotateLocked() {
                }

                @Override
                public boolean onRotateEvent(float v) {
                    return false;
                }

                @Override
                public boolean onTiltEvent(float v) {
                    return false;
                }

                @Override
                public boolean onLongPressEvent(PointF pointF) {
                    return false;
                }

                @Override
                public void onLongPressRelease() {
                }

                @Override
                public boolean onTwoFingerTapEvent(PointF pointF) {
                    return false;
                }
            };



    private class AsyncCaller extends AsyncTask<Void, Void, Void>
    {

        MapObject mapObject;
        public AsyncCaller(MapObject mapObject) {
            this.mapObject =mapObject;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            recordmarkercliked = ((MapMarker) mapObject);

            latitude = recordmarkercliked.getCoordinate().getLatitude();
            markerIdcliked = (Double.toString(latitude).replace(".", ""));
            /*poiLat = recordmarkercliked.getCoordinate().getLatitude();
            poiLng = recordmarkercliked.getCoordinate().getLongitude();*/
        }

        @Override
        protected Void doInBackground(Void... params) {

            DatabaseReference ShowIncidentValue = FirebaseDatabase.getInstance().getReference().child("Incidents").child(markerIdcliked);
            ShowIncidentValue.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                        java.util.Map<String, Object> map = (java.util.Map<String, Object>) dataSnapshot.getValue();

                        if(map.get("imageUrl")!=null){
                            String mProfileImageUrl = map.get("imageUrl").toString();
                            Glide.with(getApplication()).load(mProfileImageUrl).into(imageView);
                        }
                        if(map.get("timestamp")!=null){
                            String time = map.get("timestamp").toString();
                            timestamp.setText(time);
                        }
                        if(map.get("address")!=null){
                            String contact = map.get("address").toString();
                            address.setText(contact);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            LayPoiInfo.setVisibility(View.VISIBLE);
            btnReportIncident.setVisibility(View.INVISIBLE);
            imageButton.setVisibility(View.INVISIBLE);

            super.onPostExecute(result);

        }
    }

    private void addAllIncidentMarkers() {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        GeoFire geoFire = new GeoFire(ref);
        geoFire.getLocation("coordinates", new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location !=null) {
                    allincidentlat = location.latitude;
                    allincidentlong = location.longitude;
                }
                Image img = new Image();
                try {
                    img.setImageResource(R.drawable.scoo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mapMarker = new MapMarker();

                    mapMarker.setIcon(img);
                    mapMarker.setCoordinate(new GeoCoordinate(allincidentlat,allincidentlong));
                    map.addMapObject(mapMarker);
                }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        GeoCoordinate geoCoordinate=new GeoCoordinate(allincidentlat,allincidentlong);
        mapMarker = new MapMarker();
        mapMarker.setCoordinate(geoCoordinate);
        map.addMapObject(mapMarker);
        m_mapObjectList.add(mapMarker);



        DatabaseReference addIncidentRef = FirebaseDatabase.getInstance().getReference().child("ParkingZone");
        GeoCoordinate geoCoordinateParking1=new GeoCoordinate(19.136248, 73.020805);
        GeoCoordinate geoCoordinateParking2=new GeoCoordinate(19.145303, 72.996523);

        addIncidentRef.child("Parkinglat1").setValue(true);
        addIncidentRef.child("Parkinglong1").setValue(true);
        addIncidentRef.child("Parkinglat2").setValue(true);
        addIncidentRef.child("Parkinglong2").setValue(true);

        HashMap map1= new HashMap();

        map1.put("Parkinglat1","19.136248");
        map1.put("Parkinglong1","73.020805");
        map1.put("Parkinglat2","19.145303");
        map1.put("Parkinglong2","72.996523");
        addIncidentRef.updateChildren(map1);

        Image img1 = new Image();
        Image img2 = new Image();
        try {
            img1.setImageResource(R.drawable.parking);
            img2.setImageResource(R.drawable.parking);
        } catch (IOException e) {
            e.printStackTrace();
        }

        parking1 = new MapMarker();
        parking1.setCoordinate(geoCoordinateParking1);

        parking2 = new MapMarker();
        parking2.setCoordinate(geoCoordinateParking2);
        parking1.setIcon(img1);
        parking2.setIcon(img2);
        map.addMapObject(parking1);
        map.addMapObject(parking2);



        m_mapObjectList.add(parking1);
        m_mapObjectList.add(parking2);
    }





    public void getLiveCoordinates()
    {
        if (mPositioningManager!=null)
        {
            pickupLocation = mPositioningManager.getPosition().getCoordinate();
            latitude=pickupLocation.getLatitude();
            longitude=pickupLocation.getLongitude();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            GeoFire geoFire = new GeoFire(ref);
            geoFire.setLocation("coordinates",new GeoLocation(latitude,longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    Log.e("TAG", "GeoFire Complete");
                }
            });
        }
        else
        {
            Toast.makeText(this, "Please wait for position fix", Toast.LENGTH_SHORT).show();
        }
    }

    public void Opendashboard() {
        Intent intent = new Intent(this, AddIncident.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<>();
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (missingPermissions.isEmpty()) {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        } else {
            final String[] permissions = missingPermissions.toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Required permission '" + permissions[index] + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                initialize();
                break;
        }
    }

    private void initialize() {

// Search for the map fragment to finish setup by calling init().
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);
// Set up disk cache path for the map service for this application
// It is recommended to use a path under your application folder for storing the disk cache
        boolean success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath(
                getApplicationContext().getExternalFilesDir(null) + File.separator + ".here-maps",
                "{YOUR_INTENT_NAME}"); /* ATTENTION! Do not forget to update {YOUR_INTENT_NAME} */
        if (!success) {
            Toast.makeText(getApplicationContext(), "Unable to set isolated disk cache path.",
                    Toast.LENGTH_LONG);
        } else {
            mapFragment.init(new OnEngineInitListener() {
                @Override
                public void onEngineInitializationCompleted(Error error) {
                    if (error == Error.NONE) {
    // retrieve a reference of the map from the map fragment
                        map = mapFragment.getMap();
    // Set the map center to the Vancouver region (no animation)
                        map.setCenter(new GeoCoordinate(19.1756161, 72.9926607, 21),
                                Map.Animation.NONE);
    // Set the zoom level to the average between min and max
                        map.setZoomLevel(17);

                        map.addTransformListener(MapActivity.this);
                        mPositioningManager = PositioningManager.getInstance();
                        mHereLocation = LocationDataSourceHERE.getInstance(
                                new StatusListener() {
                                    @Override
                                    public void onOfflineModeChanged(boolean offline) {
                                        // called when offline mode changes
                                    }

                                    @Override
                                    public void onAirplaneModeEnabled() {
                                        // called when airplane mode is enabled
                                    }

                                    @Override
                                    public void onWifiScansDisabled() {
                                        // called when Wi-Fi scans are disabled
                                    }

                                    @Override
                                    public void onBluetoothDisabled() {
                                        // called when Bluetooth is disabled
                                    }

                                    @Override
                                    public void onCellDisabled() {
                                        // called when Cell radios are switch off
                                    }

                                    @Override
                                    public void onGnssLocationDisabled() {
                                        // called when GPS positioning is disabled
                                    }

                                    @Override
                                    public void onNetworkLocationDisabled() {
                                        // called when network positioning is disabled
                                    }

                                    @Override
                                    public void onServiceError(ServiceError serviceError) {
                                        // called on HERE service error
                                    }

                                    @Override
                                    public void onPositioningError(PositioningError positioningError) {
                                        // called when positioning fails
                                    }

                                    @Override
                                    public void onWifiIndoorPositioningNotAvailable() {

                                    }

                                    @Override
                                    public void onWifiIndoorPositioningDegraded() {

                                    }
                                });
                        if (mHereLocation == null) {
                            Toast.makeText(MapActivity.this, "LocationDataSourceHERE.getInstance(): failed, exiting", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        mPositioningManager.setDataSource(mHereLocation);
                        mPositioningManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(positionListener));
                        // start position updates, accepting GPS, network or indoor positions
                        if (mPositioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK)) {
                            if (mPositioningManager != null) {

                                pickupLocation = mPositioningManager.getPosition().getCoordinate();

                                mapFragment.getPositionIndicator().setVisible(true);
                            }

                        } else {
                            Toast.makeText(MapActivity.this, "PositioningManager.start: failed, exiting", Toast.LENGTH_LONG).show();
                            // finish();
                        }

                    } else {
                        System.out.println("ERROR: Cannot initialize Map Fragment");
                    }
                    mapFragment.getMapGesture().addOnGestureListener(onGestureListenernew, 1, true);
                }
            });
        }
    }

    private PositioningManager.OnPositionChangedListener positionListener = new
            PositioningManager.OnPositionChangedListener() {
                @Override
                public void onPositionUpdated(final PositioningManager.LocationMethod locationMethod, final GeoPosition geoPosition, final boolean mapMatched) {
                    final GeoCoordinate coordinate = geoPosition.getCoordinate();

                    if (mTransforming) {
                        mPendingUpdate = new Runnable() {
                            @Override
                            public void run() {
                                onPositionUpdated(locationMethod, geoPosition, mapMatched);

                            }
                        };
                    } else {
                        map.setCenter(coordinate, Map.Animation.BOW);
                    }
                }

                @Override
                public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {
                    // ignored
                }
            };

    @Override
    public void onMapTransformStart() {
        mTransforming = true;
    }


    @Override
    public void onMapTransformEnd(MapState mapState) {
        mTransforming = false;
        if (mPendingUpdate != null) {
            mPendingUpdate.run();
            mPendingUpdate = null;
        }
    }

    // Resume positioning listener on wake up
    public void onResume() {
        super.onResume();
        paused = false;
        if (mPositioningManager != null) {
            mPositioningManager.start(
                    PositioningManager.LocationMethod.GPS_NETWORK);
        }
    }

    // To pause positioning listener
    public void onPause() {
        if (mPositioningManager != null) {
            mPositioningManager.stop();
        }
        super.onPause();
        paused = true;
    }

    // To remove the positioning listener
    public void onDestroy() {
        if (mPositioningManager != null) {
            // Cleanup
            mPositioningManager.removeListener(
                    positionListener);
        }
        map = null;
        super.onDestroy();
    }
    private void erasePolylines()
    {
        if (map != null && Maproute != null) {
            map.removeMapObject(Maproute);
            Maproute = null;
        }
    }
    private void removeMarkers()
    {
        if (m_mapObjectList != null) {
            map.removeMapObjects(m_mapObjectList);
            mapMarker = null;
            parking1=null;
            parking2=null;
        }
    }
}

