package com.example.blog

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.BoringLayout
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blog.databinding.ActivitySearchBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.single_search_user.view.*
import java.util.*
import kotlin.collections.ArrayList

class SearchActivity : AppCompatActivity() {

    var fbstore = FirebaseFirestore.getInstance()

    var fbAuth = FirebaseAuth.getInstance()

    var user_id: String = fbAuth.currentUser!!.uid

    lateinit var current_user:User

    companion object{
        var isPosts: Boolean = true
        var fbAuth = FirebaseAuth.getInstance()
        var user_id: String = fbAuth.currentUser!!.uid
        lateinit var t :Activity
    }

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var isPosts: Boolean = true
        t=this

        fbstore.collection("Users").document(user_id).get().addOnSuccessListener {document->
            current_user = document.toObject(User::class.java)!!
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)

        binding.postsOrUsers.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.search_nav_posts -> {
                    isPosts = true
                    forPosts("")

                }
                R.id.search_nav_users -> {
                    isPosts = false
                    forUsers("")
                }
            }
            return@setOnNavigationItemSelectedListener true
        }

        binding.startSearch.setOnClickListener {
            var str:String= binding.searchBar.text.toString()
            binding.searchBar.hideKeyboard()
            if(isPosts){
                forPosts(str)
            }
            else{
                forUsers(str)
            }
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }



    private fun forUsers(str: String) {
        var UserQuery : Query = fbstore
            .collection("Users")
            .orderBy("name",Query.Direction.ASCENDING)

        val adapter = GroupAdapter<ViewHolder>()

        UserQuery.addSnapshotListener (this) { querySnapshot, firebaseFirestoreException ->

            if (querySnapshot != null&&!querySnapshot.isEmpty) {
                for (dc in querySnapshot.documentChanges) {

                    if (dc.type == DocumentChange.Type.ADDED) {
                        var user: User = dc.document.toObject(User::class.java)
                        if (dc.document.id !=user_id){
                            if (str == "") {
                                adapter.add(SearchItem(user, dc.document.id,current_user))
                            } else {
                                if (user.name.contains(str, true)) {
                                    adapter.add(SearchItem(user, dc.document.id,current_user))
                                }
                            }
                        }
                    }
                }
                binding.searchResults.adapter = adapter
            }
        }
    }

    private fun forPosts(str: String) {
        var post_list = ArrayList<Post>()
        var user_list = ArrayList<User?>()
        var postRecyclerAdapter =  PostRecyclerAdapter(post_list, user_list,false)
        binding.searchResults.adapter = postRecyclerAdapter

        var PostQuery : Query = fbstore
            .collection("Posts")
            .orderBy("timestamp",Query.Direction.DESCENDING)

        PostQuery.addSnapshotListener (this) { querySnapshot, firebaseFirestoreException ->

            if (querySnapshot != null&&!querySnapshot.isEmpty) {
                for (dc in querySnapshot.documentChanges) {

                    if (dc.type == DocumentChange.Type.ADDED) {
                        val blogPostId: String = dc.document.id
                        var post: Post = dc.document.toObject(Post::class.java).withId(blogPostId)
                        var post_user_id:String? = dc.document.getString("user_id")

                        fbstore.collection("Users").document(post_user_id!!).get().addOnCompleteListener {
                                task->
                            if(task.isSuccessful){
                                var user: User? = task.result?.toObject(User::class.java)
                                if(str == "") {
                                    user_list.add(user)
                                    post_list.add(post)

                                    postRecyclerAdapter.notifyDataSetChanged()
                                }else{
                                    if(post.information.contains(str,true)){
                                        user_list.add(user)
                                        post_list.add(post)

                                        postRecyclerAdapter.notifyDataSetChanged()
                                    }
                                }
                            }else{
                                Toast.makeText(this, "fbStore Retrieve Error:" + task.exception?.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }

}

class SearchItem(val user : User , val id:String,val curr:User): Item<ViewHolder>(){
    override fun getLayout(): Int {
            return R.layout.single_search_user
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.user_name.text = user.name
            // Picasso.get().load(user.image).into(viewHolder.itemView.User_photo)
            val placeholderRequest = RequestOptions()
            placeholderRequest.placeholder(R.drawable.default_icon)
            Glide.with(viewHolder.root.context).applyDefaultRequestOptions(placeholderRequest)
                .load(user.image).thumbnail(Glide.with(viewHolder.root.context).load(user.thumb))
                .into(viewHolder.itemView.User_photo)
        viewHolder.itemView.Add_user_as_friend_btn.setOnClickListener {
            var notf : Friend_Request = Friend_Request(com.google.firebase.Timestamp.now(),curr.name,curr.image)

            FirebaseFirestore.getInstance().collection("Users/"+ SearchActivity.user_id+"/Friends").get()
                .addOnSuccessListener {
                    if(it==null || it.isEmpty){
                        FirebaseFirestore.getInstance().collection("Users/"+id+"/FriendRequests").document(SearchActivity.user_id).set(notf)
                        Toast.makeText(SearchActivity.t, "Friend Request Sent", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        var flag = false
                        for (dc in it.documentChanges) {
                            if (dc.type == DocumentChange.Type.ADDED) {
                                if(dc.document.id==id){
                                    Toast.makeText(SearchActivity.t, "This User is already your friend", Toast.LENGTH_SHORT).show()
                                    flag=true
                                    break
                                }
                            }
                        }
                        if(!flag){
                            FirebaseFirestore.getInstance().collection("Users/"+id+"/FriendRequests").document(SearchActivity.user_id).set(notf)
                            Toast.makeText(SearchActivity.t, "Friend Request Sent", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(SearchActivity.t, "fbStore Retrieve Error", Toast.LENGTH_SHORT).show()
                }
        }
    }

}
class Friend_Request(var timestamp:com.google.firebase.Timestamp?=null,var name:String="",var image:String="")

