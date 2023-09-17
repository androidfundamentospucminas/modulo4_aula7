package com.walker.aula7modulo4

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import java.io.IOException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapGoogle: GoogleMap
    private val LOCATION_PERMISSION_ID = 100

    private var currentLocation: Location? = null

    private lateinit var searchView: SearchView

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchView = findViewById(R.id.searchView)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                var addressList: List<Address>? = null

                if (query != null) {
                    val geoCoder: Geocoder = Geocoder(this@MainActivity)

                    try {
                        addressList = geoCoder.getFromLocationName(query, 1)
                    } catch (e: IOException) {
                        Toast.makeText(
                            this@MainActivity,
                            "Não foi possível encontrar uma localização para este endereço",
                            Toast.LENGTH_SHORT).show()
                        // e.printStackTrace()
                    }

                    if (addressList?.isEmpty() == false) {
                        addressList[0].let {
                            val latLng = LatLng(it.latitude, it.longitude)
                            mapGoogle.addMarker(
                                MarkerOptions().position(latLng).title(it.featureName)
                            )
                            mapGoogle.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                        }
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Não foi possível encontrar uma localização para este endereço",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
    }

    private fun getLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_ID)) {
            val task: Task<Location> = fusedLocationProviderClient.lastLocation
            task.addOnSuccessListener {
                currentLocation = it
                currentLocation?.let {
                    val mapFragment =
                        supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment

                    mapFragment.getMapAsync(this)
                }
            }
        } else {
            return
        }
    }

    private fun checkPermission(permission: String, requestCode: Int): Boolean {
        return if (ContextCompat.checkSelfPermission(this@MainActivity, permission) ==
                PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Toast.makeText(this, "Não é possível mostrar a posição do usuário.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapGoogle = googleMap

        val location = LatLng(
            currentLocation?.latitude ?: -23.533773,
            currentLocation?.longitude ?: -46.625290)

        val title = if (currentLocation !== null) {
            "Posição Atual"
        } else {
            "São Paulo"
        }

        mapGoogle.addMarker(MarkerOptions().position(location).title(title))

        mapGoogle.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))
    }
}