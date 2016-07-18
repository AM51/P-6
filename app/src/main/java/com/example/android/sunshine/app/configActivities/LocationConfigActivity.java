//package com.example.android.sunshine.app.configActivities;
//
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.net.Uri;
//import android.os.Bundle;
//import android.app.Activity;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.wearable.companion.WatchFaceCompanion;
//import android.util.Log;
//
//import com.example.android.sunshine.app.Utility;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.ResultCallback;
//import com.google.android.gms.wearable.DataApi;
//import com.google.android.gms.wearable.DataItem;
//import com.google.android.gms.wearable.DataMap;
//import com.google.android.gms.wearable.DataMapItem;
//import com.google.android.gms.wearable.Wearable;
//
//public class LocationConfigActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
//        ResultCallback<DataApi.DataItemResult>
//{
//
//    private static final String TAG = "DigitalWatchFaceConfig";
//    private String mPeerId;
//    private static final String PATH_WITH_FEATURE = "/watch_face_config/Digital";
//    private GoogleApiClient mGoogleApiClient;
//
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_location_config);
//        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(Wearable.API)
//                .build();
//
//    }
//
//    @Override
//    protected void onStop() {
//        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.disconnect();
//        }
//        super.onStop();
//    }
//
//    @Override
//    public void onConnected(@Nullable Bundle connectionHint) {
//        if (Log.isLoggable(TAG, Log.DEBUG)) {
//            Log.d(TAG, "onConnected: " + connectionHint);
//        }
//
//        if (mPeerId != null) {
//            Uri.Builder builder = new Uri.Builder();
//            Uri uri = builder.scheme("wear").path(PATH_WITH_FEATURE).authority(mPeerId).build();
//            Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(this);
//        } else {
//            displayNoConnectedDeviceDialog();
//        }
//    }
//
//    private void displayNoConnectedDeviceDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        String messageText = getResources().getString(R.string.title_no_device_connected);
//        String okText = getResources().getString(R.string.ok_no_device_connected);
//        builder.setMessage(messageText)
//                .setCancelable(false)
//                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) { }
//                });
//        AlertDialog alert = builder.create();
//        alert.show();
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//
//    }
//
//    @Override
//    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
//
//        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
//
//            String location = Utility.getPreferredLocation(getApplicationContext());
//
//            DataMap config = new DataMap();
//            config.putString("location", location);
//            byte[] rawData = config.toByteArray();
//            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, PATH_WITH_FEATURE, rawData);
//
//        } else {
//            // If DataItem with the current config can't be retrieved, select the default items on
//            // each picker.
//        }
//
//    }
//}
