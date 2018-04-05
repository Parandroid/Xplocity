package services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
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
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xplocity.xplocity.R;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;

import app.XplocityApplication;
import managers.PositionManager;
import managers.interfaces.PositionManagerInterface;
import models.Route;
import utils.Factory.LogFactory;
import utils.Log.Logger;
import utils.LogLevelGetter;
import utils.ResourceGetter;

public class XplocityPositionService
        extends Service
        implements PositionManagerInterface {
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private PositionManager mPositionManager;

    private LocationManager mLocationManager;


    private Logger mLogger;

    private class LocationListener implements android.location.LocationListener {
        private Logger mLogger;

        public LocationListener(String provider) {
            mLogger = LogFactory.createLogger(this, LogLevelGetter.get());
        }

        @Override
        public void onLocationChanged(Location location) {
            if (mPositionManager.trackingActive) {
                mPositionManager.addPosToPath(new GeoPoint(location.getLatitude(), location.getLongitude()));
                broadcastPositionChanged();

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

        if (prefs.contains("positionManager")) {
            Gson gson = new Gson();
            mPositionManager = gson.fromJson(prefs.getString("positionManager", ""), new TypeToken<PositionManager>() {
            }.getType());
            mPositionManager.setCallback(this);
            clearStorage();
        }
        else {
            mPositionManager = new PositionManager(this);
        }


        if (mPositionManager.trackingActive) {
            requestLocationUpdates();
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
        String json = gson.toJson(mPositionManager);
        editor.putString("positionManager", json);
        editor.commit();
    }

    private void clearStorage() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove("positionManager");
        editor.commit();

    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }


    private void broadcastPositionChanged() {
        Intent localIntent =
                new Intent(getString(R.string.broadcast_position_changed));
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }


    @Override
    public void onLocationReached(models.Location location) {
        Intent localIntent =
                new Intent(getString(R.string.broadcast_location_reached));
        localIntent.putExtra("locationId", location.id);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        if (!XplocityApplication.isActivityVisible()) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.cast_ic_notification_small_icon)
                    .setContentTitle("Xplocity. Location reached.")
                    .setContentText(location.name);

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, mBuilder.build());
        }
    }


    public void startTracking() {
        if (!mPositionManager.trackingActive) {
            mPositionManager.startTracking();

            requestLocationUpdates();
        } else {
            mLogger.logWarning("Failed to start tracking: tracking already in progress");
        }
    }

    private void requestLocationUpdates() {
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

    public void stopTracking() {
        if (mPositionManager.trackingActive) {
            mPositionManager.stopTracking();

            clearStorage();

            if (mLocationManager != null) {
                try {
                    for (LocationListener l : mLocationListeners) {
                        mLocationManager.removeUpdates(l);
                    }
                } catch (Exception e) {
                    mLogger.logError("Failed to start tracking: GPS provider does not exist", e);
                }
            }
        } else {
            mLogger.logWarning("Failed to stop tracking: already stopped");
        }
    }

    public boolean trackingActive() {
        return mPositionManager.trackingActive;
    }

    public void setRoute(Route route) {
        mPositionManager.route = route;
    }

    public Route getRoute() {
        return mPositionManager.route;
    }


    public GeoPoint getLastposition() {
        return mPositionManager.lastPosition;
    }


    /*public ArrayList<LatLng> getPath() {
        return mPath;
    }*/
}
