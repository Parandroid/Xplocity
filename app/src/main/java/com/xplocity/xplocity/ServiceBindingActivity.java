package com.xplocity.xplocity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import services.XplocityPositionService;

/**
 * Created by skoparov on 8/23/17.
 */

public abstract class ServiceBindingActivity extends AppCompatActivity
{
    private class BindingConnection implements ServiceConnection
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            XplocityPositionService.LocalBinder binder =
                    (XplocityPositionService.LocalBinder) service;

            mService = binder.getService();
            mIsBound = true;
            onServiceBound();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            mIsBound = false;
            onServiceUnbound();
        }
    }

    protected XplocityPositionService mService;
    protected BindingConnection mConnection;
    protected boolean mIsBound;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mConnection = new BindingConnection();
        bindToLocationUpdateService();
    }

    protected abstract void onServiceBound();
    protected abstract void onServiceUnbound();

    @Override
    protected void onDestroy()
    {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }

        super.onDestroy();
    }

    protected void bindToLocationUpdateService()
    {
        Intent intent = new Intent(this, XplocityPositionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
}
