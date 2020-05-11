package com.remys.audiotest

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        readAudioFile()
    }

    private fun readAudioFile() {
        mediaPlayer = MediaPlayer.create(this, R.raw.sound_push)
        mediaPlayer?.setOnPreparedListener {
            Log.d("#test", "ready")
        }

        pushButton.setOnTouchListener { _, event ->
            handleTouch(event)
            true
        }
    }

    private fun handleTouch(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("#test", "down")
                mediaPlayer?.start()
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                Log.d("#test", "cancel or up")
                mediaPlayer?.pause()
                mediaPlayer?.seekTo(0)
            }
            else -> {
                Log.d("#test", "else")
            }
        }
    }
}
