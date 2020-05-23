package de.tpohrer.tpcolorselectionseekbar

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
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

    private var alphaBarBackgroundBitmap: Bitmap? = null
    private val alphaBarRect = RectF()
    private val alphaBarPaint = Paint()

    private val thumbPaint = Paint()
    private val thumbPaintBorder = Paint()
    private val thumbPath = Path()

    private var showAlphaBar = false
        set(value) {
            if (field != value) {
                field = value
                if (field) {
                    alphaBarBackgroundBitmap = BitmapFactory.decodeResource(
                        resources,
                        if (isVertical) R.drawable.background_alpha_vertical else R.drawable.background_alpha_horizontal
                    )
                }
            }
        }

    private var isVertical = false
    private var gravity = Gravity.TOP or Gravity.START

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
                currentColor = field.first()
            }
        }

    private var currentColor = colorBarColors.first()

    private var colorBarHeight = 60f
        set(value) {
            if (field != value) {
                field = value
                padding = field / 2
                thumbPos = padding
                alphaThumbPos = padding
            }
        }

    private var padding = colorBarHeight / 2

    //stores the x coordinate if bar is horizontal else stores the y coordinate
    private var thumbPos = padding

    //stores the x coordinate if bar is horizontal else stores the y coordinate
    private var alphaThumbPos = padding

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
            isVertical = a.getBoolean(R.styleable.TPColorSelectionSeekBar_isVertical, isVertical)
            showAlphaBar = a.getBoolean(R.styleable.TPColorSelectionSeekBar_showAlphaBar, showAlphaBar)
            gravity = a.getInt(R.styleable.TPColorSelectionSeekBar_android_gravity, gravity)

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

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()

        bundle.putParcelable(KEY_STATE_SUPER, super.onSaveInstanceState())
        bundle.putInt(KEY_STATE_COLOR, currentColor)

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var newState = state

        if (state is Bundle) {
            newState = state.getParcelable(KEY_STATE_SUPER)

            val newColor = state.getInt(KEY_STATE_COLOR)
            setColor(newColor)
        }

        super.onRestoreInstanceState(newState)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection)
        val horizontalGravity = absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK
        val verticalGravity = absoluteGravity and Gravity.VERTICAL_GRAVITY_MASK

        updateColorBarRect(w, h, horizontalGravity, verticalGravity)
        updateColorBarShaderPaint()

        updateAlphaBarRect()
        updateAlphaBarShaderPaint()
    }

    private fun updateColorBarRect(width: Int, height: Int, horizontalGravity: Int, verticalGravity: Int) {
        val top : Float
        val left: Float
        val bottom: Float
        val right: Float

        when (horizontalGravity) {

            Gravity.RIGHT -> {
                if (isVertical) {
                    left = if (showAlphaBar) {
                        width - 2 * colorBarHeight - 4 * padding
                    } else {
                        width - padding - colorBarHeight
                    }

                    right = left + colorBarHeight

                } else {
                    left = padding
                    right = width - padding
                }
            }

            Gravity.CENTER_HORIZONTAL, Gravity.CENTER -> {

                if (isVertical) {
                    left = if (showAlphaBar) {
                        width / 2 - colorBarHeight - padding - padding / 2
                    } else {
                        width / 2 - colorBarHeight / 2
                    }

                    right = left + colorBarHeight

                } else {
                    left = padding
                    right = width - padding
                }
            }

            else -> {
                left = padding
                right = if (isVertical) colorBarHeight + padding else width - padding
            }
        }

        when (verticalGravity) {

            Gravity.BOTTOM -> {
                if (!isVertical) {
                    top = if (showAlphaBar) {
                        height - 2 * colorBarHeight - 4 * padding
                    } else {
                        height - padding - colorBarHeight
                    }

                    bottom = top + colorBarHeight

                } else {
                    top = padding
                    bottom = height - padding
                }
            }

            Gravity.CENTER_VERTICAL, Gravity.CENTER -> {
                if (!isVertical) {
                    top = if (showAlphaBar) {
                        height / 2 - colorBarHeight - padding - padding / 2
                    } else {
                        height / 2 - colorBarHeight / 2
                    }

                    bottom = top + colorBarHeight

                } else {
                    top = padding
                    bottom = height - padding
                }
            }

            else -> {
                top = padding
                bottom = if (isVertical) height - padding else padding + colorBarHeight
            }
        }

        colorBarRect.top = top
        colorBarRect.left = left
        colorBarRect.right = right
        colorBarRect.bottom = bottom
    }

    private fun updateColorBarShaderPaint() {
        val x0 = colorBarRect.left
        val y0 = colorBarRect.top
        val x1 = colorBarRect.right
        val y1 = colorBarRect.bottom

        colorBarPaint.shader = LinearGradient(
            x0, y0, x1, y1,
            colorBarColors,
            null,
            Shader.TileMode.MIRROR
        )
    }

    private fun updateAlphaBarRect() {
        if (!showAlphaBar) return

        val top = if (isVertical) colorBarRect.top else colorBarRect.bottom + 3 * padding
        val bottom = if (isVertical) colorBarRect.bottom else top + colorBarHeight

        val left = if (isVertical) colorBarRect.right + 3 * padding else colorBarRect.left
        val right = if (isVertical) left + colorBarHeight else colorBarRect.right

        alphaBarRect.top = top
        alphaBarRect.left = left
        alphaBarRect.right = right
        alphaBarRect.bottom = bottom
    }

    private fun updateAlphaBarShaderPaint() {
        if (!showAlphaBar) return

        val rgb = getRGBFromColor(currentColor)

        val r = rgb.first
        val g = rgb.second
        val b = rgb.third

        val x0 = alphaBarRect.left
        val y0 = alphaBarRect.top
        val x1 = alphaBarRect.right
        val y1 = alphaBarRect.bottom

        alphaBarPaint.shader = LinearGradient(
            x0, y0, x1, y1,
            intArrayOf(
                Color.argb(255, r, g, b),
                Color.argb(0, r, g, b)
            ),
            null,
            Shader.TileMode.MIRROR
        )
    }

    private fun getRGBFromColor(color: Int): Triple<Int, Int, Int> {
        val r = color shr 16 and 0xff
        val g = color shr 8 and 0xff
        val b = color and 0xff

        return Triple(r, g, b)
    }

    private fun getAlphaFromColor(color: Int): Int {
        return color shr 24 and 0xff
    }

    private var isMoveActionForColorBar: Boolean? = null

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)

                val xPos = event.x
                val yPos = event.y

                isMoveActionForColorBar = when {
                    colorBarRect.contains(xPos, yPos) -> true
                    alphaBarRect.contains(xPos, yPos) -> false
                    else -> null
                }

                updateThumbPosition(if (isVertical) yPos else xPos)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                parent.requestDisallowInterceptTouchEvent(true)

                val xPos = event.x
                val yPos = event.y

                updateThumbPosition(if (isVertical) yPos else xPos)
                return true
            }
            else -> isMoveActionForColorBar = null
        }

        return super.onTouchEvent(event)
    }

    private fun updateThumbPosition(pos: Float) {
        var newPos = pos

        if (newPos < padding) newPos = padding

        if (isVertical) {
            if (newPos > height - padding) newPos = height - padding
        } else {
            if (newPos > width - padding) newPos = width - padding
        }

        if (isMoveActionForColorBar == true) {
            thumbPos = newPos
        } else if (isMoveActionForColorBar == false) {
            alphaThumbPos = newPos
        }

        currentColor = calculateColor(thumbPos, alphaThumbPos)

        if (isMoveActionForColorBar == true) {
            updateAlphaBarShaderPaint()
        }

        selectedColorChangedListener?.onSelectedColorChanged(currentColor, id)

        invalidate()
    }

    private fun calculateColor(posColor: Float, posAlpha: Float): Int {

        val posAdjusted = posColor - padding
        val percentage = if (isVertical) posAdjusted / colorBarRect.height() else posAdjusted / colorBarRect.width()

        var colorFirst = colorBarColors.last()
        var colorSecond = colorBarColors.last()
        var colorPosition = colorBarColors.size

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
        var alpha = 255

        if (showAlphaBar) {
            val posAlphaAdjusted = posAlpha - padding
            val percentageAlpha = 1 - if (isVertical) posAlphaAdjusted / alphaBarRect.height() else posAlphaAdjusted / alphaBarRect.width()

            alpha = (alpha * percentageAlpha).toInt()
        }

        return Color.argb(alpha, redFinal, greenFinal, blueFinal)
    }

    private fun mixColor(c1: Int, c2: Int, percentageOfC1: Float, percentageOfC2: Float): Int {
        return (c1 * percentageOfC1 + c2 * percentageOfC2).toInt()
    }

    private fun setFinalColor(color: Int, pos: Float, posAlpha: Float, callListener: Boolean) {
        currentColor = color
        thumbPos = pos
        alphaThumbPos = posAlpha

        updateAlphaBarShaderPaint()

        if (callListener) {
            selectedColorChangedListener?.onSelectedColorChanged(currentColor, id)
        }

        invalidate()
    }

    private fun colorDifference(c1: Int, c2: Int): Int {
        return abs(Color.red(c1) - Color.red(c2)) + abs(Color.green(c1) - Color.green(c2)) + abs(Color.blue(c1) - Color.blue(c2))
    }

    override fun onDraw(canvas: Canvas?) {
        drawBars(canvas)
        drawThumbnails(canvas)
    }

    private fun drawBars(canvas: Canvas?) {
        canvas?.drawRoundRect(colorBarRect, colorBarCornerRadius, colorBarCornerRadius, colorBarPaint)
        canvas?.drawRoundRect(colorBarRect, colorBarCornerRadius, colorBarCornerRadius, colorBarPaintBorder)

        if (showAlphaBar) {
            alphaBarBackgroundBitmap?.let {
                if (colorBarCornerRadius > 0f) {
                    val bm = getCroppedBitmap(it, alphaBarRect, colorBarCornerRadius)

                    canvas?.drawBitmap(bm, 0f, 0f, null)
                } else {
                    canvas?.drawBitmap(it, null, alphaBarRect, null)
                }
            }

            canvas?.drawRoundRect(alphaBarRect, colorBarCornerRadius, colorBarCornerRadius, alphaBarPaint)
            canvas?.drawRoundRect(alphaBarRect, colorBarCornerRadius, colorBarCornerRadius, colorBarPaintBorder)
        }
    }

    private fun getCroppedBitmap(src: Bitmap, rect: RectF, radius: Float): Bitmap {
        val output = Bitmap.createBitmap(
            src.width,
            src.height, Bitmap.Config.ARGB_8888
        )

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val canvas = Canvas(output)
        canvas.drawRoundRect(rect, radius, radius, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        canvas.drawBitmap(src, null, rect, paint)

        return output
    }

    private fun drawThumbnails(canvas: Canvas?) {
        thumbPath.reset()

        setPathForColorBarThumb()
        setPathForAlphaBarThumb()

        canvas?.drawPath(thumbPath, thumbPaint)
        canvas?.drawPath(thumbPath, thumbPaintBorder)
    }

    private fun setPathForColorBarThumb() {
        if (isVertical) {
            thumbPath.moveTo(colorBarRect.centerX() - colorBarRect.width() * 0.25f, thumbPos)
            thumbPath.lineTo(colorBarRect.centerX() - colorBarRect.width() * 0.9f, thumbPos + colorBarRect.width() * 0.4f)
            thumbPath.lineTo(colorBarRect.centerX() - colorBarRect.width() * 0.9f, thumbPos - colorBarRect.width() * 0.4f)
            thumbPath.close()

            thumbPath.moveTo(colorBarRect.centerX() + colorBarRect.width() * 0.25f, thumbPos)
            thumbPath.lineTo(colorBarRect.centerX() + colorBarRect.width() * 0.9f, thumbPos + colorBarRect.width() * 0.4f)
            thumbPath.lineTo(colorBarRect.centerX() + colorBarRect.width() * 0.9f, thumbPos - colorBarRect.width() * 0.4f)
            thumbPath.close()
        } else {
            thumbPath.moveTo(thumbPos, colorBarRect.centerY() - colorBarRect.height() * 0.25f)
            thumbPath.lineTo(thumbPos + colorBarRect.height() * 0.4f, colorBarRect.centerY() - colorBarRect.height() * 0.9f)
            thumbPath.lineTo(thumbPos - colorBarRect.height() * 0.4f, colorBarRect.centerY() - colorBarRect.height() * 0.9f)
            thumbPath.close()

            thumbPath.moveTo(thumbPos, colorBarRect.centerY() + colorBarRect.height() * 0.25f)
            thumbPath.lineTo(thumbPos + colorBarRect.height() * 0.4f, colorBarRect.centerY() + colorBarRect.height() * 0.9f)
            thumbPath.lineTo(thumbPos - colorBarRect.height() * 0.4f, colorBarRect.centerY() + colorBarRect.height() * 0.9f)
            thumbPath.close()
        }
    }

    private fun setPathForAlphaBarThumb() {
        if (!showAlphaBar) return

        if (isVertical) {
            thumbPath.moveTo(alphaBarRect.centerX() - alphaBarRect.width() * 0.25f, alphaThumbPos)
            thumbPath.lineTo(alphaBarRect.centerX() - alphaBarRect.width() * 0.9f, alphaThumbPos + alphaBarRect.width() * 0.4f)
            thumbPath.lineTo(alphaBarRect.centerX() - alphaBarRect.width() * 0.9f, alphaThumbPos - alphaBarRect.width() * 0.4f)
            thumbPath.close()

            thumbPath.moveTo(alphaBarRect.centerX() + alphaBarRect.width() * 0.25f, alphaThumbPos)
            thumbPath.lineTo(alphaBarRect.centerX() + alphaBarRect.width() * 0.9f, alphaThumbPos + alphaBarRect.width() * 0.4f)
            thumbPath.lineTo(alphaBarRect.centerX() + alphaBarRect.width() * 0.9f, alphaThumbPos - alphaBarRect.width() * 0.4f)
            thumbPath.close()
        } else {
            thumbPath.moveTo(alphaThumbPos, alphaBarRect.centerY() - alphaBarRect.height() * 0.25f)
            thumbPath.lineTo(alphaThumbPos + alphaBarRect.height() * 0.4f, alphaBarRect.centerY() - alphaBarRect.height() * 0.9f)
            thumbPath.lineTo(alphaThumbPos - alphaBarRect.height() * 0.4f, alphaBarRect.centerY() - alphaBarRect.height() * 0.9f)
            thumbPath.close()

            thumbPath.moveTo(alphaThumbPos, alphaBarRect.centerY() + alphaBarRect.height() * 0.25f)
            thumbPath.lineTo(alphaThumbPos + alphaBarRect.height() * 0.4f, alphaBarRect.centerY() + alphaBarRect.height() * 0.9f)
            thumbPath.lineTo(alphaThumbPos - alphaBarRect.height() * 0.4f, alphaBarRect.centerY() + alphaBarRect.height() * 0.9f)
            thumbPath.close()
        }
    }

    private fun getColorsById(id: Int): IntArray {
        val a = resources.obtainTypedArray(id)
        val colors = IntArray(a.length())

        for (i in colors.indices) {
            val color = a.getColor(i, Color.BLACK)
            val rgb = getRGBFromColor(color)

            colors[i] = Color.argb(255, rgb.first, rgb.second, rgb.third)
        }

        a.recycle()
        return colors
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isVertical) {
            val requiredWidth = if (showAlphaBar) {
                resolveSize((2 * colorBarHeight + 5 * padding).toInt(), widthMeasureSpec)
            } else {
                resolveSize((colorBarHeight + 2 * padding).toInt(), widthMeasureSpec)
            }

            setMeasuredDimension(requiredWidth, heightMeasureSpec)
        } else {
            val requiredHeight = if (showAlphaBar) {
                resolveSize((2 * colorBarHeight + 5 * padding).toInt(), heightMeasureSpec)
            } else {
                resolveSize((colorBarHeight + 2 * padding).toInt(), heightMeasureSpec)
            }

            setMeasuredDimension(widthMeasureSpec, requiredHeight)
        }
    }

    /**
     * Returns the currently selected color
     *
     * @return the currently selected color
     */
    fun getCurrentColor() = currentColor

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
            val end = if (isVertical) {
                (padding + colorBarRect.height()).toInt()
            } else {
                (padding + colorBarRect.width()).toInt()
            }

            var smallestDifference = Int.MAX_VALUE
            var smallestDifferencePos = Int.MAX_VALUE
            var smallestDifferenceColor = Int.MAX_VALUE

            var posAlpha = alphaThumbPos
            if (showAlphaBar) {
                val alphaPositionPercentage = 1 - getAlphaFromColor(newColor) / 255f
                posAlpha = if (isVertical) {
                    alphaPositionPercentage * alphaBarRect.height() + start
                } else {
                    alphaPositionPercentage * alphaBarRect.width() + start
                }
            }

            for (i in start..end) {
                val color = calculateColor(i.toFloat(), posAlpha)
                val difference = colorDifference(color, newColor)

                if (difference == 0) {
                    setFinalColor(newColor, i.toFloat(), posAlpha, callListener)
                    return@post
                } else if (difference < smallestDifference) {
                    smallestDifference = difference
                    smallestDifferencePos = i
                    smallestDifferenceColor = color
                }
            }

            setFinalColor(smallestDifferenceColor, smallestDifferencePos.toFloat(), posAlpha, callListener)
        }
    }

    /**
     * Add a new listener that should get informed about a color change
     *
     * @param listener the listener that should get informed about a color change
     */
    fun setColorSelectionChangedListener(listener: ISelectedColorChangedListener?) {
        selectedColorChangedListener = listener
    }

    fun cleanUp() {
        alphaBarBackgroundBitmap?.recycle()
        alphaBarBackgroundBitmap = null

        selectedColorChangedListener = null
    }

    interface ISelectedColorChangedListener {
        fun onSelectedColorChanged(color: Int, viewId: Int)
    }

    companion object {
        const val KEY_STATE_SUPER = "stateSuper"
        const val KEY_STATE_COLOR = "stateColor"
    }
}