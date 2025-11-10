package com.example.gestordearchivos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class FileAdapter(
    private var files: List<File>,
    private val favoritesManager: FavoritesManager,
    private val onFileClickListener: (File, Boolean) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    private val selectedItems = mutableListOf<File>()
    var isSelectionMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.bind(file, favoritesManager.isFavorite(file.path)) { isFavorite ->
            if (isFavorite) {
                favoritesManager.removeFavorite(file.path)
            } else {
                favoritesManager.addFavorite(file.path)
            }
            notifyItemChanged(position)
        }

        holder.itemView.setBackgroundColor(if (selectedItems.contains(file)) Color.LTGRAY else Color.TRANSPARENT)

        holder.itemView.setOnClickListener {
            onFileClickListener(file, false)
        }

        holder.itemView.setOnLongClickListener {
            onFileClickListener(file, true)
            true
        }
    }

    override fun getItemCount(): Int {
        return files.size
    }

    fun toggleSelection(file: File) {
        if (selectedItems.contains(file)) {
            selectedItems.remove(file)
        } else {
            selectedItems.add(file)
        }
        if (selectedItems.isEmpty()) {
            isSelectionMode = false
        }
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<File> = selectedItems

    fun clearSelection() {
        selectedItems.clear()
        isSelectionMode = false
        notifyDataSetChanged()
    }

    fun updateData(newFiles: List<File>) {
        files = newFiles
        notifyDataSetChanged()
    }

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileNameTextView: TextView = itemView.findViewById(R.id.fileNameTextView)
        private val fileDetailsTextView: TextView = itemView.findViewById(R.id.fileDetailsTextView)
        private val favoriteIcon: ImageView = itemView.findViewById(R.id.favoriteIcon)
        private val fileIcon: ImageView = itemView.findViewById(R.id.fileIcon)

        fun bind(file: File, isFavorite: Boolean, onFavoriteClick: (Boolean) -> Unit) {
            fileNameTextView.text = file.name
            val date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(file.lastModified())
            val size = if (file.isDirectory) {
                "Folder"
            } else {
                val fileSize = file.length()
                when {
                    fileSize < 1024 -> "$fileSize B"
                    fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
                    else -> "${fileSize / (1024 * 1024)} MB"
                }
            }
            fileDetailsTextView.text = "$date | $size"

            fileIcon.setImageResource(getFileIcon(file))
            favoriteIcon.setImageResource(if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
            favoriteIcon.setOnClickListener { onFavoriteClick(isFavorite) }
        }

        private fun getFileIcon(file: File): Int {
            if (file.isDirectory) {
                return R.drawable.ic_folder
            }
            return when (file.extension.lowercase()) {
                "jpg", "jpeg", "png", "gif", "bmp", "webp" -> R.drawable.ic_image
                "txt", "md", "log", "json", "xml" -> R.drawable.ic_file
                else -> R.drawable.ic_file
            }
        }
    }
}
