package com.beemer.seoulbike.view.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.beemer.seoulbike.databinding.FragmentMapBinding
import com.beemer.seoulbike.databinding.MarkerCustomBinding
import com.beemer.seoulbike.viewmodel.BikeViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding : FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val bikeViewModel by viewModels<BikeViewModel>()

    private val PERMISSION_REQUEST_CODE = 1001

    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource
    private val markerList = mutableListOf<Marker>()

    private var isInitialLocationSet = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMap()
        setupView()
        setupViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            logoGravity = Gravity.TOP or Gravity.START // 네이버 로고 위치
            setLogoMargin(16, 16, 0, 0) // 네이버 로고 마진
        }
        binding.scaleBar.map = naverMap // 축척바 설정
        binding.locationButton.map = naverMap // 현위치 버튼 설정

        // 레이어 설정
//        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BICYCLE, true) // 자전거 도로, 자전거 주차대 등 자전거와 관련된 요소 표시

        // 현위치 설정
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        getLocation(naverMap)

        setupCamera()
    }

    private fun setupMap() {
        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(binding.mapView.id) as com.naver.maps.map.MapFragment?
            ?: com.naver.maps.map.MapFragment.newInstance().also {
                fm.beginTransaction().add(binding.mapView.id, it).commit()
            }

        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)
    }

    private fun setupView() {
        binding.lottieRelolad.setOnClickListener {
            binding.lottieRelolad.playAnimation()

            val cameraPosition = naverMap.cameraPosition
            val lat = cameraPosition.target.latitude
            val lon = cameraPosition.target.longitude

            getNearbyStations(lat, lon, cameraPosition.zoom)
        }
    }

    private fun setupViewModel() {
        bikeViewModel.apply {
            nearbyStations.observe(viewLifecycleOwner) { stations ->
                markerList.forEach { it.map = null }
                markerList.clear()

                stations.forEach { station ->
                    val lat = station.stationDetails.lat
                    val lon = station.stationDetails.lon

                    if (lat != null && lon != null) {
                        station.stationStatus.parkingCnt?.let { count ->
                            val markerBinding = MarkerCustomBinding.inflate(LayoutInflater.from(context))
                            markerBinding.txtCount.text = "${count}대"

                            val marker = Marker().apply {
                                position = LatLng(lat, lon)
                                icon = OverlayImage.fromView(markerBinding.root)
                                setCaptionAligns(Align.TopRight)
                                onClickListener = Overlay.OnClickListener {
                                    Toast.makeText(context, station.stationNm, Toast.LENGTH_SHORT).show()

                                    true
                                }
                                map = naverMap
                            }
                            markerList.add(marker)
                        }
                    }
                }
            }
        }
    }

    private fun setupCamera() {
        naverMap.addOnCameraIdleListener {
            if (isInitialLocationSet) {
                val cameraPosition = naverMap.cameraPosition
                val lat = cameraPosition.target.latitude
                val lon = cameraPosition.target.longitude

                getNearbyStations(lat, lon, cameraPosition.zoom)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(naverMap: NaverMap) {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener {
            val lat = it.latitude
            val lon = it.longitude

            naverMap.locationOverlay.run {
                isVisible = true
                position = LatLng(lat, lon)
            }

            val zoomLevel = 17.0

            val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(lat, lon), zoomLevel)
            naverMap.moveCamera(cameraUpdate)

            isInitialLocationSet = true

            getNearbyStations(lat, lon, zoomLevel)
        }.addOnFailureListener { }
    }

    private fun getNearbyStations(lat: Double, lon: Double, zoomLevel: Double) {
        val distance = when (zoomLevel) {
            in 0.0..13.0 -> null
            in 13.5..14.0 -> 2000.0
            in 14.0..14.5 -> 1500.0
            in 14.5..15.0 -> 1000.0
            in 15.0..15.5 -> 700.0
            in 15.5..16.0 -> 500.0
            in 16.0..16.5 -> 300.0
            in 16.5..17.0 -> 200.0
            in 17.0..17.5 -> 150.0
            in 17.5..18.0 -> 100.0
            in 18.0..21.0 -> 50.0
            else -> null
        }

        if (distance == null) {
            markerList.forEach { it.map = null }
            markerList.clear()
        } else {
            bikeViewModel.getNearbyStations(lat, lon, distance)
        }
    }
}