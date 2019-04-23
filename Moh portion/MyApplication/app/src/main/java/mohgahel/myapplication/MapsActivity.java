package mohgahel.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.media.Image;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

//import com.bumptech.glide.Glide;
import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap map;
    Location lastLocation;
    LocationRequest locationRequest;

    private FusedLocationProviderClient fusedLocationClient;
    private List<Polyline> polylines;
    private Button logout;
    private String customerID = "", destination;
    private LatLng destLatLng, pickupLatLng;
    private float rideDistance;
    private int status = 0;
    String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private CardView customerInfo;
    private ImageView customerProfilePic, driverProfilePic;
    private TextView customerName, customerPhone, customerDestination, customerFee;


    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        polylines = new ArrayList<>();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        customerInfo = (CardView) findViewById(R.id.customerInfo);
        customerProfilePic = (ImageView) findViewById(R.id.customerProfilePic);
        customerName = (TextView) findViewById(R.id.customerName);
        customerPhone = (TextView) findViewById(R.id.customerPhone);
        customerDestination = (TextView) findViewById(R.id.customerDestination);
        customerFee = (TextView) findViewById(R.id.customerFee);
        driverProfilePic = (ImageView) findViewById(R.id.driverProfilePic);
        logout = (Button) findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MapsActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Logging out")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                disconnectDriver();
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                                return; }
                                })
                        .setNegativeButton("No", null)
                        .show();

/*
                disconnectDriver();

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;*/
            }
        });
        DatabaseReference customerCost = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID);
        customerCost.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("profileImageUrl") != null) {
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(driverProfilePic);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        getAssignedClient();
        getRideCost();
    }


    //determine the customer(s) for which the driver will be servicing
    private void getAssignedClient() {
        String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedClients = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID).child("customerRideId");
        assignedClients.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    status = 1;
                    customerID = dataSnapshot.getValue().toString();
                    getAssignedClientPickUpLocation();//determine location
                    getAssignedClientDestination();//determine destination
                    getAssignedClientInfo();//determine information
                    getRideCost();
                }else {
                    endRide();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    Marker pickupMarker, dropOffMarker;
    private DatabaseReference assignedCustomerPickupLocationRef;
    private ValueEventListener assignedCustomerPickupLocationRefListener;

    private void getAssignedClientPickUpLocation() {
        //retrieve customer location for pickup from database
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("CustomerPickupLocation").child(customerID).child("l");
        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !customerID.equals("")) {
                    List<Object> mapList = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(mapList.get(0) != null) {
                        locationLat = Double.parseDouble(mapList.get(0).toString());
                    }
                    if(mapList.get(1) != null) {
                        locationLng = Double.parseDouble(mapList.get(1).toString());
                    }
                    pickupLatLng = new LatLng(locationLat,locationLng);//set coordinates for pickup
                    //set map marker on coordinates
                    pickupMarker = map.addMarker(new MarkerOptions().position(pickupLatLng).title("Customer Pickup Location"));//icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                    getRouteToMarker(pickupLatLng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //display path to marker
    private void getRouteToMarker(LatLng pickupLatLng) {
        if (pickupLatLng != null && lastLocation != null) {
            Routing routing = new Routing.Builder()
                    .key("AIzaSyAw9j67DZdCh6Du6iZgqCj0mD-V3aK6WHQ")
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), pickupLatLng)
                    .build();
            routing.execute();
        }
    }

    //determine the customer's destination
    private void getAssignedClientDestination() {
        //String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedClientRef = FirebaseDatabase.getInstance().getReference().child("CustomerDropOffLocation").child(customerID).child("l");//.child("Users").child("Drivers").child(driverID);
        assignedClientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    /*Map<String, Object> mapKey = (Map<String, Object>) dataSnapshot.getValue();
                    if(mapKey.get("destination") != null) {
                        destination = mapKey.get("destination").toString();
                    }
                    else {

                    }

                    Double destLat = 0.0;
                    Double destLng;
                    if(mapKey.get("destLat") != null) {
                        destLat = Double.valueOf(mapKey.get("destLat").toString());
                    }
                    if(mapKey.get("destLng") != null) {
                        destLng = Double.valueOf(mapKey.get("destLng").toString());
                        destLatLng = new LatLng(destLat, destLng);

                    }*/

                    List<Object> mapList = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(mapList.get(0) != null) {
                        locationLat = Double.parseDouble(mapList.get(0).toString());
                    }
                    if(mapList.get(1) != null) {
                        locationLng = Double.parseDouble(mapList.get(1).toString());
                    }
                    pickupLatLng = new LatLng(locationLat,locationLng);//set coordinates for dropoff
                    //set map marker on coordinates
                    dropOffMarker = map.addMarker(new MarkerOptions().position(pickupLatLng).title("Customer Drop-Off Location"));//icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                    //getRouteToMarker(pickupLatLng);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getAssignedClientInfo() {
        customerInfo.setVisibility(View.VISIBLE);
        DatabaseReference customerData = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(customerID);
        customerData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("First Name") != null) {
                        customerName.setText(map.get("First Name").toString());
                    }
                    if(map.get("Phone Number") != null) {
                        customerPhone.setText(map.get("Phone Number").toString());
                    }
                    if(map.get("profileImageUrl") != null) {
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(customerProfilePic);
                    }
                    if(map.get("Destination") != null) {
                        customerDestination.setText(map.get("Destination").toString());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void getRideCost() {
        DatabaseReference customerCost = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID);
        customerCost.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("amountToBePaid") != null) {
                        customerFee.setText(map.get("amountToBePaid").toString());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void endRide() {

        String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID).child("customerRequest");
        driverRef.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerPickupLocation");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(customerID);
        customerID="";
        rideDistance = 0;

        if(pickupMarker != null){
            pickupMarker.remove();
            dropOffMarker.remove();
        }

        if (assignedCustomerPickupLocationRefListener != null){
            assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
        }
        customerInfo.setVisibility(View.GONE);
        customerName.setText("");
        customerPhone.setText("");
        customerDestination.setText("");
        customerFee.setText("");
        //customerDestination.setText("Destination: --");
        //customerProfilePic.setImageResource(R.mipmap.ic_default_user);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {   //displays the map with your location
        map = googleMap;

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                map.setMyLocationEnabled(true);
            }else{
                checkLocationPermission();
            }
        }

        /*
        LatLng ottAirport = new LatLng(45.32, -75.66);
        mMap.addMarker(new MarkerOptions().position(ottAirport).title("Ottawa International Airport"));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(ottAirport).zoom(14.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.moveCamera(cameraUpdate);
        */
    }

    //get permission to use location services
    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Give permission?")
                        .setMessage("Give permission to access location?")
                        .setPositiveButton("YES ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }


    //opens driver display when pressed
    public void driverProfile(View view) {
        Intent intent = new Intent(this, driverProfile.class);
        startActivity(intent);
    }

    //constantly updates driver position on the map
    LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()) {
                if(getApplicationContext()!= null) {
                    if(!customerID.equals("") && lastLocation != null && location != null){
                        rideDistance += lastLocation.distanceTo(location)/1000;
                    }
                    lastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    map.animateCamera(CameraUpdateFactory.zoomTo(12));

                    //String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference availability = FirebaseDatabase.getInstance().getReference("driverAvailable");
                    DatabaseReference working = FirebaseDatabase.getInstance().getReference("driverWorking");
                    GeoFire geoFireAvail = new GeoFire(availability);
                    GeoFire geoFireWork = new GeoFire(working);

                    switch(customerID){
                        case "":
                            geoFireWork.removeLocation(driverID);
                            geoFireAvail.setLocation(driverID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;

                        default:
                            geoFireAvail.removeLocation(driverID);
                            geoFireWork.setLocation(driverID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;
                    }
                }
            }
        }


    };

    public void disconnectDriver(){
        if(fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driverAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(driverID);
    }


    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Unknown error, please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {
    }
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRoute) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = map.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {
    }

    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }

}
