package com.nyp.sit.fypj.smartbackpackapp

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
import com.nyp.sit.fypj.smartbackpackapp.bluetooth.BluetoothService
import com.nyp.sit.fypj.smartbackpackapp.bluetooth.DeviceListActivity

class MainActivity : AppCompatActivity() {

    lateinit var button: Button
    var REQUEST_CONNECT_DEVICE_SECURE = 1
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mChatService: BluetoothService? = null

    var display: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mChatService = BluetoothService(mHandler)

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show()
            this!!.finish()
        }

        button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val serverIntent = Intent(this, DeviceListActivity::class.java)
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE)
        }

        var editText = findViewById<EditText>(R.id.editText)
        this.display = findViewById(R.id.display)

        var senbtn = findViewById<Button>(R.id.button2)
        senbtn.setOnClickListener {
            var byte = editText.text.toString().toByteArray()
            mChatService!!.write(byte)
        }
    }

    public override fun onStart() {
        super.onStart()
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, 3)
            // Otherwise, setup the chat session
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CONNECT_DEVICE_SECURE ->
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data!!, true)
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An [Intent] with [DeviceListActivity.EXTRA_DEVICE_ADDRESS] extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private fun connectDevice(data: Intent, secure: Boolean) {
        // Get the device MAC address
        val address = data.extras!!
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
        // Get the BluetoothDevice object
        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        // Attempt to connect to the device
        mChatService!!.connect(device, secure)
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            Log.d(TAG, msg.what.toString())
            when (msg.what) {
                Constants.HANDLER_MESSAGE_RECEIVED -> {
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val readMessage = String(readBuf, 0, msg.arg1)

                    display!!.setText(readMessage)
                }
                Constants.HANDLER_TOAST -> {
                    var mConnectedDeviceName = msg.data.getString(Constants.DEVICE_NAME)
                    Toast.makeText(this@MainActivity, "Connected to $mConnectedDeviceName", Toast.LENGTH_SHORT).show()
                }
                Constants.HANDLER_MESSAGE_SEND -> {
                    Toast.makeText(this@MainActivity,"Message Send", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        var TAG = "main"
    }

}
