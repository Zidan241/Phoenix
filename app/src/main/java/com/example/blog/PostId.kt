package com.example.blog

import androidx.annotation.NonNull
import com.google.firebase.firestore.Exclude



open class PostId {

    @Exclude
    var BlogPostId: String = ""

    fun <T : PostId> withId(@NonNull id: String): T {
        this.BlogPostId = id
        return this as T
    }
}