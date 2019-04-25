package com.github.sadaharusong.wordsmallguess.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.github.sadaharusong.wordsmallguess.R
import com.github.sadaharusong.wordsmallguess.entity.ItemEntity
import org.json.JSONObject
import android.animation.ObjectAnimator
import android.animation.Animator
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.github.sadaharusong.wordsmallguess.widget.FadeTransitionImageView
import com.github.sadaharusong.wordsmallguess.widget.VerticalTransitionLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.github.sadaharusong.wordsmallguess.util.Utils
import com.github.sadaharusong.wordsmallguess.widget.HorizontalTransitionLayout
import com.github.sadaharusong.wordsmallguess.widget.PileLayout


class SelectActivity : AppCompatActivity() {

    private var positionView: View? = null
    private var pileLayout: PileLayout? = null
    private var dataList: MutableList<ItemEntity>? = null

    private var lastDisplay = -1

    private var transitionAnimator: ObjectAnimator? = null
    private var transitionValue: Float = 0.toFloat()
    private var countryView: HorizontalTransitionLayout? = null
    private var temperatureView:HorizontalTransitionLayout? = null
    private var addressView: VerticalTransitionLayout? = null
    private var timeView:VerticalTransitionLayout? = null
    private var bottomView: FadeTransitionImageView? = null
    private var animatorListener: Animator.AnimatorListener? = null
    private var descriptionView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        positionView = findViewById<View>(R.id.positionView)
        countryView = findViewById<View>(R.id.countryView) as HorizontalTransitionLayout?
        temperatureView = findViewById<View>(R.id.temperatureView) as HorizontalTransitionLayout
        pileLayout = findViewById<View>(R.id.pileLayout) as PileLayout
        addressView = findViewById<View>(R.id.addressView) as VerticalTransitionLayout?
        descriptionView = findViewById<View>(R.id.descriptionView) as TextView?
        timeView = findViewById<View>(R.id.timeView) as VerticalTransitionLayout
        bottomView = findViewById<View>(R.id.bottomImageView) as FadeTransitionImageView?

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
                countryView!!.onAnimationEnd()
                temperatureView!!.onAnimationEnd()
                addressView!!.onAnimationEnd()
                bottomView!!.onAnimationEnd()
                timeView!!.onAnimationEnd()
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
                    view.setTag(viewHolder)
                }

                Glide.with(this@SelectActivity).load(dataList!![position].coverImageUrl).into(viewHolder.imageView!!)
            }

            override fun displaying(position: Int) {
                descriptionView!!.text = dataList!![position].description + " Since the world is so beautiful, You have to believe me, and this index is " + position
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
        countryView!!.firstInit(dataList!![position].country!!)
        temperatureView!!.firstInit(dataList!![position].temperature!!)
        addressView!!.firstInit(dataList!![position].address!!)
        bottomView!!.firstInit(dataList!![position].mapImageUrl!!)
        timeView!!.firstInit(dataList!![position].time!!)
    }

    private fun transitionSecene(position: Int) {
        if (transitionAnimator != null) {
            transitionAnimator!!.cancel()
        }

        countryView!!.saveNextPosition(position, dataList!![position].country + "-" + position)
        temperatureView!!.saveNextPosition(position, dataList!![position].temperature!!)
        addressView!!.saveNextPosition(position, dataList!![position].address!!)
        bottomView!!.saveNextPosition(position, dataList!![position].mapImageUrl!!)
        timeView!!.saveNextPosition(position, dataList!![position].time!!)

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
        val lp = positionView!!.getLayoutParams()
        lp.height = statusBarHeight
        positionView!!.setLayoutParams(lp)
    }


    /**
     * 从asset读取文件json数据
     */
    private fun initDataList() {
        dataList = ArrayList()
        try {
            val `in` = assets.open("preset.config")
            val size = `in`.available()
            val buffer = ByteArray(size)
            `in`.read(buffer)
            val jsonStr = String(buffer)
            val jsonObject = JSONObject(jsonStr)
            val jsonArray = jsonObject.optJSONArray("result")
            if (null != jsonArray) {
                val len = jsonArray.length()
                for (j in 0..2) {
                    for (i in 0 until len) {
                        val itemJsonObject = jsonArray.getJSONObject(i)
                        val itemEntity = ItemEntity(itemJsonObject)
                        dataList!!.add(itemEntity)
                    }
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
        countryView!!.duringAnimation(transitionValue)
        temperatureView!!.duringAnimation(transitionValue)
        addressView!!.duringAnimation(transitionValue)
        bottomView!!.duringAnimation(transitionValue)
        timeView!!.duringAnimation(transitionValue)
    }

    fun getTransitionValue(): Float {
        return transitionValue
    }

    internal inner class ViewHolder {
        var imageView: ImageView? = null
    }
}
