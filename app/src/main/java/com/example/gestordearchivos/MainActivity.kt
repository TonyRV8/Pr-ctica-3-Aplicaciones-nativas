package com.example.gestordearchivos

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private lateinit var currentPath: String
    private lateinit var toolbar: MaterialToolbar
    private lateinit var breadcrumbLayout: LinearLayout
    private var actionMode: ActionMode? = null

    private var allFiles = listOf<File>()
    private lateinit var recentFilesManager: RecentFilesManager
    private lateinit var favoritesManager: FavoritesManager
    private lateinit var themeManager: ThemeManager
    private var showFavoritesOnly = false

    private var clipboard: List<File>? = null
    private var isMoveOperation = false

    private val storagePermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                loadFiles(currentPath)
            } else {
                Toast.makeText(this, "Permission is required to manage files", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        themeManager = ThemeManager(this)
        themeManager.applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        breadcrumbLayout = findViewById(R.id.breadcrumbLayout)

        recentFilesManager = RecentFilesManager(this)
        favoritesManager = FavoritesManager(this)

        currentPath = Environment.getExternalStorageDirectory().path
        checkAndRequestPermissions()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (actionMode != null) {
                    actionMode?.finish()
                } else if (showFavoritesOnly) {
                    showFavoritesOnly = false
                    loadFiles(currentPath)
                } else {
                    val parent = File(currentPath).parentFile
                    if (parent != null && parent.path.isNotEmpty() && parent.path != "/storage/emulated") {
                        currentPath = parent.path
                        loadFiles(currentPath)
                    } else {
                        finish()
                    }
                }
            }
        })
    }

    private fun updateBreadcrumbs() {
        breadcrumbLayout.removeAllViews()
        val parts = currentPath.split("/").filter { it.isNotEmpty() }
        var path = ""
        parts.forEachIndexed { index, part ->
            path += "/$part"
            val breadcrumb = (layoutInflater.inflate(R.layout.breadcrumb_item, breadcrumbLayout, false) as TextView)
            breadcrumb.text = if (part == "0") "Internal Storage" else part
            val finalPath = path
            breadcrumb.setOnClickListener {
                if (finalPath != currentPath) {
                    currentPath = finalPath
                    loadFiles(currentPath)
                }
            }
            breadcrumbLayout.addView(breadcrumb)

            if (index < parts.size - 1) {
                val separator = (layoutInflater.inflate(R.layout.breadcrumb_separator, breadcrumbLayout, false) as TextView)
                breadcrumbLayout.addView(separator)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                loadFiles(currentPath)
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                storagePermissionLauncher.launch(intent)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                loadFiles(currentPath)
            } else {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun onFileClicked(file: File, isLongClick: Boolean) {
        if (isLongClick || fileAdapter.isSelectionMode) {
            if (showFavoritesOnly) return // Disable selection mode in favorites view
            if (actionMode == null) {
                actionMode = startSupportActionMode(actionModeCallback)
            }
            fileAdapter.isSelectionMode = true
            fileAdapter.toggleSelection(file)
            val selectedCount = fileAdapter.getSelectedItems().size
            if (selectedCount == 0) {
                actionMode?.finish()
            } else {
                actionMode?.title = "$selectedCount selected"
                actionMode?.invalidate()
            }
        } else {
            if (file.isDirectory) {
                currentPath = file.path
                loadFiles(currentPath)
            } else {
                openFile(file)
            }
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.main_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.findItem(R.id.action_search).isVisible = false
            menu.findItem(R.id.action_history).isVisible = false
            menu.findItem(R.id.action_show_favorites).isVisible = false
            menu.findItem(R.id.action_paste).isVisible = false
            menu.findItem(R.id.action_create_folder).isVisible = false
            menu.findItem(R.id.action_change_theme).isVisible = false
            menu.findItem(R.id.action_rename).isVisible = fileAdapter.getSelectedItems().size == 1
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_delete -> deleteSelectedFiles()
                R.id.action_rename -> showRenameDialog()
                R.id.action_copy -> {
                    clipboard = fileAdapter.getSelectedItems().toList()
                    isMoveOperation = false
                    Toast.makeText(this@MainActivity, "Copied ${clipboard?.size} items", Toast.LENGTH_SHORT).show()
                    mode.finish()
                }
                R.id.action_move -> {
                    clipboard = fileAdapter.getSelectedItems().toList()
                    isMoveOperation = true
                    Toast.makeText(this@MainActivity, "Selected ${clipboard?.size} items to move", Toast.LENGTH_SHORT).show()
                    mode.finish()
                }
                else -> return false
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            fileAdapter.clearSelection()
            actionMode = null
            invalidateOptionsMenu()
        }
    }

    private fun showCreateFolderDialog() {
        val editText = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Create Folder")
            .setView(editText)
            .setPositiveButton("Create") { _, _ ->
                val name = editText.text.toString()
                if (name.isNotEmpty()) {
                    val newFolder = File(currentPath, name)
                    if (newFolder.mkdir()) {
                        loadFiles(currentPath)
                        Toast.makeText(this, "Folder created", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to create folder", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRenameDialog() {
        val selectedFile = fileAdapter.getSelectedItems().firstOrNull() ?: return
        val editText = EditText(this)
        editText.setText(selectedFile.name)
        AlertDialog.Builder(this)
            .setTitle("Rename")
            .setView(editText)
            .setPositiveButton("Rename") { _, _ ->
                val newName = editText.text.toString()
                if (newName.isNotEmpty()) {
                    val newFile = File(selectedFile.parent, newName)
                    if (selectedFile.renameTo(newFile)) {
                        loadFiles(currentPath)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
        actionMode?.finish()
    }

    private fun deleteSelectedFiles() {
        val selectedFiles = fileAdapter.getSelectedItems()
        AlertDialog.Builder(this)
            .setTitle("Delete Files")
            .setMessage("Are you sure you want to delete ${selectedFiles.size} files?")
            .setPositiveButton("Delete") { _, _ ->
                var allDeleted = true
                selectedFiles.forEach { file ->
                    if (!file.deleteRecursively()) {
                        allDeleted = false
                    }
                }
                loadFiles(currentPath)
                if (!allDeleted) {
                    Toast.makeText(this, "Could not delete all files", Toast.LENGTH_SHORT).show()
                }
                actionMode?.finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun pasteFiles() {
        try {
            val filesToPaste = clipboard ?: return
            filesToPaste.forEach { fileToPaste ->
                val destFile = File(currentPath, fileToPaste.name)
                if (isMoveOperation) {
                    fileToPaste.copyRecursively(destFile, overwrite = true)
                    fileToPaste.deleteRecursively()
                } else {
                    fileToPaste.copyRecursively(destFile, overwrite = true)
                }
            }
            val message = if (isMoveOperation) "Moved" else "Copied"
            Toast.makeText(this, "$message ${filesToPaste.size} items to ${File(currentPath).name}", Toast.LENGTH_SHORT).show()
            
            clipboard = null
            isMoveOperation = false
            loadFiles(currentPath)
            invalidateOptionsMenu()
        } catch (e: IOException) {
            Toast.makeText(this, "Paste failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openFile(file: File) {
        recentFilesManager.addFile(file.path)
        when (file.extension.lowercase()) {
            "txt", "md", "log", "json", "xml" -> {
                val intent = Intent(this, TextPlayerActivity::class.java)
                intent.putExtra("file_path", file.path)
                startActivity(intent)
            }
            "jpg", "jpeg", "png", "gif", "bmp", "webp" -> {
                val intent = Intent(this, ImagePlayerActivity::class.java)
                intent.putExtra("file_path", file.path)
                startActivity(intent)
            }
            else -> openFileWithIntent(file)
        }
    }

    private fun openFileWithIntent(file: File) {
        val uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", file)
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, mime)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No app found to open this file type", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadFiles(currentPath)
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterFiles(newText)
                return true
            }
        })
        return true
    }

    private fun filterFiles(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            allFiles
        } else {
            val (type, date, name) = parseSearchQuery(query)
            allFiles.filter { file ->
                val nameMatches = name.isEmpty() || file.name.contains(name, ignoreCase = true)
                val typeMatches = type.isEmpty() || file.extension.equals(type, ignoreCase = true)
                val dateMatches = date.isEmpty() || SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(file.lastModified())) == date
                nameMatches && typeMatches && dateMatches
            }
        }
        fileAdapter.updateData(filteredList)
    }

    private fun parseSearchQuery(query: String): Triple<String, String, String> {
        var type = ""
        var date = ""
        var name = ""

        query.split(" ").forEach { part ->
            when {
                part.startsWith("type:") -> type = part.substring(5)
                part.startsWith("date:") -> date = part.substring(5)
                else -> name += "$part "
            }
        }
        return Triple(type.trim(), date.trim(), name.trim())
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isFavoritesView = showFavoritesOnly
        menu.findItem(R.id.action_search).isVisible = actionMode == null && !isFavoritesView
        menu.findItem(R.id.action_history).isVisible = actionMode == null && !isFavoritesView
        menu.findItem(R.id.action_show_favorites).isVisible = actionMode == null
        menu.findItem(R.id.action_paste).isVisible = clipboard != null && !isFavoritesView
        menu.findItem(R.id.action_create_folder).isVisible = clipboard == null && !isFavoritesView

        menu.findItem(R.id.action_rename).isVisible = false
        menu.findItem(R.id.action_copy).isVisible = false
        menu.findItem(R.id.action_move).isVisible = false
        menu.findItem(R.id.action_delete).isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    private fun showRecentFilesDialog() {
        val recentFiles = recentFilesManager.getRecentFiles()
        val fileNames = recentFiles.map { File(it).name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Recent Files")
            .setItems(fileNames) { _, which ->
                val file = File(recentFiles[which])
                openFile(file)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showThemeChooserDialog() {
        val themes = arrayOf(
            "Guinda IPN (System)", "Guinda IPN (Light)", "Guinda IPN (Dark)",
            "Azul ESCOM (System)", "Azul ESCOM (Light)", "Azul ESCOM (Dark)"
        )
        val themeKeys = arrayOf(
            ThemeManager.GUINDA_SYSTEM, ThemeManager.GUINDA_LIGHT, ThemeManager.GUINDA_DARK,
            ThemeManager.AZUL_SYSTEM, ThemeManager.AZUL_LIGHT, ThemeManager.AZUL_DARK
        )
        val currentThemeKey = themeManager.getTheme()
        val currentThemeIndex = themeKeys.indexOf(currentThemeKey)

        AlertDialog.Builder(this)
            .setTitle("Choose Theme")
            .setSingleChoiceItems(themes, currentThemeIndex) { dialog, which ->
                val selectedTheme = themeKeys[which]
                if (selectedTheme != themeManager.getTheme()) {
                    themeManager.setTheme(selectedTheme)
                    dialog.dismiss()
                    recreate()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_create_folder -> showCreateFolderDialog()
            R.id.action_paste -> pasteFiles()
            R.id.action_history -> showRecentFilesDialog()
            R.id.action_show_favorites -> {
                showFavoritesOnly = !showFavoritesOnly
                loadFiles(currentPath)
            }
            R.id.action_change_theme -> showThemeChooserDialog()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun loadFiles(path: String) {
        if (showFavoritesOnly) {
            toolbar.title = "Favorites"
            breadcrumbLayout.visibility = View.GONE
            val favoriteFiles = favoritesManager.getFavorites().map { File(it) }
            allFiles = favoriteFiles.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
            if (::fileAdapter.isInitialized) {
                fileAdapter.updateData(allFiles)
            } else {
                fileAdapter = FileAdapter(allFiles, favoritesManager) { file, isLongClick -> onFileClicked(file, isLongClick) }
                recyclerView.adapter = fileAdapter
            }
            return
        }

        breadcrumbLayout.visibility = View.VISIBLE
        val file = File(path)
        toolbar.title = file.name
        val files = file.listFiles()
        if (files != null) {
            allFiles = files.toList().sortedWith(compareBy({ !it.isDirectory }, { it.name }))
            if (::fileAdapter.isInitialized) {
                fileAdapter.updateData(allFiles)
            } else {
                fileAdapter = FileAdapter(allFiles, favoritesManager) { file, isLongClick -> onFileClicked(file, isLongClick) }
                recyclerView.adapter = fileAdapter
            }
            updateBreadcrumbs()
        } else {
            Toast.makeText(this, "Could not read files", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1
    }
}
