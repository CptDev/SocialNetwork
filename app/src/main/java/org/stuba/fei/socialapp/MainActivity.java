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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity{
    //main
    private CoordinatorLayout coordinatorLayout;
    private Toast toast;
    private Activity activity;
    private Context context;

    //recyclerview
    private List list;
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
    final int MyVersion = Build.VERSION.SDK_INT;
    private static final String BUNDLE_LIST_PIXELS = "allPixels1";

    //file storage
    private File file;
    private String path;
    private static final String app_dir = "mobv";

    //fab
    private FloatingActionButton fab, fab1, fab2, fab3, fab4;
    private boolean isFABOpen = false;
    private Animation rotate_forward,rotate_backward;
    private boolean disable = false;

    //onresultCallbacks
    private boolean user_info_fab_menu = false;
    private boolean user_posts_fab_menu = false;
    private float user_posts_all_pixels;
    private int user_posts_id;

    //logoutCall
    private boolean logout = false;

    //firebase param
    private static String fb_user_uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coordinatorLayout =  (CoordinatorLayout) findViewById(R.id.main);
        context = getApplicationContext();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            fb_user_uid = extras.getString("user_uid");
        }

        initRecyclerView();
        initSwipe();
        initFAB();
    }

    @Override
    protected void onResume() {
        super.onResume();
        disableAfterReturm=true;
        getAllPixelsEnd = -1.f;
        items = (RecyclerView) findViewById(R.id.card_recycler_view);
        ViewTreeObserver vto = items.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                items.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                calculatePositionAndScroll(items);
            }
        });
        itemTouchHelper.attachToRecyclerView(items);
        adapter.notifyDataSetChanged();
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
        items.setItemViewCacheSize(6);
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
                    }
                    else {
                        FABMenuClose();
                        disable = true;
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                allPixels += dx;
            }
        });

        list = getData();
        adapter = new ExtraItemsAdapter(getApplicationContext(),list,true);
        items.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        items.scrollToPosition(items.getAdapter().getItemCount()-1);
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
                Log.d("position", Integer.toString(position));

                if (direction == ItemTouchHelper.UP){
                    FABMenuClose();
                    adapter.onViewDetachedFromWindow((ViewHolder) viewHolder);
                    userPosts(false);
                } else if(direction == ItemTouchHelper.DOWN) {
                    adapter.onViewDetachedFromWindow((ViewHolder) viewHolder);
                    FABMenuClose();
                    userInfo(false);
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
        if(getAllPixelsEnd==allPixels && getGetAllPixelsStart==allPixels && !disableAfterReturm){
            updateRecycler();
        }
        else {
            if(!disableAfterReturm) {
                getAllPixelsEnd = allPixels;
            }
            else {
                disableAfterReturm=false;
            }
            int expectedPosition = Math.round((allPixels + padding - firstItemWidth) / itemWidth);
            if (expectedPosition == 0) {
                expectedPosition = -1;
            } else if (expectedPosition <= (-1) * recyclerView.getAdapter().getItemCount() - 1) {
                expectedPosition++;
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
                    cameraInit("image");
                }
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!disable){
                    cameraInit("video");
                }
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!disable) {
                    galeryInit();
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
                            builder.setMessage("Logout ?");
                            builder.setCancelable(true);
                            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    logout=true;
                                    finish();
                                }
                            });
                            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            builder.setNeutralButton("SWITCH OFF", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FABMenuClose();
                                    logout=false;
                                    finish();
                                }
                            });
                            builder.show();
                        }
                    });
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
            fab1.setClickable(false);
            fab2.setClickable(false);
            fab3.setClickable(false);
            fab4.setClickable(false);
            isFABOpen = false;
        }
    }

    private void FABMenuOpen(){
        if(!isFABOpen){
            fab.startAnimation(rotate_forward);
            fab1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
            fab2.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
            fab3.animate().translationY(-getResources().getDimension(R.dimen.standard_155));
            fab4.animate().translationY(-getResources().getDimension(R.dimen.standard_205));
            fab1.setClickable(true);
            fab2.setClickable(true);
            fab3.setClickable(true);
            fab4.setClickable(true);
            isFABOpen = true;
        }
    }

    private void galeryInit(){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if (MyVersion >= Build.VERSION_CODES.M) {
            if (!PermissionsAllow.checkIfAlreadyhavePermissionWrite(this)) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
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
        startActivityForResult(mediaChooser, RESULT_LOAD_IMAGE);
    }

    private void cameraInit(String type){
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
            if (!PermissionsAllow.checkIfAlreadyhavePermissionWrite(this)) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
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
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
        else if (type.equalsIgnoreCase("video")) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST ) {
            if(resultCode == Activity.RESULT_OK ){
                if (MyVersion >= Build.VERSION_CODES.M) {
                    if (!PermissionsAllow.checkIfAlreadyhavePermissionRead(this)) {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                }
                new ApiPostRequest(path.toString(), MainActivity.this, this.getApplicationContext(),coordinatorLayout, toast, adapter, items, list);
                FABMenuClose();
            }
            else if(user_info_fab_menu){
                user_info_fab_menu = false;
                userInfo(true);
            }
            else if(user_posts_fab_menu){
                user_posts_fab_menu = false;
                userPosts(true);
            }
        }
        if (requestCode == RESULT_LOAD_IMAGE) {
            if(resultCode == RESULT_OK && null != data){
                if (MyVersion >= Build.VERSION_CODES.M) {
                    if (!PermissionsAllow.checkIfAlreadyhavePermissionRead(this)) {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                }
                String path = ImageFilePath.getPath(MainActivity.this, data.getData());
                new ApiPostRequest(path.toString(), MainActivity.this, this.getApplicationContext(),coordinatorLayout, toast, adapter, items, list);
                FABMenuClose();
            }
            else if(user_info_fab_menu){
                user_info_fab_menu = false;
                userInfo(true);
            }
            else if(user_posts_fab_menu){
                user_posts_fab_menu = false;
                userPosts(true);
            }
        }
        if (requestCode == USER_INFO_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                String call =data.getStringExtra("call");
                switch (call){
                    case "image":
                        user_info_fab_menu= true;
                        cameraInit("image");
                        break;
                    case "video":
                        user_info_fab_menu= true;
                        cameraInit("video");
                        break;
                    case "galery":
                        user_info_fab_menu= true;
                        galeryInit();
                        break;
                    case "logout":
                        logout=true;
                        finish();
                        break;
                    case "switchoff":
                        logout=false;
                        finish();
                        break;
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                onResume();
            }
        }
        if (requestCode == USER_POSTS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                String call =data.getStringExtra("call");
                user_posts_id = data.getIntExtra("id", 0);
                user_posts_all_pixels = data.getFloatExtra("allPixels", 0);
                switch (call){
                    case "image":
                        user_posts_fab_menu= true;
                        cameraInit("image");
                        break;
                    case "video":
                        user_posts_fab_menu= true;
                        cameraInit("video");
                        break;
                    case "galery":
                        user_posts_fab_menu= true;
                        galeryInit();
                        break;
                    case "logout":
                        logout=true;
                        finish();
                        break;
                    case "switchoff":
                        logout=false;
                        finish();
                        break;

                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                onResume();
            }
        }
    }

    private void readOrCreateDir(){
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

    private void updateRecycler(){
        getAllPixelsEnd = -1.f;
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this.getApplicationContext(), "Update", Toast.LENGTH_SHORT);
        toast.show();
    }

    private void userInfo(boolean fabMenu){
        getAllPixelsEnd = -1.f;
        itemTouchHelper.attachToRecyclerView(null);
        Intent intent = new Intent(this.getApplicationContext(), UserInfo.class);
        intent.putExtra("fab_menu",fabMenu);
        startActivityForResult(intent,USER_INFO_REQUEST);
        if(!fabMenu) {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    private void userPosts(boolean fabMenu){
        getAllPixelsEnd = -1.f;
        itemTouchHelper.attachToRecyclerView(null);
        Intent intent = new Intent(this.getApplicationContext(), UserPosts.class);
        intent.putExtra("fab_menu",fabMenu);
        intent.putExtra("id",user_posts_id);
        intent.putExtra("allPixels",user_posts_all_pixels);
        startActivityForResult(intent,USER_POSTS_REQUEST);
        if(!fabMenu){
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

    public static List<Data> getData() {
        List<Data> subActivityData = new ArrayList<>();
        String[] cardTitle = {
                "Card 1",
                "Card 2",
                "Card 3",
                "Card 4",
                "Card 5",
                "Card 6",
                "Card 7",
                "Card 8",
                "Card 9",
                "Card 10",
                "Card 11",
                "Card 12",
                "Card 13",
                "Card 14",
                "Card 15",
                "Card 16",
                "Card 17",
                "Card 18",
                "Card 19",
                "Card 20",
                "Card 21",
                "Card 22",
                "Card 23",
                "Card 24",
                "Card 25",
                "Card 26",
                "Card 27",
                "Card 28",
                "Card 29",
                "Card 30",
                "Card 31"
        };
        String[] android_image_urls = {
                "http://api.learn2crack.com/android/images/donut.png",
                "http://api.learn2crack.com/android/images/eclair.png",
                "http://api.learn2crack.com/android/images/froyo.png",
                "http://api.learn2crack.com/android/images/ginger.png",
                "http://api.learn2crack.com/android/images/honey.png",
                "http://api.learn2crack.com/android/images/icecream.png",
                "http://api.learn2crack.com/android/images/donut.png",
                "http://api.learn2crack.com/android/images/eclair.png",
                "http://api.learn2crack.com/android/images/froyo.png",
                "http://api.learn2crack.com/android/images/ginger.png",
                "http://api.learn2crack.com/android/images/honey.png",
                "http://api.learn2crack.com/android/images/icecream.png",
                "http://api.learn2crack.com/android/images/donut.png",
                "http://api.learn2crack.com/android/images/eclair.png",
                "http://api.learn2crack.com/android/images/froyo.png",
                "http://api.learn2crack.com/android/images/ginger.png",
                "http://api.learn2crack.com/android/images/honey.png",
                "http://api.learn2crack.com/android/images/icecream.png",
                "http://api.learn2crack.com/android/images/donut.png",
                "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4",
                "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4",
                "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4",
                "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4",
                "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4",
                "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4",
                "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4",
                "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4",
                "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4",
                "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4",
                "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4",
                "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4"
        };

        for (int i = 0; i < cardTitle.length; i++) {
            Data current = new Data();
            current.setData(cardTitle[i]);
            current.setUrl(android_image_urls[i]);
            current.setDate(new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()).toString());
            current.setUser(fb_user_uid);
            subActivityData.add(current);
        }
        return subActivityData;
    }

}

