package com.test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;



import java.util.Properties;

public  class TestMainUtil {

    public static ActorSystem crearActorSystem(String port) {
        return crearActorSystem("127.0.0.1",port);
    }

    public static ActorSystem crearActorSystem(String host,String port) {
        Properties propertie = new Properties();
        propertie.setProperty("akka.actor.provider","akka.remote.RemoteActorRefProvider");
        propertie.setProperty("akka.remote.netty.tcp.hostname", host);
        propertie.setProperty("akka.remote.netty.tcp.port", port);
        Config configuration = ConfigFactory.parseProperties(propertie);
        ActorSystem system = ActorSystem.create("strategy", configuration);
        return system;
    }
    // 这样创建的ActorSystem实例化对象是直接在一个jvm终端饿相互调用
    public static ActorSystem crearActorSystem() {
        return ActorSystem.create("strategy", ConfigFactory.load("akka.config"));
    }
}
