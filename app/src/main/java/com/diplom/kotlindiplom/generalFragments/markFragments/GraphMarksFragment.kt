package com.diplom.kotlindiplom.generalFragments.markFragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ViewPortHandler
import com.google.android.gms.common.util.MurmurHash3
import kotlinx.android.synthetic.main.fragment_graph_marks.*
import kotlin.collections.ArrayList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GraphMarksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GraphMarksFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var lessonName: String
    private lateinit var semestrNumber: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            lessonName = it.getString("lessonName", "")
            semestrNumber = it.getString("semestrNumber", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_graph_marks, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title =lessonName
        val firebase = FunctionsFirebase()
        firebase.getDetailsMarks(
            lessonName,
            semestrNumber,
            object : FirebaseCallback<Map<String, String>> {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onComplete(value: Map<String, String>) {
                    val dataValues = mutableListOf<Entry>()
                    val marks = mutableListOf<Int>()
                    var i = 0f
                    value.forEach { (s, s2) ->
                        if (s2.isDigitsOnly() && s2.isNotEmpty()) {
                            dataValues.add(Entry(i, s2.toFloat()))
                            marks.add(s2.toInt())
                            i++;
                        }

                    }
                    settingGraph(dataValues)
                    settingPie(marks)

                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun settingGraph(dataValues: MutableList<Entry>){
        graphMark?.setNoDataText("Нет оценок")
        graphMark?.setNoDataTextColor(resources.getColor(R.color.colorText))
        graphMark?.setDrawGridBackground(true)
        graphMark?.setDrawBorders(true)
        graphMark?.isScaleYEnabled = false
        graphMark?.axisLeft?.setLabelCount(4, true)
        graphMark?.axisRight?.setLabelCount(4, true)
        //graphMark?.xAxis?.setCenterAxisLabels(true)
        /*val description = Description()
        description.text = "График оценок"
        description.textSize = 15f*/
        graphMark?.description?.isEnabled = false
        val legend = graphMark?.legend
        legend?.isEnabled = true
        legend?.textColor = resources.getColor(R.color.colorText)
        legend?.textSize = 15f
        legend?.xEntrySpace = 15f

        graphMark?.xAxis?.setLabelCount(dataValues.size/2, true)
        val lineDataSet = LineDataSet(dataValues, "Оценки")
        lineDataSet.lineWidth = 2f
        lineDataSet.color = Color.BLACK
        lineDataSet.setDrawCircleHole(false)
        lineDataSet.circleRadius = 3f
        lineDataSet.valueTextSize = 10f
        lineDataSet.valueFormatter = MyValueFormatter()
        //lineDataSet.enableDashedLine()
        val dataSets = mutableListOf<ILineDataSet>()
        dataSets.add(lineDataSet)

        val data = LineData(dataSets)

        graphMark?.data = data
        graphMark?.invalidate()
    }
    private fun settingPie(marks: List<Int>){
        pieMark?.setNoDataText("Нет оценок")
        pieMark?.setNoDataTextColor(resources.getColor(R.color.colorText))
        val colors = listOf<Int>(Color.GREEN,Color.CYAN,Color.YELLOW,Color.RED)
        val pieDataSet = PieDataSet(dataValuesToPie(marks),"")
        pieDataSet.colors = colors
        val pieData = PieData(pieDataSet)
        pieData.setValueTextSize(15f)
        pieData.setValueTextColor(resources.getColor(R.color.colorGray))
        pieMark?.setUsePercentValues(true)
        pieMark?.holeRadius = 0f
        pieMark?.transparentCircleRadius = 10f
        pieMark?.description?.isEnabled = false
        pieMark?.legend?.isEnabled = false
        pieMark?.setEntryLabelColor(Color.BLACK)
        //pieMark?.centerText = "Оценки"
        //pieMark?.setCenterTextSize(20f)
        pieMark?.data = pieData
        pieMark?.invalidate()
    }
    private fun dataValuesToPie(marks:List<Int>) : ArrayList<PieEntry>{
        val dataValues = arrayListOf<PieEntry>()
        if (getPercentMark(5,marks) !=0f){
            dataValues.add(PieEntry(getPercentMark(5,marks),"5"))
        }
        if (getPercentMark(4,marks) !=0f){
            dataValues.add(PieEntry(getPercentMark(4,marks),"4"))
        }
        if (getPercentMark(3,marks) !=0f){
            dataValues.add(PieEntry(getPercentMark(3,marks),"3"))
        }
        if (getPercentMark(2,marks) !=0f){
            dataValues.add(PieEntry(getPercentMark(2,marks),"2"))
        }
        return dataValues
    }
    private fun getPercentMark(mark:Int, marks:List<Int>):Float{
        var countMark  = 0f
        marks.forEach {
            if (it == mark){
                countMark++
            }
        }
        return countMark/marks.size*100
    }

private class MyValueFormatter: ValueFormatter(){
    override fun getFormattedValue(value: Float): String {
        return "${value.toInt()}"
    }
}
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GraphMarksFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GraphMarksFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}