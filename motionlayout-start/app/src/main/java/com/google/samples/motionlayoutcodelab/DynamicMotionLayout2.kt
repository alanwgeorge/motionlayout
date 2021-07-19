package com.google.samples.motionlayoutcodelab

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.constraintlayout.motion.widget.TransitionBuilder
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import java.util.*

class DynamicMotionLayout2 : MotionLayout {
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    lateinit var sceneTransition: MotionScene.Transition

    val scene = MotionScene(this)

    override fun onFinishInflate() {
        super.onFinishInflate()
        createBars(this, DEFAULT_BAR_COUNT)

        sceneTransition = createTransition(scene)

        scene.addTransition(sceneTransition)
        scene.setTransition(sceneTransition)
        setScene(scene)

    }

    private fun createTransition(scene: MotionScene): MotionScene.Transition {
        val startSetId = View.generateViewId()
        val startSet = ConstraintSet()
        startSet.clone(this)

        val endSetId = View.generateViewId()
        val endSet = ConstraintSet()
        endSet.clone(this)

        val transitionId = View.generateViewId()
        return TransitionBuilder.buildTransition(
            scene,
            transitionId,
            startSetId,
            startSet,
            endSetId,
            endSet
        )
    }

    // All the bars, id to view map. Alternatively we can use findViewById.
    private val bars: MutableMap<Int, TextView> = HashMap()

    // Currently state of the bars
    private var currentBars: MutableList<HistogramBarMetaData> = ArrayList()
    // Bars to which we're animating towards.
    private var nextBars: MutableList<HistogramBarMetaData> = ArrayList()

    // Default left margin in dp
    private var leftMarginDp = 0

    // Weight to use for the horizontal chain (of bars)
    private val weights: FloatArray = FloatArray(DEFAULT_BAR_COUNT)

    val barIds: List<Int> get() = currentBars.map { it.id }

    private fun createBars(layout: MotionLayout, columns: Int = DEFAULT_BAR_COUNT) {
        if (columns <= 1) {
            return
        }

        val marginInDp = fromDp(context, leftMarginDp)
        val size = fromDp(context, DEFAULT_HEIGHT_DP)

        val set = ConstraintSet()
        set.clone(layout)
        for (i in 0 until columns) {
            val bar = createBar(layout.context)
            val barColour = ContextCompat.getColor(context, R.color.design_default_color_primary_variant)

            // Initialize to the best knowledge (non-zero width/height so it's not gone)
            bar.text = i.toString()
            bar.background = ColorDrawable(barColour)
            val layoutParams = LayoutParams(size, size)
            layout.addView(bar, layoutParams)
            set.constrainHeight(bar.id, size)
            set.connect(
                bar.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM)
            set.setMargin(bar.id, ConstraintSet.END, marginInDp)
            weights[i] = 1f

            // Create the currentBars list to best mimic the initial state.
            currentBars.add(HistogramBarMetaData(bar.id, DEFAULT_HEIGHT_PERCENT, barColour, 0, bar.text.toString()))
            bars[bar.id] = bar
        }
        set.createHorizontalChain(
            ConstraintSet.PARENT_ID, ConstraintSet.LEFT,
            ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,
            barIds.toIntArray(), weights, ConstraintSet.CHAIN_SPREAD
        )
        set.applyTo(layout)
    }

    private fun createBar(context: Context): TextView {
        val bar = TextView(context)
        bar.id = ViewGroup.generateViewId()
        bar.gravity = Gravity.CENTER
        return bar
    }

    private fun add() {
        val rand = Random()
        var barColour = ContextCompat.getColor(context, DEFAULT_COLOUR_ID)
        val barDataList = ArrayList<HistogramBarMetaData>(DEFAULT_BAR_COUNT)
        var name = 0
        for (id in barIds) {
            val barData = HistogramBarMetaData(
                id,
                rand.nextFloat(),
                barColour,
                ColorHelper.getContrastColor(barColour),
                name.toString()
            )
            barColour = ColorHelper.getNextColor(barColour)
            barDataList.add(barData)
            name++
        }
        setData(barDataList)
    }

    fun setData(newData: List<HistogramBarMetaData>) {
        val startSet: ConstraintSet = getConstraintSet(sceneTransition.startConstraintSetId)
        updateConstraintSet(startSet, currentBars, false)
        val endSet: ConstraintSet = getConstraintSet(sceneTransition.endConstraintSetId)
        updateConstraintSet(endSet, newData)
        nextBars = ArrayList(newData)
    }

    private fun updateConstraintSet(
        set: ConstraintSet,
        list: List<HistogramBarMetaData>,
        useHeightFromMetaData: Boolean = true) {
        list.forEach { metadata ->
            val view = bars[metadata.id]!!
            val height: Int =
                if (useHeightFromMetaData) (metadata.height * height).toInt()
                else bars[metadata.id]!!.height
            view.setTextColor(metadata.barTextColour)
            view.text = metadata.name

            // These are attributes we wish to animate. We set them through ConstraintSet.
            set.constrainHeight(view.id, height)
            set.setColorValue(view.id, "BackgroundColor", metadata.barColour)
        }
    }

    fun animateWidget() {
        add()
        setTransition(sceneTransition.startConstraintSetId, sceneTransition.endConstraintSetId)
        transitionToEnd()
        currentBars = ArrayList(nextBars)
    }


    private fun fromDp(context: Context, inDp: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (inDp * scale).toInt()
    }

    companion object {
        const val DEFAULT_HEIGHT_DP = 100
        const val DEFAULT_HEIGHT_PERCENT = 0.9f
        const val DEFAULT_BAR_COUNT = 10
        private const val DEFAULT_COLOUR_ID = R.color.design_default_color_primary_dark
    }
}

data class HistogramBarMetaData(
    val id: Int,
    val height: Float,
    val barColour: Int,
    val barTextColour: Int,
    val name: String = id.toString()) : Parcelable {

    private constructor(source: Parcel): this(
        source.readInt(),
        source.readFloat(),
        source.readInt(),
        source.readInt(),
        source.readString()!!)

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeFloat(height)
        dest.writeInt(barColour)
        dest.writeInt(barTextColour)
        dest.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HistogramBarMetaData> {
        override fun createFromParcel(parcel: Parcel): HistogramBarMetaData {
            return HistogramBarMetaData(parcel)
        }

        override fun newArray(size: Int): Array<HistogramBarMetaData?> {
            return arrayOfNulls(size)
        }
    }
}

class ColorHelper {
    companion object {
        private const val GRADIENT = 20

        /**
         * Returns a different color somewhat gradient.
         */
        @JvmStatic
        fun getNextColor(@ColorInt color: Int): Int {
            return Color.argb(
                alpha(color),
                (red(color) + GRADIENT) % 256,
                (green(color) + GRADIENT) % 256,
                (blue(color) + GRADIENT) % 256)
        }
        /**
         * Returns a different color somewhat contrasting.
         */
        @JvmStatic
        fun getContrastColor(@ColorInt color: Int): Int {
            return Color.argb(
                alpha(color),
                255 - red(color),
                255 - green(color),
                255 - blue(color)
            )
        }

        private fun alpha(@ColorInt color: Int): Int {
            return color shr 24 and 0xff
        }

        private fun red(@ColorInt color: Int): Int {
            return color shr 16 and 0xff
        }

        private fun green(@ColorInt color: Int): Int {
            return color shr 8 and 0xff
        }

        private fun blue(@ColorInt color: Int): Int {
            return color and 0xff
        }
    }
}