package com.example.pawsitivelife

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import android.R.color.black
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
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //  Initialize Google Places SDK
        if (!Places.isInitialized()) {
            val apiKey = getString(R.string.google_maps_key)
            Places.initialize(applicationContext, apiKey, Locale.getDefault())

        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup custom toolbar
        setSupportActionBar(binding.toolbar)

        //  Get the NavHostFragment and NavController
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment_activity_main
        ) as NavHostFragment
        navController = navHostFragment.navController

        //  Define top-level destinations
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

        val navView: BottomNavigationView = binding.navView

        //  Automatically sync bottom nav with nav controller
        navView.setupWithNavController(navController)

        // 🆕 Manually update the selected icon when navigating via code
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> navView.menu.findItem(R.id.navigation_home).isChecked = true
                R.id.navigation_appointments -> navView.menu.findItem(R.id.navigation_appointments).isChecked = true
                R.id.navigation_settings -> navView.menu.findItem(R.id.navigation_settings).isChecked = true
                R.id.dogParkFragment -> navView.menu.findItem(R.id.dogParkFragment).isChecked = true
                else -> {
                    // 🆕 Disable selection if destination is outside bottom nav
                    navView.menu.setGroupCheckable(0, false, true)
                }
            }
        }

        //  Add custom behavior for item selection to avoid backstack issues
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.popBackStack(R.id.navigation_home, false)
                    navController.navigate(R.id.navigation_home)
                    true
                }
                R.id.navigation_appointments -> {
                    navController.popBackStack(R.id.navigation_appointments, false)
                    navController.navigate(R.id.navigation_appointments)
                    true
                }
                R.id.navigation_settings -> {
                    navController.popBackStack(R.id.navigation_settings, false)
                    navController.navigate(R.id.navigation_settings)
                    true
                }
                R.id.dogParkFragment -> {
                    navController.popBackStack(R.id.dogParkFragment, false)
                    navController.navigate(R.id.dogParkFragment)
                    true
                }
                else -> false
            }
        }

        //  Tint the toolbar back icon
        binding.toolbar.navigationIcon?.setTint(getColor(black))
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
