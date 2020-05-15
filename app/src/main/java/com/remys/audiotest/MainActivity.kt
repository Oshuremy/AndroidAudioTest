package com.remys.audiotest

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.MediaRecorder.AudioEncoder
import android.media.MediaRecorder.OutputFormat
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
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

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null

    // Requesting permission to RECORD_AUDIO
    private var recordPermission: String = Manifest.permission.RECORD_AUDIO
    private var readStoragePermission: String = Manifest.permission.READ_EXTERNAL_STORAGE

    private var isRecording: Boolean = false
    private var isPlaying: Boolean = false

    private var path: String? = null
    private var allFiles: ArrayList<File>? = null

    private var playlistAdapter: PlaylistAdapter? = null

    private lateinit var codecFormatSelected: CodecFormat
    private val codecFormatList = arrayOf(
        // .aac
        CodecFormat(AudioEncoder.AAC, OutputFormat.AAC_ADTS, ".aac", "AAC|AAC_ADTS|aac"),
        // CodecFormat(AudioEncoder.HE_AAC, OutputFormat.AAC_ADTS, ".aac", "HE_AAC|AAC_ADTS|aac"),
        CodecFormat(AudioEncoder.AAC_ELD, OutputFormat.AAC_ADTS, ".aac", "AAC_ELD|AAC_ADTS|aac"),

        // .mpa
        // CodecFormat(AudioEncoder.AAC, OutputFormat.AAC_ADTS, ".m4a", "AAC|AAC_ADTS|m4a"),
        // CodecFormat(AudioEncoder.HE_AAC, OutputFormat.AAC_ADTS, ".m4a", "HE_AAC|AAC_ADTS|m4a"),
        // CodecFormat(AudioEncoder.AAC_ELD, OutputFormat.AAC_ADTS, ".m4a", "AAC_ELD|AAC_ADTS|m4a"),

        // .mp4
        CodecFormat(AudioEncoder.AAC_ELD, OutputFormat.AAC_ADTS, ".mp4", "AAC_ELD|AAC_ADTS|mp4"),

        // mp3
        CodecFormat(AudioEncoder.AAC, OutputFormat.MPEG_4, ".mp3", "AAC|MPEG_4|mp3"),

        // OPUS .ogg/.webm
        CodecFormat(AudioEncoder.OPUS, OutputFormat.OGG, ".ogg", "OPUS|OGG|ogg"),
        CodecFormat(AudioEncoder.OPUS, OutputFormat.WEBM, ".webm", "OPUS|WEBM|webm")
        // CodecFormat(AudioEncoder.VORBIS, OutputFormat.OGG, ".ogg", "VORBIS|OGG|ogg"),
        // CodecFormat(AudioEncoder.VORBIS, OutputFormat.WEBM, ".webm", "VORBIS|WEBM|webm")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        path = getExternalFilesDir("/")?.absolutePath

        setSpinners()
        initSpinnersListener()
        setRecyclerView()

        checkReadStoragePermissions()

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
        play_audio_url.setOnClickListener {
            playAudioUrl()
        }
    }

    private fun checkReadStoragePermissions() {
        if (ActivityCompat.checkSelfPermission(this, readStoragePermission) == PackageManager.PERMISSION_GRANTED)
            return
        else
            ActivityCompat.requestPermissions(this, arrayOf(readStoragePermission), REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun checkPermissions(): Boolean {
        return if (ActivityCompat.checkSelfPermission(this, recordPermission) == PackageManager.PERMISSION_GRANTED)
            true
        else {
            ActivityCompat.requestPermissions(this, arrayOf(recordPermission), REQUEST_RECORD_AUDIO_PERMISSION)
            false
        }
    }

    private fun startRecording() {
        timer.base = SystemClock.elapsedRealtime()
        timer.start()

        val formatter = SimpleDateFormat("ddHHmmss", Locale.FRANCE)
        val recordFile =
            "${formatter.format(Date())}-${bitrateValue}-" +
                "${codecFormatSelected.toString}${codecFormatSelected.extension}"

        mediaRecorder = MediaRecorder()
        mediaRecorder?.apply {
            this.setAudioSource(MediaRecorder.AudioSource.MIC)
            this.setOutputFormat(codecFormatSelected.outputFormat)
            this.setOutputFile("$path/$recordFile")
            this.setAudioEncoder(codecFormatSelected.codec)
            // this.setAudioEncodingBitRate(bitrateValue)

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

    private fun setSpinners() {
        val bitrateSelection = this.resources.getStringArray(R.array.bitrate_select)
        bitrate_selector.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, bitrateSelection)

        val codecFormatStrings: ArrayList<String> = arrayListOf()
        for (codecFormat in codecFormatList)
            codecFormatStrings.add(codecFormat.toString())

        codecformat_selector.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, codecFormatStrings)
    }

    private fun initSpinnersListener() {
        bitrate_selector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                bitrateValue = resources.getStringArray(R.array.bitrate_select)[position].toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                bitrateValue = resources.getStringArray(R.array.bitrate_select)[0].toInt()
            }
        }

        codecformat_selector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                codecFormatSelected = codecFormatList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                codecFormatSelected = codecFormatList[0]
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
        mediaPlayer = MediaPlayer.create(this, Uri.fromFile(file))

        mediaPlayer?.apply {
            try {
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        imageView.setImageDrawable(resources.getDrawable(R.drawable.list_pause_btn))
        isPlaying = true

        mediaPlayer?.setOnCompletionListener {
            it?.apply {
                stop()
                release()
            }

            imageView.setImageDrawable(resources.getDrawable(R.drawable.list_play_btn))
            isPlaying = false
            mediaPlayer = null
        }
    }

    private fun stopAudio(imageView: ImageView) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        imageView.setImageDrawable(resources.getDrawable(R.drawable.list_play_btn))
        isPlaying = false
    }

    private fun playAudioUrl() {
        val url =
            "https://cdn.fbsbx.com/v/t59.3654-21/97884637_254134322452318_3856315336802435072_n.mp4/audioclip-1589449208-6443.mp4?_nc_cat=102&_nc_sid=7272a8&_nc_ohc=5D6mZdlt9fEAX802MS3&_nc_ht=cdn.fbsbx.com&oh=24fce8c520b061af1face8677d032be6&oe=5EBEC301&dl=1"

        // val mp = MediaPlayer.create(this, R.raw.sound_push)
        val mp = MediaPlayer.create(this, Uri.parse(url))
        // val mp = MediaPlayer()
        try {
            // mp.setDataSource(url)
            // mp.prepare()
            mp.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun playAudioFile(file: File?, imageView: ImageView) {
        if (!isPlaying)
            playAudio(file, imageView)
        else
            stopAudio(imageView)
    }

    override fun getAudioFileDetail(position: Int) {
        TODO("Not yet implemented")
    }
}
