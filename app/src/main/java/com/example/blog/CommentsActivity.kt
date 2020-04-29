package com.example.blog

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.blog.databinding.ActivityCommentsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_comments.*

class CommentsActivity : AppCompatActivity() {


    var fbstore = FirebaseFirestore.getInstance()

    var fbAuth = FirebaseAuth.getInstance()

    var user_id: String = fbAuth.currentUser!!.uid

    var comments_list = ArrayList<Comment>()
    var user_list = ArrayList<User?>()

    var commentsRecyclerAdapter = CommentsRecyclerAdapter(comments_list,user_list)


    private lateinit var binding: ActivityCommentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_comments)



        setSupportActionBar(binding.commentBar)
        supportActionBar?.setTitle("Comments")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.commentsView.setHasFixedSize(true)
        binding.commentsView.adapter = commentsRecyclerAdapter

        val post_id = intent.getStringExtra("post_id")

        var commentsQuery : Query = fbstore
            .collection("Posts/"+post_id+"/Comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        commentsQuery.addSnapshotListener (this) { querySnapshot, firebaseFirestoreException ->

            if (querySnapshot != null) {
                if(!querySnapshot.isEmpty){
                    for (dc in querySnapshot.documentChanges) {

                        if (dc.type == DocumentChange.Type.ADDED) {

                            var comment: Comment = dc.document.toObject(Comment::class.java)
                            var comment_user : String = dc.document.getString("user_id").toString()

                            fbstore.collection("Users").document(comment_user).get().addOnCompleteListener {
                                    task->
                                if(task.isSuccessful){
                                    var user: User? = task.result?.toObject(User::class.java)

                                    comments_list.add(comment)
                                    user_list.add(user)

                                    commentsRecyclerAdapter.notifyDataSetChanged()
                                }else{
                                    Toast.makeText(this, "fbStore Retrieve Error:" + task.exception?.message, Toast.LENGTH_LONG).show()
                                }

                            }
                        }
                    }
                    binding.commentsView.scrollToPosition(comments_list.size-1)
                }
            }

        }

        binding.sendComment.setOnClickListener {

            val commentMessage : String = binding.addComment.text.toString()

            if(!commentMessage.isEmpty()){
                progressBar6.visibility = View.VISIBLE
                binding.sendComment.isEnabled=false
                binding.commentBar.isEnabled=false

                val comment = Comment(commentMessage,user_id,com.google.firebase.Timestamp.now())

                fbstore.collection("Posts/"+post_id+"/Comments").add(comment).addOnCompleteListener {
                    task->
                    if(task.isSuccessful){
                        Toast.makeText(this, "Comment Sent", Toast.LENGTH_SHORT).show()
                        binding.addComment.setText("")
                        binding.addComment.hideKeyboard()
                        progressBar6.visibility = View.INVISIBLE
                        binding.commentsView.scrollToPosition(comments_list.size-1)
                        binding.sendComment.isEnabled=true
                        binding.commentBar.isEnabled=true

                    }else{
                        Toast.makeText(this, "Error Sending The Comment: " + task.exception?.message, Toast.LENGTH_SHORT).show()
                        binding.addComment.hideKeyboard()
                        progressBar6.visibility = View.INVISIBLE
                        binding.sendComment.isEnabled=true
                        binding.commentBar.isEnabled=true
                    }

                }

            }else{
                Toast.makeText(this, "text is left empty", Toast.LENGTH_SHORT).show()
            }


        }

    }
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
class Comment(val message:String = "", val user_id:String="",var timestamp:com.google.firebase.Timestamp?=null){}
