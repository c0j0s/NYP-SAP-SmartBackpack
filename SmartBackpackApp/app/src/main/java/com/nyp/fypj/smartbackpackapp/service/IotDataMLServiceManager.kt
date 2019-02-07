package com.nyp.fypj.smartbackpackapp.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
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
import com.sap.cloud.android.odata.sbp.IotdeviceinfoType
import com.sap.cloud.mobile.odata.LocalDateTime
import com.sap.cloud.mobile.odata.SortOrder

class IotDataMLServiceManager(private val sapServiceManager: SAPServiceManager?,private val configurationData: ConfigurationData?) {

    private val data:IotDataType = IotDataType()
    var mlServiceStatus = false

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

    @SuppressLint("MissingPermission")
    fun setDataFeedback(activity:Context,userProfile:UserinfosType,connectedDevice:IotdeviceinfoType,realTimeData:IotDataType,feedbackLevel:Int,predictedLevel:Int,success:() -> Unit,error:(e:RuntimeException) -> Unit) {
        if (feedbackLevel in 0..4) {

            val lm: LocationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var longitude = 0.0
            var latitude = 0.0
            if (lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
                val location: Location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                longitude = location.longitude
                latitude = location.latitude
            }

            sapServiceManager!!.openODataStore {
                val lastIdQuery = DataQuery().top(1).orderBy(IotDataType.dataId, SortOrder.DESCENDING)
                sapServiceManager.getsbp().getIotDataAsync(lastIdQuery, {

                    val createData = IotDataType()
                    createData.dataId = it[0].dataId + 1
                    createData.userId = userProfile.userId
                    createData.deviceSn = connectedDevice.deviceSn
                    createData.pm10 = realTimeData.pm10
                    createData.pm25 = realTimeData.pm25
                    createData.temperature = realTimeData.temperature.toDouble()
                    createData.humidity = realTimeData.humidity
                    createData.alertTriggered = "N"
                    createData.recordedOn = LocalDateTime.now()
                    createData.city = "SINGAPORE"
                    createData.country = "SINGAPORE"
                    createData.state = "SINGAPORE"
                    createData.countryCode = "SG"
                    createData.geoLat = latitude
                    createData.geoLng = longitude

                    createData.userFeedbackComfortLevel = feedbackLevel
                    createData.predictedComfortLevel = predictedLevel

                    sapServiceManager.getsbp().createEntityAsync(createData, {
                        success()
                    }, { e: RuntimeException ->
                        error(e)
                    })
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

    fun getLevelAndSuggestion(user: UserinfosType, data: IotDataType, success: (level:Int,suggestion:SuggestionsType) -> Unit,error:(e:Any) -> Unit){
        predictComfortLevel(data,user, {
            level ->
            val query = DataQuery().filter(SuggestionsType.comfortLevel.equal(level))
            getSuggestions(query,{
                suggestion ->
                success(level,suggestion)
            },{e: RuntimeException ->
                mlServiceStatus = false
                error(e)
            })
        },{
            mlServiceStatus = false
            error(it)
        })
    }

    //NOT TESTED
    fun getSuggestions(suggestionQuery: DataQuery,success:(suggestion:SuggestionsType) -> Unit,error:(e:RuntimeException) -> Unit){
        sapServiceManager!!.openODataStore {
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

    public fun predictComfortLevel(data: IotDataType, user: UserinfosType, success: (level:Int) -> Unit, error: (e: IOException) -> Unit){
        val input = hashMapOf<String,Float>()
        input["HUMIDITY"] = data.humidity.toFloat()
        input["TEMPERATURE"] = data.temperature.toFloat()
        input["PM2_5"] = data.pm25.toFloat()
        input["PM10"] = data.pm10.toFloat()
        input["ASTHMATIC_LEVEL"] = user.asthmaticLevel.toFloat()

        val gson = Gson()

        val jsonString = gson.toJson(input)

        val okHttpClient = OkHttpClient().newBuilder()
                .addInterceptor(AuthenticationInterceptor(configurationData!!.mlServiceAccount, configurationData.mlServicePasswd))
                .build()

        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString)
        val request = Request.Builder()
                .url("http://35.198.225.149/predict")
                .post(requestBody)
                .build()

        try {
            okHttpClient.newCall(request).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    error(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val type = object : TypeToken<HashMap<String, Int>>() {}.type
                        val dataMap: HashMap<String, Int> = gson.fromJson(response.body()!!.string(), type)

                        Log.e(TAG, dataMap["PREDICTED_COMFORT_LEVEL"].toString())
                        mlServiceStatus = true
                        success(dataMap["PREDICTED_COMFORT_LEVEL"]!!.toInt())
                    }catch (e:Exception){
                        error(e)
                    }
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