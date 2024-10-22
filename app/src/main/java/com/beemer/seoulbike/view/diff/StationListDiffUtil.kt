package com.beemer.seoulbike.view.diff

import androidx.recyclerview.widget.DiffUtil
import com.beemer.seoulbike.model.dto.NearbyStationListDto

class StationListDiffUtil(private val oldList: List<NearbyStationListDto>, private val newList: List<NearbyStationListDto>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].stationNo == newList[newItemPosition].stationNo
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].stationNo == newList[newItemPosition].stationNo
    }
}