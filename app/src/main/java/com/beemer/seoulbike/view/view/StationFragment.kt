package com.beemer.seoulbike.view.view

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.airbnb.lottie.LottieAnimationView
import com.beemer.seoulbike.R
import com.beemer.seoulbike.databinding.FragmentStationBinding
import com.beemer.seoulbike.model.dto.StationDto
import com.beemer.seoulbike.model.entity.FavoriteStationEntity
import com.beemer.seoulbike.view.adapter.FavoriteAdapter
import com.beemer.seoulbike.view.adapter.StationAdapter
import com.beemer.seoulbike.viewmodel.BikeViewModel
import com.beemer.seoulbike.viewmodel.FavoriteStationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class StationFragment : Fragment(), FavoriteAdapter.OnFavoriteClickListener, StationAdapter.OnFavoriteClickListener {
    private var _binding : FragmentStationBinding? = null
    private val binding get() = _binding!!

    private val favoriteStationViewModel by viewModels<FavoriteStationViewModel>()
    private val bikeViewModel by viewModels<BikeViewModel>()

    private lateinit var favoriteAdapter: FavoriteAdapter
    private lateinit var stationAdapter: StationAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var isViewModelInitialized = false
    private var lat: Double = 0.0
    private var lon: Double = 0.0
    private var favoriteStationIds = emptyList<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStationBinding.inflate(inflater, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupRecyclerView()
        getLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun setOnFavoriteClick(item: StationDto, lottie: LottieAnimationView) {
        if (lottie.progress == 1.0f) {
            DefaultDialog(
                title = null,
                message = "즐겨찾기에서 삭제하시겠습니까?",
                onConfirm = {
                    favoriteStationViewModel.deleteFavoriteStation(item.stationId)
                    lottie.progress = 0.0f
                    lottie.cancelAnimation()
                }
            ).show(childFragmentManager, "DefaultDialog")
        } else if (lottie.progress == 0.0f) {
            favoriteStationViewModel.insertFavoriteStation(FavoriteStationEntity(stationId = item.stationId))
            lottie.playAnimation()
        }
    }

    private fun setupView() {
        binding.swipeRefreshLayout.apply {
            isRefreshing = true
            setColorSchemeResources(R.color.colorSecondary)
            setOnRefreshListener {
                favoriteStationViewModel.favoriteStation.value?.let { stations ->
                    if (lat != 0.0 && lon != 0.0)
                        bikeViewModel.getFavoriteStations(lat, lon, null, null, stations.map { it.stationId })
                    else
                        Toast.makeText(requireContext(), "잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
                getLocation()
            }
        }

        binding.btnRetry.setOnClickListener {
            getLocation()
            binding.swipeRefreshLayout.isRefreshing = true
        }
    }

    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteAdapter(this)
        stationAdapter = StationAdapter(this)

        binding.recyclerFavorite.apply {
            adapter = favoriteAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

        binding.recyclerStation.apply {
            adapter = stationAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

        favoriteAdapter.setOnItemClickListener { item, _ ->
            StationDetailsDialog(
                item = item
            ).show(childFragmentManager, "DetailsDialog")
        }

        stationAdapter.setOnItemClickListener { item, _ ->
            StationDetailsDialog(
                item = item
            ).show(childFragmentManager, "DetailsDialog")
        }
    }

    private fun setupViewModel() {
        favoriteStationViewModel.apply {
            favoriteStation.observe(viewLifecycleOwner) { stations ->
                if (stations.isEmpty()) {
                    favoriteAdapter.setItemList(emptyList())
                } else {
                    bikeViewModel.getFavoriteStations(lat, lon, null, null, stations.map { it.stationId })
}
                favoriteStationIds = stations.map { it.stationId }

                binding.txtFavoriteCount.text = stations.size.toString()
            }
        }

        bikeViewModel.apply {
            favoriteStations.observe(viewLifecycleOwner) { stations ->
                val updatedList = stations.map { station ->
                    station.copy(isFavorite = station.stationId in favoriteStationIds)
                }
                favoriteAdapter.setItemList(updatedList.sortedBy { it.distance })
            }

            nearbyStations.observe(viewLifecycleOwner) { stations ->
                binding.swipeRefreshLayout.isRefreshing = false
                binding.layoutBody.visibility = View.VISIBLE
                binding.txtEmptyList.visibility = if (stations.isEmpty()) View.VISIBLE else View.GONE

                val updatedList = stations.map { station ->
                    station.copy(isFavorite = station.stationId in favoriteStationIds)
                }
                stationAdapter.setItemList(updatedList.filter { it.stationStatus.qrBikeCnt != null }.sortedBy { it.distance })
            }

            errorMessage.observe(viewLifecycleOwner) { message ->
                if (message != null) {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.layoutBody.visibility = View.GONE
                    binding.txtEmptyList.visibility = View.GONE

                    binding.txtError.visibility = View.VISIBLE
                    binding.btnRetry.visibility = View.VISIBLE
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener {
            if (!isViewModelInitialized) {
                setupViewModel()
                isViewModelInitialized = true
            }

            lat = it.latitude
            lon = it.longitude

            val geocoder = Geocoder(requireContext(), Locale.KOREA)
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            val fullAddress = addresses?.get(0)?.getAddressLine(0) ?: "위치를 찾을 수 없습니다."
            val addressParts = fullAddress.split(" ")
            val shortAddress = if (addressParts.size >= 4) "${addressParts[2]} ${addressParts[3]}" else ""

            binding.txtAddress.text = shortAddress

            binding.txtError.visibility = View.GONE
            binding.btnRetry.visibility = View.GONE
            bikeViewModel.getNearbyStations(lat, lon, lat, lon, 500.0)
        }.addOnFailureListener { }
    }
}