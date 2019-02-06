package com.nyp.fypj.smartbackpackapp.mdui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nyp.fypj.smartbackpackapp.R
import com.sap.cloud.android.odata.sbp.IotdeviceinfoType
import com.sap.cloud.android.odata.sbp.UserinfosType
import kotlinx.android.synthetic.main.components_user_devices_list.view.*

private const val USER_PROFILE = "userProfile"
private const val USER_DEVICES = "userDevices"

class MyDevicesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var userProfile: UserinfosType? = null
    private var userDevices: ArrayList<IotdeviceinfoType>? = null
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userProfile = it.getParcelable(USER_PROFILE)
            userDevices = it.getParcelableArrayList(USER_DEVICES)
        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_my_devices, container, false)
        activity!!.title = "My Backpacks (${userDevices!!.size})"
        viewManager = LinearLayoutManager(activity)
        recyclerView = rootView.findViewById<RecyclerView>(R.id.rcv_device_list).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            viewAdapter = UserDevicesAdapter(userDevices!!)
            adapter = viewAdapter
        }

        return rootView
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

    companion object {

        private const val TAG = "MyDevicesFragment"

        @JvmStatic
        fun newInstance(param1: UserinfosType, param2: ArrayList<IotdeviceinfoType>) =
                HomeFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(USER_PROFILE, param1)
                        putParcelableArrayList(USER_DEVICES, param2)
                    }
                }
    }

    class UserDevicesAdapter(private val userDevices: ArrayList<IotdeviceinfoType>) :
            RecyclerView.Adapter<UserDevicesAdapter.CardViewHolder>() {

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder.
        // Each data item is just a string in this case that is shown in a TextView.
        class CardViewHolder(view:View) : RecyclerView.ViewHolder(view){
            val deviceName = view.tv_device_name!!
            val lastOnline = view.tv_last_online
        }


        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): UserDevicesAdapter.CardViewHolder {

            val view = LayoutInflater.from(parent.context).inflate(R.layout.components_user_devices_list, parent,
                    false) as CardView

            return CardViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
            holder.deviceName.text = userDevices[position].deviceName
            holder.lastOnline.text = "Last online: ${userDevices[position].lastOnline.date.toString() + " " + userDevices[position].lastOnline.hour + ":" + userDevices[position].lastOnline.minute} "
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = userDevices.size
    }
}
