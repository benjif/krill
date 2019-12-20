package com.example.krill

import android.accounts.AccountManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.login_dialog.view.*
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.coroutines.runBlocking

const val recyclerLoadAheadOffset = 5

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val mFetchAdapter = FetchAdapter(this)
    //private val mAccountManager = applicationContext.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
    private lateinit var mToggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO DARK THEME
        //val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        //val darkTheme = sharedPref.getBoolean("darkTheme", false)

        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        fetchMore()
        mFetchAdapter.setHasStableIds(true)

        val layoutManager = LinearLayoutManager(this)

        articles.layoutManager = layoutManager
        articles.itemAnimator = DefaultItemAnimator()
        articles.setItemViewCacheSize(25)
        articles.adapter = mFetchAdapter

        val navigation = findViewById<NavigationView>(R.id.navigationView)
        navigation.setNavigationItemSelectedListener(this)
        val navigationHeader = navigation.getHeaderView(0)

        mToggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, 0, 0)
        drawer_layout.addDrawerListener(mToggle)
        mToggle.isDrawerIndicatorEnabled = true
        mToggle.syncState()

        swipeRefresh.setOnRefreshListener {
            val total = mFetchAdapter.itemCount
            mFetchAdapter.clearItems()
            articles.clearAnimation()
            mFetchAdapter.notifyItemRangeRemoved(0, total)
            fetchMore()
            mFetchAdapter.notifyItemRangeInserted(0, 25)
            swipeRefresh.isRefreshing = false
        }

        articles.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (mFetchAdapter.isLoading()) return
                if (layoutManager.findLastCompletelyVisibleItemPosition() >= layoutManager.itemCount - 1 - recyclerLoadAheadOffset) {
                    fetchMore()
                }
            }
        })
    }

    private fun fetchMore() = runBlocking {
        mFetchAdapter.fetchPage()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mToggle.onOptionsItemSelected(item)) return true
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

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        // TODO: this
        /*when (p0.itemId) {
            R.id.hottest -> {

            }
            R.id.recent -> {

            }
        }*/
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        mToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mToggle.onConfigurationChanged(newConfig)
    }
}