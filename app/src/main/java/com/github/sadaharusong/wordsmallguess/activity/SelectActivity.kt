package com.github.sadaharusong.wordsmallguess.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.github.sadaharusong.wordsmallguess.R
import com.github.sadaharusong.wordsmallguess.adapter.SelectListAdapter
import kotlinx.android.synthetic.main.activity_select.*

class SelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)
        var adapter = SelectListAdapter()
        select_list.adapter = adapter
    }
}
