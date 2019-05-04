package me.dominikoso.quiznow.profile.model

import java.io.Serializable

/**
 * Model class for User Object
 * @property name String
 * @property url String
 * @property uid String
 * @constructor
 */
data class UserItem (
        var name : String = "",
        var url : String = "",
        var uid : String = "") : Serializable