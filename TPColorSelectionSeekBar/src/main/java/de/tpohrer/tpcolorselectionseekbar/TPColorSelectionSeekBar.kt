package de.tpohrer.tpcolorselectionseekbar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * Created by Tim Pohrer on 2020-03-07.
 */

class TPColorSelectionSeekBar @JvmOverloads constructor(ctx: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(ctx, attributeSet, defStyleAttr) {

    private val colorBarRect = RectF()
    private val colorBarPaint = Paint()
    private val colorBarPaintBorder = Paint()

    private val thumbPaint = Paint()
    private val thumbPaintBorder = Paint()
    private val thumbPath = Path()

    private var colorBarColors = intArrayOf(
        Color.parseColor("#000000"),
        Color.parseColor("#FF0000"),
        Color.parseColor("#FFFF00"),
        Color.parseColor("#00FF00"),
        Color.parseColor("#00FFFF"),
        Color.parseColor("#0000FF"),
        Color.parseColor("#FF00FF"),
        Color.parseColor("#FFFFFF")
    )
        set(value) {
            if (!field.contentEquals(value)) {
                field = value
                currentColor = value.first()
            }
        }

    private var currentColor = colorBarColors.first()

    private var colorBarHeight = 60f
        set(value) {
            if (field != value) {
                field = value
                padding = value / 2
                thumbXPos = padding
            }
        }

    private var padding = colorBarHeight / 2
    private var thumbXPos = padding

    private var colorBarCornerRadius = 0f
    private var selectedColorChangedListener: ISelectedColorChangedListener? = null

    init {
        var thumbFillColor = Color.parseColor("#FF0000")
        var colorBarBorderColor = Color.parseColor("#000000")
        var thumbBorderColor = Color.parseColor("#000000")

        attributeSet?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.TPColorSelectionSeekBar)

            thumbFillColor = a.getColor(R.styleable.TPColorSelectionSeekBar_thumbFillColor, thumbFillColor)
            thumbBorderColor = a.getColor(R.styleable.TPColorSelectionSeekBar_thumbBorderColor, thumbBorderColor)
            colorBarBorderColor = a.getColor(R.styleable.TPColorSelectionSeekBar_colorBarBorderColor, colorBarBorderColor)

            colorBarCornerRadius = a.getDimension(R.styleable.TPColorSelectionSeekBar_colorBarCornerRadius, colorBarCornerRadius)
            colorBarHeight = a.getDimension(R.styleable.TPColorSelectionSeekBar_colorBarHeight, colorBarHeight)

            val colorsArrayId = a.getResourceId(R.styleable.TPColorSelectionSeekBar_colorBarColors, 0)
            if (colorsArrayId != 0) {
                colorBarColors = getColorsById(colorsArrayId)
            }

            a.recycle()
        }

        colorBarPaintBorder.style = Paint.Style.STROKE
        colorBarPaintBorder.strokeWidth = 4f
        colorBarPaintBorder.color = colorBarBorderColor

        thumbPaint.style = Paint.Style.FILL
        thumbPaint.color = thumbFillColor

        thumbPaintBorder.style = Paint.Style.STROKE
        thumbPaintBorder.strokeWidth = 4f
        thumbPaintBorder.color = thumbBorderColor
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        colorBarPaint.shader = LinearGradient(
            padding, 0f, w - padding, 0f,
            colorBarColors,
            null,
            Shader.TileMode.MIRROR
        )

        colorBarRect.left = padding
        colorBarRect.right = w - padding
        colorBarRect.top = h / 2 - colorBarHeight / 2
        colorBarRect.bottom = h / 2 + colorBarHeight / 2
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event?.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {

                var newXPos = event.x
                if (newXPos < padding) newXPos = padding
                if (newXPos > width - padding) newXPos = width - padding

                thumbXPos = newXPos

                currentColor = calculateColor(thumbXPos)

                selectedColorChangedListener?.onSelectedColorChanged(currentColor, id)

                invalidate()

                return true
            }
        }

        return super.onTouchEvent(event)
    }

    private fun calculateColor(xPos: Float): Int {
        val xPosAdjusted = xPos - padding
        val percentage = xPosAdjusted / colorBarRect.width()

        if (percentage <= 0.0) return colorBarColors.first()
        if (percentage >= 1.0) return colorBarColors.last()

        var colorFirst = -1
        var colorSecond = -1
        var colorPosition = -1

        for (i in 1 until colorBarColors.size) {
            if (i * (1f / (colorBarColors.size - 1)) > percentage) {
                colorFirst = colorBarColors[i - 1]
                colorSecond = colorBarColors[i]
                colorPosition = i - 1
                break
            }
        }

        val percentageOfSecondColor = (percentage * (colorBarColors.size - 1)) - colorPosition
        val percentageOfFirstColor = 1 - percentageOfSecondColor

        val redFinal = mixColor(Color.red(colorFirst), Color.red(colorSecond), percentageOfFirstColor, percentageOfSecondColor)
        val greenFinal = mixColor(Color.green(colorFirst), Color.green(colorSecond), percentageOfFirstColor, percentageOfSecondColor)
        val blueFinal = mixColor(Color.blue(colorFirst), Color.blue(colorSecond), percentageOfFirstColor, percentageOfSecondColor)

        return Color.rgb(redFinal, greenFinal, blueFinal)
    }

    private fun mixColor(c1: Int, c2: Int, percentageOfC1: Float, percentageOfC2: Float): Int {
        return (c1 * percentageOfC1 + c2 * percentageOfC2).toInt()
    }

    /**
     * Returns the currently selected color
     *
     * @return the currently selected color
     */
    fun getCurrentColor(): Int = currentColor

    /**
     * Sets the new color and updates the UI.
     * If the specified color couldn't be found, the "closest" color will be selected
     *
     * @param newColor the new color to be set
     * @param callListener true if any present listener should be informed about the color change
     *
     * */
    fun setColor(newColor: Int, callListener: Boolean = true) {
        post {
            val start = padding.toInt()
            val end = (padding + colorBarRect.width()).toInt()

            var smallestDifference = Int.MAX_VALUE
            var smallestDifferenceXPos = Int.MAX_VALUE
            var smallestDifferenceColor = Int.MAX_VALUE

            for (i in start..end) {
                val color = calculateColor(i.toFloat())
                val difference = colorDifference(color, newColor)

                if (difference == 0) {
                    setFinalColor(newColor, i.toFloat(), callListener)
                    return@post
                } else if (difference < smallestDifference) {
                    smallestDifference = difference
                    smallestDifferenceXPos = i
                    smallestDifferenceColor = color
                }
            }

            setFinalColor(smallestDifferenceColor, smallestDifferenceXPos.toFloat(), callListener)
        }
    }

    private fun setFinalColor(color: Int, xPos: Float, callListener: Boolean) {
        currentColor = color
        thumbXPos = xPos

        if (callListener) selectedColorChangedListener?.onSelectedColorChanged(currentColor, id)

        invalidate()
    }

    private fun colorDifference(c1: Int, c2: Int): Int {
        return abs(Color.red(c1) - Color.red(c2)) + abs(Color.green(c1) - Color.green(c2)) + abs(Color.blue(c1) - Color.blue(c2))
    }

    /**
     * Add a new listener that should get informed about a color change
     *
     * @param listener the listener that should get informed about a color change
     */
    fun setColorSelectionChangedListener(listener: ISelectedColorChangedListener?) {
        selectedColorChangedListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val requiredHeight = resolveSize((colorBarHeight + 2 * padding).toInt(), heightMeasureSpec)

        setMeasuredDimension(widthMeasureSpec, requiredHeight)
    }

    override fun onDraw(canvas: Canvas?) {

        canvas?.drawRoundRect(colorBarRect, colorBarCornerRadius, colorBarCornerRadius, colorBarPaint)
        canvas?.drawRoundRect(colorBarRect, colorBarCornerRadius, colorBarCornerRadius, colorBarPaintBorder)

        thumbPath.reset()
        thumbPath.moveTo(thumbXPos, colorBarRect.centerY() - colorBarRect.height() * 0.25f)
        thumbPath.lineTo(thumbXPos + colorBarRect.height() * 0.4f, colorBarRect.centerY() - colorBarRect.height() * 0.9f)
        thumbPath.lineTo(thumbXPos - colorBarRect.height() * 0.4f, colorBarRect.centerY() - colorBarRect.height() * 0.9f)
        thumbPath.close()

        thumbPath.moveTo(thumbXPos, colorBarRect.centerY() + colorBarRect.height() * 0.25f)
        thumbPath.lineTo(thumbXPos + colorBarRect.height() * 0.4f, colorBarRect.centerY() + colorBarRect.height() * 0.9f)
        thumbPath.lineTo(thumbXPos - colorBarRect.height() * 0.4f, colorBarRect.centerY() + colorBarRect.height() * 0.9f)
        thumbPath.close()

        canvas?.drawPath(thumbPath, thumbPaint)
        canvas?.drawPath(thumbPath, thumbPaintBorder)
    }

    private fun getColorsById(id: Int): IntArray {
        val a = resources.obtainTypedArray(id)
        val colors = IntArray(a.length())

        for (i in colors.indices) {
            colors[i] = a.getColor(i, Color.BLACK)
        }

        a.recycle()
        return colors
    }

    interface ISelectedColorChangedListener {
        fun onSelectedColorChanged(color: Int, viewId : Int)
    }
}

 