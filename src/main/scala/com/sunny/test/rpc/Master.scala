package com.sunny.test.rpc

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import com.sunny.test.rpc.massage.HelloWord

class Master extends Actor{

  println("创建Master对象")

  override def preStart(): Unit = {
    println("Worker 初始化")
  }

  override def receive :Receive = {
    case "connect" => {
      println("一个Worker连接了，，，")
      // 向发送者返回信息
      sender ! "reply"
    }
    case "helloworld" => {

    }

  }
}


object Master{
  def main(args: Array[String]): Unit = {
    val host = "127.0.0.1"
    val port = 8082
    val configStr =
      s"""
                       |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
                       |akka.remote.netty.tcp.hostname = "$host"
                       |akka.remote.netty.tcp.port = "$port"
      """.stripMargin
    val config = ConfigFactory.parseString(configStr)
    val masterSystem = ActorSystem.create("MasterSystem",config)
    val master = masterSystem.actorOf(Props(new Master),"master")

    masterSystem.awaitTermination()

  }
}
