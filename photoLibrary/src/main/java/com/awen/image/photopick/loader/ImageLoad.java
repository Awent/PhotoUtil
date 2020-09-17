package com.awen.image.photopick.loader;

import android.view.View;
import android.widget.FrameLayout;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.chrisbanes.photoview.OnViewTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import java.io.File;

public interface ImageLoad {

    int PROGRESS_NUM = 0;
    int PROGRESS_GONE = 1;
    int PROGRESS_VISIBLE = 2;

    void load(FrameLayout parent, PhotoView photoView, String url);

    void loadGif(PhotoView photoView,String url);

    SubsamplingScaleImageView getSubsamplingScaleImageView(File file, int bitmapWidth, int bitmapHeight, String url) ;

    void loadLongHorizontalImage(SubsamplingScaleImageView imageView, File file,float hScale);

    void loadLongVerticalImage(SubsamplingScaleImageView imageView, File file);

    void setOnClickListener(View.OnClickListener onClickListener);

    void setOnLongClickListener(View.OnLongClickListener onLongClickListener);

    void setOnViewTapListener(OnViewTapListener onViewTapListener);

    void onDestroy();

}
