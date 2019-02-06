package com.nyp.fypj.smartbackpackapp.mdui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.nyp.fypj.smartbackpackapp.Constants
import com.nyp.fypj.smartbackpackapp.R
import com.nyp.fypj.smartbackpackapp.app.ConfigurationData
import com.nyp.fypj.smartbackpackapp.app.SAPWizardApplication
import com.nyp.fypj.smartbackpackapp.bluetooth.BtCommandObject
import com.nyp.fypj.smartbackpackapp.bluetooth.BtWrapper
import com.nyp.fypj.smartbackpackapp.bluetooth.HoldingZoneData
import com.nyp.fypj.smartbackpackapp.service.IotDataMLServiceManager
import com.nyp.fypj.smartbackpackapp.service.IotDeviceConfigManager
import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager
import com.sap.cloud.android.odata.sbp.IotDataType
import com.sap.cloud.android.odata.sbp.IotdeviceinfoType
import com.sap.cloud.android.odata.sbp.UserDevicesType
import com.sap.cloud.android.odata.sbp.UserinfosType
import com.sap.cloud.mobile.fiori.`object`.GridTableRow
import com.sap.cloud.mobile.fiori.formcell.FormCell
import com.sap.cloud.mobile.odata.*
import kotlinx.android.synthetic.main.components_iot_data_table_row.view.*
import kotlinx.android.synthetic.main.dialog_change_device_setting.view.*
import kotlinx.android.synthetic.main.dialog_give_feedback.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import java.lang.Exception
import java.util.*


private const val USER_PROFILE = "userProfile"
private const val USER_DEVICES = "userDevices"

class HomeFragment : Fragment() {

    private lateinit var sapServiceManager: SAPServiceManager
    private lateinit var iotDeviceConfigManager: IotDeviceConfigManager
    private lateinit var iotDataMLServiceManager: IotDataMLServiceManager
    private lateinit var configurationData: ConfigurationData
    private lateinit var btWrapper: BtWrapper

    private lateinit var userProfile: UserinfosType
    private lateinit var userDevices: ArrayList<IotdeviceinfoType>
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var connectedDevice: IotdeviceinfoType = IotdeviceinfoType()
    private var connectStatus = false
    private var predictedComfortLevel = 0
    private var realTimeDate = IotDataType()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userProfile = it.getParcelable(USER_PROFILE)!!
            userDevices = it.getParcelableArrayList(USER_DEVICES)!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        // Inflate the layout for this fragment
        viewManager = LinearLayoutManager(activity)
        setHasOptionsMenu(true)

        sapServiceManager = (activity!!.application as SAPWizardApplication).sapServiceManager
        configurationData = (activity!!.application as SAPWizardApplication).configurationData
        btWrapper = BtWrapper(mHandler)
        iotDeviceConfigManager = IotDeviceConfigManager(btWrapper,sapServiceManager,userProfile.userId,connectedDevice.deviceSn)
        iotDataMLServiceManager = IotDataMLServiceManager(sapServiceManager,configurationData)

        connectedDevice = userDevices[0]
        btWrapper.connectDevice(connectedDevice.deviceAddress)

        rootView.ib_change_device_config.setOnClickListener {
            showChangeDeviceConfigDialog(container)
        }

        rootView.btn_give_feedback.setOnClickListener {
            showGiveFeedbackDialog(container)
        }

        recyclerView = rootView.findViewById<RecyclerView>(R.id.rcv_iot_data).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
        }

        Thread(Runnable {
            sapServiceManager.openODataStore {
                val iotDataQuery = DataQuery()
                        .filter(IotDataType.userId.equal(userProfile.userId))
                        .filter(IotDataType.deviceSn.equal(connectedDevice.deviceSn))
                        .top(10)

                Log.e(TAG,iotDataQuery.toString())
                sapServiceManager.getsbp().getIotDataAsync(iotDataQuery,
                        {iotDataList:List<IotDataType>->

                            viewAdapter = IotDataAdapter(iotDataList)
                            activity!!.runOnUiThread { recyclerView.adapter = viewAdapter }
                        },
                        {re:RuntimeException->
                            Log.d(TAG, "An error occurred during async query:  "  + re.message)
                        })
            }
        }).start()

        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.fragment_home_menu,menu);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.fragment_home_menu_sync -> {
                if(connectStatus)
                    syncDataAndHoldingZone()
                else
                    Toast.makeText(activity,"Backpack not connected", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.fragment_home_menu_connect -> {
                if(!connectStatus) {
                    btWrapper.connectDevice(connectedDevice.deviceAddress)
                    item.icon = activity!!.getDrawable(R.drawable.ic_close_black_24dp)
                }else{
                    btWrapper.disconnectDevice()
                    item.icon = activity!!.getDrawable(R.drawable.ic_add_circle_outline_black_24dp)
                }
                return true
            }
        }
        return false
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        if (context is OnFragmentInteractionListener) {
//            listener = context
//        } else {
//            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
//        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    fun syncDataAndHoldingZone(){
        pb_syncing.visibility = View.VISIBLE

        pb_syncing.progress = 10
        btWrapper.getSensorData()

        pb_syncing.progress = 20
        btWrapper.syncHoldingZone()
    }

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            Log.d(TAG, msg.what.toString())
            when (msg.what) {
                Constants.HANDLER_ACTION.CONNECTED.value->{

                    updateDeviceConfigCard()
                    iotDeviceConfigManager.updateLastOnline()

                    Toast.makeText(activity,"Backpack Connected",Toast.LENGTH_SHORT).show()

                    connectStatus = true

                    object : Thread() {
                        override fun run() {
                            try {
                                while (true) {
                                    Thread.sleep(5000)
                                    if(connectStatus)
                                        btWrapper.getSensorData()
                                }
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }

                        }
                    }.start()

                }
                Constants.HANDLER_ACTION.DISCONNECTED.value->{
                    Toast.makeText(activity,"Backpack Disconnected",Toast.LENGTH_SHORT).show()
                    connectStatus = false
                }
                Constants.HANDLER_ACTION.CONNECT_LOST.value->{
                    Toast.makeText(activity,"Backpack Disconnected",Toast.LENGTH_SHORT).show()
                    connectStatus = false
                }
                Constants.HANDLER_ACTION.CONNECT_ERROR.value->{
                    Toast.makeText(activity,"Unable to Contact Backpack",Toast.LENGTH_SHORT).show()
                    pb_loading.visibility = View.GONE
                    connectStatus = false
                }
                Constants.HANDLER_ACTION.COMMAND_SEND.value->{
                    Log.i(TAG,"Command send")
                }
                Constants.HANDLER_ACTION.RECEIVE_RESPONSE.value->{

                    Log.i(TAG,"Backpack data received")
                    val mBtCommandObject = msg.obj as BtCommandObject

                    when(mBtCommandObject.function_code){
                        Constants.BT_FUN_CODE.GET_SENSOR_DATA.code->{

                            pb_loading.visibility = View.INVISIBLE
                            tv_sensor_hum.text = mBtCommandObject.data["HUMIDITY"]
                            tv_sensor_temp.text = mBtCommandObject.data["TEMPERATURE"]
                            tv_sensor_pm10.text = mBtCommandObject.data["PM10"]
                            tv_sensor_pm25.text = mBtCommandObject.data["PM2_5"]

                            realTimeDate.humidity = mBtCommandObject.data["HUMIDITY"]!!.toDouble()
                            realTimeDate.temperature = mBtCommandObject.data["TEMPERATURE"]!!.toDouble()
                            realTimeDate.pm10 = mBtCommandObject.data["PM10"]!!.toDouble()
                            realTimeDate.pm25 = mBtCommandObject.data["PM2_5"]!!.toDouble()

                            retrieveMLService(realTimeDate)

                        }
                        Constants.BT_FUN_CODE.SYNC_HOLDING_ZONE.code-> {

                            Log.i(TAG, "SYNC_HOLDING_ZONE")

                            val lm: LocationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                            var longitude = 0.0
                            var latitude = 0.0
                            if (lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
                                val location: Location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                longitude = location.getLongitude()
                                latitude = location.getLatitude()
                            }

                            val holdingZoneDataList: MutableList<HoldingZoneData> = mutableListOf()
                            for (keyValuePair in mBtCommandObject.data) {
                                holdingZoneDataList.add(HoldingZoneData(keyValuePair.key, keyValuePair.value))
                            }
                            pb_syncing.progress = 30

                            if (!holdingZoneDataList.isEmpty()) {

                                val updateIncrement = 60 / holdingZoneDataList.size

                                //Sync to hana database
                                sapServiceManager.openODataStore {

                                    val batch = RequestBatch()
                                    val updateChangeSet = ChangeSet()

                                    val lastIdQuery = DataQuery().top(1).orderBy(IotDataType.dataId, SortOrder.DESCENDING)

                                    sapServiceManager.getsbp().getIotDataAsync(lastIdQuery, {

                                        var dataId = it[0].dataId
                                        Log.e(TAG, "Increment from ${dataId}")

                                        holdingZoneDataList.forEach {
                                            try {
                                                dataId += 1

                                                val createData = IotDataType()
                                                createData.dataId = dataId
                                                createData.userId = userProfile.userId
                                                createData.deviceSn = connectedDevice.deviceSn
                                                createData.pm10 = it.pm10.toDouble()
                                                createData.pm25 = it.pm2_5.toDouble()
                                                createData.temperature = it.temperature.toDouble()
                                                createData.humidity = it.humidity.toDouble()
                                                createData.alertTriggered = if (it.alertTriggered == "True") "Y" else "N"
                                                createData.recordedOn = LocalDateTime.parse(it.recorededOn)
                                                createData.city = "SINGAPORE"
                                                createData.country = "SINGAPORE"
                                                createData.state = "SINGAPORE"
                                                createData.countryCode = "SG"
                                                createData.geoLat = latitude
                                                createData.geoLng = longitude

                                                updateChangeSet.createEntity(createData)
                                                pb_syncing.progress += updateIncrement
                                                batch.addChanges(updateChangeSet)
                                            } catch (e: Exception) {
                                                Log.e(TAG, e.message)
                                            }

                                        }

                                        Log.e(TAG, updateChangeSet.size().toString())

                                        sapServiceManager.getsbp().processBatchAsync(batch, {
                                            val changeSetStatus = updateChangeSet.status

                                            if (changeSetStatus >= 400) {
                                                val dsEx = updateChangeSet.error
                                                val response = dsEx.response
                                                Log.e(TAG, response.message)
                                            }
                                        }, { e: RuntimeException ->
                                            Log.e(TAG, e.message)
                                        })
                                    }, { e: RuntimeException ->
                                        Log.e(TAG, e.message)
                                    })

                                }

                                pb_syncing.progress = 90
                                Log.i(TAG, "Holding zone flushing start")
                                btWrapper.flushHoldingZone()
                            }else{
                                setHoldingZoneSyncCompleteState()
                            }
                        }
                        Constants.BT_FUN_CODE.FLUSH_HOLDING_ZONE.code->{
                            Log.i(TAG,"Holding zone flushing complete")
                            setHoldingZoneSyncCompleteState()
                        }
                        Constants.BT_FUN_CODE.CHANGE_DEVICE_SETTINGS.code ->{
                            Log.i(TAG,"Change device Settings Complete")
                            iotDeviceConfigManager.syncConfigToHana({
                                Toast.makeText(activity,"Backpack Settings Changed", Toast.LENGTH_SHORT)
                            },{
                                Toast.makeText(activity,"Fail to Change Backpack Settings", Toast.LENGTH_SHORT)
                                Log.e(TAG,it.message)
                            })
                        }
                        Constants.BT_FUN_CODE.TOGGLE_DEBUG.code->{


                        }
                        else -> {
                            //received code not supported
                            Log.i(TAG,"received code not supported: " + mBtCommandObject.function_code)
                        }

                    }

                }
                Constants.HANDLER_ACTION.RECEIVE_ERROR.value->{



                }
                else -> {
                    //state not supported
                    Log.i(TAG,"state not supported: " + msg.what)
                }
            }
        }
    }

    private fun updateDeviceConfigCard() {
        activity!!.title = connectedDevice.deviceName
        oh_device_ovp.headline = connectedDevice.deviceSn
        oh_device_ovp.subheadline = "Last online: ${connectedDevice.lastOnline.date.toString() + " " + connectedDevice.lastOnline.hour + ":" + connectedDevice.lastOnline.minute} "
        oh_device_ovp.footnote = connectedDevice.applicationVersion
        oh_device_ovp.body = connectedDevice.systemPlatform

        if(connectedDevice.sensorHumidity == "Y")
            oh_device_ovp.setTag("Humidity", 0)

        if(connectedDevice.sensorTemperature == "Y")
            oh_device_ovp.setTag("Temperature", 1)

        if(connectedDevice.sensorAirQuality == "Y")
            oh_device_ovp.setTag("Air Quality", 1)

        tv_buzzer_config.text = if (connectedDevice.configEnableBuzzer == "Y") "Enabled" else "Disabled"
        tv_led_config.text = if (connectedDevice.configEnableLed == "Y") "Enabled" else "Disabled"
        tv_data_record_interval_config.text = connectedDevice.minutesToRecordData.toString()
    }

    private fun setHoldingZoneSyncCompleteState() {
        pb_syncing.progress = 100
        pb_syncing.visibility = View.GONE
        Toast.makeText(activity, "Backpack synchronised", Toast.LENGTH_SHORT).show()
    }

    private fun retrieveMLService(data:IotDataType){
        iotDataMLServiceManager.getLevelAndSuggestion(userProfile,data,{level,suggestion ->
            when(level){
                0 -> {
                    iv_comfort_level_icon.setImageResource(R.drawable.ic_sentiment_very_satisfied_black_24dp)
                    ImageViewCompat.setImageTintList(iv_comfort_level_icon, ColorStateList.valueOf(ContextCompat.getColor(activity!!, R.color.sap_ui_positive_text)))
                }
                1 -> {
                    iv_comfort_level_icon.setImageResource(R.drawable.ic_sentiment_satisfied_black_24dp)
                    ImageViewCompat.setImageTintList(iv_comfort_level_icon, ColorStateList.valueOf(ContextCompat.getColor(activity!!, R.color.sap_ui_positive_text)))
                }
                2 -> {
                    iv_comfort_level_icon.setImageResource(R.drawable.ic_sentiment_neutral_black_24dp)
                    ImageViewCompat.setImageTintList(iv_comfort_level_icon, ColorStateList.valueOf(ContextCompat.getColor(activity!!, R.color.sap_ui_neutral_text)))
                }
                3 -> {
                    iv_comfort_level_icon.setImageResource(R.drawable.ic_sentiment_dissatisfied_black_24dp)
                    ImageViewCompat.setImageTintList(iv_comfort_level_icon, ColorStateList.valueOf(ContextCompat.getColor(activity!!, R.color.sap_ui_negative_text)))
                }
                4 -> {
                    iv_comfort_level_icon.setImageResource(R.drawable.ic_sentiment_very_dissatisfied_black_24dp)
                    ImageViewCompat.setImageTintList(iv_comfort_level_icon, ColorStateList.valueOf(ContextCompat.getColor(activity!!, R.color.sap_ui_negative_text)))
                }
            }

            tv_comfort_level_indicator.text = iotDataMLServiceManager.getFeedbackLabel(level)

            tv_comfort_level_suggestions.visibility = View.VISIBLE
            tv_comfort_level_suggestions.text = suggestion.advise

            predictedComfortLevel = level
        },{
            iv_comfort_level_icon.setImageResource(R.drawable.ic_sentiment_neutral_black_24dp)
            ImageViewCompat.setImageTintList(iv_comfort_level_icon, ColorStateList.valueOf(ContextCompat.getColor(activity!!, R.color.sap_ui_neutral_text)))
            tv_comfort_level_indicator.text = "ML Bot not around"
            tv_comfort_level_suggestions.text = "Can't provide any advice"
            Log.e(TAG,(it as Exception).message)
        })
    }

    private fun showChangeDeviceConfigDialog(container: ViewGroup?){
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_change_device_setting, container, false)

        dialogView.spf_device_name.value = connectedDevice.deviceName
        dialogView.sfc_enable_buzzer.setValue(connectedDevice.configEnableBuzzer == "Y")
        dialogView.sfc_enable_led.setValue(connectedDevice.configEnableLed == "Y")
        dialogView.sl_record_interval.value = connectedDevice.minutesToRecordData

        dialogView.sfc_enable_buzzer.cellValueChangeListener = object : FormCell.CellValueChangeListener<Boolean>() {
            override fun cellChangeHandler(value: Boolean) {
                iotDeviceConfigManager.toggleBuzzerNow(value)
            }
        }

        dialogView.sfc_enable_led.cellValueChangeListener = object : FormCell.CellValueChangeListener<Boolean>() {
            override fun cellChangeHandler(value: Boolean) {
                iotDeviceConfigManager.toggleLedNow(value)
            }
        }

        dialogView.sl_record_interval.cellValueChangeListener = object : FormCell.CellValueChangeListener<Int>() {
            override fun cellChangeHandler(value: Int) {
                dialogView.sl_record_interval.displayValue = "$value Minutes"
            }
        }

        val builder = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.AlertDialogStyle))

        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("Save"
                ) { dialog, _ ->
                    connectedDevice.deviceName = dialogView.spf_device_name.value.toString()
                    connectedDevice.configEnableBuzzer = if(dialogView.sfc_enable_buzzer.value) "Y" else "N"
                    connectedDevice.configEnableLed = if(dialogView.sfc_enable_led.value) "Y" else "N"
                    connectedDevice.minutesToRecordData = dialogView.sl_record_interval.value
                    updateDeviceConfigCard()
                    iotDeviceConfigManager.changeDeviceName(dialogView.spf_device_name.value.toString())
                    iotDeviceConfigManager.commitChanges()

                    Toast.makeText(activity,"Settings Changed",Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel
                ) { dialog, _ ->
                    dialog.cancel()
                }
                .setTitle("Change Device Settings")

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showGiveFeedbackDialog(container: ViewGroup?){
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_give_feedback, container, false)

        dialogView.sl_feedback_level.value = predictedComfortLevel
        handleGiveFeedbackInfPane(dialogView,predictedComfortLevel)

        dialogView.sl_feedback_level.cellValueChangeListener = object : FormCell.CellValueChangeListener<Int>() {
            override fun cellChangeHandler(value: Int) {
                dialogView.sl_feedback_level.value = value
                handleGiveFeedbackInfPane(dialogView,value)
            }
        }

        val builder = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.AlertDialogStyle))

        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("Submit"
                ) { dialog, _ ->
                    iotDataMLServiceManager.setDataFeedback(activity!!,userProfile,connectedDevice,realTimeDate, dialogView.sl_feedback_level.value,{
                        Toast.makeText(activity,"Thank you, I will get smarter next time.",Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    },{
                        Toast.makeText(activity,"Oops, can you try again?",Toast.LENGTH_SHORT).show()
                        Log.e(TAG,it.message)
                    })
                }
                .setNegativeButton(R.string.cancel
                ) { dialog, _ ->
                    dialog.cancel()
                }
                .setTitle("How do you feel")

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun handleGiveFeedbackInfPane(dialogView:View, value:Int){
        when(value){
            0 -> {
                dialogView.d_tv_comfort_level_info.text = "Air quality is considered satisfactory, and air pollution poses little or no risk."
                dialogView.d_iv_comfort_level_icon.setImageResource(R.drawable.ic_sentiment_very_satisfied_black_24dp)
                ImageViewCompat.setImageTintList(dialogView.d_iv_comfort_level_icon, ColorStateList.valueOf(ContextCompat.getColor(activity!!, R.color.sap_ui_positive_text)))
            }
            1 -> {
                dialogView.d_tv_comfort_level_info.text = "Air quality is acceptable; however, for some pollutants there may be a moderate health concern for a very small number of people"
                dialogView.d_iv_comfort_level_icon.setImageResource(R.drawable.ic_sentiment_satisfied_black_24dp)
                ImageViewCompat.setImageTintList(dialogView.d_iv_comfort_level_icon, ColorStateList.valueOf(ContextCompat.getColor(activity!!, R.color.sap_ui_positive_text)))
            }
            2 -> {
                dialogView.d_tv_comfort_level_info.text = "Although general public is not likely to be affected at this AQI range, people with lung disease, older adults and children are at a greater risk from exposure to ozone, whereas persons with heart and lung disease, older adults and children are at greater risk from the presence of particles in the air"
                dialogView.d_iv_comfort_level_icon.setImageResource(R.drawable.ic_sentiment_neutral_black_24dp)
                ImageViewCompat.setImageTintList(dialogView.d_iv_comfort_level_icon, ColorStateList.valueOf(ContextCompat.getColor(activity!!, R.color.sap_ui_neutral_text)))
            }
            3 -> {
                dialogView.d_tv_comfort_level_info.text = "This would trigger a health alert signifying that everyone may experience more serious health effects"
                dialogView.d_iv_comfort_level_icon.setImageResource(R.drawable.ic_sentiment_dissatisfied_black_24dp)
                ImageViewCompat.setImageTintList(dialogView.d_iv_comfort_level_icon, ColorStateList.valueOf(ContextCompat.getColor(activity!!, R.color.sap_ui_negative_text)))
            }
            4 -> {
                dialogView.d_tv_comfort_level_info.text = "This would trigger a health warnings of emergency conditions. The entire population is more likely to be affected."
                dialogView.d_iv_comfort_level_icon.setImageResource(R.drawable.ic_sentiment_very_dissatisfied_black_24dp)
                ImageViewCompat.setImageTintList(dialogView.d_iv_comfort_level_icon, ColorStateList.valueOf(ContextCompat.getColor(activity!!, R.color.sap_ui_negative_text)))
            }
        }

        dialogView.d_tv_comfort_level_indicator.text = iotDataMLServiceManager.getFeedbackLabel(value)
    }

    companion object {

        private const val TAG = "HomeFragment"

        @JvmStatic
        fun newInstance(param1: UserinfosType, param2: ArrayList<UserDevicesType>) =
                HomeFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(USER_PROFILE, param1)
                        putParcelableArrayList(USER_DEVICES, param2)
                    }
                }
    }

    class IotDataAdapter(private val iotData: List<IotDataType>) :
            RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val HEADER_TYPE = 0
        private val ITEM_TYPE = 1

        class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            var recorded_on:TextView = itemView.row_recorded_on
            var temperature:TextView = itemView.row_temperature
            var humidity:TextView = itemView.row_humidity
            var pm2_5:TextView = itemView.row_pm2_5
            var pm10:TextView = itemView.row_pm10
        }
        class RowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            var recorded_on:TextView = itemView.row_recorded_on
            var temperature:TextView = itemView.row_temperature
            var humidity:TextView = itemView.row_humidity
            var pm2_5:TextView = itemView.row_pm2_5
            var pm10:TextView = itemView.row_pm10
        }


        // Create new views (invoked by the layout manager)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            val context = parent.context
            val inflater = LayoutInflater.from(context)
            return if (viewType == HEADER_TYPE) {
                val view = inflater.inflate(R.layout.components_iot_data_table_row, parent,
                        false) as GridTableRow
                HeaderViewHolder(view)
            } else {
                val view = inflater.inflate(R.layout.components_iot_data_table_row, parent,
                        false) as GridTableRow
                RowViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            if (holder is RowViewHolder) {
                val dataItem = getItem(position)
                var record_date = dataItem.recordedOn
                holder.recorded_on.text = record_date.date.toString() + " " + record_date.hour + ":" + record_date.minute
                holder.temperature.text = dataItem.temperature.toString()
                holder.humidity.text = dataItem.humidity.toString()
                holder.pm10.text = dataItem.pm10.toString()
                holder.pm2_5.text = dataItem.pm25.toString()

            } else if (holder is HeaderViewHolder) {
                holder.recorded_on.text = "Recorded on"
                holder.temperature.text = "Temperature (°C)"
                holder.humidity.text = "Humidity (%)"
                holder.pm10.text = "PM10 (μg/m3)"
                holder.pm2_5.text = "PM2.5 (μg/m3)"

            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (isPositionHeader(position)) HEADER_TYPE else ITEM_TYPE

        }

        override fun getItemCount() = iotData.size

        private fun isPositionHeader(position: Int): Boolean {
            return position == 0
        }

        private fun getItem(position: Int): IotDataType {
            return iotData[position - 1]
        }

    }

}
