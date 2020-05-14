package com.remys.audiotest

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.remys.audiotest.SpinnerHelper.BITRATE_SELECTOR
import com.remys.audiotest.SpinnerHelper.ENCODER_SELECTOR
import com.remys.audiotest.SpinnerHelper.EXTENSION_SELECTOR
import com.remys.audiotest.SpinnerHelper.getAudioEncoder
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class MainActivity : AppCompatActivity(),
    OnAudioFileListener {
    private var bitrateValue: Int = 0
    private lateinit var encoderValue: String
    private lateinit var extensionValue: String

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null

    // Requesting permission to RECORD_AUDIO
    private var recordPermission: String = Manifest.permission.RECORD_AUDIO

    private var isRecording: Boolean = false
    private var isPlaying: Boolean = false

    private var path: String? = null
    private var allFiles: ArrayList<File>? = null

    private var playlistAdapter: PlaylistAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        path = this.getExternalFilesDir("/")?.absolutePath

        setSpinners()
        initSpinnersListener()
        setRecyclerView()

        record_button.setOnTouchListener { _, event ->
            if (checkPermissions()) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Start recording
                        isRecording = true
                        record_button.setImageDrawable(resources.getDrawable(R.drawable.record_btn_stopped))
                        startRecording()
                    }
                    MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                        // Stop recording
                        isRecording = false
                        record_button.setImageDrawable(resources.getDrawable(R.drawable.record_btn_recording))
                        stopRecording()
                    }
                }
            }

            true
        }
    }

    private fun startRecording() {
        timer.base = SystemClock.elapsedRealtime()
        timer.start()

        val formatter = SimpleDateFormat("dd:HH:mm:ss", Locale.FRANCE)
        val recordFile = "${bitrateValue}_${encoderValue}_${formatter.format(Date())}$extensionValue"

        mediaRecorder = MediaRecorder()
        mediaRecorder?.apply {
            this.setAudioSource(MediaRecorder.AudioSource.MIC)
            this.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            this.setOutputFile("$path/$recordFile")
            this.setAudioEncoder(getAudioEncoder(encoderValue))
            this.setAudioEncodingBitRate(bitrateValue)

            try {
                this.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            this.start()
        }

        recordText.text = "Enregistrement de: $recordFile"
    }

    private fun stopRecording() {
        timer.stop()

        mediaRecorder?.apply {
            this.stop()
            this.release()
        }
        mediaRecorder = null

        recordText.text = ""

        setRecyclerView()
    }

    private fun checkPermissions(): Boolean {
        return if (ActivityCompat.checkSelfPermission(this, recordPermission) == PackageManager.PERMISSION_GRANTED)
            true
        else {
            ActivityCompat.requestPermissions(this, arrayOf(recordPermission), REQUEST_RECORD_AUDIO_PERMISSION)
            false
        }
    }

    private fun setSpinners() {
        val bitrateSelection = this.resources.getStringArray(R.array.bitrate_select)
        bitrate_selector.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, bitrateSelection)

        val encoderSelection = this.resources.getStringArray(R.array.encoder_select)
        encoder_selector.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, encoderSelection)

        val extensionSelection = this.resources.getStringArray(R.array.extension_select)
        extension_selector.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, extensionSelection)
    }

    private fun initSpinnersListener() {
        bitrate_selector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                bitrateValue =
                    SpinnerHelper.getSpinnerSelectedValue(applicationContext, BITRATE_SELECTOR, position).toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                bitrateValue = SpinnerHelper.getSpinnerSelectedValue(applicationContext, BITRATE_SELECTOR, 0).toInt()
            }
        }

        encoder_selector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                encoderValue = SpinnerHelper.getSpinnerSelectedValue(applicationContext, ENCODER_SELECTOR, position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                encoderValue = SpinnerHelper.getSpinnerSelectedValue(applicationContext, ENCODER_SELECTOR, 0)
            }
        }

        extension_selector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                extensionValue = SpinnerHelper.getSpinnerSelectedValue(applicationContext, EXTENSION_SELECTOR,
                    position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                extensionValue = SpinnerHelper.getSpinnerSelectedValue(applicationContext, EXTENSION_SELECTOR, 0)
            }
        }
    }

    private fun setRecyclerView() {
        val directory = File(path)
        allFiles = directory.listFiles()?.toMutableList() as ArrayList<File>?

        playlistAdapter = PlaylistAdapter(allFiles, this)

        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager = LinearLayoutManager(this.applicationContext)

        recycler_view.adapter = playlistAdapter

        playlistAdapter?.notifyDataSetChanged()
    }

    private fun playAudio(file: File?, imageView: ImageView) {
        mediaPlayer = MediaPlayer()

        Log.d("AlexTest", file!!.absolutePath)

        mediaPlayer?.apply {
            /*setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build())*/

            try {
                setOnPreparedListener {
                    MediaPlayer.OnPreparedListener { mediaPlayer?.start() }
                }
                setDataSource(file.path)
                prepare()
            } catch (e: IOException) {
                Log.e("AlexTest", "prepare() failed $e")
            }

            start()
        }


        imageView.setImageDrawable(resources.getDrawable(R.drawable.list_pause_btn))

        isPlaying = true

        mediaPlayer?.setOnCompletionListener {
            MediaPlayer.OnCompletionListener {
                it?.apply {
                    stop()
                    release()

                }

                imageView.setImageDrawable(resources.getDrawable(R.drawable.list_play_btn))
                isPlaying = false
                mediaPlayer = null
            }

        }
    }

    private fun stopAudio(imageView: ImageView) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        imageView.setImageDrawable(resources.getDrawable(R.drawable.list_play_btn))
        isPlaying = false
    }

    override fun playAudioFile(file: File?, imageView: ImageView) {
        if (!isPlaying) {
            playAudio(file, imageView)
        } else {
            stopAudio(imageView)
        }
    }

    override fun getAudioFileDetail(position: Int) {
        TODO("Not yet implemented")
    }
}
