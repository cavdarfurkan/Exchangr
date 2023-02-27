package com.cavdardevelopment.exchangr.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.cavdardevelopment.exchangr.R
import com.cavdardevelopment.exchangr.databinding.WatchListHeaderBinding
import com.cavdardevelopment.exchangr.databinding.WatchListRowBinding
import com.cavdardevelopment.exchangr.model.database.entity.WatchListEntity
import com.cavdardevelopment.exchangr.util.callbacks.ItemMoveCallback
import java.util.Collections
import kotlin.math.abs

class WatchListAdapter(
    val reorderDatabase: (watchListModels: ArrayList<WatchListEntity>) -> Unit,
    val deleteFromDatabase: (watchListModel: WatchListEntity) -> Unit
) : RecyclerView.Adapter<ViewHolder>(), ItemMoveCallback.ItemTouchHelperContract {

    companion object {
        private const val ViewTypeHeader = 0
        private const val ViewTypeItem = 1
    }

    private var response: ArrayList<WatchListEntity> = ArrayList()

    class WatchListViewHolder(val binding: WatchListRowBinding) : ViewHolder(binding.root)
    class WatchListHeaderViewHolder(val binding: WatchListHeaderBinding) : ViewHolder(binding.root)

    fun setData(response: ArrayList<WatchListEntity>) {
        this.response = response
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == ViewTypeHeader) {
            WatchListHeaderViewHolder(WatchListHeaderBinding.inflate(inflater, parent, false))
        } else {
            WatchListViewHolder(WatchListRowBinding.inflate(inflater, parent, false))
        }
    }

    override fun getItemCount(): Int = this.response.size + 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is WatchListViewHolder) {
            val it = response[position - 1]

            holder.binding.currencyTextView.text = "${it.base}/${it.quote}"
            holder.binding.rateTextView.text = "${it.rate} ${it.quote}"

            val changeRate = it.changeRate
            val color = when {
                changeRate > 0 -> ContextCompat.getColor(holder.itemView.context, R.color.red)
                changeRate < 0 -> ContextCompat.getColor(holder.itemView.context, R.color.green)
                else -> holder.binding.changeTextView.currentTextColor
            }
            val changeText = when {
                changeRate > 0 -> "-${changeRate}"
                changeRate < 0 -> "+${abs(changeRate)}"
                else -> "${abs(changeRate)}"
            }

            holder.binding.changeTextView.apply {
                setTextColor(color)
                text = changeText
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            ViewTypeHeader
        }
        else {
            ViewTypeItem
        }
    }

    override fun isRowLongPressDragEnabled(): Boolean = response.size > 1
    override fun isRowSwipeEnabled(): Boolean = response.isNotEmpty()

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (!isRowLongPressDragEnabled() || fromPosition == 0 || toPosition == 0) {
            return
        }
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(response, i-1, i)
            }
        }
        else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(response, i-1, i-2)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(viewHolder: ViewHolder) {
        if (!isRowLongPressDragEnabled() || viewHolder is WatchListHeaderViewHolder) {
            return
        }
        val watchListViewHolder = viewHolder as WatchListViewHolder
        watchListViewHolder.binding.linearLayout.animate()
            .scaleX(watchListViewHolder.binding.linearLayout.scaleX+0.02F)
            .scaleY(watchListViewHolder.binding.linearLayout.scaleY+0.02F)
            .start()
    }

    override fun onRowClear(viewHolder: ViewHolder) {
        if (!isRowLongPressDragEnabled() || viewHolder is WatchListHeaderViewHolder) {
            return
        }
        val watchListViewHolder = viewHolder as WatchListViewHolder
        watchListViewHolder.binding.linearLayout.animate()
            .scaleX(watchListViewHolder.binding.linearLayout.scaleX-0.02F)
            .scaleY(watchListViewHolder.binding.linearLayout.scaleY-0.02F)
            .start()

        reorderDatabase(response)
    }

    override fun onRowRemoved(position: Int) {
        deleteFromDatabase(response[position-1])
        response.removeAt(position-1)
        notifyItemRemoved(position)
    }
}
