package com.example.blog


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.blog.databinding.FragmentAccountBinding


class AccountFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentAccountBinding= DataBindingUtil.inflate(inflater,R.layout.fragment_account, container , false)

       // Glide.with(this).load(R.drawable.phoenix_gif).into(binding.appLogoAccount)
        binding.myPostsBtn.setOnClickListener {
            var MyPostsIntent: Intent = Intent(activity, CurrentUserPostsActivity::class.java)
            startActivity(MyPostsIntent)
            activity?.finish()
        }
        binding.addNewFriendBtn.setOnClickListener {
            var SearchIntent: Intent = Intent(activity, SearchActivity::class.java)
            startActivity(SearchIntent)
        }


        return binding.root
    }


}
