package com.eventose.eventose;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;


public class AddEvent extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        String temp_username = "";
        Bundle extra = getIntent().getExtras();
        if(extra != null){
            temp_username = extra.getString("username");
        }
        final String username = temp_username;

        Log.v("String",username);
        Firebase.setAndroidContext(this);
        final Firebase firebase = new Firebase("https://eventose.firebaseio.com");

        Button createEventButton = (Button) findViewById(R.id.createevent);
        final EditText eventname = (EditText) findViewById(R.id.eventname);
        final EditText minor = (EditText) findViewById(R.id.minor);
        final EditText uuid = (EditText) findViewById(R.id.uuid);
        final EditText major = (EditText) findViewById(R.id.major);


        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebase.child("Events").child(uuid.getText().toString().trim() + major.getText().toString().trim() + minor.getText().toString().trim()).child("Owner").setValue(username);
                firebase.child("Events").child(uuid.getText().toString().trim() + major.getText().toString().trim() + minor.getText().toString().trim()).child("EventName").setValue(eventname.getText().toString());
                Toast.makeText(getApplicationContext(), "Event sucessfully added", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), DashBoard.class);
                intent.putExtra("username",username);
                finish();
                startActivity(intent);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
