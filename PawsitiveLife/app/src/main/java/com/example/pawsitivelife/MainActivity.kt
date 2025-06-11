package com.example.pawsitivelife

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pawsitivelife.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.libraries.places.api.Places
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialize Google Places SDK
        if (!Places.isInitialized()) {
            val apiKey = getString(R.string.google_maps_key)
            Places.initialize(applicationContext, apiKey, Locale.getDefault())

        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup custom toolbar
        setSupportActionBar(binding.toolbar)

        // Add padding between the navigation (back) button and the screen edge
            //   binding.toolbar.setContentInsetStartWithNavigation(32)


        // Setup bottom navigation
        val navView: BottomNavigationView = binding.navView

        // Get the NavHostFragment and NavController correctly
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment_activity_main
        ) as NavHostFragment
        navController = navHostFragment.navController

        // Define top-level destinations (no back button shown here)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_settings,
                R.id.navigation_appointments,
                R.id.dogParkFragment
            )
        )

        // Setup action bar for back navigation logic only (we hide the view later)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Connect bottom nav with navigation controller
        navView.setupWithNavController(navController)

        binding.toolbar.navigationIcon?.setTint(getColor(R.color.black))

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
