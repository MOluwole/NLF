package com.yung_coder.oluwole.nearestlocationfinder.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.yung_coder.oluwole.nearestlocationfinder.NLF
import com.yung_coder.oluwole.nearestlocationfinder.R
import com.yung_coder.oluwole.nearestlocationfinder.models.Models

/**
 * Created by yung on 10/6/17.
 */
class LocationAdapter constructor(data_list:ArrayList<Models.locations>): RecyclerView.Adapter<LocationAdapter.ViewAdapter>() {

        var data_list: ArrayList<Models.locations>? = null
        var context: Context? = null

    init {
        this.data_list = data_list
    }

    override fun getItemCount(): Int {
        return data_list?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewAdapter?, position: Int) {
        val data = data_list?.get(position)

        Picasso.with(context).load(data?.icon).into(holder?.image_location)

        holder?.text_name?.text = data?.name
        holder?.rating_bar?.rating = data?.rating?.toFloat()!!

        holder?.single_item?.setOnClickListener{
            val extras = Bundle()
            extras.putDouble("destlat", data.destination_latitude.toDouble())
            extras.putDouble("destlng", data.destination_longitude.toDouble())
            extras.putString("destination", data.name)

            val intent = Intent(context, NLF::class.java)
            intent.putExtra("extra", extras)
            context?.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewAdapter {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.location_list, parent, false)
        val view_adapter = ViewAdapter(view)
        context = view.context
        return view_adapter
    }

    class ViewAdapter(layoutView: View): RecyclerView.ViewHolder(layoutView){
        val text_name: TextView = layoutView.findViewById(R.id.text_location_name)
        val image_location: ImageView = layoutView.findViewById(R.id.image_location_icon)
        val rating_bar: RatingBar = layoutView.findViewById(R.id.rating_location)
        val single_item: RelativeLayout = layoutView.findViewById(R.id.location_item)
    }
}