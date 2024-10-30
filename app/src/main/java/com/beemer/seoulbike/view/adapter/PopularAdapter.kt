package com.beemer.seoulbike.view.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.beemer.seoulbike.R
import com.beemer.seoulbike.databinding.RowStationPopularBinding
import com.beemer.seoulbike.model.dto.StationPopularDto
import com.beemer.seoulbike.view.diff.PopularListDiffUtil

class PopularAdapter : RecyclerView.Adapter<PopularAdapter.ViewHolder>() {
    private var itemList = mutableListOf<StationPopularDto>()
    private var onItemClickListener: ((StationPopularDto, Int) -> Unit)? = null

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowStationPopularBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    inner class ViewHolder(private val binding: RowStationPopularBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(itemList[position], position)
                }
            }
        }

        fun bind(item: StationPopularDto) {
            binding.txtRank.text = "${item.rank}"
            binding.txtStationNo.text = "${item.stationNo}."
            binding.txtStationNm.text = item.stationNm

            binding.txtRank.backgroundTintList = if (item.rank <= 3)
                ColorStateList.valueOf(ResourcesCompat.getColor(itemView.resources, R.color.colorPrimary, null))
            else
                ColorStateList.valueOf(ResourcesCompat.getColor(itemView.resources, R.color.colorSecondary, null))
        }
    }

    fun setOnItemClickListener(listener: (StationPopularDto, Int) -> Unit) {
        onItemClickListener = listener
    }

    fun setItemList(list: List<StationPopularDto>) {
        val diffCallBack = PopularListDiffUtil(itemList, list)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)

        itemList.clear()
        itemList.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }
}