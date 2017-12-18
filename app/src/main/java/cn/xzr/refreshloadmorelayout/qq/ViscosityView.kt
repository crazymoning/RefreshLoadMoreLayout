package cn.xzr.refreshloadmorelayout.qq

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * Created by xzr on 2017/11/28.
 */

class ViscosityView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(context, attrs, defStyleAttr){

    private var progress = 0f
    var circleWidth = 40f
    var path = Path()
    var mPaint = Paint()

    init {
        mPaint.isAntiAlias = true
        mPaint.color = Color.GRAY
        mPaint.style = Paint.Style.FILL_AND_STROKE

    }

    constructor(context: Context) : this(context,null,0)

    constructor(context: Context,attrs: AttributeSet?) : this(context,attrs,0)

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        path.reset()
        path.moveTo(width/2 - circleWidth*(1 - progress/200)/2, circleWidth/2+paddingTop)
        path.lineTo(width/2 + circleWidth*(1 - progress/200)/2, circleWidth/2+paddingTop)
        path.quadTo(
                width/2+15 - progress/100*15,
                circleWidth+paddingTop,
                width/2+circleWidth*(1 - progress/120)/2,
                circleWidth/2+progress/100*(height - circleWidth)+paddingTop)
        path.lineTo(
                width/2 - circleWidth*(1 - progress/120)/2,
                circleWidth/2+progress/100*(height - circleWidth)+paddingTop)
        path.quadTo(
                width/2-15 + progress/100*15,
                circleWidth+paddingTop,
                width/2 - circleWidth*(1 - progress/200)/2,
                circleWidth/2+paddingTop)
        path.close()
        canvas!!.drawPath(path,mPaint)

        canvas.drawCircle((width/2).toFloat(), circleWidth/2+paddingTop,
                circleWidth*(1 - progress/200)/2,mPaint)
        canvas.drawCircle((width/2).toFloat(),
                circleWidth/2+progress/100*(height - circleWidth)+paddingTop,
                circleWidth*(1 - progress/120)/2,mPaint)

    }

    fun setProgress(progress:Float){
        this.progress = progress
        invalidate()
    }


}
