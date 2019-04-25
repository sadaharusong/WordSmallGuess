package com.github.sadaharusong.wordsmallguess.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.github.sadaharusong.wordsmallguess.R

class FadeTransitionImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseTransitionLayout(context, attrs, defStyleAttr) {

    private var imageView1: ImageView? = null
    private var imageView2: ImageView? = null

    protected var currentPosition = -1
    protected var nextPosition = -1

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.scene)
        a.recycle()
    }

    override fun addViewWhenFinishInflate() {
        imageView1 = ImageView(context)
        val lp1 = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        imageView1!!.scaleType = ImageView.ScaleType.CENTER_CROP
        addView(imageView1, lp1)

        imageView2 = ImageView(context)
        val lp2 = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        imageView2!!.scaleType = ImageView.ScaleType.CENTER_CROP
        addView(imageView2, lp2)
    }


    override fun firstInit(url: String) {
        Glide.with(context).load(url).into(imageView1!!)
        currentPosition = 0
    }

    @SuppressLint("MissingSuperCall")
    override fun onAnimationEnd() {
        currentPosition = nextPosition
        val tmp = imageView1
        imageView1 = imageView2
        imageView2 = tmp
    }

    /**
     * rate从零到1
     */
    override fun duringAnimation(rate: Float) {
        imageView1!!.alpha = 1 - rate
        imageView2!!.alpha = rate
    }

    override fun saveNextPosition(position: Int, url: String) {
        this.nextPosition = position
        Glide.with(context).load(url).into(imageView2!!)
    }
}