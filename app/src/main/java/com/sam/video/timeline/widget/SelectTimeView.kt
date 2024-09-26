package com.sam.video.timeline.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.sam.video.timeline.R
import com.sam.video.util.dp2px
import java.util.concurrent.TimeUnit
import kotlin.math.ceil


/**
 * 实现时间轴功能
 * 该功能描述如下：
 * 未选择某个视频片段，则只展示当前视频（屏幕中间那个视频）的起始时间
 * 选中视频后，展示起始时间以及中间时间轴的时间
 * 起始时间是指在整个视频序列里的时间，而不是单个视频的时间
 */
class SelectTimeView @JvmOverloads constructor(
    context: Context, paramAttributeSet: AttributeSet? = null,
    paramInt: Int = 0
) : View(context, paramAttributeSet, paramInt),
    TimeLineBaseValue.TimeLineBaseView {

    /** 选择的时间范围 */
    var startTime = 0L
    var endTime = 0L
    private var currentTime = 0L

    var isNeedShowCurrentTime: Boolean = false
    private val textMarginRight = context.dp2px(2f) //选区时间的右间距

    private val paintBg = Paint(Paint.ANTI_ALIAS_FLAG)
    private val unSelectBgColor = ContextCompat.getColor(context, R.color.video_blue_50) //未选择的区域色
//    private val unSelectBgColor = ContextCompat.getColor(context, R.color.transparent) //未选择的区域色，方便调试
    private val unSelectBgPaddingTop = context.dp2px(0f)
    private val textColor = Color.parseColor("#66FFFFFF")
    init {
        paintBg.style = Paint.Style.FILL
        paintBg.textSize = context.dp2px(9f)
        paintBg.textAlign = Paint.Align.RIGHT
    }

    /** 基础数据 */
    override var timeLineValue: TimeLineBaseValue? = null
        set(value) {
            field = value
            invalidate()
        }

    private var startTimeX = 0f
    private var endTimeX = 0f

    private fun getStartTime(): String {
        return formatMillisToTimeString(startTime)
    }

    private fun getEndTime(): String {
        return formatMillisToTimeString(endTime)
    }

    private fun formatMillisToTimeString(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val timeLineValue = timeLineValue ?: return
        canvas.save()
        canvas.clipRect(paddingLeft, 0, width, height)
        val timeX = width / 2 //当前时间，中间那个标记线的位置

        val timeInPx = timeLineValue.time2px(timeLineValue.time)
        val startTimeInPx = timeLineValue.time2px(startTime)
        startTimeX = ceil(timeX + startTimeInPx - timeInPx) //上取整损失一些精度，避免开始的游标和中间线对不上
        val endTimeInPx = timeLineValue.time2px(endTime)
        endTimeX = timeX + endTimeInPx - timeInPx

        //左
        paintBg.color = unSelectBgColor
        canvas.drawRect(0f, unSelectBgPaddingTop, startTimeX, height.toFloat(), paintBg)
        // 右
        paintBg.color = unSelectBgColor
        canvas.drawRect(endTimeX, unSelectBgPaddingTop, width.toFloat(), height.toFloat(), paintBg)


        // 获取文字的度量信息
        val fontMetrics = paintBg.fontMetrics
        val textHeight = fontMetrics.descent - fontMetrics.ascent
        val verticalCenter = (height / 2) - (fontMetrics.ascent + fontMetrics.descent) / 2
        paintBg.color = textColor

        //开始时间
        val startTimeText = getStartTime()
        val startTextWidth = paintBg.measureText(startTimeText)
        if (textMarginRight + startTextWidth < endTimeX - startTimeX) {
            canvas.drawText(startTimeText,startTimeX + startTextWidth / 2, verticalCenter, paintBg)
        }

        if (isNeedShowCurrentTime) {
            currentTime = timeLineValue.px2time((timeX - startTimeX)) + startTime
            //当前时间
            val currentTimeText = formatMillisToTimeString(currentTime)
            val currentTextWidth = paintBg.measureText(currentTimeText)
            if (textMarginRight + currentTextWidth < endTimeX - startTimeX) {
                canvas.drawText(currentTimeText,timeX + currentTextWidth / 2, verticalCenter, paintBg)
            }
        }


        //结束时间
        val endTimeText = getEndTime()
        val endTextWidth = paintBg.measureText(endTimeText)
        if (textMarginRight + endTextWidth < endTimeX - startTimeX) {
            canvas.drawText(endTimeText,endTimeX + endTextWidth / 2, verticalCenter, paintBg)
        }

        canvas.restore()
    }

    override fun scaleChange() {
        invalidate()
    }
    override fun updateTime() {
        invalidate()
    }

}