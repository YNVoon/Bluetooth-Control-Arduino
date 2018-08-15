package com.example.bluetoothfyp;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class ActivityControl extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{

    private String address = null;
    private Button turnOnButton;
    private Button turnOffButton;
    private Button disconnectButton;
    private Button turnOnButton2;
    private Button turnOffButton2;
    private Button timerButton;
    private ProgressDialog progress;
    private Switch pirSwitch;
    private TextView instructionText;
    private SeekBar slider;
    private TextView brightness;

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
        turnOnButton2 = findViewById(R.id.turnOnButton2);
        turnOffButton2 = findViewById(R.id.turnOffButton2);
        disconnectButton = findViewById(R.id.disconnectButton);
        timerButton = findViewById(R.id.timerButton);
        pirSwitch = findViewById(R.id.pirSwitch);
        instructionText = findViewById(R.id.instructionText);
        slider = findViewById(R.id.slider);
        brightness = findViewById(R.id.brightness);

        slider.setOnSeekBarChangeListener(seekBarChangeListener);

        hideButtonsVisibility(); // Initial was controlled by the PIR.

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

        turnOnButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOn2Comment();
            }
        });

        turnOffButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOff2Comment();
            }
        });

        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v4.app.DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "time picker");
            }
        });

        pirSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pirSwitch.isChecked()){
                    Toast.makeText(ActivityControl.this, "PIR On", Toast.LENGTH_SHORT).show();
                    turnOnPIRComment();
                }else {
                    Toast.makeText(ActivityControl.this, "PIR Off", Toast.LENGTH_SHORT).show();
                    turnOffPIRComment();
                }
            }
        });
    }

    // Slider listener
    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            //Toast.makeText(ActivityControl.this, Integer.toString(i), Toast.LENGTH_SHORT).show();
            brightness.setText("Brightness: " + Integer.toString(i));

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int i = seekBar.getProgress();
            Toast.makeText(ActivityControl.this, Integer.toString(i), Toast.LENGTH_SHORT).show();
            if (bluetoothSocket != null){
                try {

                    i = i + 70;
                    bluetoothSocket.getOutputStream().write(Integer.toString(i).getBytes());

                } catch (IOException e){
                    Toast.makeText(ActivityControl.this, "Sending error", Toast.LENGTH_SHORT).show();
                }
            }

        }
    };

    private void turnOffPIRComment() {
        showButtonsVisibility();
        if (bluetoothSocket != null){
            try {
                bluetoothSocket.getOutputStream().write("PIROFF".toString().getBytes());
                Toast.makeText(this, "Comment sent", Toast.LENGTH_SHORT).show();
            } catch (IOException e){
                Toast.makeText(this, "Sending error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void hideButtonsVisibility() {
        instructionText.setVisibility(View.VISIBLE);
        turnOffButton.setVisibility(View.GONE);
        turnOnButton.setVisibility(View.GONE);
        turnOnButton2.setVisibility(View.GONE);
        turnOffButton2.setVisibility(View.GONE);
    }

    private void showButtonsVisibility() {
        instructionText.setVisibility(View.GONE);
        turnOffButton.setVisibility(View.VISIBLE);
        turnOnButton.setVisibility(View.VISIBLE);
        turnOnButton2.setVisibility(View.VISIBLE);
        turnOffButton2.setVisibility(View.VISIBLE);

    }

    private void turnOnPIRComment() {
        hideButtonsVisibility();
        if (bluetoothSocket != null){
            try {
                bluetoothSocket.getOutputStream().write("PIRON".toString().getBytes());
                Toast.makeText(this, "Comment sent", Toast.LENGTH_SHORT).show();
            } catch (IOException e){
                Toast.makeText(this, "Sending error", Toast.LENGTH_SHORT).show();
            }
        }
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

    private void turnOn2Comment(){
        if (bluetoothSocket != null){
            try {
                bluetoothSocket.getOutputStream().write("TOO".toString().getBytes());
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

    private void turnOff2Comment(){
        if (bluetoothSocket != null){
            try {
                bluetoothSocket.getOutputStream().write("TFF".toString().getBytes());
                Toast.makeText(this, "Comment sent", Toast.LENGTH_SHORT).show();
            } catch (IOException e){
                Toast.makeText(this, "Sending error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String min = Integer.toString(minute);
        final String hour = Integer.toString(hourOfDay);
        if (bluetoothSocket != null){
            try {
                bluetoothSocket.getOutputStream().write(min.toString().getBytes());

                Toast.makeText(this, min + "sent", Toast.LENGTH_SHORT).show();
            } catch (IOException e){
                Toast.makeText(this, "Sending error", Toast.LENGTH_SHORT).show();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        bluetoothSocket.getOutputStream().write(hour.toString().getBytes());
                        Toast.makeText(getApplicationContext(), "Comment sent", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Sending error", Toast.LENGTH_SHORT).show();
                    }
                }
            }, 500);
        }
        Toast.makeText(this, "Set time " + hourOfDay + ":" + minute, Toast.LENGTH_SHORT).show();
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
