package com.diplom.kotlindiplom.generalFragments.markFragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.android.synthetic.main.fragment_graph_marks.*
import kotlin.collections.ArrayList
import kotlin.math.pow


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
        activity?.title = lessonName
        val firebase = FunctionsFirebase()
        firebase.getDetailsMarks(
            lessonName,
            semestrNumber,
            object : FirebaseCallback<Map<String, String>> {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onComplete(value: Map<String, String>) {
                    val dataValues = mutableListOf<Entry>()
                    val marks = mutableListOf<Int>()
                    var i = 1f
                    value.forEach { (s, s2) ->
                        if (s2.isDigitsOnly() && s2.isNotEmpty()) {
                            dataValues.add(Entry(i, s2.toFloat()))
                            marks.add(s2.toInt())
                            i++;
                        }

                    }
                    settingGraph(dataValues,marks)
                    settingPie(marks)

                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun settingGraph(dataValues: MutableList<Entry>,marks:List<Int>) {
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

        graphMark?.xAxis?.setLabelCount(dataValues.size / 2, true)
        val lineDataSet = LineDataSet(dataValues, "Оценки")
        lineDataSet.lineWidth = 4f
        lineDataSet.color = Color.BLACK
        lineDataSet.setDrawCircleHole(true)
        lineDataSet.circleRadius = 3f
        lineDataSet.valueTextSize = 10f
        lineDataSet.circleHoleColor = resources.getColor(R.color.colorPrimaryDark)
        lineDataSet.valueFormatter = MyValueFormatter()
        //lineDataSet.enableDashedLine()
        val lineTrend = LineDataSet(getDataForTrend(marks),"Тренд")
        lineTrend.enableDashedLine(3f,2f,5f)
        lineTrend.lineWidth = 3f
        lineTrend.setDrawCircles(false)

        val dataSets = mutableListOf<ILineDataSet>()
        dataSets.add(lineDataSet)
        //dataSets.add(lineTrend)

        val data = LineData(dataSets)

        graphMark?.data = data
        graphMark?.invalidate()
    }
    private fun getDataForTrend(marks:List<Int>):MutableList<Entry>{
        val dataValues = mutableListOf<Entry>()
        val polynom = getDataValuesPoly(marks)
        var i = 1f
        while(i<=marks.size+1){
            val functionValue = polynom[0]+polynom[1]*i+polynom[2]*i*i
            dataValues.add(Entry(i,functionValue))
            i+=0.02f
        }
        return dataValues
    }
    private fun calculateSum(list:List<Int>):Int{
        var summa = 0
        list.forEach {
            summa+=it
        }
        return summa
    }
    private fun getDeterminant(matrix: Array<Array<Float>>):Float{
        val mainDiag = matrix[0][0]*matrix[1][1]*matrix[2][2]
        val sideDiag = matrix[0][2]*matrix[1][1]*matrix[2][0]
        val oneTriangle = matrix[0][1]*matrix[1][2]*matrix[2][0] + matrix[1][0]*matrix[2][1]*matrix[0][2]
        val twoTriangle = matrix[2][1]*matrix[1][2]*matrix[0][0] + matrix[1][0]*matrix[0][1]*matrix[2][2]
        return mainDiag + oneTriangle - sideDiag - twoTriangle
    }
    private fun setMatrix(x1:Float,x2:Float,x3:Float,x4:Float,x5:Float,x6:Float,x7:Float,x8:Float,x9:Float) :Array<Array<Float>>{
        val mainMatrix: Array<Array<Float>> = Array(3) { Array(3) { 0f } }
        mainMatrix[0][0] = x1
        mainMatrix[0][1] = x2
        mainMatrix[0][2] = x3
        mainMatrix[1][0] = x4
        mainMatrix[1][1] = x5
        mainMatrix[1][2] = x6
        mainMatrix[2][0] = x7
        mainMatrix[2][1] = x8
        mainMatrix[2][2] = x9
        return  mainMatrix
    }
    private fun getDataValuesPoly(marks:List<Int>):List<Float>{
        var sumX1 = 0
        var sumX2 = 0
        var sumX3 = 0
        var sumX4 = 0
        var sumY1 = 0
        var sumXY = 0
        var sumX2Y = 0
        val xDegrees1 = mutableListOf<Int>()
        val xDegrees2 = mutableListOf<Int>()
        val xDegrees3 = mutableListOf<Int>()
        val xDegrees4 = mutableListOf<Int>()
        val xy = mutableListOf<Int>()
        val x2y = mutableListOf<Int>()
        for(i in 1..marks.size){
            xDegrees1.add(i)
            xDegrees2.add(i*i)
            xDegrees3.add(i.toDouble().pow(3.0).toInt())
            xDegrees4.add(i.toDouble().pow(4.0).toInt())
        }
        var i = 1
        marks.forEach {
           xy.add(it*i)
           x2y.add(it*i*i)
           i++
        }
        Log.d("Tag",xy.toString())
        Log.d("Tag",x2y.toString())
        sumX1 = calculateSum(xDegrees1)
        sumX2 = calculateSum(xDegrees2)
        sumX3 = calculateSum(xDegrees3)
        sumX4 = calculateSum(xDegrees4)
        sumY1 = calculateSum(marks)
        sumXY = calculateSum(xy)
        sumX2Y = calculateSum(x2y)

        var mainMatrix: Array<Array<Float>> = Array(3) { Array(3) { 0f } }
        mainMatrix = setMatrix(marks.size.toFloat(),sumX1.toFloat(),sumX2.toFloat(),sumX1.toFloat(),sumX2.toFloat(),sumX3.toFloat(),sumX2.toFloat(),sumX3.toFloat(),sumX4.toFloat())
        val b0Matrix = setMatrix(sumY1.toFloat(),sumX1.toFloat(),sumX2.toFloat(),sumXY.toFloat(),sumX2.toFloat(),sumX3.toFloat(),sumX2Y.toFloat(),sumX3.toFloat(),sumX4.toFloat())
        val b1Matrix = setMatrix(marks.size.toFloat(),sumY1.toFloat(),sumX2.toFloat(),sumX1.toFloat(),sumXY.toFloat(),sumX3.toFloat(),sumX2.toFloat(),sumX2Y.toFloat(),sumX4.toFloat())
        val b2Matrix= setMatrix(marks.size.toFloat(),sumX1.toFloat(),sumY1.toFloat(),sumX1.toFloat(),sumX2.toFloat(),sumXY.toFloat(),sumX2.toFloat(),sumX3.toFloat(),sumX2Y.toFloat())

        val mainDeterminant = getDeterminant(mainMatrix)
        val b0Determinant = getDeterminant(b0Matrix)
        val b1Determinant = getDeterminant(b1Matrix)
        val b2Determinant = getDeterminant(b2Matrix)
        Log.d("Tag",mainDeterminant.toString())
        //Log.d("Tag",(b1Determinant/mainDeterminant).toString())
        //Log.d("Tag",(b2Determinant/mainDeterminant).toString())
        Log.d("Tag",listOf(b0Determinant/mainDeterminant,b1Determinant/mainDeterminant,b2Determinant/mainDeterminant).toString())
        return listOf(b0Determinant/mainDeterminant,b1Determinant/mainDeterminant,b2Determinant/mainDeterminant)
    }
    private fun settingPie(marks: List<Int>) {
        pieMark?.setNoDataText("Нет оценок")
        pieMark?.setNoDataTextColor(resources.getColor(R.color.colorText))
        val colors = listOf<Int>(resources.getColor(R.color.colorPie5),resources.getColor(R.color.colorPie4), resources.getColor(R.color.colorPie3), resources.getColor(R.color.colorPie2))
        val pieDataSet = PieDataSet(dataValuesToPie(marks), "")
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

    private fun dataValuesToPie(marks: List<Int>): ArrayList<PieEntry> {
        val dataValues = arrayListOf<PieEntry>()
        if (getPercentMark(5, marks) != 0f) {
            dataValues.add(PieEntry(getPercentMark(5, marks), "5"))
        }
        if (getPercentMark(4, marks) != 0f) {
            dataValues.add(PieEntry(getPercentMark(4, marks), "4"))
        }
        if (getPercentMark(3, marks) != 0f) {
            dataValues.add(PieEntry(getPercentMark(3, marks), "3"))
        }
        if (getPercentMark(2, marks) != 0f) {
            dataValues.add(PieEntry(getPercentMark(2, marks), "2"))
        }
        return dataValues
    }

    private fun getPercentMark(mark: Int, marks: List<Int>): Float {
        var countMark = 0f
        marks.forEach {
            if (it == mark) {
                countMark++
            }
        }
        return countMark / marks.size * 100
    }

    private class MyValueFormatter : ValueFormatter() {
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