package org.stuba.fei.socialapp;

/**
 * Created by Marian-PC on 22.11.2018.
 */

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;


public abstract class PermissionsAllow {
    public static boolean checkIfAlreadyhavePermissionRead(Context context) {
        int result = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkIfAlreadyhavePermissionWrite(Context context) {
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }
}
