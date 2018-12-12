package org.stuba.fei.socialapp;

/**
 * Created by Marian-PC on 15.11.2018.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity{
    //main
    private CoordinatorLayout coordinatorLayout;
    private Toast toast;
    private Activity activity;
    private Context context;
    private String path;

    //recyclerview
    private ArrayList<PostPojo> list = null;
    private ArrayList<PostPojo> list2 = null;
    private ExtraItemsAdapter adapter;
    private RecyclerView items;
    private float firstItemWidth;
    private float itemWidth;
    private float padding;
    private float allPixels;
    private float getAllPixelsEnd = -1.f;
    private float getGetAllPixelsStart = 0.f;
    private boolean disableAfterReturm = false;

    //swipe
    private ItemTouchHelper.SimpleCallback simpleItemTouchCallback;
    private ItemTouchHelper itemTouchHelper;

    //static variables
    private static final int RESULT_LOAD_IMAGE = 1999;
    private static final int CAMERA_REQUEST = 1888;
    private static final int USER_INFO_REQUEST = 1777;
    private static final int USER_POSTS_REQUEST = 1666;
    private static final int MyVersion = Build.VERSION.SDK_INT;
    private static final String BUNDLE_LIST_PIXELS = "allPixels1";

    //fab
    private FloatingActionButton fab, fab1, fab2, fab3, fab4,fab5;
    private boolean isFABOpen = false;
    private Animation rotate_forward,rotate_backward;
    private boolean disable = false;


    //logoutCall
    private boolean logout = false;

    //firebase
    FireStoreUtils fireStoreUtils = new FireStoreUtils();
    public static FirebaseAuth mAuth;
    FirebaseUser user;
    private boolean post = false;
    private int listSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coordinatorLayout =  (CoordinatorLayout) findViewById(R.id.main);
        context = getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        fireStoreUtils.getUser(user,new Callback() {
            @Override
            public void onCallback(UserPojo userPojo) {
                Log.d("TAG", userPojo.getDateOfRegistration());
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

        fireStoreUtils.getPosts(new Callback() {
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
                list = new ArrayList<PostPojo>();
                if (pojos.size()>0) {
                    getAllPixelsEnd = -1.f;


                    for (PostPojo p:pojos){
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
                }
                else {
                    PostPojo p = new PostPojo();
                    p.setType("image");
                    p.setImageurl("http://chittagongit.com//images/no-data-icon/no-data-icon-4.jpg");
                    p.setDate("Database is empty");
                    p.setUsername(user.getEmail());
                    p.setUserid(user.getUid());
                    list.add(p);
                }

                initRecyclerView();
                initSwipe();
                initFAB();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        disableAfterReturm = true;
        getAllPixelsEnd = -1.f;
        items = (RecyclerView) findViewById(R.id.card_recycler_view);
        if(items!=null ) {
            if (adapter!=null){
            ViewTreeObserver vto = items.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    items.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    calculatePositionAndScroll(items);
                }
            });
            if (adapter!=null)
            adapter.notifyDataSetChanged();
            itemTouchHelper.attachToRecyclerView(items);
            }
       }

    }

    @Override
    public void finish() {
        if(!logout) {
            if (isFABOpen) {
                FABMenuClose();
            } else {
                FABMenuClose();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("logout", logout);
                setResult(Activity.RESULT_OK, returnIntent);
                super.finish();
            }
        }
        else {
            FABMenuClose();
            Intent returnIntent = new Intent();
            returnIntent.putExtra("logout", logout);
            setResult(Activity.RESULT_OK, returnIntent);
            super.finish();
        }
    }

    private void initRecyclerView(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        Point size = new Point();
        WindowManager wm = getWindowManager();
        int Measuredwidth = 0;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            wm.getDefaultDisplay().getSize(size);
            Measuredwidth = size.x;
        }else{
            Display d = wm.getDefaultDisplay();
            Measuredwidth = d.getWidth();
        }
        itemWidth = width;
        padding = (Measuredwidth - itemWidth) / 2;
        firstItemWidth = width;
        allPixels = 0;

        items = (RecyclerView) findViewById(R.id.card_recycler_view);
        items.setHasFixedSize(true);
        items.setItemViewCacheSize(30);
        items.setDrawingCacheEnabled(true);
        items.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getApplicationContext(),0,false);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.setExtraLayoutSpace(Measuredwidth);
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
                allPixels += dx;
            }
        });

        adapter = new ExtraItemsAdapter(getApplicationContext(),list,true);
        items.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void initSwipe(){
        simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.DOWN| ItemTouchHelper.UP) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                    if (direction == ItemTouchHelper.UP) {
                        itemTouchHelper.attachToRecyclerView(null);
                        adapter.onViewDetachedFromWindow((ViewHolder) viewHolder);
                        if (isNetworkAvailable(MainActivity.this)) {
                            FABMenuClose();
                            userPosts((ViewHolder) viewHolder);
                        }
                        else{
                            adapter.notifyDataSetChanged();
                            itemTouchHelper.attachToRecyclerView(items);
                        }

                    } else if (direction == ItemTouchHelper.DOWN) {
                        itemTouchHelper.attachToRecyclerView(null);
                        adapter.onViewDetachedFromWindow((ViewHolder) viewHolder);
                        if (isNetworkAvailable(MainActivity.this)) {
                            FABMenuClose();
                            userInfo((ViewHolder) viewHolder);
                        }
                        else{
                            adapter.notifyDataSetChanged();
                            itemTouchHelper.attachToRecyclerView(items);
                        }
                    }

            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            }
        };
        itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(items);
    }

    private void calculatePositionAndScroll(RecyclerView recyclerView) {
        if(getAllPixelsEnd==allPixels && getGetAllPixelsStart==allPixels && !disableAfterReturm && post==false){
            if(isNetworkAvailable(this)) {
                updateRecycler();
            }
        }
        else {
            if(!disableAfterReturm) {
                getAllPixelsEnd = allPixels;
            }
            else {
                disableAfterReturm=false;
            }
            int expectedPosition = Math.round((allPixels + padding - firstItemWidth) / itemWidth);
            Log.d("position",Integer.toString(expectedPosition));
            if (expectedPosition == -2) {
                expectedPosition = -1;
            } else if (expectedPosition >= recyclerView.getAdapter().getItemCount() - 1) {
                expectedPosition--;
            }
            scrollListToPosition(recyclerView, expectedPosition);
        }
    }

    private void scrollListToPosition(RecyclerView recyclerView, int expectedPosition) {
        float targetScrollPos = expectedPosition * itemWidth + firstItemWidth - padding;
        float missingPx = targetScrollPos - allPixels;
        if (missingPx != 0) {
            recyclerView.scrollBy((int) missingPx, 0);
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
                if(!disable){
                    FABMenu();
                }
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!disable){
                    if(isNetworkAvailable(MainActivity.this)) {
                        path = VideoImage.cameraInit(MainActivity.this, "image");
                    }
                }
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!disable){
                    if(isNetworkAvailable(MainActivity.this)) {
                        path = VideoImage.cameraInit(MainActivity.this, "video");
                    }
                }
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!disable) {
                    if(isNetworkAvailable(MainActivity.this)) {
                        VideoImage.galeryInit(MainActivity.this);
                    }
                }
            }
        });
        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!disable) {
                    findViewById(R.id.main).post(new Runnable() {
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("");
                            builder.setMessage("Are you sure you wish to logout?");
                            builder.setCancelable(true);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    logout=true;
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
                if(isNetworkAvailable(MainActivity.this)) {
                    FABMenuClose();
                    updateRecycler();
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(MainActivity.this.getApplicationContext(), "Refreshed", Toast.LENGTH_SHORT);
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

    private void FABMenuOpen(){
        if(!isFABOpen){
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
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (MyVersion >= Build.VERSION_CODES.M) {
                    if (!PermissionsAllow.checkIfAlreadyhavePermissionRead(this)) {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                }
                listSize= list.size();
                post = true;
                new ApiPostRequest(path, MainActivity.this, this.getApplicationContext(), coordinatorLayout, toast, user, allPixels, getAllPixelsEnd, items, adapter, list);
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
                listSize= list.size();
                post = true;
                String path = ImageFilePath.getPath(MainActivity.this, data.getData());
                new ApiPostRequest(path.toString(), MainActivity.this, this.getApplicationContext(), coordinatorLayout, toast, user, allPixels, getAllPixelsEnd, items, adapter, list);
                FABMenuClose();
            }
        }
        if (requestCode == USER_INFO_REQUEST || requestCode == USER_POSTS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                String call = data.getStringExtra("call");
                if (call.equalsIgnoreCase("logout")) {
                    logout = true;
                    finish();
                }
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                onResume();
            }
        }

    }


    private void updateRecycler(){
        fireStoreUtils.getPosts(new Callback() {
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
                if (pojos.size()>0) {
                    list.clear();
                    for (PostPojo p:pojos){
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

            }
        });

            if (toast != null) {
                toast.cancel();
            }
            toast = Toast.makeText(this.getApplicationContext(), "Refreshed", Toast.LENGTH_SHORT);
            toast.show();

    }

    private void userInfo(final ViewHolder holder){
        getAllPixelsEnd = -1.f;
        fireStoreUtils.getUserUID(list.get(holder.getAdapterPosition()).getUserid(), new Callback() {
            @Override
            public void onCallback(UserPojo userPojo) {
                Intent intent = new Intent(MainActivity.this.getApplicationContext(), UserInfo.class);
                intent.putExtra("user_name",list.get(holder.getAdapterPosition()).getUserid());
                intent.putExtra("user_uID",userPojo.getuId());
                intent.putExtra("user_date",userPojo.getDateOfRegistration());
                intent.putExtra("user_posts",userPojo.getNumberOfPosts());
                startActivityForResult(intent,USER_INFO_REQUEST);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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

    private void userPosts(final ViewHolder holder){
        getAllPixelsEnd = -1.f;
        fireStoreUtils.getPostsForUser(list.get(holder.getAdapterPosition()).getUserid(),new Callback() {
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
                list2 = new ArrayList<PostPojo>();
                if (pojos.size()>0) {
                    for (PostPojo p:pojos){
                        list2.add(p);
                    }
                    final DateFormat f = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    Collections.sort(list2, new Comparator<PostPojo>() {
                        @Override
                        public int compare(PostPojo u1, PostPojo u2) {
                            try {
                                return (f.parse(u2.getDate()).compareTo(f.parse(u1.getDate())));
                            } catch (ParseException e) {
                                throw new IllegalArgumentException(e);
                            }
                        }
                    });
                }
                else {
                    PostPojo p = new PostPojo();
                    p.setType("image");
                    p.setImageurl("http://chittagongit.com//images/no-data-icon/no-data-icon-4.jpg");
                    p.setDate("Database is empty");
                    p.setUsername(user.getEmail());
                    list2.add(p);
                }
                Intent intent = new Intent(MainActivity.this.getApplicationContext(), UserPosts.class);
                intent.putExtra("user_name",list.get(holder.getAdapterPosition()).getUserid());
                intent.putExtra("user_posts", list2);
                startActivityForResult(intent,USER_POSTS_REQUEST);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

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

}

