package com.example.blog

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import com.example.blog.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import android.content.pm.PackageManager
import android.content.ComponentName
import android.os.Handler
import android.view.inputmethod.EditorInfo

import android.widget.SearchView
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_post.*


class HomeActivity : AppCompatActivity() {

    var fbstore = FirebaseFirestore.getInstance()

    var fbAuth = FirebaseAuth.getInstance()

    private lateinit var binding: ActivityHomeBinding


    var postF:PostFragment = PostFragment()
    var notificationF:NotificationsFragment = NotificationsFragment()
    var accountF:AccountFragment = AccountFragment()
    var secondClickPost : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_home)

        // limiting the number of entries in the fragment backStack for this activity

        val fm = supportFragmentManager

        if (fm.backStackEntryCount > 10) {
           fm.popBackStack() // remove one (you can also remove more)
        }

        setSupportActionBar(binding.mainBar)

        supportActionBar?.setTitle("Phoenix")

        binding.addPostButton.setOnClickListener{

            var newPostIntent: Intent = Intent(this, NewPostActivity::class.java)
            startActivity(newPostIntent)
            //finish()
        }
        initializeFragment()

        binding.mainBottomNav.setOnNavigationItemSelectedListener {it->
            when(it.itemId){
                R.id.bottom_home_nav -> {
                    replaceFragment(postF)
                }
                R.id.bottom_account_nav -> {
                    replaceFragment(accountF)
                }
                R.id.bottom_notifications_nav -> {
                    replaceFragment(notificationF)
                }
            }
            return@setOnNavigationItemSelectedListener true

        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

   /* override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)

        if(menu!=null){
            var searchItem:MenuItem = menu.findItem(R.id.action_search_button)
            var searchview: SearchView = searchItem.actionView as SearchView

            searchview.imeOptions = EditorInfo.IME_ACTION_DONE

            searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextChange(p0: String?): Boolean {
                    postF.postRecyclerAdapter.filter.filter(p0)
                    return false
                }
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    return false
                }
            })
        }

        return true
    }*/



    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_logout_button -> {

                FirebaseAuth.getInstance().signOut()

                var MainIntent: Intent = Intent(this, MainActivity::class.java)
                startActivity(MainIntent)
                finish()

                return true
            }
            R.id.action_setting_button ->{

                var AccountSettingsIntent : Intent = Intent(this, AccountSettingsActivity::class.java)
                startActivity(AccountSettingsIntent)
                finish()

                return true
            }
            R.id.action_search_button ->{
                var SearchIntent : Intent = Intent(this, SearchActivity::class.java)
                startActivity(SearchIntent)

                return true
            }

        }
        return false

    }

    private fun replaceFragment(fragment: Fragment){
        var fragmentTransaction : FragmentTransaction? = supportFragmentManager?.beginTransaction()

        if(fragment == postF) {

            if (secondClickPost) {
                fragmentTransaction?.remove(postF)
                postF=PostFragment()
                fragmentTransaction?.add(R.id.main_container , postF)
              // fragmentTransaction?.detach(postF)?.attach(postF)
                //postF.post_list_view.scrollToPosition(0)

            } else {
                fragmentTransaction?.hide(accountF)
                fragmentTransaction?.hide(notificationF)
                secondClickPost = true
                Toast.makeText(this, "click on the home icon again to refresh page", Toast.LENGTH_SHORT).show()
                Handler().postDelayed( { secondClickPost = false }, 2000)
            }

        }

        if(fragment ==accountF){

            fragmentTransaction?.hide(postF)
            fragmentTransaction?.hide(notificationF)

        }

        if(fragment == notificationF){

            fragmentTransaction?.hide( postF)
            fragmentTransaction?.hide(accountF)

        }
        fragmentTransaction?.show(fragment)

        //fragmentTransaction?.replace(R.id.main_container , fragment)
        //fragmentTransaction?.addToBackStack(null)

        fragmentTransaction?.commit()
    }

    private fun initializeFragment() {

        val fragmentTransaction = supportFragmentManager.beginTransaction()

        fragmentTransaction.add(R.id.main_container, postF)
        fragmentTransaction.add(R.id.main_container, notificationF)
        fragmentTransaction.add(R.id.main_container,accountF)

        fragmentTransaction.hide(notificationF)
        fragmentTransaction.hide(accountF)

        fragmentTransaction.commit()

    }



    override fun onBackPressed() {

       // if(fragmentManager.backStackEntryCount==0){
            val ab = AlertDialog.Builder(this@HomeActivity)
            ab.setTitle("Warning")
            ab.setMessage("are you sure to exit?")
            ab.setPositiveButton("yes", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()

                this.finish()
            })
            ab.setNegativeButton("no", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
            ab.show()

    }

}
