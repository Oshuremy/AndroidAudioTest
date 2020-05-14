package com.remys.audiotest

import android.widget.ImageView
import java.io.File

interface OnAudioFileListener {
    fun playAudioFile(file: File?, imageView: ImageView)
    fun getAudioFileDetail(position: Int)
}
