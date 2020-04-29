package com.example.blog


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.blog.databinding.FragmentNewPostBinding
import com.example.blog.databinding.FragmentPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.fragment_acount.*
import kotlinx.android.synthetic.main.fragment_new_post.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.sql.Timestamp
import java.util.*


class NewPost : Fragment() {


    var newPostImgUri : Uri ?= null

    var fbstore = FirebaseFirestore.getInstance()

    var fbAuth = FirebaseAuth.getInstance()

    var fbStorage = FirebaseStorage.getInstance().getReference()

    var user_id: String = fbAuth.currentUser!!.uid



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentNewPostBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_new_post, container , false)

        (activity as AppCompatActivity).setSupportActionBar(binding.newPostBar)

        (activity as AppCompatActivity).supportActionBar?.setTitle("Add New Post")

        //(activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setHasOptionsMenu(true)



        binding.newPostCancelBtn.setOnClickListener {

            activity?.onBackPressed()

            //var HomeIntent: Intent = Intent(activity, HomeActivity::class.java)
            //startActivity(HomeIntent)
            //(activity as AppCompatActivity).finish()

        }

        binding.newPostImg.setOnClickListener {

            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512,512)
                .setAspectRatio(3,2)
                .start( activity as AppCompatActivity ,this)

        }

        binding.newPostSaveBtn.setOnClickListener{

            var information : String = binding.newPostText.text.toString()

            if(!TextUtils.isEmpty(information)&&newPostImgUri!=null){



                binding.newPostSaveBtn.isEnabled=false
                binding.newPostCancelBtn.isEnabled=false



                binding.progressBar4.visibility = View.VISIBLE

                var randomName : String = getRandomString(100)

                var img_path: StorageReference = fbStorage.child("post_images").child(randomName+".jpg")

                img_path.putFile(newPostImgUri!!).addOnCompleteListener {
                    task->

                    if(task.isSuccessful){

                        var newPostImgFile :File = File(newPostImgUri!!.path)

                        var compressedImageFile = Compressor(context)
                            .setMaxHeight(50)
                            .setMaxWidth(50)
                            .setQuality(2)
                            .compressToBitmap(newPostImgFile)

                        val baos = ByteArrayOutputStream()
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val thumb_data = baos.toByteArray()


                        var thumbnail_path : StorageReference = fbStorage.child("post_images/thumbs").child(randomName+".jpg")

                        var uploadTask : UploadTask = thumbnail_path.putBytes(thumb_data)

                        uploadTask.addOnSuccessListener {
                                thumbnail_path.downloadUrl.addOnSuccessListener {
                                    var thumbnailUri = it.toString()

                                    img_path.downloadUrl.addOnSuccessListener {
                                        var downloadUri = it.toString()

                                        val post = Post(information, downloadUri, thumbnailUri, user_id, com.google.firebase.Timestamp.now(),false)

                                        fbstore.collection("Posts").add(post).addOnCompleteListener { task ->


                                                if (task.isSuccessful) {

                                                            //activity?.onBackPressed()
                                                            //(Activity() as MainActivity).finish()
                                                            var HomeIntent: Intent = Intent(activity, HomeActivity::class.java)
                                                            HomeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                            startActivity(HomeIntent)
                                                            activity?.finish()
                                                            Toast.makeText(activity, "Post added successfully", Toast.LENGTH_LONG).show()
                                                            binding.progressBar4.visibility = View.INVISIBLE
                                                            binding.newPostSaveBtn.isEnabled = true
                                                            binding.newPostCancelBtn.isEnabled = true

                                                } else {
                                                    Toast.makeText(activity, "fbStore Error: " + task.exception?.message, Toast.LENGTH_LONG).show()
                                                    binding.progressBar4.visibility = View.INVISIBLE
                                                    binding.newPostSaveBtn.isEnabled=true
                                                    binding.newPostCancelBtn.isEnabled=true
                                                }
                                            }
                                    }.addOnFailureListener {
                                        Toast.makeText(activity, "Error while retrieving the URL", Toast.LENGTH_LONG).show()
                                        binding.progressBar4.visibility = View.INVISIBLE
                                        binding.newPostSaveBtn.isEnabled=true
                                        binding.newPostCancelBtn.isEnabled=true
                                    }
                                }.addOnFailureListener {
                                    Toast.makeText(activity, "Error while retrieving the URL", Toast.LENGTH_LONG).show()
                                    binding.progressBar4.visibility = View.INVISIBLE
                                    binding.newPostSaveBtn.isEnabled=true
                                    binding.newPostCancelBtn.isEnabled=true
                                }
                        }.addOnFailureListener {
                            Toast.makeText(activity, "Image Error:" + task.exception?.message, Toast.LENGTH_LONG).show()
                            binding.progressBar4.visibility = View.INVISIBLE
                            binding.newPostSaveBtn.isEnabled=true
                            binding.newPostCancelBtn.isEnabled=true
                        }
                    }else{
                        Toast.makeText(activity, "Image Error:" + task.exception?.message, Toast.LENGTH_LONG).show()
                        binding.progressBar4.visibility = View.INVISIBLE
                        binding.newPostSaveBtn.isEnabled=true
                        binding.newPostCancelBtn.isEnabled=true
                    }
                }
            }else{
                Toast.makeText(activity, "image or information are left empty", Toast.LENGTH_LONG).show()
            }
        }

        return binding.root
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            var result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                newPostImgUri=result.uri

                new_post_img.setImageURI(newPostImgUri)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText( activity , "Crop Error: "+error, Toast.LENGTH_LONG).show()
            }
        }
    }

   // override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    //    if(item?.itemId == android.R.id.home){

     //       activity?.onBackPressed()
            //var HomeIntent: Intent = Intent(activity, HomeActivity::class.java)
            //startActivity(HomeIntent)
            //(activity as AppCompatActivity).finish()
     //   }

     //   return super.onOptionsItemSelected(item)
   // }

    companion object {
        private val ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm"
    }

    private fun getRandomString(sizeOfRandomString: Int): String {
        val random = Random()
        val sb = StringBuilder(sizeOfRandomString)
        for (i in 0 until sizeOfRandomString)
            sb.append(ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)])
        return sb.toString()
    }




}

class Post(val information:String = "" , val image_url:String="", val thumbnail_url:String="" , val user_id:String="" , var timestamp:com.google.firebase.Timestamp?=null,var deleted:Boolean=false):PostId(){}
