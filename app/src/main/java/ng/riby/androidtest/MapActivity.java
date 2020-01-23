package ng.riby.androidtest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import ng.riby.androidtest.room.model.Note;
import ng.riby.androidtest.utils.LocationViewModel;
import ng.riby.androidtest.utils.Message;
import ng.riby.androidtest.utils.PH;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView locationText;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private Context mContext;
    private LocationViewModel locationViewModel;
    private Location locationObj;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mContext = this;
        setupViews();
        setupViewModel();
    }

    private void setupViewModel() {
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);

    }

    public void checkLocationPermission() {
        int hasWriteStoragePermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasWriteStoragePermission = getApplicationContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CHECK_SETTINGS);
                return;
            }
          //  subscribeToLocationUpdate();
        } else {
           // subscribeToLocationUpdate();
        }
    }

    private void setupViews() {
        Button startLocation = findViewById(R.id.button_start_location);
        Button stopLocation = findViewById(R.id.button_stop_location);

        locationText = findViewById(R.id.locationText);

        startLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeToLocationUpdate();
            }
        });

        stopLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocationUpdates();
            }
        });

        //Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void stopLocationUpdates() {
        locationViewModel.getLocationHelper(mContext).stopLocationUpdates();
        try {
            Exportdatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Exportdatabase() throws IOException {

        //Open your local db as the input stream
        String inFileName = "/data/data/ng.riby.androidtest/databases/db_riby";
        File dbFile = new File(inFileName);
        FileInputStream fis = new FileInputStream(dbFile);

        String outFileName = Environment.getExternalStorageDirectory()+"/Peter_Ihaza_Test.db";
        //Open the empty db as the output stream
        OutputStream output = new FileOutputStream(outFileName);
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer))>0){
            output.write(buffer, 0, length);
        }
        Message.message(getApplicationContext(),"Database has been exported to phone storage as Peter_Ihaza_Test");
        //Close the streams
        output.flush();
        output.close();
        fis.close();
   }



    private void subscribeToLocationUpdate() {
        locationViewModel.getLocationHelper(mContext).observe(this, new Observer<Location>() {

            @Override
            public void onChanged(@Nullable Location location) {
                Toast.makeText(mContext, "on changed called", Toast.LENGTH_SHORT).show();
                locationObj = location;
                locationText.setText(location.getLatitude() + " " + location.getLongitude());

                plotMarkers(locationObj);
            }
        });
    }

    ArrayList<Location> locationArrayList = new ArrayList<>();
    private void plotMarkers(Location locationObj) {
        Log.d("Location list start", "plotMarkers: "+locationArrayList.size());
        if(map != null){
            LatLng current = new LatLng(locationObj.getLatitude(), locationObj.getLongitude());
            map.addMarker(new MarkerOptions().position(current));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
            map.moveCamera(CameraUpdateFactory.newLatLng(current));
            map.animateCamera(zoom);



            locationArrayList.add(locationObj);
            Log.d("Location list", "plotMarkers: "+locationArrayList.size());

            //Draw Line
            LatLng singleLatLong = null;
            ArrayList<LatLng> pnts = new ArrayList<LatLng>();
            if(locationArrayList != null) {
                for(int i = 0 ; i < locationArrayList.size(); i++) {
                    double routePoint1Lat = locationArrayList.get(i).getLatitude();
                    double routePoint2Long = locationArrayList.get(i).getLongitude();
                    singleLatLong = new LatLng(routePoint1Lat,
                            routePoint2Long);
                    pnts.add(singleLatLong);


                    map.addPolyline(new PolylineOptions().
                            addAll(pnts)
                            .width(1)
                            .color(Color.BLUE)
                            .zIndex(1));
                }
                //locationArrayList = new ArrayList<>();
                locationArrayList = new ArrayList<>();
            }



        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        //subscribeToLocationUpdate();
                    }

                } else {

                    // permission denied
                }
                return;
            }

        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        displayPath();
    }


    private void displayPath() {
        if(locationArrayList.size()==0) {

            MainActivity.noteRepository.getTasks().observe(this, new Observer<List<Note>>() {
                @Override
                public void onChanged(@Nullable List<Note> notes) {
                    int noteSize = 0;
                    for (Note note : notes) {
                        noteSize++;
                        locationObj = new Location(LocationManager.NETWORK_PROVIDER);
                        locationObj.setLatitude(note.getLatitude());
                        locationObj.setLongitude(note.getLongitude());
                        locationArrayList.add(locationObj);
                        if (notes.size() == noteSize) {
                            locationObj = new Location(LocationManager.NETWORK_PROVIDER);
                            locationObj.setLatitude(PH.get().getFloat(getApplicationContext(), "Lati", 0));
                            locationObj.setLongitude(PH.get().getFloat(getApplicationContext(), "Longi", 0));
                            plotMarkers(locationObj);
                        }
                    }
                }
            });

        }

    }
}
