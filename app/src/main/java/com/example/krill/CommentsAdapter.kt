package com.example.krill

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.comment.view.*
import kotlinx.android.synthetic.main.user.view.*
import kotlin.math.abs

class CommentsAdapter (val context: Context, var commentItems: List<Comment>) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {
    override fun getItemCount(): Int {
        return commentItems.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
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
        holder.commentText.text = commentItems[position].comment
        val score = commentItems[position].score.toString() + 'p'
        holder.commentScoreText.text = score
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val indentMargin = commentItems[position].indentMargin
        params.setMargins(6 + indentMargin, 6, 6, 6)
        holder.commentCardView.layoutParams = params
        holder.user = commentItems[position].commentingUser
    }

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var commentText = view.commentText
        var authorText = view.commentAuthorText
        var commentScoreText = view.commentScoreText
        var commentCardView = view.commentCardView
        var user: User? = null

        init {
            authorText.setOnClickListener {
                if (user != null) {
                    val intent = Intent(context, UserViewActivity::class.java)
                    intent.putExtra("username", user!!.username)
                    intent.putExtra("karma", user!!.karma)
                    intent.putExtra("about", user!!.about)
                    context.startActivity(intent)
                }
            }
        }
    }
}

