package com.test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class TestMain2_1 extends TestMainUtil {
    public static void main(String[] args) {
        ActorSystem system = null;

        system = TestMainUtil.crearActorSystem("8083");
        testMethod1(system);
    }

    public static void testMethod1(ActorSystem system){
        ActorRef workerActor = system.actorOf(Props.create(WorkerActor.class),"workerActor");
        system.awaitTermination();
    }
}
