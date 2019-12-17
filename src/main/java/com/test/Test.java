package com.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class Test {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Callable<Chuju> chujuCallable = new Callable<Chuju>() {
            public Chuju call() throws Exception {
                System.out.println("下单");
                System.out.println("等待送货");
                Thread.sleep(5000);
                System.out.println("快递送达");
                return new Chuju();
            }
        };


        FutureTask<Chuju> futureTask = new FutureTask<Chuju>(chujuCallable);
        new Thread(futureTask).start();

        System.out.println("去超市买食材");
        Thread.sleep(3000);

        System.out.println("购买食材成功");

        if (!futureTask.isDone()){
            System.out.println("餐具还未到货，等待");
        }

        Chuju chuju = futureTask.get();

        System.out.println("餐具到货开始烹饪");



    }
}

class Chuju {

}

class Shicai {

}
