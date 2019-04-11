package net.benwoodworth.groupmebot

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.runBlocking
import net.benwoodworth.groupme.api.GroupMe
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.doAsync

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        doAsync {
            val groupMe = GroupMe(
                accessToken = "7BM5EjDbqWz2N3dZZCi6uvSC2tlowyV2D8Bp7ZTD"
            )

            val groups = runBlocking {
                groupMe.groups()
            }

            val groupNames = groups.response
                ?.map { it.name }

            i(groupNames)
        }
    }

    private fun i(s: Any?) = Log.i("[GroupMe Bot]", s?.toString() ?: "null")
}
