package com.google.samples.motionlayoutcodelab

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.constraintlayout.motion.widget.TransitionBuilder
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import java.util.*

class DynamicMotionLayout : MotionLayout {
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    lateinit var sceneTransition: MotionScene.Transition
    lateinit var items: List<TextView>

    val scene = MotionScene(this)

    override fun onFinishInflate() {
        super.onFinishInflate()
        createItems(this, 10)

        sceneTransition = createTransition(scene)

        scene.addTransition(sceneTransition)
        scene.setTransition(sceneTransition)
        setScene(scene)

    }

    private fun createTransition(scene: MotionScene): MotionScene.Transition {
        val startSetId = View.generateViewId()
        val startSet = ConstraintSet()
        startSet.clone(this)

//        items.forEachIndexed { index, textView ->
//            startSet.connect(textView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 30)
//            startSet.connect(textView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 30)
//            startSet.connect(textView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 30)
//        }

        val endSetId = View.generateViewId()
        val endSet = ConstraintSet()
        endSet.clone(this)

//        items.forEachIndexed { index, textView ->
//            if (index == 0) {
//                endSet.connect(textView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 30)
//            } else {
//                endSet.connect(textView.id, ConstraintSet.TOP, items[index - 1].id, ConstraintSet.BOTTOM, 30)
//            }
//            endSet.connect(textView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 30)
//            endSet.connect(textView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 30)
//        }

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

    private fun createItems(layout: MotionLayout, itemCount: Int) {
        items = (itemCount downTo 0).map { index ->
            createItem(Item("helloworld $index"), layout.context).also {
                layout.addView(it, 0, 100)
            }
        }

        val set = ConstraintSet().apply {
            clone(layout)
        }

        items.forEach { textView ->
            set.connect(textView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 30)
            set.connect(textView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 30)
            set.connect(textView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 30)
            set.constrainHeight(textView.id, 100)
        }

        set.applyTo(layout)
    }

    private fun createItem(item: Item, context: Context) = TextView(context).apply {
        id = ViewGroup.generateViewId()
        setBackgroundColor(Color.BLUE)
        gravity = Gravity.CENTER
        text = item.title
    }

    fun animateWidget() {
        val startSet = getConstraintSet(sceneTransition.startConstraintSetId)
        val endSet = getConstraintSet(sceneTransition.endConstraintSetId)

        items.forEachIndexed { index, textView ->
            startSet.constrainHeight(textView.id, 100)
            startSet.connect(textView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 30)
            startSet.connect(textView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 30)
            startSet.connect(textView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 30)

            endSet.constrainHeight(textView.id, 100)
            if (index == 0) {
                endSet.connect(textView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 30)
            } else {
                endSet.connect(textView.id, ConstraintSet.TOP, items[index - 1].id, ConstraintSet.BOTTOM, 30)
            }
            endSet.connect(textView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 30)
            endSet.connect(textView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 30)
        }

        setTransition(sceneTransition.startConstraintSetId, sceneTransition.endConstraintSetId)
        transitionToEnd()
    }

    private fun fromDp(context: Context, inDp: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (inDp * scale).toInt()
    }
}

data class Item(val title: String)
