package com.example.krill

import android.content.Context
import android.text.Html
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.comment.view.*
import kotlinx.android.synthetic.main.user.view.*
import kotlin.math.abs

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
        holder.authorText.setOnClickListener  {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                as LayoutInflater
            val popupView = inflater.inflate(R.layout.user, null)
            val user = commentItems[position].commentingUser
            popupView.username.text = user.username
            popupView.karma.text = user.karma.toString()
            popupView.about.text =
                Html.fromHtml(user.about, HtmlCompat.FROM_HTML_MODE_LEGACY)
            val popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            popupWindow.isOutsideTouchable = false
            popupWindow.isFocusable = true
            popupWindow.animationStyle = R.style.PopupAnimation
            popupWindow.showAtLocation(it, Gravity.CENTER, 0, 0)
            popupView.setOnTouchListener { view: View, motionEvent: MotionEvent ->
                popupWindow.dismiss()
                true
            }
        }
        holder.authorText.text = commentItems[position].commentingUser.username
        holder.commentText.text = Html.fromHtml(commentItems[position].comment, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val score = commentItems[position].score.toString() + 'p'
        holder.commentScoreText.text = score
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val indentMargin = abs((commentItems[position].indentLevel + 3) % 8 - 4) * 35
        params.setMargins(6 + indentMargin, 6, 6, 6)
        holder.commentCardView.layoutParams = params
    }
}

class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var commentText = view.commentText
    var authorText = view.commentAuthorText
    var commentScoreText = view.commentScoreText
    var commentCardView = view.commentCardView
}