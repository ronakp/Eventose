package com.eventose.eventose;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Iterator;


public class Register extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        Firebase.setAndroidContext(this);
        final Firebase firebase = new Firebase("https://eventose.firebaseio.com");
        final EditText fname = (EditText) findViewById(R.id.editText3);
        final EditText lname = (EditText) findViewById(R.id.editText4);
        final EditText email = (EditText) findViewById(R.id.editText5);
        final EditText pass = (EditText) findViewById(R.id.editText6);
        final EditText repass = (EditText) findViewById(R.id.editText7);
        final EditText username = (EditText) findViewById(R.id.editText8);


        Button button = (Button)findViewById(R.id.button4);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pass.getText().toString().equals(repass.getText().toString())){

                    firebase.child("User").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            Iterable<DataSnapshot> iter = snapshot.getChildren();
                            Iterator iterator = iter.iterator();
                            boolean isTaken = false;
                            for(DataSnapshot child : snapshot.getChildren()){
                                if(child.getKey().toString().equals(username.getText().toString())){
                                    isTaken = true;
                                   // Toast.makeText(getApplicationContext(), "Username is taken", Toast.LENGTH_LONG).show();
                                }
                            }
                            if (isTaken == false) {
                                String deviceId = Secure.getString(getApplicationContext().getContentResolver(),
                                        Secure.ANDROID_ID);
                                firebase.child("User").child(username.getText().toString()).child("firstname").setValue(fname.getText().toString());
                                firebase.child("User").child(username.getText().toString()).child("lastname").setValue(lname.getText().toString());
                                firebase.child("User").child(username.getText().toString()).child("email").setValue(email.getText().toString());
                                firebase.child("User").child(username.getText().toString()).child("password").setValue(pass.getText().toString());
                                firebase.child("User").child(username.getText().toString()).child("deviceid").setValue(deviceId);
                                Intent newIntent = new Intent(getApplicationContext(), Login.class);
                                startActivity(newIntent);
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            System.out.println("The read failed: " + firebaseError.getMessage());
                        }
                    });

                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
