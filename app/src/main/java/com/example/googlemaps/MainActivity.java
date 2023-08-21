package com.example.googlemaps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.InfoWindowAdapter
{
    private GoogleMap map;
    private ArrayList<LatLng> lista = new ArrayList<>();
    private PolylineOptions lineas = new PolylineOptions();

    private class VolleyRequest {

        private final RequestQueue requestQueue;

        public VolleyRequest(RequestQueue requestQueue) {
            this.requestQueue = requestQueue;
        }

        public void sendJsonObjectRequest(String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, listener, errorListener);
            requestQueue.add(request);
        }
    }

    private VolleyRequest volleyRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        volleyRequest = new VolleyRequest(requestQueue);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        lineas.width(8);
        lineas.color(Color.RED);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.getUiSettings().setZoomControlsEnabled(true);

        CameraUpdate camUpd1 = CameraUpdateFactory
                .newLatLngZoom(new LatLng(-1.010221134283359, -79.47311652740541), 20);
        map.moveCamera(camUpd1);
        map.setOnMapClickListener(this);
        map.setInfoWindowAdapter(this);
    }

    MarkerOptions marcador;

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        String nearbySearchUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + latLng.latitude + "," + latLng.longitude +
                "&radius=1500&type=bar" +
                "&key=" + "AIzaSyC-S81Po-m6s4ZmQmrv4exwBETjuJHXMsg";

        volleyRequest.sendJsonObjectRequest(nearbySearchUrl,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);
                            JSONObject location = place.getJSONObject("geometry").getJSONObject("location");
                            double placeLat = location.getDouble("lat");
                            double placeLng = location.getDouble("lng");
                            String placeName = place.getString("name");
                            String vicinity = place.getString("vicinity");

                            JSONArray photos = place.optJSONArray("photos");
                            String photoReference = "";
                            if (photos != null && photos.length() > 0) {
                                JSONObject photoObject = photos.getJSONObject(0);
                                photoReference = photoObject.getString("photo_reference");
                            }

                            LatLng placeLatLng = new LatLng(placeLat, placeLng);

                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(placeLatLng)
                                    .title(placeName)
                                    .snippet(vicinity);

                            map.addMarker(markerOptions).setTag(photoReference);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                }
        );
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        View view = getLayoutInflater().inflate(R.layout.info_window_layout, null);
        TextView nameTextView = view.findViewById(R.id.txtLocationName);
        TextView nameTextView2 = view.findViewById(R.id.txtubicacion);
        ImageView imageView = view.findViewById(R.id.imageView);
        nameTextView.setText(marker.getTitle());
        nameTextView2.setText(marker.getSnippet());
        String photoReference = (String) marker.getTag();

        if (!photoReference.isEmpty()) {
            String photoUrl = "https://maps.googleapis.com/maps/api/place/photo" +
                    "?maxwidth=100" +
                    "&photo_reference=" + photoReference +
                    "&key=" + "AIzaSyC-S81Po-m6s4ZmQmrv4exwBETjuJHXMsg";

            Glide.with(this)
                    .load(photoUrl)
                    .into(imageView);
        }

        return view;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        return null;
    }
}
