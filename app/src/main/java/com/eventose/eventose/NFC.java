package com.eventose.eventose;

import android.content.Intent;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

public class NFC extends ActionBarActivity implements NfcAdapter.CreateNdefMessageCallback{
    NfcAdapter mNfcAdapter;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        String temp_eventname = "";
        Bundle extra = getIntent().getExtras();
        if(extra != null){
            temp_eventname = extra.getString("eventname");
        }
        final String eventname = temp_eventname;
        final Button startNFC = (Button)findViewById(R.id.button5);
        final Button stopNFC = (Button)findViewById(R.id.button6);
        Firebase firebase = new Firebase("https://eventose.firebaseio.com");

        startNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Start NFC button clicked", Toast.LENGTH_SHORT).show();
                mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
                if (!mNfcAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(android.provider.Settings.ACTION_NFC_SETTINGS);
                    startActivity(enableBtIntent);
                } else {
                    if (mNfcAdapter == null) {
                        Toast.makeText(getApplicationContext(), "NFC is not available", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    // Register callback
                    mNfcAdapter.setNdefPushMessageCallback(null, NFC.this);
                    startNFC.setBackgroundColor(Color.parseColor("#42ff23"));
                    stopNFC.setBackgroundColor(Color.parseColor("#ff0000"));
                }

            }
        });





        stopNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Stop NFC button clicked", Toast.LENGTH_SHORT).show();
                startNFC.setBackgroundColor(Color.parseColor("#006F94"));
                stopNFC.setBackgroundColor(Color.parseColor("#006F94"));

            }

        });
}
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        //update user in firebase to have NFC timestamp
        
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        textView = (TextView) findViewById(R.id.textView);
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        textView.setText(new String(msg.getRecords()[0].getPayload()));
    }


}
