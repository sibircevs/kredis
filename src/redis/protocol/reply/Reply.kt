package redis.protocol.reply

import java.nio.charset.Charset

enum class ReplyType {
    STATUS, ERROR, INT, BULK, MULTIBULK
}

/**
 * Reply class
 * 
 * User: sibircevs
 * Date: 04.01.2017
 * @param bytes
 * @param type
 */
open class Reply(val bytes: ByteArray, val type: ReplyType)

/**
 * StatusReply class
 * 
 * User: sibircevs
 * Date: 04.01.2017
 * @param bytes
 */
class StatusReply(bytes: ByteArray) : Reply(bytes, ReplyType.STATUS) {
    companion object {
        val MARKER: Char = '+'
    }
  
    constructor(data: String) : this(data.toByteArray(Charsets.UTF_8))
    
    fun asString(): String = String(bytes, Charsets.UTF_8)
    override fun toString(): String = "StatusReply(${asString()})"
}

/**
 * ErrorReply class
 * 
 * User: sibircevs
 * Date: 04.01.2017
 * @param bytes
 */
class ErrorReply(bytes: ByteArray) : Reply(bytes, ReplyType.ERROR) {
    companion object {
        val MARKER: Char = '-'
    }
  
    constructor(data: String) : this(data.toByteArray(Charsets.UTF_8))
    
    fun asString(): String = String(bytes, Charsets.UTF_8)
    override fun toString(): String = "ErrorReply(${asString()})"
}

/**
 * IntegerReply class
 * 
 * User: sibircevs
 * Date: 04.01.2017
 * @param bytes
 */
class IntegerReply(bytes: ByteArray) : Reply(bytes, ReplyType.INT) {
    companion object {
        val MARKER: Char = ':'
    }
    
    constructor(data: String) : this(data.toByteArray(Charsets.UTF_8))

    fun asString(): String = String(bytes, Charsets.UTF_8)
    @Throws(NumberFormatException::class)
    fun asLong(): Long = asString().toLong()
    @Throws(NumberFormatException::class)
    fun asInt(): Int = asString().toInt()
    override fun toString(): String = "IntegerReply(${asString()})"
}

/**
 * BulkReply class
 * 
 * User: sibircevs
 * Date: 04.01.2017
 * @param bytes
 */
open class BulkReply(bytes: ByteArray) : Reply(bytes, ReplyType.BULK) {
    companion object {
        val MARKER: Char = '$'
    }
  
    fun asByteArray(): ByteArray = bytes
    fun asAsciiString(): String = String(bytes, Charsets.US_ASCII)
    fun asUTF8String(): String = String(bytes, Charsets.UTF_8)
    fun asString(charset: Charset = Charsets.UTF_8): String = String(bytes, charset)
    override fun toString(): String = "BulkReply(${asUTF8String()})"
}

/**
 * NullBulkString class
 * 
 * User: sibircevs
 * Date: 04.01.2017
 */
class NullBulkString() : BulkReply(byteArrayOf()) {
    override fun toString(): String = "NullBulkString()"
}

/**
 * MultiBulkReply class
 * 
 * User: sibircevs
 * Date: 04.01.2017
 * @param replies
 */
class MultiBulkReply(replies: List<Reply>) : Reply(byteArrayOf(), ReplyType.MULTIBULK) {
    companion object {
        val MARKER: Char = '*'
    }
    private val _replies: List<Reply>
    
    init {
        _replies = replies
    }
    
    fun asReplyList(): List<Reply> = _replies
    @Throws(IllegalArgumentException::class)
    fun asStringList(charset: Charset = Charsets.UTF_8): List<String> {
        if (_replies.isEmpty()) return listOf<String>()
        
        val strings = mutableListOf<String>()
        for (reply in _replies) {
            when(reply) {
                is StatusReply -> strings.add(reply.asString())
                is IntegerReply -> strings.add(reply.asString())
                is BulkReply -> strings.add(reply.asString(charset))
                else -> IllegalArgumentException("Could not convert " + reply + " to a string")
            }
        }
        return strings
    }
    override fun toString(): String = "MultiBulkReply(replies count = ${_replies.size})"
}