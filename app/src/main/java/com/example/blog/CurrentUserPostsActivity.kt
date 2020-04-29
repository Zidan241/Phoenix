package com.example.blog

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_current_user_posts.*

class CurrentUserPostsActivity : AppCompatActivity() {


    var fbstore = FirebaseFirestore.getInstance()
    var fbAuth = FirebaseAuth.getInstance()
    var user_id: String = fbAuth.currentUser!!.uid
    var post_list = ArrayList<Post>()
    var user_list = ArrayList<User?>()
    var current_user:User?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_current_user_posts)
        var postRecyclerAdapter = PostRecyclerAdapter(post_list,user_list,true)
        current_user_posts_view.adapter = postRecyclerAdapter

            setSupportActionBar(my_posts_bar)

            supportActionBar?.setTitle("My Posts")

            supportActionBar?.setDisplayHomeAsUpEnabled(true)


        if (fbAuth.currentUser != null){

            fbstore.collection("Users").document(user_id!!).get().addOnCompleteListener {
                    task->
                if(task.isSuccessful){
                    current_user = task.result?.toObject(User::class.java)

                    var firstQuery : Query = fbstore
                        .collection("Posts")
                        .orderBy("timestamp", Query.Direction.DESCENDING)


                    firstQuery.addSnapshotListener (this) { querySnapshot, firebaseFirestoreException ->


                        if (querySnapshot != null&&!querySnapshot.isEmpty) {
                            for (dc in querySnapshot.documentChanges) {
                                if (dc.type == DocumentChange.Type.ADDED) {

                                    val blogPostId: String = dc.document.id
                                    var post: Post = dc.document.toObject(Post::class.java).withId(blogPostId)

                                    if(post.user_id==user_id) {

                                        user_list.add(current_user)
                                        post_list.add(post)

                                        postRecyclerAdapter.notifyDataSetChanged()
                                    }

                                }
                            }

                        }
                    }

                }else{
                    Toast.makeText(PostRecyclerAdapter.context, "fbStore Retrieve Error:" + task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item?.itemId == android.R.id.home){
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        var HomeIntent: Intent = Intent(this, HomeActivity::class.java)
        startActivity(HomeIntent)
        finish()
    }
}
