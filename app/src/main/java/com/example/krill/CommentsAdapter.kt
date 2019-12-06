package com.example.krill

import android.content.Context
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
        holder.commentText.text = Html.fromHtml(commentItems[position].comment, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val score = commentItems[position].score.toString() + 'p'
        holder.commentScoreText.text = score
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val indentMargin = abs((commentItems[position].indentLevel + 3) % 8 - 4) * 35
        params.setMargins(6 + indentMargin, 6, 6, 6)
        holder.commentCardView.layoutParams = params
        holder.popupUsername.text = commentItems[position].commentingUser.username
        holder.popupKarma.text = commentItems[position].commentingUser.karma.toString()
        holder.popupAbout.text = commentItems[position].commentingUser.about
    }

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var commentText = view.commentText
        var authorText = view.commentAuthorText
        var commentScoreText = view.commentScoreText
        var commentCardView = view.commentCardView
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                as LayoutInflater
        val popupView = inflater.inflate(R.layout.user, null)
        var popupUsername = popupView.username
        var popupKarma = popupView.karma
        var popupAbout = popupView.about

        init {
            authorText.setOnClickListener {
                val popupWindow = PopupWindow(
                    popupView,
                    Resources.getSystem().displayMetrics.widthPixels,
                    Resources.getSystem().displayMetrics.heightPixels
                )
                popupWindow.isOutsideTouchable = false
                popupWindow.isFocusable = true
                popupWindow.animationStyle = R.style.PopupAnimation
                popupWindow.showAtLocation(it, Gravity.CENTER, 0, 0)
                popupView.setOnTouchListener { _: View, _: MotionEvent ->
                    popupWindow.dismiss()
                    true
                }
            }
        }
    }
}

