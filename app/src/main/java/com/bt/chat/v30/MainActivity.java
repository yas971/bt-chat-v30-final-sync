package com.bt.chat.v30;
import android.app.Activity;
import android.bluetooth.*;
import android.os.*;
import android.widget.*;
import java.util.*;
import java.io.*;

public class MainActivity extends Activity {
    private final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter mAdapter;
    private BluetoothSocket mSocket;
    private TextView mLogs;
    private EditText mInput;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(50,50,50,50);
        mLogs = new TextView(this);
        mLogs.setText("V30 NATIVE SOURCE SYNC\n");
        l.addView(mLogs);
        Button scn = new Button(this);
        scn.setText("RECHERCHER CAROLE");
        scn.setOnClickListener(v -> connectBonded());
        l.addView(scn);
        mInput = new EditText(this);
        l.addView(mInput);
        Button snd = new Button(this);
        snd.setText("ENVOYER MESSAGE");
        snd.setOnClickListener(v -> send());
        l.addView(snd);
        setContentView(l);
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if(Build.VERSION.SDK_INT >= 31) {
            requestPermissions(new String[]{"android.permission.BLUETOOTH_SCAN","android.permission.BLUETOOTH_CONNECT","android.permission.ACCESS_FINE_LOCATION"}, 1);
        }
        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                BluetoothServerSocket s = mAdapter.listenUsingInsecureRfcommWithServiceRecord("BTCHAT", UUID_SPP);
                mSocket = s.accept();
                listen();
            } catch (Exception e) {}
        }).start();
    }

    private void connectBonded() {
        Set<BluetoothDevice> bonded = mAdapter.getBondedDevices();
        for(BluetoothDevice d : bonded) {
            new Thread(() -> {
                try {
                    mSocket = d.createInsecureRfcommSocketToServiceRecord(UUID_SPP);
                    mSocket.connect();
                    listen();
                } catch (Exception e) {}
            }).start();
        }
    }

    private void listen() {
        runOnUiThread(() -> mLogs.append("CONNECTÉ !\n"));
        try {
            InputStream is = mSocket.getInputStream();
            byte[] buf = new byte[1024];
            while(true) {
                int len = is.read(buf);
                if(len > 0) {
                    String m = new String(buf, 0, len);
                    runOnUiThread(() -> mLogs.append("Ami: " + m + "\n"));
                }
            }
        } catch (Exception e) {}
    }

    private void send() {
        try {
            String m = mInput.getText().toString();
            mSocket.getOutputStream().write(m.getBytes());
            mLogs.append("Moi: " + m + "\n");
            mInput.setText("");
        } catch (Exception e) {}
    }
}