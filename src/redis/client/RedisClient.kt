package redis.client

import redis.protocol.RedisProtocol
import redis.protocol.reply.*

import java.net.Socket
import java.io.IOException

/**
 * RedisClient class
 * 
 * User: sibircevs
 * Date: 04.01.2017
 * @param host
 * @param port
 * @param db
 * @param passwd
 */
class RedisClient(val host: String, val port: Int = 6379, val db: Int = 0, val passwd: String = "") {
    lateinit var redisProtocol: RedisProtocol

    @Throws(RedisException::class)
    fun connect(): Boolean {
        try {
            redisProtocol = RedisProtocol(Socket(host, port))
            if (passwd != "")
                execute { cmdAuth(passwd) } // RedisException will be thrown if the ErrorReply occurs
            if (db != 0)
                execute { cmdSelect(db) }
            return true
        } catch (e: IOException) {
            throw RedisException("Could not connect", e)
        } finally {
        }
    }
    
    @Throws(RedisException::class)
    fun execute(block: () -> Command): Reply {
        val command = block()
        val executeReply: Reply = try {
            redisProtocol.sendAsync(command.cmd)
            val reply = redisProtocol.receiveAsync()
            when (reply) {
                is ErrorReply -> throw RedisException(reply.asString())
                else -> reply
            }
        } catch (e: IOException) {
            throw RedisException("I/O Failure: ${command.name}", e)
        }
        return executeReply
    }
    
    @Throws(RedisException::class)
    fun close() {
        try {
            redisProtocol.close()
        } catch (e: IOException) {
            throw RedisException("Could not close connect", e)
        }
    }
}

fun main(args: Array<String>) {
    println("Hello! RedisClient TEST.")
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