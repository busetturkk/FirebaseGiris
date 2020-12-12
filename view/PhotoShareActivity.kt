package com.example.firebasegiris

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_photo_share.*
import java.sql.Timestamp
import java.time.Instant.now
import java.util.*

class PhotoShareActivity : AppCompatActivity() {

    var selectedPhoto : Uri? = null
    var selectedBitmap : Bitmap? = null
    private lateinit var storage : FirebaseStorage
    private lateinit var database: FirebaseFirestore
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_share)

        storage= FirebaseStorage.getInstance()
        database= FirebaseFirestore.getInstance()
        auth= FirebaseAuth.getInstance()

    }

    fun share(view : View){

        //depo işlemleri
        //UUID -> universal unique id


        val uuid=UUID.randomUUID()
        val photoName="${uuid}.jpg"

        val reference = storage.reference

        val photoReference =  reference.child("images").child(photoName)

        if(selectedPhoto != null ){
            photoReference.putFile(selectedPhoto!!).addOnSuccessListener { taskSnapshot ->
                val uploadPhotoReference =storage.reference.child("images").child(photoName)
                //bir şeyin yüklendiğini anlamak için ancak o yüklendikten sonra yeni bir referans oluşturarak anlayabiliriz
                uploadPhotoReference.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl=uri.toString()
                    val currentUserEmail=auth.currentUser!!.email.toString()
                    val userComment=commentText.text.toString()
                    val time = com.google.firebase.Timestamp.now()


                    //veritabanı işlemleri
                    val postHashMap = hashMapOf<String,Any>()
                    postHashMap.put("photourl",downloadUrl)
                    postHashMap.put("useremail",currentUserEmail)
                    postHashMap.put("usersomment",userComment)
                    postHashMap.put("time",time)

                    database.collection("Post").add(postHashMap).addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            finish()
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
                }
            }
        }


    }

    fun photoSelect(view : View){

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            //izin almamışız
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)

        }

        else{
            //izin almışız

            val galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent,2)
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if(requestCode == 1){
            if(grantResults.size>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //izin verilince yapılacaklar

                val galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent,2)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode==2 && resultCode == Activity.RESULT_OK && data !=null){

            selectedPhoto = data.data

            if(Build.VERSION.SDK_INT >=28 ){

                val source = ImageDecoder.createSource(this.contentResolver,selectedPhoto!!)
                selectedBitmap=ImageDecoder.decodeBitmap(source)
                imageView.setImageBitmap(selectedBitmap)
            }

            if(selectedPhoto != null){

                selectedBitmap =MediaStore.Images.Media.getBitmap(this.contentResolver,selectedPhoto)
                imageView.setImageBitmap(selectedBitmap)

            }

        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}