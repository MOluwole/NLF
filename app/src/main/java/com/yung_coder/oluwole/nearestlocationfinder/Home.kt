package com.yung_coder.oluwole.nearestlocationfinder

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.yung_coder.oluwole.nearestlocationfinder.adapters.DataAdapter
import android.content.DialogInterface
import android.support.v4.content.ContextCompat.startActivity
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.content.Intent
import android.location.LocationManager
import android.content.Context.LOCATION_SERVICE
import android.provider.Settings
import android.support.v7.app.AlertDialog


class Home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        var menu_list = ArrayList<String>()
        menu_list.add("Nearest Bank")
        menu_list.add("Nearest Cinema")
        menu_list.add("Nearest Hotel")
        menu_list.add("Nearest Restaurant")
        menu_list.add("Nearest School")

        checkPermission()

        val layout_manager = LinearLayoutManager(this)
        val recycler_view: RecyclerView = findViewById(R.id.recycler_view)

        recycler_view.layoutManager = layout_manager
        val divider_item = DividerItemDecoration(this, layout_manager.orientation)
        recycler_view.addItemDecoration(divider_item)

        val adapter = DataAdapter(menu_list, this)
        recycler_view.adapter = adapter
    }

    private fun checkPermission(){
        val lm = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }


        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }


        if (!gps_enabled && !network_enabled) {
            // notify user
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage("Location settings not enabled. Enable Location Settings")
            dialog.setPositiveButton("Change Settings", { _, _ ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                this.startActivity(myIntent)
                //get gps
            })
            dialog.setNegativeButton("Cancel", { _, _ ->
            })
            dialog.show()
        }
    }

}
