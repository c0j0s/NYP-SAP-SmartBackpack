package com.nyp.fypj.smartbackpackapp.bluetooth

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class BtCommandObject(var function_code: String, var data: HashMap<String,String>, var end_code: String, var debug: String = ""): Parcelable {

}