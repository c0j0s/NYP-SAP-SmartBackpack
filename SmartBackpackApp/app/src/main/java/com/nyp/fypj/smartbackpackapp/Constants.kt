package com.nyp.fypj.smartbackpackapp

class Constants {

    enum class ACTIVITY_RESULT_CODE(val value: Int){
        REQUEST_CONNECT_DEVICE(3),
        REQUEST_LOCATION(3)
    }

    enum class HANDLER_ACTION(val value: Int) {
        //system action
        TOAST(10),

        //iot status action
        CONNECTED(20),
        DISCONNECTED(21),
        CONNECT_ERROR(22),
        CONNECT_LOST(23),

        //app action status
        COMMAND_SEND(30),
        RECEIVE_RESPONSE(31),
        RECEIVE_ERROR(32),

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
        FLUSH_HOLDING_ZONE("32500"),

        TOGGLE_DEBUG("41000"),
        EXE_SH("42000"),
        GET_NETWORK_IP("43000"),
        BUZZER_TEST("44000"),
        ELSE("-1"),

    }

    enum class BT_END_CODE(val code: String) {
        EOT("EOT"),
        MSE("MSE"),
        ERR("ERR")
    }

    enum class TAB_PAGE() {
        HOME,
        MY_DEVICE,
        MY_PROFILE
    }
}