package com.github.sadaharusong.wordsmallguess.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.view.View.OnClickListener
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.github.sadaharusong.wordsmallguess.R

/**
 * @author sadaharusong
 * @date 2019/4/27.
 * GitHub：https://github.com/sadaharusong
 */
class PileLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {

    private val mMaximumVelocity: Int
    private val onClickListener: View.OnClickListener

    // 以下三个参数，可通过属性定制
    private var interval = 30 // view之间的间隔
    private var sizeRatio = 1.1f
    private var scaleStep = 0.36f

    private var everyWidth: Int = 0
    private var everyHeight: Int = 0
    private var scrollDistanceMax: Int = 0 // 滑动参考值
    private val originX : ArrayList<Int> = arrayListOf() // 存放的是最初的七个View的位置
    private var scrollMode: Int = 0
    private var downX: Int = 0
    private var downY: Int = 0
    private var lastX: Float = 0.toFloat()
    private val mTouchSlop: Int // 判定为滑动的阈值，单位是像素

    private var animateValue: Float = 0.toFloat()
    private var animator: ObjectAnimator? = null
    private val interpolator = DecelerateInterpolator(1.6f)
    private var adapter: Adapter? = null
    private var hasSetAdapter = false
    private var displayCount = 1.6f
    private var animatingView: FrameLayout? = null
    private var mVelocityTracker: VelocityTracker? = null

    init {

        val a = context.obtainStyledAttributes(attrs, R.styleable.pile)
        interval = a.getDimension(R.styleable.pile_interval, interval.toFloat()).toInt()
        sizeRatio = a.getFloat(R.styleable.pile_sizeRatio, sizeRatio)
        scaleStep = a.getFloat(R.styleable.pile_scaleStep, scaleStep)
        displayCount = a.getFloat(R.styleable.pile_displayCount, displayCount)
        a.recycle()

        val configuration = ViewConfiguration.get(getContext())
        mTouchSlop = configuration.scaledTouchSlop
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity

        onClickListener = OnClickListener { v ->
            if (null != adapter) {
                val position = Integer.parseInt(v.getTag().toString())
                if (position >= 0 && position < adapter!!.itemCount) {
                    adapter!!.onItemClick((v as FrameLayout).getChildAt(0), position)
                }
            }
        }

        viewTreeObserver.addOnGlobalLayoutListener {
            if (height > 0 && null != adapter && !hasSetAdapter) {
                setAdapter(adapter)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        //可以根据自己需求计算视图的高度，或者根据自己设计写固定值
        //everyWidth = ((width - paddingLeft - paddingRight - interval * 8) / displayCount).toInt()
        //everyHeight = (everyWidth * sizeRatio).toInt()
        everyWidth = 400
        everyHeight = 400
        setMeasuredDimension(width, (everyHeight * (1 + scaleStep) + paddingTop.toFloat() + paddingBottom.toFloat()).toInt())

        // 把每个View的初始位置坐标都计算好
        if (originX!!.size == 0) {
            val position0 = 0
            originX.add(position0)

            val position1 = interval
            originX.add(position1)

            val position2 = interval * 2
            originX.add(position2)

            val position3 = interval * 3
            originX.add(position3)

            val position4 = (position3.toFloat() + everyWidth * (1 + scaleStep) + interval.toFloat()).toInt()
            originX.add(position4)

            val position5 = position4 + everyWidth + interval
            originX.add(position5)

            val position6 = position5 + everyWidth + interval
            originX.add(position6)

            val position7 = position6 + everyWidth + interval
            originX.add(position7)

            val position8 = position7 + everyWidth + interval
            originX.add(position8)

            scrollDistanceMax = position4 - position3
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val num = childCount
        for (i in 0 until num) {
            val itemView = getChildAt(i)
            val left = originX!!.get(i)
            val top = (measuredHeight - everyHeight) / 2
            val right = left + everyWidth
            val bottom = top + everyHeight
            itemView.layout(left, top, right, bottom)
            itemView.pivotX = 0F
            itemView.pivotY = (everyHeight / 2).toFloat()
            adjustScale(itemView)
            adjustAlpha(itemView)
        }
    }

    /**
     * 根据X坐标位置调整ImageView的透明度
     *
     * @param itemView 需要调整的imageView
     */
    private fun adjustAlpha(itemView: View) {
        val position2 = originX!![2]
        if (itemView.left >= position2) {
            itemView.alpha = 1F
        } else {
            val position0 = originX[0]
            val alpha = itemView.left.toFloat() / (position2 - position0)
            itemView.alpha = alpha
        }
    }

    private fun adjustScale(itemView: View) {
        var scale = 1.0f
        val position4 = originX!!.get(4)
        val thisLeft = itemView.left
        if (thisLeft < position4) {
            val position3 = originX.get(3)
            if (thisLeft > position3) {
                scale = 1 + scaleStep - scaleStep * (thisLeft - position3) / (position4 - position3)
            } else {
                val position2 = originX.get(2)
                scale = 1 + (thisLeft - position2) * scaleStep / interval
            }
        }
        itemView.setScaleX(scale)
        itemView.setScaleY(scale)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // 决策是否需要拦截
        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x.toInt()
                downY = event.y.toInt()
                lastX = event.x
                scrollMode = MODE_IDLE
                if (null != animator) {
                    animator!!.cancel()
                }

                initVelocityTrackerIfNotExists()
                mVelocityTracker!!.addMovement(event)
                animatingView = null
            }

            MotionEvent.ACTION_MOVE -> if (scrollMode == MODE_IDLE) {
                val xDistance = Math.abs(downX - event.x)
                val yDistance = Math.abs(downY - event.y)
                if (xDistance > yDistance && xDistance > mTouchSlop) {
                    // 水平滑动，需要拦截
                    scrollMode = MODE_HORIZONTAL
                    return true
                } else if (yDistance > xDistance && yDistance > mTouchSlop) {
                    // 垂直滑动
                    scrollMode = MODE_VERTICAL
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                recycleVelocityTracker()
                // ACTION_UP还能拦截，说明手指没滑动，只是一个click事件，同样需要snap到特定位置
                onRelease(event.x, 0)
            }
        }
        return false // 默认都是不拦截的
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mVelocityTracker!!.addMovement(event)
        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> {
            }

            MotionEvent.ACTION_MOVE -> {
                val currentX = event.x.toInt()
                val dx = (currentX - lastX).toInt()
                requireScrollChange(dx)
                lastX = currentX.toFloat()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val velocityTracker = mVelocityTracker
                velocityTracker!!.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                val velocity = velocityTracker.xVelocity.toInt()
                recycleVelocityTracker()

                onRelease(event.x, velocity)
            }
        }// 此处说明底层没有子View愿意消费Touch事件
        return true
    }

    private fun onRelease(eventX: Float, velocityX: Int) {
        animatingView = getChildAt(3) as FrameLayout
        animateValue = animatingView!!.left.toFloat()
        var tag = Integer.parseInt(animatingView!!.tag.toString())

        // 计算目标位置
        var destX = originX!!.get(3)
        if (velocityX > VELOCITY_THRESHOLD || animatingView!!.left > originX.get(3) + scrollDistanceMax / 2 && velocityX > -VELOCITY_THRESHOLD) {
            destX = originX.get(4)
            tag--
        }
        if (tag < 0 || tag >= adapter!!.itemCount) {
            return
        }

        if (Math.abs(animatingView!!.left - destX) < mTouchSlop && Math.abs(eventX - downX) < mTouchSlop) {
            return
        }

        adapter!!.displaying(tag)
        animator = ObjectAnimator.ofFloat(this, "animateValue" ,animatingView!!.left.toFloat(), destX.toFloat())
        animator!!.interpolator = interpolator
        animator!!.setDuration(360).start()
    }

    private fun requireScrollChange(dx: Int) {
        var dx = dx
        if (dx == 0) {
            return
        }

        val currentPosition = Integer.parseInt(getChildAt(3).tag.toString())
        if (dx < 0 && currentPosition >= adapter!!.itemCount) {
            return
        } else if (dx > 0) {
            if (currentPosition <= 0) {
                return
            } else if (currentPosition == 1) {
                if (getChildAt(3).left + dx >= originX!!.get(4)) {
                    dx = originX.get(4) - getChildAt(3).left
                }
            }
        }


        val num = childCount

        // 1. View循环复用
        val firstView = getChildAt(0) as FrameLayout
        if (dx > 0 && firstView.left >= originX!!.get(1)) {
            // 向右滑动，从左边把View补上
            val lastView = getChildAt(childCount - 1) as FrameLayout

            val lp = lastView.layoutParams
            removeViewInLayout(lastView)
            addViewInLayout(lastView, 0, lp)

            var tag = Integer.parseInt(lastView.tag.toString())
            tag -= num
            lastView.tag = tag
            if (tag < 0) {
                lastView.visibility = View.INVISIBLE
            } else {
                lastView.visibility = View.VISIBLE
                adapter!!.bindView(lastView.getChildAt(0), tag)
            }
        } else if (dx < 0 && firstView.left <= originX!!.get(0)) {
            // 向左滑动，从右边把View补上
            val lp = firstView.layoutParams
            removeViewInLayout(firstView)
            addViewInLayout(firstView, -1, lp)

            var tag = Integer.parseInt(firstView.tag.toString())
            tag += num
            firstView.tag = tag
            if (tag >= adapter!!.itemCount) {
                firstView.visibility = View.INVISIBLE
            } else {
                firstView.visibility = View.VISIBLE
                adapter!!.bindView(firstView.getChildAt(0), tag)
            }
        }

        // 2. 位置修正
        val view3 = getChildAt(3)
        var rate = (view3.left + dx - originX!![3]).toFloat() / scrollDistanceMax
        if (rate < 0) {
            rate = 0f
        }
        val position1 = Math.round(rate * (originX.get(2) - originX.get(1))) + originX.get(1)
        var endAnim = false
        if (position1 >= originX.get(2) && null != animatingView) {
            animator!!.cancel()
            endAnim = true
        }
        for (i in 0 until num) {
            val itemView = getChildAt(i)
            if (endAnim) {
                itemView.offsetLeftAndRight(originX.get(i + 1) - itemView.left)
            } else if (itemView === animatingView) {
                itemView.offsetLeftAndRight(dx)
            } else {
                var position = Math.round(rate * (originX.get(i + 1) - originX.get(i))) + originX.get(i)
                if (i + 1 < originX.size && position >= originX.get(i + 1)) {
                    position = originX.get(i + 1)
                }
                itemView.offsetLeftAndRight(position - itemView.left)
            }
            adjustAlpha(itemView) // 调整透明度
            adjustScale(itemView) // 调整缩放
        }
    }

    /**
     * 绑定Adapter
     */
    fun setAdapter(adapter: Adapter?) {
        this.adapter = adapter

        // ViewdoBindAdapter尚未渲染出来的时候，不做适配
        if (everyWidth > 0 && everyHeight > 0) {
            doBindAdapter()
        }
    }


    /**
     * 真正绑定Adapter
     */
    private fun doBindAdapter() {
        if (adapter == null) {
            return
        }
        if (hasSetAdapter) {
            throw RuntimeException("PileLayout can only hold one adapter.")
        }
        hasSetAdapter = true
        if (childCount == 0) {
            val inflater = LayoutInflater.from(context)
            for (i in 0..5) {
                val frameLayout = FrameLayout(context)
                val view = inflater.inflate(adapter!!.layoutId, null)
                val lp1 = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                lp1.width = everyWidth
                lp1.height = everyHeight
                frameLayout.addView(view, lp1)
                val lp2 = ViewGroup.LayoutParams(everyWidth, everyHeight)
                lp2.width = everyWidth
                lp2.height = everyHeight
                frameLayout.layoutParams = lp2
                frameLayout.setOnClickListener(onClickListener)
                addView(frameLayout)
                frameLayout.tag = i - 3 // 这个tag主要是对应于在dataList中的数据index
                frameLayout.measure(everyWidth, everyHeight)
            }
        }

        val num = childCount
        for (i in 0 until num) {
            if (i < 3) {
                getChildAt(i).visibility = View.INVISIBLE
            } else {
                val frameLayout = getChildAt(i) as FrameLayout
                if (i - 3 < adapter!!.itemCount) {
                    frameLayout.visibility = View.VISIBLE
                    adapter!!.bindView(frameLayout.getChildAt(0), i - 3)
                } else {
                    frameLayout.visibility = View.INVISIBLE
                }
            }
        }

        if (adapter!!.itemCount > 0) {
            adapter!!.displaying(0)
        }
    }

    /**
     * 数据更新通知
     */
    fun notifyDataSetChanged() {
        val num = childCount
        for (i in 0 until num) {
            val frameLayout = getChildAt(i) as FrameLayout
            val tag = Integer.parseInt(frameLayout.tag.toString())
            if (tag > 0 && tag < adapter!!.itemCount) {
                frameLayout.visibility = View.VISIBLE
                adapter!!.bindView(frameLayout.getChildAt(0), tag)
            } else {
                frameLayout.visibility = View.INVISIBLE
            }

            if (i == 3 && tag == 0) {
                adapter!!.displaying(0)
            }
        }
    }

    private fun initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
    }

    private fun recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if (disallowIntercept) {
            recycleVelocityTracker()
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }


    /**
     * 属性动画，请勿删除
     */
    fun setAnimateValue(animateValue: Float) {
        this.animateValue = animateValue // 当前应该在的位置
        val dx = Math.round(animateValue - animatingView!!.left)
        requireScrollChange(dx)
    }

    fun getAnimateValue(): Float {
        return animateValue
    }

    /**
     * 适配器
     */
    abstract class Adapter {

        /**
         * layout文件ID，调用者必须实现
         */
        abstract val layoutId: Int

        /**
         * item数量，调用者必须实现
         */
        abstract val itemCount: Int

        /**
         * View与数据绑定回调，可重载
         */
        open fun bindView(view: View, index: Int) {}

        /**
         * 正在展示的回调，可重载
         */
        open fun displaying(position: Int) {}

        /**
         * item点击，可重载
         */
        open fun onItemClick(view: View, position: Int) {}
    }

    companion object {

        // 拖拽相关
        private val MODE_IDLE = 0
        private val MODE_HORIZONTAL = 1
        private val MODE_VERTICAL = 2
        private val VELOCITY_THRESHOLD = 200
    }
}