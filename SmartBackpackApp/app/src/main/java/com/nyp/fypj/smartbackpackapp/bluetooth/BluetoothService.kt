package com.nyp.sit.fypj.smartbackpackapp.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.STATE_CONNECTED
import android.bluetooth.BluetoothAdapter.STATE_CONNECTING
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.media.session.PlaybackState.STATE_NONE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.nyp.fypj.smartbackpackapp.bluetooth.BtCommandObject
import com.nyp.sit.fypj.smartbackpackapp.Constants
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothService(private val displayHandler: Handler) {

    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null

    private var mState = STATE_NONE
    private var mAdapter = BluetoothAdapter.getDefaultAdapter()

    fun sendToastMessage(toastType:String, toastContent:String){
        val msg = displayHandler.obtainMessage(Constants.HANDLER_ACTION.TOAST.value)
        val bundle = Bundle()
        bundle.putString(toastType, toastContent)
        msg.data = bundle
        displayHandler.sendMessage(msg)
    }

    @Synchronized
    fun connect(device: BluetoothDevice) {
        Log.d(TAG, "connect to: $device")

        mConnectThread?.cancel()
        mConnectThread = null

        mConnectedThread?.cancel()
        mConnectedThread = null

        // Start the thread to connect with the given device
        mConnectThread = ConnectThread(device)
        mConnectThread?.start()
    }

    @Synchronized
    fun connected(socket: BluetoothSocket, device: BluetoothDevice, socketType: String) {
        Log.d(TAG, "connected, Socket Type:$socketType")

        mConnectThread?.cancel()
        mConnectThread = null

        mConnectedThread?.cancel()
        mConnectedThread = null

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(socket, socketType)
        mConnectedThread?.start()

        // Send the name of the connected device back to the UI Activity
        val msg = displayHandler.obtainMessage(Constants.HANDLER_STATE_CHANGE)
        val bundle = Bundle()
        bundle.putString(Constants.DEVICE_NAME, device.name)
        msg.data = bundle
        displayHandler.sendMessage(msg)
    }

    /**
     * Stop all threads
     */
    @Synchronized
    fun stop() {
        Log.d(TAG, "stop")

        mConnectThread?.cancel()
        mConnectThread = null

        mConnectedThread?.cancel()
        mConnectedThread = null

        mState = STATE_NONE

        // Send the name of the connected device back to the UI Activity
        sendToastMessage(Constants.HANDLER_DATA_KEY.TOAST_CONTENT.value, "Disconnected")
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread.write
     */
    fun write(out: ByteArray) {
        var r: ConnectedThread? = null
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (mState != STATE_CONNECTED) return
            r = mConnectedThread
        }
        // Perform the write unsynchronized
        r?.write(out)
    }

    fun write(out: String) {
        var outByte = out.toByteArray()
        var r: ConnectedThread? = null
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (mState != STATE_CONNECTED) return
            r = mConnectedThread
        }
        // Perform the write unsynchronized
        r?.write(outByte)
    }

    fun write(out: BtCommandObject) {
        val gson = Gson()
        GsonBuilder().setPrettyPrinting().create()
        Log.e(TAG,gson.toJson(out))
        var outByte = gson.toJson(out).toByteArray()
        Log.e(TAG,outByte.toString())
        var r: ConnectedThread? = null
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (mState != STATE_CONNECTED) return
            r = mConnectedThread
        }
        // Perform the write unsynchronized
        r?.write(outByte)
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private fun connectionFailed() {
        // Send a failure message back to the Activity
        sendToastMessage(Constants.HANDLER_DATA_KEY.TOAST_CONTENT.value, "Unable to connect device")

        mState = STATE_NONE
        // Update UI title

    }


    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private fun connectionLost() {
        // Send a failure message back to the Activity
        sendToastMessage(Constants.HANDLER_DATA_KEY.TOAST_CONTENT.value, "Device connection was lost")

        mState = STATE_NONE

    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private inner class ConnectThread(private val mmDevice: BluetoothDevice) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mSocketType: String

        init {
            var tmp: BluetoothSocket? = null
            this.mSocketType = "Secure"

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(
                        UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
                )
            } catch (e: IOException) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e)
            }

            this.mmSocket = tmp
            mState = STATE_CONNECTING
        }

        override fun run() {

            Log.i(TAG, "BEGIN mConnectThread SocketType:$mSocketType")
            name = "ConnectThread$mSocketType"

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery()

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket!!.connect()
            } catch (e: IOException) {
                // Close the socket
                try {
                    mmSocket!!.close()
                } catch (e2: IOException) {
                    Log.e(
                        TAG, "unable to close() " + mSocketType +
                                " socket during connection failure", e2
                    )
                }

                connectionFailed()
                return
            }

            // Reset the ConnectThread because we're done
            synchronized(this@BluetoothService) {
                mConnectThread = null
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType)
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect $mSocketType socket failed", e)
            }

        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private inner class ConnectedThread(private val mmSocket: BluetoothSocket, socketType: String) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            Log.d(TAG, "create ConnectedThread: $socketType")
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = mmSocket.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
                Log.e(TAG, "temp sockets not created", e)
            }

            mmInStream = tmpIn
            mmOutStream = tmpOut
            mState = STATE_CONNECTED
        }

        override fun run() {
            Log.i(TAG, "BEGIN mConnectedThread")
            val buffer = ByteArray(1024)
            var bytes: Int

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream!!.read(buffer)
                    val readBuf = buffer as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val jsonResponse = String(readBuf, 0, bytes)

                    var gson = Gson()
                    Log.e(TAG,jsonResponse)
                    var mBtCommandObject = gson.fromJson(jsonResponse, BtCommandObject::class.java);
                    // Send the obtained bytes to the UI Activity
                    val msg = displayHandler.obtainMessage(Constants.HANDLER_ACTION.DISPLAY_SENSOR_DATA.value)
                    msg.obj = mBtCommandObject
                    displayHandler.sendMessage(msg)
                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                    connectionLost()
                    break
                }

            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        fun write(buffer: ByteArray) {
            try {
                mmOutStream!!.write(buffer)

                // Share the sent message back to the UI Activity
                displayHandler.obtainMessage(Constants.HANDLER_MESSAGE_SEND, -1, -1, buffer)
                    .sendToTarget()
            } catch (e: IOException) {
                Log.e(TAG, "Exception during write", e)
            }

        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }

        }
    }

    companion object {
        private val TAG = "BluetoothService"
    }
}
