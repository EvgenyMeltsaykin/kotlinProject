package com.diplom.kotlindiplom.models.recyclerViewItems

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.SchoolBook
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.school_book_item.view.*


class SchoolBookItem(
    val book: SchoolBook,
    val context: Context,
    val downloadListener: OnClickDownloadButton
): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.schoolBookNameTextView.text = book.name
        val cover = viewHolder.itemView.findViewById<ImageView>(R.id.coverSchoolBookImageView)
        Glide.with(context)
            .load(book.cover)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(cover)
        viewHolder.itemView.downloadButton.setOnClickListener {
            downloadListener.onClickDownloadButton(viewHolder, book)
        }
    }
    interface OnClickDownloadButton{
        fun onClickDownloadButton(viewHolder:ViewHolder, book: SchoolBook)
    }

    override fun getLayout(): Int {
        return R.layout.school_book_item
    }
}