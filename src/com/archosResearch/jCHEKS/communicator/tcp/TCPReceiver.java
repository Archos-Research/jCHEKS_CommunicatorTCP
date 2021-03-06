package com.archosResearch.jCHEKS.communicator.tcp;

import com.archosResearch.jCHEKS.communicator.*;
import com.archosResearch.jCHEKS.concept.exception.CommunicationException;
import com.archosResearch.jCHEKS.communicator.exception.ReceiverObserverNotFoundException;
import com.archosResearch.jCHEKS.communicator.tcp.exception.*;
import java.io.*;
import java.net.*;
import java.util.logging.*;

/**
 *
 * @author Thomas Lepage thomas.lepage@hotmail.ca
 */
public class TCPReceiver extends AbstractReceiver implements Runnable {

    private static TCPReceiver instance = null;

    private static int port = 9000;
    private boolean running = true;

    protected TCPReceiver() {
    }

    public static TCPReceiver getInstance(int port) {
        if (instance == null) {
            instance = new TCPReceiver();
            TCPReceiver.port = port;
            Thread receiverThread = new Thread(instance);
            receiverThread.setDaemon(true);
            receiverThread.start();
        }
        return instance;
    }
    
    public static TCPReceiver getInstance() {
        return TCPReceiver.getInstance(TCPReceiver.port);
    }

    public void stopReceiver() {
        this.running = false;
    }

    @Override
    public void run() {
        try {
            ServerSocket listeningSocket = new ServerSocket(TCPReceiver.port);

            while (running) {
                Socket client = listeningSocket.accept();

                Runnable receiveTask = () -> {
                    try {
                        receiveCommunication(client, this);
                    } catch (ReceiverObserverNotFoundException | TCPSocketException | TCPCommunicatorException ex) {
                        Logger.getLogger(TCPReceiver.class.getName()).log(Level.SEVERE, null, ex);
                    }
                };
                Thread receiveThread = new Thread(receiveTask);
                receiveThread.start();
            }

            listeningSocket.close();
        } catch (IOException ex) {
            //Find how to throw an exception through a thread.
            Logger.getLogger(TCPCommunicator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void receiveCommunication(Socket socket, TCPReceiver receiver) throws ReceiverObserverNotFoundException, TCPCommunicatorException, TCPSocketException {
        try {

            DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
            
            String incommingMessage = dataIn.readUTF();          
            System.out.println("Receiving communication");

            //TODO create better ack system.
            
            //Reception of the message ACK
            Communication receivedCommunication = Communication.createCommunication(incommingMessage);
            dataOut.writeUTF(receivedCommunication.getCipherCheck());

            String ackSecure = receiver.notifyMessageReceived(receivedCommunication);
            //Decoding of the message ACK (Secure ACK)
            dataOut.writeUTF(ackSecure);

            socket.close();
        } catch (IOException ex) {
            throw new TCPSocketException("TCPReceiver error", ex);
        } catch (CommunicationException ex) {
            throw new TCPCommunicatorException("Error while creating communication", ex);
        }
    }

}
