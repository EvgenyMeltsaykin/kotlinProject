package com.diplom.kotlindiplom

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.github.signaflo.timeseries.TimePeriod
import com.github.signaflo.timeseries.TimeSeries
import com.github.signaflo.timeseries.model.arima.Arima
import com.github.signaflo.timeseries.model.arima.ArimaOrder
import com.google.firebase.auth.FirebaseAuth
import java.util.*


class SplashActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        /*
        val values = listOf(5.0, 4.0, 4.0, 5.0, 4.0, 4.0,4.0,5.0,5.0,4.0)
        val day = TimePeriod.oneDay()
        val data = DoubleArray(values.size + 100)
        var i = 0
        values.forEach {
            data[i] = it
            i++
        }
        val series = TimeSeries.from(day, *data)

        val models = mutableListOf<ArimaOrder>()

        for (p in 3..6) for (d in 0..1)for (q in 2..6)  {
            models.add(ArimaOrder.order(p, d, q))
            models.add(ArimaOrder.order(p, d, q,Arima.Drift.INCLUDE))
            models.add(ArimaOrder.order(p, d, q,Arima.Constant.INCLUDE))
        }

        val fmodels = ArrayList<Arima>()

        var tmp: Arima
        for (i in 0 until models.size) {
            tmp = try {
                Arima.model(series, models[i],day)
            } catch (e: Exception) {
                continue
            }
            if (!java.lang.Double.isNaN(tmp.aic())) fmodels.add(tmp)
        }
        var optimalModel = fmodels[0]
        var aic = fmodels[0].aic()
        var aic_curr: Double
        fmodels.forEach {
            Log.d("Tag",it.aic().toString())
            if (it.aic() < aic){
                aic = it.aic()
                optimalModel = it
            }
        }
        Log.d("Tag","aic")
        Log.d("Tag",optimalModel.aic().toString())
        Log.d("Tag","forecast")
        Log.d("Tag",optimalModel.forecast(1).toString())





        */
        //Log.d("Tag",model.fittedSeries().asList().toString())
        /*val week = TimePeriod.oneWeek()
        val order = ArimaOrder.order(0, 1, 1, 0, 1, 1)
        val model = Arima.model(series, order, day)
        Log.d("Tag", model.forecast(2).toString())

        Log.d("Tag","errors")
        Log.d("Tag",model.predictionErrors().toString())
        Log.d("Tag","model")
        Log.d("Tag",model.toString())
        val forecast = model.forecast(7)
        Log.d("Tag", forecast.toString())*/

        //println(model.aic()); // Get and display the model AIC
        // println(model.coefficients()); // Get and display the estimated coefficients
        // println(Arrays.toString(model.stdErrors()));

        this.window.statusBarColor = resources.getColor(R.color.colorActionBarSplash)
        verifyUserIsLoggedIn()
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            if (user.isEmailVerified) {
                val firebase = FunctionsFirebase()
                firebase.getRoleByUid(uid!!, object : FirebaseCallback<String> {
                    override fun onComplete(value: String) {
                        if (value == "child") {
                            intent = Intent(applicationContext, MainActivity::class.java)
                            intent.putExtra("role", "child")
                        }
                        if (value == "parent") {
                            intent = Intent(applicationContext, MainActivity::class.java)
                            intent.putExtra("role", "parent")
                        }
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }

                })
            } else {
                Toast.makeText(
                    applicationContext,
                    "Подтвердите электронную почту",
                    Toast.LENGTH_LONG
                ).show()
                FirebaseAuth.getInstance().signOut()
                intent = Intent(this, ChooseActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        } else {
            intent = Intent(this, ChooseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
