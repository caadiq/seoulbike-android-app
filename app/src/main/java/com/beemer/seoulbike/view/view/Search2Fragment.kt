package com.beemer.seoulbike.view.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.beemer.seoulbike.databinding.FragmentSearch2Binding
import com.beemer.seoulbike.model.dto.StationDto
import com.beemer.seoulbike.model.entity.FavoriteStationEntity
import com.beemer.seoulbike.view.adapter.StationSearchAdapter
import com.beemer.seoulbike.viewmodel.BikeViewModel
import com.beemer.seoulbike.viewmodel.FavoriteStationViewModel
import com.beemer.seoulbike.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Search2Fragment : Fragment(), StationSearchAdapter.OnFavoriteClickListener  {
    private var _binding : FragmentSearch2Binding? = null
    private val binding get() = _binding!!

    private val searchViewModel by activityViewModels<SearchViewModel>()
    private val bikeViewModel by activityViewModels<BikeViewModel>()
    private val favoriteStationViewModel by viewModels<FavoriteStationViewModel>()

    private lateinit var stationSearchAdapter: StationSearchAdapter

    private var isLoading = false
    private var isRefreshed = false
    private var favoriteStationIds = emptyList<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearch2Binding.inflate(inflater, container, false)
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
        binding.swipeRefreshLayout.setOnRefreshListener {
            getStations()
        }
    }

    private fun setupRecyclerView() {
        stationSearchAdapter = StationSearchAdapter(this)

        binding.recyclerView.apply {
            adapter = stationSearchAdapter
            setHasFixedSize(true)
            itemAnimator = null

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = recyclerView.adapter?.itemCount ?: 0

                    if (totalItemCount > 0 && !isLoading && lastVisibleItemPosition >= totalItemCount - 3) {
                        bikeViewModel.page.value?.let { page ->
                            page.nextPage?.let {
                                getStations(it, false)
                            }
                        }
                    }
                }
            })
        }

        stationSearchAdapter.setOnItemClickListener { item, _ ->
            bikeViewModel.addPopularStation(item.stationId)

            StationDetailsDialog(
                item = item,
                onClose = { stationId, isFavorite ->
                    stationSearchAdapter.setItemList(
                        stationSearchAdapter.getItemList().map {
                            if (it.stationId == stationId)
                                it.copy(isFavorite = isFavorite)
                            else
                                it
                        }
                    )
                }
            ).show(childFragmentManager, "DetailsDialog")
        }
    }

    private fun setupViewModel() {
        bikeViewModel.apply {
            stations.observe(viewLifecycleOwner) { stations ->
                binding.swipeRefreshLayout.isRefreshing = false
                setLoading(false)

                binding.txtEmptyList.visibility = if (stations.isEmpty()) View.VISIBLE else View.GONE
                val updatedList = stations.map { station ->
                    station.copy(isFavorite = station.stationId in favoriteStationIds)
                }

                stationSearchAdapter.setItemList(updatedList.filter { it.stationStatus.qrBikeCnt != null })

                if (this@Search2Fragment.isRefreshed)
                    binding.recyclerView.scrollToPosition(0)
            }

            isLoading.observe(viewLifecycleOwner) { isLoading ->
                this@Search2Fragment.isLoading = isLoading

                if (isLoading)
                    stationSearchAdapter.showProgress()
                else
                    stationSearchAdapter.hideProgress()
            }

            isRefreshed.observe(viewLifecycleOwner) { isRefreshed ->
                this@Search2Fragment.isRefreshed = isRefreshed
            }
        }

        searchViewModel.apply {
            query.observe(viewLifecycleOwner) { query ->
                if (query != null)
                    binding.swipeRefreshLayout.isRefreshing = true
                stationSearchAdapter.setItemList(emptyList())
            }
        }

        favoriteStationViewModel.apply {
            favoriteStation.observe(viewLifecycleOwner) { stations ->
                favoriteStationIds = stations.map { it.stationId }
            }
        }
    }

    private fun getStations(page: Int = 0, refresh: Boolean = true) {
        val lat = searchViewModel.location.value?.first
        val lng = searchViewModel.location.value?.second
        val query = searchViewModel.query.value

        if (lat != null && lng != null && query != null)
            bikeViewModel.getStations(lat, lng, page, 20, query, refresh)
    }
}