package com.nyp.fypj.smartbackpackapp.mdui

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.nyp.fypj.smartbackpackapp.R
import com.nyp.fypj.smartbackpackapp.bluetooth.BtCommandObject
import com.nyp.fypj.smartbackpackapp.bluetooth.BtWrapper
import com.nyp.fypj.smartbackpackapp.bluetooth.DeviceListActivity
import com.nyp.sit.fypj.smartbackpackapp.Constants
import com.nyp.sit.fypj.smartbackpackapp.bluetooth.BluetoothService

class MainActivitytest : AppCompatActivity() {

    /**
     * Old main activity class for bluetooth testing purposes only
     * codes bellow for reference on how to implement the wrapper class
     *
     * handler class is needed to make changes to the ui when a response is received
     *
     * on start is needed only when the activity need bluetooth capabilities
     *
     *
     *
     *
     *
     *
     */
    var REQUEST_CONNECT_DEVICE = 1

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBtWrapper : BtWrapper? = null

    var display: EditText? = null
    var button: Button? = null
    var stop_button: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_test)

        /**
         * Setup bluetooth communication layer
         */
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mBtWrapper = BtWrapper(mHandler)

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show()
            this!!.finish()
        }


        /**
         * Controls
         */
        button = findViewById(R.id.button)
        button!!.setOnClickListener {
            val serverIntent = Intent(this, DeviceListActivity::class.java)
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE)
        }

        stop_button = findViewById(R.id.btn_stop)
        stop_button!!.setOnClickListener {
            mBtWrapper!!.disconnectDevice()
        }

        this.display = findViewById(R.id.display)

        var senbtn = findViewById<Button>(R.id.button2)
        senbtn.setOnClickListener {
            //send the command to the device
            mBtWrapper!!.getSensorData()
        }
    }

    public override fun onStart() {
        /**
         * Check for Bluetooth capabilities, if none, ask for access
         */
        super.onStart()
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, 3)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CONNECT_DEVICE ->
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data!!, true)
                }
        }
    }

    private fun connectDevice(data: Intent, secure: Boolean) {
        // Get the device MAC address
        val address = data.extras!!
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
        // Connect to IOT Device
        mBtWrapper!!.connectDevice(address)
    }

    /**
     * The Handler that gets information back from the BluetoothService
     * Handler identifiers can be found in Constants Class
     */
    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            Log.d(TAG, msg.what.toString())
            when (msg.what) {
                /**
                 * Depreciated
                 */
//                Constants.HANDLER_MESSAGE_RECEIVED -> {
//                    val readBuf = msg.obj as ByteArray
//                    // construct a string from the valid bytes in the buffer
//                    val readMessage = String(readBuf, 0, msg.arg1)
//                    display!!.setText(readMessage)
//                }
//                Constants.HANDLER_ACTION.TOAST.value -> {
//                    var content = msg.data.getString(Constants.HANDLER_DATA_KEY.TOAST_CONTENT.value)
//                    Toast.makeText(this@MainActivitytest, content, Toast.LENGTH_LONG).show()
//                }
//                Constants.HANDLER_MESSAGE_SEND -> {
//                    Toast.makeText(this@MainActivitytest,"Message Send", Toast.LENGTH_SHORT).show()
//                }
//                Constants.HANDLER_STATE_CHANGE -> {
//                    var mConnectedDeviceName = msg.data.getString(Constants.DEVICE_NAME)
//                    Toast.makeText(this@MainActivitytest, "Connected to $mConnectedDeviceName", Toast.LENGTH_SHORT).show()
//                }
                /**
                 * Active
                 */
                Constants.HANDLER_ACTION.DISPLAY_SENSOR_DATA.value ->{
                    //receive the response and handle the UI Changes
                    var mBtCommandObject = msg.obj as BtCommandObject
                    var text = mBtCommandObject.data.toString()
                    display!!.setText(text)
                }
            }
        }
    }

    companion object {
        var TAG = "main"
    }

}
