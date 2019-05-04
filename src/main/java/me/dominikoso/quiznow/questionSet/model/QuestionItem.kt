package me.dominikoso.quiznow.questionSet.model

import java.io.Serializable

/**
 * Model class for quiz question
 * @property ask String
 * @property positive String
 * @property falseOne String
 * @property falseTwo String
 * @constructor
 */
data class QuestionItem (
        var ask : String = "ask",
        var positive : String = "pos",
        var falseOne : String = "false 1",
        var falseTwo : String = "false 2") : Serializable
