package com.example.mywifidirectapp.services;

import android.graphics.Color;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientThread  extends Thread{
    public static final int SERVERPORT = 3003;
    public InetAddress SERVER_IP;
    private Socket socket;
    private BufferedReader input;
    public ClientThread(InetAddress groupOwnerAddress){
        SERVER_IP=groupOwnerAddress;
    }

        @Override
        public void run() {

            try {
                Log.d("SERVER_IP", SERVER_IP.toString());
                socket = new Socket(SERVER_IP, SERVERPORT);

                while (!Thread.currentThread().isInterrupted()) {

                    this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = input.readLine();
                    if (null == message || "Disconnect".contentEquals(message)) {
                        Thread.interrupted();
                        message = "Server Disconnected.";

                        //  showMessage(message, Color.RED);
                        break;
                    }
                    Log.d("line39",""+message);
                    //showMessage("Server: " + message, clientTextColor);
                }

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }


       public void sendMessage(final String message) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (null != socket) {
                            Log.d("MESSAGE "," client sending  : "+message);
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())),
                                    true);
                            out.println(message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }


}
