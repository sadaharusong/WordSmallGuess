package com.github.sadaharusong.wordsmallguess.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * @author sadaharusong
 * @date 2019/4/27.
 * GitHub：https://github.com/sadaharusong
 */
abstract class BaseTransitionLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        addViewWhenFinishInflate()
    }

    abstract fun addViewWhenFinishInflate()

    abstract fun firstInit(info: String)

    public abstract override fun onAnimationEnd()

    abstract fun duringAnimation(rate: Float)

    abstract fun saveNextPosition(position: Int, info: String)
}