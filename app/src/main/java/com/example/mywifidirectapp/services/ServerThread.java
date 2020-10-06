package com.example.mywifidirectapp.services;

import android.graphics.Color;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread{
    private ServerSocket serverSocket;
    private Socket tempClientSocket;
    Thread serverThread = null;
    public static final int SERVER_PORT = 3003;

    public void sendMessage(final String message) {
        try {
            Log.d("tempClientSocket",":  not null");
            if (null != tempClientSocket) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PrintWriter out = null;
                        try {
                            out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(tempClientSocket.getOutputStream())),
                                    true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        out.println(message);
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                //findViewById(R.id.start_server).setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
                //showMessage("Error Starting Server : " + e.getMessage(), Color.RED);
            }
            if (null != serverSocket) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        socket = serverSocket.accept();
                        Log.d("SOCKET value :",socket.toString());
                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                       // showMessage("Error Communicating to Client :" + e.getMessage(), Color.RED);
                    }
                }
            }
        }
    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            tempClientSocket = clientSocket;
            try {
                //this.input = new BufferedReader(new InputStreamReader(tempClientSocket.getInputStream()));
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
                //showMessage("Error Connecting to Client!!", Color.RED);
            }
            //showMessage("Connected to Client!!", greenColor);
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    if (null == read || "Disconnect".contentEquals(read)) {
                        Thread.interrupted();
                        read = "Client Disconnected";
                        //showMessage("Client : " + read, greenColor);
                        Log.d("SERVERSIDE","CLient msg on server recived"+read);
                        break;
                    }
                    Log.d("SERVERSIDE","CLient msg on server recived"+read);
                    //showMessage("Client : " + read, greenColor); //take msg from read coming from client side
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }
}
