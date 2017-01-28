# KRedis
KRedis is simple Redis kotlin client.

Request-Response part is implemented now.

KRedis is EASY to use.

# How do I use it?
```
fun main(args: Array<String>) {
    val rs = RedisClient("localhost", 6379)
    rs.connect()
    val cmd1 = cmdPing()
    println("Command Ping reply -> ${rs.execute { cmd1 }}")
    println("Command Echo reply -> ${rs.execute { cmdEcho("TEST MSG") }}")
    val cmdList = listOf<Command>(cmdPing(), cmdEcho("TEST MSG 2"))
    val rplList = cmdList.map { rs.execute { it } }
    println("Command List replies -> $rplList")
    
    val ccyList = listOf<String>("EUR", "USD", "JPY", "GBP", "RUB")
    val ccyReply = ccyList.map { cmdLPush("currency", it) }.map { rs.execute { it } }
    println("Command List replies -> $ccyReply")
    rs.close()
}
```
