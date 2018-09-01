package services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.xplocity.xplocity.R;

import services.interfaces.ServiceStateReceiverInterface;
import utils.ResourceGetter;

/**
 * Created by dmitry on 02.09.17.
 */

// Broadcast receiver for receiving status updates from the IntentService
public class ServiceStateReceiver extends BroadcastReceiver {

    private ServiceStateReceiverInterface mCallback;

    public ServiceStateReceiver(ServiceStateReceiverInterface callback) {
        mCallback = callback;
        // The filter's action is BROADCAST_ACTION
    }

    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    @Override
    public void onReceive(Context context, Intent intent) {

        String s = intent.getAction();
        if (s.equals(ResourceGetter.getString("broadcast_position_changed"))) {
            mCallback.onPositionChanged();
        }
        else if (s.equals(ResourceGetter.getString("broadcast_location_reached"))) {
            mCallback.onLocationReached(intent.getIntExtra("locationId", 0));
        }
        else if (s.equals(ResourceGetter.getString("broadcast_location_circle_reached"))) {
            mCallback.onLocationCircleReached(intent.getIntExtra("locationId", 0));
        }




    }

    public void registerReceiver(Context context) {
        IntentFilter statusIntentFilter = new IntentFilter();
        statusIntentFilter.addAction(ResourceGetter.getString("broadcast_position_changed"));
        statusIntentFilter.addAction(ResourceGetter.getString("broadcast_location_reached"));
        statusIntentFilter.addAction(ResourceGetter.getString("broadcast_location_circle_reached"));

        LocalBroadcastManager.getInstance(context).registerReceiver(
                this,
                statusIntentFilter);
    }

    public void unregisterReceiver(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

}
