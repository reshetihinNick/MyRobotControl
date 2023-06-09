package com.example.myrobotcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

public class ConnectionActivity extends AppCompatActivity {

    private EditText ipEntry;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_activity);
        ipEntry = findViewById(R.id.ip_entry);
        connectButton = findViewById(R.id.connection_button);

        connectButton.setOnClickListener(view -> {
            String ipAddress = ipEntry.getText().toString();
            Pattern ipValidator =
                    Pattern.compile(
                            "^((?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\.)" +
                                    "{3}(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)$"
                    );
            if (ipValidator.matcher(ipAddress).matches()) {
                goToRobotControlActivity(ipAddress);
            }
            else {
                incorrectAddressMessage();
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