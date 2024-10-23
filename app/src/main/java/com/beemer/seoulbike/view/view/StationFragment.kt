package com.beemer.seoulbike.view.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.beemer.seoulbike.R
import com.beemer.seoulbike.databinding.FragmentStationBinding
import com.beemer.seoulbike.view.adapter.StationAdapter
import com.beemer.seoulbike.view.utils.DateTimeConverter.convertDateTime
import com.beemer.seoulbike.viewmodel.BikeViewModel
import com.beemer.seoulbike.viewmodel.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class StationFragment : Fragment() {
    private var _binding : FragmentStationBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel by viewModels<MainViewModel>()
    private val bikeViewModel by viewModels<BikeViewModel>()

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupView() {
        binding.progressIndicator.show()

        binding.swipeRefreshLayout.setOnRefreshListener {
            mainViewModel.distance.value?.let { distance ->
                getLocation(distance)
            }
        }

        binding.btnRetry.setOnClickListener {
            mainViewModel.distance.value?.let { distance ->
                getLocation(distance)
                binding.progressIndicator.show()
            }
        }

        val items = listOf("500m 이내", "1km 이내", "2km 이내")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_selected, R.id.txtTitle, items)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        binding.spinner.apply {
            this.adapter = adapter
            binding.spinner.setSelection(1)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when (position) {
                        0 -> {
                            mainViewModel.setDistance(500.0)
                            mainViewModel.setEmptyListText("500m 이내에 대여소가 없습니다.")
                        }
                        1 -> {
                            mainViewModel.setDistance(1000.0)
                            mainViewModel.setEmptyListText("1km 이내에 대여소가 없습니다.")
                        }
                        2 -> {
                            mainViewModel.setDistance(2000.0)
                            mainViewModel.setEmptyListText("2km 이내에 대여소가 없습니다.")
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = stationAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }
    }

    private fun setupViewModel() {
        mainViewModel.apply {
            distance.observe(viewLifecycleOwner) { distance ->
                stationAdapter.setItemList(emptyList())
                getLocation(distance)
                binding.progressIndicator.show()
            }
        }

        bikeViewModel.apply {
            nearbyStations.observe(viewLifecycleOwner) { stations ->
                binding.progressIndicator.hide()
                binding.swipeRefreshLayout.isRefreshing = false
                binding.txtEmptyList.visibility = if (stations.isEmpty()) View.VISIBLE else View.GONE

                stationAdapter.setItemList(
                    stations.filter { it.stationStatus.parkingCnt != null }.sortedBy { it.distance }
                )

                stations[0].stationStatus.updateTime?.let {
                    binding.txtUpdate.text = "${convertDateTime(it, "yyyy-MM-dd'T'HH:mm:ss", "HH:mm:ss", Locale.KOREA)} 기준"
                }
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
    private fun getLocation(distance: Double) {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener {
            val lat = it.latitude
            val lon = it.longitude

            binding.txtError.visibility = View.GONE
            binding.btnRetry.visibility = View.GONE
            bikeViewModel.getNearbyStations(lat, lon, distance)
        }.addOnFailureListener { }
    }
}