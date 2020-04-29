package com.example.blog


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class LoadingFragment : Fragment() {

    var fbstore = FirebaseFirestore.getInstance()

    var fbAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var currentUser: FirebaseUser?= FirebaseAuth.getInstance().currentUser
        if(currentUser!=null){

            var user_id: String = fbAuth.currentUser!!.uid
            fbstore.collection("Users").document(user_id).get().addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    if (!task.getResult()!!.exists()) {

                        var AccountSettingsIntent : Intent = Intent(activity, AccountSettingsActivity::class.java)
                        Handler().postDelayed({
                            startActivity(AccountSettingsIntent)
                            activity?.finish()
                            Toast.makeText(activity, "name and image must exist", Toast.LENGTH_LONG).show()
                        }, 1000)


                    }
                    else{
                        var HomeIntent: Intent = Intent(activity, HomeActivity::class.java)
                        Handler().postDelayed({
                            startActivity(HomeIntent)
                            (activity as AppCompatActivity).finish()
                        }, 1000)
                    }
                }else{
                    Toast.makeText(activity, "fbStore Retrieve Error:" + task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }

        }

        return inflater.inflate(R.layout.fragment_loading, container, false)



    }


}
