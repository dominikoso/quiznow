package me.dominikoso.quiznow.profile.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.fragment_profile.*
import me.dominikoso.quiznow.BaseActivity
import me.dominikoso.quiznow.MainActivity
import me.dominikoso.quiznow.QApp
import me.dominikoso.quiznow.R
import me.dominikoso.quiznow.news.fragment.NewsListFragment
import me.dominikoso.quiznow.profile.fragment.ProfileFragment
import me.dominikoso.quiznow.profile.model.UserItem

class OtherProfileActivity: BaseActivity(), NewsListFragment.OnNewsInteractionListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = intent?.extras?.get(MainActivity.User_ITEM) as UserItem
        setContentView(R.layout.other_profile_activity)
        val ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.layout_other_profile, ProfileFragment.newInstance(user)).commit()
    }

    override fun onStart() {
        super.onStart()
        setUpToolbar()
    }

    private fun setUpToolbar() {
        toolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener{ onBackPressed() }
    }

    override fun onUserSelected(user: UserItem, image: View) {
        //not in this window
    }

    override fun onLikeSelected(feedId: String, diff: Int) {
        if(QApp.fUser != null) {
            QApp.fData.getReference("feeds/$feedId/respects").updateChildren(mapOf(Pair(QApp.fUser?.uid, diff)))
                .addOnCompleteListener { Log.d("MainActivity", "Just liked $feedId with $diff") }
        }
    }
}