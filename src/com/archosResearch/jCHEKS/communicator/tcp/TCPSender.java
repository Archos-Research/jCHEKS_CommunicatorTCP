package com.archosResearch.jCHEKS.communicator.tcp;

import com.archosResearch.jCHEKS.communicator.AbstractSender;
import com.archosResearch.jCHEKS.communicator.tcp.exception.TCPSocketException;
import com.archosResearch.jCHEKS.communicator.SenderObserver;
import com.archosResearch.jCHEKS.communicator.tcp.exception.TCPSecureAckReceiverException;
import com.archosResearch.jCHEKS.concept.communicator.AbstractCommunication;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Thomas Lepage thomas.lepage@hotmail.ca
 */
public class TCPSender extends AbstractSender{

    private final String ipAddress;
    private final int port;
    
    public TCPSender(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }
     
    @Override
    public void sendCommunication(AbstractCommunication communication) throws TCPSocketException {
        try {
            Socket clientSocket = new Socket(this.ipAddress, port);
            //Wait 10 sec and if nothing come in the socket throws Timeout exception.
            clientSocket.setSoTimeout(10000); 
            
            OutputStream outToDestination = clientSocket.getOutputStream();
            DataOutputStream dataOutToDestination = new DataOutputStream(outToDestination);

            System.out.println("Sending communication to destination...");
            dataOutToDestination.write(communication.getCommunicationString().getBytes());

            // TODO : Then, we need to decide what we do if we don't receive the ack : to evolve, or not to evolve, that is the question!
            System.out.println("Waiting for ACK");            

            InputStream inFromDestination = clientSocket.getInputStream();
            DataInputStream dataInFromDestination = new DataInputStream(inFromDestination);

            //TODO Create better ack system.
            System.out.println(dataInFromDestination.readUTF());
            notifyMessageACK(communication);
            
            Runnable senderTask = () -> { try {
                senderSecureAck(clientSocket, communication);
                } catch (TCPSecureAckReceiverException ex) {
                    Logger.getLogger(TCPSender.class.getName()).log(Level.SEVERE, null, ex);
                }
            };
            Thread senderSecureAckThread = new Thread(senderTask);
            senderSecureAckThread.start();

        } catch (IOException ex) {
            throw new TCPSocketException("Socket error.", ex);
        }
    }

    protected void notifyMessageACK(AbstractCommunication communication) {
        for (SenderObserver observer : this.observers) {
            observer.ackReceived(communication);
        }
    }
    
    protected void notifySecureACK(AbstractCommunication communication) {
        for (SenderObserver observer : this.observers) {
            observer.secureAckReceived(communication);
        }
    }
    
    private void senderSecureAck(Socket clientSocket, AbstractCommunication communication) throws TCPSecureAckReceiverException {
        try {
            InputStream inFromDestination = clientSocket.getInputStream();
            DataInputStream dataInFromDestination = new DataInputStream(inFromDestination);
            System.out.println("Waiting for secure ack");
            String ackMessage = dataInFromDestination.readUTF();
            
            //Maybe send the ack.
            notifySecureACK(communication);
            
            clientSocket.close();
            
            
        } catch (IOException ex) {
            throw new TCPSecureAckReceiverException("Secure ACK error", ex);
        }
    }
}
