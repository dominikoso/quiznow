package me.dominikoso.quiznow

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_newsitem_list.*
import kotlinx.android.synthetic.main.fragment_quizitem_list.*
import me.dominikoso.quiznow.news.fragment.NewsListFragment
import me.dominikoso.quiznow.news.model.NewsItem
import me.dominikoso.quiznow.profile.activity.OtherProfileActivity
import me.dominikoso.quiznow.profile.fragment.ProfileFragment
import me.dominikoso.quiznow.profile.model.UserItem
import me.dominikoso.quiznow.questionSet.activity.QuizActivity
import me.dominikoso.quiznow.questionSet.model.QuestionItem
import me.dominikoso.quiznow.selector.fragment.QuizSelectorFragment
import me.dominikoso.quiznow.selector.model.QuizItem
import me.dominikoso.quiznow.summary.activity.QuizSummaryActivity
import me.dominikoso.quiznow.summary.activity.QuizSummaryActivity.Companion.NEW_FEED

/**
 * Main activity in application. Here is gesture supported ViewPager enriched in Navigation bar located on bottom
 */

class MainActivity : BaseActivity(),
                    QuizSelectorFragment.OnStartQuizListener,
                    NewsListFragment.OnNewsInteractionListener,
                    ProfileFragment.OnLogStateChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setViewPager()
    }

    //region ViewPager and bottomNavigation setup
    private fun setViewPager() {
        viewpager.adapter = getFragmentPagerAdapter()
        navigation.setOnNavigationItemSelectedListener (getBottomNavigationItemSelectedListener())
        viewpager.addOnPageChangeListener(getOnPageChangeListener())
        viewpager.offscreenPageLimit = 2

    }

    private fun getFragmentPagerAdapter() =
            object : FragmentPagerAdapter(supportFragmentManager) {
                override fun getItem(p0: Int) = when (p0) {
                    FEED_ID -> NewsListFragment() //NewsListFragment()
                    CHOOSER_ID -> QuizSelectorFragment() //NewsListFragment()
                    PROFILE_ID -> ProfileFragment()
                    else -> {
                        Log.wtf("Fragment out of bounds", "Something not quite right...")
                        Fragment()
                    }
                }

                override fun getCount() = 3

            }

    private fun getBottomNavigationItemSelectedListener() =
    BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                viewpager.currentItem = 0
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                viewpager.currentItem = 1
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                viewpager.currentItem = 2
                return@OnNavigationItemSelectedListener true
            }
            else -> false
        }
    }

    private fun getOnPageChangeListener() =
    object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(p0: Int) {}
        override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}
        override fun onPageSelected(p0: Int) { navigation.menu.getItem(p0).isChecked = true }

    }

    //endregion

    //region Return form window controller
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            when{
                (requestCode == QUIZ_ACT_REQ_CODE) -> {
                    navigateToSummaryActivity(data)
                }
                (requestCode == QUIZ_SUMMARY_RCODE) -> {
                    pushNewNews(data)
                }
            }
        }
    }

    private fun pushNewNews(data: Intent?) {
        val feedItem = data!!.extras.get(NEW_FEED) as NewsItem
        QApp.fData.getReference("feeds").push().setValue(feedItem.apply {
            uid = QApp.fUser!!.uid
            image = QApp.fUser!!.photoUrl.toString()
            user = QApp.fUser!!.displayName!!
        })
        viewpager.currentItem = 0
        getNewsListFragment().feed_item_list.smoothScrollToPosition(0)
    }

    private fun navigateToSummaryActivity(data: Intent?) {
        var intent = Intent(this, QuizSummaryActivity::class.java).apply {
            if (QApp.fUser != null){

                data?.putExtra(USER_NAME, QApp.fUser?.displayName
                    ?: QApp.res.getString(R.string.anonym_name))
                data?.putExtra(USER_URL, QApp.fUser?.photoUrl.toString())
            }
            putExtras(data!!.extras)
        }
        startActivityForResult(intent, QUIZ_SUMMARY_RCODE)
    }
    //endregion

    //region Fragment Interface controller
    private fun getSelectorListFragment() =
        (supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + CHOOSER_ID) as QuizSelectorFragment)
    private fun getNewsListFragment() =
        (supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + FEED_ID) as NewsListFragment)


    override fun onStartQuizSelected(quiz: QuizItem, name: String) {
        getSelectorListFragment().loader_quiz.visibility = View.VISIBLE
        //todo comunication

        QApp.fData.getReference("questions/${quiz.questset}").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val quizset = ArrayList<QuestionItem>()
                p0.children.map { it.getValue(QuestionItem::class.java) }.mapTo(quizset) {it!!}
                getSelectorListFragment().loader_quiz.visibility = View.GONE
                navigateQuiz(quizset, name, quiz)
            }

        })


    }

    private fun navigateQuiz(quizset: ArrayList<QuestionItem>, title: String, quiz: QuizItem) {
        val intent = Intent(this, QuizActivity::class.java).apply {
            putExtra(QUIZ_SET, quizset)
            putExtra(TITLE, title)
            putExtra(QUIZ, quiz)
        }
        startActivityForResult(intent, QUIZ_ACT_REQ_CODE)
    }

    //endregion

    //region NewsListFragment

    override fun onUserSelected(user: UserItem, image: View) {
        val intent = Intent(this, OtherProfileActivity::class.java)
            intent.putExtra(User_ITEM, user)
            val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, image,"circleProfileImageTransition")
        startActivity(intent, optionsCompat.toBundle())
    }

    override fun onLikeSelected(feedId: String, diff: Int) {
        if(QApp.fUser != null) {
            QApp.fData.getReference("feeds/$feedId/respects").updateChildren(mapOf(Pair(QApp.fUser?.uid, diff)))
                .addOnCompleteListener { Log.d("MainActivity", "Just liked $feedId with $diff") }
        }
    }

    override fun onLogOut() {
        QApp.fAuth.signOut()
        getNewsListFragment().feed_item_list.adapter?.notifyDataSetChanged()
    }

    override fun onLogIn() {
        logIn()
    }

    //endregion

    companion object {
        const val FEED_ID = 0
        const val CHOOSER_ID = 1
        const val PROFILE_ID = 2

       const val QUIZ_SET = "quiz_set"
       const val TITLE = "TITLE"
       const val QUIZ = "QUIZ"

        const val USER_NAME = "USER_NAME"
        const val USER_URL = "USER_URL"
        const val User_ITEM = "USER_ITEM"

        const val QUIZ_ACT_REQ_CODE = 666
        const val QUIZ_SUMMARY_RCODE = 777

    }
}
