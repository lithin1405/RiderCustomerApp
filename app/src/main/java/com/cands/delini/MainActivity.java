package com.cands.delini;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;


import com.cands.delini.asynctask.MyAsyncTask;
import com.cands.delini.listener.AsyncTaskCompleteListener;
import com.cands.delini.model.CabsInfo;
import com.cands.delini.utility.Utilities;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap map;
    private LocationManager locationManager;
    private Context context;

    private RadioGroup vehicleType;

    private String vType = "cab";

    private CountDownTimer timer;

    private WeakHashMap<Marker, String> markerMap = new WeakHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        vehicleType = (RadioGroup) findViewById(R.id.vehicleType);

        vehicleType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                networkCall();
                if(vehicleType.getCheckedRadioButtonId() == R.id.cab){
                    vType = "car";
                }else if(vehicleType.getCheckedRadioButtonId() == R.id.clado){
                    vType = "bike";
                }else if(vehicleType.getCheckedRadioButtonId() == R.id.keke){
                    vType = "keke";
                }
            }
        });


        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        //  map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, this);

        if(Utilities.networkAvailability(context)) {
            networkCall();
        }else {
            Utilities.showAlert(context, "", "Check your internet connection");
        }

    }

    private void networkCall() {
        MyAsyncTask myAsyncTask = new MyAsyncTask("", new AsyncTaskCompleteListener() {
            @Override
            public void onAsynComplete(String result) {
                if (result != null && !result.isEmpty()) {
                    if(Utilities.isJSONValid(result)){
                        try {
                            JSONArray array = new JSONArray(result);
                            List<CabsInfo> cabsList = new ArrayList<>();
                            for(int i=0; i<array.length(); i++){
                                CabsInfo dto = new CabsInfo();
                                JSONObject obj = array.getJSONObject(i);
                                dto.setName(obj.optString("name"));
                                dto.setContact(obj.optString("mobileno"));
                                dto.setVehicleNo(obj.optString("vehicleno"));
                                dto.setLocation(obj.optString("location"));
                                dto.setVehicleType(obj.optString("vehicle_type"));
                                cabsList.add(dto);
                            }
                            showCabLocations(cabsList);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else{
                        Utilities.showAlert(context, "", "Invalid response");
                    }

                } else {
                    Utilities.showAlert(context, "Network problem", "Check your internet connection");
                }
                Utilities.hideProgressDialog();
            }
        }, 30 * 1000, 30 * 1000);

        myAsyncTask.execute(Utilities.APP_URL + "cabdriver.php");
//        myAsyncTask.execute(Utilities.APP_URL + "gettojson.php");
    }

    private void showCabLocations(List<CabsInfo> cabs) {

        if(cabs != null && !cabs.isEmpty()){
            map.clear();
            markerMap.clear();
            for(CabsInfo dto : cabs){
                if(!dto.getVehicleType().equals(vType))
                    continue;
                MarkerOptions markerOptions = new MarkerOptions();
                LatLng currentLocation = new LatLng(Double.valueOf(dto.getLocation().split(",")[0]), Double.valueOf(dto.getLocation().split(",")[1]));
                markerOptions.position(currentLocation);
                markerOptions.title("" + dto.getName() + " " + dto.getContact());
                if(dto.getVehicleType().equals("car")) {
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.cab_icon_48));
                }else if(dto.getVehicleType().equals("bike")){
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.clado_48));
                }else if(dto.getVehicleType().equals("keke")){
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.keke));
                }



                Marker marker = map.addMarker(markerOptions);

                markerMap.put(marker, dto.getContact());

            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;


         map.setMyLocationEnabled(true);

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final String number = markerMap.get(marker);

                timer = new CountDownTimer(1000, 3000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        showAlert(number);
                    }
                }.start();

                //Toast.makeText(MainActivity.this, "number : " + number, Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    private void showAlert(final String number){
        if(number == null)
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Alert");
        builder.setMessage("Voulez vous appeler"+number);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callToNum(number);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }

    private void callToNum(String num){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:"+num));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        if(timer != null)
            timer.cancel();
        super.onResume();
    }

    @Override
    public void onLocationChanged(Location location) {

        //map.clear();
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());


        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLocation);
        markerOptions.title("i'm here");

        //map.addMarker(markerOptions);

        // map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17.0f));

        networkCall();

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13.0f));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
