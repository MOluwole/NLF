package com.yung_coder.oluwole.nearestlocationfinder.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.yung_coder.oluwole.nearestlocationfinder.Home
import com.yung_coder.oluwole.nearestlocationfinder.Locations
import com.yung_coder.oluwole.nearestlocationfinder.R

/**
 * Created by yung on 9/27/17.
 */
class DataAdapter constructor(mList: List<String>, context: Context): RecyclerView.Adapter<DataAdapter.ViewAdapter>() {
    var mList: List<String>? = null
    var context: Context? = null

    private val image_list =  ArrayList<Int>()

    init {
        this.mList = mList
        this.context = context
        image_list.add(R.drawable.bank)
        image_list.add(R.drawable.cinema)
        image_list.add(R.drawable.hotel)
        image_list.add(R.drawable.restaurant)
        image_list.add(R.drawable.school)
    }

    override fun getItemCount(): Int {
        return mList?.count() ?: 0
    }

    override fun onBindViewHolder(holder: ViewAdapter?, position: Int) {
        val menu = mList?.get(position)
        holder?.image?.setImageResource(image_list[position])
        holder?.text_menu?.text = menu

        holder?.single_item?.setOnClickListener{
            //Map Logic here
            val intent = Intent(context as Home, Locations::class.java)
            val extras = Bundle()
            extras.putInt("query", position)
            intent.putExtras(extras)
//            intent.putExtra("query", 0)
            context?.startActivity(intent)

            Log.e("Position", position.toString())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewAdapter {
        val rootView = LayoutInflater.from(parent?.context).inflate(R.layout.item_list, parent, false)
        val view_adapter = ViewAdapter(rootView)
        context = rootView.context
        return view_adapter
    }

    class ViewAdapter(layoutView: View): RecyclerView.ViewHolder(layoutView){
        var image: ImageView = layoutView.findViewById(R.id.image_menu)
        var text_menu: TextView = layoutView.findViewById(R.id.text_menu)
        var single_item: RelativeLayout = layoutView.findViewById(R.id.single_item)
    }
}