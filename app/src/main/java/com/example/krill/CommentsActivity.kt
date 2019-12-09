package com.example.krill

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.comments.*
import kotlinx.coroutines.*
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
        supportActionBar?.title = "Comments"

        val shortId = intent.getStringExtra("id")

        job = Job()
        val context = this

        if (shortId != null) {
            Thread {
                val commentsCallResponse = RetrofitClient.Api.getComments(shortId).execute()
                val commentsResponse = commentsCallResponse.body()

                val indentLevelColors = listOf<Int>(
                    ContextCompat.getColor(context, R.color.colorPrimary),
                    ContextCompat.getColor(context, R.color.colorSecondary),
                    ContextCompat.getColor(context, R.color.colorTertiary),
                    ContextCompat.getColor(context, R.color.colorQuaternary),
                    ContextCompat.getColor(context, R.color.colorQuinary),
                    ContextCompat.getColor(context, R.color.colorSenary),
                    ContextCompat.getColor(context, R.color.colorSeptenary),
                    ContextCompat.getColor(context, R.color.colorOctonary),
                    ContextCompat.getColor(context, R.color.colorNonary),
                    ContextCompat.getColor(context, R.color.colorDenary)
                )

                if (commentsResponse != null) {
                    val commentList = commentsResponse.comments.toMutableList()

                    // Pre-processing to avoid slowing RV adapter
                    for (i in commentList.indices) {
                        commentList[i].comment = Html.fromHtml(commentList[i].comment, HtmlCompat.FROM_HTML_MODE_LEGACY)
                            .toString()
                        val indentLevelWrapped = abs((commentList[i].indentLevel + 3) % 8 - 4)
                        commentList[i].indentMargin = indentLevelWrapped * 35
                        commentList[i].indentColor = indentLevelColors[indentLevelWrapped]
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
        //comments.setItemViewCacheSize(10)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}