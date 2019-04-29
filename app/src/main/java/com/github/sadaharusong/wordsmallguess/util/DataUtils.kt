package com.github.sadaharusong.wordsmallguess.util

import android.content.Context
import com.github.sadaharusong.wordsmallguess.entity.ItemEntity
import com.github.sadaharusong.wordsmallguess.entity.WordsEntity
import org.json.JSONObject

/**
 * @author sadaharusong
 * @date 2019/4/30.
 * GitHub：https://github.com/sadaharusong
 */

object DataUtils {

    /**
     * 从asset读取文件json数据
     */
    fun getTypeList(context: Context) : ArrayList<ItemEntity>{
        var dataList = ArrayList<ItemEntity>()
        try {
            val inputStream = context.assets.open("preset.config")
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
        return dataList
    }

    /**
     * 获取词库数据
     */
    fun getDataList(context: Context,type: String,title: String): ArrayList<String> {
        var dataList = ArrayList<String>()
        try {
            val inputStream = context.assets.open(type + ".json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            val jsonStr = String(buffer)
            val jsonObject = JSONObject(jsonStr)
            val jsonArray = jsonObject.optJSONArray("data")
            if (null != jsonArray) {
                val len = jsonArray.length()
                for (i in 0 until len) {
                    val itemJsonObject = jsonArray.getJSONObject(i)
                    val wordsEntity = WordsEntity(itemJsonObject)
                    if (wordsEntity.category.equals(title)){
                        // todo some bug
                        dataList!!.add(wordsEntity.allWords!!)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dataList
    }
}