package com.beemer.seoulbike.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.beemer.seoulbike.databinding.RowNavigationviewMenuBinding
import com.beemer.seoulbike.model.dto.NavigationViewMenuDto
import com.beemer.seoulbike.view.diff.NavigationViewMenuDiffUtil

class NavigationViewMenuAdapter : RecyclerView.Adapter<NavigationViewMenuAdapter.ViewHolder>() {
    private var itemList = mutableListOf<NavigationViewMenuDto>()
    private var onItemClickListener: ((NavigationViewMenuDto, Int) -> Unit)? = null

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowNavigationviewMenuBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    inner class ViewHolder(private val binding: RowNavigationviewMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(itemList[position], position)
                }
            }
        }

        fun bind(item: NavigationViewMenuDto) {
            binding.imgIcon.setImageResource(item.icon)
            binding.txtTitle.text = item.title
        }
    }

    fun setOnItemClickListener(listener: (NavigationViewMenuDto, Int) -> Unit) {
        onItemClickListener = listener
    }

    fun setItemList(list: List<NavigationViewMenuDto>) {
        val diffCallBack = NavigationViewMenuDiffUtil(itemList, list)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)

        itemList.clear()
        itemList.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }
}