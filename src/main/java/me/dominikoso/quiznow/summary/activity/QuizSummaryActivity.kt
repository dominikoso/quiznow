package me.dominikoso.quiznow.summary.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ActionMode
import android.view.View
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_newsitem.*
import kotlinx.android.synthetic.main.result_activity.*
import me.dominikoso.quiznow.BaseActivity
import me.dominikoso.quiznow.MainActivity.Companion.USER_NAME
import me.dominikoso.quiznow.MainActivity.Companion.USER_URL
import me.dominikoso.quiznow.QApp
import me.dominikoso.quiznow.R
import me.dominikoso.quiznow.news.model.NewsItem
import me.dominikoso.quiznow.questionSet.activity.QuizActivity.Companion.POINTS
import me.dominikoso.quiznow.questionSet.activity.QuizActivity.Companion.QUIZ_NAME
import me.dominikoso.quiznow.questionSet.activity.QuizActivity.Companion.SUCCESS_SUMMARY
import java.lang.Exception

class QuizSummaryActivity : BaseActivity() {
    //region intent extras
    private val quiz_name by lazy {intent.extras.get(QUIZ_NAME) as String}
    private val success_summary by lazy {intent.extras.get(SUCCESS_SUMMARY) as String}
    private val quiz_points by lazy {intent.extras.get(POINTS) as Int}
    private val user_name by lazy {intent.extras.get(USER_NAME) as? String}
    private val user_url by lazy {intent.extras.get(USER_URL) as? String}
    //endregion

    //region window init
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_activity)
        setUpViews()
    }

    private fun setUpViews() {
        title_caption.text = success_summary
        quizTitle.text = quiz_name
        pointsText.text = quiz_points.toString()
        respects.text = 1.toString()
        time.text = "00m"

        setUserName()
        setUserImage()
        setAddComment()

        likesImage.isEnabled = false
        setUpOkButton()
        setUpCloseButton()
    }

    private fun setUserImage() {
        if (!user_url.isNullOrEmpty()){
            Glide.with(this)
                .load(user_url)
                .into(circleImageProfile)
        }
    }

    private fun setUserName() {
        if(!user_name.isNullOrEmpty()) {
            name.text = user_name
        }
    }
    //endregion

    //region comment
    private fun setAddComment() {
        add_comment.visibility = View.VISIBLE
            comment.visibility = View.GONE
        add_comment.setOnClickListener { v -> showEditComment() }

    }

    private fun showEditComment() {
        add_comment.visibility = View.GONE
        edit_comment.visibility = View.VISIBLE
    }
    //endregion

    //region public
    private fun setUpOkButton() {
        if (FirebaseAuth.getInstance().currentUser != null){
            ok.setOnClickListener { v -> goToPublish() }
        }else{
            ok.text = QApp.res.getString(R.string.not_logged_news)
            ok.setOnClickListener { logIn() }
        }
    }

     override fun onLoginSuccess() {
        //todo bazowa
        goToPublish()
        //on login failed/success
    }

    override fun onLoginFailure(exception: Exception?) {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun goToPublish() {
        val intent = Intent().apply {
            putExtra(NEW_FEED, NewsItem().apply {
                comment = edit_comment.text.toString()
                points = quiz_points
                quiz = quiz_name
                timeMilis = System.currentTimeMillis()
            })
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun setUpCloseButton(){
        close_btn.setOnClickListener { v ->
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
    //endregion

    companion object {
        const val NEW_FEED = "newFeed"
    }

}