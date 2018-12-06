package org.stuba.fei.socialapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Marian-PC on 17.11.2018.
 */

public class UserPosts extends AppCompatActivity {
    //static variables
    private static final String BUNDLE_LIST_PIXELS = "allPixels";

    //recycler view
    private ExtraItemsAdapter adapter;
    private RecyclerView items;
    private float itemHeight;
    private float padding;
    private float firstItemHeight;
    private float allPixels;
    private float allPixelsPom = 0;
    private float getAllPixelsEnd = -1.f;
    private float getGetAllPixelsStart = 0.f;
    private int id;

    //fab
    private FloatingActionButton fab, fab1, fab2, fab3, fab4;
    private boolean isFABOpen = false;
    private boolean disable = false;
    private boolean callBack = false;
    private Animation rotate_forward,rotate_backward;

    //logoutCall
    private boolean logout = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_posts);

        initRecyclerView();
        initFAB();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            boolean value = extras.getBoolean("fab_menu",false);
            if(value){
                int idd = extras.getInt("id",0);
                this.allPixels = extras.getFloat("allPixels",0);
                scrollListToPositionOnResume(items,idd);
                FABMenuOpen();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        items = (RecyclerView) findViewById(R.id.card_recycler_view2);
        ViewTreeObserver vto = items.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                items.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                calculatePositionAndScroll(items);
            }
        });
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
                allPixels += dy;
            }
        });

        adapter = new ExtraItemsAdapter(getApplicationContext(),getData(),false);
        items.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void calculatePositionAndScroll(RecyclerView recyclerView) {
        //swipe in top of recycler
        if(getAllPixelsEnd==allPixels && getGetAllPixelsStart==allPixels){
            //recyclerView.onChildDetachedFromWindow((View) recyclerView.getParent());
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
        id = (int) expectedPosition;
        if (missingPx != 0) {
            recyclerView.smoothScrollBy(0, (int) missingPx);
        }
        allPixelsPom = allPixels;

    }
    private void scrollListToPositionOnResume(RecyclerView recyclerView, int expectedPosition) {
        float targetScrollPos = expectedPosition * itemHeight + firstItemHeight - padding;
        recyclerView.scrollToPosition(expectedPosition+1);
        float missingPx = targetScrollPos - allPixels;
        id = (int) expectedPosition;
        if (missingPx != 0) {
            recyclerView.smoothScrollBy(0, (int) missingPx);
        }
        allPixelsPom = allPixels;
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
                if(!disable) {
                    FABMenu();
                }
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!disable) {
                    callBack = true;
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("call", "image");
                    returnIntent.putExtra("allPixels",allPixelsPom);
                    returnIntent.putExtra("id", id);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!disable) {
                    callBack = true;
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("call", "video");
                    returnIntent.putExtra("allPixels", allPixelsPom);
                    returnIntent.putExtra("id", id);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!disable) {
                    callBack = true;
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("call", "galery");
                    returnIntent.putExtra("allPixels", allPixelsPom);
                    returnIntent.putExtra("id", id);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
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
                            builder.setMessage("Logout ?");
                            builder.setCancelable(true);
                            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    callBack = true;
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("call", "logout");
                                    returnIntent.putExtra("allPixels", allPixelsPom);
                                    returnIntent.putExtra("id", id);
                                    setResult(Activity.RESULT_OK, returnIntent);
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
                                    callBack = true;
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("call", "switchoff");
                                    returnIntent.putExtra("allPixels", allPixelsPom);
                                    returnIntent.putExtra("id", id);
                                    setResult(Activity.RESULT_OK, returnIntent);
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

    private void FABMenuOpen() {
        if (!isFABOpen) {
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

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static ArrayList<Data> reverse(ArrayList<Data> list) {
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
            current.setDate(new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()).toString());
            current.setUser("Admin");
            current.setUrl(android_image_urls[i]);
            subActivityData.add(current);
        }
        return reverse((ArrayList<Data>) subActivityData);
    }
}
