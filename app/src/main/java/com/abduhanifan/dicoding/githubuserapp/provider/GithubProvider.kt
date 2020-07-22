package com.abduhanifan.dicoding.githubuserapp.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri

import com.abduhanifan.dicoding.githubuserapp.db.DatabaseContract.AUTHORITY
import com.abduhanifan.dicoding.githubuserapp.db.DatabaseContract.FavoriteColumns.Companion.CONTENT_URI
import com.abduhanifan.dicoding.githubuserapp.db.DatabaseContract.FavoriteColumns.Companion.TABLE_NAME
import com.abduhanifan.dicoding.githubuserapp.db.FavoriteHelper

class GithubProvider : ContentProvider() {

    companion object {
        private const val GITHUB = 1
        private const val GITHUB_ID = 2
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        private lateinit var githubHelper: FavoriteHelper

        init {
            uriMatcher.addURI(AUTHORITY, TABLE_NAME, GITHUB)
            uriMatcher.addURI(AUTHORITY, "$TABLE_NAME/#", GITHUB_ID)
        }
    }

    override fun onCreate(): Boolean {
        githubHelper = FavoriteHelper.getInstance(context as Context)
        githubHelper.open()
        return true
    }

    override fun query(uri: Uri, strings: Array<String>?, s: String?, strings1: Array<String>?, s1: String?): Cursor? {
        return when (uriMatcher.match(uri)) {
            GITHUB -> githubHelper.queryAll()
            GITHUB_ID -> githubHelper.queryByLogin(uri.lastPathSegment.toString())
            else -> null
        }
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val added: Long = when (GITHUB) {
            uriMatcher.match(uri) -> githubHelper.insert(values)
            else -> 0
        }

        context?.contentResolver?.notifyChange(CONTENT_URI, null)

//        Log.e("content-provider", "${values?.get("_id")}")
        return Uri.parse("$CONTENT_URI/$added")
    }

    override fun update(uri: Uri, values: ContentValues?, s: String?, strings: Array<String>?): Int = 0

    override fun delete(uri: Uri, s: String?, strings: Array<String>?): Int {
        val delete: Int = when (GITHUB_ID) {
            uriMatcher.match(uri) -> githubHelper.deleteByLogin(uri.lastPathSegment.toString())
            else -> 0
        }

        context?.contentResolver?.notifyChange(CONTENT_URI, null)
        return delete
    }
}
