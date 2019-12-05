package com.example.krill

import android.view.View
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.synthetic.main.article.view.*
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import kotlin.coroutines.CoroutineContext

class FetchAdapter(val context: Context) : RecyclerView.Adapter<ArticleViewHolder>() {
    var items = mutableListOf<Article?>()
    private var pageNumber = 1
    private val parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default
    private val scope = CoroutineScope(coroutineContext)
    private var loading = false
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://lobste.rs/page/")
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
            //val curURL = "https://lobste.rs/page/$pageNumber.json"
            val retroResp = lobstersApi.getArticles(pageNumber.toString()).execute()
            pageNumber += 1
            val lobstResp = retroResp.body()
            if (lobstResp != null) {
                if (lobstResp.size == 25) {
                    for (i in 0 until lobstResp.size) {
                        items[startIndex + i] = lobstResp[i]
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
            holder.clink = items[position]!!.commentsUrl
        }
    }
}

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

class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var link: String? = null
    var clink: String? = null
    val titleText = view.titleText
    val authorText = view.authorText
    val scoreText = view.scoreText
    val context = titleText.context

    init {
        view.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (link != null)
                    openWebpage(context, link!!)
            }
        })
        view.commentsButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (clink != null)
                    openWebpage(context, clink!!)
            }
        })
    }
}

interface LobstersApi {
    @GET("/page/{pageNumber}.json")
    fun getArticles(@Path("pageNumber") pageNumber: String) : Call<List<Article>>
}

data class SubmitterUser(
    val username: String,
    @Json(name = "avatar_url")
    val avatarUrl: String
)

@JsonClass(generateAdapter = true)
data class Article(
    @Json(name = "short_id")
    val short_id: String,
    @Json(name = "short_id_url")
    val shortIdUrl: String,
    @Json(name = "created_at")
    val createdAt: String,
    val title: String,
    var url: String,
    val score: Int,
    val upvotes: Int,
    val downvotes: Int,
    @Json(name = "comment_count")
    val commentCount: Int,
    val description: String,
    @Json(name = "comments_url")
    val commentsUrl: String,
    @Json(name = "submitter_user")
    val submitterUser: SubmitterUser
)

/*
interface ArticleValues {
    val title: String
    val author: String
    val score: String
    val link: String
    val commentsLink: String
}

data class Article(
    override val title: String,
    override val author: String,
    override val score: String,
    override val link: String,
    override val commentsLink: String
) : ArticleValues
 */