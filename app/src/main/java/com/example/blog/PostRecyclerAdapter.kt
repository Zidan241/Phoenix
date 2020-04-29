package com.example.blog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.constraintlayout.widget.Placeholder
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.target.SimpleTarget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage


class PostRecyclerAdapter :RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder>{
    //implements Filterable

    var fbstore = FirebaseFirestore.getInstance()
    var fbAuth = FirebaseAuth.getInstance()
    var current_user_id: String = fbAuth.currentUser!!.uid



     var post_list : ArrayList<Post>
     var user_list : ArrayList<User?>
     var isMyPost : Boolean

    companion object {
        public lateinit var context: Context
    }

    constructor(post_list: ArrayList<Post>,user_list:ArrayList<User?>,bool:Boolean){
        this.post_list = post_list
        this.user_list=user_list
        isMyPost = bool

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        var view:View = LayoutInflater.from(parent.context).inflate(R.layout.single_list_item,parent,false)
        context = parent.context
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {


        return post_list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        holder.setIsRecyclable(false)

        var desc_data :String = post_list.get(position).information
        holder.setDescText(desc_data)

        var imageUrl : String = post_list.get(position).image_url
        var thumbUrl : String = post_list.get(position).thumbnail_url
        holder.setPostImg(imageUrl,thumbUrl)

        var user_id : String = post_list.get(position).user_id


        if(user_id.equals(current_user_id)){
            holder.delete_btn.isEnabled = true
            holder.delete_btn.visibility = View.VISIBLE

        }




               holder.setUserName(user_list.get(position)?.name)
               holder.setUserImg(user_list.get(position)?.image,user_list.get(position)?.thumb)




        var date : Date = post_list.get(position).timestamp!!.toDate()
        var millisecond:Long = date.time
        var dateString : String = android.text.format.DateFormat.format("dd/MM/yyyy", Date(millisecond)).toString()

        holder.setDate(dateString)


        var blogPostId : String = post_list.get(position).BlogPostId

        fbstore.collection("Posts/"+blogPostId+"/Comments").addSnapshotListener { querySnapshot, firebaseFirestoreException ->

            if (querySnapshot != null) {
                if(!querySnapshot.isEmpty){
                    holder.updateCommentsCount(querySnapshot.size())
                }
            }

        }

        fbstore.collection("Posts/"+blogPostId+"/Likes").addSnapshotListener { querySnapshot, firebaseFirestoreException ->

            if (querySnapshot != null) {
                if(!querySnapshot.isEmpty){
                    holder.updateLikesCount(querySnapshot.size())
                }else{
                    holder.updateLikesCount(0)
                }
            }

        }

        fbstore.collection("Posts/"+blogPostId+"/Likes").document(current_user_id).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

            if (documentSnapshot != null) {
                if(documentSnapshot.exists()){
                    holder.postLikeBtn.setImageResource(R.mipmap.action_like_accent)
                }else{
                    holder.postLikeBtn.setImageResource(R.mipmap.action_like_grey)
                }
            }

        }

        holder.postLikeBtn.setOnClickListener {

            fbstore.collection("Posts/"+blogPostId+"/Likes").document(current_user_id).get().addOnCompleteListener {
                task->

                if(task.result!!.exists()){
                    fbstore.collection("Posts/"+blogPostId+"/Likes").document(current_user_id).delete()
                }else{
                    var likes = Likes(com.google.firebase.Timestamp.now())
                    fbstore.collection("Posts/"+blogPostId+"/Likes").document(current_user_id).set(likes)
                }

            }
        }
        holder.postCommentsBtn.setOnClickListener {

            var commentIntent: Intent = Intent(context, CommentsActivity::class.java)
            commentIntent.putExtra("post_id",blogPostId)
            context.startActivity(commentIntent)
        }

        if(isMyPost) {
            holder.delete_btn.setOnClickListener {

                fbstore.collection("Posts").document(blogPostId).delete().addOnSuccessListener {
                    holder.setDeletePost()
                    Toast.makeText(context, "Post Removed Successfully", Toast.LENGTH_LONG).show()
                }


            }
        }
        else{
            holder.delete_btn.setText("")
        }

    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var descView:TextView = itemView.findViewById(R.id.post_information)
         var postImg:ImageView = itemView.findViewById(R.id.post_main_img)
       var userImg:CircleImageView = itemView.findViewById(R.id.post_user_img)
         var userName:TextView = itemView.findViewById(R.id.post_username_text)
         var dateText:TextView = itemView.findViewById(R.id.post_date)
         var postLikeCount:TextView = itemView.findViewById(R.id.blog_like_count)
        var postLikeBtn:ImageView = itemView.findViewById(R.id.blog_like_btn)
        var postCommentsBtn:ImageView = itemView.findViewById(R.id.blog_comment_icon)
         var postCommentCount:TextView =  itemView.findViewById(R.id.blog_comment_count)

        var delete_btn : Button = itemView.findViewById(R.id.delete_post_btn)
        public fun setDeletePost(){

            userImg.setImageResource(R.color.LikeBtnGray)
            descView.setText("POST DELETED")
            descView.gravity = Gravity.CENTER
            postImg.setImageResource(R.color.LikeBtnGray)
            userName.setText("")
            dateText.setText("")
            postCommentCount.setText("0 Comments")
            postLikeCount.setText("0 Likes")
            postLikeBtn.isEnabled = false
            postLikeBtn.setImageResource(R.mipmap.action_like_grey)
            postCommentsBtn.isEnabled = false
        }



         fun setDescText(text:String){

            descView.setText(text)
        }


         fun setPostImg(downloadUrl:String,thumbUrl:String){


            val placeholderRequest = RequestOptions()
            placeholderRequest.placeholder(R.drawable.add_photo_img)

            Glide.with(context).applyDefaultRequestOptions(placeholderRequest).load(downloadUrl).thumbnail(Glide.with(context).load(thumbUrl)).into(postImg)
        }
         fun setUserImg(downloadUrl:String?,thumbUrl:String?){
            val placeholderRequest = RequestOptions()
            placeholderRequest.placeholder(R.drawable.default_icon)



            Glide.with(context).applyDefaultRequestOptions(placeholderRequest).load(downloadUrl).thumbnail(Glide.with(context).load(thumbUrl)).into(userImg)
        }
         fun setUserName(name:String?){

            userName.setText(name)
        }
         fun setDate(date:String?){

            dateText.setText(date)
        }
         fun updateLikesCount(count:Int){

            postLikeCount.setText("$count Likes")
        }
         fun updateCommentsCount(count:Int){

            postCommentCount.setText("$count Comments")
        }
    }

   /* override fun getFilter(): Filter {
        return postFilter
    }
    private val postFilter = object : Filter(){
        override fun performFiltering(p0: CharSequence?): FilterResults {
            var Filtered_list = ArrayList<Post>()

            if(p0==null||p0.length == 0){
                Filtered_list.addAll(Full_list)
                //Log.d("TITO241",Full_list.toString())
            }else{
                var filteredPattern:String = p0.toString().toLowerCase().trim()

                for(post in post_list){
                    if(post.information.toLowerCase().contains(filteredPattern)){
                        Filtered_list.add(post)
                    }
                }

            }
            var results = FilterResults()
            results.values = Filtered_list
            return results
        }

        override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
            post_list.clear()
            if(p1!=null)
            post_list.addAll(p1.values as ArrayList<Post>)
            notifyDataSetChanged()
        }


    }*/
}
class Likes (var timestamp:com.google.firebase.Timestamp?=null)
