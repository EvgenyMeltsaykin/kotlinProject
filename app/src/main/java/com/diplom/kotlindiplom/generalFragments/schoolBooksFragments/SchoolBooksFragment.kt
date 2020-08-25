package com.diplom.kotlindiplom.generalFragments.schoolBooksFragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.SchoolBook
import com.diplom.kotlindiplom.models.SchoolBookItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_school_books.*
import kotlinx.android.synthetic.main.school_book_item.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SchoolBooksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SchoolBooksFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var subjectName: String? = null
    private var numberClass: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            subjectName = it.getString("subjectName")
            numberClass = it.getString("numberClass")
        }
        activity?.title = "Выберите учебник"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_school_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = GroupAdapter<ViewHolder>()
        val firebase = FunctionsFirebase()
        emptySchoolBooksTextView?.isVisible = false
        schoolBooksProgressBar?.isVisible = true
        firebase.getSchoolBooks(
            numberClass,
            subjectName,
            object : FirebaseCallback<List<SchoolBook>> {
                override fun onComplete(value: List<SchoolBook>) {
                    if (value.isEmpty())emptySchoolBooksTextView?.isVisible = true
                    value.forEach {
                        adapter.add(
                            SchoolBookItem(it, requireContext(),
                                object : SchoolBookItem.OnClickDownloadButton {
                                override fun onClickDownloadButton(
                                    viewHolder: ViewHolder,
                                    book: SchoolBook
                                ) {
                                    val animAlpha = AnimationUtils.loadAnimation(context, R.anim.alpha)
                                    viewHolder.itemView.downloadButton.startAnimation(animAlpha)
                                    firebase.downloadSchoolBook(book,requireContext(),requireActivity())
                                }
                            })
                        )
                    }
                    listSchoolBooksRecyclerView?.adapter = adapter
                    schoolBooksProgressBar?.isVisible = false
                }
            })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment schoolBooksFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SchoolBooksFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}