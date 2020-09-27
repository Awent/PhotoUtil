package com.simaple.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simaple.ImageProvider
import com.simaple.R
import com.simaple.adapter.ListAdapter
import com.simaple.bean.UserBean

/**
 * 这里只是模拟获取网络数据，展示了如何调用查看网络大图功能
 */
class ListActivity : AppCompatActivity() {

    private lateinit var adapter: ListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)
        title = "网络大图示例"
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        adapter = ListAdapter(this,getInfo())
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
    }

    private fun getInfo(): List<UserBean.User>{
        val bigList = ImageProvider.getBigImgList()
        val smallList = ImageProvider.getSmallImgList()
        val size = bigList.size
        val list = ArrayList<UserBean.User>()

        for (index in 0 until size){
            val bean = UserBean.User()
            bean.age = index
            bean.smallAvatar = smallList[index]
            bean.avatar = bigList[index]
            list.add(bean)
        }
        return list
    }
}