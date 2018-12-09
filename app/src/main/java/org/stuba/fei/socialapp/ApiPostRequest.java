package org.stuba.fei.socialapp;

/**
 * Created by Marian-PC on 22.11.2018.
 */

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


public class ApiPostRequest {
    private static final String POSTURL =  "http://mobv.mcomputing.eu/upload/index.php";
    FireStoreUtils fireStoreUtils = new FireStoreUtils();

    public ApiPostRequest(final String photo, final Activity parentActivity, final Context parentContext, final CoordinatorLayout parentLayout, final Toast parentToast, final FirebaseUser user) {
        //check internet connection
        if (fileType(parentContext, photo)) {
            if (fileSize(photo)) {
                if (isNetworkAvailable(parentActivity)) {
                    final RequestQueue queue = Volley.newRequestQueue(parentActivity);
                    final RequestSending rs = new RequestSending(parentContext, parentActivity, parentLayout);
                    SimpleMultiPartRequest smr = new SimpleMultiPartRequest(Request.Method.POST, POSTURL,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    String message = parseResponse(parentContext, parentToast, response);
                                    if(!message.equalsIgnoreCase("")) {
                                        try {
                                            PostPojo current = new PostPojo();
                                            current.setUsername(user.getEmail());
                                            current.setUserid(user.getUid());
                                            current.setDate(new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()).toString());
                                            if (fileTypeVideo(parentContext, photo)) {
                                                current.setVideourl("http://mobv.mcomputing.eu/upload/v/" + message);
                                                current.setType("video");
                                            } else {
                                                current.setImageurl("http://mobv.mcomputing.eu/upload/v/" + message);
                                                current.setType("image");
                                            }
                                            fireStoreUtils.addPost(current);
                                            fireStoreUtils.getUser(user, new Callback() {
                                                @Override
                                                public void onCallback(UserPojo userPojo) {
                                                    fireStoreUtils.updatePocetPrispevkov(user.getUid(), Integer.parseInt(userPojo.getNumberOfPosts()) + 1);
                                                    succesToast(parentContext, parentToast);
                                                }

                                                @Override
                                                public void onCallback2(List<UserPojo> pojos) {

                                                }

                                                @Override
                                                public void onCallback(PostPojo pojo) {

                                                }

                                                @Override
                                                public void onCallback3(List<PostPojo> pojos) {
                                                }
                                            });
                                        } catch (Exception error) {
                                            errorToast(parentContext, parentToast, error.toString());
                                        }
                                    }

                                    rs.stop();

                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            rs.stop();
                            errorToast(parentContext, parentToast, error.toString());
                        }
                    });
                    smr.addFile("upfile", photo);
                    queue.add(smr);
                    rs.stopButton(queue, parentContext, parentToast);
                }
                //no internet connection
                else {
                    errorToast(parentContext, parentToast, "No internet connection");
                }
            }
            //incorrect file size
            else {
                errorToast(parentContext, parentToast, "Incorrect file size");
            }
        }
        //incorrect file type
        else {
            errorToast(parentContext, parentToast, "Incorret file type");
        }
    }

    public ApiPostRequest(final String photo, final Activity parentActivity, final Context parentContext, final CoordinatorLayout parentLayout, final Toast parentToast, final FirebaseUser user, final TextView parentTextView) {
        //check internet connection
        if (fileType(parentContext, photo)) {
            if (fileSize(photo)) {
                if (isNetworkAvailable(parentActivity)) {
                    final RequestQueue queue = Volley.newRequestQueue(parentActivity);
                    final RequestSending rs = new RequestSending(parentContext, parentActivity, parentLayout);
                    SimpleMultiPartRequest smr = new SimpleMultiPartRequest(Request.Method.POST, POSTURL,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    String message = parseResponse(parentContext, parentToast, response);
                                    if(!message.equalsIgnoreCase("")) {
                                        try {
                                            PostPojo current = new PostPojo();
                                            current.setUsername(user.getEmail());
                                            current.setUserid(user.getUid());
                                            current.setDate(new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()).toString());
                                            if (fileTypeVideo(parentContext, photo)) {
                                                current.setVideourl("http://mobv.mcomputing.eu/upload/v/" + message);
                                                current.setType("video");
                                            } else {
                                                current.setImageurl("http://mobv.mcomputing.eu/upload/v/" + message);
                                                current.setType("image");
                                            }
                                            fireStoreUtils.addPost(current);
                                            fireStoreUtils.getUser(user, new Callback() {
                                                @Override
                                                public void onCallback(UserPojo userPojo) {
                                                    fireStoreUtils.updatePocetPrispevkov(user.getUid(), Integer.parseInt(userPojo.getNumberOfPosts()) + 1);
                                                    parentTextView.setText(Integer.toString(Integer.parseInt(userPojo.getNumberOfPosts()) + 1));
                                                    succesToast(parentContext, parentToast);
                                                }

                                                @Override
                                                public void onCallback2(List<UserPojo> pojos) {

                                                }

                                                @Override
                                                public void onCallback(PostPojo pojo) {

                                                }

                                                @Override
                                                public void onCallback3(List<PostPojo> pojos) {
                                                }
                                            });
                                        } catch (Exception error) {
                                            errorToast(parentContext, parentToast, error.toString());
                                        }
                                    }

                                    rs.stop();

                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            rs.stop();
                            errorToast(parentContext, parentToast, error.toString());
                        }
                    });
                    smr.addFile("upfile", photo);
                    queue.add(smr);
                    rs.stopButton(queue, parentContext, parentToast);
                }
                //no internet connection
                else {
                    errorToast(parentContext, parentToast, "No internet connection");
                }
            }
            //incorrect file size
            else {
                errorToast(parentContext, parentToast, "Incorrect file size");
            }
        }
        //incorrect file type
        else {
            errorToast(parentContext, parentToast, "Incorret file type");
        }
    }

    //check internet connection
    private static boolean isNetworkAvailable(Activity parentActivity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) parentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //error messae in log and toast
    private static void errorToast(Context context, Toast toast, String message){
        Log.d("Error", message);
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, "Error: "+message, Toast.LENGTH_SHORT);
        toast.show();
    }

    //error messae in log and toast
    private static void succesToast(Context context, Toast toast){
        Log.d("Succes", "New post added");
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, "New post added", Toast.LENGTH_SHORT);
        toast.show();
    }

    //parse response to list
    private static String parseResponse(Context context, Toast toast, String response){
        Log.d("Response", response);
        JSONObject jsonResponse;
        String status = "";
        String message = "";
        try {
            jsonResponse = new JSONObject(response);
            status = (jsonResponse.getString("status"));
            message = (jsonResponse.getString("message"));
            Log.d("status",status);
            Log.d("message",message);
        } catch (JSONException e) {
            e.printStackTrace();
            errorToast(context, toast, message);
        }
        if (status.equalsIgnoreCase("ok")){
            return message;
        }
        else if (status.equalsIgnoreCase("fail")){
            errorToast(context, toast, message);
            return "";
        }
        else{
            errorToast(context, toast, "unexpected error");
            return "";
        }
    }

    //check file size
    private static boolean fileSize(String uri) {
        File file = new File(uri);
        long size = file.length();
        if (size<(7.5*1024*1024)){
                return true;
        }
        else {
            return false;
        }
    }

    //check file type
    private static boolean fileType(Context context, String path) {
        String extension;
        File file = new File(path);
        Uri uri = Uri.fromFile(file);
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
       if(extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("mp4")){
           return true;
        }
        else {
           return false;
       }
    }

    private static boolean fileTypeVideo(Context context, String path) {
        String extension;
        File file = new File(path);
        Uri uri = Uri.fromFile(file);
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
        if(extension.equalsIgnoreCase("mp4")){
            return true;
        }
        else {
            return false;
        }
    }
}





