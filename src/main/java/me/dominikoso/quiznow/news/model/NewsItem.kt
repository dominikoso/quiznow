package me.dominikoso.quiznow.news.model

import java.io.Serializable

/**
 * Model class for News Object
 * @property comment String
 * @property points Int
 * @property quiz String
 * @property image String
 * @property user String
 * @property timeMilis Long
 * @property uid String
 * @property respects HashMap<String, Int>
 * @constructor
 */
data class NewsItem(
        var comment : String = "",
        var points : Int = 0,
        var quiz : String = "",
        var image : String = "",
        var user : String = "",
        var timeMilis : Long = 0,
        var uid : String = "",
        var respects : HashMap<String, Int> = hashMapOf()) : Serializable