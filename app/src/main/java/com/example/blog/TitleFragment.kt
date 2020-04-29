package com.example.blog


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.blog.databinding.FragmentTitleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class TitleFragment : Fragment() {

    var fbstore = FirebaseFirestore.getInstance()

    var fbAuth = FirebaseAuth.getInstance()
    var currentUser: FirebaseUser?= FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        val binding: FragmentTitleBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_title, container , false)



        binding.playButton.setOnClickListener{

            Glide.with(this!!.context!!).load(R.drawable.phoenix_gif).into(binding.titleImage)

            Handler().postDelayed({
                view?.findNavController()?.navigate(R.id.action_titleFragment_to_loginFragment)
            }, 2000)
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if(currentUser!=null){
            view?.findNavController()?.navigate(R.id.action_titleFragment_to_loadingFragment)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        (activity as MainActivity).onTitle  = true

    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).onTitle  = true
    }


    override fun onPause() {
        super.onPause()
        (activity as MainActivity).onTitle  = false
    }






}
