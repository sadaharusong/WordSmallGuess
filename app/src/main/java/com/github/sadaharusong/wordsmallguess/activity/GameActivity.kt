package com.github.sadaharusong.wordsmallguess.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.sadaharusong.wordsmallguess.R
import java.util.*

/**
 * @author sadaharusong
 * @date 2019/4/29.
 * GitHub：https://github.com/sadaharusong
 */

class GameActivity : AppCompatActivity() {

    private var mGameList = ArrayList<String>()
    private var mTimeHandler: TimeHandler? = null
    private var mGameTime: Int = 0
    private var mTotalTime: Int = 0
    private var mCorrectCount: Int = 0
    private var mErrorCount: Int = 0

    private var mWordView: TextView? = null
    private var mSizeView: TextView? = null
    private var mTimeView: TextView? = null
    private var mCloseView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_activity)
        mTimeHandler = TimeHandler()

        initData()
        initView()
        resetGame()
    }

    private fun initData() {
        mGameList = intent.getStringArrayListExtra("gameList")
        mGameTime = intent.getIntExtra("gameTime", 0)

    }

    private fun initView() {
        mWordView = findViewById<View>(R.id.word_view) as TextView
        mSizeView = findViewById<View>(R.id.size_view) as TextView
        mTimeView = findViewById<View>(R.id.author_view) as TextView
        mCloseView = findViewById<View>(R.id.close_view) as ImageView

        mCloseView!!.setOnClickListener { v -> mTimeHandler!!.sendEmptyMessage(MSG_PAUSE) }
    }

    private fun showPauseDialog() {
        AlertDialog.Builder(this)
                .setTitle("暂停")
                .setMessage("暂停啦！")
                .setPositiveButton("退出") { dialog, which -> finish() }
                .setNegativeButton("继续") { dialog, which ->
                    mTimeHandler!!.sendEmptyMessage(MSG_START)
                }.create().show()
    }

    private fun showEndDialog() {
        AlertDialog.Builder(this)
                .setTitle("游戏结束")
                .setMessage("答对啦 : " + mCorrectCount + "题\n" + "答错啦 : " + mErrorCount + "题")
                .setPositiveButton("退出") { dialog, which -> finish() }
                .setNegativeButton("继续") { dialog, which ->
                    resetGame()
                }.create().show()
    }

    private fun resetGame() {
        mTotalTime = mGameTime
        mTimeHandler!!.sendEmptyMessage(MSG_START)
        changeWord()
    }

    private fun changeTime() {
        var min = mTotalTime / 60 % 60
        var sec = mTotalTime % 60
        var time:String = min.toString() + ":" + sec
        mTimeView!!.text = time
    }

    private fun changeWord() {
        if (mGameList.size > 0) {
            val l = System.currentTimeMillis()
            val index = (l % mGameList.size).toInt()
            mWordView!!.text = mGameList[index]
            mGameList.removeAt(index)
            mSizeView!!.text = "词库剩余 : " + mGameList.size.toString()
        } else {
            mTimeHandler!!.sendEmptyMessage(MSG_STOP)
        }
    }

    @SuppressLint("HandlerLeak")
    internal inner class TimeHandler : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_START) {
                mTotalTime--
                if (mTotalTime > 0) {
                    changeTime()
                    mTimeHandler!!.sendEmptyMessageDelayed(MSG_START, 1000)
                } else {
                    mTimeHandler!!.sendEmptyMessage(MSG_STOP)
                    mTimeHandler!!.removeMessages(MSG_START)
                }
            } else if (msg.what == MSG_PAUSE) {
                mTimeHandler!!.removeMessages(MSG_START)
                showPauseDialog()
            } else if (msg.what == MSG_STOP) {
                mTimeHandler!!.removeMessages(MSG_START)
                showEndDialog()
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mErrorCount++
            changeWord()
            return true
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mCorrectCount++
            changeWord()
            return true
        } else {
            return super.onKeyUp(keyCode, event)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onDestroy() {
        mTimeHandler = null
        super.onDestroy()
    }

    companion object {
        private const val MSG_START = 1
        private const val MSG_PAUSE = 2
        private const val MSG_STOP = 3
    }
}