package com.example.krill

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.synthetic.main.comments.*
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

class CommentsActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comments)

        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val shortId = intent.getStringExtra("id")

        job = Job()
        val context = this

        if (shortId != null) {
            Thread {
                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://lobste.rs")
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()
                val lobstersApi = retrofit.create(LobstersApi::class.java)

                val commentsCallResponse = lobstersApi.getComments(shortId).execute()
                val commentsResponse = commentsCallResponse.body()

                if (commentsResponse != null) {
                    val commentList = commentsResponse.comments.toMutableList()

                    // Pre-processing to avoid slowing RV adapter
                    for (i in commentList.indices) {
                        commentList[i].comment = Html.fromHtml(commentList[i].comment, HtmlCompat.FROM_HTML_MODE_LEGACY)
                            .toString()
                        commentList[i].indentMargin = abs((commentList[i].indentLevel + 3) % 8 - 4) * 35
                    }

                    runOnUiThread(Runnable() {
                        if (commentList.isEmpty())
                            emptyCommentsIndicator.visibility = View.VISIBLE
                        val commentsAdapter =
                            CommentsAdapter(context, commentList)
                        commentsAdapter.setHasStableIds(true)
                        comments.adapter = commentsAdapter
                    })
                }
            }.start()
        }

        val layoutManager = LinearLayoutManager(this)

        comments.layoutManager = layoutManager
        comments.itemAnimator = DefaultItemAnimator()
        comments.setItemViewCacheSize(10)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}