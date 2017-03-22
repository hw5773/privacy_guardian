package org.socialcoding.privacyguardian.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.socialcoding.privacyguardian.Activity.MainActivity;
import org.socialcoding.privacyguardian.Inteface.MainActivityInterfaces;
import org.socialcoding.privacyguardian.R;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static org.socialcoding.privacyguardian.R.id.container;
import static org.socialcoding.privacyguardian.R.id.map;
import static org.socialcoding.privacyguardian.R.id.submenuarrow;

public class GoogleMapsFragment extends Fragment implements OnMapReadyCallback{
    private GoogleMap googleMap;
    private MapView mapView;
    private MainActivityInterfaces.OnGoogleMapsInteractionListener mListener;
    private ArrayList<String> coordinates;

    public static final String ARG_LAT_LANG = "DataLatLng";
    
    @Override
    public void onMapReady(GoogleMap map){
        map.addMarker(new MarkerOptions()
                .position(new LatLng(0,0))
                .title("Marker"));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            if(getArguments() != null){
                    //coordinates are saved as string "123.456;78.91234"
                    coordinates = getArguments().getStringArrayList(ARG_LAT_LANG);
            }
        else Log.d("VpnService","no args");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_google_maps, container, false);

        Button button = (Button) rootView.findViewById(R.id.backbutton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackButtonPressed();
            }
        });

        mapView = (MapView) rootView.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        mapView.onResume();  // needed to get the map to display immediately
        try{
            MapsInitializer.initialize(getActivity().getApplicationContext());
        }catch (Exception e){
            e.printStackTrace();
        }
        //from http://stackoverflow.com/questions/19353255/how-to-put-google-maps-v2-on-a-fragment-using-viewpager
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                    //For showing a move to my location button
                    // googleMap.setMyLocationEnabled(true);
                    int length = coordinates.size();
                    LatLng latLng = new LatLng(0,0);
                    for (int i=0;i< length ;i++) {
                        //For dropping a marker at a point on the Map
                        String[] tmpArray;
                        Log.d("VpnService",length+coordinates.get(i)+i);
                        tmpArray = coordinates.get(i).split(";");
                        double lang = Double.parseDouble(tmpArray[0]);
                       // sumlang += lang;
                        double lng = Double.parseDouble(tmpArray[1]);
                        //sumlng += lng;
                        latLng = new LatLng(lang,lng);
                        googleMap.addMarker(new MarkerOptions().position(latLng).title("Marker Title").snippet("Marker Description"));
                    }
                    if(length==0){System.out.print("??"); return;}
                    //LatLng cameraCenter= new LatLng(sumlang/length,sumlng/length);
                    //For zooming automatically to the location of the marker
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(17).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivityInterfaces.OnGoogleMapsInteractionListener) {
            mListener = (MainActivityInterfaces.OnGoogleMapsInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement MainActivityInterfaces.OnGoogleMapsInteractionListener");
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        mapView.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();
        mapView.onPause();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void onBackButtonPressed(){
        if(mListener !=null){
            mListener.onbackButtonPressed();
        }
    }
}
