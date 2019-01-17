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

    fun sendCommand(command:Constants.BT_FUN_CODE,Data:HashMap<String,String>){
        mBtCommandObject = BtCommandObject(
                command.code,
                Data,
                Constants.BT_END_CODE.EOT.code)
        mBluetoothService!!.write(mBtCommandObject!!)
    }

    @Synchronized
    fun connectDevice(deviceBluetoothAddress:String){
        Log.i(TAG,deviceBluetoothAddress)
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

    fun getSensorStatus(){
        sendCommand(Constants.BT_FUN_CODE.GET_SENSOR_STATUS)
    }

    fun restartSensorService(){
        sendCommand(Constants.BT_FUN_CODE.RESTART_SENSOR_SERVICE)
    }

    fun getBluetoothStatus(){
        sendCommand(Constants.BT_FUN_CODE.GET_BLUETOOTH_STATUS)
    }

    fun restartBluetoothService(){
        sendCommand(Constants.BT_FUN_CODE.RESTART_BLUETOOTH_SERVICE)
    }

    fun syncHoldingZone(){
        sendCommand(Constants.BT_FUN_CODE.SYNC_HOLDING_ZONE)
    }

    fun flushHoldingZone(){
        sendCommand(Constants.BT_FUN_CODE.FLUSH_HOLDING_ZONE)
    }

    fun changeDeviceSettings(key:String,value:String){
        var data = HashMap<String,String>()
        data[key] = value
        sendCommand(Constants.BT_FUN_CODE.CHANGE_DEVICE_SETTINGS,data)
    }

    fun toggleDebug(){
        sendCommand(Constants.BT_FUN_CODE.TOGGLE_DEBUG)
    }

    fun restartDevice(){
        sendCommand(Constants.BT_FUN_CODE.REBOOT_DEVICE)
    }

    fun exeShCommand(command:String){
        var data = HashMap<String,String>()
        data["command"] = command
        sendCommand(Constants.BT_FUN_CODE.EXE_SH,data)
    }

    fun elseTest(){
        sendCommand(Constants.BT_FUN_CODE.ELSE)
    }

    companion object {
        var TAG = "BTWrapper"
    }
}