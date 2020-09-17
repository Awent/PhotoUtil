package com.awen.image.photopick.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.awen.image.R;
import com.awen.image.photopick.bean.Photo;
import com.awen.image.photopick.bean.PhotoPickBean;
import com.awen.image.photopick.controller.PhotoPickConfig;
import com.awen.image.photopick.controller.PhotoPreviewConfig;
import com.awen.image.photopick.ui.ClipPictureActivity;
import com.awen.image.photopick.ui.PhotoPickActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import kr.co.namee.permissiongen.PermissionGen;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by Awen <Awentljs@gmail.com>
 */
public class PhotoPickAdapter extends RecyclerView.Adapter<PhotoPickAdapter.ViewHolder> {
    private final String TAG = getClass().getSimpleName();
    private Context context;
    public static ArrayList<Photo> photos;//图库
    public static ArrayList<String> selectPhotos;//已选择了的图片
    public static ArrayList<Photo> selectPhotoList;//已选择了的图片
    public static ArrayList<Photo> previewPhotos;//要预览选择了的图片
    private PhotoPickBean pickBean;

    public PhotoPickAdapter(Context context, PhotoPickBean pickBean) {
        this.context = context;
        this.pickBean = pickBean;
        if (photos == null) {
            photos = new ArrayList<>();
        }
        if (selectPhotos == null) {
            selectPhotos = new ArrayList<>();
        }
        if (selectPhotoList == null) {
            selectPhotoList = new ArrayList<>();
        }
        if (previewPhotos == null) {
            previewPhotos = new ArrayList<>();
        }
    }

    public void refresh(List<Photo> list) {
        photos.clear();
        photos.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_pick, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return pickBean.isShowCamera() ? (photos == null ? 0 : photos.size() + 1) : (photos == null ? 0 : photos.size());
    }

    private Photo getItem(int position) {
        return pickBean.isShowCamera() ? photos.get(position - 1) : photos.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageView;
        private CheckBox checkbox;
        private ImageView gifIcon, videoIcon;
        private TextView duration;

        ViewHolder(View itemView) {
            super(itemView);
            videoIcon = itemView.findViewById(R.id.videoIcon);
            gifIcon = itemView.findViewById(R.id.gifIcon);
            imageView = itemView.findViewById(R.id.imageView);
            checkbox = itemView.findViewById(R.id.checkbox);
            duration = itemView.findViewById(R.id.duration);
            checkbox.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        public void setData(int position) {
            Uri uri;
            if (pickBean.isShowCamera() && position == 0) {
                checkbox.setVisibility(View.GONE);
                gifIcon.setVisibility(View.GONE);
                videoIcon.setVisibility(View.GONE);
                duration.setText(null);
                imageView.setImageResource(R.mipmap.take_photo);
            } else {
                Photo photo = getItem(position);
                gifIcon.setVisibility(photo.isGif() ? View.VISIBLE : View.GONE);
                videoIcon.setVisibility(photo.isVideo() ? View.VISIBLE : View.GONE);
                duration.setText(photo.isVideo() ? generateTime(photo.getDuration()) : null);

                if (pickBean.isClipPhoto()) {
                    checkbox.setVisibility(View.GONE);
                } else {
                    checkbox.setVisibility(View.VISIBLE);
//                    checkbox.setChecked(selectPhotos.contains(photo.getPath()));
                    checkbox.setChecked(selectPhotoList.contains(photo));
                }
                uri = photo.getUri();
                if (photo.isGif()) {
                    Glide.with(context).asBitmap().load(uri).error(R.mipmap.failure_image).into(imageView);
                } else {
                    Glide.with(context).load(uri).error(R.mipmap.failure_image).transition(withCrossFade()).into(imageView);
                }

            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (v.getId() == R.id.checkbox) {
                Photo photo = getItem(position);
                if (selectPhotoList.contains(photo)) {
                    checkbox.setChecked(false);
                    selectPhotos.remove(photo.getPath());
                    selectPhotoList.remove(photo);
                    previewPhotos.remove(photo);
                } else {
                    if (selectPhotoList.size() == pickBean.getMaxPickSize()) {
                        checkbox.setChecked(false);
                        return;
                    } else {
                        checkbox.setChecked(true);
                        selectPhotos.add(photo.getPath());
                        selectPhotoList.add(photo);
                        previewPhotos.add(photo);
                    }
                }
                if (onUpdateListener != null) {
                    onUpdateListener.updataToolBarTitle(getTitle());
                }
            } else if (v.getId() == R.id.photo_pick_layout) {
                if (pickBean.isShowCamera() && position == 0) {
                    //以下操作会回调Activity中的#selectPicFromCameraSuccess()或selectPicFromCameraFailed()
                    PermissionGen.needPermission((Activity) context, PhotoPickActivity.REQUEST_CODE_PERMISSION_CAMERA, Manifest.permission.CAMERA);
                } else if (pickBean.isClipPhoto()) {//头像裁剪
                    Photo photo = getItem(position);
                    if(pickBean.isSystemClipPhoto()){//启动系统裁剪
                        if(onUpdateListener != null){
                            onUpdateListener.startSystemCrop(photo.getUri());
                            return;
                        }
                    }
                    startClipPic(photo.getPath(), photo.getUri().toString());
                } else {//查看大图
                    new PhotoPreviewConfig.Builder((Activity) context)
                            .setPosition(pickBean.isShowCamera() ? position - 1 : position)
                            .setMaxPickSize(pickBean.getMaxPickSize())
                            .setOriginalPicture(pickBean.isOriginalPicture())
                            .build();
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    public static String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    public void startClipPic(String path, String uri) {
        Intent intent = new Intent(context, ClipPictureActivity.class);
        intent.putExtra(ClipPictureActivity.USER_PHOTO_PATH, path);
        intent.putExtra(ClipPictureActivity.USER_PHOTO_URI, uri);
        ((Activity) context).startActivityForResult(intent, PhotoPickActivity.REQUEST_CODE_CLIPIC);
        ((Activity) context).overridePendingTransition(R.anim.bottom_in, 0);
    }

    /**
     * 如果是多选title才会变化，要不然单选的没有变
     *
     * @return only work for {@link PhotoPickConfig#MODE_MULTIP_PICK}
     */
    public String getTitle() {
        String title = context.getString(R.string.select_photo);
        if (pickBean.getPickMode() == PhotoPickConfig.MODE_MULTIP_PICK && selectPhotos.size() >= 1) {//不是单选，更新title
            title = selectPhotos.size() + "/" + pickBean.getMaxPickSize();
        }
        return title;
    }

    /**
     * 获取已经选择了的图片
     *
     * @return selected photos
     */
    public ArrayList<String> getSelectPhotos() {
        return selectPhotos;
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }

    public static ArrayList<Photo> getSelectPhotoList() {
        return selectPhotoList;
    }

    public static ArrayList<Photo> getPreviewPhotos() {
        return new ArrayList<>(previewPhotos);
    }

    private OnUpdateListener onUpdateListener;

    public void setOnUpdateListener(OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
    }

    public interface OnUpdateListener {
        void updataToolBarTitle(String title);

        void startSystemCrop(Uri uri);
    }

    public void destroy() {
        if (photos != null) {
            photos.clear();
        }
        if (selectPhotos != null) {
            selectPhotos.clear();
        }
        if (selectPhotoList != null) {
            selectPhotoList.clear();
        }
        if (previewPhotos != null) {
            previewPhotos.clear();
        }
        photos = null;
        selectPhotos = null;
        selectPhotoList = null;
        previewPhotos = null;
        onUpdateListener = null;
        pickBean = null;
    }
}
