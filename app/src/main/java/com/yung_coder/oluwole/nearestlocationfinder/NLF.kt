package com.yung_coder.oluwole.nearestlocationfinder

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.yung_coder.oluwole.nearestlocationfinder.parser.Parser

class NLF : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    companion object {
        private lateinit var mMap: GoogleMap
        private var search_query = 0
        private var origin_latitude: Double = 0.toDouble()
        private var origin_longitude: Double = 0.toDouble()
        private val PROXIMITY_RADIUS = 10000
        private var mGoogleApiClient: GoogleApiClient? = null
        private var mLastLocation: Location? = null
        private var mCurrLocationMarker: Marker? = null
        private var mLocationRequest: LocationRequest? = null

        private var destination_latitude: Double = 0.0
        private var destination_longitude: Double = 0.0
        private var destination = ""
        var points = ArrayList<LatLng>()
        var line_options = PolylineOptions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nlf)

        val bundle = intent.getBundleExtra("extra")
        destination = bundle.getString("destination")
        destination_latitude = bundle.getDouble("destlat")
        destination_longitude = bundle.getDouble("destlng")
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//            checkLocationPermission()
        }

        if(!CheckGooglePlayServices()){
            finish()
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                buildGoogleApiClient()
                mMap.isMyLocationEnabled = true
            }
        }
        else{
            buildGoogleApiClient()
            mMap.isMyLocationEnabled = true
        }
        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    @Synchronized
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mGoogleApiClient?.connect()
    }

    private fun CheckGooglePlayServices(): Boolean {
        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(this)
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show()
            }
            return false
        }
        return true
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
        Log.e("Connection Failed", p0.errorMessage)
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onLocationChanged(p0: Location?) {
        mLastLocation = p0
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker?.remove()
        }
        origin_latitude = p0?.latitude!!
        origin_longitude = p0.longitude
        val latLng = LatLng(p0.latitude, p0.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Current Position")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
        mCurrLocationMarker = mMap.addMarker(markerOptions)

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
//        Toast.makeText(this@NLF, "Your Current Location", Toast.LENGTH_LONG).show()

        val destlatlng = LatLng(destination_latitude, destination_longitude)

        val dest_marker = MarkerOptions()
        dest_marker.position(destlatlng)
        dest_marker.title(destination)
        dest_marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        mMap.addMarker(dest_marker)

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
            Log.d("onLocationChanged", "Removing Location Updates")
        }



        val origin_param = "origin=$origin_latitude,$origin_longitude"
        val dest_param = "destination=$destination_latitude,$destination_longitude"
        val param = "$origin_param&$dest_param&sensor=false"
        val url = "https://maps.googleapis.com/maps/api/directions/json?$param"

        val queue = Volley.newRequestQueue(this@NLF)
        val json_object = JsonObjectRequest(Request.Method.GET, url, null, Response.Listener { response ->

            val parser = Parser()
            val route = parser.parse(response)

            for(i in 0 until route.size){
                points = ArrayList()
                line_options = PolylineOptions()

                val path = route[i]
                for(j in 0 until path.size){
                    val point = path[j]
                    val lat = point.get("lat")!!.toDouble()
                    val lng = point.get("lng")!!.toDouble()
                    val position = LatLng(lat, lng)

                    points.add(position)
                }

                line_options.addAll(points)
                line_options.width(10F)
                line_options.color(Color.RED)
            }

            if (line_options != null){
                mMap.addPolyline(line_options)
            }

        }, Response.ErrorListener { error ->
            error.printStackTrace()
        })
        queue.add(json_object)

//        Log.e("URL", url)
    }
}
