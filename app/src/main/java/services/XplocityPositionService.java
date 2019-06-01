package services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.xplocity.xplocity.R;
import com.xplocity.xplocity.RouteNewActivity;

import org.joda.time.DateTime;
import org.osmdroid.util.GeoPoint;

import java.lang.reflect.Modifier;
import java.util.Calendar;

import app.XplocityApplication;
import managers.PositionManager;
import managers.interfaces.PositionManagerInterface;
import models.Route;
import utils.DateTimeConverter;
import utils.Factory.LogFactory;
import utils.Log.Logger;
import utils.LogLevelGetter;

public class XplocityPositionService
        extends Service
        implements PositionManagerInterface {

    //singleton for accessing the service instance
    private static XplocityPositionService mInstance;

    public static XplocityPositionService getInstance() {
        if (mInstance != null) {
            return mInstance;
        } else {
            return null;
        }

    }

    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 5f;

    public static final int TRACKING_STATE_NOT_STARTED = 1;
    public static final int TRACKING_STATE_ACTIVE = 2;
    public static final int TRACKING_STATE_FINISHED = 3;


    public int trackingState;

    private PositionManager mPositionManager;
    private LocationManager mLocationManager;


    private boolean mIsForeground;
    private Logger mLogger;

    private static final int STICKY_NOTIFICATION_ID = 10000;
    NotificationCompat.Builder mNotifBuilder;
    NotificationManager mNotifManager;

    private class LocationListener implements android.location.LocationListener {
        private Logger mLogger;

        public LocationListener(String provider) {
            mLogger = LogFactory.createLogger(this, LogLevelGetter.get());
        }

        @Override
        public void onLocationChanged(Location location) {
            if (trackingState == TRACKING_STATE_ACTIVE) {
                mPositionManager.addPosToPath(new GeoPoint(location.getLatitude(), location.getLongitude()));
                broadcastPositionChanged();

                //writeStateToStorage();
                //mLogger.logVerbose("Location update: " + location.toString());
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
        mInstance = this;

        mLogger = LogFactory.createLogger(this, LogLevelGetter.get());
        initializeLocationManager();

        mNotifManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(getString(R.string.notification_channel_id),
                    "Xplocity",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotifManager.createNotificationChannel(channel);
        }

        mNotifBuilder = new NotificationCompat.Builder(this);

        //runServiceForeground();

        loadStateFromStorage();


    }

    public void runServiceForeground() {
        if (!mIsForeground) {
            Intent intent = new Intent(this, RouteNewActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            mNotifBuilder.setContentTitle("Ready to start tracking")
                    .setSmallIcon(R.drawable.ic_walking)
                    .setOngoing(true)
                    .setChannelId(getString(R.string.notification_channel_id))
                    .setContentIntent(pendingIntent);

            Notification notification = mNotifBuilder.build();
            mNotifManager.notify(STICKY_NOTIFICATION_ID, notification);

            startForeground(STICKY_NOTIFICATION_ID, notification);
            mIsForeground = true;
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

        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithModifiers(/*Modifier.FINAL, */Modifier.TRANSIENT, Modifier.STATIC);
        builder.registerTypeAdapter(DateTime.class, new DateTimeConverter());
        Gson gson = builder.create();

        String json = gson.toJson(mPositionManager);
        editor.putString("positionManager", json);
        editor.putInt("trackingState", trackingState);
        editor.commit();
    }

    private void writeTrackingStateToStorage() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("trackingState", trackingState);
        editor.commit();
    }

    private void loadStateFromStorage() {
        // load tasks from preference
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        if (prefs.contains("positionManager") && prefs.contains("trackingState")) {
            try {
                GsonBuilder builder = new GsonBuilder();
                builder.excludeFieldsWithModifiers(/*Modifier.FINAL, */Modifier.TRANSIENT, Modifier.STATIC);
                builder.registerTypeAdapter(DateTime.class, new DateTimeConverter());
                Gson gson = builder.create();

                mPositionManager = gson.fromJson(prefs.getString("positionManager", ""), new TypeToken<PositionManager>() {
                }.getType());
                mPositionManager.setCallback(this);

                trackingState = prefs.getInt("trackingState", TRACKING_STATE_NOT_STARTED);
                if (trackingState == TRACKING_STATE_ACTIVE) {
                    requestLocationUpdates();
                }

            } catch (Exception e) {
                mLogger.logError("Couldn't restore position service state.", e);
                clearStorage();
                loadStateFromStorage();
            }
        } else {
            mPositionManager = new PositionManager(this);
            trackingState = TRACKING_STATE_NOT_STARTED;
        }


        clearStorage();


    }


    private void clearStorage() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove("positionManager");
        editor.remove("trackingState");
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


        Intent intent = new Intent(this, RouteNewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        if (!XplocityApplication.isActivityVisible()) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.cast_ic_notification_small_icon)
                    .setContentTitle("Location reached")
                    .setContentText(location.name)
                    .setChannelId(getString(R.string.notification_channel_id))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            //NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifManager.notify(0, mBuilder.build());
        }
    }


    @Override
    public void onLocationCircleReached(models.Location location) {
        Intent localIntent =
                new Intent(getString(R.string.broadcast_location_circle_reached));
        localIntent.putExtra("locationId", location.id);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }


    public void startTracking() {
        if (trackingState != TRACKING_STATE_ACTIVE) {
            mPositionManager.startTracking();
            trackingState = TRACKING_STATE_ACTIVE;
            writeTrackingStateToStorage();

            if (mNotifBuilder != null) {
                mNotifBuilder.setContentTitle("Tracking started");
                mNotifManager.notify(STICKY_NOTIFICATION_ID, mNotifBuilder.build());
            }

            /*try {
                Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    mPositionManager.addPosToPath(new GeoPoint(location.getLatitude(), location.getLongitude()));
                    broadcastPositionChanged();
                }
            } catch (java.lang.SecurityException e) {
                mLogger.logError("Failed to start tracking: Failed to request location update", e);
            }*/

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
        if (trackingState == TRACKING_STATE_ACTIVE) {
            trackingState = TRACKING_STATE_FINISHED;
            writeTrackingStateToStorage();

            if (mNotifBuilder != null) {
                mNotifBuilder.setContentTitle("Tracking stopped. Route can be saved.");
                mNotifManager.notify(STICKY_NOTIFICATION_ID, mNotifBuilder.build());
            }


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

    public void destroyService() {
        clearStorage();
        trackingState = TRACKING_STATE_NOT_STARTED;
        stopForeground(true);
        mIsForeground = false;
        mNotifManager.cancelAll();
        stopSelf();
    }


    public boolean trackingActive() {
        return trackingState == TRACKING_STATE_ACTIVE;
    }

    public boolean savingActive() {
        return trackingState == TRACKING_STATE_FINISHED;
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

    public int getUnexploredLocationsCount() {
        return getRoute().loc_cnt_total - getRoute().loc_cnt_explored;
    }


    /*public ArrayList<LatLng> getPath() {
        return mPath;
    }*/
}
