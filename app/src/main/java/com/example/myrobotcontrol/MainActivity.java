package com.example.myrobotcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText ipEntry;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipEntry = findViewById(R.id.ip_entry);
        connectButton = findViewById(R.id.connection_button);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ipAddress = ipEntry.getText().toString();
                Pattern ipValidator =
                        Pattern.compile(
                                "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.)" +
                                        "{3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
                if (ipValidator.matcher(ipAddress).matches()) {
                    goToRobotControlActivity(ipAddress);
                }
                else {
                    incorrectAddressMessage();
                }
            }
        });
    }

    private void goToRobotControlActivity(String ipAddress) {
        Intent toRobotControl = new Intent(this, RobotControlActivity.class);
        toRobotControl.putExtra("IP_ADDRESS", ipAddress);
        startActivity(toRobotControl);
    }

    private void incorrectAddressMessage() {
        Toast.makeText(this, "Введен неверный IP адрес", Toast.LENGTH_LONG).show();
    }
}