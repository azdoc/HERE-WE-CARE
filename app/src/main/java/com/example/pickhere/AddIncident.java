package com.example.pickhere;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.Location;
import com.here.android.mpa.search.ResultListener;
import com.here.android.mpa.search.ReverseGeocodeRequest;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class AddIncident extends AppCompatActivity {

    private Button btnSubmitIncident;
    private ImageView imageView;
    private static final int CAMERA_REQUEST_CODE = 1;
    private StorageReference mStorage;
    private ProgressDialog mProgress;
    private StorageReference filepath;
    private EditText txtAddDescription;
    private TextView livecoordinate,incidentAddress,textView;
    private String userID,address;
    private FirebaseAuth mAuth;
    private double latitude,longitude;
    private Uri downloadUrl;
    private MapMarker mapMarker;
    private SimpleDateFormat sdf;
    private String currentDateandTime,incidentId;;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_incident);
        livecoordinate = findViewById(R.id.coordinates);
        txtAddDescription=findViewById(R.id.addDescription);
        incidentAddress=findViewById(R.id.address);

        /*getLatitude= getIntent().getExtras().getString("Latitude");
        getLongitude=getIntent().getExtras().getString("Longitude");*/

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        btnSubmitIncident=findViewById(R.id.submitIncident);
        imageView =findViewById(R.id.captureimage);

         textView=findViewById(R.id.date);
         sdf = new SimpleDateFormat("'Date:' yyyy-MM-dd ' Time:' HH:mm");
         currentDateandTime = sdf.format(new Date());
        textView.setText(currentDateandTime);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        GeoFire geoFire = new GeoFire(ref);
        geoFire.getLocation("coordinates", new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                latitude=location.latitude;
                longitude=location.longitude;
                livecoordinate.setText("Lat/Long : "+latitude+","+longitude);
                triggerRevGeocodeRequest();
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation("coordinates",new GeoLocation(location.latitude,location.longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        Log.e("TAG", "GeoFire Complete");
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnSubmitIncident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitIncident();
                Toast.makeText(AddIncident.this, "Congrats you have earned 10 points", Toast.LENGTH_SHORT).show();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStorage = FirebaseStorage.getInstance().getReference();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });
    }

    private void submitIncident() {
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        DatabaseReference addIncidentRef = FirebaseDatabase.getInstance().getReference().child("Incidents");

        incidentId = (Double.toString(latitude).replace(".", ""));

        addIncidentRef.child(incidentId).setValue(true);

                    HashMap map= new HashMap();
                    GeoFire geoFire = new GeoFire(addIncidentRef.child(incidentId));
                    geoFire.setLocation("coordinates",new GeoLocation(latitude,longitude), (key, error) -> Log.e("TAG", "GeoFire Complete"));
                    { map.put("rulecode","Illegal Parking");}
                    if(txtAddDescription.getText().toString()!=null)
                    { map.put("description",txtAddDescription.getText().toString());}
                    if (downloadUrl!=null)
                    { map.put("imageUrl", downloadUrl.toString());}
                    if (userID!=null)
                    { map.put("userID", userID);}
                    if (address!=null)
                    { map.put("address",address);}
                    addIncidentRef.child(incidentId).updateChildren(map);
                    if (textView.getText().toString()!=null)
                    {map.put("timestamp",textView.getText().toString());}

        addIncidentRef.child(incidentId).updateChildren(map);
        Toast.makeText(AddIncident.this, "success", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(AddIncident.this, MapActivity.class);// New activity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }



    private void triggerRevGeocodeRequest() {
        /* Create a ReverseGeocodeRequest object with a GeoCoordinate. */
        GeoCoordinate coordinate = new GeoCoordinate(latitude,longitude);
        ReverseGeocodeRequest revGecodeRequest = new ReverseGeocodeRequest(coordinate);
        revGecodeRequest.execute(new ResultListener<Location>() {
            @Override
            public void onCompleted(Location location, ErrorCode errorCode) {
                if (errorCode == ErrorCode.NONE) {
                    /*
                     * From the location object, we retrieve the address and display to the screen.
                     * Please refer to HERE Android SDK doc for other supported APIs.
                     */
                    address=updateTextView(location.getAddress().toString());

                } else {
                    updateTextView("ERROR:RevGeocode Request returned error code:" + errorCode);
                }
            }
        });
    }

    private String updateTextView(final String txt) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                incidentAddress.setText("City : "+txt);
            }
        });
        return txt;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            //set the progress dialog
            mProgress = new ProgressDialog(this);
            mProgress.setMessage("Uploding image...");
            mProgress.show();
            Uri uri = data.getData();
            //get the camera image
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] databaos = baos.toByteArray();

            //set the image into imageview
            imageView.setImageBitmap(bitmap);
            //String img = "fire"

            //name of the image file (add time to have different files to avoid rewrite on the same file)
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            filepath = mStorage.child("Photos").child(userID);
            //send this name to database
            //upload image
            UploadTask uploadTask = filepath.putBytes(databaos);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful());
                    downloadUrl = urlTask.getResult();
                    mProgress.dismiss();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddIncident.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            mProgress.dismiss();
                        }
                    });
        }
        else
        {
            finish();
        }
    }
}
