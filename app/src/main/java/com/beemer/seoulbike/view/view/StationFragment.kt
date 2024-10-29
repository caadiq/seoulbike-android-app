package com.beemer.seoulbike.view.view

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.airbnb.lottie.LottieAnimationView
import com.beemer.seoulbike.databinding.FragmentStationBinding
import com.beemer.seoulbike.model.dto.StationListDto
import com.beemer.seoulbike.model.entity.FavoriteStationEntity
import com.beemer.seoulbike.view.adapter.BookmarkAdapter
import com.beemer.seoulbike.view.adapter.StationAdapter
import com.beemer.seoulbike.viewmodel.BikeViewModel
import com.beemer.seoulbike.viewmodel.FavoriteStationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class StationFragment : Fragment(), BookmarkAdapter.OnFavoriteClickListener, StationAdapter.OnFavoriteClickListener {
    private var _binding : FragmentStationBinding? = null
    private val binding get() = _binding!!

    private val favoriteStationViewModel by viewModels<FavoriteStationViewModel>()
    private val bikeViewModel by viewModels<BikeViewModel>()

    private lateinit var  bookmarkAdapter: BookmarkAdapter
    private lateinit var stationAdapter: StationAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var lat: Double? = null
    private var lon: Double? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStationBinding.inflate(inflater, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupRecyclerView()
        setupViewModel()
        getLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun setOnFavoriteClick(item: StationListDto, lottie: LottieAnimationView) {
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
        binding.swipeRefreshLayout.isRefreshing = true

        binding.swipeRefreshLayout.setOnRefreshListener {
            getLocation()
        }

        binding.btnRetry.setOnClickListener {
            getLocation()
            binding.swipeRefreshLayout.isRefreshing = true
        }
    }

    private fun setupRecyclerView() {
        bookmarkAdapter = BookmarkAdapter(this)
        stationAdapter = StationAdapter(this)

        binding.recyclerBookmark.apply {
            adapter = bookmarkAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

        binding.recyclerStation.apply {
            adapter = stationAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

        bookmarkAdapter.setOnItemClickListener { item, _ ->
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
            top5FavoriteStation.observe(viewLifecycleOwner) { stations ->
                if (stations.isEmpty())
                    bookmarkAdapter.setItemList(emptyList())
                else
                    bikeViewModel.getFavoriteStations(lat, lon, null, null, stations.map { it.stationId })
            }

            favoriteStation.observe(viewLifecycleOwner) { stations ->
                val favoriteStationIds = stations.map { it.stationId }
                bookmarkAdapter.setFavoriteStation(favoriteStationIds)
                stationAdapter.setFavoriteStation(favoriteStationIds)

                binding.txtBookmarkCount.text = stations.size.toString()
            }
        }

        bikeViewModel.apply {
            favoriteStations.observe(viewLifecycleOwner) { stations ->
                bookmarkAdapter.setItemList(stations)
            }

            nearbyStations.observe(viewLifecycleOwner) { stations ->
                binding.swipeRefreshLayout.isRefreshing = false
                binding.txtEmptyList.visibility = if (stations.isEmpty()) View.VISIBLE else View.GONE

                stationAdapter.setItemList(
                    stations.filter { it.stationStatus.qrBikeCnt != null }.sortedBy { it.distance }
                )
            }

            errorMessage.observe(viewLifecycleOwner) { message ->
                if (message != null) {
                    binding.swipeRefreshLayout.isRefreshing = false
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
            lat = it.latitude
            lon = it.longitude

            val geocoder = Geocoder(requireContext(), Locale.KOREA)
            val addresses = geocoder.getFromLocation(lat!!, lon!!, 1)
            val fullAddress = addresses?.get(0)?.getAddressLine(0) ?: "위치를 찾을 수 없습니다."
            val addressParts = fullAddress.split(" ")
            val shortAddress = if (addressParts.size >= 4) "${addressParts[2]} ${addressParts[3]}" else ""

            binding.txtAddress.text = shortAddress

            binding.txtError.visibility = View.GONE
            binding.btnRetry.visibility = View.GONE
            bikeViewModel.getNearbyStations(lat!!, lon!!, lat!!, lon!!, 500.0)
        }.addOnFailureListener { }
    }
}