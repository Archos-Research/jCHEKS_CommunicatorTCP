package com.archosResearch.jCHEKS.communicator.tcp;

import com.archosResearch.jCHEKS.communicator.AbstractSender;
import com.archosResearch.jCHEKS.communicator.tcp.exception.TCPSocketException;
import com.archosResearch.jCHEKS.communicator.SenderObserver;
import com.archosResearch.jCHEKS.concept.communicator.AbstractCommunication;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

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
            Socket clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(this.ipAddress, port), 5000);
                        
            OutputStream outToDestination = clientSocket.getOutputStream();
            DataOutputStream dataOutToDestination = new DataOutputStream(outToDestination);

            System.out.println("Sending communication to destination...");
            dataOutToDestination.write(communication.getCommunicationString().getBytes());

            // TODO : We should put a max waiting time for the ack to avoid hanging processes if the communication is lost
            // TODO : Then, we need to decide what we do if we don't receive the ack : to evolve, or not to evolve, that is the question!
            System.out.println("Waiting for ACK");
            InputStream inFromDestination = clientSocket.getInputStream();
            DataInputStream dataInFromDestination = new DataInputStream(inFromDestination);

            //TODO Create better ack system.
            System.out.println(dataInFromDestination.readUTF());
            notifyMessageACK(communication);
            
            /*TCPSecureAckReceiver ackReceiver = new TCPSecureAckReceiver(clientSocket);
            ackReceiver.addObserver(this);
            new Thread(ackReceiver).start();
            */
            Runnable senderTask = () -> { senderSecureAck(clientSocket, communication); };
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
    
    private void senderSecureAck(Socket clientSocket, AbstractCommunication communication) {
        try {
            InputStream inFromDestination = clientSocket.getInputStream();
            DataInputStream dataInFromDestination = new DataInputStream(inFromDestination);
            System.out.println("Waiting for secure ack");
            String ackMessage = dataInFromDestination.readUTF();
            
            //Maybe send the ack.
            notifySecureACK(communication);
            
            clientSocket.close();
            
            
        } catch (IOException ex) {
            //TODO throw TCPSecureAckReceiverException
        }
    }
}
