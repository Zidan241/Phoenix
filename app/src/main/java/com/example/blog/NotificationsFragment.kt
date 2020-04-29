package com.example.blog


import android.app.Notification
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.blog.databinding.FragmentNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.single_notification.view.*

/**
 * A simple [Fragment] subclass.
 */
class NotificationsFragment : Fragment() {

    var fbstore = FirebaseFirestore.getInstance()

    var fbAuth = FirebaseAuth.getInstance()

    var user_id: String = fbAuth.currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentNotificationsBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_notifications, container , false)

        val adapter = GroupAdapter<ViewHolder>()

        var ReqsQuery:Query= fbstore.collection("Users/"+user_id+"/FriendRequests")

       ReqsQuery.addSnapshotListener(this!!.activity!!){ querySnapshot, firebaseFirestoreException ->
            if(querySnapshot!=null && !querySnapshot.isEmpty){
                for (dc in querySnapshot.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        var notf: Friend_Request = dc.document.toObject(Friend_Request::class.java)
                        adapter.add(NotificationItem(notf))
                    }
                }
                binding.notificationsView.adapter=adapter
            }
            }

        return binding.root
    }


}
class NotificationItem(var n:Friend_Request):Item<ViewHolder>(){
    var fbAuth = FirebaseAuth.getInstance()
    var user_id: String = fbAuth.currentUser!!.uid
    override fun getLayout(): Int {
        return R.layout.single_notification
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.notification_desc.text = n.name+ "\n has sent you a Friend Request"
        Glide.with(viewHolder.root.context).load(n.image).into(viewHolder.itemView.notification_type)
        viewHolder.itemView.req_accept_btn.setOnClickListener {

        }

    }

}
class Friend()
