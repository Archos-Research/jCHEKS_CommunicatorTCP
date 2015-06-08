package com.archosResearch.jCHEKS.communicator.tcp;

import com.archosResearch.jCHEKS.communicator.tcp.exception.TCPSocketException;
import com.archosResearch.jCHEKS.communicator.ReceiverObserver;
import com.archosResearch.jCHEKS.communicator.SenderObserver;
import com.archosResearch.jCHEKS.communicator.exception.CommunicatorException;
import com.archosResearch.jCHEKS.concept.communicator.AbstractCommunication;
import com.archosResearch.jCHEKS.concept.communicator.AbstractCommunicator;
import com.archosResearch.jCHEKS.concept.exception.AbstractCommunicatorException;

/**
 *
 * @author Thomas Lepage
 */
public class TCPCommunicator extends AbstractCommunicator implements SenderObserver, ReceiverObserver {

    private final AbstractSender sender;
    private final AbstractReceiver receiver;

    public TCPCommunicator(TCPSender sender, TCPReceiver receiver) {

        this.sender = sender;
        this.sender.addObserver(this);

        this.receiver = receiver;
        this.receiver.addObserver(this.sender.getIpAddress(), this);
    }

    @Override
    public void sendCommunication(AbstractCommunication communication) throws AbstractCommunicatorException {
        try {
            this.sender.sendCommunication(communication);
        } catch (TCPSocketException ex) {
            throw new CommunicatorException("Cannot send communication.", ex);
        }
    }
    
    @Override
    public void messageReceived(AbstractCommunication communication) {
        notifyCommunicationReceived(communication);
    }

    @Override
    public void ackReceived() {
        notifyAckReceived();
    }

}
