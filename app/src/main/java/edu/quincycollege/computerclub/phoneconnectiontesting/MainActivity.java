package edu.quincycollege.computerclub.phoneconnectiontesting;

import android.bluetooth.BluetoothDevice;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.quincycollege.computerclub.btinrange.BtInRangeCallBacks;
import edu.quincycollege.computerclub.btinrange.BtInRange;

public class MainActivity extends AppCompatActivity implements BtInRangeCallBacks {

    private final String MAC1 = "FF:FF:FF:FF:FF:FF";
    private final String MAC2 = "FF:FF:FF:FF:FF:FF";
    private BtInRange mBtInRange;
    private Button mButton1;
    private Button mButton2;
    private TextView text1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text1 = (TextView)findViewById(R.id.text1);

        mBtInRange = new BtInRange(this);
        BroadcastReceiver mReceiver = mBtInRange.getReceiver();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        registerReceiver(mReceiver, filter);

        mButton1 = (Button)findViewById(R.id.button1);
        mButton1.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            text1.setText("Checking");
                                            mBtInRange.setTargetDeviceByMac(MAC1);
                                            mBtInRange.setIntervalMillisec(30000);
                                            mBtInRange.startChecker();
                                        }
                                    }
        );

        mButton2 = (Button)findViewById(R.id.button2);
        mButton2.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            text1.setText("Checking");
                                            mBtInRange.setTargetDeviceByMac(MAC2);
                                            mBtInRange.setIntervalMillisec(30000);
                                            mBtInRange.startChecker();
                                        }
                                    }
        );

    }

    @Override
    public void onPause() {
        super.onPause();
        mBtInRange.stopChecker();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the BroadcastReceiver receiver.
        unregisterReceiver(mBtInRange.getReceiver());

    }

    public void btInRangeResult(final boolean inRange) {
        //Update UI via runOnUiThread(), avoid CalledFromWrongThreadException
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (inRange) {
                        text1.setText("Connected");
                        Log.v("Msg", "btInRangeResult Connected");

                    } else {
                        text1.setText("Disconnected");

                        Log.v("Msg", "btInRangeResult Disconnected");
                    }
                }
            });


        } catch (Exception ex) {
            Log.v("Msg", "btInRangeResult error", ex);

        }
    }
}
