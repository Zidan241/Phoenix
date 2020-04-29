package com.example.blog


import android.app.Activity
import android.os.Bundle
import android.text.BoringLayout
import android.util.Log

import android.view.*
import android.widget.Adapter
import android.widget.Toast
import androidx.fragment.app.Fragment

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.blog.databinding.FragmentPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage


class PostFragment : Fragment() {




    var fbstore = FirebaseFirestore.getInstance()

    var fbAuth = FirebaseAuth.getInstance()

    var user_id: String = fbAuth.currentUser!!.uid


        var post_list = ArrayList<Post>()
        var user_list = ArrayList<User?>()
        lateinit var postRecyclerAdapter :PostRecyclerAdapter



    lateinit var lastVisible:DocumentSnapshot

    var isFirstPageFirstLoad : Boolean = true

    var PostsFinished : Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentPostBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_post, container, false)

       // var postRecyclerAdapter = PostRecyclerAdapter(post_list)
        postRecyclerAdapter =  PostRecyclerAdapter(post_list, user_list,false)
        binding.postListView.adapter = postRecyclerAdapter

       //binding.postListView.setDrawingCacheEnabled(true)
       // binding.postListView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH)

        if (fbAuth.currentUser != null){

            binding.postListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    var reachedBottom :Boolean = !recyclerView.canScrollVertically(1)

                    if(reachedBottom){

                        if(PostsFinished){

                            Toast.makeText(activity, "No More Posts :(", Toast.LENGTH_SHORT).show()

                        }
                        else {

                            Toast.makeText(activity, "LoadingMore...", Toast.LENGTH_SHORT).show()

                            loadMorePost()
                        }

                    }

                }
            })

          /*  var allQuery : Query = fbstore
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
            allQuery.addSnapshotListener (activity as Activity
            ) { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot != null&&!querySnapshot.isEmpty) {
                        for (dc in querySnapshot.documentChanges) {
                            if (dc.type == DocumentChange.Type.ADDED) {
                                val blogPostId: String = dc.document.id
                                var post: Post = dc.document.toObject(Post::class.java).withId(blogPostId)
                                            full_list.add(post)
                            }
                        }
                }
            }*/



            var firstQuery : Query = fbstore
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)

        firstQuery.addSnapshotListener (activity as Activity
        ) { querySnapshot, firebaseFirestoreException ->


            if (querySnapshot != null&&!querySnapshot.isEmpty) {
            if (isFirstPageFirstLoad) {
                lastVisible = querySnapshot!!.documents[querySnapshot.size() - 1]
            }
                if (!querySnapshot.isEmpty){
                    for (dc in querySnapshot.documentChanges) {

                        if (dc.type == DocumentChange.Type.ADDED) {

                            val blogPostId: String = dc.document.id
                            var post: Post = dc.document.toObject(Post::class.java).withId(blogPostId)

                            var post_user_id:String? = dc.document.getString("user_id")

                            fbstore.collection("Users").document(post_user_id!!).get().addOnCompleteListener {
                                task->
                                if(task.isSuccessful){
                                    var user: User? = task.result?.toObject(User::class.java)

                                        if (isFirstPageFirstLoad) {
                                            user_list.add(user)
                                            post_list.add(post)
                                        } else {
                                            user_list.add(0, user)
                                            post_list.add(0, post)
                                        }
                                        postRecyclerAdapter.notifyDataSetChanged()
                                }else{
                                    Toast.makeText(PostRecyclerAdapter.context, "fbStore Retrieve Error:" + task.exception?.message, Toast.LENGTH_LONG).show()
                                }

                            }

                        }
                    }

                isFirstPageFirstLoad = false
            }
            }
        }

        }


        return binding.root

    }





    private fun loadMorePost(){




            var nextQuery : Query = fbstore
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(10)



            nextQuery.addSnapshotListener(activity as Activity) { querySnapshot, firebaseFirestoreException ->

                if (querySnapshot != null) {
                    if (!querySnapshot.isEmpty){

                        lastVisible = querySnapshot!!.documents[querySnapshot.size() - 1]


                        for (dc in querySnapshot.documentChanges) {

                            if (dc.type == DocumentChange.Type.ADDED) {

                                val blogPostId : String = dc.document.id
                                var post: Post = dc.document.toObject(Post::class.java).withId(blogPostId)

                                var post_user_id:String? = dc.document.getString("user_id")

                                fbstore.collection("Users").document(post_user_id!!).get().addOnCompleteListener {
                                        task->
                                    if(task.isSuccessful){
                                        var user: User? = task.result?.toObject(User::class.java)
                                            user_list.add(user)
                                            post_list.add(post)

                                        postRecyclerAdapter.notifyDataSetChanged()


                                    }else{
                                        Toast.makeText(PostRecyclerAdapter.context, "fbStore Retrieve Error:" + task.exception?.message, Toast.LENGTH_LONG).show()
                                    }

                                }

                            }
                        }
                    }
                    else{
                        PostsFinished = true
                        Toast.makeText(activity, "No More Posts :(", Toast.LENGTH_SHORT).show()

                    }
                }
            }
    }



}
