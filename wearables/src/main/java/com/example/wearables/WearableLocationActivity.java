//package com.example.wearables;
//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.os.Bundle;
//import android.app.Activity;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.ActivityCompat;
//import android.util.Log;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.Result;
//import com.google.android.gms.common.api.ResultCallback;
//import com.google.android.gms.common.api.Status;
//import com.google.android.gms.location.LocationListener;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.wearable.Wearable;
//
//public class WearableLocationActivity extends Activity implements
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener,
//        LocationListener {
//
//    private GoogleApiClient mGoogleApiClient;
//    private String TAG = getClass().getName();
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_wearable_location);
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(LocationServices.API)
//                .addApi(Wearable.API)  // used for data layer API
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .build();
//
//    }
//
//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//
//        LocationRequest locationRequest = LocationRequest.create()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(UPDATE_INTERVAL_MS)
//                .setFastestInterval(FASTEST_INTERVAL_MS);
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        LocationServices.FusedLocationApi
//                .requestLocationUpdates(mGoogleApiClient, locationRequest, this)
//                .setResultCallback(new ResultCallback() {
//
//
//                    @Override
//                    public void onResult(@NonNull Result result) {
//                        Status status = result.getStatus();
//                        if (status.getStatus().isSuccess()) {
//                            if (Log.isLoggable(TAG, Log.DEBUG)) {
//                                Log.d(TAG, "Successfully requested location updates");
//                            }
//                        } else {
//                            Log.e(TAG,
//                                    "Failed in requesting location updates, "
//                                            + "status code: "
//                                            + status.getStatusCode()
//                                            + ", message: "
//                                            + status.getStatusMessage());
//                        }
//                    }
//
//
//                });
//
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        if (Log.isLoggable(TAG, Log.DEBUG)) {
//            Log.d(TAG, "connection to location client suspended");
//        }
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mGoogleApiClient.connect();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (mGoogleApiClient.isConnected()) {
//            LocationServices.FusedLocationApi
//                    .removeLocationUpdates(mGoogleApiClient, this);
//        }
//        mGoogleApiClient.disconnect();
//    }
//}
