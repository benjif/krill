package com.example.krill

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.user.*

class UserViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user)

        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val username = intent.getStringExtra("username")
        val karma = intent.getIntExtra("karma", 0).toString() + 'p'
        val about = intent.getStringExtra("about")

        usernameText.text = username
        karmaText.text = karma
        aboutText.text = about
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}