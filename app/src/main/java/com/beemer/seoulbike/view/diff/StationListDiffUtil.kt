package com.beemer.seoulbike.view.diff

import androidx.recyclerview.widget.DiffUtil
import com.beemer.seoulbike.model.dto.StationDto

class StationListDiffUtil(private val oldList: List<StationDto>, private val newList: List<StationDto>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].stationId == newList[newItemPosition].stationId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}