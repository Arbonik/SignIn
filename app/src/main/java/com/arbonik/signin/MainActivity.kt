package com.arbonik.signin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object{
        val TAG = "SignInActivity"
        val RC_SIGN_IN = 9001
    }

    var database = FirebaseDatabase.getInstance()
    var myRef = database.getReference("clickCount")
    var clickCount = 0



    var mGoogleSignInClient: GoogleSignInClient? = null
    var mStatusTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mStatusTextView = findViewById(R.id.status)
        sign_in_button.setOnClickListener{v -> signIn() }
        sign_out_button.setOnClickListener { v ->signOut() }
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
           .requestEmail()
           .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        myRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                clickCount = p0
            }
        })

    }

    override fun onStart() {
        super.onStart()
        var account = GoogleSignIn.getLastSignedInAccount(this)
        updateUI(account)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN){
            var task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

     fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            var account = task.getResult()
            updateUI(account)
        } catch (e : ApiException){
            Log.d(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    fun signIn(){
        var sighInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(sighInIntent, RC_SIGN_IN)
    }
    fun signOut(){
        mGoogleSignInClient?.signOut()!!
            .addOnCompleteListener (this) { task -> updateUI(null) }
    }

    fun revokeAccess(){
        mGoogleSignInClient?.revokeAccess()!!
            .addOnCompleteListener {  task -> updateUI(null) }
    }

    fun updateUI(account: GoogleSignInAccount?) {
        if (account != null){
            mStatusTextView?.setText("ВЫ АВТОРИЗОВАНЫ КАК ${account.displayName}")
        }
        else{
            mStatusTextView?.setText("SIGNED OUT")
        }
    }
}
