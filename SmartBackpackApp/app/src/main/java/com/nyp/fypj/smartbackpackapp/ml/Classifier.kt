package com.nyp.fypj.smartbackpackapp.ml

import android.graphics.Bitmap
import com.nyp.fypj.smartbackpackapp.ml.Classifier.Recognition
import java.nio.FloatBuffer


interface Classifier{
    class  Recognition(var id: String, var title: String, var confidence: Float){


        override fun toString(): String {
            var resultString = ""
            if (id != null) {
                resultString += "[$id] "
            }

            if (title != null) {
                resultString += "$title "
            }

            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence!! * 100.0f)
            }

            return resultString.trim { it <= ' ' }
        }
    }

    fun classifyComfortLevel(input:FloatArray): List<Recognition>

    fun close()
}