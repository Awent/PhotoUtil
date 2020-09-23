package com.awen.image.photopick.loader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.awen.image.PhotoSetting;
import com.awen.image.R;
import com.awen.image.photopick.pro.ProgressInterceptor;
import com.awen.image.photopick.pro.ProgressListener;
import com.awen.image.photopick.util.AppPathUtil;
import com.awen.image.photopick.util.CalculateUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.HttpException;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.chrisbanes.photoview.OnViewTapListener;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;

import static android.view.View.VISIBLE;
import static com.awen.image.PhotoSetting.DEBUG;

/**
 * 图片加载
 * 这里会自动判断图片是否为长图，长图分为横向长图和纵向长图
 * 图片加载进度监听参考：https://juejin.im/post/6847902221951041549
 */
public class ImageLoadImpl implements ImageLoad {

    private static final String TAG = "ImageLoadImpl";
    private static final int HORIZONTAL = 0;
    private static final int VERTICAL = 1;
    private static final float ratioX = 1.05f; //大于此值即表示横向长图
    private static final float ratioY = 0.8f; //大于此值即表示纵向长图
    //    private static final float ratioX = 0.25f; //大于此值即表示横向长图
//    private static final float ratioY = 1.05f; //大于此值即表示纵向长图
    private Context context;
    private float screenWith, screenHeight;
    private View.OnClickListener onClickListener;
    private View.OnLongClickListener onLongClickListener;
    private OnViewTapListener onViewTapListener;
    private int position;
    private boolean isLongImage = false;
    private boolean isLoadThumbnail = true;
    private int errorResId;
    private Handler handler;
    private String url;

    public ImageLoadImpl(Context context,
                         Handler handler,
                         int position,
                         int screenWith,
                         int screenHeight,
                         int errorResId) {
        this.context = context;
        this.handler = handler;
        this.screenWith = screenWith;
        this.screenHeight = screenHeight;
        this.position = position;
        this.errorResId = errorResId;
    }

    @Override
    public void load(final FrameLayout parent, final PhotoView photoView, final String url) {
        load(parent, photoView, url, null);
    }

    @Override
    public void load(final FrameLayout parent, final PhotoView photoView, final String url, String thumbnailUrl) {
        this.url = url;
        ProgressInterceptor.addListener(url, progressListener);
        photoView.setOnViewTapListener(onViewTapListener);
        photoView.setOnClickListener(onClickListener);
        if (isGifOrWebp(url)) {
            loadGif(photoView, url,thumbnailUrl);
            return;
        }

        Glide.with(context)
                .asBitmap()
                .load(url)
                .thumbnail(Glide.with(context).asBitmap().load(thumbnailUrl))
                .skipMemoryCache(true)
                .error(errorResId != 0 ? errorResId : R.mipmap.failure_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        if (DEBUG) {
                            Log.e(TAG, "Load failed", e);
                            for (Throwable t : e.getRootCauses()) {
                                if (t instanceof HttpException) {
                                    Log.e(TAG, "Request failed due to HttpException!", t);
                                    break;
                                }
                                Log.e(TAG, "Caused by", t);
                            }
                            // Or, to log all root causes locally, you can use the built in helper method:
                            e.logRootCauses(TAG);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (DEBUG) {
                            Log.e(TAG, "---RequestListener-onResourceReady---");
                        }
                        isLongImage = isLongImage(resource, url);
                        isLoadThumbnail = false;
                        return false;
                    }
                })
                .into(new BitmapImageViewTarget(photoView) {

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        handler.sendEmptyMessage(PROGRESS_VISIBLE);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        removeProListener();
                    }

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                        if(isLoadThumbnail){//如果是小图，也会回调这里，要做下判断
                            return;
                        }
                        removeProListener();
                        if (isLongImage) {
                            String cancelPath = AppPathUtil.getGlideLocalCachePath(url);
                            if (DEBUG) {
                                Log.e(TAG, "cancelPath = " + cancelPath);
                            }
                            if (cancelPath != null) {
                                File file = new File(cancelPath);
                                View view = getSubsamplingScaleImageView(file, resource.getWidth(), resource.getHeight(), url);
                                if (view != null) {//长图
                                    photoView.setVisibility(View.GONE);
                                    view.setTag(position);
                                    parent.addView(view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                                    return;
                                }
                            }
                        }

                        photoViewLoad(photoView, resource);
                    }
                });
    }

    @Override
    public void loadGif(final PhotoView photoView, final String url, String thumbnailUrl) {
        Glide.with(context)
                .asGif()
                .load(url)
                .thumbnail(Glide.with(context).asGif().load(thumbnailUrl))
                .skipMemoryCache(true)
                .error(errorResId != 0 ? errorResId : R.mipmap.failure_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        removeProListener();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        removeProListener();
                        setPhotoViewListener(photoView);
                        return false;
                    }
                })
                .into(photoView);
    }

    @Override
    public SubsamplingScaleImageView getSubsamplingScaleImageView(File file, int bitmapWidth, int bitmapHeight, String url) {
        if (isGifOrWebp(url) || file == null || !file.exists()) {
            return null;
        }
//        float[] ratio = calculateRatio2(bitmapWidth, bitmapHeight);
        float[] ratio = calculateRatio(bitmapWidth, bitmapHeight);
        float offsetW = ratio[0];
        float offsetH = ratio[1];
        if (DEBUG) {
            Log.e(TAG, "offsetW = " + offsetW + ", offsetH = " + offsetH);
//            Log.e(TAG, "imageWidth = " + imageWidth + ",imageHeight = " + imageHeight);
        }
        if (offsetW > ratioX) {//横向长图
            if (DEBUG) {
                Log.e(TAG, "横向长图");
            }
            float hScale = screenHeight / bitmapHeight;
            return getLongImageView(file, HORIZONTAL, hScale / 2);
        } else if (offsetH > ratioY) {//纵向长图
            if (DEBUG) {
                Log.e(TAG, "纵向长图");
            }
            return getLongImageView(file, VERTICAL, 0);
        }
        return null;
    }

    @Override
    public void loadLongHorizontalImage(SubsamplingScaleImageView imageView, File file, float hScale) {
        //            imageView.setMaxScale(hScale);
        imageView.setImage(ImageSource.uri(file.getAbsolutePath()));
    }

    @Override
    public void loadLongVerticalImage(SubsamplingScaleImageView imageView, File file) {
        imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
        imageView.setImage(ImageSource.uri(file.getAbsolutePath()), new ImageViewState(0, new PointF(0, 0), SubsamplingScaleImageView.ORIENTATION_0));
    }

    @Override
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    @Override
    public void setOnViewTapListener(OnViewTapListener onViewTapListener) {
        this.onViewTapListener = onViewTapListener;
    }

    @Override
    public void onDestroy() {
        removeProListener();
    }

    private void removeProListener() {
        handler.sendEmptyMessage(PROGRESS_GONE);
        ProgressInterceptor.removeListener(url);
    }

    private SubsamplingScaleImageView getLongImageView(File file, int orientation, float hScale) {
        SubsamplingScaleImageView imageView = new SubsamplingScaleImageView(context);
        if (orientation == VERTICAL) {//纵向图
            loadLongVerticalImage(imageView, file);
        } else {//横向长图
            loadLongHorizontalImage(imageView, file, hScale);
        }
        imageView.setOnClickListener(onClickListener);
        //图片下载完成并且开启图片保存才给长按保存
        imageView.setOnLongClickListener(onLongClickListener);
        return imageView;
    }


    private boolean isGifOrWebp(String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        return (fileName.contains(".webp") || fileName.contains(".gif"));
    }

    /**
     * 是否是长图
     */
    private boolean isLongImage(Bitmap bitmap, String url) {
        if (isGifOrWebp(url)) {
            return false;
        }
//        float[] ratio = calculateRatio2(bitmap.getWidth(), bitmap.getHeight());
        float[] ratio = calculateRatio(bitmap.getWidth(), bitmap.getHeight());
        return (ratio[0] > ratioX || ratio[1] > ratioY);
    }

    /**
     * 根据图片宽高和屏幕宽高计算出是否是长图
     *
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    private float[] calculateRatio(float imageWidth, float imageHeight) {
        float offsetW = (imageWidth / imageHeight) - (screenWith / screenHeight);
        float offsetH = (imageHeight / imageWidth) - (screenHeight / screenWith);
        return new float[]{offsetW, offsetH};
    }

    private float[] calculateRatio2(float imageWidth, float imageHeight) {
        float offsetW = CalculateUtil.tongFen(imageWidth, imageHeight, screenWith, screenHeight);
        float offsetH = CalculateUtil.tongFen(imageHeight, imageWidth, screenHeight, screenWith);
        return new float[]{offsetW, offsetH};
    }

    private ProgressListener progressListener = new ProgressListener() {
        @Override
        public void onLoadProgress(boolean isDone, int progress) {
            Message msg = Message.obtain();
            msg.what = PROGRESS_NUM;
            msg.arg1 = progress;
            handler.sendMessage(msg);
        }

        @Override
        public void onLoadFailed() {
            handler.sendEmptyMessage(PROGRESS_GONE);
        }
    };

    private void photoViewLoad(PhotoView photoView, Bitmap bitmap) {
        photoView.setVisibility(VISIBLE);
        photoView.setImageBitmap(bitmap);
        setPhotoViewListener(photoView);
    }

    private void setPhotoViewListener(PhotoView photoView) {
        photoView.setOnViewTapListener(onViewTapListener);
        photoView.setOnClickListener(onClickListener);
        photoView.setOnLongClickListener(onLongClickListener);
    }
}
