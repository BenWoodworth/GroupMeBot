package net.benwoodworth.groupmebot

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun i(s: Any?) = Log.i("[GroupMe Bot]", s?.toString() ?: "null")
}
