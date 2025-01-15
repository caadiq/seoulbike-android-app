package com.beemer.seoulbike.view.view

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PointF
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.beemer.seoulbike.R
import com.beemer.seoulbike.databinding.ActivityMainBinding
import com.beemer.seoulbike.databinding.MarkerCustomBinding
import com.beemer.seoulbike.model.data.UserData
import com.beemer.seoulbike.model.dto.NavigationViewMenuDto
import com.beemer.seoulbike.model.dto.TokenDto
import com.beemer.seoulbike.view.adapter.NavigationViewMenuAdapter
import com.beemer.seoulbike.viewmodel.AuthViewModel
import com.beemer.seoulbike.viewmodel.BikeViewModel
import com.beemer.seoulbike.viewmodel.DataStoreViewModel
import com.beemer.seoulbike.viewmodel.MainViewModel
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val dataStoreViewModel by viewModels<DataStoreViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()
    private val mainViewModel by viewModels<MainViewModel>()
    private val bikeViewModel by viewModels<BikeViewModel>()

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

    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource
    private val markerList = mutableListOf<Marker>()

    private var isFirstLoad = false
    private var myLocation: Pair<Double?, Double?> = Pair(null, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splashScreen()
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)

        checkPermissions()
        setupInsets()
        setupMap()
        setupView()
        setupViewModel()
    }

    override fun onResume() {
        super.onResume()
        setupNavigationView()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
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

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        // UI 컨트롤 설정
        val uiSettings = naverMap.uiSettings
        uiSettings.apply {
            isCompassEnabled = false // 나침반
            isScaleBarEnabled = false // 축척바
            isZoomControlEnabled = false // 줌 컨트롤
            isLocationButtonEnabled = false // 현위치 버튼
            logoGravity = Gravity.BOTTOM or Gravity.START // 네이버 로고 위치
            setLogoMargin(0, 0, 0, 0) // 네이버 로고 마진
        }
        binding.scaleBar.map = naverMap // 축척바 설정
        binding.locationButton.map = naverMap // 현위치 버튼 설정

        // 레이어 설정
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BICYCLE, true) // 자전거 도로, 자전거 주차대 등 자전거와 관련된 요소 표시

        // 현위치 설정
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        getLocation(naverMap)

        // 카메라 설정
        naverMap.minZoom = 10.0
        naverMap.maxZoom = 18.0
        naverMap.extent = LatLngBounds(LatLng(37.413294, 126.734086), LatLng(37.715133, 127.269311))

        // 카메라 이동 후 멈출 때
        naverMap.addOnCameraIdleListener {
            getNearbyStations(naverMap)
        }
    }

    private fun splashScreen() {
        splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition { true }

        lifecycleScope.launch {
            val accessToken = dataStoreViewModel.accessTokenFlow.first()
            val refreshToken = dataStoreViewModel.refreshTokenFlow.first()

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
            reissueAllTokens.observe(this@MainActivity) { tokens ->
                UserData.apply {
                    isLoggedIn = true
                    accessToken = tokens.accessToken
                    refreshToken = tokens.refreshToken
                }

                authViewModel.getUser()
                tokens.accessToken?.let { dataStoreViewModel.saveAccessToken(it) }
                tokens.refreshToken?.let { dataStoreViewModel.saveRefreshToken(it) }
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
                if (code == null)
                    return@observe

                splashScreen.setKeepOnScreenCondition { false }
            }
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, locationPermissions[0]) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, locationPermissions[1]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, locationPermissions, PERMISSION_REQUEST_CODE)
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutChild) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.mapView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.navigationView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<MarginLayoutParams> { bottomMargin = insets.bottom }
            v.setPadding(insets.left, insets.top, insets.right, 0)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupMap() {
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(binding.mapView.id) as com.naver.maps.map.MapFragment?
            ?: com.naver.maps.map.MapFragment.newInstance().also {
                fm.beginTransaction().add(binding.mapView.id, it).commit()
            }

        mapFragment.getMapAsync(this)
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

        bindProgressButton(binding.btnReload)

        binding.btnReload.setOnClickListener {
            if (bikeViewModel.isLoading.value == false) {
                binding.btnReload.showProgress {
                    buttonTextRes = R.string.str_map_reloading
                    progressColor = ContextCompat.getColor(this@MainActivity, R.color.white)
                }

                getNearbyStations(naverMap)
            }
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
            binding.drawerLayout.closeDrawer(binding.navigationView)
            startActivity(Intent(this, SignInActivity::class.java))
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
                    NavigationViewMenuDto(R.drawable.icon_favorite, resources.getString(R.string.str_main_favorite)),
                    NavigationViewMenuDto(R.drawable.icon_bike, resources.getString(R.string.str_main_nearby))
                )
            )
            setOnItemClickListener { _, position ->
                binding.drawerLayout.closeDrawer(binding.navigationView)
                when (position) {
                    0 -> {}
                    1 -> {}
                }
            }
        }
    }

    private fun setupViewModel() {
        bikeViewModel.apply {
            nearbyStations.observe(this@MainActivity) { stations ->
                binding.btnReload.hideProgress(R.string.str_map_reload)

                markerList.forEach { it.map = null }
                markerList.clear()

                stations.forEach { station ->
                    val lat = station.stationDetails.lat
                    val lon = station.stationDetails.lon

                    val qrBikeCnt = station.stationStatus.qrBikeCnt
                    val elecBikeCnt = station.stationStatus.elecBikeCnt

                    if (lat != null && lon != null && qrBikeCnt != null && elecBikeCnt != null) {
                        val totalBikeCnt = qrBikeCnt + elecBikeCnt

                        val markerBinding = MarkerCustomBinding.inflate(LayoutInflater.from(this@MainActivity))
                        markerBinding.txtCount.text = "${totalBikeCnt}대"

                        markerBinding.layoutParent.background = when (totalBikeCnt) {
                            0 -> ResourcesCompat.getDrawable(resources, R.drawable.chat_bubble_red, null)
                            in 1..2 -> ResourcesCompat.getDrawable(resources, R.drawable.chat_bubble_yellow, null)
                            else -> ResourcesCompat.getDrawable(resources, R.drawable.chat_bubble_primary, null)
                        }

                        val marker = Marker().apply {
                            position = LatLng(lat, lon)
                            icon = OverlayImage.fromView(markerBinding.root)
                            anchor = PointF(0.2f, 1.0f)
                            onClickListener = Overlay.OnClickListener {
                                val cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lon)).animate(
                                    CameraAnimation.Easing)
                                naverMap.moveCamera(cameraUpdate)

                                StationStatusBottomsheetDialog(
                                    item = station
                                ).show(supportFragmentManager, "StatusBottomsheetDialog")
                                true
                            }
                            map = naverMap
                        }
                        markerList.add(marker)
                    }
                }
            }

            errorCode.observe(this@MainActivity) { code ->
                if (code != null)
                    binding.btnReload.hideProgress(R.string.str_map_reload)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(naverMap: NaverMap) {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!isGpsEnabled) {
            Toast.makeText(this, "GPS가 꺼져 있어 내 위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener {
            val lat = it.latitude
            val lon = it.longitude

            myLocation = Pair(lat, lon)
            mainViewModel.setMyLocation(lat, lon)

            naverMap.locationOverlay.run {
                isVisible = true
                position = LatLng(lat, lon)
            }

            val zoomLevel = 14.5

            val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(lat, lon), zoomLevel)
            naverMap.moveCamera(cameraUpdate)
        }.addOnFailureListener {}
    }

    private fun getNearbyStations(naverMap: NaverMap) {
        if (!isFirstLoad) {
            isFirstLoad = true
            return
        }

        val cameraPosition = naverMap.cameraPosition
        val lat = cameraPosition.target.latitude
        val lon = cameraPosition.target.longitude
        val zoomLevel = cameraPosition.zoom

        val distance = when (zoomLevel) {
            in 13.5..14.5 -> 1500.0
            in 14.5..15.0 -> 1000.0
            in 15.0..15.5 -> 700.0
            in 15.5..16.0 -> 500.0
            in 16.0..16.5 -> 300.0
            in 16.5..17.0 -> 200.0
            in 17.0..17.5 -> 150.0
            in 17.5..18.0 -> 100.0
            else -> null
        }

        if (distance == null) {
            markerList.forEach { it.map = null }
            markerList.clear()
            binding.btnReload.visibility = View.GONE
        } else {
            val myLat = myLocation.first
            val myLon = myLocation.second

            bikeViewModel.getNearbyStations(myLat ?: lat, myLon ?: lon, lat, lon, distance, UserData.accessToken)

            bikeViewModel.setLoading(true)
            binding.btnReload.visibility = View.VISIBLE
        }
    }
}