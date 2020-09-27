package com.simaple.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.awen.image.photopick.bean.Photo
import com.awen.image.photopick.ui.VideoPlayActivity
import com.bumptech.glide.Glide
import com.simaple.R

class SampleAdapter(mContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), View.OnClickListener {

    private val context = mContext
    private val ITEM_IMAGE = 0
    private val ITEM_VIDEO = 1
    private var photos = ArrayList<Photo>()

    fun addData(photos: ArrayList<Photo>) {
        val lastIndex = itemCount
        if (this.photos.addAll(photos)) {
            notifyItemRangeInserted(lastIndex, photos.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_IMAGE) {
            ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false))
        } else {
            VideoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val photo = photos[position]
        when (holder.itemViewType) {
            ITEM_IMAGE -> {
                photo.let {
                    holder as ImageViewHolder
                    if (!it.uri.isNullOrEmpty()) {
                        Glide.with(context).load(Uri.parse(it.uri)).into(holder.image)
                    } else {
                        //通过图片裁剪或取到的path，存在app私有目录下，可直接访问
                        //val uri: Uri = UriUtils.getImageContentUri(context,it.path)
                        Glide.with(context).load(it.path).into(holder.image)
                    }
                }
            }
            ITEM_VIDEO -> {
                photo.let {
                    holder as VideoViewHolder
                    holder.play.tag = position
                    holder.play.setOnClickListener(this)
                    if (!it.uri.isNullOrEmpty()) {
                        Glide.with(context).load(Uri.parse(it.uri)).into(holder.video)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (photos[position].isVideo) ITEM_VIDEO else ITEM_IMAGE
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image)
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val video: ImageView = itemView.findViewById(R.id.video)
        val play: ImageView = itemView.findViewById(R.id.play)

    }

    override fun onClick(v: View?) {
        val position: Int = v?.tag as Int
        if (v.id == R.id.play) {
            context.startActivity(Intent(context, VideoPlayActivity::class.java)
                    .putExtra("videoUrl", photos[position].uri))
        }
    }

}