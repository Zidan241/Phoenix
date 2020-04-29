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
import com.example.blog.databinding.FragmentSignupBinding
import com.example.blog.databinding.FragmentTitleBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

class Signup : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentSignupBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_signup, container , false)

        binding.createAccBtn.setOnClickListener {
            var emailString : String = binding.emailReg.text.toString()
            var passString : String = binding.passReg.text.toString()
            var confirmPass : String = binding.confirmPassBtn.text.toString()

            if(!TextUtils.isEmpty(emailString)&&!TextUtils.isEmpty(passString)&&!TextUtils.isEmpty(confirmPass)){

                if(confirmPass.equals(passString)){

                    binding.progressBar2.visibility = View.VISIBLE
                    binding.cancelBtn.isEnabled = false
                    binding.createAccBtn.isEnabled =false

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailString, passString).addOnCompleteListener(
                        OnCompleteListener { task ->
                            if(task.isSuccessful) {

                                var AccountSettingsIntent : Intent = Intent(activity, AccountSettingsActivity::class.java)
                                startActivity(AccountSettingsIntent)
                                activity?.finish()

                            } else {
                                Toast.makeText( activity , "Error" + task.exception.toString(), Toast.LENGTH_LONG).show()
                                binding.progressBar2.visibility = View.INVISIBLE
                                binding.cancelBtn.isEnabled = true
                                binding.createAccBtn.isEnabled =true

                            }
                        }
                    )
                }
                else{
                    Toast.makeText( activity , "Password do not match" , Toast.LENGTH_LONG).show()
                }

            }

        }

        binding.cancelBtn.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_signup_to_loginFragment)
        }


        return binding.root
    }


}
