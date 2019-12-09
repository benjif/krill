package com.example.krill

import android.view.View
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.article.view.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class FetchAdapter(val context: Context) : RecyclerView.Adapter<ArticleViewHolder>() {
    var items = mutableListOf<Article?>()
    private var pageNumber = 1
    private var loading = false
    private val parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default
    private val scope = CoroutineScope(coroutineContext)

    fun isLoading(): Boolean {
        return loading
    }

    fun clearItems() {
        items.clear()
        pageNumber = 1
    }

    suspend fun fetchPage() {
        val h = Handler(context.mainLooper)
        val startIndex = items.size
        for (i in 0 until 25) {
            items.add(null)
            h.post(Runnable() {
                notifyItemInserted(items.size)
            })
        }
        loading = true
        scope.launch {
            val retroResp = RetrofitClient.Api.getArticles(pageNumber.toString()).execute()
            pageNumber += 1
            val articlesResponse = retroResp.body()
            if (articlesResponse != null) {
                if (articlesResponse.size == 25) {
                    for (i in 0 until articlesResponse.size) {
                        items[startIndex + i] = articlesResponse[i]
                        items[startIndex + i]!!.tagsString = items[startIndex + i]!!.tags
                            .take(3)
                            .joinToString()
                    }
                    h.post(Runnable() {
                        notifyItemRangeChanged(startIndex, 25)
                    })
                }
            }
        }
        loading = false
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.article,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        if (items[position] != null) {
            holder.titleText.text = items[position]!!.title
            holder.authorText.text = items[position]!!.submitterUser.username
            holder.scoreText.text = items[position]!!.score.toString()
            holder.tagsText.text = items[position]!!.tagsString
            if (items[position]!!.commentCount != 0)
                holder.commentCountText.text = items[position]!!.commentCount.toString()
            else
                holder.commentCountText.text = ""
            holder.link = items[position]!!.url
            holder.shortId = items[position]!!.shortId
        } else {
            holder.titleText.text = "...";
            holder.authorText.text = "...";
            holder.tagsText.text = "...";
            holder.commentCountText.text = "";
            holder.scoreText.text = "";
        }
    }
}

class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var link: String? = null
    var shortId: String? = null
    val titleText = view.titleText
    val authorText = view.authorText
    val scoreText = view.scoreText
    val tagsText = view.tagsText
    val commentCountText = view.commentCountText
    val commentsButton = view.commentsButton
    val context = titleText.context

    init {
        // is this bad practice?
        view.setOnClickListener {
            if (link != null && link != "")
                openCustomTab(context, link!!)
            if (link == "" && shortId != null) {
                val intent = Intent(context, CommentsActivity::class.java)
                intent.putExtra("id", shortId)
                context.startActivity(intent)
            }
        }
        commentsButton.setOnClickListener {
            if (shortId != null) {
                val intent = Intent(context, CommentsActivity::class.java)
                intent.putExtra("id", shortId)
                context.startActivity(intent)
            }
        }
    }
}