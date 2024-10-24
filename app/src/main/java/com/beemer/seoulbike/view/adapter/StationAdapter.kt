package com.beemer.seoulbike.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.beemer.seoulbike.R
import com.beemer.seoulbike.databinding.RowNearStationBinding
import com.beemer.seoulbike.model.dto.NearbyStationListDto
import com.beemer.seoulbike.view.diff.StationListDiffUtil
import com.beemer.seoulbike.view.utils.UnitConversion.formatDistance

class StationAdapter : RecyclerView.Adapter<StationAdapter.ViewHolder>() {
    private var itemList = mutableListOf<NearbyStationListDto>()
    private var onItemClickListener: ((NearbyStationListDto, Int) -> Unit)? = null

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowNearStationBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    inner class ViewHolder(private val binding: RowNearStationBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(itemList[position], position)
                }
            }
        }

        fun bind(item: NearbyStationListDto) {
            binding.txtName.text = "${item.stationNo.replace("^0+".toRegex(), "")}. ${item.stationNm}"
            binding.txtAddress.text = item.stationDetails.addr1
            binding.txtDistance.text = item.distance?.let { formatDistance(it)}
            binding.txtParking.text = item.stationStatus.parkingCnt.toString()
            binding.txtRack.text = item.stationStatus.rackCnt.toString()

            binding.txtParking.setTextColor(
                if (item.stationStatus.parkingCnt == 0) {
                    ContextCompat.getColor(binding.root.context, R.color.red)
                } else {
                    ContextCompat.getColor(binding.root.context, R.color.colorPrimary)
                }
            )
        }
    }

    fun setOnItemClickListener(listener: (NearbyStationListDto, Int) -> Unit) {
        onItemClickListener = listener
    }

    fun setItemList(list: List<NearbyStationListDto>) {
        val diffCallBack = StationListDiffUtil(itemList, list)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)

        itemList.clear()
        itemList.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }
}