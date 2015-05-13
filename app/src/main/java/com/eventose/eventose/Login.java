package com.eventose.eventose;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;


public class Login extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Firebase.setAndroidContext(this);
        final Firebase firebase = new Firebase("https://eventose.firebaseio.com");

        final Button login = (Button) findViewById(R.id.button3);
        final EditText username  = (EditText) findViewById(R.id.editText);
        final EditText password  = (EditText) findViewById(R.id.editText2);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebase.child("User").addValueEventListener(new ValueEventListener() {
                    boolean usernameFound = false;
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            if (data.getKey().toString().equals(username.getText().toString())){
                                usernameFound = true;
                                if (snapshot.child(data.getKey().toString()).child("password").getValue().toString().equals(password.getText().toString())) {
                                    //  login to the dashboard
                                    Intent newIntent = new Intent(getApplicationContext(), DashBoard.class);
                                    newIntent.putExtra("username",username.getText().toString());
                                    startActivity(newIntent);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Password is wrong", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        if(!usernameFound)
                            Toast.makeText(getApplicationContext(), "Username does not exist", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        System.out.println("The read failed: " + firebaseError.getMessage());
                    }
                });
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
