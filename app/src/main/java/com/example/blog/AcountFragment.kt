package com.example.blog


import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.ActivityManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.blog.databinding.FragmentAcountBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import id.zelory.compressor.Compressor
import android.text.TextUtils
import android.view.MotionEvent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_acount.*
import com.example.blog.AccountSettingsActivity
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.io.File


class AcountFragment : Fragment() {


    var mainImageUri : Uri ?= null
    var thumbnailUri : Uri ?= null

    var fbAuth = FirebaseAuth.getInstance()

    var fbStorage = FirebaseStorage.getInstance().getReference()

    var fbstore = FirebaseFirestore.getInstance()

    var user_id: String = fbAuth.currentUser!!.uid

    var isChanged : Boolean = false

    var dataexists : Boolean = false



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentAcountBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_acount, container , false)


        //Log.d("TITO",activity.toString())

        (activity as AppCompatActivity).setSupportActionBar(binding.setupToolbar)

        (activity as AppCompatActivity).supportActionBar?.setTitle("Account Setup")

        binding.progressBar3.visibility = View.VISIBLE
        binding.saveProfile.isEnabled = false


        fbstore.collection("Users").document(user_id).get().addOnCompleteListener { task->

            if(task.isSuccessful){

                if(task.getResult()!!.exists()) {
                 //   Toast.makeText(activity, "Data exists", Toast.LENGTH_LONG).show()

                    dataexists = true

                    var name:String? = task.result?.getString("name")
                    var image:String? = task.result?.getString("image")
                    var thumb:String? = task.result?.getString("thumb")

                    binding.userName.setText(name)

                    val placeholderRequest = RequestOptions()
                    placeholderRequest.placeholder(R.drawable.default_icon)

                    Glide.with(this).setDefaultRequestOptions(placeholderRequest).load(image).into(binding.setupImage)

                    mainImageUri = Uri.parse(image)
                    thumbnailUri = Uri.parse(thumb)

                }

            } else{
                Toast.makeText(activity, "fbStore Retrieve Error:" + task.exception?.message, Toast.LENGTH_LONG).show()
            }

            Handler().postDelayed({

                binding.progressBar3.visibility = View.INVISIBLE
                binding.saveProfile.isEnabled = true

            }, 1000)


        }

        binding.cancelSaveBtn.setOnClickListener {

           if(dataexists){
                var HomeIntent: Intent = Intent(activity, HomeActivity::class.java)
                startActivity(HomeIntent)
                activity?.finish()
          }
           else  Toast.makeText(activity, "name and image must exist", Toast.LENGTH_LONG).show()
        }

        binding.setupImage.setOnClickListener{



            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                if(ContextCompat.checkSelfPermission(activity as AppCompatActivity,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){

                    Toast.makeText( activity , "Permission denied", Toast.LENGTH_SHORT).show()

                    ActivityCompat.requestPermissions(activity as AppCompatActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)

                } else{
                    CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start( activity as AppCompatActivity ,this)
                }

            } else{
                CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start( activity as AppCompatActivity ,this)
            }


        }
        binding.saveProfile.setOnClickListener {

            val profileName: String = binding.userName.text.toString()

            if (!TextUtils.isEmpty(profileName) && mainImageUri != null) {

            progressBar3.visibility = View.VISIBLE
                binding.saveProfile.isEnabled=false
                binding.cancelSaveBtn.isEnabled=false

            if (isChanged){

                var img_path: StorageReference = fbStorage.child("profile_images").child(user_id + ".jpg")

                img_path.putFile(mainImageUri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        var UserImgFile :File = File(mainImageUri!!.path)

                        var compressedImageFile = Compressor(context)
                            .setMaxHeight(50)
                            .setMaxWidth(50)
                            .setQuality(1)
                            .compressToBitmap(UserImgFile)

                        val baos = ByteArrayOutputStream()
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val thumb_data = baos.toByteArray()
                        var thumbnail_path : StorageReference = fbStorage.child("profile_images/thumbs").child(user_id+".jpg")

                        var uploadTask : UploadTask = thumbnail_path.putBytes(thumb_data)

                        uploadTask.addOnSuccessListener {
                            thumbnail_path.downloadUrl.addOnSuccessListener {
                                thumbnailUri = it

                                storeFireStore(img_path, profileName)
                            }
                        }

                    } else {

                        Toast.makeText(activity, "Image Error:" + task.exception?.message, Toast.LENGTH_LONG).show()
                        binding.progressBar3.visibility = View.INVISIBLE
                        binding.saveProfile.isEnabled=true
                        binding.cancelSaveBtn.isEnabled=true

                    }

                }

            } else {
                storeFireStore(null,profileName)
            }
        } else{
                Toast.makeText(activity, "image or name are left empty", Toast.LENGTH_LONG).show()
                binding.saveProfile.isEnabled=true
                binding.cancelSaveBtn.isEnabled=true
            }
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            var result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {

                mainImageUri = result.uri

                if(mainImageUri!=null) {

                    setup_image.setImageURI(mainImageUri)

                }

                isChanged=true

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                val error = result.error
                Toast.makeText( activity , "Crop Error: "+ error, Toast.LENGTH_LONG).show()

            }
        }
    }

    private fun storeFireStore(img_path: StorageReference?, profileName:String){

        if(img_path!=null){


        img_path.downloadUrl.addOnSuccessListener {
            var downloadUri = it.toString()
            val user = User(profileName, downloadUri,thumbnailUri.toString())

            fbstore.collection("Users").document(user_id).set(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    Toast.makeText(activity, "Setting updated", Toast.LENGTH_LONG).show()

                   // (activity as AccountSettingsActivity).backFn()
                    var HomeIntent: Intent = Intent(activity, HomeActivity::class.java)
                    startActivity(HomeIntent)
                    (activity as AppCompatActivity).finish()

                } else {

                    Toast.makeText(activity, "fbStore Error: " + task.exception?.message, Toast.LENGTH_LONG).show()
                }
                progressBar3.visibility = View.INVISIBLE
            }
        }.addOnFailureListener {
            Toast.makeText(activity, "Error while retrieving the URL", Toast.LENGTH_LONG)
            progressBar3.visibility = View.INVISIBLE
        }}

        else{

            val user = User(profileName, mainImageUri.toString() , thumbnailUri.toString())
            fbstore.collection("Users").document(user_id).set(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    Toast.makeText(activity, "Setting updated", Toast.LENGTH_LONG).show()

                    //(activity as AccountSettingsActivity).backFn()
                    var HomeIntent: Intent = Intent(activity, HomeActivity::class.java)
                    startActivity(HomeIntent)
                    (activity as AppCompatActivity).finish()

                } else {

                    Toast.makeText(activity, "fbStore Error: " + task.exception?.message, Toast.LENGTH_LONG).show()
                }
                progressBar3.visibility = View.INVISIBLE
            }
        }
    }


}

class User(val name:String="" , val image:String="" , val thumb:String="")

