package com.abduhanifan.dicoding.githubuserapp.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.abduhanifan.dicoding.githubuserapp.R
import com.abduhanifan.dicoding.githubuserapp.db.FavoriteHelper
import com.abduhanifan.dicoding.githubuserapp.helper.MappingHelper
import com.bumptech.glide.Glide

internal class StackRemoteViewsFactory(private val mContext: Context) :
    RemoteViewsService.RemoteViewsFactory {

    private lateinit var favoriteHelper: FavoriteHelper
    private val widgetItem = ArrayList<Bitmap>()

    override fun onCreate() {
        favoriteHelper = FavoriteHelper.getInstance(mContext)
        if (!favoriteHelper.isOpen()) favoriteHelper.open()
    }

    override fun onDataSetChanged() {
        favoriteHelper = FavoriteHelper.getInstance(mContext)
        if (!favoriteHelper.isOpen()) favoriteHelper.open()

        val identifyToken = Binder.clearCallingIdentity()
        Binder.restoreCallingIdentity(identifyToken)

        try {
            val cursorSearch = favoriteHelper.queryAll()
            val cursor = MappingHelper.mapCursorToArrayList(cursorSearch)
            if (cursor.size > 0) {
                widgetItem.clear()
                for (i in 0 until cursor.size) {
                    try {
                        val avatar = cursor[i].avatar_url.toString()
                        val bitmap = Glide.with(mContext)
                            .asBitmap()
                            .load(avatar)
                            .submit()
                            .get()
                        widgetItem.add(bitmap)
                    } catch (e: Exception) {
                        Log.d("ErrorWidget", e.message.toString())
                        widgetItem.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_baseline_broken_image_24))
                    }
                }
            }
        } catch (e: IllegalStateException) {
            Log.d("ErrorWidget", "${e.message}")
        }
    }

    override fun getViewAt(position: Int): RemoteViews {
        val remote = RemoteViews(mContext.packageName, R.layout.widget_item)
        remote.setImageViewBitmap(R.id.imageView, widgetItem[position])

        val extras = bundleOf(StackWidget.EXTRA_ITEM to position)

        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)

        remote.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return remote
    }

    override fun onDestroy() {}

    override fun getCount(): Int = widgetItem.size

    override fun getLoadingView(): RemoteViews? = null

    override fun getItemId(position: Int): Long = 0

    override fun getViewTypeCount(): Int = 1

    override fun hasStableIds(): Boolean = false

}