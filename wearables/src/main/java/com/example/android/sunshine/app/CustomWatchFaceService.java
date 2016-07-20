package com.example.android.sunshine.app;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.TimeZone;


/**
 * Created by archit.m on 16/07/16.
 */
public class CustomWatchFaceService extends CanvasWatchFaceService {


    @Override
    public Engine onCreateEngine() {
        Log.e("test","Engine cons");
        return new WatchFaceEngine();
    }

    private class WatchFaceEngine extends Engine implements DataApi.DataListener,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

        private Typeface WATCH_TEXT_TYPEFACE = Typeface.create( Typeface.SERIF, Typeface.NORMAL );

        private static final int MSG_UPDATE_TIME_ID = 42;
        private static final String PATH_WITH_FEATURE = "/watch_face_config/Digital";
        private long mUpdateRateMs = 1000;

        private Time mDisplayTime;

        private Paint mBackgroundColorPaint;
        private Paint mTextColorPaint;
        private Paint mTextColorPaintWeatherDetails;


        private boolean mHasTimeZoneReceiverBeenRegistered = false;
        private boolean mIsInMuteMode;
        private boolean mIsLowBitAmbient;

        private float mXOffset;
        private float mYOffset;

        private float mXOffsetWeatherDetails;
        private float mYOffsetWeatherDetails;

        private int mBackgroundColor = Color.parseColor( "red" );
        private int mTextColor = Color.parseColor( "blue" );

        private String location;

        private String TAG = "mittal";


        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(CustomWatchFaceService.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();


        final BroadcastReceiver mTimeZoneBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mDisplayTime.clear( intent.getStringExtra( "time-zone" ) );
                mDisplayTime.setToNow();
            }
        };

        private final Handler mTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch( msg.what ) {
                    case MSG_UPDATE_TIME_ID: {
                        invalidate();
                        if( isVisible() && !isInAmbientMode() ) {
                            long currentTimeMillis = System.currentTimeMillis();
                            long delay = mUpdateRateMs - ( currentTimeMillis % mUpdateRateMs );
                            mTimeHandler.sendEmptyMessageDelayed( MSG_UPDATE_TIME_ID, delay );
                        }
                        break;
                    }
                }
            }
        };


        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);


            Log.e("test","Creating Custom Service");
            //Log.v("archit","Inside on create");
            setWatchFaceStyle( new WatchFaceStyle.Builder( CustomWatchFaceService.this )
                    .setBackgroundVisibility( WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE )
                    .setCardPeekMode( WatchFaceStyle.PEEK_MODE_VARIABLE )
                    .setShowSystemUiTime( false )
                    .build()
            );

            mDisplayTime = new Time();

            initBackground();
            initDisplayText();
            initWeatherDetaialsText();
            mGoogleApiClient.connect();

        }

        private void initBackground() {
            mBackgroundColorPaint = new Paint();
            mBackgroundColorPaint.setColor( mBackgroundColor );
        }

        private void initDisplayText() {
            mTextColorPaint = new Paint();
            mTextColorPaint.setColor( mTextColor );
            mTextColorPaint.setTypeface( WATCH_TEXT_TYPEFACE );
            mTextColorPaint.setAntiAlias( true );
            mTextColorPaint.setTextSize( getResources().getDimension( R.dimen.text_size ) );
        }

        private void initWeatherDetaialsText() {
            mTextColorPaintWeatherDetails = new Paint();
            mTextColorPaintWeatherDetails.setColor( mTextColor );
            mTextColorPaintWeatherDetails.setTypeface( WATCH_TEXT_TYPEFACE );
            mTextColorPaintWeatherDetails.setAntiAlias( true );
            mTextColorPaintWeatherDetails.setTextSize( getResources().getDimension( R.dimen.text_size_weather ) );
        }


        @Override
        public void onVisibilityChanged( boolean visible ) {
            super.onVisibilityChanged(visible);

            if( visible ) {
                mGoogleApiClient.connect();

                if( !mHasTimeZoneReceiverBeenRegistered ) {

                    IntentFilter filter = new IntentFilter( Intent.ACTION_TIMEZONE_CHANGED );
                    CustomWatchFaceService.this.registerReceiver( mTimeZoneBroadcastReceiver, filter );

                    mHasTimeZoneReceiverBeenRegistered = true;
                }

                mDisplayTime.clear( TimeZone.getDefault().getID() );
                mDisplayTime.setToNow();
            } else {
                if( mHasTimeZoneReceiverBeenRegistered ) {
                    CustomWatchFaceService.this.unregisterReceiver( mTimeZoneBroadcastReceiver );
                    mHasTimeZoneReceiverBeenRegistered = false;
                }

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, this);
                    mGoogleApiClient.disconnect();
                }

            }

            updateTimer();
        }

        private void updateTimer() {
            mTimeHandler.removeMessages( MSG_UPDATE_TIME_ID );
            if( isVisible() && !isInAmbientMode() ) {
                mTimeHandler.sendEmptyMessage( MSG_UPDATE_TIME_ID );
            }
        }

        @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            mYOffset = getResources().getDimension( R.dimen.y_offset );
            mYOffsetWeatherDetails = getResources().getDimension( R.dimen.y_offset_weather );

            if( insets.isRound() ) {
                mXOffset = getResources().getDimension( R.dimen.x_offset_round );
                mXOffsetWeatherDetails = getResources().getDimension( R.dimen.x_offset_round_weather );
            } else {
                mXOffset = getResources().getDimension( R.dimen.x_offset_square );
                mXOffsetWeatherDetails = getResources().getDimension( R.dimen.x_offset_square_weather );
            }

        }

        @Override
        public void onPropertiesChanged( Bundle properties ) {
            super.onPropertiesChanged( properties );

            if( properties.getBoolean( PROPERTY_BURN_IN_PROTECTION, false ) ) {
                mIsLowBitAmbient = properties.getBoolean( PROPERTY_LOW_BIT_AMBIENT, false );
            }
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            if( inAmbientMode ) {
                mTextColorPaint.setColor( Color.parseColor( "white" ) );
            } else {
                mTextColorPaint.setColor( Color.parseColor( "red" ) );
            }

            if( mIsLowBitAmbient ) {
                mTextColorPaint.setAntiAlias( !inAmbientMode );
            }

            invalidate();
            updateTimer();
        }

//        @Override
//        public void onInterruptionFilterChanged(int interruptionFilter) {
//            super.onInterruptionFilterChanged(interruptionFilter);
//
//            boolean isDeviceMuted = ( interruptionFilter == android.support.wearable.watchface.WatchFaceService.INTERRUPTION_FILTER_NONE );
//            if( isDeviceMuted ) {
//                mUpdateRateMs = TimeUnit.MINUTES.toMillis( 1 );
//            } else {
//                mUpdateRateMs = DEFAULT_UPDATE_RATE_MS;
//            }
//
//            if( mIsInMuteMode != isDeviceMuted ) {
//                mIsInMuteMode = isDeviceMuted;
//                int alpha = ( isDeviceMuted ) ? 100 : 255;
//                mTextColorPaint.setAlpha( alpha );
//                invalidate();
//                updateTimer();
//            }
//        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();

            invalidate();
        }


        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);


            mDisplayTime.setToNow();

            //Log.v("archit",bounds.toString());
            drawBackground( canvas, bounds );
            drawTimeText( canvas );
            drawWeatherImage( canvas );
            drawWeatherDetails( canvas );
        }

        private void drawWeatherDetails(Canvas canvas) {
            String weatherText = "25 , 16";

            canvas.drawText( weatherText, mXOffsetWeatherDetails, mYOffsetWeatherDetails, mTextColorPaintWeatherDetails );
        }

        private void drawWeatherImage(Canvas canvas) {

            Drawable d = getResources().getDrawable(R.drawable.art_clear);
            d.setBounds(50, 150, 100, 200);
            d.draw(canvas);
        }

        private void drawBackground( Canvas canvas, Rect bounds ) {
            canvas.drawRect( 0, 0, bounds.width(), bounds.height(), mBackgroundColorPaint );
        }

        private void drawTimeText( Canvas canvas ) {
            String timeText = getHourString() + ":" + String.format( "%02d", mDisplayTime.minute );
            if( isInAmbientMode() || mIsInMuteMode ) {
                timeText += ( mDisplayTime.hour < 12 ) ? "AM" : "PM";
            } else {
                timeText += String.format( ":%02d", mDisplayTime.second);
            }

            //Log.v("archit",mXOffset+" "+mYOffset);
            canvas.drawText( timeText, mXOffset, mYOffset, mTextColorPaint );
        }

        private String getHourString() {
            if( mDisplayTime.hour % 12 == 0 )
                return "12";
            else if( mDisplayTime.hour <= 12 )
                return String.valueOf( mDisplayTime.hour );
            else
                return String.valueOf( mDisplayTime.hour - 12 );
        }

        @Override
        public void onDataChanged(DataEventBuffer dataEventBuffer) {
            Log.e("test","data changed");
            for (DataEvent dataEvent : dataEventBuffer) {
                DataItem dataItem = dataEvent.getDataItem();
                if(dataItem.getUri().getPath().compareTo(PATH_WITH_FEATURE) == 0){
                    Log.e("test","event received");
                    DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
                    Log.e("test","time : "+dataMap.get("time"));
                    Log.e("test","location : "+dataMap.get("location"));
                }
                //possible position to get location
            }


        }


        @Override
        public void onConnected(Bundle connectionHint) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Connected to Google Api Service");
            }
            Log.e("test","added listener");
            Wearable.DataApi.addListener(mGoogleApiClient, this);
        }



        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }

//        private class LoadWeatherDetails extends AsyncTask<Void, Void, Integer> {
//            @Override
//            protected Integer doInBackground(Void... voids) {
//                long begin = System.currentTimeMillis();
//                Uri.Builder builder =
//                        WearableCalendarContract.Instances.CONTENT_URI.buildUpon();
//                ContentUris.appendId(builder, begin);
//                ContentUris.appendId(builder, begin + DateUtils.DAY_IN_MILLIS);
//                final Cursor cursor = getContentResolver() .query(builder.build(),
//                        null, null, null, null);
//                int numMeetings = cursor.getCount();
//                if (Log.isLoggable(TAG, Log.VERBOSE)) {
//                    Log.v(TAG, "Num meetings: " + numMeetings);
//                }
//                return numMeetings;
//            }
//
//            @Override
//            protected void onPostExecute(Integer result) {
//        /* get the number of meetings and set the next timer tick */
//                onMeetingsLoaded(result);
//            }
//        }


    }
}
