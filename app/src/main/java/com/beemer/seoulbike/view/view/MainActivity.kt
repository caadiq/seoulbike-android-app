package com.beemer.seoulbike.view.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import com.beemer.seoulbike.R
import com.beemer.seoulbike.databinding.ActivityMainBinding
import com.beemer.seoulbike.viewmodel.MainFragmentType
import com.beemer.seoulbike.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val mainViewModel: MainViewModel by viewModels()

    private var backPressedTime: Long = 0
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.navigationView.isShown) {
                binding.drawerLayout.closeDrawer(binding.navigationView)
            } else {
                if (System.currentTimeMillis() - backPressedTime >= 2000) {
                    backPressedTime = System.currentTimeMillis()
                    Toast.makeText(this@MainActivity, R.string.str_main_press_back, Toast.LENGTH_SHORT).show()
                } else {
                    finish()
                }
            }
        }
    }

    private val PERMISSION_REQUEST_CODE = 1001
    private val locationPermissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutChild) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.containerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<MarginLayoutParams> { bottomMargin = insets.bottom }
            WindowInsetsCompat.CONSUMED
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.navigationView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<MarginLayoutParams> { bottomMargin = insets.bottom }
            WindowInsetsCompat.CONSUMED
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        checkPermissions()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupFragment()
//                setupViewModel()
//                setupTabLayout()
                setupView()
            } else {
                DefaultDialog(
                    title = null,
                    message = "앱을 사용하기 위해서는 위치 권한이 필요합니다. 설정으로 이동해서 권한을 허용해주세요.",
                    cancelText = "종료",
                    confirmText = "설정",
                    onConfirm = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:$packageName"))
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                            startActivity(intent)
                        } finally {
                            finish()
                        }
                    },
                    onCancel = {
                        finish()
                    }
                ).show(supportFragmentManager, "DefaultDialog")
            }
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, locationPermissions[0]) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, locationPermissions[1]) == PackageManager.PERMISSION_GRANTED) {
            setupFragment()
//            setupViewModel()
//            setupTabLayout()
            setupView()
        } else {
            ActivityCompat.requestPermissions(this, locationPermissions, PERMISSION_REQUEST_CODE)
        }
    }

    private fun setupFragment() {
        supportFragmentManager.beginTransaction().apply {
            add(binding.containerView.id, MapFragment(), MainFragmentType.MAP.tag)
            add(binding.containerView.id, StationFragment(), MainFragmentType.STATION.tag)
            commit()
        }

        supportFragmentManager.executePendingTransactions()
        supportFragmentManager.beginTransaction().apply {
            hide(supportFragmentManager.findFragmentByTag(MainFragmentType.STATION.tag)!!)
            commit()
        }
    }

//    private fun setupTabLayout() {
//        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                tab?.let {
//                    mainViewModel.setCurrentFragment(it.position)
//                }
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {}
//
//            override fun onTabReselected(tab: TabLayout.Tab?) {}
//        })
//    }

//    private fun setupViewModel() {
//        mainViewModel.currentFragmentType.observe(this) { fragmentType ->
//            val currentFragment = supportFragmentManager.findFragmentByTag(fragmentType.tag)
//            supportFragmentManager.beginTransaction().apply {
//                supportFragmentManager.fragments.forEach { fragment ->
//                    if (fragment == currentFragment)
//                        show(fragment)
//                    else
//                        hide(fragment)
//                }
//            }.commit()
//
//            binding.tabLayout.getTabAt(fragmentType.ordinal)?.select()
//        }
//    }

    private fun setupView() {
        binding.drawerLayout.apply {
            setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

                override fun onDrawerOpened(drawerView: View) {
                    setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }

                override fun onDrawerClosed(drawerView: View) {
                    setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }

                override fun onDrawerStateChanged(newState: Int) {}
            })
        }

        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.navigationView)
        }
    }
}