package com.nyp.fypj.smartbackpackapp.bluetooth

import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.util.Log
import com.nyp.fypj.smartbackpackapp.Constants
import java.lang.RuntimeException


class BtWrapper(private val displayHandler : Handler) {

    private val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val mBluetoothService: BluetoothService = BluetoothService(this.displayHandler)
    private var mBtCommandObject: BtCommandObject? = null

    init {
        if (!mBluetoothAdapter.isEnabled) {
            throw RuntimeException("Bluetooth Not Configured")
        }
    }

    fun sendCommand(command: Constants.BT_FUN_CODE){
        mBtCommandObject = BtCommandObject(
                command.code,
                HashMap(),
                Constants.BT_END_CODE.EOT.code)
        mBluetoothService.write(mBtCommandObject!!)
    }

    fun sendCommand(command: Constants.BT_FUN_CODE, Data:HashMap<String,String>){
        mBtCommandObject = BtCommandObject(
                command.code,
                Data,
                Constants.BT_END_CODE.EOT.code)
        mBluetoothService.write(mBtCommandObject!!)
    }

    @Synchronized
    fun connectDevice(deviceBluetoothAddress:String){
        Log.i(TAG,deviceBluetoothAddress)
        val device = mBluetoothAdapter.getRemoteDevice(deviceBluetoothAddress)
        mBluetoothService.connect(device)
    }

    @Synchronized
    fun disconnectDevice(){
        sendCommand(Constants.BT_FUN_CODE.DISCONNECT)
        mBluetoothService.stop()
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
        val data = HashMap<String,String>()
        data[key] = value
        sendCommand(Constants.BT_FUN_CODE.CHANGE_DEVICE_SETTINGS,data)
    }

    fun changeDeviceMultipleSettings(list: HashMap<String,String>){
        sendCommand(Constants.BT_FUN_CODE.CHANGE_DEVICE_SETTINGS,list)
    }

    fun toggleDebug(){
        sendCommand(Constants.BT_FUN_CODE.TOGGLE_DEBUG)
    }

    fun buzzerTest(){
        sendCommand(Constants.BT_FUN_CODE.BUZZER_TEST)
    }

    fun getNetworkIp(){
        sendCommand(Constants.BT_FUN_CODE.GET_NETWORK_IP)
    }

    fun restartDevice(){
        sendCommand(Constants.BT_FUN_CODE.REBOOT_DEVICE)
    }

    fun exeShCommand(command:String){
        val data = HashMap<String,String>()
        data["command"] = command
        sendCommand(Constants.BT_FUN_CODE.EXE_SH,data)
    }

    fun elseTest(){
        sendCommand(Constants.BT_FUN_CODE.ELSE)
    }

    companion object {
        private const val TAG = "BTWrapper"
    }
}