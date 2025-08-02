package com.lfgit.utilites

import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract

/** Helper class providing URI to storage conversion */
object UriHelper {

    /** Returns a path to storage from a Uniform Resource Identifier (URI) */
    fun getStoragePathFromURI(context: Context, uri: Uri): String? {
        val docUri = DocumentsContract.buildDocumentUriUsingTree(
            uri,
            DocumentsContract.getTreeDocumentId(uri)
        )
        return getPath(context, docUri)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getPath(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return if (split.size > 1 && split[1].isNotEmpty()) {
                        "${Environment.getExternalStorageDirectory()}/${split[1]}"
                    } else {
                        Environment.getExternalStorageDirectory().toString()
                    }
                } else {
                    val rootStorage = "/storage/$type"
                    return if (split.size > 1 && split[1].isNotEmpty()) {
                        "$rootStorage/${split[1]}"
                    } else {
                        rootStorage
                    }
                }
            }
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }
}