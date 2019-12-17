## 一 Callable和Runnable接口的区别

共同点：两者都是用来编写多线程的接口

不同点：
   1. 实现Callable接口的任务线程能返回执行结果；而实现Runnable接口的任务线程不能返回结果；
   2. Callable接口的call()方法允许抛出异常；而Runnable接口的run()方法的异常只能在内部消化，不能继续上抛；
   
注意点：

Callable接口支持返回执行结果，此时需要调用FutureTask.get()方法实现，此方法会阻塞主线程直到获取‘将来’结果；当不调用此方法时，主线程不会阻塞！
      