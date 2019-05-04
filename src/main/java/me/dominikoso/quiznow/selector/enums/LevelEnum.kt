package me.dominikoso.quiznow.selector.enums

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import me.dominikoso.quiznow.QApp
import me.dominikoso.quiznow.R

/**
 * Enum class for difficulty of quiz
 * @property label Int
 * @property image Int
 * @constructor
 */
enum class LevelEnum(@StringRes val label : Int,
                     @DrawableRes val image : Int)
{
    EASY(R.string.level_easy, R.drawable.ic_level_easy),
    AVERAGE(R.string.level_average, R.drawable.ic_level_average),
    HARD(R.string.level_hard, R.drawable.ic_level_hard);

    fun getString() =
        QApp.res.getString(this.label)
}