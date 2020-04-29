package com.example.blog

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.blog.databinding.ActivityMainBinding
import android.content.DialogInterface
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var onTitle : Boolean =  false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)



    }

    override fun onBackPressed() {


        if(onTitle){
            val ab = AlertDialog.Builder(this@MainActivity)
            ab.setTitle("Warning")
            ab.setMessage("are you sure to exit?")
            ab.setPositiveButton("yes", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()

                this.finish()

            })
            ab.setNegativeButton("no",
                DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })

            ab.show()
        }
        else{
            super.onBackPressed()
        }

    }

}
