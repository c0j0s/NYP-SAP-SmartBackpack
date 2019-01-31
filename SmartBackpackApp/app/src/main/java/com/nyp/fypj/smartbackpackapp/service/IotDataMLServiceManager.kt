package com.nyp.fypj.smartbackpackapp.service

import com.google.gson.Gson
import com.sap.cloud.android.odata.sbp.IotDataType
import com.sap.cloud.android.odata.sbp.SuggestionsType
import com.sap.cloud.android.odata.sbp.UserinfosType
import com.sap.cloud.mobile.odata.DataQuery
import okhttp3.*
import java.io.IOException
import java.lang.RuntimeException
import java.security.InvalidParameterException
import okhttp3.RequestBody
import android.util.Log
import com.google.gson.reflect.TypeToken
import com.nyp.fypj.smartbackpackapp.app.ConfigurationData

class IotDataMLServiceManager(private val sapServiceManager: SAPServiceManager,private val configurationData: ConfigurationData) {

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
    }

    //NOT TESTED
    fun setDataFeedback(USER_ID:String,DATA_ID:Long,level:Int,success:() -> Unit,error:(e:RuntimeException) -> Unit) {
        if (level in 0..4) {
            data.userId = USER_ID
            data.dataId = DATA_ID
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

    //NOT TESTED
    fun getFeedbackLabel(level:Int):String{
        if (level in 0..4) {
            return feedbackDescription[level]!!
        } else {
            throw InvalidParameterException()
        }
    }

    fun getLevelAndSuggestion(user: UserinfosType, data: IotDataType, success: (level:Int,suggestion:SuggestionsType) -> Unit,error:(e:RuntimeException) -> Unit){
        predictComfortLevel(data,user, {
            level ->
            var query = DataQuery().filter(SuggestionsType.comfortLevel.equal(level))
            getSuggestions(query,{
                suggestion ->
                success(level,suggestion)
            },{e: RuntimeException ->
                error(e)
            })
        },{
            throw IOException()
        })
    }

    //NOT TESTED
    fun getSuggestions(suggestionQuery: DataQuery,success:(suggestion:SuggestionsType) -> Unit,error:(e:RuntimeException) -> Unit){
        sapServiceManager.openODataStore {
            sapServiceManager.getsbp().getSuggestionsAsync(suggestionQuery, {
                suggestionList:List<SuggestionsType> ->
                if (suggestionList.isNotEmpty()){
                    success(suggestionList[0])
                }else{
                    error("No suggestions found")
                }

            }, { e: RuntimeException ->
                error(e)
            })
        }
    }

    fun predictComfortLevel(data: IotDataType, user: UserinfosType, success: (level:Int) -> Unit, error: (e: IOException) -> Unit){
        val input = hashMapOf<String,Float>()
        input["HUMIDITY"] = data.humidity.toFloat()
        input["TEMPERATURE"] = data.temperature.toFloat()
        input["PM2_5"] = data.pm25.toFloat()
        input["PM10"] = data.pm10.toFloat()
        input["ASTHMATIC_LEVEL"] = user.asthmaticLevel.toFloat()

        val gson = Gson()

        val jsonString = gson.toJson(input)

        val okHttpClient = OkHttpClient().newBuilder()
                .addInterceptor(AuthenticationInterceptor(configurationData.mlServiceAccount, configurationData.mlServicePasswd))
                .build()

        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString)
        val request = Request.Builder()
                .url(configurationData.mlServiceUrl)
                .post(requestBody)
                .build()

        try {
            okHttpClient.newCall(request).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    error(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val type = object : TypeToken<HashMap<String, Int>>() {}.getType()
                    val dataMap:HashMap<String,Int> = gson.fromJson(response.body()!!.string(), type)

                    Log.e(TAG,dataMap["PREDICTED_COMFORT_LEVEL"].toString())

                    success(dataMap["PREDICTED_COMFORT_LEVEL"]!!.toInt())
                }

            })
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    class AuthenticationInterceptor(user: String, password: String) : Interceptor {

        private val credentials: String = Credentials.basic(user, password)

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val authenticatedRequest = request.newBuilder()
                    .header("Authorization", credentials).build()
            return chain.proceed(authenticatedRequest)
        }

    }

    companion object {
        private const val TAG = "IotDataMLServiceManager"
    }
}