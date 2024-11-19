package com.example.maps

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var btnCalculate: Button

    private var start: String = ""
    private var end: String = ""

    var poly: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnCalculate = findViewById(R.id.btnCalculateRoute)
        btnCalculate.setOnClickListener {
            start = ""
            end = ""
            poly?.remove()
            poly = null
            Toast.makeText(this, "Selecciona punto de origen y final", Toast.LENGTH_SHORT).show()
            if (::map.isInitialized) {
                map.setOnMapClickListener { latLng ->
                    val coordinates = "${latLng.latitude}, ${latLng.longitude}"
                    Log.d("MapClick", "Clicked coordinates: $coordinates")

                    if (start.isEmpty()) {
                        start = "${latLng.longitude},${latLng.latitude}"
                        Toast.makeText(this, "Origen seleccionado: $coordinates", Toast.LENGTH_SHORT).show()
                    } else if (end.isEmpty()) {
                        end = "${latLng.longitude},${latLng.latitude}"
                        Toast.makeText(this, "Destino seleccionado: $coordinates", Toast.LENGTH_SHORT).show()
                        createRoute() // Invocar createRoute cuando ambos puntos estén seleccionados
                    }
                }
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
    }

    private fun createRoute() {
    if (start.isNotEmpty() && end.isNotEmpty()) {
        val startCoords = start.split(",").map { it.toDouble() }
        val endCoords = end.split(",").map { it.toDouble() }

        val polylineOptions = PolylineOptions()
            .add(
                LatLng(startCoords[1], startCoords[0]), // Origen
                LatLng(endCoords[1], endCoords[0])      // Destino
            )
            .width(5f)
            .color(Color.BLUE) // Color de la línea

        // Agregar la línea al mapa
        runOnUiThread {
            poly = map.addPolyline(polylineOptions)
        }

        Toast.makeText(this, "Ruta trazada entre los puntos seleccionados.", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(this, "Por favor selecciona ambos puntos.", Toast.LENGTH_SHORT).show()
    }
}

    private fun drawRoute(routeResponse: RouteResponse?) {
        val polyLineOptions = PolylineOptions()
        routeResponse?.features?.first()?.geometry?.coordinates?.forEach {
            polyLineOptions.add(LatLng(it[1], it[0]))
        }
        runOnUiThread {
             poly = map.addPolyline(polyLineOptions)
        }
    }

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}