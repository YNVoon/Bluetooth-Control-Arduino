package com.example.bluetoothfyp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class ActivityControl extends AppCompatActivity{

    private String address = null;
    private Button turnOnButton;
    private Button turnOffButton;
    private Button disconnectButton;
    private ProgressDialog progress;

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;

    private boolean isBluetoothConnected = false;
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        Intent newIntent = getIntent();
        address = newIntent.getStringExtra(ActivityMain.PASSING_MAC_ADDRESS);

        turnOnButton = findViewById(R.id.turnOnButton);
        turnOffButton = findViewById(R.id.turnOffButton);
        disconnectButton = findViewById(R.id.disconnectButton);

        // Execution of AsyncTask to connect Bluetooth
        new ConnectBluetooth().execute();

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });

        turnOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnComment();
            }
        });

        turnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffComment();
            }
        });

    }

    private void disconnect(){

        if (bluetoothSocket != null){
            try {
                bluetoothSocket.close();
                Toast.makeText(this, "Bluetooth disconnected", Toast.LENGTH_SHORT).show();
            }catch (IOException e){
                Toast.makeText(this, "Error disconnect", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void turnOnComment(){
        if (bluetoothSocket != null){
            try {
                bluetoothSocket.getOutputStream().write("TO".toString().getBytes());
                Toast.makeText(this, "Comment sent", Toast.LENGTH_SHORT).show();
            } catch (IOException e){
                Toast.makeText(this, "Sending error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void turnOffComment(){
        if (bluetoothSocket != null){
            try {
                bluetoothSocket.getOutputStream().write("TF".toString().getBytes());
                Toast.makeText(this, "Comment sent", Toast.LENGTH_SHORT).show();
            } catch (IOException e){
                Toast.makeText(this, "Sending error", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class ConnectBluetooth extends AsyncTask<Void, Void, Void>{

        private boolean connectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ActivityControl.this, "Connecting to "+ address, "Please wait!");
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {

                if (bluetoothSocket == null || !isBluetoothConnected) { // When bluetooth haven connect
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    // Connects to the device's address and checks if it's available
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                    // Create RFCOMM (SPP) connection
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    bluetoothSocket.connect();
                }

            } catch (IOException e){
                connectSuccess = false;

//                Toast.makeText(ActivityControl.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!connectSuccess){
                Toast.makeText(ActivityControl.this, "Connection Failed.", Toast.LENGTH_SHORT).show();
                finish(); // return to the previous layout
            } else {
                Toast.makeText(ActivityControl.this, "Connected", Toast.LENGTH_SHORT).show();
                isBluetoothConnected = true;
            }

            progress.dismiss();
        }
    }
}
