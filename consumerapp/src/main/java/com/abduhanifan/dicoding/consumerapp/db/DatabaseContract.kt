package com.abduhanifan.dicoding.consumerapp.db

import android.net.Uri
import android.provider.BaseColumns

object DatabaseContract {
    const val AUTHORITY = "com.abduhanifan.dicoding.githubuserapp"
    const val SCHEME = "content"

    class FavoriteColumns : BaseColumns {
        companion object {
            private const val TABLE_NAME = "favorite_user"
            const val _ID = "_id"
            const val LOGIN = "login"
            const val AVATAR_URL = "avatar_url"
            const val TYPE = "type"

            // untuk membuat URI content://com.abduhanifan.dicoding.consumerapp
            val CONTENT_URI: Uri = Uri.Builder().scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(TABLE_NAME)
                .build()
        }
    }
}