package com.beemer.seoulbike.view.view

import android.annotation.SuppressLint
import android.graphics.PointF
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.beemer.seoulbike.R
import com.beemer.seoulbike.databinding.FragmentMapBinding
import com.beemer.seoulbike.databinding.MarkerCustomBinding
import com.beemer.seoulbike.viewmodel.BikeViewModel
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)

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
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BICYCLE, true) // 자전거 도로, 자전거 주차대 등 자전거와 관련된 요소 표시

        // 현위치 설정
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        getLocation(naverMap)

        // 카메라 설정
        naverMap.minZoom = 13.5
        naverMap.maxZoom = 18.0
        naverMap.extent = LatLngBounds(LatLng(37.413294, 126.734086), LatLng(37.715133, 127.269311))

        // 카메라 이동시
        naverMap.addOnLocationChangeListener { location ->
            val lat = location.latitude
            val lon = location.longitude

            bikeViewModel.setMyLocation(lat, lon)
        }

        // 카메라 이동 후 멈출 때
        naverMap.addOnCameraIdleListener {
            val cameraPosition = naverMap.cameraPosition
            val lat = cameraPosition.target.latitude
            val lon = cameraPosition.target.longitude

            getNearbyStations(lat, lon, cameraPosition.zoom)
        }
    }

    private fun setupMap() {
        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(binding.mapView.id) as com.naver.maps.map.MapFragment?
            ?: com.naver.maps.map.MapFragment.newInstance().also {
                fm.beginTransaction().add(binding.mapView.id, it).commit()
            }

        mapFragment.getMapAsync(this)
    }

    private fun setupView() {
        bindProgressButton(binding.btnReload)

        binding.btnReload.setOnClickListener {
            if (bikeViewModel.isLoading.value == false) {
                binding.btnReload.showProgress {
                    buttonTextRes = R.string.str_map_reloading
                    progressColor = ContextCompat.getColor(requireContext(), R.color.white)
                }

                val cameraPosition = naverMap.cameraPosition
                val lat = cameraPosition.target.latitude
                val lon = cameraPosition.target.longitude

                getNearbyStations(lat, lon, cameraPosition.zoom)
            }
        }
    }

    private fun setupViewModel() {
        bikeViewModel.apply {
            nearbyStations.observe(viewLifecycleOwner) { stations ->
                bikeViewModel.setLoading(false)
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

                        val markerBinding = MarkerCustomBinding.inflate(LayoutInflater.from(context))
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
                                val cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lon)).animate(CameraAnimation.Easing)
                                naverMap.moveCamera(cameraUpdate)

                                StationStatusBottomsheetDialog(
                                    item = station
                                ).show(childFragmentManager, "StatusBottomsheetDialog")
                                true
                            }
                            map = naverMap
                        }
                        markerList.add(marker)
                    }
                }
            }

            errorCode.observe(viewLifecycleOwner) { code ->
                if (code != null)
                    binding.btnReload.hideProgress(R.string.str_map_reload)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(naverMap: NaverMap) {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener {
            val lat = it.latitude
            val lon = it.longitude

            bikeViewModel.setMyLocation(lat, lon)

            naverMap.locationOverlay.run {
                isVisible = true
                position = LatLng(lat, lon)
            }

            val zoomLevel = 14.5

            val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(lat, lon), zoomLevel)
            naverMap.moveCamera(cameraUpdate)

            getNearbyStations(lat, lon, zoomLevel)
        }.addOnFailureListener { }
    }

    private fun getNearbyStations(mapLat: Double, mapLon: Double, zoomLevel: Double) {
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
        } else {
            val myLat = bikeViewModel.myLocation.value?.first
            val myLon = bikeViewModel.myLocation.value?.second

            bikeViewModel.getNearbyStations(myLat ?: mapLat, myLon ?: mapLon, mapLat, mapLon, distance)
            bikeViewModel.setLoading(true)
        }
    }
}