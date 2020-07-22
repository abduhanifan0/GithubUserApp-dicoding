package com.abduhanifan.dicoding.githubuserapp

import android.content.Intent
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager

import com.abduhanifan.dicoding.githubuserapp.adapter.FavoriteAdapter
import com.abduhanifan.dicoding.githubuserapp.db.DatabaseContract.FavoriteColumns.Companion.CONTENT_URI
import com.abduhanifan.dicoding.githubuserapp.db.FavoriteHelper
import com.abduhanifan.dicoding.githubuserapp.helper.MappingHelper
import com.abduhanifan.dicoding.githubuserapp.model.FavoriteItem
import com.abduhanifan.dicoding.githubuserapp.model.UserItem
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_favorite.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class FavoriteActivity : AppCompatActivity() {

    private lateinit var adapter: FavoriteAdapter
    private lateinit var favoriteHelper: FavoriteHelper

    companion object {
        const val EXTRA_STATE = "EXTRA_STATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        supportActionBar?.setTitle(R.string.activity_favorite)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        showListFavorite()

        favoriteHelper = FavoriteHelper.getInstance(applicationContext)
        favoriteHelper.open()

        val handlerThread = HandlerThread("DataObserver")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        val myObserver = object : ContentObserver(handler) {
            override fun onChange(self: Boolean) {
                loadFavoriteAsync()
            }
        }
        contentResolver.registerContentObserver(CONTENT_URI, true, myObserver)

        if (savedInstanceState == null) {
            loadFavoriteAsync()              // Proses ambil data
        } else {
            val list = savedInstanceState.getParcelableArrayList<FavoriteItem>(EXTRA_STATE)
            if (list != null) {
                adapter.listFavorite = list
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listFavorite)
    }

    private fun showListFavorite() {
        rv_favorite.layoutManager = LinearLayoutManager(this)
        rv_favorite.setHasFixedSize(true)

        adapter = FavoriteAdapter {
            val intent = Intent(this@FavoriteActivity, DetailActivity::class.java)
            val userItem = UserItem()
                userItem.type = it.type
                userItem.avatar_url = it.avatar_url
                userItem.login = it.login
            intent.putExtra(DetailActivity.EXTRA_USER, userItem)
            startActivity(intent)
        }
        rv_favorite.adapter = adapter
    }

    private fun loadFavoriteAsync() {
        GlobalScope.launch(Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE
            val deferredFavorite = async(Dispatchers.IO) {
                val cursor = contentResolver?.query(CONTENT_URI, null, null, null, null)
                MappingHelper.mapCursorToArrayList(cursor)
            }
            val favorite = deferredFavorite.await()
            progressBar.visibility = View.INVISIBLE
            if (favorite.size > 0) {
                adapter.listFavorite = favorite
            } else {
                adapter.listFavorite = ArrayList()
                showSnackbarMessage("Tidak ada data saat ini")
            }
        }
    }

    // fungsi back button support action bar
    override fun onSupportNavigateUp(): Boolean {
        super.onBackPressed()
        return true
    }

    private fun showSnackbarMessage(message: String) {
        Snackbar.make(rv_favorite, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        showListFavorite()
        loadFavoriteAsync()
    }

    override fun onDestroy() {
        super.onDestroy()
        favoriteHelper.close()
    }
}