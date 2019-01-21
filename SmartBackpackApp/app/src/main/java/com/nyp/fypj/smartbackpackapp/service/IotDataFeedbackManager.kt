package com.nyp.fypj.smartbackpackapp.service

import com.sap.cloud.android.odata.sbp.IotDataType
import java.lang.RuntimeException
import java.security.InvalidParameterException

class IotDataFeedbackManager(private val sapServiceManager: SAPServiceManager,USER_ID:String,DATA_ID:Long) {

    private val data:IotDataType = IotDataType()

    private val feedbackDescription:HashMap<Int,String> = hashMapOf(
            Pair(0,"Very Good"),
            Pair(1,"Ok"),
            Pair(2,"Uncomfortable"),
            Pair(3,"Very Uncomfortable"),
            Pair(4,"Hazardous")
    )

    init {
        data.rememberOld()
        data.userId = USER_ID
        data.dataId = DATA_ID
    }

    //NOT TESTED
    fun setDataFeedback(level:Int,success:() -> Unit,error:(e:RuntimeException) -> Unit) {
        if (level in 0..4) {
            sapServiceManager.openODataStore {
                sapServiceManager.getsbp().updateEntityAsync(data, {
                    success()
                }, { e: RuntimeException ->
                    error(e)
                })
            }
        } else {
            throw InvalidParameterException()
        }
    }

    fun getFeedbackLabel(level:Int):String{
        if (level in 0..4) {
            return feedbackDescription[level]!!
        } else {
            throw InvalidParameterException()
        }
    }
}