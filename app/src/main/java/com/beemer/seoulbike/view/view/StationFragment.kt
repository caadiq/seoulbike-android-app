package com.beemer.seoulbike.view.view

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.beemer.seoulbike.databinding.FragmentStationBinding
import com.beemer.seoulbike.view.adapter.BookmarkAdapter
import com.beemer.seoulbike.view.adapter.StationAdapter
import com.beemer.seoulbike.viewmodel.BikeViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class StationFragment : Fragment() {
    private var _binding : FragmentStationBinding? = null
    private val binding get() = _binding!!

    private val bikeViewModel by viewModels<BikeViewModel>()

    private val bookmarkAdapter = BookmarkAdapter()
    private val stationAdapter = StationAdapter()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

    private fun setupView() {
        binding.progressIndicator.show()

        binding.swipeRefreshLayout.setOnRefreshListener {
            getLocation()
        }

        binding.btnRetry.setOnClickListener {
            getLocation()
            binding.progressIndicator.show()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerBookmark.apply {
            adapter = bookmarkAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

        bookmarkAdapter.setOnItemClickListener { item, _ ->
            StationDetailsDialog(
                item = item
            ).show(childFragmentManager, "DetailsDialog")
        }

        binding.recyclerStation.apply {
            adapter = stationAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

        stationAdapter.setOnItemClickListener { item, _ ->
            StationDetailsDialog(
                item = item
            ).show(childFragmentManager, "DetailsDialog")
        }
    }

    private fun setupViewModel() {
        bikeViewModel.apply {
            nearbyStations.observe(viewLifecycleOwner) { stations ->
                binding.progressIndicator.hide()
                binding.swipeRefreshLayout.isRefreshing = false
                binding.txtEmptyList.visibility = if (stations.isEmpty()) View.VISIBLE else View.GONE

                stationAdapter.setItemList(
                    stations.filter { it.stationStatus.qrBikeCnt != null }.sortedBy { it.distance }
                )
            }

            errorMessage.observe(viewLifecycleOwner) { message ->
                if (message != null) {
                    binding.progressIndicator.hide()
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
            val lat = it.latitude
            val lon = it.longitude

            val geocoder = Geocoder(requireContext(), Locale.KOREA)
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            val fullAddress = addresses?.get(0)?.getAddressLine(0) ?: "위치를 찾을 수 없습니다."
            val addressParts = fullAddress.split(" ")
            val shortAddress = if (addressParts.size >= 4) "${addressParts[2]} ${addressParts[3]}" else ""

            binding.txtAddress.text = shortAddress

            binding.txtError.visibility = View.GONE
            binding.btnRetry.visibility = View.GONE
            bikeViewModel.getNearbyStations(lat, lon, lat, lon, 700.0)
        }.addOnFailureListener { }
    }
}