package com.example.krill

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.synthetic.main.comments.*
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.coroutines.CoroutineContext

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
                    runOnUiThread(Runnable() {
                        if (commentsResponse.comments.isEmpty())
                            emptyCommentsIndicator.visibility = View.VISIBLE
                        val commentsAdapter =
                            CommentsAdapter(context, commentsResponse.comments)
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