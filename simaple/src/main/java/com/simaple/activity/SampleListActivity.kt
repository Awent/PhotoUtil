package com.simaple.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awen.image.PhotoUtil
import com.awen.image.photopick.bean.Photo
import com.simaple.R
import com.simaple.adapter.SampleAdapter

class SampleListActivity : AppCompatActivity() {

    private lateinit var adapter: SampleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)
        title = "my choose"
        val photos: ArrayList<Photo> = intent.getParcelableArrayListExtra<Photo>("photos") as ArrayList<Photo>
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        adapter = SampleAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.addData(photos)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_ok, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.ok) {
            PhotoUtil.pick(this)
                    .maxPickSize(8)
                    .setOnPhotoResultCallback {
                        adapter.addData(it.list)
                    }
                    .build()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}