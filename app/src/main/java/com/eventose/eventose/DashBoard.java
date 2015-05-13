package com.eventose.eventose;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DashBoard extends ActionBarActivity {

    public ArrayList<String> UUIDs;
    public ArrayList<String> Majors;
    public ArrayList<String> Minors;

    String passUsername = "";
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    private BeaconManager beaconManager;
    private static final int REQUEST_ENABLE_BT = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        Bundle extra = getIntent().getExtras();
        if(extra != null){
            passUsername = extra.getString("username");
        }
       beaconManager = new BeaconManager(this);
        //Toast.makeText(getApplicationContext(), "Created", Toast.LENGTH_LONG).show();
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);
        beaconManager.setBackgroundScanPeriod(2000,2000);
        beaconManager.setForegroundScanPeriod(2000, 2000);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {

            @Override

            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                //Toast.makeText(getApplicationContext(), "Discovered", Toast.LENGTH_LONG).show();
                // Note that results are not delivered on UI thread.
                runOnUiThread(new Runnable() {
                        @Override
                    public void run() {
                        // Note that beacons reported here are already sorted by estimated
                        // distance between device and beacon.
                        Iterator<Beacon> itra = beacons.iterator();
                        UUIDs = new ArrayList<String>();
                        Majors = new ArrayList<String>();
                        Minors = new ArrayList<String>();

                        while (itra.hasNext()){
                            Beacon b = itra.next();
                            UUIDs.add(b.getProximityUUID());
                            Majors.add(Integer.toString(b.getMajor()));
                            Minors.add(Integer.toString(b.getMinor()));
                        }
                        callFireBase(UUIDs, Majors, Minors);
                        //Toast.makeText(getApplicationContext(), "run", Toast.LENGTH_LONG).show();
                        //Log.d("Beacons", UUIDs);
                    }
                });
            }
        });

    }

    public void callFireBase(final ArrayList<String> arrayList,final ArrayList<String> majors,final ArrayList<String> minors){
        Log.v("UUIDs ArrayList", arrayList.toString());
        Log.v("Major ArrayList", majors.toString());
        Log.v("Minor ArrayList", minors.toString());


        Firebase.setAndroidContext(getApplicationContext());
        final Firebase firebase = new Firebase("https://eventose.firebaseio.com");

        String temp_username = "";
        Bundle extra = getIntent().getExtras();
        if(extra != null){
            temp_username = extra.getString("username");
        }
        final String username = temp_username;
        passUsername = temp_username;
        //final String UUID_test = "asdf3";
        firebase.child("Events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                // Populates the first listview with active events in the area based on a UUID given in UUID_test

                final ListView eventListView = (ListView) findViewById(R.id.listView);
                List<String> eventList = new ArrayList<String>();

                for(int i = 0; i < arrayList.size(); i++) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        String fireBaseUUID = child.getKey();
                        String UUIDkey = arrayList.get(i);
                        String major = Majors.get(i);
                        String minor = Minors.get(i);

                        UUIDkey = UUIDkey.trim() + major.trim() + minor.trim();
                        Log.d("UUID:firebase", child.getKey());
                        Log.d("UUID:key", arrayList.get(i));
                        if(UUIDkey.trim().equals(fireBaseUUID.trim())) {
                            eventList.add(child.child("EventName").getValue().toString());
                        }

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                getApplicationContext(),
                                R.layout.listview,
                                eventList);

                        eventListView.setAdapter(arrayAdapter);

                    }
                }

                eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final String eventchosen = eventListView.getItemAtPosition(position).toString();
                        AlertDialog.Builder adb = new AlertDialog.Builder(DashBoard.this);
                        for(DataSnapshot data : dataSnapshot.getChildren()){
                            if(data.child("EventName").getValue().toString().equals(eventchosen)) {
                                if (data.child("Attendees").hasChild(username)) {
                                    adb.setTitle("Already Attending Event");
                                    adb.setPositiveButton("Cancel Attendance", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                                if (data.child("EventName").getValue().toString().equals(eventchosen)) {
                                                    String UUIDKey = data.getKey();
                                                    firebase.child("Events").child(UUIDKey).child("Attendees").child(username).removeValue();
                                                    Toast.makeText(getApplicationContext(), "Your checkin has been cancelled", Toast.LENGTH_LONG).show();
                                                }
                                            }

                                        }
                                    });

                                    adb.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });

                                    adb.show();


                                } else {
                                    adb.setTitle("Check in");
                                    adb.setPositiveButton("Check in", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //TODO make user in event checked in and give timestamp.
                                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                                if (data.child("EventName").getValue().toString().equals(eventchosen)) {
                                                    Calendar calendar = Calendar.getInstance();
                                                    java.util.Date now = calendar.getTime();
                                                    java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
                                                    String UUID = data.getKey();
                                                    Toast.makeText(getApplicationContext(), "You are checked in!", Toast.LENGTH_SHORT).show();
                                                    firebase.child("Events").child(UUID).child("Attendees").child(username).child("Beacon").setValue(currentTimestamp.toString());
                                                }
                                            }
                                        }
                                    });

                                    adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });

                                    adb.show();
                                }
                            }
                        }
                        //AlertDialog alertDialog = new AlertDialog.Builder(DashBoard.this).create();

                    }
                });

                // Get hosted events list
                final ListView hostListView = (ListView) findViewById(R.id.listView2);
                List<String> hostList = new ArrayList<String>();

                if(getApplicationContext() != null) {
                    for (DataSnapshot host : dataSnapshot.getChildren()) {
                        if (host.child("Owner").getValue().toString().equals(username)) {
                            try{
                                hostList.add(host.child("EventName").getValue().toString());
                            }catch(Exception e){

                            }

                        }
                        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>(
                                getApplicationContext(),
                                R.layout.listview,
                                hostList);
                        hostListView.setAdapter(arrayAdapter1);
                    }

                }



                hostListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String eventname = hostListView.getItemAtPosition(position).toString();
                        Intent intent = new Intent(getApplicationContext(), NFC.class);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError firebaseError){
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Check if device supports Bluetooth Low Energy.
        if (!beaconManager.hasBluetooth()) {
            Toast.makeText(this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
            return;
        }

        // If Bluetooth is not enabled, let user enable it.
        if (!beaconManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectToService();
        }
    }
    @Override
    protected void onStop() {
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
        } catch (RemoteException e) {
        }

        super.onStop();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                connectToService();
            } else {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void connectToService() {
        //Toast.makeText(this, "connecting to service", Toast.LENGTH_LONG).show();
        Log.v("Service", "CONNECTING TO SERVICE");
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (RemoteException e) {
                    Toast.makeText(DashBoard.this, "Cannot start ranging, something terrible happened",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dash_board, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_addevent) {
            Intent newintent  = new Intent(getApplicationContext(), AddEvent.class);
            newintent.putExtra("username", passUsername);
            startActivity(newintent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
