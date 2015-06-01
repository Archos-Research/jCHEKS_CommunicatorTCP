/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archosResearch.jCHEKS.communicator.tcp;

import com.archosResearch.jCHECKS.Engine.Engine;
import com.archosResearch.jCHEKS.communicator.AbstractCommunicator;

/**
 *
 * @author Thomas Lepage
 */
public class TCPCommunicator extends AbstractCommunicator{
    
    private final TCPSender sender;
    private final TCPReceiver receiver;
    
    public TCPCommunicator(String ipAddress, int port, Engine engine){

        this.sender = new TCPSender(ipAddress, port, this);
        
        if(TCPReceiver.start(engine)){            
            new Thread(TCPReceiver.getInstance()).start();
        }  
        
        this.receiver = TCPReceiver.getInstance();
        
    }
    
    @Override
    public boolean sendCommunication(Communication communication) {        
        return this.sender.sendCommunication(communication);
    }  
    
    public void ackReceived(){
        notifyMessageACK();
    }
}
