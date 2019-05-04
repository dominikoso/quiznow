package me.dominikoso.quiznow.selector.enums

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import me.dominikoso.quiznow.QApp
import me.dominikoso.quiznow.R

/**
 * Enum class for Category of Quiz
 * @property label Int
 * @property image Int
 * @constructor
 */
enum class CategoryEnum (@StringRes val label : Int,
                         @DrawableRes val image : Int)
{
    ANIME(R.string.cat_anime, R.drawable.ic_category_anime),
    LINUX(R.string.cat_linux, R.drawable.ic_category_linux),
    PROGRAMMING(R.string.cat_programming, R.drawable.ic_category_programming),
    WOW(R.string.cat_wow, R.drawable.ic_category_wow);

    fun getString() =
            QApp.res.getString(this.label)
}