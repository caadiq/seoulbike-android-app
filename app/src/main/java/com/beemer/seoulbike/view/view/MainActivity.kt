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
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.beemer.seoulbike.R
import com.beemer.seoulbike.databinding.ActivityMainBinding
import com.beemer.seoulbike.model.data.UserData
import com.beemer.seoulbike.model.dto.NavigationViewMenuDto
import com.beemer.seoulbike.model.dto.TokenDto
import com.beemer.seoulbike.view.adapter.NavigationViewMenuAdapter
import com.beemer.seoulbike.viewmodel.AuthViewModel
import com.beemer.seoulbike.viewmodel.DataStoreViewModel
import com.beemer.seoulbike.viewmodel.MainFragmentType
import com.beemer.seoulbike.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val dataStoreViewModel by viewModels<DataStoreViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()
    private val mainViewModel by viewModels<MainViewModel>()

    private val navigationViewMenuAdapter = NavigationViewMenuAdapter()

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

    private lateinit var splashScreen: SplashScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splashScreen()
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setupInsets()
        checkPermissions()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupFragment()
                setupView()
                setupNavigationView()
                setupViewModel()
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

    private fun splashScreen() {
        splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition { true }

        lifecycleScope.launch {
            val accessToken = dataStoreViewModel.accessToken.first()
            val refreshToken = dataStoreViewModel.refreshToken.first()

            if (!accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
                authViewModel.reissueAllTokens(TokenDto(
                    accessToken = accessToken,
                    refreshToken = refreshToken
                ))
            } else {
                splashScreen.setKeepOnScreenCondition { false }
            }
        }

        authViewModel.apply {
            reissueAllTokens.observe(this@MainActivity) {
                UserData.apply {
                    isLoggedIn = true
                    accessToken = it.accessToken
                    refreshToken = it.refreshToken
                }

                authViewModel.getUser()
            }

            user.observe(this@MainActivity) {
                UserData.apply {
                    email = it.email
                    nickname = it.nickname
                    socialType = it.socialType
                    createdDate = it.createdDate
                }

                splashScreen.setKeepOnScreenCondition { false }
            }

            errorCode.observe(this@MainActivity) { code ->
                if (code == 401)
                    splashScreen.setKeepOnScreenCondition { false }
            }
        }
    }

    private fun setupInsets() {
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
            v.setPadding(insets.left, insets.top, insets.right, 0)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, locationPermissions[0]) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, locationPermissions[1]) == PackageManager.PERMISSION_GRANTED) {
            setupFragment()
            setupView()
            setupNavigationView()
            setupViewModel()
        } else {
            ActivityCompat.requestPermissions(this, locationPermissions, PERMISSION_REQUEST_CODE)
        }
    }

    private fun setupFragment() {
        supportFragmentManager.beginTransaction().apply {
            add(binding.containerView.id, MapFragment(), MainFragmentType.MAP.tag)
            add(binding.containerView.id, StationFragment(), MainFragmentType.FAVORITE.tag)
            add(binding.containerView.id, StationFragment(), MainFragmentType.NEARBY.tag)
            commit()
        }

        supportFragmentManager.executePendingTransactions()
        supportFragmentManager.beginTransaction().apply {
            hide(supportFragmentManager.findFragmentByTag(MainFragmentType.FAVORITE.tag)!!)
            hide(supportFragmentManager.findFragmentByTag(MainFragmentType.NEARBY.tag)!!)
            commit()
        }
    }

    private fun setupViewModel() {
        mainViewModel.currentFragmentType.observe(this) { fragmentType ->
            val currentFragment = supportFragmentManager.findFragmentByTag(fragmentType.tag)
            supportFragmentManager.beginTransaction().apply {
                supportFragmentManager.fragments.forEach { fragment ->
                    if (fragment == currentFragment)
                        show(fragment)
                    else
                        hide(fragment)
                }
            }.commit()

            navigationViewMenuAdapter.setItemSelected(fragmentType.ordinal)
        }
    }

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

    private fun setupNavigationView() {
        if (UserData.isLoggedIn) {
            binding.layoutSignIn.visibility = View.GONE
            binding.layoutProfile.visibility = View.VISIBLE
            binding.divider.visibility = View.VISIBLE
            binding.txtSignOut.visibility = View.VISIBLE
            binding.txtNickname.text = UserData.nickname
            binding.txtEmail.text = UserData.email
        } else {
            binding.layoutSignIn.visibility = View.VISIBLE
            binding.layoutProfile.visibility = View.GONE
            binding.divider.visibility = View.GONE
            binding.txtSignOut.visibility = View.GONE
        }

        binding.layoutSignIn.setOnClickListener {
            // TODO: 로그인 화면으로 이동
        }

        binding.txtNickname.setOnClickListener {
            // TODO: 프로필 화면으로 이동
        }

        binding.recyclerMenu.apply {
            adapter = navigationViewMenuAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }

        navigationViewMenuAdapter.apply {
            setItemList(
                listOf(
                    NavigationViewMenuDto(R.drawable.icon_map, resources.getString(R.string.str_main_map), true),
                    NavigationViewMenuDto(R.drawable.icon_favorite, resources.getString(R.string.str_main_favorite), false),
                    NavigationViewMenuDto(R.drawable.icon_bike, resources.getString(R.string.str_main_nearby), false),
                )
            )
            setOnItemClickListener { _, position ->
                mainViewModel.setCurrentFragment(position)
                binding.drawerLayout.closeDrawer(binding.navigationView)
            }
        }
    }
}