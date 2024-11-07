package com.example.stepcounter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stepcounter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var stepCounterSensor:Sensor
    private lateinit var accelemeterSensor:Sensor
    private var stepCount=0
    private var initialCount=-1
    private var stepCounterTarget=2000
    private var stepLengthInMeters=0.762f

    private var lastStepTime=0L

    private var isStart=false

    private lateinit var activityMainBinding:ActivityMainBinding
    private val TAG="MainActivity"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activityMainBinding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        //startTIme= System.currentTimeMillis()
        sensorManager= getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor= sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)!!
        accelemeterSensor= sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

        if(stepCounterSensor == null){
            Toast.makeText(this,"Sensor not available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        initalize()
        setListeners()
    }

    private fun initalize(){

        var sharedPreferences:SharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        var progressStyle=sharedPreferences.getInt("ProgressStyle",0)
        stepCounterTarget=sharedPreferences.getInt("TargetSteps",500)
        //var isDarkMode=sharedPreferences.getBoolean("DarkMode",true)

        activityMainBinding.goalSteps.setTextColor(resources.getColor(R.color.white))
        activityMainBinding.steps.setTextColor(resources.getColor(R.color.white))
        activityMainBinding.percentage.setTextColor(resources.getColor(R.color.blue))

        activityMainBinding.donutProgress.visibility= if(progressStyle==0) View.VISIBLE else View.GONE
        activityMainBinding.circleProgress.visibility= if(progressStyle==1){
            activityMainBinding.goalSteps.setTextColor(resources.getColor(R.color.black))
            activityMainBinding.steps.setTextColor(resources.getColor(R.color.black))
            activityMainBinding.percentage.setTextColor(resources.getColor(R.color.black))
            View.VISIBLE
        }
        else View.GONE

        activityMainBinding.arcProgress.visibility= if(progressStyle==2) View.VISIBLE else View.GONE

        activityMainBinding.goalSteps.text= stepCounterTarget.toString()

        activityMainBinding.donutProgress.max=stepCounterTarget
        activityMainBinding.arcProgress.max=stepCounterTarget
        activityMainBinding.circleProgress.max=stepCounterTarget

        updateUI()
    }

    private fun registerSensorListener(){
        if(stepCounterSensor!=null){
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
        }
        if(accelemeterSensor!=null){
            sensorManager.registerListener(this,accelemeterSensor,SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun unregisterSensorListener(){
        if(stepCounterSensor!=null){
            sensorManager.unregisterListener(this)
        }
        if(accelemeterSensor!=null){
            sensorManager.unregisterListener(this)
        }
    }

    private fun setListeners(){
        activityMainBinding.startStopBtn.setOnClickListener(
            {
                if(!isStart) {
                    activityMainBinding.startStopBtn.text="Pause"
                    isStart = true
                    registerSensorListener()
                }
                else{
                    activityMainBinding.startStopBtn.text= if(stepCount!=0) "Resume" else "Start"
                    isStart=false
                    unregisterSensorListener()
                    initialCount=stepCount
                }

                //activityMainBinding.steps.text= "Steps: 0"
            }
        )

        activityMainBinding.settingsIv.setOnClickListener({
            var intent= Intent(this,SettingsActivity::class.java)
            startActivity(intent)
        })
    }

    override fun onStop() {
        super.onStop()
        unregisterSensorListener()
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if(event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR){
            updateSteps()
        }
        else if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            var x= event.values[0]
            var y= event.values[1]
            var z= event.values[2]

            var acceleration= Math.sqrt((x*x + y*y + z*z).toDouble())
            if(acceleration>15)
                updateSteps()

        }

        var distanceInKM= stepCount * stepLengthInMeters /1000;

        Log.d("STEP","count: "+ stepCount + " target: "+ stepCounterTarget)

        if(stepCounterTarget<= stepCount){
            Toast.makeText(this, "Goal reached count: "+ stepCount, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun updateSteps(){
        if(System.currentTimeMillis()-lastStepTime> 300){
            stepCount++
            lastStepTime=System.currentTimeMillis()
            activityMainBinding.steps.text="Steps:" + stepCount.toString()

            updateUI()
        }
    }
    private fun updateUI(){
        var percent= (stepCount*100 / stepCounterTarget)
        activityMainBinding.percentage.text= percent.toString()+"%"
        Log.d(TAG, "updateSteps: stepCount: "+ stepCount + " target: "+ stepCounterTarget+ "percent: "+ percent)
        activityMainBinding.arcProgress.progress=percent
        activityMainBinding.arcProgress.animate()
        activityMainBinding.donutProgress.progress=percent
        activityMainBinding.donutProgress.animate()
        activityMainBinding.circleProgress.progress=percent
        activityMainBinding.circleProgress.animate()
    }
}