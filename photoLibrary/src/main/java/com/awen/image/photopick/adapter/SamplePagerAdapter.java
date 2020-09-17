package com.awen.image.photopick.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.awen.image.photopick.bean.PhotoPagerBean;
import com.awen.image.photopick.ui.MyPhotoView;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.chrisbanes.photoview.OnViewTapListener;

public class SamplePagerAdapter extends PagerAdapter {

    private PhotoPagerBean photoPagerBean;
    private int screenWith, screenHeight;
    private OnViewTapListener onViewTapListener;
    private View.OnClickListener onClickListener;
    private View.OnLongClickListener onLongClickListener;

    public SamplePagerAdapter(Context context, PhotoPagerBean photoPagerBean) {
        this.photoPagerBean = photoPagerBean;
        screenWith = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    @Override
    public int getCount() {
        return photoPagerBean.getBigImgUrls() == null ? 0 : photoPagerBean.getBigImgUrls().size();
    }

    @NonNull
    @Override
    public View instantiateItem(@NonNull ViewGroup container, final int position) {
        String imageUrl = photoPagerBean.getBigImgUrls().get(position);
        final MyPhotoView photoView = new MyPhotoView(container.getContext(), position, screenWith, screenHeight, photoPagerBean.getErrorResId());
        photoView.setOnClickListener(onClickListener);
        photoView.setOnLongClickListener(onLongClickListener);
        photoView.setOnViewTapListener(onViewTapListener);
        photoView.load(imageUrl);
        container.addView(photoView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        return photoView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (photoPagerBean != null
                && photoPagerBean.getBigImgUrls().size() > 0
                && position < photoPagerBean.getBigImgUrls().size()) {
            View view = ((FrameLayout) object).findViewWithTag(position);
            if (view instanceof SubsamplingScaleImageView) {
                SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) view;
                imageView.recycle();
            }
        }
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    public void setOnViewTapListener(OnViewTapListener onViewTapListener) {
        this.onViewTapListener = onViewTapListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }
}
