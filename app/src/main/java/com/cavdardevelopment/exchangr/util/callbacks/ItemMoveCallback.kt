package com.cavdardevelopment.exchangr.util.callbacks

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.cavdardevelopment.exchangr.R
import com.cavdardevelopment.exchangr.view.adapter.WatchListAdapter
import kotlin.math.abs

class ItemMoveCallback(private val adapter: ItemTouchHelperContract) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        return adapter.isRowLongPressDragEnabled()
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return adapter.isRowSwipeEnabled()
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        if (direction == ItemTouchHelper.LEFT) {
            adapter.onRowRemoved(viewHolder.adapterPosition)
        }
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder
    ): Int {
        val dragFlags: Int
        val swipeFlags: Int
        if (viewHolder is WatchListAdapter.WatchListViewHolder) {
            dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            swipeFlags = ItemTouchHelper.LEFT
        }
        else {
            dragFlags = 0
            swipeFlags = 0
        }
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        recyclerView.scrollToPosition(target.adapterPosition)
        adapter.onRowMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder != null) {
                adapter.onRowSelected(viewHolder)
            }
        }

        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        adapter.onRowClear(viewHolder)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView
            val swipeThreshold = itemView.width

            if (abs(dX) < swipeThreshold) {
                // Set the background color to red
                val paint = Paint()
                paint.color = Color.parseColor("#eb2724")
                c.drawRect(itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(), paint)

                // Get the trash icon drawable
                val trashIcon = ContextCompat.getDrawable(
                    recyclerView.context, R.drawable.ic_delete
                )

                // Calculate the position of the trash icon
                val iconWidth = trashIcon!!.intrinsicWidth / 1.3F
                val iconHeight = trashIcon.intrinsicHeight / 1.3F
                val iconMargin = (itemView.height - iconHeight) / 2

                val iconTop = itemView.top + (itemView.height - iconHeight) / 2
                val iconLeft = itemView.right - iconMargin - iconWidth
                val iconRight = itemView.right - iconMargin
                val iconBottom = iconTop + iconHeight

                // Set the trash icon's color to white
                trashIcon.setTint(Color.WHITE)

                // Draw the trash icon on the canvas
                trashIcon.setBounds(iconLeft.toInt(), iconTop.toInt(), iconRight.toInt(),
                    iconBottom.toInt()
                )
                trashIcon.draw(c)

            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    interface ItemTouchHelperContract {
        fun isRowLongPressDragEnabled(): Boolean
        fun isRowSwipeEnabled() : Boolean
        fun onRowMoved(fromPosition: Int, toPosition: Int)
        fun onRowSelected(viewHolder: ViewHolder)
        fun onRowClear(viewHolder: ViewHolder)
        fun onRowRemoved(position: Int)
    }
}
