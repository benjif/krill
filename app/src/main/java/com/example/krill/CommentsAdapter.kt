package com.example.krill

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.comment.view.*

class CommentsAdapter (val context: Context, val commentItems: List<Comment>) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {
    override fun getItemCount(): Int {
        return commentItems.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val holder = CommentViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.comment,
                parent,
                false
            )
        )
        holder.colorBar.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        return holder
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.authorText.text = commentItems[position].commentingUser.username
        holder.commentText.text = commentItems[position].comment
        val score = commentItems[position].score.toString() + 'p'
        holder.commentScoreText.text = score
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val indentMargin = commentItems[position].indentMargin
        params.leftMargin = indentMargin
        holder.commentView.layoutParams = params
        holder.colorBar.background = ColorDrawable(commentItems[position].indentColor)
        holder.authorText.setTextColor(commentItems[position].indentColor)
        holder.user = commentItems[position].commentingUser
    }

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val commentText = view.commentText
        val authorText = view.commentAuthorText
        val commentScoreText = view.commentScoreText
        val commentView = view.commentView
        val colorBar = view.colorBar
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

