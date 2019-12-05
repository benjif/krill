package com.example.krill

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.synthetic.main.article.view.*
import kotlinx.android.synthetic.main.comment.view.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class CommentsAdapter (val context: Context, val commentItems: List<Comment>) : RecyclerView.Adapter<CommentViewHolder>() {
    override fun getItemCount(): Int {
        return commentItems.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.comment,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.authorText.text = commentItems[position].commentingUser.username
        holder.commentText.text = Html.fromHtml(commentItems[position].comment, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(6 + 35 * (commentItems[position].indentLevel - 1), 6, 6, 6)
        holder.commentCardView.layoutParams = params
    }
}

class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var commentText = view.commentText
    var authorText = view.commentAuthorText
    var commentCardView = view.commentCardView
}