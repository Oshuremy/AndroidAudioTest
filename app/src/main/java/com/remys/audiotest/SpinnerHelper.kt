package com.remys.audiotest

import android.content.Context
import android.media.MediaRecorder

object SpinnerHelper {
    const val BITRATE_SELECTOR = "bitrate_selector"
    const val ENCODER_SELECTOR = "encoder_selector"
    const val EXTENSION_SELECTOR = "extension_selector"

    private const val AAC_ENCODER = "AAC"
    private const val HE_AAC_ENCODER = "HE AAC"
    private const val ELD_AAC_ENCODER = "ELD AAC"

    fun getSpinnerSelectedValue(context: Context, selectorType: String, valuePosition: Int): String {
        return when(selectorType) {
            BITRATE_SELECTOR -> getStringFromArray(context.resources.getStringArray(R.array.bitrate_select),
                valuePosition)
            ENCODER_SELECTOR -> getStringFromArray(context.resources.getStringArray(R.array.encoder_select),
                valuePosition)
            EXTENSION_SELECTOR -> getStringFromArray(context.resources.getStringArray(R.array.extension_select),
                valuePosition)
            else -> ""
        }
    }

    private fun getStringFromArray(stringArray: Array<String>, valuePosition: Int): String {
        return stringArray[valuePosition]
    }

    fun getAudioEncoder(selectedEncoder: String): Int {
        return when(selectedEncoder) {
            AAC_ENCODER -> MediaRecorder.AudioEncoder.AAC
            HE_AAC_ENCODER -> MediaRecorder.AudioEncoder.HE_AAC
            ELD_AAC_ENCODER -> MediaRecorder.AudioEncoder.AAC_ELD
            else -> MediaRecorder.AudioEncoder.AAC
        }
    }
}