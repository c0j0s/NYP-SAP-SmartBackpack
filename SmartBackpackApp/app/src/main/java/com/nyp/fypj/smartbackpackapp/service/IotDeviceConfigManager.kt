package com.nyp.fypj.smartbackpackapp.service

import com.nyp.fypj.smartbackpackapp.bluetooth.BtWrapper
import com.sap.cloud.android.odata.sbp.UserDevicesType

class IotDeviceConfigManager(
        private val btWrapper: BtWrapper,
        private val sapServiceManager:SAPServiceManager,
        USER_ID:String,
        DEVICE_SN:String) {

    private val updateDevice = UserDevicesType()
    private val listOfConfigToChange = HashMap<String,String>()

    init {
        updateDevice.userId = USER_ID
        updateDevice.deviceSn = DEVICE_SN
        updateDevice.rememberOld()
    }

    fun syncConfigToHana(success: (updateDevice:UserDevicesType) -> Unit,error: (e:RuntimeException) -> Unit){
        sapServiceManager.openODataStore {
            sapServiceManager.getsbp().updateEntityAsync(updateDevice,{
                success(updateDevice)
            },{ e:RuntimeException ->
                error(e)
            })
        }
    }

    fun toggleBuzzer(enable:Boolean){
        listOfConfigToChange["CONFIG_ENABLE_BUZZER"] = enable.toString()
        updateDevice.configEnableBuzzer = convertBoolean(enable)
    }

    fun toggleLed(enable:Boolean){
        listOfConfigToChange["CONFIG_ENABLE_LED"] = enable.toString()
        updateDevice.configEnableLed = convertBoolean(enable)
    }

    fun changeHoldingZoneRecordInterval(minutes:Int){
        listOfConfigToChange["MINUTES_TO_RECORD_DATA"] = minutes.toString()
        updateDevice.minutesToRecordData = minutes
    }

    fun commitChanges(){
        btWrapper.changeDeviceMultipleSettings(listOfConfigToChange)
    }

    private fun convertBoolean(value:Boolean):String {
        if(value) {
            return "Y"
        }else{
            return "N"
        }
    }

    fun toggleBuzzerNow(value: Boolean) {
        updateDevice.configEnableBuzzer = convertBoolean(value)
        btWrapper.changeDeviceSettings("CONFIG_ENABLE_BUZZER",value.toString())
    }

    fun toggleLedNow(value: Boolean) {
        updateDevice.configEnableLed = convertBoolean(value)
        btWrapper.changeDeviceSettings("CONFIG_ENABLE_LED",value.toString())
    }

    companion object {
        const val TAG = "IotDeviceConfigManager"
    }

}