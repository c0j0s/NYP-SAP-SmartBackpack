package com.nyp.fypj.smartbackpackapp.mdui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.nyp.fypj.smartbackpackapp.Constants
import com.nyp.fypj.smartbackpackapp.R
import com.nyp.fypj.smartbackpackapp.app.SAPWizardApplication
import com.nyp.fypj.smartbackpackapp.bluetooth.BtCommandObject
import com.nyp.fypj.smartbackpackapp.bluetooth.BtWrapper
import com.nyp.fypj.smartbackpackapp.bluetooth.HoldingZoneData
import com.nyp.fypj.smartbackpackapp.service.SAPServiceManager
import com.sap.cloud.android.odata.sbp.IotDataType
import com.sap.cloud.android.odata.sbp.IotdeviceinfoType
import com.sap.cloud.android.odata.sbp.UserDevicesType
import com.sap.cloud.android.odata.sbp.UserinfosType
import com.sap.cloud.mobile.fiori.`object`.GridTableRow
import com.sap.cloud.mobile.odata.*
import kotlinx.android.synthetic.main.components_iot_data_table_row.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.lang.Exception
import java.util.*


private const val USER_PROFILE = "userProfile"
private const val USER_DEVICES = "userDevices"

class HomeFragment : Fragment() {

    private lateinit var sapServiceManager: SAPServiceManager
    private lateinit var btWrapper: BtWrapper

    private lateinit var userProfile: UserinfosType
    private lateinit var userDevices: ArrayList<IotdeviceinfoType>
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var connectedDevice: IotdeviceinfoType = IotdeviceinfoType()

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
        btWrapper = BtWrapper(mHandler)

        connectedDevice = userDevices[0]
        btWrapper.connectDevice(connectedDevice.deviceAddress)

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
                            Log.e(TAG,iotDataList.size.toString())

                            viewAdapter = IotDataAdapter(iotDataList)
                            recyclerView.adapter = viewAdapter

                            activity!!.runOnUiThread({ recyclerView.adapter = viewAdapter })
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
                syncDataAndHoldingZone()
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
                //handle when device connected
                Constants.HANDLER_ACTION.CONNECTED.value->{

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

                    btWrapper.getSensorData()
                }
                Constants.HANDLER_ACTION.DISCONNECTED.value->{
                    Toast.makeText(activity,"Backpack Disconnected",Toast.LENGTH_SHORT).show()
                }
                Constants.HANDLER_ACTION.CONNECT_LOST.value->{
                    Toast.makeText(activity,"Backpack Connection Lost",Toast.LENGTH_SHORT).show()
                }
                Constants.HANDLER_ACTION.CONNECT_ERROR.value->{

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

    private fun setHoldingZoneSyncCompleteState() {
        pb_syncing.progress = 100
        pb_syncing.visibility = View.GONE
        Toast.makeText(activity, "Backpack synchronised", Toast.LENGTH_SHORT).show()
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
