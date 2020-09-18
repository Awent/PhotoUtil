package com.simaple.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awen.image.photopick.bean.Photo
import com.simaple.R
import com.simaple.adapter.SampleAdapter

class SampleListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        title = "my choose"
        val photos:ArrayList<Photo> = intent.getParcelableArrayListExtra<Photo>("photos") as ArrayList<Photo>
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val adapter = SampleAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.addData(photos)
    }
}