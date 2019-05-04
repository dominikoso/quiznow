package me.dominikoso.quiznow.selector.model

import me.dominikoso.quiznow.selector.enums.CategoryEnum
import me.dominikoso.quiznow.selector.enums.LevelEnum
import java.io.Serializable

/**
 * Model class for QuizItem
 * @property level LevelEnum
 * @property category CategoryEnum
 * @property questset String
 * @constructor
 */
data class QuizItem(
    var level : LevelEnum = LevelEnum.EASY,
    var category: CategoryEnum = CategoryEnum.ANIME,
    var questset: String = "" ) : Serializable