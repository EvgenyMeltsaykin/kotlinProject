package com.diplom.kotlindiplom.generalFragments.awardFragments

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.core.app.FrameMetricsAggregator.ANIMATION_DURATION
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator
import com.diplom.kotlindiplom.ActivityCallback
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.Award
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.recyclerViewItems.AwardItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_list_awards.*
import org.decimal4j.scale.Scale0f.SCALE_FACTOR

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ListAwardsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListAwardsFragment : Fragment() {
    private lateinit var role : String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activityCallback = context as ActivityCallback
        role = activityCallback.getRoleUser().toString()
        if (role == "child") role = "children"
        else role = "parents"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_awards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        activity?.title = "Вознаграждение"
        emptyListAwardTextView?.isVisible = false
        if (role == "children"){
            addAwardButton?.isVisible = false
        }
        addAwardButton?.setOnClickListener {
            Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_listAwardsFragment_to_newAwardFragment)
        }
        //item.isVisible = true
        updateAwardList()

    }
    fun updateAwardList(){
        val adapter = GroupAdapter<ViewHolder>()
        val firebase = FunctionsFirebase()
        firebase.getFieldUserDatabase(firebase.uidUser!!,"parentUid",object :FirebaseCallback<String>{
            override fun onComplete(value: String) {
                adapter.clear()
                if (value.isEmpty()){
                    listAwardProgressBar?.isVisible = false
                    if (role == "children"){
                        emptyListAwardTextView?.text = "Не привязан родительский аккаунт"
                        emptyListAwardTextView?.isVisible = true
                    }
                }
                firebase.getAwardOutFirebaseWithParentUid(role,value,object :FirebaseCallback<List<Award>>{
                    override fun onComplete(value: List<Award>) {
                        listAwardProgressBar?.isVisible = false
                        if (value.isEmpty()){
                            if (role == "children"){
                                emptyListAwardTextView?.text = "Родители ещё не добавили вознаграждения"
                            }
                            if (role == "parents"){
                                emptyListAwardTextView?.text = "Чтобы у ребенка была дополнительная мотивация делать задания, добавляйте ему вознаграждение. Это очень просто! Просто нажмите на кнопку добавить"
                            }
                            emptyListAwardTextView?.isVisible = true
                            return
                        }
                        value.forEach {
                            val status = it.status
                            val nameAward = it.name
                            val costAward = it.cost
                            val awardId = it.awardId
                            if (status != null && nameAward != null && costAward != null && awardId != null){
                                adapter.add(AwardItem(nameAward,costAward,awardId,status.toString()))
                            }
                            listAwardRecyclerView?.adapter = adapter
                        }
                    }
                })
            }
        })
        adapter.setOnItemClickListener { item , view ->
            val bundle = bundleOf()
            val f = item as AwardItem
            bundle.putString("awardId",f.awardId)
            bundle.putString("nameAward",f.name)
            bundle.putString("costAward",f.cost)
            bundle.putString("status",f.status)
            Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_listAwardsFragment_to_detailAwardFragment,bundle)
        }
    }

}