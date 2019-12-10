package com.example.krill

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Typeface
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.login_dialog.view.*
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.pow

const val recyclerLoadAheadOffset = 5

// Shared preferences IDs
//const val SAVED_POSTS = "saved"
const val ACCOUNT_PREFS = "account"
const val ACCOUNT_USERNAME = "username"
const val ACCOUNT_PASSWORD = "password"

var accountUsername = ""
var accountPassword = ""

//var savedPosts = HashSet<Article>()

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val mFetchAdapter = FetchAdapter(this)
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

        getAccount()
        if (accountUsername != "")
            navigationHeader.navUsername.text = accountUsername

        navigationHeader.navHeaderLogin.setOnClickListener {
            val inflater = LayoutInflater.from(this)
            val loginView = inflater.inflate(R.layout.login_dialog, null)
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setView(loginView)
            alertDialogBuilder.setPositiveButton("Login", DialogInterface.OnClickListener { _, _ ->
                val emailOrName = loginView.dialogUsername.text
                    .toString()
                val password = loginView.dialogPassword.text
                    .toString()
                Thread {
                    val loginResponse = RetrofitClient.Api
                        .login(emailOrName, password)
                        .execute()
                    if (loginResponse.code() == 302) {
                        Toast.makeText(this, "Account added successfully", Toast.LENGTH_SHORT).show()
                        accountUsername = emailOrName
                        accountPassword = password
                    } else {
                        Toast.makeText(this, "Account login failed", Toast.LENGTH_LONG).show()
                    }
                }.start()
            })
            alertDialogBuilder.show()
        }

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

        /*
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                mFetchAdapter.notifyItemChanged(viewHolder.adapterPosition)
                Toast.makeText(applicationContext, "Post saved", Toast.LENGTH_LONG).show()
            }

            private val textPaint = Paint(ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#5c5c5c")
                textSize = 45f
                typeface = Typeface.DEFAULT_BOLD
            }

            private val padding = 45f

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val item = viewHolder.itemView
                    val alpha = 1 - abs(dX) / item.width
                    item.alpha = alpha

                    if (dX > 0) {
                        textPaint.alpha = (255 * (1 - alpha.pow(4))).toInt()
                        c.drawText("Save", item.left.toFloat() + padding,
                            item.top.toFloat() + (item.bottom.toFloat() - item.top.toFloat()) / 2 + 25, textPaint)
                    }
                }
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(articles)
        */
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

    override fun onPause() {
        super.onPause()
        setAccount()
    }

    override fun onResume() {
        // TODO: verify account credentials still work
        super.onResume()
        getAccount()
    }

    private fun setAccount() {
        val sharedPreferences = getSharedPreferences(ACCOUNT_PREFS,
            Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(ACCOUNT_USERNAME, accountUsername)
        editor.putString(ACCOUNT_PASSWORD, accountPassword)
        editor.commit()
    }

    private fun getAccount() {
        val sharedPreferences = getSharedPreferences(ACCOUNT_PREFS,
            Context.MODE_PRIVATE)
        val usernameValue = sharedPreferences.getString(ACCOUNT_USERNAME, "")
        val passwordValue = sharedPreferences.getString(ACCOUNT_PASSWORD, "")
        accountUsername = usernameValue ?: ""
        accountPassword = passwordValue ?: ""
    }
}