package me.dominikoso.quiznow.questionSet.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import android.widget.Toast
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand
import com.kofigyan.stateprogressbar.StateProgressBar
import kotlinx.android.synthetic.main.quiz_activity.*
import me.dominikoso.quiznow.BaseActivity
import me.dominikoso.quiznow.MainActivity.Companion.QUIZ
import me.dominikoso.quiznow.MainActivity.Companion.QUIZ_SET
import me.dominikoso.quiznow.MainActivity.Companion.TITLE
import me.dominikoso.quiznow.QApp
import me.dominikoso.quiznow.R
import me.dominikoso.quiznow.questionSet.model.QuestionItem
import me.dominikoso.quiznow.selector.model.QuizItem
import java.util.*

class QuizActivity : BaseActivity() {

    private val questionList by lazy { intent.extras.get(QUIZ_SET) as ArrayList<QuestionItem>}
    private val quizTitle by lazy { intent.extras.get(TITLE) as String }
    private val quiz by lazy { intent.extras.get(QUIZ) as QuizItem }

    val successArray : BooleanArray by lazy { BooleanArray(questionList.size) }

    private val quizIterator by lazy { questionList.iterator() }

    private lateinit var currentQuestionItem : QuestionItem

    private var currentPositive = 0
    private var currentNumber : Int = 0
    get() = if(field<5) field else 4

    private var questionTimer = getQuestionTimer()

    var prepareNext = getPrepareNextTimer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quiz_activity)

        quizLogo.setImageResource(quiz.category.image)
        levelImageView.setImageResource(quiz.level.image)
        nextQuestion()
    }

    private fun nextQuestion() {
        if(quizIterator.hasNext()){
            currentQuestionItem = quizIterator.next()
            currentPositive = Random().nextInt(3) + 1
            progress.setCurrentStateNumber(StateProgressBar.StateNumber.values()[currentNumber])
            questionText.niceSetText(currentQuestionItem.ask)
            setUpButtons()
            questionTimer.start()
        }else{
            returnResultFromQuiz()
        }
    }

    private fun setUpButtons() {
        ans_a.setOnClickListener ( onChoiceListener(false) )
        ans_b.setOnClickListener ( onChoiceListener(false) )
        ans_c.setOnClickListener ( onChoiceListener(false) )
        when (currentPositive){
            1 ->{
                ans_a.niceSetText(currentQuestionItem.positive)
                ans_a.setOnClickListener ( onChoiceListener(true) )
                ans_b.niceSetText(currentQuestionItem.falseTwo)
                ans_c.niceSetText(currentQuestionItem.falseOne)
            }
            2 ->{
                ans_a.niceSetText(currentQuestionItem.falseTwo)
                ans_b.niceSetText(currentQuestionItem.positive)
                ans_b.setOnClickListener ( onChoiceListener(true) )
                ans_c.niceSetText(currentQuestionItem.falseOne)
            }
            3 ->{
                ans_a.niceSetText(currentQuestionItem.falseTwo)
                ans_b.niceSetText(currentQuestionItem.falseOne)
                ans_c.niceSetText(currentQuestionItem.positive)
                ans_c.setOnClickListener ( onChoiceListener(true) )
            }
        }
    }

    private fun onChoiceListener(b: Boolean) : View.OnClickListener {
        return View.OnClickListener {
            successArray[currentNumber] = b
            questionTimer.cancel()
            if (!b){
                setButtonsColorBrand(DefaultBootstrapBrand.DANGER)
            }

            when(currentPositive) {
                1 -> ans_a.bootstrapBrand = DefaultBootstrapBrand.SUCCESS
                2 -> ans_b.bootstrapBrand = DefaultBootstrapBrand.SUCCESS
                3 -> ans_c.bootstrapBrand = DefaultBootstrapBrand.SUCCESS
            }

            setButtonsClickable(false)

            resetNextTimer()
            prepareNext.start()
        }
    }

    private fun setButtonsClickable(b: Boolean) {
        ans_a.isClickable = b        
        ans_b.isClickable = b        
        ans_c.isClickable = b        
    }

    private fun setButtonsColorBrand(brand: DefaultBootstrapBrand) {
        ans_a.bootstrapBrand = brand
        ans_b.bootstrapBrand = brand
        ans_c.bootstrapBrand = brand
    }

    private fun TextView.niceSetText(string: String){
        if(currentNumber>0){
            val anim = AlphaAnimation(1.0f, 0.0f)
            anim.duration = 300
            anim.repeatCount = 1
            anim.repeatMode = Animation.REVERSE
            anim.setAnimationListener(object : Animation.AnimationListener{
                override fun onAnimationRepeat(animation: Animation?) {this@niceSetText.text = string}
                override fun onAnimationEnd(animation: Animation?) {}
                override fun onAnimationStart(animation: Animation?) {}

            })
            this.startAnimation(anim)
        }else{
            this.text = string
        }
    }

    private var questionTimerRemain: Long = TIMER_REMAIN

    private fun getQuestionTimer(): CountDownTimer {
        return object : CountDownTimer(TIMER_REMAIN, 100){
            override fun onTick(millisUntilFinished: Long) {
                questionTimerRemain = millisUntilFinished
                timeLeftProgress.progressValue = millisUntilFinished / 1000f
            }

            override fun onFinish() {
                resetNextTimer()
                successArray[currentNumber] = false
                setButtonsColorBrand(DefaultBootstrapBrand.WARNING)
                setButtonsClickable(false)
                prepareNext.start()
                Toast.makeText(this@QuizActivity, QApp.res.getString(R.string.time_is_up), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetNextTimer() {
        questionTimerRemain = TIMER_REMAIN
        prepareNextRemain = PREPARE_NEXT_REMAIN
        prepareNext = getPrepareNextTimer()
    }

    private var prepareNextRemain: Long = PREPARE_NEXT_REMAIN

    private fun getPrepareNextTimer(): CountDownTimer {
        return object : CountDownTimer(PREPARE_NEXT_REMAIN, 10){
            override fun onTick(millisUntilFinished: Long) {
                prepareNextRemain = millisUntilFinished
                timeLeftProgress.progressValue = 40 - millisUntilFinished.toFloat() / 50
            }

            override fun onFinish() {
                resetQuestionTimer()
                setButtonsClickable(true)
                setButtonsColorBrand(DefaultBootstrapBrand.SECONDARY)
                currentNumber++
                nextQuestion()
            }

        }
    }

    private fun resetQuestionTimer() {
        questionTimerRemain = TIMER_REMAIN
        prepareNextRemain = PREPARE_NEXT_REMAIN
        questionTimer = getQuestionTimer()
    }

    override fun onPause() {
        super.onPause()
        questionTimer.cancel()
        prepareNext.cancel()
    }

    override fun onResume() {
        super.onResume()
        if (questionTimerRemain != TIMER_REMAIN){
            questionTimer = getQuestionTimer()
            questionTimer.start()
        }
        if (prepareNextRemain != PREPARE_NEXT_REMAIN){
            prepareNext = getPrepareNextTimer()
            prepareNext.start()
        }
    }

    private fun returnResultFromQuiz() {
        val intent = Intent().apply {
            putExtra(QUIZ_NAME, quizTitle)
            putExtra(SUCCESS_SUMMARY, "Correct ${successArray.count({it})}/5")
            putExtra(POINTS, successArray.count({it}) * (quiz.level.ordinal + 1) * 39)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        const val QUIZ_NAME = "QUIZNAME"
        const val SUCCESS_SUMMARY = "SUCCESS_SUMMARY"
        const val POINTS = "POINTS"

        const val TIMER_REMAIN = 40000L
        const val PREPARE_NEXT_REMAIN = 2000L
    }
}