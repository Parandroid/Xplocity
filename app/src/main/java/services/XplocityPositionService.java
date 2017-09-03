package services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xplocity.xplocity.R;

import java.util.ArrayList;
import java.util.Calendar;

import utils.Factory.LogFactory;
import utils.Log.Logger;
import utils.LogLevelGetter;
import utils.ResourceGetter;

public class XplocityPositionService extends Service {
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private LatLng mLastPosition;
    private ArrayList<LatLng> mRoute;
    private boolean mTrackingActive = false;
    private LocationManager mLocationManager;
    private Logger mLogger;

    private class LocationListener implements android.location.LocationListener {
        private Logger mLogger;

        public LocationListener(String provider) {
            mLogger = LogFactory.createLogger(this, LogLevelGetter.get());
        }

        @Override
        public void onLocationChanged(Location location) {
            if (mTrackingActive) {
                mLastPosition = new LatLng(location.getLatitude(), location.getLongitude());
                mRoute.add(mLastPosition);
                broadcastLocationChanged();


                writeStateToStorage();

                mLogger.logVerbose("Location update: " + location.toString());
            }
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            //new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    public class LocalBinder extends Binder {
        public XplocityPositionService getService() {
            // Return this instance of LocalService so clients can call public methods
            return XplocityPositionService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        mLogger = LogFactory.createLogger(this, LogLevelGetter.get());
        initializeLocationManager();

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Xplocity")
                .setContentText("Tracking started")
                .setOngoing(true)
                .build();

        startForeground(ResourceGetter.getInteger("location_service_id"), notification);

        // load tasks from preference
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mTrackingActive = prefs.getBoolean("tracking_active", false);

        if(mTrackingActive) {
            Gson gson = new Gson();
            mRoute = gson.fromJson(prefs.getString("route", ""), new TypeToken<ArrayList<LatLng>>() {
            }.getType());

            prefs.edit().remove("route");
            prefs.edit().remove("tracking_active");

            startTracking();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scheduleService();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        scheduleService();
    }

    private void scheduleService() {
        writeStateToStorage();

        Intent myIntent = new Intent(getApplicationContext(), XplocityPositionService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, myIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10);

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        mLogger.logInfo("Service stopped");
    }

    private void writeStateToStorage() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(mRoute);
        editor.putString("route", json);
        editor.putBoolean("tracking_active", mTrackingActive);

        editor.commit();
    }

    private void clearStorage() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();


        editor.remove("route");
        editor.remove("tracking_active");

        editor.commit();

    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }


    private void broadcastLocationChanged() {
        Intent localIntent =
                new Intent(getString(R.string.broadcast_position_changed));
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }






    public void startTracking() {
        if(!mTrackingActive)
        {
            mTrackingActive = true;

            if (mRoute == null) {
                mRoute = new ArrayList<LatLng>();
            }

            mRoute.clear();

            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[0]);
            } catch (java.lang.SecurityException e) {
                mLogger.logError("Failed to start tracking: Failed to request location update", e);
            } catch (IllegalArgumentException e) {
                mLogger.logError("Failed to start tracking: GPS provider does not exist", e);
            }
        }
        else {
            mLogger.logWarning("Failed to start tracking: tracking already in progress");
        }
    }

    public void stopTracking() {
        if(mTrackingActive)
        {
            mTrackingActive = false;
            mRoute.clear();
            clearStorage();

            if (mLocationManager != null) {
                try {
                    for(LocationListener l: mLocationListeners ){
                        mLocationManager.removeUpdates(l);
                    }
                } catch (Exception e) {
                    mLogger.logError("Failed to start tracking: GPS provider does not exist", e);
                }
            }
        }
        else{
            mLogger.logWarning("Failed to stop tracking: already stopped");
        }
    }

    public boolean trackingActive(){
        return mTrackingActive;
    }


    public ArrayList<LatLng> getPath() {
        return mRoute;
    }
}
