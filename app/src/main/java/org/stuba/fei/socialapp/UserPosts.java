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
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Marian-PC on 17.11.2018.
 */

public class UserPosts extends AppCompatActivity {
    //main
    private CoordinatorLayout coordinatorLayout;
    private String path;
    private Toast toast;


    //recycler view
    private ArrayList<PostPojo> list = null;
    private ExtraItemsAdapter adapter;
    private RecyclerView items;
    private float itemHeight;
    private float padding;
    private float firstItemHeight;
    private float allPixels;
    private float getAllPixelsEnd = -1.f;
    private float getGetAllPixelsStart = 0.f;

    //fab
    private FloatingActionButton fab, fab1, fab2, fab3, fab4, fab5;
    private boolean isFABOpen = false;
    private boolean disable = false;
    private boolean callBack = false;
    private Animation rotate_forward,rotate_backward;

    //firebase
    private FireStoreUtils fireStoreUtils = new FireStoreUtils();
    private static FirebaseAuth mAuth;
    private FirebaseUser user;
    private static String userName;
    private boolean isUser = false;
    private boolean post = false;
    private int listSize = 0;

    //static variables
    private static final String BUNDLE_LIST_PIXELS = "allPixels";
    private static final int RESULT_LOAD_IMAGE = 1999;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MyVersion = Build.VERSION.SDK_INT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_posts);
        coordinatorLayout =  (CoordinatorLayout) findViewById(R.id.user_posts);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userName = extras.getString("user_name");
            list = new ArrayList<PostPojo>();
            list = (ArrayList<PostPojo>) getIntent().getSerializableExtra("user_posts");
        }

        if((user.getUid()).equals(list.get(0).getUserid())){
            isUser=true;
        }

        getAllPixelsEnd = -1.f;
        initRecyclerView();
        initFAB();
    }

    @Override
    protected void onResume() {
        super.onResume();
        items = (RecyclerView) findViewById(R.id.card_recycler_view2);
        if(items!=null ) {
            if (adapter != null) {
                ViewTreeObserver vto = items.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        items.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        calculatePositionAndScroll(items);
                    }
                });
            }
        }
    }

    @Override
    public void finish() {
        if (isFABOpen) {
            if (callBack) {
                super.finish();
            }
            else{
                FABMenuClose();
            }
        }
        else{
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            super.finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    private void reloudUserPosts(){
        fireStoreUtils.getPostsForUser(userName,new Callback() {
            @Override
            public void onCallback(UserPojo pojo) {
            }

            @Override
            public void onCallback2(List<UserPojo> pojos) {

            }

            @Override
            public void onCallback(PostPojo pojo) {
            }

            @Override
            public void onCallback3(List<PostPojo> pojos) {
                list.clear();
                if (pojos.size() > 0) {
                    for (PostPojo p : pojos) {
                        list.add(p);
                    }
                    final DateFormat f = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    Collections.sort(list, new Comparator<PostPojo>() {
                        @Override
                        public int compare(PostPojo u1, PostPojo u2) {
                            try {
                                return (f.parse(u2.getDate()).compareTo(f.parse(u1.getDate())));
                            } catch (ParseException e) {
                                throw new IllegalArgumentException(e);
                            }
                        }
                    });
                    items.scrollToPosition(0);
                    allPixels = 0;
                    getAllPixelsEnd = -1.f;
                    adapter.notifyDataSetChanged();
                }
                if (toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(UserPosts.this.getApplicationContext(), "Refreshed", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    private void initRecyclerView(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        Point size = new Point();
        WindowManager w = getWindowManager();
        int Measuredheight = 0;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)    {
            w.getDefaultDisplay().getSize(size);
            Measuredheight = size.y;
        }else{
            Display d = w.getDefaultDisplay();
            Measuredheight = d.getHeight();
        }
        allPixels=0;
        itemHeight = height - getStatusBarHeight();
        padding = ((Measuredheight - (itemHeight +getStatusBarHeight())) / 2);
        firstItemHeight = height - getStatusBarHeight();

        items = (RecyclerView) findViewById(R.id.card_recycler_view2);
        items.setHasFixedSize(true);
        items.setItemViewCacheSize(6);
        items.setDrawingCacheEnabled(true);
        items.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getApplicationContext(),1,false);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setExtraLayoutSpace(Measuredheight);
        items.setLayoutManager(layoutManager);
        items.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                synchronized (this) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        disable = false;
                        calculatePositionAndScroll(recyclerView);
                        if(post){
                            if(listSize!=list.size()){
                                allPixels = 0;
                                getAllPixelsEnd = -1.f;
                                post= false;
                            }
                            else{
                                post = false;
                            }
                        }
                    }
                    else {
                        FABMenuClose();
                        disable = true;
                        if(post){
                            if(listSize!=list.size()){
                                allPixels = 0;
                                getAllPixelsEnd = -1.f;
                                post= false;
                            }
                            else{
                                post = false;
                            }
                        }
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                allPixels += dy;
            }
        });

        adapter = new ExtraItemsAdapter(getApplicationContext(),list,false);
        items.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void calculatePositionAndScroll(RecyclerView recyclerView) {
        //swipe in top of recycler
        if(getAllPixelsEnd==allPixels && getGetAllPixelsStart==allPixels && post==false){
            finish();
        }
        else {
            getAllPixelsEnd=allPixels;
            int expectedPosition = Math.round((allPixels + padding - firstItemHeight) / itemHeight);
            if (expectedPosition == -2) {
                expectedPosition = -1;
            }
            else if (expectedPosition >= recyclerView.getAdapter().getItemCount() - 1) {
                expectedPosition--;
            }
            scrollListToPosition(recyclerView, expectedPosition);
        }
    }

    private void scrollListToPosition(RecyclerView recyclerView, int expectedPosition) {
        float targetScrollPos = expectedPosition * itemHeight + firstItemHeight - padding;
        float missingPx = targetScrollPos - allPixels;
        if (missingPx != 0) {
            recyclerView.smoothScrollBy(0, (int) missingPx);
        }

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
                if(!disable) {
                    FABMenu();
                }
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!disable) {
                    if(isNetworkAvailable(UserPosts.this)) {
                        path = VideoImage.cameraInit(UserPosts.this, "image");
                    }

                }
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!disable) {
                    if(isNetworkAvailable(UserPosts.this)) {
                        path = VideoImage.cameraInit(UserPosts.this, "video");
                    }
                }
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!disable) {
                    if(isNetworkAvailable(UserPosts.this)) {
                        VideoImage.galeryInit(UserPosts.this);
                    }
                }
            }
        });
        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!disable) {
                    findViewById(R.id.user_posts).post(new Runnable() {
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(UserPosts.this);
                            builder.setTitle("");
                            builder.setMessage("Are you sure you wish to logout?");
                            builder.setCancelable(true);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    callBack = true;
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("call", "logout");
                                    setResult(Activity.RESULT_OK, returnIntent);
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
            }
        });
        fab5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable(UserPosts.this)) {
                    FABMenuClose();
                    reloudUserPosts();
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(UserPosts.this.getApplicationContext(), "Refreshed", Toast.LENGTH_SHORT);
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
        if(isFABOpen) {
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
    }

    private void FABMenuOpen() {
        if (!isFABOpen) {
            fab.startAnimation(rotate_forward);
            fab1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
            fab2.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
            fab3.animate().translationY(-getResources().getDimension(R.dimen.standard_155));
            fab5.animate().translationY(-getResources().getDimension(R.dimen.standard_205));
            fab4.animate().translationY(-getResources().getDimension(R.dimen.standard_255));
            fab1.setClickable(true);
            fab2.setClickable(true);
            fab3.setClickable(true);
            fab4.setClickable(true);
            fab5.setClickable(true);
            isFABOpen = true;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (MyVersion >= Build.VERSION_CODES.M) {
                    if (!PermissionsAllow.checkIfAlreadyhavePermissionRead(this)) {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                }
                if(isUser){
                    listSize = list.size();
                    new ApiPostRequest(path, UserPosts.this, this.getApplicationContext(), coordinatorLayout, toast, user, allPixels, getAllPixelsEnd, items, adapter, list);
                    post = true;
                }
                else{
                    new ApiPostRequest(path, UserPosts.this, this.getApplicationContext(), coordinatorLayout, toast, user);
                }
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
                String path = ImageFilePath.getPath(UserPosts.this, data.getData());
                if(isUser) {
                    listSize = list.size();
                    new ApiPostRequest(path.toString(), UserPosts.this, this.getApplicationContext(), coordinatorLayout, toast, user, allPixels, getAllPixelsEnd, items, adapter, list);
                    post = true;

                }
                else{
                    new ApiPostRequest(path.toString(), UserPosts.this, this.getApplicationContext(), coordinatorLayout, toast, user);
                }
                FABMenuClose();
            }
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static ArrayList<PostPojo> reverse(ArrayList<PostPojo> list) {
        for(int i = 0, j = list.size() - 1; i < j; i++) {
            list.add(i, list.remove(j));
        }
        return list;
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        allPixels = savedInstanceState.getFloat(BUNDLE_LIST_PIXELS);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(BUNDLE_LIST_PIXELS, allPixels);
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
