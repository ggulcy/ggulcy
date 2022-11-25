package spamdetector.zani.util

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RevDecoration(private val dividerHeight:Int,
                    private val dividerColor:Int = android.graphics.Color.GRAY)
    : RecyclerView.ItemDecoration()
{
    private val paint = Paint()
    private val padding = 10

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        getDivider(c, parent, color=dividerColor)
    }
    private fun getDivider(c: Canvas, parent: RecyclerView, color:Int)
    {
        paint.color = color
        for(i in 0 until parent.childCount)
        {
            val child = parent.getChildAt(i)
            val param = child.layoutParams as RecyclerView.LayoutParams

            val dividerBottom = child.bottom + dividerHeight

            c.drawRect(
                child.left.toFloat(),
                child.bottom.toFloat(),
                child.right.toFloat(),
                child.bottom.toFloat(),
                paint
            )
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
//        outRect.bottom = dividerHeight
        super.getItemOffsets(outRect, view, parent, state)
        outRect.top = padding
        outRect.bottom = padding
        outRect.left = padding
        outRect.right = padding
    }
}