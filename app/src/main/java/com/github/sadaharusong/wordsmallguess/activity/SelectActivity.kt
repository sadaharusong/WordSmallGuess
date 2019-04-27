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
    private var countryView: HorizontalTransitionLayout? = null
    private var temperatureView:HorizontalTransitionLayout? = null
    private var addressView: VerticalTransitionLayout? = null
    private var timeView:VerticalTransitionLayout? = null
    private var bottomView: FadeTransitionImageView? = null
    private var animatorListener: Animator.AnimatorListener? = null
    private var descriptionView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)

        positionView = findViewById(R.id.position_view)
        countryView = findViewById<View>(R.id.country_view) as HorizontalTransitionLayout?
        temperatureView = findViewById<View>(R.id.temperature_view) as HorizontalTransitionLayout?
        pileLayout = findViewById<View>(R.id.pile_layout) as PileLayout?
        addressView = findViewById<View>(R.id.address_view) as VerticalTransitionLayout?
        descriptionView = findViewById<View>(R.id.description_view) as TextView?
        timeView = findViewById<View>(R.id.time_view) as VerticalTransitionLayout?
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
                get() = 50

            override fun bindView(view: View, position: Int) {
                var viewHolder: ViewHolder? = view.tag as ViewHolder?
                if (viewHolder == null) {
                    viewHolder = ViewHolder()
                    viewHolder.imageView = view.findViewById(R.id.imageView) as ImageView
                    view.tag = viewHolder
                }

                Glide.with(this@SelectActivity).load(getDrawable(R.mipmap.ic_launcher)).into(viewHolder.imageView!!)
            }

            override fun displaying(position: Int) {
                descriptionView!!.text = " Since the world is so beautiful, You have to believe me, and this index is " + position
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
        countryView!!.firstInit("firstInit countryView")
        temperatureView!!.firstInit("firstInit temperatureView")
        addressView!!.firstInit("firstInit addressView")
        bottomView!!.firstInit("firstInit bottomView")
        timeView!!.firstInit("firstInit timeView")
    }

    private fun transitionSecene(position: Int) {
        if (transitionAnimator != null) {
            transitionAnimator!!.cancel()
        }

        countryView!!.saveNextPosition(position, "country")
        temperatureView!!.saveNextPosition(position, ":temperature")
        addressView!!.saveNextPosition(position, "address")
        bottomView!!.saveNextPosition(position, "mapImageUrl")
        timeView!!.saveNextPosition(position, "time")

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
        /*try {
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
*/
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
