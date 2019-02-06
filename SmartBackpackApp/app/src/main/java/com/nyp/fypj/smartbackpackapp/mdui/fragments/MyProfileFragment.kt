package com.nyp.fypj.smartbackpackapp.mdui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nyp.fypj.smartbackpackapp.R
import com.sap.cloud.android.odata.sbp.IotdeviceinfoType
import com.sap.cloud.android.odata.sbp.UserDevicesType
import com.sap.cloud.android.odata.sbp.UserinfosType

private const val USER_PROFILE = "userProfile"
private const val USER_DEVICES = "userDevices"

class MyProfileFragment : Fragment() {

    private var userProfile: UserinfosType? = null
    private var userDevices: ArrayList<IotdeviceinfoType>? = null
    private var listener: OnFragmentInteractionListener? = null

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
        val rootView = inflater.inflate(R.layout.fragment_my_profile, container, false)
        activity!!.title = "My Profile"
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

        private const val TAG = "MyProfileFragment"

        @JvmStatic
        fun newInstance(param1: UserinfosType, param2: ArrayList<UserDevicesType>) =
                HomeFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(USER_PROFILE, param1)
                        putParcelableArrayList(USER_DEVICES, param2)
                    }
                }
    }
}
