package org.stuba.fei.socialapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * Created by Marian-PC on 17.11.2018.
 */

public class UserInfo extends AppCompatActivity {
    //main
    private CoordinatorLayout coordinatorLayout;
    private Toast toast;
    private String path;

    //swipe
    float initialX;
    private int Measuredheight;

    //fab
    private FloatingActionButton fab, fab1, fab2, fab3, fab4, fab5;
    private boolean isFABOpen = false;
    private boolean callBack = false;
    private boolean swap = false;
    private Animation rotate_forward,rotate_backward;

    //firebase
    private FireStoreUtils fireStoreUtils = new FireStoreUtils();
    private static FirebaseAuth mAuth;
    private FirebaseUser user;
    private static String userName;

    //static variables
    private static final int RESULT_LOAD_IMAGE = 1999;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MyVersion = Build.VERSION.SDK_INT;

    //textfield
    private TextView TextViewName, TextViewPosts, TextViewRegistracion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        Bundle extras = getIntent().getExtras();
        coordinatorLayout =  (CoordinatorLayout) findViewById(R.id.user_info);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Point size = new Point();
        WindowManager w = getWindowManager();
        Measuredheight = 0;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)    {
            w.getDefaultDisplay().getSize(size);
            Measuredheight = size.y;
        }else{
            Display d = w.getDefaultDisplay();
            Measuredheight = d.getHeight();
        }

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        TextViewName = findViewById(R.id.textView5);
        TextViewPosts = findViewById(R.id.textView9);
        TextViewRegistracion = findViewById(R.id.textView7);

        initFAB();
        if (extras != null) {
            userName = extras.getString("user_name");
            TextViewName.setText(extras.getString("user_uID"));
            TextViewPosts.setText(extras.getString("user_posts"));

            StringBuilder sb = new StringBuilder(extras.getString("user_date"));
            int idx = extras.getString("user_date").indexOf("GMT");
            sb.delete(idx, idx+9);
            sb.delete(0,4);
            TextViewRegistracion.setText(sb.toString());
        }
    }

    @Override
    public void finish() {
        if (isFABOpen) {
            if (callBack) {
                FABMenuClose();
                super.finish();
            } else if (swap) {
                FABMenuClose();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                super.finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                FABMenuClose();
            }
        }
        else{
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            super.finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    private void reloudUserInfo(){
        fireStoreUtils.getUserUID(userName, new Callback() {
            @Override
            public void onCallback(UserPojo userPojo) {
                TextViewName.setText(userPojo.getuId());
                TextViewPosts.setText(userPojo.getNumberOfPosts());
                TextViewRegistracion.setText(userPojo.getDateOfRegistration());
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
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
            switch(action)
            {
                case MotionEvent.ACTION_DOWN:
                    initialX = event.getY();
                    return true;

                case MotionEvent.ACTION_UP:
                    float finalX = event.getY();
                    if (initialX > finalX && (initialX-finalX)>=Measuredheight/6) {
                        swap = true;
                        finish();
                    }
                    break;
            }
        return super.onTouchEvent(event);
    }

    private void initFAB(){
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        fab4 = (FloatingActionButton) findViewById(R.id.fab4);
        fab5 = (FloatingActionButton) findViewById(R.id.fab5);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FABMenu();
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable(UserInfo.this)) {
                    path = VideoImage.cameraInit(UserInfo.this, "image");
                }
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable(UserInfo.this)){
                    path = VideoImage.cameraInit(UserInfo.this,"video");
                }

            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable(UserInfo.this)) {
                    VideoImage.galeryInit(UserInfo.this);
                }
            }
        });
        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.user_info).post(new Runnable() {
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(UserInfo.this);
                        builder.setTitle("");
                        builder.setMessage("Are you sure you wish to logout?");
                        builder.setCancelable(true);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                callBack = true;
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("call","logout");
                                setResult(Activity.RESULT_OK,returnIntent);
                                finish();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                    }
                });

            }
        });
        fab5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable(UserInfo.this)) {
                    FABMenuClose();
                    reloudUserInfo();
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(UserInfo.this.getApplicationContext(), "Refreshed", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    private void FABMenu(){
        if(isFABOpen) {
            FABMenuClose();
        }
        else {
            FABMenuOpen();
        }
    }

    private void FABMenuClose(){
        fab.startAnimation(rotate_backward);
        fab1.animate().translationY(0);
        fab2.animate().translationY(0);
        fab3.animate().translationY(0);
        fab4.animate().translationY(0);
        fab5.animate().translationY(0);
        fab1.setClickable(false);
        fab2.setClickable(false);
        fab3.setClickable(false);
        fab4.setClickable(false);
        fab5.setClickable(false);
        isFABOpen = false;
    }

    private void FABMenuOpen(){
        fab.startAnimation(rotate_forward);
        fab1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fab2.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        fab3.animate().translationY(-getResources().getDimension(R.dimen.standard_155));
        fab4.animate().translationY(-getResources().getDimension(R.dimen.standard_255));
        fab5.animate().translationY(-getResources().getDimension(R.dimen.standard_205));
        fab1.setClickable(true);
        fab2.setClickable(true);
        fab3.setClickable(true);
        fab4.setClickable(true);
        fab5.setClickable(true);
        isFABOpen = true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (MyVersion >= Build.VERSION_CODES.M) {
                    if (!PermissionsAllow.checkIfAlreadyhavePermissionRead(this)) {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                }
                new ApiPostRequest(path, UserInfo.this, this.getApplicationContext(), coordinatorLayout, toast, user,TextViewPosts);
                FABMenuClose();
            }
        }
        if (requestCode == RESULT_LOAD_IMAGE) {
            if (resultCode == RESULT_OK && null != data) {
                if (MyVersion >= Build.VERSION_CODES.M) {
                    if (!PermissionsAllow.checkIfAlreadyhavePermissionRead(this)) {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                }
                String path = ImageFilePath.getPath(UserInfo.this, data.getData());
                new ApiPostRequest(path.toString(), UserInfo.this, this.getApplicationContext(), coordinatorLayout, toast, user, TextViewPosts);
                FABMenuClose();

            }
        }
    }

    //check internet connection
    private boolean isNetworkAvailable(Activity parentActivity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) parentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        else {
            if (toast != null) {
                toast.cancel();
            }
            toast = Toast.makeText(this.getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT);
            toast.show();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }


}
