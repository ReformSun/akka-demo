package com.sunny.test.rpc

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import com.sunny.test.rpc.massage.RunAsync
import com.typesafe.config.ConfigFactory

class Worker extends Actor{

  var master:ActorSelection=_

  override def preStart(): Unit = {

    master = context.actorSelection("akka.tcp://MasterSystem@127.0.0.1:8082/user/Master")

    master.tell(new RunAsync, ActorRef.noSender);
//    master ! "connect"
  }
  override def receive :Receive = {

    case "reply" => {
      println("reply")
    }

  }
}


object Worker{

  def main(args: Array[String]): Unit = {
    val host = "127.0.0.1"
    val port = 8083
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
      """.stripMargin
    val config = ConfigFactory.parseString(configStr)
    val masterSystem = ActorSystem.create("WorkerSystem",config)
    val master = masterSystem.actorOf(Props(new Worker),"worker")

    masterSystem.awaitTermination()
  }

}

