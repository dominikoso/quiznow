package me.dominikoso.quiznow

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

abstract class BaseActivity: AppCompatActivity() {

    private val baseAuthStateListener: FirebaseAuth.AuthStateListener by lazy {
        FirebaseAuth.AuthStateListener { firebaseAuth ->
            QApp.fUser = firebaseAuth.currentUser
        }
    }

    override fun onResume() {
        super.onResume()
        QApp.fAuth.addAuthStateListener (baseAuthStateListener)
    }

    override fun onPause() {
        super.onPause()
        QApp.fAuth.removeAuthStateListener(baseAuthStateListener)
    }

    fun logIn(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)

                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                QApp.fAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, {
                            task ->
                            if (task.isSuccessful) {
                                onLoginSuccess()
                            }else{
                                onLoginFailure(task.exception)
                            }

                    })
            } catch (e: ApiException) {
                Log.w("BASE_ACTIVITY", "Google signIn Failed")
            }
        }
    }

    open fun onLoginFailure(exception: Exception?) {
        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
        Log.d("BASE_ACTIVITY",  "log in failed")
    }

    open fun onLoginSuccess() {
        Log.d("BASE_ACTIVITY",  "log in success")
    }

    companion object {
        const val RC_SIGN_IN = 33651 //Thanks for MoTY
    }
}
