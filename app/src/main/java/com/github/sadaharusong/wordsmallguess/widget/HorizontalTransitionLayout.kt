package com.github.sadaharusong.wordsmallguess.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import com.github.sadaharusong.wordsmallguess.R

class HorizontalTransitionLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseTransitionLayout(context, attrs, defStyleAttr) {

    private var textView1: TextView? = null
    private var textView2: TextView? = null

    protected var currentPosition = -1
    protected var nextPosition = -1

    private var leftMargin = 50
    private var textSize = 22f
    private var textColor = Color.BLACK
    private var leftDistance = 50
    private var rightDistance = 450

    init {

        val a = context.obtainStyledAttributes(attrs, R.styleable.scene)
        leftMargin = a.getDimensionPixelSize(R.styleable.scene_leftMargin, leftMargin)
        textSize = a.getFloat(R.styleable.scene_textSize, textSize)
        textColor = a.getColor(R.styleable.scene_textColor, textColor)
        leftDistance = a.getDimensionPixelSize(R.styleable.scene_leftDistance, leftDistance)
        rightDistance = a.getDimensionPixelSize(R.styleable.scene_rightDistance, rightDistance)
        a.recycle()
    }

    override fun addViewWhenFinishInflate() {
        textView1 = TextView(context)
        textView1!!.gravity = Gravity.CENTER_VERTICAL
        textView1!!.textSize = textSize
        textView1!!.setTextColor(textColor)
        val lp1 = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        lp1.setMargins(leftMargin, 0, 0, 0)
        addView(textView1, lp1)

        textView2 = TextView(context)
        textView2!!.gravity = Gravity.CENTER_VERTICAL
        textView2!!.textSize = textSize
        textView2!!.setTextColor(textColor)
        val lp2 = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        lp2.setMargins(leftMargin, 0, 0, 0)
        addView(textView2, lp2)
    }

    override fun firstInit(text: String) {
        this.textView1!!.text = text
        currentPosition = 0
    }

    @SuppressLint("MissingSuperCall")
    override fun onAnimationEnd() {
        currentPosition = nextPosition
        val tmp = textView1
        textView1 = textView2
        textView2 = tmp
    }

    /**
     * rate从零到1
     */
    override fun duringAnimation(rate: Float) {
        textView1!!.alpha = 1 - rate
        textView2!!.alpha = rate

        if (nextPosition > currentPosition) {
            textView1!!.offsetLeftAndRight((leftMargin.toFloat() - leftDistance * rate - textView1!!.left.toFloat()).toInt())
            textView2!!.offsetLeftAndRight((leftMargin + rightDistance * (1 - rate) - textView2!!.left).toInt())
        } else {
            textView1!!.offsetLeftAndRight((leftMargin + rightDistance * rate - textView1!!.left).toInt())
            textView2!!.offsetLeftAndRight((leftMargin * rate - textView2!!.left).toInt())
        }
    }

    override fun saveNextPosition(position: Int, text: String) {
        this.nextPosition = position
        this.textView2!!.text = text
        this.textView2!!.alpha = 0f
    }
}