package com.awen.image.photopick.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;

import com.awen.image.PhotoSetting;
import com.awen.image.photopick.loader.ImageLoad;
import com.awen.image.photopick.loader.ImageLoadImpl;
import com.awen.image.photopick.widget.RoundProgressBar;
import com.github.chrisbanes.photoview.OnViewTapListener;
import com.github.chrisbanes.photoview.PhotoView;

import java.lang.ref.WeakReference;

@SuppressLint("ViewConstructor")
public class MyPhotoView extends FrameLayout {
    private static final String TAG = "MyPhotoView";
    private PhotoView photoView;
    private RoundProgressBar progressBar;
    private ImageLoadImpl imageLoad;

    public MyPhotoView(@NonNull Context context,
                       int position,
                       int screenWith,
                       int screenHeight,
                       int errorResId) {
        super(context);
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //add photoView
        photoView = new PhotoView(context);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        photoView.setLayoutParams(params);
        addView(photoView);

        //add progressBar
        progressBar = new RoundProgressBar(context, screenWith >> 4);
        FrameLayout.LayoutParams bParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        bParams.gravity = Gravity.CENTER;
        addView(progressBar, bParams);
        PreHandler handler = new PreHandler(this);
        imageLoad = new ImageLoadImpl(
                context,
                handler,
                position,
                screenWith,
                screenHeight,
                errorResId);
    }

    public void load(final String url) {
        imageLoad.load(this, photoView, url);
    }

    private void updateProgress(int progress) {
        if (PhotoSetting.DEBUG) {
//            Log.e(TAG, " --progress = " + progress);
        }
        boolean isDone = progress == 100;
        if (isDone) {
            progressBar.setVisibility(GONE);
        } else {
            progressBar.setVisibility(VISIBLE);
            progressBar.setProgress(progress);
        }
    }

    private static class PreHandler extends Handler {
        private WeakReference<MyPhotoView> rf;

        public PreHandler(MyPhotoView instance) {
            rf = new WeakReference<>(instance);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            MyPhotoView photoView = rf.get();
            if (photoView == null) {
                return;
            }
            if (msg.what == ImageLoad.PROGRESS_NUM) {
                photoView.updateProgress(msg.arg1);
            } else if (msg.what == ImageLoad.PROGRESS_GONE) {
                photoView.progressBar.setVisibility(GONE);
            } else if (msg.what == ImageLoad.PROGRESS_VISIBLE) {
                photoView.progressBar.setVisibility(VISIBLE);
            }
        }
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        imageLoad.setOnClickListener(onClickListener);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        imageLoad.setOnLongClickListener(onLongClickListener);
    }

    public void setOnViewTapListener(OnViewTapListener onViewTapListener) {
        imageLoad.setOnViewTapListener(onViewTapListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        imageLoad.onDestroy();
    }
}
