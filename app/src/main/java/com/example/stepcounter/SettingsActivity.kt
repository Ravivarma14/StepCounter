package com.example.stepcounter

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stepcounter.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsActivityBinding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        settingsActivityBinding=ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(settingsActivityBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        setListeners()
        initFromSharedPreferces()
    }
    private fun setListeners(){
        settingsActivityBinding.applyBtn.setOnClickListener({
            var sharedPreferences:SharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)

            var editor:Editor= sharedPreferences.edit()
            editor.putInt("ProgressStyle",settingsActivityBinding.progressStyleSpinner.selectedItemPosition)
            editor.putInt("TargetSteps",Integer.parseInt(settingsActivityBinding.targetStepsCount.text.toString()))
            //editor.putBoolean("DarkMode",settingsActivityBinding.darkModeToggle.isChecked)
            editor.apply()

            //AppCompatDelegate.setDefaultNightMode(if(settingsActivityBinding.darkModeToggle.isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_YES)

            onBackPressed()
        })
    }
    private fun initFromSharedPreferces(){
        var sharedPreferences:SharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        var progressStyle=sharedPreferences.getInt("ProgressStyle",0)
        var stepCounterTarget=sharedPreferences.getInt("TargetSteps",100)
        //var isDarkMode=sharedPreferences.getBoolean("DarkMode",true)

        settingsActivityBinding.progressStyleSpinner.setSelection(progressStyle)
        settingsActivityBinding.targetStepsCount.setText(stepCounterTarget.toString())
        //settingsActivityBinding.darkModeToggle.isChecked=isDarkMode


    }
}