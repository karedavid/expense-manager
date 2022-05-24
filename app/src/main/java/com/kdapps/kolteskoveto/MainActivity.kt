package com.kdapps.kolteskoveto

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kdapps.kolteskoveto.databinding.ActivityMainBinding
import com.kdapps.kolteskoveto.ui.archive.ArchiveDialogFragment


class MainActivity : AppCompatActivity(), ArchiveDialogFragment.ArchiveAllDialogListener {

    private lateinit var mainActivityViewModel  : MainActivityViewModel
    private lateinit var binding                : ActivityMainBinding
    private lateinit var navController          : NavController
    private lateinit var appBarConfiguration    : AppBarConfiguration

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        //supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.md_theme_dark_surface)))

        // ViewModel
        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        // Layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bottom Navigation View
        val navView: BottomNavigationView = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_main)

        navView.setupWithNavController(navController)

        binding.addFab.setOnClickListener { startActivity(Intent(this, NewActivity::class.java)) }

        binding.quickArchiveFab.setOnClickListener {
            ArchiveDialogFragment().show(
                supportFragmentManager,
                ArchiveDialogFragment.TAG
            )
        }

        binding.addFab.setOnLongClickListener{
            binding.addFab.extend()
            Handler(Looper.getMainLooper()).postDelayed({
                binding.addFab.shrink()
            }, 3000)
            return@setOnLongClickListener true
        }

        binding.quickArchiveFab.setOnLongClickListener{
            binding.quickArchiveFab.extend()
            Handler(Looper.getMainLooper()).postDelayed({
                binding.quickArchiveFab.shrink()
            }, 3000)
            return@setOnLongClickListener true
        }

        Handler(Looper.getMainLooper()).postDelayed({
            binding.quickArchiveFab.shrink()
        }, 3000)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.addFab.shrink()
        }, 3100)
    }

    override fun onArchiveCreated(name: String) {
        mainActivityViewModel.archiveAllSpendNode(name)
    }

}