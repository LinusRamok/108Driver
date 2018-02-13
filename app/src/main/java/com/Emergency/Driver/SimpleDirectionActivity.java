package com.Emergency.Driver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.Emergency.Driver.Models.Locate;
import com.Emergency.Driver.Models.Patient;
import com.Emergency.Driver.Models.Patients;
import com.Emergency.Driver.Models.ambStatus;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;


public class SimpleDirectionActivity extends AppCompatActivity implements
        OnMapReadyCallback ,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
        {

    private LocationCallback mLocationCallback;
    int patientUpdateCnt=0,mylocUpdateCnt=0;

    Location mLastLocation;
    Marker ambCurrLocationMarker;
    Marker currPatientMarker;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;


    private Button btnRequestDirection;
    private GoogleMap googleMap;
    private String serverKey = "AIzaSyDi3B9R9hVpC9YTmOCCz_pCR1BKW3tIRGY";
  //      private LatLng origin = new LatLng(21.218681, 80.307411);
  //  private LatLng destination = new LatLng(21.212248, 81.316434);
    TextView distance, duration,mylonglat,amblonglat;
    private LatLng origin = null;
    private LatLng destination = null;

    private Patient patient=null;
    private String allotedPatient;
    private long patientCount=0;
     private Map<String,Patient> patientMap=new HashMap<>();
    DatabaseReference ambRef,ambLocRef,ambStatusRef,PatientsRef,allotedPatientref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_direction);
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        
        PatientsRef=database.getReference("patients");
        ambRef = database.getReference("ambulance");
        ambLocRef = ambRef.child("location");
        ambStatusRef = ambRef.child("ambStatus");
        allotedPatientref=ambStatusRef.child("status");


        distance = findViewById(R.id.distance);
        duration = findViewById(R.id.duration);
        mylonglat=findViewById(R.id.mylonglat);
        amblonglat=findViewById(R.id.amblonglat);


        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);


        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    mylocUpdateCnt+=1;
                    mylonglat.setText(mylocUpdateCnt + " my location :"+location.getLatitude()+","+location.getLongitude());

                    ambLocRef.setValue(new Locate(location.getLatitude(), location.getLongitude()));
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    mLastLocation = location;
                    if (ambCurrLocationMarker != null) {
                   //     Toast.makeText(getApplicationContext(),"my location changed",Toast.LENGTH_SHORT).show();
                        ambCurrLocationMarker.setPosition(latLng);

                    }else{
                        Toast.makeText(getApplicationContext(),"my location set",Toast.LENGTH_SHORT).show();
                        origin =new LatLng(location.getLatitude(),location.getLongitude());
                        Toast.makeText(SimpleDirectionActivity.this,location.getLatitude()+":"+location.getLongitude(),Toast.LENGTH_LONG).show();

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title("Amb Current Position");
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                        ambCurrLocationMarker = googleMap.addMarker(markerOptions);
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(40));
                    }


                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                }
            }
        };


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                googleMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            googleMap.setMyLocationEnabled(true);
        }

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Toast.makeText(SimpleDirectionActivity.this, "Marker Clicked :"+marker.getTag(), Toast.LENGTH_SHORT).show();
                System.out.println("marker tag :" + marker.getTag());

                //  marker.setSnippet(new Gson().toJson(marker));
                marker.getPosition();


                return false;
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }




    @Override
    protected void onStart() {
        super.onStart();
        //  requestDirection();
        checkLocationPermission();

        ambStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("ambStatusRef ,listener :"+dataSnapshot.toString());
                ambStatus AmbStatus=dataSnapshot.getValue(ambStatus.class);
                System.out.println("ambstatus :"+new Gson().toJson(AmbStatus));
                if(AmbStatus!=null && AmbStatus.getAllottedPatient()!=null){
                    allotedPatient=AmbStatus.getAllottedPatient();
                    System.out.println("alloted patient :"+allotedPatient);
                }else{
                    System.out.println("ambulance is free ....no patients");
                    Toast.makeText(getApplicationContext(), "no patients left", Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG", "Failed to read value.", databaseError.toException());
            }
        });

        PatientsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null) {
                    System.out.println("patientsref ,listener" + dataSnapshot.toString());
                    patientCount = dataSnapshot.getChildrenCount();
                    System.out.println("patient count :" + patientCount);


//                    System.out.println("patient list after :" + Arrays.deepToString(patientList.toArray()));
                }else{

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("patientsref ,listener :"+databaseError);
            }
        });

        PatientsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                System.out.println("on child added :");
                System.out.println("DataSnapshot :"+dataSnapshot);
                System.out.println(" s :"+s);
                if(patientCount==0){
                    ambStatusRef.setValue(new ambStatus("busy",100,dataSnapshot.getKey()));
                    Locate location = dataSnapshot.getValue(Patient.class).getLocate();
                    setPatientMarker(location);
                }
                patientCount+=1;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                System.out.println("on child changed :");
                System.out.println("DataSnapshot :"+dataSnapshot);
                System.out.println(" s :"+s);
                if(allotedPatient !=null){
                    if(dataSnapshot.getKey().equals(allotedPatient)){
                        try {
                            System.out.println("patient location changed....");

                            Locate location = dataSnapshot.getValue(Patient.class).getLocate();
                            setPatientMarker(location);
                            Log.d("on changed listener ,", "Patient location: " + new Gson().toJson(location));
                        }catch (Exception e) {
                            e.printStackTrace();

                        }
                    }
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                System.out.println("on child removed :");
                System.out.println("DataSnapshot :"+dataSnapshot);
                System.out.println("last alotted patient :"+allotedPatient);
                patientCount-=1;

                if(allotedPatient !=null) {
                    if (patientCount>0 && dataSnapshot.getKey().equals(allotedPatient)){
                        allotedPatient=patientMap.e
                        System.out.println("new alotted patient :"+allotedPatient);
                        ambStatus ambStatus =new ambStatus("busy",100,allotedPatient);
                        System.out.println("changing amb status....");
                        ambStatusRef.setValue(ambStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                System.out.println("ambStatusRef,change :"+task.getResult());
                                if(task.isSuccessful()){
                                    System.out.println("ambStatusRef,status changed");
                                }else{
                                    System.out.println("task unsuccess :"+task.getException());
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                System.out.println("on child moved :");
                System.out.println("DataSnapshot :"+dataSnapshot);
                System.out.println(" s :"+s);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError);
            }
        });



//        PatientsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                System.out.println("patientsref ,listener"+dataSnapshot.toString());
//                if(dataSnapshot.getChildrenCount()>0){
//                    System.out.println("last alotted patient :"+allotedPatient);
//                    allotedPatient=dataSnapshot.getChildren().iterator().next().getKey();
//                    System.out.println("new alotted patient :"+allotedPatient);
//                    ambStatus ambStatus =new ambStatus("busy",100,allotedPatient);
//                    System.out.println("changing amb status....");
//                    ambStatusRef.setValue(ambStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            System.out.println("ambStatusRef,change :"+task.getResult());
//                             if(task.isSuccessful()){
//                                 System.out.println("ambStatusRef,status changed");
//                             }else{
//                                 System.out.println("task unsuccess :"+task.getException());
//                             }
//                        }
//                    });
//
//                    Button button= findViewById(R.id.btn_TaskCompInform);
//                    button.setText("Task Completed");
//                    button.setClickable(true);
//                }else{
//                    System.out.println("ambulance is free");
//                    ambStatus ambStatus =new ambStatus("free",0,null);
//                    ambStatusRef.setValue(ambStatus);
//                    Button button= findViewById(R.id.btn_TaskCompInform);
//                    button.setText("No Task in Que");
//                    button.setClickable(false);
//                    distance.setText("---");
//                    duration.setText("---");
//                    if(currPatientMarker!=null) {
//                        currPatientMarker.remove();
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }



            void setPatientMarker(Locate location){
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                patientUpdateCnt += 1;
                amblonglat.setText(patientUpdateCnt + " patient:" + location.getLatitude() + "," + location.getLongitude());
                if (currPatientMarker != null) {
                    Toast.makeText(getApplicationContext(), "patient location changed...", Toast.LENGTH_SHORT).show();
                    System.out.println("patient marker already created....");
                    currPatientMarker.setPosition(latLng);
                } else {
                    Toast.makeText(getApplicationContext(), "patient location set", Toast.LENGTH_SHORT).show();
                    System.out.println("no marker ......creating");
                    destination = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("patient Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    currPatientMarker = googleMap.addMarker(markerOptions);
                }
            }


//            private Task<Void> getUser() {
//                final TaskCompletionSource<Void> tcs = new TaskCompletionSource();
//                System.out.println("inside getUser fn");
//                PatientsRef.child(allotedPatient).child("location").addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        System.out.println("here is patient data :"+dataSnapshot.toString());
//                        try {
//                            System.out.println("ambulance location changed....");
//
//                            Locate location = dataSnapshot.getValue(Locate.class);
//
//                            Log.d("TAG", "Value is: " + new Gson().toJson(location));
//
//                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//
//                            patientUpdateCnt += 1;
//                            amblonglat.setText(patientUpdateCnt + " patient:" + location.getLatitude() + "," + location.getLongitude());
//                            if (currPatientMarker != null) {
//                                Toast.makeText(getApplicationContext(), "patient location changed...", Toast.LENGTH_SHORT).show();
//                                System.out.println("patient marker already created....");
//                                currPatientMarker.setPosition(latLng);
//                            } else {
//                                Toast.makeText(getApplicationContext(), "patient location set", Toast.LENGTH_SHORT).show();
//                                System.out.println("no marker ......creating");
//                                destination = new LatLng(location.getLatitude(), location.getLongitude());
//                                MarkerOptions markerOptions = new MarkerOptions();
//                                markerOptions.position(latLng);
//                                markerOptions.title("patient Position");
//                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//                                currPatientMarker = googleMap.addMarker(markerOptions);
//                            }
//
//                        }catch (Exception e) {
//                            e.printStackTrace();
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError error) {
//                        // Failed to read value
//                        Log.w("TAG", "Failed to read value.", error.toException());
//                    }
//                });
//                return tcs.getTask();
//            }



    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
     //   mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
         //   LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,null );
         LocationServices.getFusedLocationProviderClient(getApplicationContext()).requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        googleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }


        public void TaskCompInform(View view) {
            System.out.println("current task completed button clked");
            System.out.println("alloted patient :"+allotedPatient);

            System.out.println("patientref :"+PatientsRef.child(allotedPatient).toString());
                PatientsRef.child(allotedPatient).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       if(task.isSuccessful()){
                           System.out.println("task success,current patient removed from list");
                           Toast.makeText(getApplicationContext(),"Last patient removed",Toast.LENGTH_SHORT).show();
                       }else{
                           System.out.println("task unsuccess,current patient not removed from list");
                           Toast.makeText(getApplicationContext(),"error",Toast.LENGTH_SHORT).show();
                       }
                    }
                });
        }

  }
