package com.nyp.fypj.smartbackpackapp.mdui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.provider.SyncStateContract
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
import kotlinx.android.synthetic.main.activity_main.*
import com.nyp.fypj.smartbackpackapp.mdui.fragments.HomeFragment
import com.nyp.fypj.smartbackpackapp.mdui.fragments.MyDevicesFragment
import com.nyp.fypj.smartbackpackapp.mdui.fragments.MyProfileFragment
import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager
import com.nyp.sit.fypj.smartbackpackapp.Constants
import com.sap.cloud.android.odata.sbp.IotdeviceinfoType
import com.sap.cloud.android.odata.sbp.UserDevicesType
import com.sap.cloud.mobile.fiori.indicator.FioriProgressBar
import com.sap.cloud.mobile.foundation.authentication.BasicAuthDialogAuthenticator
import com.sap.cloud.mobile.foundation.common.ClientProvider
import com.sap.cloud.mobile.foundation.common.CpmsParameters
import com.sap.cloud.mobile.foundation.common.SettingsParameters
import com.sap.cloud.mobile.foundation.networking.AppHeadersInterceptor
import com.sap.cloud.mobile.foundation.networking.CsrfTokenInterceptor
import com.sap.cloud.mobile.foundation.networking.WebkitCookieJar
import com.sap.cloud.mobile.foundation.user.UserInfo
import com.sap.cloud.mobile.foundation.user.UserRoles
import com.sap.cloud.mobile.odata.DataQuery
import com.sap.cloud.mobile.odata.core.Action1
import com.sap.cloud.mobile.odata.http.HttpException
import okhttp3.OkHttpClient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Serializable

class MainActivity : AppCompatActivity() {

    private val LOGGER = LoggerFactory.getLogger(MainActivity::class.java)

    private var sapServiceManager: SAPServiceManager? = null
    private var configurationData: ConfigurationData? = null
    private var okHttpClient: OkHttpClient? = null
    private var settingsParameter: SettingsParameters? = null
    private var btWrapper:BtWrapper? = null

    private val homeFragment: Fragment = HomeFragment()
    private val myDevicesFragment: Fragment = MyDevicesFragment()
    private val myProfileFragment: Fragment = MyProfileFragment()
    private val fm = supportFragmentManager
    private var active = homeFragment
    private var homeDisabled: Boolean? = false

    private var indeterminateBar:FioriProgressBar? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                if(!homeDisabled!!) {
                    fm.beginTransaction().hide(active).show(homeFragment).commit()
                    active = homeFragment
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

        indeterminateBar = findViewById(R.id.indeterminateBar);

        btWrapper = BtWrapper(mHandler)
        sapServiceManager = (application as SAPWizardApplication).sapServiceManager
        configurationData = (application as SAPWizardApplication).configurationData

        val settingsParameters = SettingsParameters(configurationData!!.serviceUrl, this.packageName, Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID), "0.0.0.1")
        CpmsParameters.init(settingsParameters)
        val appHeadersInterceptor = AppHeadersInterceptor(CpmsParameters.getSettingsParameters())
        ClientProvider.set(OkHttpClient.Builder()
                .addInterceptor(appHeadersInterceptor)
                .addInterceptor(CsrfTokenInterceptor(
                        CpmsParameters.getSettingsParameters().backendUrl))
                .authenticator(BasicAuthDialogAuthenticator())
                .cookieJar(WebkitCookieJar())
                .build())

        okHttpClient = ClientProvider.get()
        settingsParameter = CpmsParameters.getSettingsParameters()

        //get user profile
        val roles = UserRoles(okHttpClient!!, settingsParameter!!)
        roles.load(object : UserRoles.CallbackListener {
            override fun onSuccess(o: UserInfo) {
                Log.e("User Name", o.userName)
                Log.e("User Id", o.id)

                //open database session
                sapServiceManager!!.openODataStore {
                    val query = DataQuery()
                            .filter(IotdeviceinfoType.userId.equal(o.id))
                            .orderBy(IotdeviceinfoType.lastOnline)

                    //get user devices
                    sapServiceManager!!.getsbp().getIotdeviceinfoAsync(query,
                    {deviceList:List<IotdeviceinfoType>->
                        Log.e(TAG, deviceList.size.toString())
                        if(deviceList.size > 0){
                            //indeterminateBar!!.isIndeterminate = false

                            //connect to user first device
                            Log.e("Device Address", deviceList[0].deviceAddress)
                            btWrapper!!.connectDevice(deviceList[0].deviceAddress)
                        }else{
                            fm.beginTransaction().hide(active).show(myDevicesFragment).commit()
                            active = myDevicesFragment
                            indeterminateBar!!.visibility = View.INVISIBLE
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
                //Handle error here...
                if (result is HttpException) {
                    //HttpException type com.sap.cloud.mobile.foundation.networking.HttpException
                    Log.e("Http Exception: ", result.message)
                } else {
                    Log.e("Exception occurred: ", result.message)
                }
            }
        })
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LOGGER.debug("EntitySetListActivity::onActivityResult, request code: $requestCode result code: $resultCode")
    }

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            Log.d(MainActivitytest.TAG, msg.what.toString())
            when (msg.what) {
                Constants.HANDLER_ACTION.CONNECTED.value->{
                    //Device connected

                    //TODO move to after sensor data receviced handler, else empty homepage will be visiable before get sensor data command
                    fm.beginTransaction().hide(active).show(homeFragment).commit()
                    active = homeFragment

                    Toast.makeText(this@MainActivity,"Backpack Connected",Toast.LENGTH_SHORT).show()
                    indeterminateBar!!.visibility = View.INVISIBLE
                    Log.i(TAG,"Backpack Connected")

                    //TODO start get sensor command and display the output from device
                }
                //TODO handle when device disconnected

                //TODO handle when device connection error

                //TODO sensor data retrieved from device

            }
        }
    }

    companion object {
        var TAG = "main"
    }
}
