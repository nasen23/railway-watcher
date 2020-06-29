package com.nasen.railwaywatcher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.nasen.railwaywatcher.ui.RailwayAdapter

class MainActivity : AppCompatActivity() {
    lateinit var railwayAdapter: RailwayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Global.init(applicationContext)
        val controller = Navigation.findNavController(this, R.id.fragment)
        NavigationUI.setupActionBarWithNavController(this, controller)
        railwayAdapter = RailwayAdapter()
    }

    override fun onSupportNavigateUp(): Boolean {
        val controller = Navigation.findNavController(this, R.id.fragment)
        return controller.navigateUp()
    }
}