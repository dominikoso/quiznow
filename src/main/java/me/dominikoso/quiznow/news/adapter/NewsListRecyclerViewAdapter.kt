package me.dominikoso.quiznow.news.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import com.beardedhen.androidbootstrap.BootstrapCircleThumbnail
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import me.dominikoso.quiznow.QApp
import me.dominikoso.quiznow.R
import me.dominikoso.quiznow.news.fragment.NewsListFragment
import me.dominikoso.quiznow.news.model.NewsItem
import me.dominikoso.quiznow.profile.model.UserItem

class NewsListRecyclerViewAdapter(private val mNewsMap: HashMap<String, NewsItem>, private val onNewsInteractionListener: NewsListFragment.OnNewsInteractionListener) : RecyclerView.Adapter<NewsListRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NewsListRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.fragment_newsitem, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = mNewsMap.size

    override fun onBindViewHolder(p0: ViewHolder, p1: Int)  {
        val sortedList = mNewsMap.toList().sortedWith(Comparator { o1, o2 -> if(o1.second.timeMilis < o2.second.timeMilis) 1 else -1 })
        val second = sortedList[p1].second

        p0.mItem = second

        p0.name.text = second.user
        p0.time.text = getElapsedTimeMinutesFromString(second.timeMilis)
        p0.quizTitle.text = second.quiz
        p0.comment.text = second.comment
        p0.comment.visibility = getCommentVisibility(second)
        p0.pointsText.text = second.points.toString()
        p0.respects.text = countRespects(second)
        Glide.with(p0.mView.context)
            .load(second.image)
            .into(p0.profile)
        p0.respects.setOnClickListener { p0.likesImage.isChecked = !p0.likesImage.isChecked }
        p0.name.setOnClickListener { _ -> onNewsInteractionListener.onUserSelected(UserItem(second.user, second.image, second.uid), p0.profile) }
        p0.profile.setOnClickListener { _ -> onNewsInteractionListener.onUserSelected(UserItem(second.user, second.image, second.uid), p0.profile) }

        p0.likesImage.setOnCheckedChangeListener(null)
        p0.likesImage.isChecked = getLikeChecked(second)
        p0.likesImage.setOnCheckedChangeListener(onCheckedChangeListener(sortedList[p1].first))
    }

    private fun onCheckedChangeListener(feedId: String): (CompoundButton, Boolean) -> Unit = {
        compoundButton, isChecked ->
        if(FirebaseAuth.getInstance().currentUser != null){
            val diff = if (isChecked) 1 else 0
            onNewsInteractionListener.onLikeSelected(feedId = feedId, diff = diff)
        }else{
            compoundButton.isChecked = false
            Toast.makeText(compoundButton.context, QApp.res.getString(R.string.not_logged_like), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCommentVisibility(second: NewsItem): Int {
        return if(second.comment.isNotEmpty()) {
            View.VISIBLE
        }else{
            View.GONE
        }
    }

    private fun getLikeChecked(second: NewsItem): Boolean =
        FirebaseAuth.getInstance().currentUser != null && second.respects[FirebaseAuth.getInstance().currentUser?.uid] == 1


    private fun countRespects(second: NewsItem): String {
       return second.respects.values.count { it == 1 }.plus(1).toString()
    }

    private fun getElapsedTimeMinutesFromString(timeMilis: Long): String {
        val elapsedTimeSec = (System.currentTimeMillis() - timeMilis) / 1000
        val format = String.format("%%0%dd", 2)
        return when {
            (elapsedTimeSec / 3600 > 24) -> {
                val days = elapsedTimeSec / (60 * 60 * 24)
                String.format(format, days) + "d"
            }
            (elapsedTimeSec / 60 > 60) -> {
                val hours = elapsedTimeSec / (60*60)
                String.format(format, hours) + "h"
            }
            else -> {
                String.format(format, elapsedTimeSec / 60) + "m"
            }
        }


    }

    inner class ViewHolder(val mView : View) : RecyclerView.ViewHolder(mView){
        var mItem: NewsItem? = null

        val name = mView.findViewById<TextView>(R.id.name)!!
        val time = mView.findViewById<TextView>(R.id.time)!!
        val quizTitle = mView.findViewById<TextView>(R.id.quizTitle)!!
        val comment = mView.findViewById<TextView>(R.id.comment)!!
        val pointsText = mView.findViewById<TextView>(R.id.pointsText)!!
        val respects = mView.findViewById<TextView>(R.id.respects)!!
        val likesImage = mView.findViewById<CheckBox>(R.id.likesImage)!!
        val profile = mView.findViewById<BootstrapCircleThumbnail>(R.id.circleImageProfile)!!
    }

}
