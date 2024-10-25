package com.beemer.seoulbike.view.view

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.beemer.seoulbike.R
import com.beemer.seoulbike.databinding.DialogStationDetailsBinding
import com.beemer.seoulbike.model.dto.StationListDto
import com.beemer.seoulbike.view.utils.UnitConversion.formatDistance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource

class StationDetailsDialog(private val item: StationListDto) : DialogFragment(), OnMapReadyCallback {
    private var _binding: DialogStationDetailsBinding? = null
    private val binding get() = _binding!!

    private val PERMISSION_REQUEST_CODE = 1001

    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogStationDetailsBinding.inflate(inflater, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDialog()
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
            logoGravity = Gravity.BOTTOM or Gravity.START // 네이버 로고 위치
            setLogoMargin(16, 0, 0, 16) // 네이버 로고 마진
        }
        binding.locationButton.map = naverMap // 현위치 버튼 설정

        // 레이어 설정
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BICYCLE, true) // 자전거 도로, 자전거 주차대 등 자전거와 관련된 요소 표시

        // 현위치 설정
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
        getLocation(naverMap)

        // 카메라 이동
        val lat = item.stationDetails.lat
        val lon = item.stationDetails.lon

        if (lat != null && lon != null) {
            val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(lat, lon.toDouble()), 15.5)
            naverMap.moveCamera(cameraUpdate)

            Marker().apply {
                position = LatLng(lat, lon)
                icon = OverlayImage.fromResource(R.drawable.icon_marker)
                width = 160
                height = 160
                map = naverMap
            }
        }
    }

    private fun setupDialog() {
        dialog?.apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.attributes?.width = (context.resources.displayMetrics.widthPixels.times(0.85)).toInt()
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
        binding.txtName.text = "${item.stationNo.replace("^0+".toRegex(), "")}. ${item.stationNm}"
        binding.txtAddress.text = item.stationDetails.addr1
        binding.txtParking.text = item.stationStatus.parkingCnt.toString()
        binding.txtRack.text = item.stationStatus.rackCnt.toString()
        binding.txtDistance.text = item.distance?.let { formatDistance(it) }

        binding.txtParking.setTextColor(
            when (item.stationStatus.parkingCnt) {
                0 -> ContextCompat.getColor(binding.root.context, R.color.red)
                in 1..3 -> ContextCompat.getColor(binding.root.context, R.color.yellow)
                else -> ContextCompat.getColor(binding.root.context, R.color.colorPrimary)
            }
        )
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
        }.addOnFailureListener { }
    }
}