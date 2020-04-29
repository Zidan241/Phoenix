package com.example.blog


import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
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
import com.example.blog.databinding.FragmentLoginBinding
import com.google.android.gms.common.internal.ConnectionErrorMessages
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : Fragment() {




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentLoginBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_login,container,false)

        binding.Login.setOnClickListener {

            var emailString : String = binding.LoginEmail.text.toString()
            var passString : String = binding.LoginPassword.text.toString()

            if(!TextUtils.isEmpty(emailString)&&!TextUtils.isEmpty(passString)){
                binding.progressBar.visibility = View.VISIBLE
                binding.Login.isEnabled = false
                binding.newAccount.isEnabled = false

                FirebaseAuth.getInstance().signInWithEmailAndPassword(emailString,passString).addOnCompleteListener(
                    OnCompleteListener { task ->
                        if(task.isSuccessful) {

                            var HomeIntent: Intent = Intent(activity, HomeActivity::class.java)
                            startActivity(HomeIntent)
                            activity?.finish()


                        } else {
                            Toast.makeText( activity , "Error" + task.exception.toString(), Toast.LENGTH_LONG).show()
                            binding.progressBar.visibility = View.INVISIBLE
                            binding.Login.isEnabled = true
                            binding.newAccount.isEnabled = true
                        }
                    }
                )

                }
            }

        binding.newAccount.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_loginFragment_to_signup)
        }




        return binding.root
    }


}
