package com.diplom.kotlindiplom.models

import com.diplom.kotlindiplom.Callback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlin.math.roundToInt

class FunctionsFirebaseMark:FunctionsFirebase() {
    fun getMarksLessonSemester(
        lessonName: String,
        firebaseCallBack: Callback<List<Int>>
    ) {
        val ref =
            userRef.child(userUid).child("diary")
                .child("marks").orderByChild("lessonName").equalTo(lessonName)
        ref.keepSynced(true)
        val marksSemester = mutableListOf<Int>()
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach { lesson ->
                        lesson.children.forEach { semestr ->
                            semestr.children.forEach {
                                if (it.key.toString() == "middleMark") {
                                    marksSemester.add(
                                        it.value.toString().toFloat().roundToInt()
                                    )
                                }
                            }
                        }
                    }
                    firebaseCallBack.onComplete(marksSemester)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun getFieldMarks(field: String, firebaseCallBack: Callback<String>) {

        val ref = userRef.child(userUid).child("diary").child("marks")
        ref.keepSynced(true)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach {
                        if (it.key.toString() == field) {
                            firebaseCallBack.onComplete(it.value.toString())
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    fun getMiddleMark(
        lessonName: String,
        semesterNumber: String,
        firebaseCallBack: Callback<String>
    ) {
        val ref = userRef.child(userUid).child("diary").child("marks").orderByChild("lessonName")
            .equalTo(lessonName)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach { lesson ->
                        lesson.children.forEach { semestr ->
                            if (semestr.key.toString() == "semestr$semesterNumber") {
                                semestr.children.forEach { middleMark ->
                                    if (middleMark.key.toString() == "middleMark") {
                                        val mark = middleMark.value.toString()
                                        firebaseCallBack.onComplete(mark)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun getDetailsMarks(
        lessonName: String,
        numberSemester: String,
        firebaseCallBack: Callback<Map<String, String>>
    ) {
        val ref = userRef.child(userUid).child("diary").child("marks").orderByChild("lessonName")
            .equalTo(lessonName)
        val detailMarksMap = mutableMapOf<String, String>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach { lesson ->
                        lesson.children.forEach { info ->
                            lesson.children.forEach { semestr ->
                                if (semestr.key.toString() == "semestr$numberSemester") {
                                    semestr.children.forEach { mark ->
                                        var date = ""
                                        var value = ""
                                        mark.children.forEach { detailMark ->
                                            if (detailMark.key.toString() == "date") {
                                                date =
                                                    detailMark.value.toString()
                                            }
                                            if (detailMark.key.toString() == "value") {
                                                value =
                                                    detailMark.value.toString()
                                            }
                                        }
                                        detailMarksMap[date] = value
                                    }
                                }
                            }
                        }
                    }
                    firebaseCallBack.onComplete(detailMarksMap)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

}