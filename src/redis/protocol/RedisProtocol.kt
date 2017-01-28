package redis.protocol

import redis.protocol.reply.Reply
import redis.protocol.reply.StatusReply
import redis.protocol.reply.ErrorReply
import redis.protocol.reply.IntegerReply
import redis.protocol.reply.BulkReply
import redis.protocol.reply.NullBulkString
import redis.protocol.reply.MultiBulkReply

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.EOFException
import java.io.IOException
import java.net.Socket

/**
 * RedisProtocol class
 * 
 * User: sibircevs
 * Date: 04.01.2017
 * @param bis
 * @param os
 */
class RedisProtocol(private val bis: BufferedInputStream, private val os: OutputStream) {
    companion object {
        val CR = 13.toByte()
        val LF = 10.toByte()
        val NEWLINE = byteArrayOf(13, 10)
    }

    /**
     * Create a new RedisProtocol from a socket connection.
     *
     * @param socket
     * @throws IOException
     */
    @Throws(IOException::class)
    constructor(socket: Socket) : this(BufferedInputStream(socket.getInputStream()), BufferedOutputStream(socket.getOutputStream()))
    
    /**
     * Read a Reply from an input stream.
     *
     * @return
     * @throws IOException
     */
    @Throws(IOException::class, EOFException::class)
    fun receive(): Reply {
        val code: Int = bis.read()
        
        if (code == -1) {
            throw EOFException()
        }
        when (code) {
            StatusReply.MARKER.toInt() -> {
                return StatusReply(readSimpleReply())
            }
            ErrorReply.MARKER.toInt() -> {
                return ErrorReply(readSimpleReply())
            }
            IntegerReply.MARKER.toInt() -> {
                return IntegerReply(readSimpleReply())
            }
            BulkReply.MARKER.toInt() -> {
                val (size, bytes) = readBulkReply()
                if (size == -1)
                    return NullBulkString()
                else
                    return BulkReply(bytes)
            }
            MultiBulkReply.MARKER.toInt() -> {
                val size = String(readSimpleReply()).toInt()
                val replies: List<Reply> = (1..size).map { receive() }.toList()
                return MultiBulkReply(replies)
            }
            else -> throw IOException("Unexpected character in stream: " + code)
        }
    }
    
    fun send(msg: ByteArray): Unit {
        os.write(msg)
    }
    
    @Throws(IOException::class)
    private fun readSimpleReply(): ByteArray =
        ByteArrayOutputStream().use { baos ->
            for (b: Byte in bis) {
                if (b == CR) {
                    val lf = bis.iterator().next() // Remove byte LF from stream
                    if (lf == LF)
                        break
                    else
                        throw IOException("String that cannot contain a CR or LF character (no newlines are allowed).")
                } else {
                    baos.write(b.toInt())
                }
            }
            baos.toByteArray()
        }
    
    @Throws(IOException::class, NumberFormatException::class, IllegalArgumentException::class)
    private fun readBulkReply(): Pair<Int, ByteArray> {
        val size = String(readSimpleReply()).toInt()

        if (size > Integer.MAX_VALUE - 8) {
            throw IllegalArgumentException("Supports arrays up to ${Integer.MAX_VALUE -8 } in size")
        }
        if (size == -1)
            return Pair(-1, byteArrayOf())
        if (size < 0)
            throw IllegalArgumentException("Invalid size: " + size)

        val bytes = ByteArrayOutputStream().use { baos ->
            var total = 0
            if (size > 0) // For correct "$0\r\n\r\n" processing
                for (b: Byte in bis) {
                    baos.write(b.toInt())
                    total += 1
                    if (total == size) break
                }
            baos.toByteArray()
        }
        
        val cr: Int = bis.read()
        val lf: Int = bis.read()
        if (bytes.size != size) {
            throw IOException("Wrong size $size. Bytes have been read: ${bytes.size}")
        }        
        if (cr != CR.toInt() || lf != LF.toInt()) {
            throw IOException("Improper line ending: $cr, $lf")
        }
        return Pair(size, bytes)
    }
    
    /**
     * Wait for a reply on the input stream.
     *
     * @return
     * @throws IOException
     */
    @Throws(IOException::class, EOFException::class)
    fun receiveAsync(): Reply {
        synchronized (bis) {
            return receive()
        }
    }

    /**
     * Send a command over the wire and do not wait for a reponse.
     *
     * @param msg
     * @throws IOException
     */
    @Throws(IOException::class)
    fun sendAsync(msg: ByteArray) {
        synchronized (os) {
            send(msg)
        }
        os.flush()
    }

    /**
     * Close the input and output streams.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun close() {
        bis.close()
        os.close()
    }
}

fun main(args: Array<String>) {
    println("Hello!")
    println('$'.toInt())
    
    val isTst1: BufferedInputStream = BufferedInputStream(java.io.ByteArrayInputStream("+OK\r\n".toByteArray()))
    val osTst1: OutputStream = BufferedOutputStream(java.io.ByteArrayOutputStream())
    val redisTst1 = RedisProtocol(isTst1, osTst1)
    println("Test 1 [OK]: ${redisTst1.receive()}")
    redisTst1.close()
    
    val isTst2: BufferedInputStream = BufferedInputStream(java.io.ByteArrayInputStream("-ERR unknown command 'foobar'\r\n".toByteArray()))
    val osTst2: OutputStream = BufferedOutputStream(java.io.ByteArrayOutputStream())
    val redisTst2 = RedisProtocol(isTst2, osTst2)
    println("Test 2 [ERR unknown command 'foobar']: ${redisTst2.receive()}")
    redisTst2.close()
    
    val isTst3: BufferedInputStream = BufferedInputStream(java.io.ByteArrayInputStream(":1000\r\n".toByteArray()))
    val osTst3: OutputStream = BufferedOutputStream(java.io.ByteArrayOutputStream())
    val redisTst3 = RedisProtocol(isTst3, osTst3)
    println("Test 3 [1000]: ${redisTst3.receive()}")
    redisTst3.close()
    
    val isTst4_1: BufferedInputStream = BufferedInputStream(java.io.ByteArrayInputStream("$6\r\nfoobar\r\n".toByteArray()))
    val osTst4_1: OutputStream = BufferedOutputStream(java.io.ByteArrayOutputStream())
    val redisTst4_1 = RedisProtocol(isTst4_1, osTst4_1)
    println("Test 4_1 [foobar]: ${redisTst4_1.receive()}")
    redisTst4_1.close()
    
    val isTst4_2: BufferedInputStream = BufferedInputStream(java.io.ByteArrayInputStream("$0\r\n\r\n".toByteArray()))
    val osTst4_2: OutputStream = BufferedOutputStream(java.io.ByteArrayOutputStream())
    val redisTst4_2 = RedisProtocol(isTst4_2, osTst4_2)
    println("Test 4_2 [foobar]: ${redisTst4_2.receive()}")
    redisTst4_2.close()
    
    val isTst4_3: BufferedInputStream = BufferedInputStream(java.io.ByteArrayInputStream("$-1\r\n".toByteArray()))
    val osTst4_3: OutputStream = BufferedOutputStream(java.io.ByteArrayOutputStream())
    val redisTst4_3 = RedisProtocol(isTst4_3, osTst4_3)
    println("Test 4_3 [foobar]: ${redisTst4_3.receive()}")
    redisTst4_3.close()
    
    val isTst5: BufferedInputStream = BufferedInputStream(java.io.ByteArrayInputStream("*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n".toByteArray()))
    val osTst5: OutputStream = BufferedOutputStream(java.io.ByteArrayOutputStream())
    val redisTst5 = RedisProtocol(isTst5, osTst5)
    val mbr5 = redisTst5.receive()
    when (mbr5) {
        is MultiBulkReply -> mbr5.asReplyList().forEach{ println("Test 5 $it") }
        else -> println("Test 5 Unknown Reply")
    }
    redisTst5.close()
}