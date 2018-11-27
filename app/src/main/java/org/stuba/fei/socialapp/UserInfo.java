package org.stuba.fei.socialapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by Marian-PC on 17.11.2018.
 */

public class UserInfo extends AppCompatActivity {
    //swipe
    float initialX;

    //fab
    private FloatingActionButton fab, fab1, fab2, fab3, fab4;
    private boolean isFABOpen = false;
    private boolean callBack = false;
    private boolean swap = false;
    private Animation rotate_forward,rotate_backward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        Bundle extras = getIntent().getExtras();
        initFAB();
        if (extras != null) {
            boolean value = extras.getBoolean("fab_menu",false);
            if(value){
                FABMenuOpen();
            }
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
            switch(action)
            {
                case MotionEvent.ACTION_DOWN:
                    initialX = event.getX();
                    break;

                case MotionEvent.ACTION_UP:
                    float finalX = event.getX();
                    if (initialX > finalX) {
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
                callBack = true;
                Intent returnIntent = new Intent();
                returnIntent.putExtra("call","image");
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callBack=true;
                Intent returnIntent = new Intent();
                returnIntent.putExtra("call","video");
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callBack = true;
                Intent returnIntent = new Intent();
                returnIntent.putExtra("call","galery");
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });
        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.user_info).post(new Runnable() {
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(UserInfo.this);
                        builder.setTitle("");
                        builder.setMessage("Logout ?");
                        builder.setCancelable(true);
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                callBack = true;
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("call","logout");
                                setResult(Activity.RESULT_OK,returnIntent);
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
                                returnIntent.putExtra("call","switchoff");
                                setResult(Activity.RESULT_OK,returnIntent);
                                finish();
                            }
                        });
                        builder.show();
                    }
                });

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
        fab1.setClickable(false);
        fab2.setClickable(false);
        fab3.setClickable(false);
        fab4.setClickable(false);
        isFABOpen = false;
    }

    private void FABMenuOpen(){
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
