package com.example.firebasegiris.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.firebasegiris.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_user.*

class UserActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        auth= FirebaseAuth.getInstance()

        val currentUser=auth.currentUser
        if(currentUser!=null){
            val intent=Intent(this, FeedActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    fun girisYap(view : View){

        auth.signInWithEmailAndPassword(emailText.text.toString(),passwordText.text.toString()).addOnCompleteListener { task ->

            if (task.isSuccessful){

                val currentUser=auth.currentUser?.email.toString()
                Toast.makeText(this,"Hoşgeldiniz ${currentUser}",Toast.LENGTH_LONG).show()


                val intent=Intent(this,
                    FeedActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this,exception.localizedMessage,Toast.LENGTH_LONG).show()
        }

    }

    fun kayitOl(view : View){

        val email=emailText.text.toString()
        val password = passwordText.text.toString()

        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            //asenkron,cevap geldikten sonra ,gelen isteğe göre çalışmasını istiyoruz
            if (task.isSuccessful){
                //diğer aktiviteye geçiyoruz

                val intent=Intent(this,
                    FeedActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
        }


    }


}