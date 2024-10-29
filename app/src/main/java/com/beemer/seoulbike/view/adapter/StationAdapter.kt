package com.beemer.seoulbike.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.beemer.seoulbike.databinding.RowStationsBinding
import com.beemer.seoulbike.model.dto.StationListDto
import com.beemer.seoulbike.view.diff.StationListDiffUtil
import com.beemer.seoulbike.view.utils.UnitConversion.formatDistance

class StationAdapter(private val listener: OnFavoriteClickListener,) : RecyclerView.Adapter<StationAdapter.ViewHolder>() {
    interface OnFavoriteClickListener {
        fun setOnFavoriteClick(item: StationListDto, lottie: LottieAnimationView)
    }

    private var itemList = mutableListOf<StationListDto>()
    private var onItemClickListener: ((StationListDto, Int) -> Unit)? = null

    private val favoriteStationId = mutableListOf<String>()

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowStationsBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    inner class ViewHolder(private val binding: RowStationsBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(itemList[position], position)
                }
            }
        }

        fun bind(item: StationListDto) {
            binding.txtNo.text = item.stationNo.replace("^0+".toRegex(), "")
            binding.txtName.text = item.stationNm
            binding.txtDistance.text = item.distance?.let { formatDistance(it)}
            binding.txtQrBike.text = item.stationStatus.qrBikeCnt.toString()
            binding.txtElecBike.text = item.stationStatus.elecBikeCnt.toString()
            if (item.stationId in favoriteStationId) binding.lottie.playAnimation() else binding.lottie.progress = 0.0f

            binding.lottie.setOnClickListener {
                listener.setOnFavoriteClick(item, binding.lottie)
            }
        }
    }

    fun setOnItemClickListener(listener: (StationListDto, Int) -> Unit) {
        onItemClickListener = listener
    }

    fun setItemList(list: List<StationListDto>) {
        val diffCallBack = StationListDiffUtil(itemList, list)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)

        itemList.clear()
        itemList.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }

    fun setFavoriteStation(stationId: List<String>) {
        favoriteStationId.clear()
        favoriteStationId.addAll(stationId)

        for (position in itemList.indices) {
            notifyItemChanged(position)
        }
    }
}