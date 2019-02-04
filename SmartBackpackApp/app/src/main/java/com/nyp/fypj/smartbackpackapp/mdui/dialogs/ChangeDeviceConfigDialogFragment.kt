package com.nyp.fypj.smartbackpackapp.mdui.dialogs

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.Button
import com.nyp.fypj.smartbackpackapp.R
import com.nyp.fypj.smartbackpackapp.service.IotDeviceConfigManager
import com.sap.cloud.android.odata.sbp.IotdeviceinfoType


class ChangeDeviceConfigDialogFragment() : DialogFragment() {

    lateinit var yes: Button
    lateinit var no:Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(layoutInflater.inflate(R.layout.dialog_change_device_setting, null))
                    // Add action buttons
                    .setPositiveButton("Update backpack Settings"
                    ) { dialog, id ->
                        //iotDeviceConfigManager.commitChanges()
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.cancel
                    ) { dialog, id ->
                        dialog.cancel()
                    }
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }

//     fun onCreateo(savedInstanceState: Bundle) {
//        super.onCreate(savedInstanceState)
//
////        requestWindowFeature(Window.FEATURE_NO_TITLE)
////        setContentView(R.layout.dialog_change_device_setting)
//
//
//
//        val connectedDevice =
//
//        spf_device_name.value = connectedDevice.deviceName
//        sfc_enable_buzzer.setValue(connectedDevice.configEnableBuzzer == "Y")
//        sfc_enable_led.setValue(connectedDevice.configEnableLed == "Y")
//        sl_record_interval.value = connectedDevice.minutesToRecordData
//
//        sfc_enable_buzzer.cellValueChangeListener = object : FormCell.CellValueChangeListener<Boolean>() {
//            override fun cellChangeHandler(value: Boolean) {
//                iotDeviceConfigManager.toggleBuzzerNow(value)
//            }
//        }
//
//        sfc_enable_led.cellValueChangeListener = object : FormCell.CellValueChangeListener<Boolean>() {
//            override fun cellChangeHandler(value: Boolean) {
//                iotDeviceConfigManager.toggleLedNow(value)
//            }
//        }
//
//        sl_record_interval.cellValueChangeListener = object : FormCell.CellValueChangeListener<Int>() {
//            override fun cellChangeHandler(value: Int) {
//                sl_record_interval.displayValue = "$value Minutes"
//            }
//        }
//
//        btn_yes.setOnClickListener{
//            iotDeviceConfigManager.commitChanges()
//            activity.finish()
//        }
//
//        btn_no.setOnClickListener{
//            activity.finish()
//        }
//
//    }

    companion object {
        private const val TAG = "ChangeDeviceConfigDf"

        fun newInstance(activity:Activity,iotDeviceConfigManager:IotDeviceConfigManager ,connectedDevice: IotdeviceinfoType):ChangeDeviceConfigDialogFragment{

            val f = ChangeDeviceConfigDialogFragment()

            // Supply num input as an argument.
            val args = Bundle()
            args.putParcelable("connectedDevice",connectedDevice)
            f.setArguments(args)

            return f
        }
    }
}