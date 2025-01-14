package com.beemer.seoulbike.view.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.beemer.seoulbike.databinding.FragmentSearch1Binding
import com.beemer.seoulbike.model.data.UserData
import com.beemer.seoulbike.model.dto.StationPopularDto
import com.beemer.seoulbike.model.entity.SearchHistoryEntity
import com.beemer.seoulbike.view.adapter.PopularAdapter
import com.beemer.seoulbike.view.adapter.SearchHistoryAdapter
import com.beemer.seoulbike.viewmodel.BikeViewModel
import com.beemer.seoulbike.viewmodel.PopularViewModel
import com.beemer.seoulbike.viewmodel.SearchHistoryViewModel
import com.beemer.seoulbike.viewmodel.SearchViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Search1Fragment : Fragment(), SearchHistoryAdapter.OnDeleteClickListener {
    private var _binding : FragmentSearch1Binding? = null
    private val binding get() = _binding!!

    private val searchViewModel by activityViewModels<SearchViewModel>()
    private val searchHistoryViewModel by activityViewModels<SearchHistoryViewModel>()
    private val bikeViewModel by activityViewModels<BikeViewModel>()
    private val popularViewModel by activityViewModels<PopularViewModel>()

    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private val popularAdapter = PopularAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearch1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun setOnDeleteClick(item: String) {
        searchHistoryViewModel.deleteHistoryByTitle(item)
    }

    private fun setupRecyclerView() {
        searchHistoryAdapter = SearchHistoryAdapter(this)

        binding.recyclerSearch.apply {
            adapter = searchHistoryAdapter
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
            }
            itemAnimator = null
        }

        searchHistoryAdapter.setOnItemClickListener { item, _ ->
            searchViewModel.updateQuery(item)
            searchHistoryViewModel.checkTitleExists(item)
        }

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanIndex(position: Int, spanCount: Int): Int {
                return position % spanCount
            }

            override fun getSpanSize(position: Int): Int {
                return 1
            }
        }

        binding.recyclerPopular.apply {
            layoutManager = gridLayoutManager
            adapter = popularAdapter
        }

        popularAdapter.setOnItemClickListener { item, _ ->
            val (lat, lon) = searchViewModel.location.value ?: return@setOnItemClickListener
            bikeViewModel.getStationDetails(lat, lon, item.stationId, UserData.accessToken)
        }
    }

    private fun setupViewModel() {
        searchHistoryViewModel.apply {
            searchHistory.observe(viewLifecycleOwner) { history ->
                searchHistoryAdapter.setItemList(history.map { it.title })
            }

            isTitleExists.observe(viewLifecycleOwner) { exists ->
                searchViewModel.query.value?.let { query ->
                    if (exists)
                        searchHistoryViewModel.deleteHistoryByTitle(query)
                    searchHistoryViewModel.insertHistory(SearchHistoryEntity(title = query))
                }
            }
        }

        bikeViewModel.apply {
            stationDetails.observe(viewLifecycleOwner) { station ->
                StationDetailsDialog(
                    item = station
                ).show(childFragmentManager, "DetailsDialog")
            }
        }

        popularViewModel.apply {
            getPopularStations()

            popularStations.observe(viewLifecycleOwner) { stations ->
                binding.progressIndicator.hide()

                val rearrangedList = mutableListOf<StationPopularDto>()
                val halfSize = (stations.size + 1) / 2
                for (i in 0 until halfSize) {
                    rearrangedList.add(stations[i])
                    if (i + halfSize < stations.size) {
                        rearrangedList.add(stations[i + halfSize])
                    }
                }

                popularAdapter.setItemList(rearrangedList)
            }
        }
    }
}