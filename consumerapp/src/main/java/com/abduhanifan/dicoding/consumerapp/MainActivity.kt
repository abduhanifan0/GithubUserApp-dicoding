package com.abduhanifan.dicoding.consumerapp

import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.abduhanifan.dicoding.consumerapp.adapter.FavoriteAdapter
import com.abduhanifan.dicoding.consumerapp.db.DatabaseContract.FavoriteColumns.Companion.CONTENT_URI
import com.abduhanifan.dicoding.consumerapp.helper.MappingHelper
import com.abduhanifan.dicoding.consumerapp.model.FavoriteItem
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: FavoriteAdapter

    companion object {
        const val EXTRA_STATE = "EXTRA_STATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "Consumer Github App"

        adapter = FavoriteAdapter()
        rv_favorite.layoutManager = LinearLayoutManager(this)
        rv_favorite.setHasFixedSize(true)
        rv_favorite.adapter = adapter

        val handlerThread = HandlerThread("DataObserver")
            handlerThread.start()
        val handler = Handler(handlerThread.looper)
        val myObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                loadFavoriteAsync()
            }
        }
        contentResolver.registerContentObserver(CONTENT_URI, true, myObserver)

        if (savedInstanceState == null) {
            loadFavoriteAsync()
        } else {
            val list = savedInstanceState.getParcelableArrayList<FavoriteItem>(EXTRA_STATE)
            if (list != null) {
                adapter.listFavorite = list
            }
        }
    }

    private fun loadFavoriteAsync() {
        GlobalScope.launch(Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE
            val defferedFavorite = async(Dispatchers.IO) {
                val cursor = contentResolver?.query(CONTENT_URI, null, null, null, null)
                MappingHelper.mapCursorToArrayList(cursor)
            }
            val favorite = defferedFavorite.await()
            progressBar.visibility = View.INVISIBLE
            if (favorite.size > 0) {
                adapter.listFavorite = favorite
            } else {
                adapter.listFavorite = ArrayList()
                showSnackbarMessage("Tidak ada data saat ini")
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listFavorite)
    }

    private fun showSnackbarMessage(message: String) {
        Snackbar.make(rv_favorite, message, Snackbar.LENGTH_SHORT).show()
    }
}
