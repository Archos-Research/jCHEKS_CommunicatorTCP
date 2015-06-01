/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archosResearch.jCHEKS.communicator;

import com.archosResearch.jCHEKS.Engine.MockEngine;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Thomas Lepage
 */
public class CommunicatorObservableTest {
    
    /**
     * Test of notifyMessageACK method, of class CommunicatorObservable.
     */
    @Test
    public void testNotifyMessageACK() {
        MockEngine engine = new MockEngine();
        SenderObservable instance = new SenderObservable();
        instance.addObserver(engine);
        
        instance.notifyMessageACK();
        
        assertTrue(engine.isAckReceived());
    }
    
}
