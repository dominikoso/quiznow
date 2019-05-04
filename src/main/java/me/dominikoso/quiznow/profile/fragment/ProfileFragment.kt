package me.dominikoso.quiznow.profile.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_profile.*
import me.dominikoso.quiznow.MainActivity
import me.dominikoso.quiznow.QApp
import me.dominikoso.quiznow.R
import me.dominikoso.quiznow.firebase.toUserItem
import me.dominikoso.quiznow.news.adapter.NewsListRecyclerViewAdapter
import me.dominikoso.quiznow.news.fragment.NewsListFragment
import me.dominikoso.quiznow.news.model.NewsItem
import me.dominikoso.quiznow.profile.model.UserItem
import me.dominikoso.quiznow.BuildConfig

class ProfileFragment : Fragment() {

    var currentUser: UserItem? = null

    var feedRef: Query? = null

    var respectsValue = 1

    val authListener:FirebaseAuth.AuthStateListener by lazy {
        FirebaseAuth.AuthStateListener { firebaseAuth ->
            if(firebaseAuth.currentUser != null) {
                updateCurrentUser()
                updateFeedRefEventListener()
                updateLogon()
            }else {
                Log.d("NewsListFragment", "authListener - user:null")
            }
        }
    }

    val eventListener:ValueEventListener by lazy {
        object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                var pointsValue = 0
                    respectsValue = 0
                    for (it in p0.children) {
                        val feed = it.getValue(NewsItem::class.java)!!
                        mNewsMap.put(it.key!!, feed)
                        pointsValue += feed.points
                        respectsValue += 1 + it.child("respects").children.sumBy { it.getValue(Int::class.java)!! }
                    }
                feed_recycler?.adapter?.notifyDataSetChanged()
                respects?.text = respectsValue.toString()
                points?.text = pointsValue.toString()
                loader_profil?.visibility = View.INVISIBLE
            }

        }
    }

    private fun updateFeedRefEventListener() {

        currentUser?.let {
            feedRef = FirebaseDatabase.getInstance().getReference("feeds").orderByChild("uid").equalTo(it.uid)
            loader_profil.visibility = View.VISIBLE
            feedRef?.addValueEventListener(eventListener)
        }

    }

    private val mNewsMap: HashMap<String, NewsItem> = hashMapOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpRecycler()
    }

    private fun setUpRecycler() {
        if (context is NewsListFragment.OnNewsInteractionListener){
            feed_recycler.layoutManager = LinearLayoutManager(context)
            feed_recycler.adapter = NewsListRecyclerViewAdapter(mNewsMap, context as NewsListFragment.OnNewsInteractionListener)
        }
    }

    override fun onStop() {
        super.onStop()
        QApp.fAuth.removeAuthStateListener(authListener)
    }

    override fun onStart() {
        super.onStart()
        QApp.fAuth.addAuthStateListener(authListener)
        updateCurrentUser()
        updateFeedRefEventListener()
        updateLogon()
    }

    private fun updateCurrentUser() {
        currentUser = arguments?.get(USER) as? UserItem
        if (currentUser == null) {
            val firebase = QApp.fAuth.currentUser
            firebase?.let {
                currentUser = firebase.toUserItem()
            }

        }
    }

    private fun updateLogon() {
        when{
            currentUser == null -> {
                setUpViewsNotLogged()
                sign_in_button.setOnClickListener {
                    (activity as OnLogStateChangeListener).onLogIn()
                    loader_profil.visibility = View.VISIBLE
                }
            }
            currentUser != null -> setUpViewsLogged()
        }
        updateDebugFabLogout()
    }

    private fun updateDebugFabLogout() {
        if (BuildConfig.DEBUG) {
            val visibility = (currentUser !=null && context is MainActivity)
           if (visibility) fab_debug_logout.show() else fab_debug_logout.hide()
            fab_debug_logout.setOnClickListener {
                currentUser = null
                (activity as OnLogStateChangeListener).onLogOut()
                updateLogon()
            }

        }
    }

    private fun setUpViewsNotLogged() {
        login_layout.visibility = View.VISIBLE
        feed_recycler.visibility = View.GONE
        respects.text = 0.toString()
        points.text = 0.toString()
        circleProfileImage.setImageDrawable(QApp.res.getDrawable(R.drawable.ic_anonym_face, null))
        collapsing_toolbar.title = QApp.res.getString(R.string.anonym_name)
    }

    private fun setUpViewsLogged() {
        login_layout.visibility = View.GONE
        feed_recycler.visibility = View.VISIBLE
        setUpUserInformations()
    }

    private fun setUpUserInformations() {
        collapsing_toolbar.title = currentUser!!.name
        Glide.with(this@ProfileFragment)
            .load(currentUser?.url)
            .into(circleProfileImage)
    }

    interface OnLogStateChangeListener{
        fun onLogOut()
        fun onLogIn()
    }

    companion object {

        const val USER = "USER"

        fun newInstance(user: UserItem) : ProfileFragment {
            val fragment = ProfileFragment()
            val bundle = Bundle()
            bundle.putSerializable(USER, user)
            fragment.arguments = bundle
            return fragment
        }
    }
}