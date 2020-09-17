package com.awen.image.photopick.ui;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.awen.image.PhotoUtil;
import com.awen.image.R;

public class VideoPlayLayout extends FrameLayout {

    private Context context;
    private Uri videoUri;

    public VideoPlayLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VideoPlayLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoPlayLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
    }

    private void addPlayButton() {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.mipmap.video_play_icon);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        LayoutParams params = new LayoutParams(200, 200);
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
        addView(imageView);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoUtil.startVideoActivity((Activity) context,videoUri);
            }
        });
    }

    public void setData(View photoDraweeView,Uri videoUri) {
        this.videoUri = videoUri;
        addView(photoDraweeView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addPlayButton();
    }

}
