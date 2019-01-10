package com.nyp.fypj.smartbackpackapp.mdui

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.nyp.fypj.smartbackpackapp.R
import kotlinx.android.synthetic.main.activity_main.*
import com.nyp.fypj.smartbackpackapp.mdui.fragments.HomeFragment
import com.nyp.fypj.smartbackpackapp.mdui.fragments.MyDevicesFragment
import com.nyp.fypj.smartbackpackapp.mdui.fragments.MyProfileFragment


class MainActivity : AppCompatActivity() {
    val homeFragment: Fragment = HomeFragment()
    val myDevicesFragment: Fragment = MyDevicesFragment()
    val myProfileFragment: Fragment = MyProfileFragment()
    val fm = supportFragmentManager
    var active = homeFragment

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                fm.beginTransaction().hide(active).show(homeFragment).commit()
                active = homeFragment
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

        fm.beginTransaction().add(R.id.main_container, myProfileFragment, "3").hide(myProfileFragment).commit();
        fm.beginTransaction().add(R.id.main_container, myDevicesFragment, "2").hide(myDevicesFragment).commit();
        fm.beginTransaction().add(R.id.main_container,homeFragment, "1").commit();

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
