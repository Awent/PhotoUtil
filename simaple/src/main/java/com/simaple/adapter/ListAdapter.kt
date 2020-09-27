package com.simaple.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.awen.image.PhotoUtil
import com.bumptech.glide.Glide
import com.simaple.R
import com.simaple.bean.UserBean

class ListAdapter(cxt: Context, users: List<UserBean.User>) : RecyclerView.Adapter<ListAdapter.ViewHolder>(), View.OnClickListener {

    private val context = cxt
    private val list = users

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_image, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        list[position].let {
            Glide.with(context).load(Uri.parse(it.smallAvatar)).into(holder.image)
            holder.image.tag = position
            holder.image.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onClick(v: View?) {
        val position: Int = v?.tag as Int
        if (v.id == R.id.imageView) {
            //这里把小图跟大图设置进去即可
            PhotoUtil.browser(context, UserBean.User::class.java)
                    .fromList(list) { item, builder ->
                        builder.addSingleBigImageUrl(item.avatar)
                        builder.addSingleSmallImageUrl(item.smallAvatar)
                    }
                    .setPosition(position)
                    .build()

        }
    }


}