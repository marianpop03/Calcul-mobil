package elixer.com.boundservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketService extends Service {
    private static String LOG_TAG = "ServerService";
    private final IBinder myBinder = new LocalBinder();
    private ServerSocketThread sst;

    // 1. Definim o "ureche" (Listener) prin care trimitem mesajele afara
    private OnMessageReceivedListener messageListener;

    // Interfata contract
    public interface OnMessageReceivedListener {
        void onMessageReceived(String message);
    }

    // Metoda prin care Activitatea se "aboneaza" la mesaje
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.messageListener = listener;
    }

    public class LocalBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return myBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sst = new ServerSocketThread();
        sst.start();
    }

    // --- THREAD-UL DE RETEA ESTE ACUM INTERN (Inner Class) ---
    // Il mutam aici ca sa aiba acces la variabila "messageListener"
    class ServerSocketThread extends Thread {
        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(8089);
                while (true) {
                    Socket socket = serverSocket.accept();
                    startReader(socket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void startReader(final Socket mSocket) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "UTF-8"));
                        String line;
                        while ((line = in.readLine()) != null) {
                            Log.v(LOG_TAG, "Mesaj: " + line);

                            // 2. AICI ESTE CHEIA: Daca cineva asculta, ii trimitem mesajul
                            if (messageListener != null) {
                                messageListener.onMessageReceived(line);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    public void IsBoundable(){
        // Metoda veche, o poti lasa
    }
}