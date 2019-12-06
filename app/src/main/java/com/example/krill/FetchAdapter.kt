package com.example.krill

import android.view.View
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.synthetic.main.article.view.*
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.coroutines.CoroutineContext

// OLD WEBVIEW CODE
// KEEPING IT HERE FOR POTENTIAL REUSE
fun openWebpage(context: Context, url: String) {
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    val externalBrowser = sharedPref.getBoolean("externalBrowser", false)
    if (externalBrowser) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
        context.startActivity(intent)
    } else {
        val intent = Intent(context, WebActivity::class.java)
        intent.putExtra("link", url)
        context.startActivity(intent)
    }
}

class FetchAdapter(val context: Context) : RecyclerView.Adapter<ArticleViewHolder>() {
    var items = mutableListOf<Article?>()
    private var pageNumber = 1
    private var loading = false
    private val parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default
    private val scope = CoroutineScope(coroutineContext)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://lobste.rs")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    private val lobstersApi = retrofit.create(LobstersApi::class.java)

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
            val retroResp = lobstersApi.getArticles(pageNumber.toString()).execute()
            pageNumber += 1
            val articlesResponse = retroResp.body()
            if (articlesResponse != null) {
                if (articlesResponse.size == 25) {
                    for (i in 0 until articlesResponse.size) {
                        items[startIndex + i] = articlesResponse[i]
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
        if (items[position] == null) {
            holder.titleText.text = "..."
            holder.authorText.text = "..."
            holder.scoreText.text = ""
        } else {
            holder.titleText.text = items[position]!!.title
            holder.authorText.text = items[position]!!.submitterUser.username
            holder.scoreText.text = items[position]!!.score.toString()
            holder.link = items[position]!!.url
            holder.commentsButton.setOnClickListener {
                    val intent = Intent(context, CommentsActivity::class.java)
                    intent.putExtra("id", items[position]!!.shortId)
                    context.startActivity(intent)
            }
        }
    }
}

class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var link: String? = null
    val titleText = view.titleText
    val authorText = view.authorText
    val scoreText = view.scoreText
    val commentsButton = view.commentsButton
    val context = titleText.context

    init {
        view.setOnClickListener {
                if (link != null)
                    openCustomTab(context, link!!)
                    //openWebpage(context, link!!)
        }
    }
}