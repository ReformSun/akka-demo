package com.sunny.test.rpc.massage

class HelloWord(val content:String) extends Serializable {
    def getContent:String = {
        return content
    }
}
