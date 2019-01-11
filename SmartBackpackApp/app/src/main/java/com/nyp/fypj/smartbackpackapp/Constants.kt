package com.nyp.sit.fypj.smartbackpackapp

class Constants {
    companion object {
        //Standard types for handler
        val HANDLER_TOAST = 1
        val HANDLER_STATE_CHANGE = 2
        val HANDLER_MESSAGE_SEND = 3
        val HANDLER_MESSAGE_RECEIVED = 4

        //Key names
        val DEVICE_NAME = "bluetooth_device_name"
        val TOAST = "toast_content"

    }

    enum class HANDLER_ACTION(val value: Int) {
        //system action
        TOAST(10),

        //iot status action
        CONNECTED(20),
        DISCONNECTED(21),

        //app action status
        COMMAND_SEND(30),
        RECEIVE_RESPONSE(31),

        //iot response actions
        DISPLAY_SENSOR_DATA(40),
        HANDLE_HOLDING_ZONE_DATA(41),
    }

    enum class HANDLER_DATA_KEY(val value: String) {
        DEVICE_NAME("bluetooth_device_name"),
        TOAST_CONTENT("toast_content"),
    }

    //Bluetooth communication commands
    enum class BT_FUN_CODE(val code: String) {
        DISCONNECT("00000"),
        REBOOT_DEVICE("10000"),

        RESTART_SENSOR_SERVICE("11000"),
        GET_SENSOR_STATUS("11500"),

        RESTART_BLUETOOTH_SERVICE("12000"),
        GET_BLUETOOTH_STATUS("12500"),

        GET_SENSOR_DATA("30000"),
        CHANGE_DEVICE_SETTINGS("31000"),
        SYNC_HOLDING_ZONE("32000"),

        TOGGLE_DEBUG("41000"),
        EXE_SH("42000"),
        ELSE("-1")
    }

    enum class BT_END_CODE(val code: String) {
        EOT("EOT"),
        MSE("MSE"),
        ERR("ERR")
    }
}