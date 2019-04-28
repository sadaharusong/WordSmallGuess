package com.github.sadaharusong.wordsmallguess.activity

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.github.sadaharusong.wordsmallguess.R
import com.github.sadaharusong.wordsmallguess.entity.ItemEntity
import com.github.sadaharusong.wordsmallguess.util.Utils
import com.github.sadaharusong.wordsmallguess.widget.FadeTransitionImageView
import com.github.sadaharusong.wordsmallguess.widget.HorizontalTransitionLayout
import com.github.sadaharusong.wordsmallguess.widget.PileLayout
import com.github.sadaharusong.wordsmallguess.widget.VerticalTransitionLayout
import org.json.JSONObject

/**
 * @author sadaharusong
 * @date 2019/4/27.
 * GitHub：https://github.com/sadaharusong
 */
class SelectActivity : AppCompatActivity() {

    private var positionView: View? = null
    private var pileLayout: PileLayout? = null
    private var dataList: MutableList<ItemEntity>? = null

    private var lastDisplay = -1

    private var transitionAnimator: ObjectAnimator? = null
    private var transitionValue: Float = 0.toFloat()
    private var titleView: HorizontalTransitionLayout? = null
    private var typeView:HorizontalTransitionLayout? = null
    private var subTitleView: VerticalTransitionLayout? = null
    private var authorView:VerticalTransitionLayout? = null
    private var bottomView: FadeTransitionImageView? = null
    private var animatorListener: Animator.AnimatorListener? = null
    private var descriptionView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)

        positionView = findViewById(R.id.position_view)
        titleView = findViewById<View>(R.id.country_view) as HorizontalTransitionLayout?
        typeView = findViewById<View>(R.id.temperature_view) as HorizontalTransitionLayout?
        pileLayout = findViewById<View>(R.id.pile_layout) as PileLayout?
        subTitleView = findViewById<View>(R.id.address_view) as VerticalTransitionLayout?
        descriptionView = findViewById<View>(R.id.description_view) as TextView?
        authorView = findViewById<View>(R.id.time_view) as VerticalTransitionLayout?
        bottomView = findViewById<View>(R.id.bottom_iv) as FadeTransitionImageView?

        // 1. 状态栏侵入
        var adjustStatusHeight = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            adjustStatusHeight = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            } else {
                window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }

        // 2. 状态栏占位View的高度调整
        val brand = Build.BRAND
        if (brand.contains("Xiaomi")) {
            Utils.setXiaomiDarkMode(this)
        } else if (brand.contains("Meizu")) {
            Utils.setMeizuDarkMode(this)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decor = window.decorView
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            adjustStatusHeight = false
        }
        if (adjustStatusHeight) {
            adjustStatusBarHeight() // 调整状态栏高度
        }

        animatorListener = object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                titleView!!.onAnimationEnd()
                typeView!!.onAnimationEnd()
                subTitleView!!.onAnimationEnd()
                bottomView!!.onAnimationEnd()
                authorView!!.onAnimationEnd()
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        }


        // 3. PileLayout绑定Adapter
        initDataList()
        pileLayout!!.setAdapter(object : PileLayout.Adapter() {
            override val layoutId: Int
                get() = R.layout.item_layout

            override val itemCount: Int
                get() = dataList!!.size

            override fun bindView(view: View, position: Int) {
                var viewHolder: ViewHolder? = view.tag as ViewHolder?
                if (viewHolder == null) {
                    viewHolder = ViewHolder()
                    viewHolder.imageView = view.findViewById(R.id.imageView) as ImageView
                    view.tag = viewHolder
                }

                Glide.with(this@SelectActivity).load(dataList!![position]!!.iconUrl).
                        placeholder(R.mipmap.ic_launcher).into(viewHolder.imageView!!)
            }

            override fun displaying(position: Int) {
                descriptionView!!.text = dataList!![position].description
                if (lastDisplay < 0) {
                    initSecene(position)
                    lastDisplay = 0
                } else if (lastDisplay != position) {
                    transitionSecene(position)
                    lastDisplay = position
                }
            }

        })
    }

    private fun initSecene(position: Int) {
        titleView!!.firstInit(dataList!![position].title!!)
        typeView!!.firstInit(dataList!![position].type!!)
        subTitleView!!.firstInit(dataList!![position].subTitle!!)
        bottomView!!.firstInit("firstInit bottomView")
        authorView!!.firstInit(dataList!![position].author!!)
    }

    private fun transitionSecene(position: Int) {
        if (transitionAnimator != null) {
            transitionAnimator!!.cancel()
        }

        titleView!!.saveNextPosition(position, dataList!![position].title!!)
        typeView!!.saveNextPosition(position, dataList!![position].type!!)
        subTitleView!!.saveNextPosition(position, dataList!![position].subTitle!!)
        bottomView!!.saveNextPosition(position, "mapImageUrl")
        authorView!!.saveNextPosition(position, dataList!![position].author!!)

        transitionAnimator = ObjectAnimator.ofFloat(this, "transitionValue", 0.0f, 1.0f)
        transitionAnimator!!.duration = 300
        transitionAnimator!!.start()
        transitionAnimator!!.addListener(animatorListener)

    }

    /**
     * 调整沉浸状态栏
     */
    private fun adjustStatusBarHeight() {
        val statusBarHeight = Utils.getStatusBarHeight(this)
        val lp = positionView!!.layoutParams
        lp.height = statusBarHeight
        positionView!!.setLayoutParams(lp)
    }


    /**
     * 从asset读取文件json数据
     */
    private fun initDataList() {
        dataList = ArrayList()
        try {
            val inputStream = assets.open("preset.config")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            val jsonStr = String(buffer)
            val jsonObject = JSONObject(jsonStr)
            val jsonArray = jsonObject.optJSONArray("result")
            if (null != jsonArray) {
                val len = jsonArray.length()
                for (i in 0 until len) {
                    val itemJsonObject = jsonArray.getJSONObject(i)
                    val itemEntity = ItemEntity(itemJsonObject)
                    dataList!!.add(itemEntity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 属性动画
     */
    fun setTransitionValue(transitionValue: Float) {
        this.transitionValue = transitionValue
        titleView!!.duringAnimation(transitionValue)
        typeView!!.duringAnimation(transitionValue)
        subTitleView!!.duringAnimation(transitionValue)
        bottomView!!.duringAnimation(transitionValue)
        authorView!!.duringAnimation(transitionValue)
    }

    fun getTransitionValue(): Float {
        return transitionValue
    }

    internal inner class ViewHolder {
        var imageView: ImageView? = null
    }
}
