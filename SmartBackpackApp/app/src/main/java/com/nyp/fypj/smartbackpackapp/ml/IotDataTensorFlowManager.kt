package com.nyp.fypj.smartbackpackapp.ml

import android.content.res.AssetManager
import com.sap.cloud.android.odata.sbp.IotDataType
import com.sap.cloud.android.odata.sbp.UserinfosType
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.nio.FloatBuffer
import android.util.Log
import java.util.*
import android.R.attr.shape
import android.os.Build
import android.support.annotation.RequiresApi
import java.io.BufferedReader
import java.io.InputStreamReader
import android.support.v4.os.TraceCompat
import android.system.Os.poll
import java.io.File
import java.io.InputStream


class IotDataTensorFlowManager():Classifier {

    private var inputName: String? = null
    private var outputName: String? = null
    private var inputSize: Int = 0

    private val MODEL_FILE = "file:///android_asset/spb_model.pb"
    private val MAX_RESULTS = 3
    private val THRESHOLD = 0.1f

    private var inferenceInterface:TensorFlowInferenceInterface? = null
    private var labels = Vector<String>()
    private var outputs: FloatArray? = null
    private var outputNames: Array<String>? = null

    private var runStats = false

    override fun classifyComfortLevel(input: FloatArray): List<Classifier.Recognition> {
        // Log this method so that it can be analyzed with systrace.
        TraceCompat.beginSection("recognizeImage")



        // Copy the input data into TensorFlow.
        TraceCompat.beginSection("feed")
        inferenceInterface!!.feed(inputName, input,5)
        TraceCompat.endSection()

        // Run the inference call.
        TraceCompat.beginSection("run")
        inferenceInterface!!.run(outputNames, runStats)
        TraceCompat.endSection()

        // Copy the output Tensor back into the output array.
        TraceCompat.beginSection("fetch")
        inferenceInterface!!.fetch(outputName, outputs)
        TraceCompat.endSection()

        // Find the best classifications.
        val pq = PriorityQueue(
                3,
                object : Comparator<Classifier.Recognition> {
                    override fun compare(lhs: Classifier.Recognition, rhs: Classifier.Recognition): Int {
                        // Intentionally reversed to put high confidence at the head of the queue.
                        return java.lang.Float.compare(rhs.confidence, lhs.confidence)
                    }
                })
        for (i in 0 until outputs!!.size) {
            if (outputs!![i] > THRESHOLD) {
                pq.add(
                        Classifier.Recognition(
                                "" + i, if (labels.size > i) labels[i] else "unknown", outputs!![i]))
            }
        }
        val recognitions = ArrayList<Classifier.Recognition>()
        val recognitionsSize = Math.min(pq.size, MAX_RESULTS)
        for (i in 0 until recognitionsSize) {
            recognitions.add(pq.poll())
        }
        TraceCompat.endSection() // "recognizeImage"
        return recognitions
    }

    override fun close() {
        inferenceInterface!!.close()
    }

    companion object {
        private const val TAG = "IotDataTensorFlowMgr"

        @RequiresApi(Build.VERSION_CODES.N)
        fun create(
                assetManager: AssetManager,
                modelFilename: String,
                labelFilename: String,
                inputSize: Int,
                inputName: String,
                outputName: String): Classifier {
            val c = IotDataTensorFlowManager()
            c.inputName = inputName
            c.outputName = outputName

            // Read the label names into memory.
            // TODO(andrewharp): make this handle non-assets.
            val actualFilename = labelFilename.split("file:///android_asset/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            Log.i(TAG, "Reading labels from: $actualFilename")

            var br: BufferedReader? = null
            br = BufferedReader(InputStreamReader(assetManager.open(actualFilename)))
            var line = br.readLine()
            for (line in br.lines()) {
                c.labels.add(line)
            }
            br.close()

            c.inferenceInterface = TensorFlowInferenceInterface(assetManager, modelFilename)

            // The shape of the output is [N, NUM_CLASSES], where N is the batch size.
            //val numClasses = c.inferenceInterface!!.graph().operation(outputName)!!.output<Int>(0).shape().size(1) as Int
            //Log.i(TAG, "Read " + c.labels.size + " labels, output layer size is " + numClasses)

            // Ideally, inputSize could have been retrieved from the shape of the input operation.  Alas,
            // the placeholder node for input in the graphdef typically used does not specify a shape, so it
            // must be passed in as a parameter.
            c.inputSize = inputSize

            // Pre-allocate buffers.
            c.outputNames = arrayOf(outputName)
            c.outputs = FloatArray(5)

            return c
        }
    }
}