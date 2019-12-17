package com.sunny;

import akka.actor.UntypedActor;

import java.rmi.RemoteException;

public class Greeter extends UntypedActor{
    public void onReceive(Object message) {

    }

    public int $tag() throws RemoteException {
        return 0;
    }
}
