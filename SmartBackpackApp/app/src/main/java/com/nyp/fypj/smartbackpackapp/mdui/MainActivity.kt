package com.nyp.fypj.smartbackpackapp.mdui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
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

    private lateinit var sapServiceManager: SAPServiceManager
    private lateinit var configurationData: ConfigurationData
    private lateinit var secureStoreManager: SecureStoreManager


    /*
    Utilities
     */
    private var okHttpClient: OkHttpClient? = null
    private var settingsParameter: SettingsParameters? = null

    private var userProfile: UserinfosType? = null
    private val userDevices: ArrayList<IotdeviceinfoType> = ArrayList()


    /*
    UI Components
     */
    private var homeFragment: Fragment? = null
    private var myDevicesFragment: Fragment? = null
    private var myProfileFragment: Fragment? = null

    val fm = supportFragmentManager
    private var active = homeFragment
    private var homeDisabled:Boolean = false

    private var loadingBar:FioriProgressBar? = null

    private var homeFab: FloatingActionButton? = null
    private var myDevicesFab: FloatingActionButton? = null



    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                if(!homeDisabled) {
                    fm.beginTransaction().hide(active!!).show(homeFragment!!).commit()
                    active = homeFragment
                    title = userDevices[0].deviceName
                }else{
                    Toast.makeText(this,"No Backpack Connected, Please select a device from your backpack list",Toast.LENGTH_LONG).show()
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_my_devices -> {
                fm.beginTransaction().hide(active!!).show(myDevicesFragment!!).commit()
                active = myDevicesFragment
                title = "My Backpacks (${userDevices.size})"
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_my_profile -> {
                fm.beginTransaction().hide(active!!).show(myProfileFragment!!).commit()
                active = myProfileFragment
                title = "My Profile"
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.main_toolbar))
        title = "My Backpack"

        loadingBar = findViewById(R.id.indeterminateBar)

        sapServiceManager = (application as SAPWizardApplication).sapServiceManager
        configurationData = (application as SAPWizardApplication).configurationData
        secureStoreManager = (application as SAPWizardApplication).secureStoreManager

        val mBasicAuthPersistentCredentialStore = BasicAuthPersistentCredentialStore(secureStoreManager)
        val credential = mBasicAuthPersistentCredentialStore.getCredential(configurationData.serviceUrl,"SAP HANA Cloud Platform")

        val settingsParameters = SettingsParameters(configurationData.serviceUrl, this.packageName, Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID), "0.0.0.1")
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

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ), Constants.ACTIVITY_RESULT_CODE.REQUEST_CONNECT_DEVICE.value)
            }
        } else {
            if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

                startActivityForResult(enableIntent, Constants.ACTIVITY_RESULT_CODE.REQUEST_CONNECT_DEVICE.value)
            }else{
                StartUserDeviceSession()
            }
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
                sapServiceManager.openODataStore {

                    /*
                    Retrieve user profile from database
                     */
                    val userProfileQuery = DataQuery()
                            .filter(UserinfosType.userId.equal(o.id))

                    sapServiceManager.getsbp().getUserinfosAsync(userProfileQuery,
                            {userInfos:List<UserinfosType>->
                                Log.e(TAG, "user " + userInfos.size.toString())
                                if(userInfos.size == 1){
                                    userProfile = userInfos[0]

                                    /*
                                    Retrieve user devices from database
                                     */
                                    val userDeviceQuery = DataQuery()
                                            .filter(IotdeviceinfoType.userId.equal(o.id))
                                            .orderBy(IotdeviceinfoType.lastOnline)

                                    sapServiceManager.getsbp().getIotdeviceinfoAsync(userDeviceQuery,
                                            {deviceList:List<IotdeviceinfoType>->
                                                Log.e(TAG, deviceList.size.toString())

                                                userDevices.addAll(deviceList)

                                                homeFragment = HomeFragment()
                                                myDevicesFragment = MyDevicesFragment()
                                                myProfileFragment = MyProfileFragment()

                                                val fragmentBundles = Bundle()
                                                fragmentBundles.putParcelable("userProfile",userProfile!!)
                                                fragmentBundles.putParcelableArrayList("userDevices",userDevices)

                                                homeFragment!!.arguments = fragmentBundles
                                                myProfileFragment!!.arguments = fragmentBundles
                                                myDevicesFragment!!.arguments = fragmentBundles

                                                fm.beginTransaction().add(R.id.main_container, myProfileFragment!!, "3").hide(myProfileFragment!!).commit()
                                                fm.beginTransaction().add(R.id.main_container, myDevicesFragment!!, "2").hide(myDevicesFragment!!).commit()
                                                fm.beginTransaction().add(R.id.main_container,homeFragment!!, "1").show(homeFragment!!).commit()
                                                active = homeFragment
                                                navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

                                                if(deviceList.isEmpty()){
                                                    homeDisabled = true
                                                    active = myDevicesFragment
                                                }

                                                loadingBar!!.visibility = View.INVISIBLE
                                            },
                                            {re:RuntimeException->
                                                Log.d(TAG, "An error occurred during async query:  "  + re.message);
                                                active = myDevicesFragment
                                            })
                                }
                            },
                            {re:RuntimeException->
                                Log.d(TAG, "An error occurred during async query:  "  + re.message)
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

    companion object {
        private const val TAG = "main"
    }
}
