package com.beemer.seoulbike.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.beemer.seoulbike.R
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
            binding.layout.background = if (item.isSelected) {
                AppCompatResources.getDrawable(
                    binding.root.context,
                    R.drawable.rectangle_primary_alpha20_right_rounded_28dp
                )
            } else {
                null
            }

            binding.imgIcon.apply {
                setImageResource(item.icon)
                imageTintList = if (item.isSelected) {
                    AppCompatResources.getColorStateList(binding.root.context, R.color.primary)
                } else {
                    AppCompatResources.getColorStateList(binding.root.context, R.color.dark_gray)
                }
            }

            binding.txtTitle.apply {
                text = item.title
                setTextColor(if (item.isSelected) {
                    AppCompatResources.getColorStateList(binding.root.context, R.color.primary)
                } else {
                    AppCompatResources.getColorStateList(binding.root.context, R.color.dark_gray)
                })
            }
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

    fun setItemSelected(position: Int) {
        val previousSelectedPosition = itemList.indexOfFirst { it.isSelected }
        if (previousSelectedPosition != -1) {
            itemList[previousSelectedPosition].isSelected = false
            notifyItemChanged(previousSelectedPosition)
        }
        itemList[position].isSelected = true
        notifyItemChanged(position)
    }
}