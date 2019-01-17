package com.nyp.fypj.smartbackpackapp.service

import com.nyp.fypj.smartbackpackapp.bluetooth.BtWrapper
import com.sap.cloud.android.odata.sbp.UserDevicesType

class IotDeviceConfigManager(
        private var btWrapper: BtWrapper,
        private var sapServiceManager:SAPServiceManager,
        USER_ID:String,
        DEVICE_SN:String) {

    private var updateDevice = UserDevicesType()
    private var listOfConfigToChange = HashMap<String,String>()

    init {
        updateDevice.userId = USER_ID
        updateDevice.deviceSn = DEVICE_SN
        updateDevice.rememberOld()
    }

    fun syncConfigToHana(success: () -> Unit,error: (e:RuntimeException) -> Unit){
        sapServiceManager.openODataStore {
            sapServiceManager.getsbp().updateEntityAsync(updateDevice,{
                success()
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

    companion object {
        var TAG = "IotDeviceConfigManager"
    }

}