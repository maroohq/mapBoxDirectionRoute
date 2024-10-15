package com.example.week7maps

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.common.MapboxOptions
import com.mapbox.common.location.AccuracyLevel
import com.mapbox.common.location.DeviceLocationProvider
import com.mapbox.common.location.IntervalSettings
import com.mapbox.common.location.Location
import com.mapbox.common.location.LocationObserver
import com.mapbox.common.location.LocationProviderRequest
import com.mapbox.common.location.LocationService
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val TAG = "week7Debug"

class MainActivity : AppCompatActivity() {
    lateinit var mapView: MapView
    lateinit var permissionsManager:PermissionsManager

    val locationService : LocationService = LocationServiceFactory.getOrCreate()
    var locationProvider: DeviceLocationProvider? = null

    val locationObserver = object: LocationObserver {
        override fun onLocationUpdateReceived(locations: MutableList<Location>) {
            Log.e(TAG, "Location update received: " + locations)
        }
    }

    var permissionsListener: PermissionsListener = object : PermissionsListener {
        override fun onExplanationNeeded(permissionsToExplain: List<String>) {

        }

        override fun onPermissionResult(granted: Boolean) {
            if (granted) {

                // Permission sensitive logic called here, such as activating the Maps SDK's LocationComponent to show the device's location

            } else {

                // User denied the permission

            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

       MapboxOptions.accessToken = "pk.eyJ1IjoibWtnYXRsZSIsImEiOiJjbHlpaWNtZXkwYzZzMmlzZWxzNjA0Y2l4In0.R3WiMQ6K-UjHAuzgtLj35g"
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Permission sensitive logic called here, such as activating the Maps SDK's LocationComponent to show the device's location

            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(28.1879101,-25.7459277))
                    .pitch(0.0)
                    .zoom(2.0)
                    .bearing(0.0)
                    .build()
            )


            mapView.location.enabled =true


            val request = LocationProviderRequest.Builder()
                .interval(IntervalSettings.Builder().interval(0L).minimumInterval(0L).maximumInterval(0L).build())
                .displacement(0F)
                .accuracy(AccuracyLevel.HIGHEST)
                .build();



            val result = locationService.getDeviceLocationProvider(request)
            if (result.isValue) {
                locationProvider = result.value!!
            } else {
                Log.e(TAG,"Failed to get device location provider")
            }

            locationProvider?.addLocationObserver(locationObserver)

            mapView.mapboxMap.addOnMapClickListener{
                val startPoint = Point.fromLngLat(28.1879101,-25.7459277)
                CoroutineScope(Dispatchers.IO).launch {
                    val data = api.builder.getDirections(startPoint.latitude().toString(),
                        startPoint.longitude().toString(),

                        it.latitude().toString(),
                        it.longitude().toString(),
                        "add your api key here"
                    )


                    if (data.isSuccessful)
                    {       Log.v("whatstheerror", data.raw().toString())
                        launch(Dispatchers.Main) {


// Create an instance of the Annotation API and get the polyline manager.
                            val annotationApi = mapView?.annotations
                            val polylineAnnotationManager = annotationApi!!.createPolylineAnnotationManager()
// Define a list of geographic coordinates to be connected.
                            val points2 = arrayListOf<Point>()

                            if (data.body()!!.routes.size>0) {


                                val coordinates = data.body()!!.routes.get(0)!!.geometry.coordinates

                                for (i in 0..coordinates.size - 1) {
                                    points2.add(
                                        Point.fromLngLat(
                                            coordinates.get(i).get(0),
                                            coordinates.get(i).get(1)
                                        )
                                    )

                                }
                            }else
                            {
                                Toast.makeText(this@MainActivity,"No route, Try again",Toast.LENGTH_SHORT)
                            }

                                val points = listOf(
                                    Point.fromLngLat(17.94, 59.25),
                                    Point.fromLngLat(18.18, 59.37)
                                )

// Set options for the resulting line layer.
                            val polylineAnnotationOptions: PolylineAnnotationOptions = PolylineAnnotationOptions()
                                .withPoints(points2)
                                // Style the line that will be added to the map.
                                .withLineColor("#ee4e8b")
                                .withLineWidth(5.0)
// Add the resulting line to the map.
                          //  polylineAnnotationManager!!.deleteAll()
                            polylineAnnotationManager?.create(polylineAnnotationOptions)
                        }
                    }
                }



                true
            }




        } else {
            permissionsManager = PermissionsManager(permissionsListener)
            permissionsManager.requestLocationPermissions(this)
        }




    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode,
            permissions as Array<String>, grantResults)
    }
}