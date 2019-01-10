package com.nyp.fypj.smartbackpackapp.bluetooth

import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.util.Log
import com.nyp.sit.fypj.smartbackpackapp.Constants
import com.nyp.sit.fypj.smartbackpackapp.bluetooth.BluetoothService

class BtWrapper(private val displayHandler : Handler) {

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothService: BluetoothService? = null
    private var mBtCommandObject: BtCommandObject? = null

    init {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mBluetoothService = BluetoothService(this.displayHandler)
    }

    fun sendCommand(command:Constants.BT_FUN_CODE){
        mBtCommandObject = BtCommandObject(
                command.code,
                HashMap<String,String>(),
                Constants.BT_END_CODE.EOT.code)
        mBluetoothService!!.write(mBtCommandObject!!)
    }

    @Synchronized
    fun connectDevice(deviceBluetoothAddress:String){
        val device = mBluetoothAdapter!!.getRemoteDevice(deviceBluetoothAddress)
        mBluetoothService!!.connect(device)
    }

    @Synchronized
    fun disconnectDevice(){
        sendCommand(Constants.BT_FUN_CODE.DISCONNECT)
        mBluetoothService!!.stop()
    }

    fun getSensorData(){
        sendCommand(Constants.BT_FUN_CODE.GET_SENSOR_DATA)
    }

    fun syncHoldingZone(){
        sendCommand(Constants.BT_FUN_CODE.SYNC_HOLDING_ZONE)
    }

    fun changeDeviceSettings(key:String,value:String){
        throw NotImplementedError()
    }

    fun toggleDebug(){
        sendCommand(Constants.BT_FUN_CODE.TOGGLE_DEBUG)
    }

    fun restartDevice(){
        sendCommand(Constants.BT_FUN_CODE.REBOOT_DEVICE)
    }

    companion object {
        var TAG = "BTWrapper"
    }
}