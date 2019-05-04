package me.dominikoso.quiznow.firebase

import com.google.firebase.auth.FirebaseUser
import me.dominikoso.quiznow.profile.model.UserItem

fun FirebaseUser.toUserItem(): UserItem {
    return UserItem().apply{
        uid = this@toUserItem.uid
        url = this@toUserItem.photoUrl.toString()
        name = this@toUserItem.displayName!!
    }
}