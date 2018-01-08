package cn.edu.buaa.baomingkun.agc

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import java.util.*

/**
 * TODO: document your custom view class.
 */
class ChartView : View {
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private var values: ArrayList<Int> = arrayListOf(0, 100, 200, 300, 400, 500, 4096)
    private var paint:Paint = Paint()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawLine(values,canvas,paint)

    }

    fun normalize(raw: Int, bound: Float): Float {
        return raw.toFloat() / 4096 * bound
    }

    /*
    fun changeValues() {
        addValues(values)
        invalidate()
    }
    */

    /*
    fun addValues(values:ArrayList<Int>) {
        if(values.size<10) {
            values.add(rand.nextInt(4096))
        }
        else {
            values.removeAt(0)
            values.add(rand.nextInt(4096))
        }
    }
    */

    fun addValue(v: Int){
        if(values.size >= 100) {
            values.removeAt(0)
        }
        values.add(v)
        invalidate()
    }

    fun drawLine(values:ArrayList<Int>,canvas:Canvas,paint: Paint) {
        val vWidth = width.toFloat()
        val vHeight = height.toFloat()
        val step = vWidth / (values.size)-1
        var height = normalize(values[0], vHeight)
        for (i in values.indices) {
            val nHeight = normalize(values[i], vHeight)
            canvas.drawLine(
                    step * i ,
                    height,
                    step * (i+1),
                    nHeight,
                    paint
            )
            height = nHeight
        }
    }
}
