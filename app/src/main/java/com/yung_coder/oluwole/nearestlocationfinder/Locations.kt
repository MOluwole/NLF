package com.yung_coder.oluwole.nearestlocationfinder

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.yung_coder.oluwole.nearestlocationfinder.adapters.LocationAdapter
import com.yung_coder.oluwole.nearestlocationfinder.models.Models
import kotlinx.android.synthetic.main.activity_locations.*

class Locations : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    companion object {
        var query = ""
        private var mGoogleApiClient: GoogleApiClient? = null
        private var mLocationRequest: LocationRequest? = null
        private var latitude: Double = 0.toDouble()
        private var longitude: Double = 0.toDouble()
        private val PROXIMITY_RADIUS = 4000
        private var mLastLocation: Location? = null

        private var returned_data = ArrayList<Models.locations>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locations)

        val position = intent.extras.getInt("query")
        when(position){
            0 -> query = "bank"
            1 ->  query = "cinema"
            2 -> query = "hotel"
            3 -> query = "restaurant"
            4 -> query = "school"
        }

        location_view.adapter = null
        val layoutManager = LinearLayoutManager(this@Locations)
        location_view.layoutManager = layoutManager
        val divider_item = DividerItemDecoration(this@Locations, layoutManager.orientation)
        location_view.addItemDecoration(divider_item)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mGoogleApiClient?.connect()

        getLocations(query)

        location_swipe.setOnRefreshListener {
            getLocations(query)
        }
    }

    override fun onLocationChanged(p0: Location?) {
        mLastLocation = p0
        latitude = p0?.latitude!!
        longitude = p0.longitude

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        }
    }

    override fun onConnected(p0: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest?.interval = 1000
        mLocationRequest?.fastestInterval = 1000
        mLocationRequest?.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.e("Error", p0.errorMessage)
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.e("Connection Suspended", p0.toString())
    }

    private fun getUrl(latitude: Double, longitude: Double, nearbyPlace: String): String {
        val googlePlacesUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?")
        googlePlacesUrl.append("location=$latitude,$longitude")
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS)
        googlePlacesUrl.append("&type=" + nearbyPlace)
        googlePlacesUrl.append("&sensor=true")
        googlePlacesUrl.append("&key=" + "AIzaSyATuUiZUkEc_UgHuqsBJa1oqaODI-3mLs0")
        Log.d("getUrl", googlePlacesUrl.toString())
        return googlePlacesUrl.toString()
    }

    fun getLocations(location: String) {
        location_swipe.isRefreshing = true
        val queue = Volley.newRequestQueue(this@Locations)
        val url = getUrl(latitude, longitude, location)
        Log.e("URl", url)

        val json_object = JsonObjectRequest(Request.Method.GET, url, null, Response.Listener { response ->
            val results = response.getJSONArray("results")
            Log.e("response", results.toString())
            var count = 0
            returned_data.clear()
            while(count < results.length()){
                try {
                    val counter = results.getJSONObject(count)
//                val rating: Double? = counter.getDouble("rating") ?: 0.0
                    var rating = 0.0
                    if (counter.getDouble("rating") != null) {
                        rating = counter.getDouble("rating")
                    }
                    val data = Models.locations(counter.getString("name"), counter.getString("vicinity"), counter.getJSONObject("geometry")
                            .getJSONObject("location").getString("lat"), counter.getJSONObject("geometry").getJSONObject("location")
                            .getString("lng"), counter.getString("reference"), counter.getString("icon"), rating, latitude.toString(), longitude.toString())
                    returned_data.add(data)
                }
                catch (e: Exception){
                    e.printStackTrace()
                    Log.e("Exception", e.message)
                }
                count++
            }

            location_view.adapter = LocationAdapter(returned_data)
            Log.e("Message", returned_data.count().toString())
            location_swipe.isRefreshing = false
            if(returned_data.count() == 0){
                Snackbar.make(location_swipe, "An error occurred. Swipe down to refresh", Snackbar.LENGTH_LONG).show()
            }

        }, Response.ErrorListener { error ->
            error.printStackTrace()
            location_swipe.isRefreshing = false
            Snackbar.make(location_swipe, "An error occurred. Swipe down to refresh", Snackbar.LENGTH_LONG).show()
        })
        queue.add(json_object)
    }
}
