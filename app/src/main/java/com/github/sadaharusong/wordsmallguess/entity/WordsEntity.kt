package com.github.sadaharusong.wordsmallguess.entity

import org.json.JSONObject

/**
 * @author sadaharusong
 * @date 2019/4/30.
 * GitHub：https://github.com/sadaharusong
 */
class WordsEntity(jsonObject: JSONObject) {

    var category: String? = null
    var count: String? = null
    var allWords: String? = null

    init {
        this.category = jsonObject.optString("category")
        this.count = jsonObject.optString("count")
        this.allWords = jsonObject.optString("all_words")
    }
}