package com.example.krill

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface LobstersApi {
    @GET("/page/{pageNumber}.json")
    fun getArticles(@Path("pageNumber") pageNumber: String) : Call<List<Article>>

    @GET("/s/{shortId}.json")
    fun getComments(@Path("shortId") shortId: String) : Call<CommentsResponse>
}

data class User(
    val username: String,
    @Json(name = "avatar_url")
    val avatarUrl: String
)

data class CommentsResponse(
    val comments: List<Comment>
)

data class Comment(
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "is_deleted")
    val isDeleted: Boolean,
    val score: Int,
    val upvotes: Int,
    val downvotes: Int,
    val comment: String,
    val url: String,
    @Json(name = "indent_level")
    val indentLevel: Int,
    @Json(name = "commenting_user")
    val commentingUser: User
)

@JsonClass(generateAdapter = true)
data class Article(
    @Json(name = "short_id")
    val shortId: String,
    @Json(name = "short_id_url")
    val shortIdUrl: String,
    @Json(name = "created_at")
    val createdAt: String,
    val title: String,
    val url: String,
    val score: Int,
    val upvotes: Int,
    val downvotes: Int,
    @Json(name = "comment_count")
    val commentCount: Int,
    val description: String,
    @Json(name = "comments_url")
    val commentsUrl: String,
    @Json(name = "submitter_user")
    val submitterUser: User,
    val tags: List<String>
)