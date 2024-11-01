package com.beemer.seoulbike.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.beemer.seoulbike.databinding.RowProgressBinding
import com.beemer.seoulbike.databinding.RowStationSearchBinding
import com.beemer.seoulbike.model.dto.StationDto
import com.beemer.seoulbike.view.diff.StationListDiffUtil
import com.beemer.seoulbike.view.utils.UnitConversion.formatDistance

class StationSearchAdapter(private val listener: OnFavoriteClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface OnFavoriteClickListener {
        fun setOnFavoriteClick(item: StationDto, lottie: LottieAnimationView)
    }

    private var itemList = mutableListOf<StationDto>()
    private var onItemClickListener: ((StationDto, Int) -> Unit)? = null
    private var isLoading = false

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    override fun getItemCount(): Int = if (isLoading) itemList.size + 1 else itemList.size

    override fun getItemViewType(position: Int): Int = if (isLoading && position == itemList.size) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val binding = RowStationSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(binding)
        } else {
            val binding = RowProgressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            LoadingViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(itemList[position])
        }
    }

    inner class ViewHolder(private val binding: RowStationSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(itemList[position], position)
                }
            }
        }

        fun bind(item: StationDto) {
            binding.txtStationNo.text = item.stationNo.replace("^0+".toRegex(), "")
            binding.txtStationNm.text = item.stationNm
            binding.txtAddress.text = item.stationDetails.addr1
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

    inner class LoadingViewHolder(binding: RowProgressBinding) : RecyclerView.ViewHolder(binding.root)

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

    fun getItemList(): List<StationDto> {
        return itemList
    }

    fun showProgress() {
        isLoading = true
        notifyItemInserted(itemList.size)
    }

    fun hideProgress() {
        isLoading = false
        notifyItemRemoved(itemList.size)
    }
}