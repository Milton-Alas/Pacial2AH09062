package com.example.pacial2ah09062.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.pacial2ah09062.R;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // Coordenadas de ejemplo
    private static final double LATITUDE = 13.701398345532427; // Plaza Salvador del Mundo
    private static final double LONGITUDE = -89.2244762189281;
    private static final String BUSINESS_NAME = "Mi Negocio";
    private static final String BUSINESS_ADDRESS = "Dirección del negocio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Configurar toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ubicación");
        }

        // Obtener el SupportMapFragment y notificar cuando el mapa esté listo
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Configurar el mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Crear la ubicación del negocio
        LatLng businessLocation = new LatLng(LATITUDE, LONGITUDE);

        // Agregar marcador
        mMap.addMarker(new MarkerOptions()
                .position(businessLocation)
                .title(BUSINESS_NAME)
                .snippet(BUSINESS_ADDRESS));

        // Mover la cámara al marcador
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(businessLocation, 15));

        // Listener para clicks en el marcador
        mMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            return true;
        });

        // Listener para clicks en el info window
        mMap.setOnInfoWindowClickListener(marker -> {
            Toast.makeText(MapActivity.this,
                    "Ubicación: " + marker.getTitle(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}