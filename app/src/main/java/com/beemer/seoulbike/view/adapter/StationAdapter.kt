package com.beemer.seoulbike.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.beemer.seoulbike.databinding.RowStationsBinding
import com.beemer.seoulbike.model.dto.StationDto
import com.beemer.seoulbike.view.diff.StationListDiffUtil
import com.beemer.seoulbike.view.utils.UnitConversion.formatDistance

class StationAdapter(private val listener: OnFavoriteClickListener) : RecyclerView.Adapter<StationAdapter.ViewHolder>() {
    interface OnFavoriteClickListener {
        fun setOnFavoriteClick(item: StationDto, lottie: LottieAnimationView)
    }

    private var itemList = mutableListOf<StationDto>()
    private var onItemClickListener: ((StationDto, Int) -> Unit)? = null

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

        fun bind(item: StationDto) {
            binding.txtNo.text = item.stationNo.replace("^0+".toRegex(), "")
            binding.txtName.text = item.stationNm
            binding.txtDistance.text = item.distance?.let { formatDistance(it)}
            binding.txtQrBike.text = item.stationStatus.qrBikeCnt.toString()
            binding.txtElecBike.text = item.stationStatus.elecBikeCnt.toString()

            if (item.isFavorite) {
                if (binding.lottie.progress == 0.0f)
                    binding.lottie.playAnimation()
            } else {
                binding.lottie.progress = 0.0f
                binding.lottie.cancelAnimation()
            }

            binding.lottie.setOnClickListener {
                listener.setOnFavoriteClick(item, binding.lottie)
            }
        }
    }

    fun setOnItemClickListener(listener: (StationDto, Int) -> Unit) {
        onItemClickListener = listener
    }

    fun setItemList(list: List<StationDto>) {
        val diffCallBack = StationListDiffUtil(itemList, list)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)

        itemList.clear()
        itemList.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }
}