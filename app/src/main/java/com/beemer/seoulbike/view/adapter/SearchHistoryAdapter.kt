package com.beemer.seoulbike.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.beemer.seoulbike.databinding.RowEmptySearchHistoryBinding
import com.beemer.seoulbike.databinding.RowSearchHistoryBinding
import com.beemer.seoulbike.view.diff.StringListDiffUtil

class SearchHistoryAdapter(private val listener: OnDeleteClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface OnDeleteClickListener {
        fun setOnDeleteClick(item: String)
    }

    private var itemList = mutableListOf<String>()
    private var onItemClickListener: ((String, Int) -> Unit)? = null

    companion object {
        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_EMPTY = 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (itemList.isEmpty()) VIEW_TYPE_EMPTY else VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int = if (itemList.isEmpty()) 1 else itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_EMPTY) {
            val binding = RowEmptySearchHistoryBinding.inflate(inflater, parent, false)
            EmptyViewHolder(binding)
        } else {
            val binding = RowSearchHistoryBinding.inflate(inflater, parent, false)
            ViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder && itemList.isNotEmpty()) {
            holder.bind(itemList[position])
        }
    }

    inner class ViewHolder(private val binding: RowSearchHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(itemList[position], position)
                }
            }
        }

        fun bind(item: String) {
            binding.txtTitle.text = item

            binding.imgDelete.setOnClickListener {
                listener.setOnDeleteClick(item)
            }
        }
    }

    inner class EmptyViewHolder(binding: RowEmptySearchHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    fun setOnItemClickListener(listener: (String, Int) -> Unit) {
        onItemClickListener = listener
    }

    fun setItemList(list: List<String>) {
        val diffCallBack = StringListDiffUtil(itemList, list)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)

        itemList.clear()
        itemList.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }
}