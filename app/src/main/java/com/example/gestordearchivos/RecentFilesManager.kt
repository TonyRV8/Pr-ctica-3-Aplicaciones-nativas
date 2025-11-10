package com.example.gestordearchivos

import android.content.Context

class RecentFilesManager(context: Context) {

    private val prefs = context.getSharedPreferences("recent_files", Context.MODE_PRIVATE)

    fun addFile(path: String) {
        val recentFiles = getRecentFiles().toMutableList()
        recentFiles.remove(path)
        recentFiles.add(0, path)
        val editor = prefs.edit()
        editor.putStringSet("files", recentFiles.take(10).toSet())
        editor.apply()
    }

    fun getRecentFiles(): List<String> {
        return prefs.getStringSet("files", emptySet())?.toList() ?: emptyList()
    }
}
