package com.test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;

public class TestMain3_C {

    public static void main(String[] args) {
        testMethod1();
    }
    public static void testMethod1(){
        ActorSystem system = TestMainUtil.crearActorSystem("8089");
        ActorRef printActor = system.actorOf(Props.create(TestAkkaRpcActorC.class), "testAkkaRpcActorC");
//        system.awaitTermination();
        System.out.println(printActor.path());
    }
}
