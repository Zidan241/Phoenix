package com.example.blog

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountSettingsActivity : AppCompatActivity() {

    var fbAuth = FirebaseAuth.getInstance()
    var user_id: String = fbAuth.currentUser!!.uid
    var fbstore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)
    }

    private var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {

            val ab = AlertDialog.Builder(this@AccountSettingsActivity)
            ab.setTitle("Warning")
            ab.setMessage("are you sure to exit?")
            ab.setPositiveButton("yes", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity()
                }else{
                    ActivityCompat.finishAffinity(this);
                }

            })
            ab.setNegativeButton("no",
                DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })

            ab.show()
        }

        else {

            fbstore.collection("Users").document(user_id).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    if (!task.getResult()!!.exists()) {
                        Toast.makeText(this, "name and image must exist\n- - - - press again to exit - - - -", Toast.LENGTH_SHORT).show()
                    } else {
                        var HomeIntent: Intent = Intent(this, HomeActivity::class.java)
                        startActivity(HomeIntent)
                        finish()
                    }

                }

            }
            this.doubleBackToExitPressedOnce = true
            Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)

        }

    }

    public fun backFn(){
        super.onBackPressed()
    }
}
