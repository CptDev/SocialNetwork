package org.stuba.fei.socialapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Marian-PC on 9.12.2018.
 */

public class VideoImage {
    private static final int RESULT_LOAD_IMAGE = 1999;
    private static final int CAMERA_REQUEST = 1888;
    private static final int USER_INFO_REQUEST = 1777;
    private static final int USER_POSTS_REQUEST = 1666;
    private static final int MyVersion = Build.VERSION.SDK_INT;

    private static File file;
    private static String path;
    private static final String app_dir = "mobv";

    public static void galeryInit(Activity PareActivity){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if (MyVersion >= Build.VERSION_CODES.M) {
            if (!PermissionsAllow.checkIfAlreadyhavePermissionWrite(PareActivity)) {
                ActivityCompat.requestPermissions(PareActivity, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                readOrCreateDir();
            }
        }
        Intent mediaChooser = new Intent(Intent.ACTION_GET_CONTENT);
        String[] mimeTypes = {"video/mp4","image/jpg", "image/jpeg","image/png"};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mediaChooser.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                mediaChooser.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            mediaChooser.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }
        PareActivity.startActivityForResult(mediaChooser, RESULT_LOAD_IMAGE);
    }

    public static String cameraInit(Activity PareActivity, String type){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        String outDate = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String out = "";
        if(type.equalsIgnoreCase("image")){
            out =  File.separator + app_dir + File.separator+ "image" + File.separator+"image_" + outDate + ".jpeg";
        }
        else if (type.equalsIgnoreCase("video")) {
            out = File.separator + app_dir + File.separator + "video" + File.separator + "video_" + outDate + ".mp4";
        }
        if (MyVersion >= Build.VERSION_CODES.M) {
            if (!PermissionsAllow.checkIfAlreadyhavePermissionWrite(PareActivity)) {
                ActivityCompat.requestPermissions(PareActivity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                readOrCreateDir();
            }
            else{
                readOrCreateDir();
            }
        }
        if (MyVersion < Build.VERSION_CODES.M) {
            readOrCreateDir();
        }
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ out);
        path = file.toString();
        if(type.equalsIgnoreCase("image")){
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            PareActivity.startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
        else if (type.equalsIgnoreCase("video")) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            PareActivity.startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
        return path;
    }

    private static void readOrCreateDir(){
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + app_dir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File directoryImage = new File(Environment.getExternalStorageDirectory() + File.separator + app_dir + File.separator + "image");
        if (!directoryImage.exists()) {
            directoryImage.mkdirs();
        }
        File directoryVideo = new File(Environment.getExternalStorageDirectory() + File.separator + app_dir + File.separator + "video");
        if (!directoryVideo.exists()) {
            directoryVideo.mkdirs();
        }
    }
}
