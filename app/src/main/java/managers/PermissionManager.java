package managers;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.xplocity.xplocity.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dmitry on 24.11.18.
 */



public class PermissionManager {

    private final static int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;


    // Returns true if permissions were requested
    //         false if permissions have been given already
    public static boolean requestPermissions(Activity activity) {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION, activity))
            permissionsNeeded.add(activity.getString(R.string.info_location_perm_request_text));
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE, activity))
            permissionsNeeded.add(activity.getString(R.string.info_write_external_storage_perm_request_text));


        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = activity.getString(R.string.permission_request_header) + System.lineSeparator() + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + System.lineSeparator() + permissionsNeeded.get(i);
                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity,
                                        permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        }, activity);
                return true;
            }
            ActivityCompat.requestPermissions(activity,
                    permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return true;
        } else {
            return false;
        }

    }


    private static boolean addPermission(List<String> permissionsList, String permission, Activity activity) {
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
                return false;
        }
        return true;
    }

    private static void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener, Context context) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    // Check result of a permission request
    public static boolean permissionsGranted(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for permissions
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    return true;
                } else {
                    // Permission Denied
                    return false;
                }
            }
            default: return false;

        }
    }

}
