package com.example.blog

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView


class CommentsRecyclerAdapter: RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {


    var fbstore = FirebaseFirestore.getInstance()
    var fbAuth = FirebaseAuth.getInstance()

    var comments_list:ArrayList<Comment>
    var user_list : ArrayList<User?>

    constructor(comments_list: ArrayList<Comment>,user_list : ArrayList<User?>){
        this.comments_list = comments_list
        this.user_list = user_list
    }

    companion object {
        public lateinit var context: Context
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view:View = LayoutInflater.from(parent.context).inflate(com.example.blog.R.layout.comment_list_item,parent,false)
        context = parent.context

        return CommentsRecyclerAdapter.ViewHolder(view)

    }

    override fun getItemCount(): Int {
        if(comments_list != null) {

            return comments_list.size

        } else {

            return 0

        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)


                var u = user_list.get(position)
                holder.setUserName(u?.name)
                holder.setUserImg(u?.image,u?.thumb)

        val commentMessage = comments_list.get(position).message
        holder.set_comment_text(commentMessage)
    }



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        lateinit var comment_message: TextView
        lateinit var userImg: CircleImageView
        lateinit var userName: TextView
        public fun set_comment_text(message:String){
            comment_message = itemView.findViewById(R.id.comments_message)
            comment_message.setText(message)
        }
        public fun setUserImg(downloadUrl:String?,thumbUrl:String?){
            val placeholderRequest = RequestOptions()
            placeholderRequest.placeholder(R.drawable.default_icon)

            userImg = itemView.findViewById(R.id.comments_image)

            Glide.with(context).applyDefaultRequestOptions(placeholderRequest).load(downloadUrl).thumbnail(Glide.with(context).load(thumbUrl)).into(userImg)
        }
        public fun setUserName(name:String?){
            userName = itemView.findViewById(R.id.comments_username)
            userName.setText(name)
        }


    }
}
