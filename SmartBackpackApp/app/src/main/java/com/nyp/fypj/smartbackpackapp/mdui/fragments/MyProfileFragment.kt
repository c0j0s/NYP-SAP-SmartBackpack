package com.nyp.fypj.smartbackpackapp.mdui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import com.nyp.fypj.smartbackpackapp.R
import com.nyp.fypj.smartbackpackapp.service.IotDataMLServiceManager
import com.sap.cloud.android.odata.sbp.IotdeviceinfoType
import com.sap.cloud.android.odata.sbp.UserDevicesType
import com.sap.cloud.android.odata.sbp.UserinfosType
import kotlinx.android.synthetic.main.fragment_my_profile.view.*
import kotlinx.android.synthetic.main.notification_template_lines_media.view.*
import android.app.AlarmManager
import android.app.AlertDialog
import android.support.v4.content.ContextCompat.getSystemService
import android.app.PendingIntent
import android.content.Intent
import com.nyp.fypj.smartbackpackapp.mdui.MainActivity
import android.support.v4.app.ActivityCompat.finishAffinity
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.nyp.fypj.smartbackpackapp.mdui.IotBtTestingActivity


private const val USER_PROFILE = "userProfile"
private const val USER_DEVICES = "userDevices"

class MyProfileFragment : Fragment() {

    private lateinit var userProfile: UserinfosType
    private lateinit var userDevices: ArrayList<IotdeviceinfoType>
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var container:ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userProfile = it.getParcelable(USER_PROFILE)!!
            userDevices = it.getParcelableArrayList(USER_DEVICES)!!

        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_my_profile, container, false)
        setHasOptionsMenu(true)
        this.container = container!!

        activity!!.title = "My Profile"
        rootView.ph_profile_ovp.headline = userProfile.name
        rootView.ph_profile_ovp.subheadline = userProfile.userId
        rootView.ph_profile_ovp.description = if(userProfile.gender == "M") "Male" else "Female" + ", ${userProfile.race}"

        rootView.spf_asthmatic_level.value = userProfile.asthmaticDesc
        rootView.spf_dob_age.value = "${userProfile.dob.date}, ${userProfile.age}"
        rootView.spf_user_contact.value = userProfile.contactNo
        rootView.spf_user_address.value = "${userProfile.userCity}, ${userProfile.userState}, ${userProfile.userCountry}".toLowerCase()
        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.fragment_my_profile_menu,menu);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.fragment_my_profile_menu_connect -> {
                showEditProfileDialog()
            }
            R.id.fragment_my_profile_debug -> {
                var debugIntent = Intent(activity,IotBtTestingActivity::class.java)
                startActivity(debugIntent)
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

    private fun showEditProfileDialog(){
        val builder = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.AlertDialogStyle))

        val label = TextView(activity)
        label.text = "Edit My Profile Not Available"
        label.setPadding(16,8,16,8)

        builder.setView(label).setPositiveButton("Ok"
                ) { dialog, _ ->
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel
                ) { dialog, _ ->
                    dialog.cancel()
                }
                .setTitle("Edit My Profile")

        val alertDialog = builder.create()
        alertDialog.show()
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
