package com.abduhanifan.dicoding.githubuserapp

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.abduhanifan.dicoding.githubuserapp.adapter.TabsPagerAdapter
import com.abduhanifan.dicoding.githubuserapp.db.DatabaseContract.FavoriteColumns.Companion.AVATAR_URL
import com.abduhanifan.dicoding.githubuserapp.db.DatabaseContract.FavoriteColumns.Companion.LOGIN
import com.abduhanifan.dicoding.githubuserapp.db.DatabaseContract.FavoriteColumns.Companion.TYPE
import com.abduhanifan.dicoding.githubuserapp.db.FavoriteHelper
import com.abduhanifan.dicoding.githubuserapp.model.UserItem
import com.abduhanifan.dicoding.githubuserapp.viewModel.DetailViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.layout_tabs.*

class DetailActivity : AppCompatActivity() {

    private lateinit var detailViewModel: DetailViewModel
    private lateinit var favoriteHelper: FavoriteHelper

    private var menuItem: Menu? = null
    private var statusFavorite: Boolean = false
    private lateinit var username: UserItem

    companion object {
        const val EXTRA_USER = "extra_user"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        supportActionBar?.setTitle(R.string.detail_label)
        supportActionBar?.elevation = 0F
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        favoriteHelper = FavoriteHelper.getInstance(applicationContext)
        favoriteHelper.open()

        username = intent.getParcelableExtra(EXTRA_USER) as UserItem

        detailViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(DetailViewModel::class.java)
        detailViewModel.setDetailUser(username.login.toString())

        detailViewModel.getDetailUser().observe(this, Observer { detailUserItem ->
            if (detailUserItem != null) {
                Glide.with(this)
                    .load(detailUserItem.avatar_url)
                    .apply(RequestOptions().override(86, 86))
                    .into(imgAvatar)
                textName.text = detailUserItem.name
                textCompany.text = detailUserItem.company
                textFollower.text = detailUserItem.followers
                textFollowing.text = detailUserItem.following
                textLocation.text = detailUserItem.location
                textRepository.text = detailUserItem.public_repos
            }
        })

        showTabs()
        favoriteState()
    }

    private fun favoriteState() {
        val result = favoriteHelper.queryByLogin(username.login.toString())
        val favorite = (1..result.count).map {
            result.apply {
                moveToNext()
                getInt(result.getColumnIndexOrThrow(LOGIN))
            }
        }
        if (favorite.isNotEmpty()) statusFavorite = true
    }

    // Menambahkan data Favorite user ke SQLite
    private fun addFavorite() {
        try {
            val values = ContentValues().apply {
                put(LOGIN, username.login)
                put(AVATAR_URL, username.avatar_url)
                put(TYPE, username.type)
            }

            favoriteHelper.insert(values)

            showSnackbarMessage("Menambahkan Favorite")
            Log.d("Masukan Nilai ::::: ", values.toString())
        } catch (e: SQLiteConstraintException) {
            showSnackbarMessage("" + e.localizedMessage)
        }
    }

    // Menghapus data Favorite user dari SQLite
    private fun removeFavorite() {
        try {
            val result = favoriteHelper.deleteByLogin(username.login.toString())

            showSnackbarMessage("Menghapus Favorite")
            Log.d("Hapus nilai ::::: ", result.toString())
        } catch (e: SQLiteConstraintException) {
            showSnackbarMessage("" + e.localizedMessage)
        }
    }

    private fun setFavorite() {
        if (statusFavorite) {
            menuItem?.getItem(0)?.icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_favorite_24)
        } else {
            menuItem?.getItem(0)?.icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_favorite_border_24)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        menuItem = menu
        setFavorite()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_setting -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_favorite -> {
                if (statusFavorite) removeFavorite() else addFavorite()

                statusFavorite = !statusFavorite
                setFavorite()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showTabs() {
        val sectionsPagerAdapter = TabsPagerAdapter(this, supportFragmentManager)
        viewPager.adapter = sectionsPagerAdapter
        tabs.setupWithViewPager(viewPager)
    }

    // fungsi back button support action bar
    override fun onSupportNavigateUp(): Boolean {
        super.onBackPressed()
        return true
    }

    // Tampilkan snackbar
    private fun showSnackbarMessage(message: String) {
        Snackbar.make(viewPager, message, Snackbar.LENGTH_SHORT).show()
    }
}
