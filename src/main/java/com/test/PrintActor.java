package com.test;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Created by liubenlong on 2017/1/12.
 */
public class PrintActor extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void preStart() throws Exception {
        System.out.println(getSelf().path());
        super.preStart();
    }

    @Override
    public void onReceive(Object o) throws Throwable {
        log.info("akka.future.PrintActor.onReceive:" + o);
        if (o instanceof Integer) {
            log.info("print:" + o);
        } else if (o instanceof String){
            if (o.equals("Hello PrintActor")){
                getSender().tell("Hello",getSelf());
            }
        }else {
            unhandled(o);
        }
    }

}