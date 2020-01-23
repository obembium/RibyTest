package ng.riby.androidtest.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.DateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import ng.riby.androidtest.BuildConfig;
import ng.riby.androidtest.MainActivity;

public class LocationClass {

    public static String TAG="ssssssssss";
    Context cs;

    // location last updated time
    private String mLastUpdateTime;

    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    // fastest updates interval - 5 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    private static final int REQUEST_CHECK_SETTINGS = 100;

    // bunch of location related apis
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    // boolean flag to toggle the ui
    private Boolean mRequestingLocationUpdates;


    public void init(final Context context, final Activity activity) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mSettingsClient = LocationServices.getSettingsClient(context);


        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateLocationUI(context,activity);
            }
        };
        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
       startLocationButtonClick(context,activity);
    }

    public void updateLocationUI(Context context,Activity activity) {
        if (mCurrentLocation != null) {
            float lati=(float) mCurrentLocation.getLatitude();
            float longi=(float) mCurrentLocation.getLongitude();
            PH.get().setFloat(context, "Lati", lati);
            PH.get().setFloat(context, "Longi", longi);
            call(context);
          //  Message.appendLog("log="+longi+" Lat="+lati+" Date="+ Message.getDateTime());
         //   stopLocationUpdates(context,activity);
        }
    }

    private static void call(Context context) {
        String ClientName = "RIBY";
        float Longitude = PH.get().getFloat(context, "Longi", 0);
        float Latitude = PH.get().getFloat(context, "Lati", 0);
        boolean Online = Message.isNetworkAvailable(context);
        MainActivity.noteRepository.insertTask(ClientName, Longitude, Latitude, Online);
        Message.appendLog2("CN: "+ClientName+
                " Long: "+Longitude+
                " Lat: "+Latitude+
                " Online: "+Online);
    }

    public void startLocationUpdates(final Context context, final Activity activity) {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                    //    Toast.makeText(context, "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocationUI(context,activity);
                    }
                })
                .addOnFailureListener( activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult( activity, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                              //  Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        updateLocationUI(context,activity);
                    }
                });
    }

    public void startLocationButtonClick(final Context context, final Activity activity) {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(activity)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates(context,activity);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings(context);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    public void stopLocationUpdates(Context context,Activity activity) {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }
    private void openSettings(Context context) {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       context.startActivity(intent);
    }


    public static class getLocation {

        static GoogleApiClient mGoogleApiClient = null;

        public static void get(final Context context, Activity activity) {
            LocationClass locationClass=new LocationClass();
            locationClass.init(context,activity);
            // Requesting ACCESS_FINE_LOCATION using Dexter library
            Dexter.withActivity(activity)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            // checkGPS(context);
                            // if (mGoogleApiClient == null) {
                            mGoogleApiClient = new GoogleApiClient.Builder(context)
                                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                                        @Override
                                        public void onConnected(@Nullable Bundle bundle) {
                                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                return;
                                            }
                                            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                                            if (mLastLocation != null) {
                                                //   Message.message1(context, "Latest located updated");
                                               PH.get().setFloat(context, "Lati", (float) mLastLocation.getLatitude());
                                                PH.get().setFloat(context, "Longi", (float) mLastLocation.getLongitude());
                                                call(context);
                                               } else {

                                            }
                                        }

                                        @Override
                                        public void onConnectionSuspended(int i) {

                                        }
                                    })
                                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                                        @Override
                                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                                         }
                                    })
                                    .addApi(LocationServices.API)
                                    .build();

                            if (mGoogleApiClient != null) {
                                mGoogleApiClient.connect();
                            }
                           /* }
                            if (mGoogleApiClient != null) {
                                Log.d(TAG, "getLocation2: b");
                                mGoogleApiClient.connect();
                            }*/
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            if (response.isPermanentlyDenied()) {
                                // open device settings when the permission is
                                // denied permanently
                                openSettings(context);
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();


        }


        private static void openSettings(Context context) {
            Intent intent = new Intent();
            intent.setAction(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package",
                    BuildConfig.APPLICATION_ID, null);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }






    }
}
