package com.shy.progressview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;

public class MainActivity extends Activity {

    private HorizontalProgressBar progressView;
    private CircleProgressBar mCircle;

    private static int MSG_UPDATE = 0x110;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int progress = progressView.getProgress();
            progress = mCircle.getProgress();
            progress++;
            progressView.setProgress(progress);
            mCircle.setProgress(progress);
            if(progress >= 100){
                handler.removeCallbacksAndMessages(null);
            }
            handler.sendEmptyMessageDelayed(MSG_UPDATE,100);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressView = (HorizontalProgressBar)findViewById(R.id.progress);
        mCircle  = (CircleProgressBar)findViewById(R.id.circle_progress);

        handler.sendEmptyMessage(MSG_UPDATE);
    }


}
