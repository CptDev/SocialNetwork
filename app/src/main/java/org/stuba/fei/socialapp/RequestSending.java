package org.stuba.fei.socialapp;

/**
 * Created by Marian-PC on 23.11.2018.
 */


import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

public class RequestSending{
    private ObjectAnimator anim;
    private PopupWindow popupWindow;
    private View customView;

    public RequestSending(Context context, Activity activity, CoordinatorLayout mLinearLayout) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(activity.LAYOUT_INFLATER_SERVICE);
        this.customView = inflater.inflate(R.layout.circle_progress_layout,null);
        ProgressBar mprogressBar = (ProgressBar) this.customView.findViewById(R.id.circular_progress_bar);

        this.popupWindow = new PopupWindow(this.customView, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        if(Build.VERSION.SDK_INT>=21){
            this.popupWindow.setElevation(5.0f);
        }
        this.popupWindow.showAtLocation(mLinearLayout, Gravity.CENTER,0,0);

        this.anim = ObjectAnimator.ofInt(mprogressBar, "progress", 0, 100);
        this.anim.setDuration(2000);
        this.anim.setInterpolator(new DecelerateInterpolator());
        this.anim.start();
    }

    public void stopButton(final RequestQueue queue, final Context parentContext, final Toast parentToast) {

        Button stopSendButton = (Button) this.customView.findViewById(R.id.stop_send);
        stopSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queue.cancelAll(new RequestQueue.RequestFilter() {
                    @Override
                    public boolean apply(Request<?> request) {
                        messageToast(parentContext,parentToast);
                        stop();
                        return true;
                    }
                });
                stop();
            }
        });
    }

    public void stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.anim.pause();
        }
        this.popupWindow.dismiss();
    }
    private static void messageToast(Context context, Toast toast){
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, "Sending stopped", Toast.LENGTH_SHORT);
        toast.show();
    }


}
