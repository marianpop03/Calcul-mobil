package elixer.com.boundservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketThread extends Thread {

    @Override
    public void run() {
        try {
            // 1. Deschidem un port (8089) și ascultăm
            ServerSocket serverSocket = new ServerSocket(8089);
            System.out.println("Start server, port 8089");

            while (true) {
                System.out.println("Se așteaptă conectarea clientului..");

                // 2. Blocant: Codul se oprește aici până se conectează cineva
                Socket socket = serverSocket.accept();

                System.out.println("Conexiune client: " + socket.getInetAddress());

                // 3. Gestionăm citirea mesajelor pe un nou thread (pt a permite și alți clienți)
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
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(mSocket.getInputStream(), "UTF-8"));

                    String line;
                    // Citim linie cu linie ce trimite clientul
                    while ((line = in.readLine()) != null) {
                        System.out.println("Mesajul receptionat: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}