package me.dominikoso.quiznow.selector.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import me.dominikoso.quiznow.R
import me.dominikoso.quiznow.selector.fragment.QuizSelectorFragment
import me.dominikoso.quiznow.selector.model.QuizItem

class QuizSelectorRecyclerViewAdapter(private val quizzesMap : HashMap<String, QuizItem>, private val onStartQuizListener : QuizSelectorFragment.OnStartQuizListener) : RecyclerView.Adapter<QuizSelectorRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context)
            .inflate(R.layout.fragment_quizitem, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = quizzesMap.size

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val sorted = quizzesMap.values.toList().sortedBy { quizItem -> (quizItem.level.ordinal + quizItem.category.ordinal*10)  }
        p0.mItem = sorted[p1]

        p0.levelImageView.setImageResource(sorted[p1].level.image)
        p0.categoryImageView.setImageResource(sorted[p1].category.image)
        p0.quizTitle.text = getDoubleLineQuizTitle(sorted, p1)

            p0.mView.setOnClickListener {
                onStartQuizListener.onStartQuizSelected(p0.mItem ,getSingleLineQuizTitle(sorted, p1))
            }
    }
    private fun getSingleLineQuizTitle(sorted: List<QuizItem>, p1: Int)
            = "${sorted[p1].category.getString()} \n ${sorted[p1].level.getString()}"

    private fun getDoubleLineQuizTitle(sorted: List<QuizItem>, p1: Int)
        = "${sorted[p1].category.getString()} \n ${sorted[p1].level.getString()}"

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val levelImageView = mView.findViewById<View>(R.id.levelImageView) as ImageView
        val categoryImageView = mView.findViewById<View>(R.id.langImageView) as ImageView
        val quizTitle = mView.findViewById<View>(R.id.quizTitle) as TextView

        lateinit var mItem: QuizItem
    }
}