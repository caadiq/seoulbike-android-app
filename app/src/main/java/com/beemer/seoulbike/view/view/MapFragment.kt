package com.beemer.seoulbike.view.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.beemer.seoulbike.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource


class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding : FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val PERMISSION_REQUEST_CODE = 1001

    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMap()
        setupView()
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
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BICYCLE, true) // 자전거 도로, 자전거 주차대 등 자전거와 관련된 요소 표시

        // 현위치 설정
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        getLocation(naverMap)
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
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(naverMap: NaverMap) {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener {
            val lat = it.latitude
            val lon = it.longitude

            Log.d("테스트", "lat: $lat, lon: $lon")

            naverMap.locationOverlay.run {
                isVisible = true
                position = LatLng(lat, lon)
            }

            val cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lon))
            naverMap.moveCamera(cameraUpdate)
        }.addOnFailureListener { }
    }
}