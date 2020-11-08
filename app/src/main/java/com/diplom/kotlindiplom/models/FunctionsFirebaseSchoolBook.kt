package com.diplom.kotlindiplom.models

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.diplom.kotlindiplom.Callback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class FunctionsFirebaseSchoolBook:FunctionsFirebase() {
    fun downloadSchoolBook(book: SchoolBook, context: Context, activity: Activity) {
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(book.url)
        ref.downloadUrl.addOnSuccessListener { uri ->
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(uri)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(context, "Загрузка началась", Toast.LENGTH_SHORT).show()
                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        book.name + ".pdf"
                    )
                    downloadManager.enqueue(request)
                } else {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1
                    )
                }
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Ошибка при загрузке", Toast.LENGTH_SHORT).show()
        }
    }

    fun getDetailsBook(bookSnap: DataSnapshot): SchoolBook {
        val schoolBook = SchoolBook()
        schoolBook.name = bookSnap.child("name").value.toString()
        schoolBook.cover = bookSnap.child("cover").value.toString()
        schoolBook.url = bookSnap.child("url").value.toString()
        return schoolBook
    }

    fun getSchoolBooks(
        numberClass: String?,
        subjectName: String?,
        firebaseCallBack: Callback<List<SchoolBook>>
    ) {
        val ref = rootRef.child("schoolBooks").child("${numberClass}class").child(subjectName!!)
        ref.keepSynced(true)
        val books = mutableListOf<SchoolBook>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach { book ->
                        books.add(getDetailsBook(book))
                    }
                }
                firebaseCallBack.onComplete(books)
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}