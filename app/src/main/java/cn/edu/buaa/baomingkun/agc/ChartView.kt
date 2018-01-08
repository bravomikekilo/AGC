package cn.edu.buaa.baomingkun.agc

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*

/**
 * TODO: document your custom view class.
 */
class ChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    companion object {
        val QUEUE_DEPTH: Int = 100
    }

    private var values: Queue<Int> = ArrayDeque<Int>(QUEUE_DEPTH)
    private var paint:Paint = Paint()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawLine(values,canvas,paint)

    }

    private fun normalize(raw: Int, bound: Float): Float {
        return raw.toFloat() / 4096 * bound
    }

    fun addValue(v: Int){
        if(values.size >= QUEUE_DEPTH) {
            values.poll()
        }
        values.add(v)
        invalidate()
    }

    fun drawLine(values:Queue<Int>,canvas:Canvas,paint: Paint) {
        if(values.isEmpty()) return
        val vWidth = width.toFloat()
        val vHeight = height.toFloat()
        val step = vWidth / (values.size)-1
        var height = normalize(values.peek(), vHeight)
        for((i, v) in values.withIndex()){
            val nHeight = normalize(v, vHeight)
            canvas.drawLine(
                    step * i,
                    height,
                    step * (i + 1),
                    nHeight,
                    paint
            )
            height = nHeight
        }

    }
}
