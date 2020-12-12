package com.example.firebasegiris.view

import android.content.AbstractThreadedSyncAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasegiris.PhotoShareActivity
import com.example.firebasegiris.model.Post
import com.example.firebasegiris.R
import com.example.firebasegiris.adapter.FeedRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_feed.*

class FeedActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerViewAdapter: FeedRecyclerAdapter
    var postList = ArrayList<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        getData()

        var layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerViewAdapter = FeedRecyclerAdapter(postList)
        recyclerView.adapter = recyclerViewAdapter

    }

    fun getData() {

        database.collection("Post").orderBy("time",Query.Direction.DESCENDING).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Toast.makeText(applicationContext, error.localizedMessage, Toast.LENGTH_LONG).show()
            } else {
                //snapshot da null gelebilir , bu yüzden kontrol ediyoruz.
                if (snapshot != null) {

                    //snapshot null olmayabilir ama içi boş da olabilir bu yüzden bunu da kontrol ediyoruz.
                    if (snapshot.isEmpty == false) {
                        //snapshotın artık boş ya da null omayacağına emin olabiliriz, verileri alıyoruz.

                        val documents = snapshot.documents

                        postList.clear()

                        for (document in documents) {
                            val userEmail = document.get("useremail") as String
                            val userComment = document.get("usersomment") as String
                            val photoUrl = document.get("photourl") as String

                            val uploadedPost =
                                Post(
                                    userEmail,
                                    userComment,
                                    photoUrl
                                )
                            postList.add(uploadedPost)

                        }

                        recyclerViewAdapter.notifyDataSetChanged()
                    }

                }
            }

        }
    }


        //menuyu burda bağlıyoruz
        //iki fonksiyonu override etmemiz gerekiyor , bunlar ;
        //1. - menuyu bağlayan oncreateoptionsmenu
        //2. - onoptionsselecteditem


        override fun onCreateOptionsMenu(menu: Menu?): Boolean {

            //inflater , xml'leri kodlarımızla bağlarken kullandğımız yapı
            val menuInflater = menuInflater
            menuInflater.inflate(R.menu.options_menu, menu)

            return super.onCreateOptionsMenu(menu)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            if (item.itemId == R.id.photo_share) {
                //Fotoğraf paylaşma aktivitesine gidecek

                val intent = Intent(this, PhotoShareActivity::class.java)
                startActivity(intent)


            } else if (item.itemId == R.id.log_out) {
                //çıkış yapılacak
                //firebaseden de çıkış yapmamız lazım
                auth.signOut()
                val intent = Intent(this, UserActivity::class.java)
                startActivity(intent)
                finish()

            }



            return super.onOptionsItemSelected(item)
        }
    }
