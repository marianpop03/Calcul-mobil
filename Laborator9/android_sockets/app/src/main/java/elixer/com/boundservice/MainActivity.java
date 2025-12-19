package elixer.com.boundservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView; // Import nou
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    boolean mServiceBound = false;
    private SocketService mBoundService;
    private TextView myTextView; // Variabila pentru text

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start = findViewById(R.id.bind);
        Button stop = findViewById(R.id.stop_service);
        myTextView = findViewById(R.id.tv_message); // 1. Gasim TextView-ul

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SocketService.class);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mServiceBound) {
                    unbindService(mConnection);
                    mServiceBound = false;
                    myTextView.setText("Server oprit.");
                }
            }
        });
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
            mBoundService = binder.getService();
            mServiceBound = true;

            // 2. Ne abonam la mesaje!
            mBoundService.setOnMessageReceivedListener(new SocketService.OnMessageReceivedListener() {
                @Override
                public void onMessageReceived(String message) {

                    // 3. IMPORTANT: Trecem pe Thread-ul UI pentru a actualiza ecranul
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myTextView.setText("Mesaj primit: " + message);
                        }
                    });

                }
            });

            mBoundService.IsBoundable();
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            mServiceBound = false;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServiceBound) {
            unbindService(mConnection);
            mServiceBound = false;
        }
    }
}