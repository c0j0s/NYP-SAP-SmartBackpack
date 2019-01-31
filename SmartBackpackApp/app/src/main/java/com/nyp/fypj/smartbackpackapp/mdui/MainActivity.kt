package com.nyp.fypj.smartbackpackapp.mdui

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.nyp.fypj.smartbackpackapp.R
import com.nyp.fypj.smartbackpackapp.app.ConfigurationData
import com.nyp.fypj.smartbackpackapp.app.SAPWizardApplication
import com.nyp.fypj.smartbackpackapp.bluetooth.BtCommandObject
import com.nyp.fypj.smartbackpackapp.bluetooth.BtWrapper
import com.nyp.fypj.smartbackpackapp.bluetooth.HoldingZoneData
import com.nyp.fypj.smartbackpackapp.logon.AuthenticationInterceptor
import com.nyp.fypj.smartbackpackapp.logon.BasicAuthPersistentCredentialStore
import com.nyp.fypj.smartbackpackapp.logon.SecureStoreManager
import com.nyp.fypj.smartbackpackapp.mdui.fragments.HomeFragment
import com.nyp.fypj.smartbackpackapp.mdui.fragments.MyDevicesFragment
import com.nyp.fypj.smartbackpackapp.mdui.fragments.MyProfileFragment
import com.nyp.fypj.smartbackpackapp.service.IotDeviceConfigManager
import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager
import com.nyp.fypj.smartbackpackapp.Constants
import com.nyp.fypj.smartbackpackapp.service.IotDataMLServiceManager
import com.sap.cloud.android.odata.sbp.IotDataType
import com.sap.cloud.android.odata.sbp.IotdeviceinfoType
import com.sap.cloud.android.odata.sbp.UserinfosType
import com.sap.cloud.mobile.fiori.indicator.FioriProgressBar
import com.sap.cloud.mobile.foundation.common.ClientProvider
import com.sap.cloud.mobile.foundation.common.CpmsParameters
import com.sap.cloud.mobile.foundation.common.SettingsParameters
import com.sap.cloud.mobile.foundation.networking.AppHeadersInterceptor
import com.sap.cloud.mobile.foundation.networking.CsrfTokenInterceptor
import com.sap.cloud.mobile.foundation.networking.WebkitCookieJar
import com.sap.cloud.mobile.foundation.user.UserInfo
import com.sap.cloud.mobile.foundation.user.UserRoles
import com.sap.cloud.mobile.odata.DataQuery
import com.sap.cloud.mobile.odata.http.HttpException
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory

class MainActivity : AppCompatActivity() {

    /*
    SAP Services
     */
    private val LOGGER = LoggerFactory.getLogger(MainActivity::class.java)

    private var sapServiceManager: SAPServiceManager? = null
    private var configurationData: ConfigurationData? = null
    private var secureStoreManager: SecureStoreManager? = null


    /*
    Utilities
     */
    private var okHttpClient: OkHttpClient? = null
    private var settingsParameter: SettingsParameters? = null
    private var btWrapper:BtWrapper? = null
    private var iotDeviceConfigManager: IotDeviceConfigManager? = null

    private var userProfile: UserinfosType? = null
    private var userDevices: List<IotdeviceinfoType>? = null


    /*
    UI Components
     */
    private val homeFragment: Fragment = HomeFragment()
    private val myDevicesFragment: Fragment = MyDevicesFragment()
    private val myProfileFragment: Fragment = MyProfileFragment()
    private val fm = supportFragmentManager
    private var active = homeFragment
    private var homeDisabled:Boolean = false

    private var loadingBar:FioriProgressBar? = null


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                if(!homeDisabled) {
                    fm.beginTransaction().hide(active).show(homeFragment).commit()
                    active = homeFragment
                }else{
                    Toast.makeText(this,"No Backpack Connected, Please select a device from your backpack list",Toast.LENGTH_LONG).show()
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_my_devices -> {
                fm.beginTransaction().hide(active).show(myDevicesFragment).commit()
                active = myDevicesFragment
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_my_profile -> {
                fm.beginTransaction().hide(active).show(myProfileFragment).commit()
                active = myProfileFragment
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle(R.id.action_bar_title)

        fm.beginTransaction().add(R.id.main_container, myProfileFragment, "3").hide(myProfileFragment).commit()
        fm.beginTransaction().add(R.id.main_container, myDevicesFragment, "2").hide(myDevicesFragment).commit()
        fm.beginTransaction().add(R.id.main_container,homeFragment, "1").hide(homeFragment).commit()
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        loadingBar = findViewById(R.id.indeterminateBar)

        sapServiceManager = (application as SAPWizardApplication).sapServiceManager
        configurationData = (application as SAPWizardApplication).configurationData
        secureStoreManager = (application as SAPWizardApplication).secureStoreManager

        val mBasicAuthPersistentCredentialStore = BasicAuthPersistentCredentialStore(secureStoreManager)
        val credential = mBasicAuthPersistentCredentialStore.getCredential(configurationData!!.serviceUrl,"SAP HANA Cloud Platform")

        val settingsParameters = SettingsParameters(configurationData!!.serviceUrl, this.packageName, Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID), "0.0.0.1")
        CpmsParameters.init(settingsParameters)
        val appHeadersInterceptor = AppHeadersInterceptor(CpmsParameters.getSettingsParameters())
        ClientProvider.set(OkHttpClient.Builder()
                .addInterceptor(appHeadersInterceptor)
                .addInterceptor(CsrfTokenInterceptor(
                        CpmsParameters.getSettingsParameters().backendUrl))
                .addInterceptor(AuthenticationInterceptor(credential!![0], credential[1]))
                .cookieJar(WebkitCookieJar())
                .build())

        okHttpClient = ClientProvider.get()
        settingsParameter = CpmsParameters.getSettingsParameters()
    }

    public override fun onStart() {
        /**
         * Check for Bluetooth capabilities, if none, ask for access, else get user logon information
         */
        super.onStart()
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, Constants.ACTIVITY_RESULT_CODE.REQUEST_CONNECT_DEVICE.value)
        }else{
            btWrapper = BtWrapper(mHandler)
            StartUserDeviceSession()
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LOGGER.debug("EntitySetListActivity::onActivityResult, request code: $requestCode result code: $resultCode")

        when (requestCode) {
            Constants.ACTIVITY_RESULT_CODE.REQUEST_CONNECT_DEVICE.value ->
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    btWrapper = BtWrapper(mHandler)

                    //start logon process after bluetooth access granted
                    StartUserDeviceSession()
                }
        }
    }

    private fun StartUserDeviceSession(){
        /**
         * The following retrieves user account details
         * look for user devices from database
         * try to connect to the first device in their list
         *
         * ensure bluetooth access is granted
         */
        val roles = UserRoles(okHttpClient!!, settingsParameter!!)
        roles.load(object : UserRoles.CallbackListener {
            override fun onSuccess(o: UserInfo) {

                //open database session
                sapServiceManager!!.openODataStore {

                    /*
                    Retrieve user profile from database
                     */
                    val userProfileQuery = DataQuery()
                            .filter(UserinfosType.userId.equal(o.id))

                    sapServiceManager!!.getsbp().getUserinfosAsync(userProfileQuery,
                            {userInfos:List<UserinfosType>->
                                Log.e(TAG, "user " + userInfos.size.toString())
                                if(userInfos.size == 1){
                                    userProfile = userInfos[0]
                                }
                            },
                            {re:RuntimeException->
                                Log.d(TAG, "An error occurred during async query:  "  + re.message);
                                fm.beginTransaction().hide(active).show(myDevicesFragment).commit()
                                active = myDevicesFragment
                            })

                    /*
                    Retrieve user devices from database
                     */
                    val userDeviceQuery = DataQuery()
                            .filter(IotdeviceinfoType.userId.equal(o.id))
                            .orderBy(IotdeviceinfoType.lastOnline)

                    sapServiceManager!!.getsbp().getIotdeviceinfoAsync(userDeviceQuery,
                    {deviceList:List<IotdeviceinfoType>->
                        Log.e(TAG, deviceList.size.toString())
                        if(deviceList.size > 0){

                            userDevices = deviceList
                            //connect to user first device
                            btWrapper!!.connectDevice(deviceList[0].deviceAddress)

                        }else{

                            fm.beginTransaction().hide(active).show(myDevicesFragment).commit()
                            active = myDevicesFragment
                            loadingBar!!.visibility = View.INVISIBLE
                            homeDisabled = true

                        }

                    },
                    {re:RuntimeException->
                        Log.d(TAG, "An error occurred during async query:  "  + re.message);
                        fm.beginTransaction().hide(active).show(myDevicesFragment).commit()
                        active = myDevicesFragment
                    })
                }
            }

            override fun onError(result: Throwable) {
                if (result is HttpException) {
                    Log.e("Http Exception: ", result.message)
                } else {
                    Log.e("Exception occurred: ", result.message)
                }
            }
        })
    }

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            Log.d(TAG, msg.what.toString())
            when (msg.what) {
                //handle when device connected
                Constants.HANDLER_ACTION.CONNECTED.value->{

                    //TODO move to after sensor data receviced handler, else empty homepage will be visiable before get sensor data command
                    fm.beginTransaction().hide(active).show(homeFragment).commit()
                    active = homeFragment

                    Toast.makeText(this@MainActivity,"Backpack Connected",Toast.LENGTH_SHORT).show()
                    loadingBar!!.visibility = View.INVISIBLE
                    Log.i(TAG,"Backpack Connected")

                    //test get sensor data
                    //btWrapper!!.getSensorData()

                    //test syncholdingzone
                    //btWrapper!!.syncHoldingZone()

                    val iotDataMLServiceManager = IotDataMLServiceManager(sapServiceManager!!,configurationData!!)
                    val iotDataType = IotDataType()
                    iotDataType.humidity = 50.toDouble()
                    iotDataType.temperature = 30.toDouble()
                    iotDataType.pm25 = 100.toDouble()
                    iotDataType.pm10 = 104.toDouble()
                    iotDataMLServiceManager.getLevelAndSuggestion(userProfile!!,iotDataType,{
                        level, suggestion ->
                        Log.e(TAG,level.toString() + " " + suggestion.advise)
                    },{
                        e: java.lang.RuntimeException ->
                        Log.e(TAG,e.message)
                    })
                }
                //TODO handle when device disconnected
                Constants.HANDLER_ACTION.DISCONNECTED.value->{

                    Log.i(TAG,"Backpack disconnected")

                }
                //TODO handle when device connection lost
                Constants.HANDLER_ACTION.CONNECT_LOST.value->{

                    Log.i(TAG,"Backpack connection lost")

                }
                //TODO handle when device connection error
                Constants.HANDLER_ACTION.CONNECT_ERROR.value->{

                    Log.i(TAG,"Backpack connection error")

                }
                Constants.HANDLER_ACTION.COMMAND_SEND.value->{

                    Log.i(TAG,"Command send")

                }
                //TODO sensor data retrieved from device
                Constants.HANDLER_ACTION.RECEIVE_RESPONSE.value->{
                    /*
                    BtCommandObject -> function_code
                                       data
                                       end_code

                    refer to documentation
                     */

                    Log.i(TAG,"Backpack data received")

                    val mBtCommandObject = msg.obj as BtCommandObject
                    when(mBtCommandObject.function_code){
                        Constants.BT_FUN_CODE.GET_SENSOR_DATA.code->{

                            Toast.makeText(this@MainActivity,mBtCommandObject.data.toString(),Toast.LENGTH_LONG).show()

                        }
                        Constants.BT_FUN_CODE.SYNC_HOLDING_ZONE.code->{

                            //all received data is in hashmap form
                            Toast.makeText(this@MainActivity,mBtCommandObject.data.size.toString(),Toast.LENGTH_LONG).show()

                            //convert to list of holdingZoneData object
                            val HoldingZoneDataList: MutableList<HoldingZoneData> = mutableListOf()
                            for (keyValuePair in mBtCommandObject.data){
                                HoldingZoneDataList.add(HoldingZoneData(keyValuePair.key,keyValuePair.value.toString()))
                            }

                            //test
                            Log.e(TAG,"holdingZoneData object test: " + HoldingZoneDataList[0].humidity)

                            //send command to flush holding zone after transmission complete
                            btWrapper!!.flushHoldingZone()
                        }
                        Constants.BT_FUN_CODE.FLUSH_HOLDING_ZONE.code->{

                            Log.i(TAG,"Holding zone flushing complete")

                        }
                        Constants.BT_FUN_CODE.CHANGE_DEVICE_SETTINGS.code ->{

                            Log.i(TAG,"Device config changed, start hana sync")

                            iotDeviceConfigManager!!.syncConfigToHana({

                                Log.i(TAG,"Hana Config Updated")

                            }, {e:RuntimeException ->

                                Log.e(TAG,"Error when synchronising config to database " + e.message)

                            })


                        }
                        Constants.BT_FUN_CODE.TOGGLE_DEBUG.code->{


                        }
                        else -> {
                            //received code not supported
                            Log.i(TAG,"received code not supported: " + mBtCommandObject.function_code)
                        }
                        //TODO handle other function code responses
                    }
                }
                //TODO handle exceptions occurred at receiving end
                Constants.HANDLER_ACTION.RECEIVE_ERROR.value->{

                    Toast.makeText(this@MainActivity,"Error occurred",Toast.LENGTH_SHORT).show()

                }
                else -> {
                    //state not supported
                    Log.i(TAG,"state not supported: " + msg.what)
                }
            }
        }
    }

    companion object {
        private const val TAG = "main"
    }
}
