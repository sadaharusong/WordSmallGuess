package com.github.sadaharusong.wordsmallguess.entity

import org.json.JSONObject

/**
 * @author sadaharusong
 * @date 2019/4/27.
 * GitHub：https://github.com/sadaharusong
 */
class ItemEntity(jsonObject: JSONObject) {

    var title: String? = null
    var type: String? = null
    var iconUrl: String? = null
    var subTitle: String? = null
    var description: String? = null
    var author: String? = null

    init {
        this.title = jsonObject.optString("title")
        this.type = jsonObject.optString("type")
        this.iconUrl = jsonObject.optString("iconUrl")
        this.subTitle = jsonObject.optString("subTitle")
        this.description = jsonObject.optString("description")
        this.author = jsonObject.optString("author")
    }
}