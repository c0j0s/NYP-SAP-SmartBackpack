package com.nyp.fypj.smartbackpackapp.bluetooth

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nyp.sit.fypj.smartbackpackapp.Constants
import kotlinx.android.parcel.Parcelize
import java.lang.Exception

@Parcelize
class BtCommandObject(var function_code: String, var data: HashMap<String,String>, var end_code: String, var debug: String = ""): Parcelable {

}