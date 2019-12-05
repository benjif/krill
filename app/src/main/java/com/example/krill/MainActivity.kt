package com.example.krill

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.runBlocking

const val recyclerLoadAheadOffset = 5

class MainActivity : AppCompatActivity() {
    private val mFetchAdapter = FetchAdapter(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        //val darkTheme = sharedPref.getBoolean("darkTheme", false)

        setContentView(R.layout.activity_main)

        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)

        mFetchAdapter.setHasStableIds(true)

        val layoutManager = LinearLayoutManager(this)

        swipeRefresh.setOnRefreshListener {
            val total = mFetchAdapter.itemCount
            mFetchAdapter.clearItems()
            articles.clearAnimation()
            mFetchAdapter.notifyItemRangeRemoved(0, total)
            fetchMore()
            mFetchAdapter.notifyItemRangeInserted(0, 25)
            swipeRefresh.isRefreshing = false
        }

        articles.layoutManager = layoutManager
        articles.itemAnimator = DefaultItemAnimator()
        articles.setItemViewCacheSize(20)
        articles.adapter = mFetchAdapter

        articles.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (mFetchAdapter.isLoading()) return
                if (layoutManager.findLastCompletelyVisibleItemPosition() >= layoutManager.itemCount - 1 - recyclerLoadAheadOffset) {
                    fetchMore()
                }
            }
        })

        fetchMore()
    }

    fun fetchMore() = runBlocking {
        mFetchAdapter.fetchPage()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.action_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}