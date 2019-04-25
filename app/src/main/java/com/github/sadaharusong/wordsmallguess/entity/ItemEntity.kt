package com.github.sadaharusong.wordsmallguess.entity

import org.json.JSONObject

class ItemEntity(jsonObject: JSONObject) {

    var country: String? = null
    var temperature: String? = null
    var coverImageUrl: String? = null
    var address: String? = null
    var description: String? = null
    var time: String? = null
    var mapImageUrl: String? = null

    init {
        this.country = jsonObject.optString("country")
        this.temperature = jsonObject.optString("temperature")
        this.coverImageUrl = jsonObject.optString("coverImageUrl")
        this.address = jsonObject.optString("address")
        this.description = jsonObject.optString("description")
        this.time = jsonObject.optString("time")
        this.mapImageUrl = jsonObject.optString("mapImageUrl")
    }
}