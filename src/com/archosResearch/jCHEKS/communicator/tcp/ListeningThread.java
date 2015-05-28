/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archosResearch.jCHEKS.communicator.tcp;

import com.archosResearch.jCHEKS.communicator.AbstractCommunicator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Thomas Lepage thomas.lepage@hotmail.ca
 */
public class ListeningThread implements Runnable{
    
    private int port;
    private TCPReceiver receiver;
    private ServerSocket listeningSocket;
    private boolean running = true;
   
    public ListeningThread(int aPort, TCPReceiver receiver){
        this.port = aPort;
        this.receiver = receiver;
    }
    
     @Override
    public void run() {
        try {
            listeningSocket = new ServerSocket(this.port);
            
            while(running) {
                Socket client = listeningSocket.accept();
                System.out.println("Receiving communication...");                
                
                DataInputStream dataIn = new DataInputStream(client.getInputStream());
                DataOutputStream dataOut = new DataOutputStream(client.getOutputStream());

                this.receiver.receivingCommunication(dataIn.readUTF());
                System.out.println("Sending ACK...");
                
                dataOut.writeUTF("I received your message");
            }                
            
            listeningSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(TCPCommunicator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void stop(){
        running = false;
    }
}
