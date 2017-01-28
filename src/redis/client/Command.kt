package redis.client

import redis.protocol.RedisProtocol
import java.io.ByteArrayOutputStream

// https://redis.io/topics/data-types-intro
// !!! Redis keys are binary safe, this means that you can use any binary sequence as a key

enum class ClientReplyMode {
    ON, OFF, SKIP
}

enum class InsertType {
    BEFORE, AFTER
}

enum class SetOptions {
    NONE, NX, XX
}

enum class ScriptDebugMode {
    YES, SYNC, NO
}

enum class ShutdownOptions {
    NOSAVE, SAVE
}

private fun ByteArrayOutputStream.writeAsBulkString(bytes: ByteArray) {
    val size: Int = bytes.size
    val strSize: String = size.toString()
    this.write('$'.toInt())
    this.write(strSize.toByteArray(), 0, strSize.length)
    this.write(RedisProtocol.NEWLINE, 0, 2)
    this.write(bytes, 0, bytes.size)
    this.write(RedisProtocol.NEWLINE, 0, 2)
}

private fun ByteArrayOutputStream.writeAsBulkString(value: Int) {
    this.writeAsBulkString(value.toString().toByteArray(Charsets.UTF_8))
}

private fun ByteArrayOutputStream.writeAsBulkString(value: Long) {
    this.writeAsBulkString(value.toString().toByteArray(Charsets.UTF_8))
}

private fun ByteArrayOutputStream.writeAsBulkString(value: Float) {
    this.writeAsBulkString(value.toString().toByteArray(Charsets.UTF_8))
}

private fun ByteArrayOutputStream.writeAsBulkString(vararg values: String) {
    for (value in values) {
        this.writeAsBulkString(value.toByteArray(Charsets.UTF_8))
    }
}

private fun ByteArrayOutputStream.writeAsArrayStart(arraySize: Int) {
    val sArraySize = arraySize.toString()
    this.write('*'.toInt())
    this.write(sArraySize.toByteArray(), 0, sArraySize.length)
    this.write(RedisProtocol.NEWLINE, 0, 2)
}

private fun singleCommand(cmdName: String): Command =
    Command(cmdName, 
        ByteArrayOutputStream().use { baos ->
            val size = 1 // komanda
            baos.writeAsArrayStart(size)
            baos.writeAsBulkString(cmdName)
            baos.toByteArray()
        }
    )
    
private fun varargParamCommand(cmdName: String, vararg param: ByteArray): Command =
    Command(cmdName, 
        ByteArrayOutputStream().use { baos ->
            val size = 1 + param.size // komanda + param.size
            baos.writeAsArrayStart(size)
            baos.writeAsBulkString(cmdName)
            for (p in param) {
                baos.writeAsBulkString(p)
            }
            baos.toByteArray()
        }
    )
    
private fun varargParamCommand(cmdName: String, vararg param: String): Command =
    Command(cmdName, 
        ByteArrayOutputStream().use { baos ->
            val size = 1 + param.size // komanda + param.size
            baos.writeAsArrayStart(size)
            baos.writeAsBulkString(cmdName)
            baos.writeAsBulkString(*param)
            baos.toByteArray()
        }
    )

private fun keyAndVarargParamCommand(cmdName: String, key: ByteArray, vararg param: String): Command =
    Command(cmdName, 
        ByteArrayOutputStream().use { baos ->
            val size = 2 + param.size // komanda + key + param1.size
            baos.writeAsArrayStart(size)
            baos.writeAsBulkString(cmdName)
            baos.writeAsBulkString(key)
            baos.writeAsBulkString(*param)
            baos.toByteArray()
        }
    )
    
private fun keyAndVarargParamCommand(cmdName: String, key: ByteArray, vararg param: ByteArray): Command =
    Command(cmdName, 
        ByteArrayOutputStream().use { baos ->
            val size = 2 + param.size // komanda + key + param1.size
            baos.writeAsArrayStart(size)
            baos.writeAsBulkString(cmdName)
            baos.writeAsBulkString(key)
            for (p in param) {
                baos.writeAsBulkString(p)
            }
            baos.toByteArray()
        }
    )

/**
 * APPEND key value
 * Available since 2.0.0.
 * Return value
 *  Integer reply: the length of the string after the append operation.
 */
public fun cmdAppend(key: String, value: String): Command = cmdAppend(key.toByteArray(Charsets.UTF_8), value.toByteArray(Charsets.UTF_8))
public fun cmdAppend(key: ByteArray, value: ByteArray): Command = varargParamCommand(Command.APPEND, key, value)

/**
 * AUTH password
 * Available since 1.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdAuth(password0: String): Command = varargParamCommand(Command.AUTH, password0.toByteArray(Charsets.UTF_8))

/**
 * BGREWRITEAOF
 * Available since 1.0.0.
 * Return value
 *  Simple string reply: always OK.
 */
public fun cmdBGReWriteAOf(): Command = singleCommand(Command.BGREWRITEAOF)

/**
 * BGSAVE
 * Available since 1.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdBGSave(): Command = singleCommand(Command.BGSAVE)

/**
 * BITCOUNT key [start end] 
 * Available since 2.6.0.
 * ! Is not implemented
 */

/**
 * BITFIELD key [GET type offset] [SET type offset value] [INCRBY type offset increment] [OVERFLOW WRAP|SAT|FAIL] 
 * Available since 3.2.0.
 * ! Is not implemented
 */

/**
 * BITOP operation destkey key [key ...] 
 * Available since 2.6.0.
 * ! Is not implemented
 */

/**
 * BITPOS key bit [start] [end] 
 * Available since 2.8.7.
 * ! Is not implemented
 */

/**
 * BLPOP key [key ...] timeout 
 * Available since 2.0.0.
 * Return value
 *  Array reply: specifically:
 *   A nil multi-bulk when no element could be popped and the timeout expired.
 *   A two-element multi-bulk with the first element being the name of the key where an element was popped and the second element being the value of the popped element.
 */
public fun cmdBLPop(key: String, timeout: Long): Command = cmdBLPop(key.toByteArray(Charsets.UTF_8), timeout)
public fun cmdBLPop(key: ByteArray, timeout: Long): Command = varargParamCommand(Command.BLPOP, key, timeout.toString().toByteArray(Charsets.UTF_8))

/**
 * BRPOP key [key ...] timeout 
 * Available since 2.0.0.
 * Return value
 *  Array reply: specifically:
 *   A nil multi-bulk when no element could be popped and the timeout expired.
 *   A two-element multi-bulk with the first element being the name of the key where an element was popped and the second element being the value of the popped element.
 */
public fun cmdBRPop(key: String, timeout: Long): Command = cmdBRPop(key.toByteArray(Charsets.UTF_8), timeout)
public fun cmdBRPop(key: ByteArray, timeout: Long): Command = varargParamCommand(Command.BRPOP, key, timeout.toString().toByteArray(Charsets.UTF_8))

/**
 * BRPOPLPUSH source destination timeout 
 * Available since 2.2.0.
 * Return value
 *  Bulk string reply: the element being popped from source and pushed to destination. If timeout is reached, a Null reply is returned.
 */
public fun cmdBRPopLPush(source: String, destination: String, timeout: Long): Command = cmdBRPopLPush(source.toByteArray(Charsets.UTF_8), destination.toByteArray(Charsets.UTF_8), timeout)
public fun cmdBRPopLPush(source: ByteArray, destination: ByteArray, timeout: Long): Command = varargParamCommand(Command.BRPOPLPUSH, source, destination, timeout.toString().toByteArray(Charsets.UTF_8))

/**
 * CLIENT GETNAME
 * Available since 2.6.9.
 * Return value
 *  Bulk string reply: The connection name, or a null bulk reply if no name is set.
 */
public fun cmdClientGetName(): Command = singleCommand(Command.CLIENT_GETNAME)

/**
 * CLIENT KILL [ip:port] [ID client-id] [TYPE normal|master|slave|pubsub] [ADDR ip:port] [SKIPME yes/no] 
 * Available since 2.4.0.
 * ! Is not implemented
 */

/**
 * CLIENT LIST
 * Available since 2.4.0.
 * Return value
 *  Bulk string reply: a unique string, formatted as follows:
 *   One client connection per line (separated by LF)
 *   Each line is composed of a succession of property=value fields separated by a space character.
 */
public fun cmdClientList(): Command = singleCommand(Command.CLIENT_LIST)

/**
 * CLIENT PAUSE timeout 
 * Available since 2.9.50.
 * Return value
 *  Simple string reply: The command returns OK or an error if the timeout is invalid.
 */
public fun cmdClientPause(timeout: Long): Command = varargParamCommand(Command.CLIENT_PAUSE, timeout.toString().toByteArray(Charsets.UTF_8))

/** 
 * CLIENT REPLY ON|OFF|SKIP 
 * Available since 3.2.
 * Return value
 *  When called with either OFF or SKIP subcommands, no reply is made. When called with ON:
 *  Simple string reply: OK.
 */
public fun cmdClientReply(mode: ClientReplyMode): Command = varargParamCommand(Command.CLIENT_REPLY,
    when (mode) {
        ClientReplyMode.ON -> "ON".toByteArray(Charsets.UTF_8)
        ClientReplyMode.OFF -> "OFF".toByteArray(Charsets.UTF_8)
        ClientReplyMode.SKIP -> "SKIP".toByteArray(Charsets.UTF_8)
    })

/**
 * CLIENT SETNAME connection-name
 * Available since 2.6.9.
 * Return value
 *  Simple string reply: OK if the connection name was successfully set.
 */
public fun cmdClientSetName(name: String): Command = varargParamCommand(Command.CLIENT_SETNAME, name.toByteArray(Charsets.UTF_8))

/**
 * CLUSTER ADDSLOTS slot [slot ...] 
 * Available since 3.0.0.
 * ! Is not implemented
 */

/**
 * CLUSTER COUNT-FAILURE-REPORTS node-id 
 * Available since 3.0.0.
 * ! Is not implemented
 */

/**
 * CLUSTER COUNTKEYSINSLOT slot 
 * Available since 3.0.0.
 * ! Is not implemented
 */

/**
 * CLUSTER DELSLOTS slot [slot ...] 
 * Available since 3.0.0.
 * ! Is not implemented
 */

/**
 * CLUSTER FAILOVER [FORCE|TAKEOVER] 
 * Available since 3.0.0.
 * ! Is not implemented
 */

/**
 * CLUSTER FORGET node-id 
 * Available since 3.0.0.
 * ! Is not implemented
 */

/**
 * CLUSTER GETKEYSINSLOT slot count 
 * Available since 3.0.0.
 * ! Is not implemented
 */

/**
 * CLUSTER INFO
 * Available since 3.0.0.
 * Return value
 *  Bulk string reply: A map between named fields and values in the form of <field>:<value> lines separated by newlines composed by the two bytes CRLF.
 */
public fun cmdClusterInfo(): Command = singleCommand(Command.CLUSTER_INFO)

/**
 * CLUSTER KEYSLOT key
 * Available since 3.0.0.
 * ! Is not implemented
 */

/**
 * CLUSTER MEET ip port
 * Available since 3.0.0.
 * ! Is not implemented
 */

/**
 * CLUSTER NODES
 * Available since 3.0.0.
 * Return value
 *  Bulk string reply: The serialized cluster configuration.
 */
public fun cmdClusterNodes(): Command = singleCommand(Command.CLUSTER_NODES)

/**
 * CLUSTER REPLICATE node-id 
 * Available since 3.0.0.
 * ! Is not implemented
 */

/**
 * CLUSTER RESET [HARD|SOFT] 
 * Available since 3.0.0.
 * ! Is not implemented
 */

/**
 * CLUSTER SAVECONFIG
 * Available since 3.0.0.
 * Return value
 *  Simple string reply: OK or an error if the operation fails.
 */
public fun cmdClusterSaveConfig(): Command = singleCommand(Command.CLUSTER_SAVECONFIG)

/**
 * CLUSTER SET-CONFIG-EPOCH config-epoch 
 * Available since 3.0.0.
 * ! Is not implemented
 */

/**
 * CLUSTER SETSLOT slot IMPORTING|MIGRATING|STABLE|NODE [node-id] 
 * Available since 3.0.0.
 * ! Is not implemented
 */

/**
 * CLUSTER SLAVES node-id 
 * Available since 3.0.0.
 * ! Is not implemented
 */

/**
 * CLUSTER SLOTS
 * Available since 3.0.0.
 * Return value
 *  Array reply: nested list of slot ranges with IP/Port mappings.
 */
public fun cmdClusterSlots(): Command = singleCommand(Command.CLUSTER_SLOTS)

/**
 * COMMAND
 * Available since 2.8.13.
 * ! Is not implemented
 */

/**
 * COMMAND COUNT
 * Available since 2.8.13.
 * Return value
 *  Integer reply: number of commands returned by COMMAND
 */
public fun cmdCommandCount(): Command = singleCommand(Command.COMMAND_COUNT)

/**
 * COMMAND GETKEYS
 * Available since 2.8.13.
 * ! Is not implemented
 */

/**
 * COMMAND INFO command-name [command-name ...] 
 * Available since 2.8.13.
 * ! Is not implemented
 */

/**
 * CONFIG GET parameter 
 * Available since 2.0.0.
 * ! Is not implemented
 */

/**
 * CONFIG RESETSTAT
 * Available since 2.0.0.
 * Return value
 *  Simple string reply: always OK.
 */
public fun cmdConfigResetStat(): Command = singleCommand(Command.CONFIG_RESETSTAT)

/**
 * CONFIG REWRITE
 * Available since 2.8.0.
 * Return value
 *  Simple string reply: OK when the configuration was rewritten properly. Otherwise an error is returned.
 */
public fun cmdConfigReWrite(): Command = singleCommand(Command.CONFIG_REWRITE)

/**
 * CONFIG SET parameter value 
 * Available since 2.0.0.
 * ! Is not implemented
 */
 
/**
 * DBSIZE
 * Available since 1.0.0.
 * Return value
 *  Integer reply
 */
public fun cmdDBSize(): Command = singleCommand(Command.DBSIZE)

/**
 * DEBUG OBJECT key 
 * Available since 1.0.0.
 * DEBUG OBJECT is a debugging command that should not be used by clients. 
 * ! Is not implemented
 */

/**
 * DEBUG SEGFAULT
 * Available since 1.0.0.
 * ! Is not implemented
 */

/**
 * DECR key 
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the value of key after the decrement
 */
public fun cmdDecr(key: String): Command = cmdIncr(key.toByteArray(Charsets.UTF_8))
public fun cmdDecr(key: ByteArray): Command = varargParamCommand(Command.DECR, key)

/**
 * DECRBY key decrement 
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the value of key after the decrement
 */
public fun cmdDecrBy(key: String, decrement: Int): Command = cmdDecrBy(key.toByteArray(Charsets.UTF_8), decrement )
public fun cmdDecrBy(key: ByteArray, decrement: Int): Command = varargParamCommand(Command.DECRBY, key, decrement.toString().toByteArray(Charsets.UTF_8))

/**
 * DEL key [key ...]
 * Available since 1.0.0.
 * Return value:
 *  Integer reply: The number of keys that were removed.
 */
public fun cmdDel(vararg key: String): Command = varargParamCommand(Command.DEL, *key)
public fun cmdDel(vararg key: ByteArray): Command = varargParamCommand(Command.DEL, *key)

/**
 * DISCARD
 * Available since 2.0.0.
 * Return value
 *  Simple string reply: always OK.
 */
public fun cmdDiscard(): Command = singleCommand(Command.DISCARD)

/**
 * DUMP key 
 * Available since 2.6.0.
 * Return value
 *  Bulk string reply: the serialized value.
 */
public fun cmdDump(key: String): Command = cmdDump(key.toByteArray(Charsets.UTF_8))
public fun cmdDump(key: ByteArray): Command = varargParamCommand(Command.DUMP, key)

/**
 * ECHO message
 * Available since 1.0.0.
 * Return value
 *  Bulk string reply
 */
public fun cmdEcho(msg: String): Command = varargParamCommand(Command.ECHO, msg.toByteArray(Charsets.UTF_8))

/**
 * EVAL script numkeys key [key ...] arg [arg ...] 
 * Available since 2.6.0.
 * ! Is not implemented
 */

/**
 * EVALSHA sha1 numkeys key [key ...] arg [arg ...] 
 * Available since 2.6.0.
 * ! Is not implemented
 */

/**
 * EXEC
 * Available since 1.2.0.
 * Return value
 *  Array reply: each element being the reply to each of the commands in the atomic transaction.
 *  When using WATCH, EXEC can return a Null reply if the execution was aborted.
 */
public fun cmdExec(): Command = singleCommand(Command.EXEC)

/**
 * EXISTS key [key ...]
 * Available since 1.0.0.
 * Return value
 *  Integer reply, specifically:
 *  1 if the key exists.
 *  0 if the key does not exist.
 */
public fun cmdExists(vararg key: String): Command = varargParamCommand(Command.EXISTS, *key)
public fun cmdExists(vararg key: ByteArray): Command = varargParamCommand(Command.EXISTS, *key)

/**
 * EXPIRE key seconds 
 * Available since 1.0.0.
 * ! Is not implemented
 */
 
/**
 * EXPIREAT key timestamp 
 * Available since 1.2.0.
 * ! Is not implemented
 */

/**
 * FLUSHALL [ASYNC] 
 * Available since 1.0.0.
 * ! Is not implemented
 */

/**
 * FLUSHDB [ASYNC] 
 * Available since 1.0.0.
 * ! Is not implemented
 */

/**
 * GEOADD key longitude latitude member [longitude latitude member ...] 
 * Available since 3.2.0.
 * ! Is not implemented
 */

/**
 * GEODIST key member1 member2 [unit] 
 * Available since 3.2.0.
 * ! Is not implemented
 */

/**
 * GEOHASH key member [member ...] 
 * Available since 3.2.0.
 * ! Is not implemented
 */

/**
 * GEOPOS key member [member ...] 
 * Available since 3.2.0.
 * ! Is not implemented
 */

/**
 * GEORADIUS key longitude latitude radius m|km|ft|mi [WITHCOORD] [WITHDIST] [WITHHASH] [COUNT count] [ASC|DESC] [STORE key] [STOREDIST key] 
 * Available since 3.2.0.
 * ! Is not implemented
 */

/**
 * GEORADIUSBYMEMBER key member radius m|km|ft|mi [WITHCOORD] [WITHDIST] [WITHHASH] [COUNT count] [ASC|DESC] [STORE key] [STOREDIST key] 
 * Available since 3.2.0.
 * ! Is not implemented
 */

/**
 * GET key 
 * Available since 1.0.0.
 * Return value
 *  Bulk string reply: the value of key, or nil when key does not exist.
 */
public fun cmdGet(key: String): Command = cmdGet(key.toByteArray(Charsets.UTF_8))
public fun cmdGet(key: ByteArray): Command = varargParamCommand(Command.GET, key)
 
/**
 * GETBIT key offset 
 * Available since 2.2.0.
 * ! Is not implemented
 */

/** 
 * GETRANGE key start end 
 * Available since 2.4.0.
 * Return value
 *  Bulk string reply
 */
public fun cmdGetRange(key: String, start: Int, end: Int): Command = cmdGetRange(key.toByteArray(Charsets.UTF_8), start, end)
public fun cmdGetRange(key: ByteArray, start: Int, end: Int): Command = varargParamCommand(Command.GETRANGE, key, start.toString().toByteArray(Charsets.UTF_8), end.toString().toByteArray(Charsets.UTF_8))

/**
 * GETSET key value 
 * Available since 1.0.0.
 * Return value
 *  Bulk string reply: the old value stored at key, or nil when key did not exist.
 */
public fun cmdGetSet(key: String, value: String): Command = cmdGetSet(key.toByteArray(Charsets.UTF_8), value.toByteArray(Charsets.UTF_8))
public fun cmdGetSet(key: ByteArray, value: ByteArray): Command = varargParamCommand(Command.GETSET, key, value)

/**
 * HDEL key field [field ...] 
 * Available since 2.0.0.
 * Return value
 * Integer reply: the number of fields that were removed from the hash, not including specified but non existing fields.
 */
public fun cmdHDel(key: String, vararg field: ByteArray): Command = cmdHDel(key.toByteArray(Charsets.UTF_8), *field)
public fun cmdHDel(key: String, vararg field: String): Command = cmdHDel(key.toByteArray(Charsets.UTF_8), *field)
public fun cmdHDel(key: ByteArray, vararg field: ByteArray): Command = keyAndVarargParamCommand(Command.HDEL, key, *field)
public fun cmdHDel(key: ByteArray, vararg field: String): Command = keyAndVarargParamCommand(Command.HDEL, key, *field)

/**
 * HEXISTS key field 
 * Available since 2.0.0.
 * Return value
 *  Integer reply, specifically:
 *   1 if the hash contains field.
 *   0 if the hash does not contain field, or key does not exist.
 */
public fun cmdHExists(key: String, field: String): Command = cmdHExists(key.toByteArray(Charsets.UTF_8), field.toByteArray(Charsets.UTF_8))
public fun cmdHExists(key: ByteArray, field: ByteArray): Command = varargParamCommand(Command.HEXISTS, key, field)

/**
 * HGET key field 
 * Available since 2.0.0.
 * Return value
 *  Bulk string reply: the value associated with field, or nil when field is not present in the hash or key does not exist.
 */
public fun cmdHGet(key: String, field: String): Command = cmdHGet(key.toByteArray(Charsets.UTF_8), field.toByteArray(Charsets.UTF_8))
public fun cmdHGet(key: ByteArray, field: ByteArray): Command = varargParamCommand(Command.HGET, key, field)

/**
 * HGETALL key 
 * Available since 2.0.0.
 * Return value
 *  Array reply: list of fields and their values stored in the hash, or an empty list when key does not exist.
 */
public fun cmdHGetAll(key: String): Command = cmdHGetAll(key.toByteArray(Charsets.UTF_8))
public fun cmdHGetAll(key: ByteArray): Command = varargParamCommand(Command.HGETALL, key)

/**
 * HINCRBY key field increment 
 * Available since 2.0.0.
 * Return value
 *  Integer reply: the value at field after the increment operation.
 */
public fun cmdHIncrBy(key: String, field: String, increment: Int): Command = cmdHIncrBy(key.toByteArray(Charsets.UTF_8), field.toByteArray(Charsets.UTF_8), increment)
public fun cmdHIncrBy(key: ByteArray, field: ByteArray, increment: Int): Command = varargParamCommand(Command.HINCRBY, key, field, increment.toString().toByteArray(Charsets.UTF_8))

/**
 * HINCRBYFLOAT key field increment 
 * Available since 2.6.0.
 * Return value
 *  Bulk string reply: the value of field after the increment.
 */
public fun cmdHIncrByFloat(key: String, field: String, increment: Float): Command = cmdHIncrByFloat(key.toByteArray(Charsets.UTF_8), field.toByteArray(Charsets.UTF_8), increment)
public fun cmdHIncrByFloat(key: ByteArray, field: ByteArray, increment: Float): Command = varargParamCommand(Command.HINCRBYFLOAT, key, field, increment.toString().toByteArray(Charsets.UTF_8))

/**
 * HKEYS key 
 * Available since 2.0.0.
 * Return value
 *  Array reply: list of fields in the hash, or an empty list when key does not exist.
 */
public fun cmdHKeys(key: String): Command = cmdHKeys(key.toByteArray(Charsets.UTF_8))
public fun cmdHKeys(key: ByteArray): Command = varargParamCommand(Command.HKEYS, key)

/**
 * HLEN key 
 * Available since 2.0.0.
 * Return value
 *  Integer reply: number of fields in the hash, or 0 when key does not exist.
 */
public fun cmdHLen(key: String): Command = cmdHLen(key.toByteArray(Charsets.UTF_8))
public fun cmdHLen(key: ByteArray): Command = varargParamCommand(Command.HLEN, key)

/**
 * HMGET key field [field ...] 
 * Available since 2.0.0.
 * Return value
 *  Array reply: list of values associated with the given fields, in the same order as they are requested.
 */
public fun cmdHMGet(key: String, vararg field: ByteArray): Command = cmdHMGet(key.toByteArray(Charsets.UTF_8), *field)
public fun cmdHMGet(key: String, vararg field: String): Command = cmdHMGet(key.toByteArray(Charsets.UTF_8), *field)
public fun cmdHMGet(key: ByteArray, vararg field: ByteArray): Command = keyAndVarargParamCommand(Command.HMGET, key, *field)
public fun cmdHMGet(key: ByteArray, vararg field: String): Command = keyAndVarargParamCommand(Command.HMGET, key, *field)

/**
 * HMSET key field value [field value ...] 
 * Available since 2.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdHMSet(key: String, data: Map<String, String>): Command = cmdHMSet(key.toByteArray(Charsets.UTF_8), data)
public fun cmdHMSet(key: ByteArray, data: Map<String, String>): Command =
    Command(Command.HMSET,
        ByteArrayOutputStream().use { baos ->
            val size = 2 + data.size * 2 // komanda + key + (field + value) * data.size
            baos.writeAsArrayStart(size)
            baos.writeAsBulkString(Command.HMSET)
            baos.writeAsBulkString(key)
            for ((field, value) in data) {
                baos.writeAsBulkString(field)
                baos.writeAsBulkString(value)
            }
            baos.toByteArray()
        }
    )

/**
 * HSCAN key cursor [MATCH pattern] [COUNT count] 
 * Available since 2.8.0.
 * ! Is not implemented
 */

/**
 * HSET key field value 
 * Available since 2.0.0.
 * Return value
 *  Integer reply, specifically:
 *   1 if field is a new field in the hash and value was set.
 *   0 if field already exists in the hash and the value was updated.
 */
public fun cmdHSet(key: String, field: String, value: String): Command = cmdHSet(key.toByteArray(Charsets.UTF_8), field.toByteArray(Charsets.UTF_8), value.toByteArray(Charsets.UTF_8))
public fun cmdHSet(key: ByteArray, field: ByteArray, value: ByteArray): Command = varargParamCommand(Command.HSET, key, field, value)

/**
 * HSETNX key field value 
 * Available since 2.0.0.
 * Return value
 *  Integer reply, specifically:
 *   1 if field is a new field in the hash and value was set.
 *   0 if field already exists in the hash and no operation was performed.
 */
public fun cmdHSetNX(key: String, field: String, value: String): Command = cmdHSetNX(key.toByteArray(Charsets.UTF_8), field.toByteArray(Charsets.UTF_8), value.toByteArray(Charsets.UTF_8))
public fun cmdHSetNX(key: ByteArray, field: ByteArray, value: ByteArray): Command = varargParamCommand(Command.HSETNX, key, field, value)

/**
 * HSTRLEN key field 
 * Available since 3.2.0.
 * Return value
 *  Integer reply: the string length of the value associated with field, or zero when field is not present in the hash or key does not exist at all.
 */
public fun cmdHStrLen(key: String, field: String): Command = cmdHStrLen(key.toByteArray(Charsets.UTF_8), field.toByteArray(Charsets.UTF_8))
public fun cmdHStrLen(key: ByteArray, field: ByteArray): Command = varargParamCommand(Command.HSTRLEN, key, field)

/**
 * HVALS key 
 * Available since 2.0.0.
 * Return value
 *  Array reply: list of values in the hash, or an empty list when key does not exist.
 */
public fun cmdHVals(key: String): Command = cmdHVals(key.toByteArray(Charsets.UTF_8))
public fun cmdHVals(key: ByteArray): Command = varargParamCommand(Command.HVALS, key)

/**
 * INCR key 
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the value of key after the increment
 */
public fun cmdIncr(key: String): Command = cmdIncr(key.toByteArray(Charsets.UTF_8))
public fun cmdIncr(key: ByteArray): Command = varargParamCommand(Command.INCR, key)

/**
 * INCRBY key increment 
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the value of key after the increment
 */
public fun cmdIncrBy(key: String, increment: Int): Command = cmdIncrBy(key.toByteArray(Charsets.UTF_8), increment)
public fun cmdIncrBy(key: ByteArray, increment: Int): Command = varargParamCommand(Command.INCRBY, key, increment.toString().toByteArray(Charsets.UTF_8))

/**
 * INCRBYFLOAT key increment 
 * Available since 2.6.0.
 * Return value
 *  Bulk string reply: the value of key after the increment.
 */
public fun cmdIncrByFloat(key: String, increment: Float): Command = cmdIncrByFloat(key.toByteArray(Charsets.UTF_8), increment)
public fun cmdIncrByFloat(key: ByteArray, increment: Float): Command = varargParamCommand(Command.INCRBYFLOAT, key, increment.toString().toByteArray(Charsets.UTF_8))

/**
 * INFO [section] 
 * Available since 1.0.0.
 * Return value
 *  Bulk string reply: as a collection of text lines.
 *  Lines can contain a section name (starting with a # character) or a property. All the properties are in the form of field:value terminated by \r\n.
 */
public fun cmdInfo(section: String = ""): Command = if (section == "") singleCommand(Command.INFO) else varargParamCommand(Command.INFO, section.toByteArray(Charsets.UTF_8))

/**
 * KEYS pattern 
 * Available since 1.0.0.
 * Return value
 *  Array reply: list of keys matching pattern.
 */
public fun cmdKeys(pattern: String): Command = varargParamCommand(Command.KEYS, pattern.toByteArray(Charsets.UTF_8))

/**
 * LASTSAVE
 * Available since 1.0.0.
 * Return value
 *  Integer reply: an UNIX time stamp.
 */
public fun cmdLastSave(): Command = singleCommand(Command.LASTSAVE)

/**
 * LINDEX key index
 * Available since 1.0.0.
 * Return value
 *  Bulk string reply: the requested element, or nil when index is out of range.
 */
public fun cmdLIndex(key: String, index: Int): Command = cmdLIndex(key.toByteArray(Charsets.UTF_8), index)
public fun cmdLIndex(key: ByteArray, index: Int): Command = varargParamCommand(Command.LINDEX, key, index.toString().toByteArray(Charsets.UTF_8))

/**
 * LINSERT key BEFORE|AFTER pivot value
 * Available since 2.2.0.
 * Return value
 *  Integer reply: the length of the list after the insert operation, or -1 when the value pivot was not found.
 */
public fun cmdLInsert(key: String, insertType: InsertType, pivot: String, value: String): Command = cmdLInsert(key.toByteArray(Charsets.UTF_8), insertType, pivot.toByteArray(Charsets.UTF_8), value.toByteArray(Charsets.UTF_8))
public fun cmdLInsert(key: ByteArray, insertType: InsertType, pivot: ByteArray, value: ByteArray): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 5 // komanda + key + BEFORE|AFTER + pivot + value
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(Command.LINSERT)
        baos.writeAsBulkString(key)
        when (insertType) {
            InsertType.BEFORE -> baos.writeAsBulkString("BEFORE")
            InsertType.AFTER -> baos.writeAsBulkString("AFTER")
        }
        baos.writeAsBulkString(pivot)
        baos.writeAsBulkString(value)
        baos.toByteArray()
    }
    return Command(Command.LINSERT, cmd)
}

/**
 * LLEN key
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the length of the list at key.
 */
public fun cmdLLen(key: String): Command = cmdLLen(key.toByteArray(Charsets.UTF_8))
public fun cmdLLen(key: ByteArray): Command = varargParamCommand(Command.LLEN, key)

/**
 * LPOP key
 * Available since 1.0.0.
 * Return value
 *  Bulk string reply
 */
public fun cmdLPop(key: String): Command = cmdLPop(key.toByteArray(Charsets.UTF_8))
public fun cmdLPop(key: ByteArray): Command = varargParamCommand(Command.LPOP, key)

/**
 * LPUSH key value [value ...]
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the length of the list after the push operations.
 */
public fun cmdLPush(key: String, vararg value: ByteArray): Command = cmdLPush(key.toByteArray(Charsets.UTF_8), *value)
public fun cmdLPush(key: String, vararg value: String): Command = cmdLPush(key.toByteArray(Charsets.UTF_8), *value)
public fun cmdLPush(key: ByteArray, vararg value: ByteArray): Command = keyAndVarargParamCommand(Command.LPUSH, key, *value)
public fun cmdLPush(key: ByteArray, vararg value: String): Command = keyAndVarargParamCommand(Command.LPUSH, key, *value)

/**
 * LPUSHX key value
 * Available since 2.2.0.
 * Return value
 *  Integer reply: the length of the list after the push operation.
 */
public fun cmdLPushX(key: String, value: ByteArray): Command = cmdLPushX(key.toByteArray(Charsets.UTF_8), value)
public fun cmdLPushX(key: String, value: String): Command = cmdLPushX(key.toByteArray(Charsets.UTF_8), value.toByteArray(Charsets.UTF_8))
public fun cmdLPushX(key: ByteArray, value: String): Command = cmdLPushX(key, value.toByteArray(Charsets.UTF_8))
public fun cmdLPushX(key: ByteArray, value: ByteArray): Command = keyAndVarargParamCommand(Command.LPUSHX, key, value)

/**
 * LRANGE key start stop 
 * Available since 1.0.0.
 * Return value
 *  Array reply: list of elements in the specified range.
 */
public fun cmdLRange(key: String, start: Int, stop: Int): Command = cmdLRange(key.toByteArray(Charsets.UTF_8), start, stop)
public fun cmdLRange(key: ByteArray, start: Int, stop: Int): Command = varargParamCommand(Command.LRANGE, key, start.toString().toByteArray(Charsets.UTF_8), stop.toString().toByteArray(Charsets.UTF_8))

/**
 * LREM key count value
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the number of removed elements.
 */
public fun cmdLRem(key: String, count: Int, value: String): Command = cmdLRem(key.toByteArray(Charsets.UTF_8), count, value.toByteArray(Charsets.UTF_8))
public fun cmdLRem(key: ByteArray, count: Int, value: String): Command = cmdLRem(key, count, value.toByteArray(Charsets.UTF_8))
public fun cmdLRem(key: ByteArray, count: Int, value: ByteArray): Command = varargParamCommand(Command.LREM, key, count.toString().toByteArray(Charsets.UTF_8), value)

/**
 * LSET key index value 
 * Available since 1.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdLSet(key: String, index: Int, value: String): Command = cmdLSet(key.toByteArray(Charsets.UTF_8), index, value.toByteArray(Charsets.UTF_8))
public fun cmdLSet(key: ByteArray, index: Int, value: String): Command = cmdLSet(key, index, value.toByteArray(Charsets.UTF_8))
public fun cmdLSet(key: ByteArray, index: Int, value: ByteArray): Command = varargParamCommand(Command.LSET, key, index.toString().toByteArray(Charsets.UTF_8), value)

/**
 * LTRIM key start stop 
 * Available since 1.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdLTrim(key: String, start: Int, stop: Int): Command = cmdLTrim(key.toByteArray(Charsets.UTF_8), start, stop)
public fun cmdLTrim(key: ByteArray, start: Int, stop: Int): Command = varargParamCommand(Command.LTRIM, key, start.toString().toByteArray(Charsets.UTF_8), stop.toString().toByteArray(Charsets.UTF_8))

/**
 * MGET key [key ...] 
 * Available since 1.0.0.
 * Return value
 *  Array reply: list of values at the specified keys.
 */
public fun cmdMGet(vararg key: ByteArray): Command = varargParamCommand(Command.MGET, *key)
public fun cmdMGet(vararg key: String): Command = varargParamCommand(Command.MGET, *key)
     
/**
 * MIGRATE host port key|"" destination-db timeout [COPY] [REPLACE] [KEYS key [key ...]] 
 * Available since 2.6.0.
 * ! Is not implemented
 */

/**
 * MONITOR
 * Available since 1.0.0.
 * ! Is not implemented
 */

/**
 * MOVE key db
 * Available since 1.0.0.
 * Return value
 *  Integer reply, specifically:
 *  1 if key was moved.
 *  0 if key was not moved.
 */
public fun cmdMove(key: String, db: Int): Command = cmdMove(key.toByteArray(Charsets.UTF_8), db)
public fun cmdMove(key: ByteArray, db: Int): Command = varargParamCommand(Command.MOVE, key, db.toString().toByteArray(Charsets.UTF_8))

/**
 * MSET key value [key value ...] 
 * Available since 1.0.1.
 * Return value
 *  Simple string reply: always OK since MSET can't fail.
 */
public fun cmdMSet(data: Map<String, String>): Command =
    Command(Command.MSET, 
        ByteArrayOutputStream().use { baos ->
            val size = 1 + data.size * 2 // komanda + (key + value) * data.size
            baos.writeAsArrayStart(size)
            baos.writeAsBulkString(Command.MSET)
            for ((key, value) in data) {
                baos.writeAsBulkString(key)
                baos.writeAsBulkString(value)
            }
            baos.toByteArray()
        }
    )

/**    
 * MSETNX key value [key value ...] 
 * Available since 1.0.1.
 * Return value
 *  Integer reply, specifically:
 *   1 if the all the keys were set.
 *   0 if no key was set (at least one key already existed).
 */
public fun cmdMSetNX(data: Map<String, String>): Command =
    Command(Command.MSETNX, 
        ByteArrayOutputStream().use { baos ->
            val size = 1 + data.size * 2 // komanda + (key + value) * data.size
            baos.writeAsArrayStart(size)
            baos.writeAsBulkString(Command.MSETNX)
            for ((key, value) in data) {
                baos.writeAsBulkString(key)
                baos.writeAsBulkString(value)
            }
            baos.toByteArray()
        }
    )

/**
 * MULTI
 * Available since 1.2.0.
 * Return value
 *  Simple string reply: always OK.
 */
public fun cmdMulti(): Command = singleCommand(Command.MULTI)

/**
 * OBJECT subcommand [arguments [arguments ...]] 
 * Available since 2.2.3.
 * ! Is not implemented
 */

/**
 * PERSIST key 
 * Available since 2.2.0.
 * Return value
 *  Integer reply, specifically:
 *   1 if the timeout was removed.
 *   0 if key does not exist or does not have an associated timeout.
 */
public fun cmdPersist(key: String): Command = cmdPersist(key.toByteArray(Charsets.UTF_8))
public fun cmdPersist(key: ByteArray): Command = varargParamCommand(Command.PERSIST, key)

/**
 * PEXPIRE key milliseconds 
 * Available since 2.6.0.
 * ! Is not implemented
 */

/**
 * PEXPIREAT key milliseconds-timestamp 
 * Available since 2.6.0.
 * ! Is not implemented
 */

/**
 * PFADD key element [element ...] 
 * Available since 2.8.9.
 * Return value
 *  Integer reply, specifically:
 *  1 if at least 1 HyperLogLog internal register was altered. 0 otherwise.
 */
public fun cmdPFAdd(key: String, vararg element: ByteArray): Command = cmdPFAdd(key.toByteArray(Charsets.UTF_8), *element)
public fun cmdPFAdd(key: String, vararg element: String): Command = cmdPFAdd(key.toByteArray(Charsets.UTF_8), *element)
public fun cmdPFAdd(key: ByteArray, vararg element: ByteArray): Command = keyAndVarargParamCommand(Command.PFADD, key, *element)
public fun cmdPFAdd(key: ByteArray, vararg element: String): Command = keyAndVarargParamCommand(Command.PFADD, key, *element)

/**
 * PFCOUNT key [key ...] 
 * Available since 2.8.9.
 * Return value
 *  Integer reply, specifically:
 *  The approximated number of unique elements observed via PFADD.
 */
public fun cmdPFCount(vararg key: String): Command = varargParamCommand(Command.PFCOUNT, *key)
public fun cmdPFCount(vararg key: ByteArray): Command = varargParamCommand(Command.PFCOUNT, *key)

/**
 * PFMERGE destkey sourcekey [sourcekey ...] 
 * Available since 2.8.9.
 * ! Is not implemented
 */

/**
 * PING [message]
 * Available since 1.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdPing(): Command = singleCommand(Command.PING)
public fun cmdPing(msg: String): Command = varargParamCommand(Command.PING, msg.toByteArray(Charsets.UTF_8))

/**
 * PSETEX key milliseconds value 
 * Available since 2.6.0.
 */
public fun cmdPSetEX(key: String, milliseconds: Long, value: String): Command = cmdPSetEX(key.toByteArray(Charsets.UTF_8), milliseconds, value.toByteArray(Charsets.UTF_8))
public fun cmdPSetEX(key: ByteArray, milliseconds: Long, value: ByteArray): Command = varargParamCommand(Command.PSETEX, key, milliseconds.toString().toByteArray(Charsets.UTF_8), value)

/**
 * PSUBSCRIBE pattern [pattern ...] 
 * Available since 2.0.0.
 * ! Is not implemented
 */

/**
 * PTTL key 
 * Available since 2.6.0.
 * Return value
 *  Integer reply: TTL in milliseconds, or a negative value in order to signal an error .
 */
public fun cmdPTtl(key: String): Command = cmdPTtl(key.toByteArray(Charsets.UTF_8))
public fun cmdPTtl(key: ByteArray): Command = varargParamCommand(Command.PTTL, key)

/**
 * PUBLISH channel message
 * Available since 2.0.0.
 * ! Is not implemented
 */

/**
 * PUBSUB subcommand [argument [argument ...]] 
 * PUBSUB CHANNELS [pattern]
 * Available since 2.8.0.
 * ! Is not implemented
 */

/**
 * PUNSUBSCRIBE [pattern [pattern ...]] 
 * Available since 2.0.0.
 * ! Is not implemented
 */

/**
 * QUIT
 * Available since 1.0.0.
 * Return value
 *  Simple string reply: always OK.
 */
public fun cmdQuit(): Command = singleCommand(Command.QUIT)

/**
 * RANDOMKEY
 * Available since 1.0.0.
 * Return value
 *  Bulk string reply: the random key, or nil when the database is empty.
 */
public fun cmdRandomKey(): Command = singleCommand(Command.RANDOMKEY)

/**
 * READONLY
 * Available since 3.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdReadOnly(): Command = singleCommand(Command.READONLY)

/**
 * READWRITE
 * Available since 3.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdReadWrite(): Command = singleCommand(Command.READWRITE)

/**
 * RENAME key newkey
 * Available since 1.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdRename(key: String, newkey: String): Command = cmdRename(key.toByteArray(Charsets.UTF_8), newkey.toByteArray(Charsets.UTF_8))
public fun cmdRename(key: ByteArray, newkey: ByteArray): Command = varargParamCommand(Command.RENAME, key, newkey)

/**
 * RENAMENX key newkey 
 * Available since 1.0.0.
 * Return value
 *  Integer reply, specifically:
 *  1 if key was renamed to newkey.
 *  0 if newkey already exists.
 */
public fun cmdRenameNx(key: String, newkey: String): Command = cmdRenameNx(key.toByteArray(Charsets.UTF_8), newkey.toByteArray(Charsets.UTF_8))
public fun cmdRenameNx(key: ByteArray, newkey: ByteArray): Command = varargParamCommand(Command.RENAMENX, key, newkey)

/**
 * RESTORE key ttl serialized-value [REPLACE] 
 * Available since 2.6.0.
 * ! Is not implemented
 */

/**
 * ROLE
 * Available since 2.8.12.
 * ! Is not implemented
 */

/**
 * RPOP key 
 * Available since 1.0.0.
 * Return value
 *  Bulk string reply: the value of the last element, or nil when key does not exist.
 */
public fun cmdRPop(key: String): Command = cmdRPop(key.toByteArray(Charsets.UTF_8))
public fun cmdRPop(key: ByteArray): Command = varargParamCommand(Command.RPOP, key)

/**
 * RPOPLPUSH source destination
 * Available since 1.2.0.
 * Return value
 *  Bulk string reply: the element being popped and pushed.
 */
public fun cmdRPopLPush(source: String, destination: String): Command = cmdRPopLPush(source.toByteArray(Charsets.UTF_8), destination.toByteArray(Charsets.UTF_8))
public fun cmdRPopLPush(source: ByteArray, destination: ByteArray): Command = varargParamCommand(Command.RPOPLPUSH, source, destination)

/**
 * RPUSH key value [value ...] 
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the length of the list after the push operation.
 */
public fun cmdRPush(key: String, vararg value: ByteArray): Command = cmdRPush(key.toByteArray(Charsets.UTF_8), *value)
public fun cmdRPush(key: String, vararg value: String): Command = cmdRPush(key.toByteArray(Charsets.UTF_8), *value)
public fun cmdRPush(key: ByteArray, vararg value: ByteArray): Command = keyAndVarargParamCommand(Command.RPUSH, key, *value)
public fun cmdRPush(key: ByteArray, vararg value: String): Command = keyAndVarargParamCommand(Command.RPUSH, key, *value)

/**
 * RPUSHX key value 
 * Available since 2.2.0.
 * Return value
 *  Integer reply: the length of the list after the push operation.
 */
public fun cmdRPushX(key: String, value: ByteArray): Command = cmdRPushX(key.toByteArray(Charsets.UTF_8), value)
public fun cmdRPushX(key: String, value: String): Command = cmdRPushX(key.toByteArray(Charsets.UTF_8), value.toByteArray(Charsets.UTF_8))
public fun cmdRPushX(key: ByteArray, value: String): Command = cmdRPushX(key, value.toByteArray(Charsets.UTF_8))
public fun cmdRPushX(key: ByteArray, value: ByteArray): Command = varargParamCommand(Command.RPUSHX, key, value)
 
/**
 * SADD key member [member ...] 
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the number of elements that were added to the set, not including all the elements already present into the set.
 */
public fun cmdSAdd(key: String, vararg member: ByteArray): Command = cmdSAdd(key.toByteArray(Charsets.UTF_8), *member)
public fun cmdSAdd(key: String, vararg member: String): Command = cmdSAdd(key.toByteArray(Charsets.UTF_8), *member)
public fun cmdSAdd(key: ByteArray, vararg member: ByteArray): Command = keyAndVarargParamCommand(Command.SADD, key, *member)
public fun cmdSAdd(key: ByteArray, vararg member: String): Command = keyAndVarargParamCommand(Command.SADD, key, *member)

/**
 * SAVE
 * Available since 1.0.0.
 * Return value
 *  Simple string reply: The commands returns OK on success.
 */
public fun cmdSave(): Command = singleCommand(Command.SAVE)

/**
 * SCAN cursor [MATCH pattern] [COUNT count] 
 * Available since 2.8.0.
 * ! Is not implemented
 */
 
/**
 * SCARD key 
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the cardinality (number of elements) of the set, or 0 if key does not exist. 
 */
public fun cmdSCard(key: String): Command = cmdSCard(key.toByteArray(Charsets.UTF_8))
public fun cmdSCard(key: ByteArray): Command = varargParamCommand(Command.SCARD, key)

/**
 * SCRIPT DEBUG YES|SYNC|NO 
 * Available since 3.2.0.
 * Return value
 *  Simple string reply: OK.
 */
public fun cmdScriptDebug(mode: ScriptDebugMode): Command =
    varargParamCommand(Command.SCRIPT_DEBUG,
        when (mode) {
            ScriptDebugMode.YES -> "YES".toByteArray(Charsets.UTF_8)
            ScriptDebugMode.SYNC -> "SYNC".toByteArray(Charsets.UTF_8)
            ScriptDebugMode.NO -> "NO".toByteArray(Charsets.UTF_8)
        }
    )

/**
 * SCRIPT EXISTS sha1 [sha1 ...] 
 * Available since 2.6.0.
 * ! Is not implemented
 */

/**
 * SCRIPT FLUSH
 * Available since 2.6.0.
 * Return value
 *  Simple string reply
 */
public fun cmdScriptFlush(): Command = singleCommand(Command.SCRIPT_FLUSH)

/**
 * SCRIPT KILL
 * Available since 2.6.0.
 * Return value
 *  Simple string reply
 */
public fun cmdScriptKill(): Command = singleCommand(Command.SCRIPT_KILL)

/**
 * SCRIPT LOAD script 
 * Available since 2.6.0.
 * ! Is not implemented
 */
 
/**
 * SDIFF key [key ...] 
 * Available since 1.0.0.
 * Return value
 *  Array reply: list with members of the resulting set.
 */
public fun cmdSDiff(vararg key: ByteArray): Command = varargParamCommand(Command.SDIFF, *key)
public fun cmdSDiff(vararg key: String): Command = varargParamCommand(Command.SDIFF, *key)

/**
 * SDIFFSTORE destination key [key ...] 
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the number of elements in the resulting set.
 */
public fun cmdSDiffStore(destination: String, vararg key: String): Command = keyAndVarargParamCommand(Command.SDIFFSTORE, destination.toByteArray(Charsets.UTF_8), *key)
public fun cmdSDiffStore(destination: ByteArray, vararg key: ByteArray): Command = keyAndVarargParamCommand(Command.SDIFFSTORE, destination, *key)

/**
 * SELECT index
 * Available since 1.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdSelect(index: Int): Command = varargParamCommand(Command.SELECT, index.toString().toByteArray(Charsets.UTF_8))

/**
 * SET key value [EX seconds] [PX milliseconds] [NX|XX] 
 * Available since 1.0.0.
 * Return value
 *  Simple string reply: OK if SET was executed correctly. Null reply: a Null Bulk Reply is returned if the SET operation was not performed because the user specified the NX or XX option but the condition was not met.
 */
public fun cmdSet(key: String, value: String, pxs: Int = 0, pxms: Long = 0.toLong(), option: SetOptions = SetOptions.NONE): Command = cmdSet(key.toByteArray(Charsets.UTF_8), value.toByteArray(Charsets.UTF_8), pxs, pxms, option)
public fun cmdSet(key: ByteArray, value: ByteArray, pxs: Int = 0, pxms: Long = 0.toLong(), option: SetOptions = SetOptions.NONE): Command =
    Command(Command.SET, 
        ByteArrayOutputStream().use { baos ->
            val size = 3 + // command + key + value
                if (pxs != 0) 1 else 0 +
                if (pxms != 0L) 1 else 0 +
                when (option) {
                    SetOptions.NONE -> 0
                    else -> 1
                }
            baos.writeAsArrayStart(size)
            baos.writeAsBulkString(Command.SET)
            baos.writeAsBulkString(key)
            baos.writeAsBulkString(value)
            if (pxs != 0)
                baos.writeAsBulkString(pxs)
            if (pxms != 0L)
                baos.writeAsBulkString(pxms)
            when (option) {
                SetOptions.NX -> baos.writeAsBulkString("NX")
                SetOptions.XX -> baos.writeAsBulkString("XX")
                else -> {}
            }
            baos.toByteArray()
        }
    )

/**
 * SETBIT key offset value 
 * Available since 2.2.0.
 * ! Is not implemented
 */

/**
 * SETEX key seconds value 
 * Available since 2.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdSetEX(key: String, seconds: Int, value: String): Command = cmdSetEX(key.toByteArray(Charsets.UTF_8), seconds, value.toByteArray(Charsets.UTF_8))
public fun cmdSetEX(key: ByteArray, seconds: Int, value: ByteArray): Command = varargParamCommand(Command.SETEX, key, seconds.toString().toByteArray(Charsets.UTF_8), value)
 
/**
 * SETNX key value 
 * Available since 1.0.0.
 * Return value
 *  Integer reply, specifically:
 *   1 if the key was set
 *   0 if the key was not set
 */
public fun cmdSetNX(key: String, value: ByteArray): Command = cmdSetNX(key.toByteArray(Charsets.UTF_8), value)
public fun cmdSetNX(key: String, value: String): Command = cmdSetNX(key.toByteArray(Charsets.UTF_8), value.toByteArray(Charsets.UTF_8))
public fun cmdSetNX(key: ByteArray, value: ByteArray): Command = varargParamCommand(Command.SETNX, key, value)

/**
 * SETRANGE key offset value 
 * Available since 2.2.0.
 * Return value
 *  Integer reply: the length of the string after it was modified by the command.
 */
public fun cmdSetRange(key: String, offset: Int, value: String): Command = cmdSetRange(key.toByteArray(Charsets.UTF_8), offset, value.toByteArray(Charsets.UTF_8))
public fun cmdSetRange(key: ByteArray, offset: Int, value: ByteArray): Command = varargParamCommand(Command.SETRANGE, key, offset.toString().toByteArray(Charsets.UTF_8), value)

/**
 * SHUTDOWN [NOSAVE|SAVE] 
 * Available since 1.0.0.
 * Return value
 *  Simple string reply on error. On success nothing is returned since the server quits and the connection is closed.
 */
public fun cmdShutdown(option: ShutdownOptions): Command =
    varargParamCommand(Command.SHUTDOWN,
        when (option) {
            ShutdownOptions.NOSAVE -> "NOSAVE".toByteArray(Charsets.UTF_8)
            ShutdownOptions.SAVE -> "SAVE".toByteArray(Charsets.UTF_8)
        }
    )
 
/**
 * SINTER key [key ...] 
 * Available since 1.0.0.
 * Return value
 *  Array reply: list with members of the resulting set.
 */
public fun cmdSInter(vararg key: ByteArray): Command = varargParamCommand(Command.SINTER, *key)
public fun cmdSInter(vararg key: String): Command = varargParamCommand(Command.SINTER, *key)

/**
 * SINTERSTORE destination key [key ...] 
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the number of elements in the resulting set.
 */
public fun cmdSInterStore(destination: String, vararg key: String): Command = keyAndVarargParamCommand(Command.SINTERSTORE, destination.toByteArray(Charsets.UTF_8), *key)
public fun cmdSInterStore(destination: ByteArray, vararg key: ByteArray): Command = keyAndVarargParamCommand(Command.SINTERSTORE, destination, *key)
 
/**
 * SISMEMBER key member 
 * Available since 1.0.0.
 * Return value
 *  Integer reply, specifically:
 *   1 if the element is a member of the set.
 *   0 if the element is not a member of the set, or if key does not exist.
 */
public fun cmdSisMember(key: String, member: ByteArray): Command = cmdSisMember(key.toByteArray(Charsets.UTF_8), member)
public fun cmdSisMember(key: String, member: String): Command = cmdSisMember(key.toByteArray(Charsets.UTF_8), member.toByteArray(Charsets.UTF_8))
public fun cmdSisMember(key: ByteArray, member: ByteArray): Command = varargParamCommand(Command.SISMEMBER, key, member)

/**
 * SLAVEOF host port 
 * Available since 1.0.0.
 * ! Is not implemented
 */

/**
 * SLOWLOG subcommand [argument] 
 * Available since 2.2.12.
 * ! Is not implemented
 */

/**
 * SMEMBERS key
 * Available since 1.0.0.
 * Return value
 *  Array reply: all elements of the set.
 */
public fun cmdSMembers(key: String): Command = cmdSMembers(key.toByteArray(Charsets.UTF_8))
public fun cmdSMembers(key: ByteArray): Command = varargParamCommand(Command.SMEMBERS, key)

/**
 * SMOVE source destination member 
 * Available since 1.0.0.
 * Return value
 *  Integer reply, specifically:
 *   1 if the element is moved.
 *   0 if the element is not a member of source and no operation was performed.
 */
public fun cmdSMove(source: String, destination: String, member: String): Command = cmdSMove(source.toByteArray(Charsets.UTF_8), destination.toByteArray(Charsets.UTF_8), member.toByteArray(Charsets.UTF_8))
public fun cmdSMove(source: ByteArray, destination: ByteArray, member: ByteArray): Command = varargParamCommand(Command.SMOVE, source, destination, member)

/**
 * SORT key [BY pattern] [LIMIT offset count] [GET pattern [GET pattern ...]] [ASC|DESC] [ALPHA] [STORE destination]     
 * Available since 1.0.0.
 * ! Is not implemented
 */

/**
 * SPOP key [count] 
 * Available since 1.0.0.
 * Return value
 *  Bulk string reply: the removed element, or nil when key does not exist.
 */
public fun cmdSPop(key: String): Command = cmdSPop(key.toByteArray(Charsets.UTF_8))
public fun cmdSPop(key: ByteArray): Command = varargParamCommand(Command.SPOP, key)
public fun cmdSPop(key: String, count: Int): Command = cmdSPop(key.toByteArray(Charsets.UTF_8), count)
public fun cmdSPop(key: ByteArray, count: Int): Command = varargParamCommand(Command.SPOP, key, count.toString().toByteArray(Charsets.UTF_8))

/**
 * SRANDMEMBER key [count] 
 * Available since 1.0.0.
 * Return value
 *  Bulk string reply
 */
public fun cmdSRandMember(key: String): Command = cmdSRandMember(key.toByteArray(Charsets.UTF_8))
public fun cmdSRandMember(key: ByteArray): Command = varargParamCommand(Command.SRANDMEMBER, key)
public fun cmdSRandMember(key: String, count: Int): Command = cmdSRandMember(key.toByteArray(Charsets.UTF_8), count)
public fun cmdSRandMember(key: ByteArray, count: Int): Command = varargParamCommand(Command.SRANDMEMBER, key, count.toString().toByteArray(Charsets.UTF_8))

/**
 * SREM key member [member ...] 
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the number of members that were removed from the set, not including non existing members.
 */
public fun cmdSRem(key: String, vararg member: ByteArray): Command = cmdSRem(key.toByteArray(Charsets.UTF_8), *member)
public fun cmdSRem(key: String, vararg member: String): Command = cmdSRem(key.toByteArray(Charsets.UTF_8), *member)
public fun cmdSRem(key: ByteArray, vararg member: ByteArray): Command = keyAndVarargParamCommand(Command.SREM, key, *member)
public fun cmdSRem(key: ByteArray, vararg member: String): Command = keyAndVarargParamCommand(Command.SREM, key, *member)

/**
 * SSCAN key cursor [MATCH pattern] [COUNT count] 
 * Available since 2.8.0.
 * ! Is not implemented
 */
 
/**
 * STRLEN key 
 * Available since 2.2.0.
 * Return value
 *  Integer reply: the length of the string at key, or 0 when key does not exist.
 */
public fun cmdStrLen(key: String): Command = cmdStrLen(key.toByteArray(Charsets.UTF_8))
public fun cmdStrLen(key: ByteArray): Command = varargParamCommand(Command.STRLEN, key)

/**
 * SUBSCRIBE channel [channel ...] 
 * Available since 2.0.0.
 */
public fun cmdSubscribe(vararg channel: ByteArray): Command = varargParamCommand(Command.SUBSCRIBE, *channel)
public fun cmdSubscribe(vararg channel: String): Command = varargParamCommand(Command.SUBSCRIBE, *channel)
 
/**
 * SUNION key [key ...] 
 * Available since 1.0.0.
 * Return value
 *  Array reply: list with members of the resulting set.
 */
public fun cmdSUnion(vararg key: ByteArray): Command = varargParamCommand(Command.SUNION, *key)
public fun cmdSUnion(vararg key: String): Command = varargParamCommand(Command.SUNION, *key)

/**
 * SUNIONSTORE destination key [key ...] 
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the number of elements in the resulting set.
 */
public fun cmdSUnionStore(destination: String, vararg key: String): Command = cmdSUnionStore(destination.toByteArray(Charsets.UTF_8), *key)
public fun cmdSUnionStore(destination: ByteArray, vararg key: ByteArray): Command = keyAndVarargParamCommand(Command.SUNIONSTORE, destination, *key)
public fun cmdSUnionStore(destination: ByteArray, vararg key: String): Command = keyAndVarargParamCommand(Command.SUNIONSTORE, destination, *key)
 
/**
 * SWAPDB index index
 * Available since 4.0.0.
 * Return value
 *  Simple string reply: OK
 */
public fun cmdSwapDB(index1: Int, index2: Int): Command = varargParamCommand(Command.SWAPDB, index1.toString().toByteArray(Charsets.UTF_8), index2.toString().toByteArray(Charsets.UTF_8))

/**
 * SYNC
 * Available since 1.0.0.
 * ! Is not implemented
 */

/** 
 * TIME
 * Available since 2.6.0.
 * Return value
 *  Array reply, specifically:
 *  A multi bulk reply containing two elements:
 *   unix time in seconds.
 *   microseconds.
 */
public fun cmdTime(): Command = singleCommand(Command.TIME)

/**
 * TOUCH key [key ...] 
 * Available since 3.2.1.
 * Return value
 *  Integer reply: The number of keys that were touched.
 */
public fun cmdTouch(vararg key: ByteArray): Command = varargParamCommand(Command.TOUCH, *key)
public fun cmdTouch(vararg key: String): Command = varargParamCommand(Command.TOUCH, *key)

/**
 * TTL key 
 * Available since 1.0.0.
 * Return value
 *  Integer reply: TTL in seconds, or a negative value in order to signal an error
 */
public fun cmdTtl(key: String): Command = cmdTtl(key.toByteArray(Charsets.UTF_8))
public fun cmdTtl(key: ByteArray): Command = varargParamCommand(Command.TTL, key)

/**
 * TYPE key 
 * Available since 1.0.0.
 * Return value
 *  Simple string reply: type of key, or none when key does not exist.
 */
public fun cmdType(key: String): Command = cmdType(key.toByteArray(Charsets.UTF_8))
public fun cmdType(key: ByteArray): Command = varargParamCommand(Command.TYPE, key)

/**
 * UNSUBSCRIBE [channel [channel ...]]
 * Available since 2.0.0.
 */
public fun cmdUnsubscribe(): Command = singleCommand(Command.UNSUBSCRIBE)
public fun cmdUnsubscribe(vararg channel: ByteArray): Command = varargParamCommand(Command.UNSUBSCRIBE, *channel)
public fun cmdUnsubscribe(vararg channel: String): Command = varargParamCommand(Command.UNSUBSCRIBE, *channel)

/**
 * UNLINK key [key ...] 
 * Available since 4.0.0.
 * Return value
 *  Integer reply: The number of keys that were unlinked.
 */
public fun cmdUnLink(vararg key: ByteArray): Command = varargParamCommand(Command.UNLINK, *key)
public fun cmdUnLink(vararg key: String): Command = varargParamCommand(Command.UNLINK, *key)

/**
 * UNWATCH
 * Available since 2.2.0.
 * Return value
 *  Simple string reply: always OK.
 */
public fun cmdUnwatch(): Command = singleCommand(Command.UNWATCH)

/**
 * WAIT numslaves timeout 
 * Available since 3.0.0.
 * ! Is not implemented
 */

/**
 * WATCH key [key ...] 
 * Available since 2.2.0.
 * Return value
 *  Simple string reply: always OK.
 */
public fun cmdWatch(vararg key: ByteArray): Command = varargParamCommand(Command.WATCH, *key)
public fun cmdWatch(vararg key: String): Command = varargParamCommand(Command.WATCH, *key)

/**
 * ZADD key [NX|XX] [CH] [INCR] score member [score member ...] 
 * Available since 1.2.0.
 * ! Is not implemented
 */

/**
 * ZCARD key 
 * Available since 1.2.0.
 * Return value
 *  Integer reply: the cardinality (number of elements) of the sorted set, or 0 if key does not exist.
 */
public fun cmdZCard(key: String): Command = cmdZCard(key.toByteArray(Charsets.UTF_8))
public fun cmdZCard(key: ByteArray): Command = varargParamCommand(Command.ZCARD, key)

/**
 * ZCOUNT key min max 
 * Available since 2.0.0.
 * ! Is not implemented
 */

/**
 * ZINCRBY key increment member 
 * Available since 1.2.0.
 * Return value
 *  Bulk string reply: the new score of member (a double precision floating point number), represented as string.
 */
public fun cmdZIncrBy(key: String, increment: Int, member: String): Command = cmdZIncrBy(key.toByteArray(Charsets.UTF_8), increment, member.toByteArray(Charsets.UTF_8))
public fun cmdZIncrBy(key: ByteArray, increment: Int, member: ByteArray): Command = varargParamCommand(Command.ZINCRBY, key, increment.toString().toByteArray(Charsets.UTF_8), member)

/**
 * ZINTERSTORE destination numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX] 
 * Available since 2.0.0.
 * ! Is not implemented
 */

/**
 * ZLEXCOUNT key min max 
 * Available since 2.8.9.
 * ! Is not implemented
 */

/**
 * ZRANGE key start stop [WITHSCORES] 
 * Available since 1.2.0.
 * ! Is not implemented
 */

/**
 * ZRANGEBYLEX key min max [LIMIT offset count] 
 * Available since 2.8.9.
 * ! Is not implemented
 */

/**
 * ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count] 
 * Available since 1.0.5.
 * ! Is not implemented
 */

/**
 * ZRANK key member 
 * Available since 2.0.0.
 * ! Is not implemented
 */

/**
 * ZREM key member [member ...] 
 * Available since 1.2.0.
 * ! Is not implemented
 */

/**
 * ZREMRANGEBYLEX key min max 
 * Available since 2.8.9.
 * ! Is not implemented
 */

/**
 * ZREMRANGEBYRANK key start stop 
 * Available since 2.0.0.
 * ! Is not implemented
 */

/**
 * ZREMRANGEBYSCORE key min max 
 * Available since 1.2.0.
 * ! Is not implemented
 */

/**
 * ZREVRANGE key start stop [WITHSCORES] 
 * Available since 1.2.0.
 * ! Is not implemented
 */

/**
 * ZREVRANGEBYLEX key max min [LIMIT offset count] 
 * Available since 2.8.9.
 * ! Is not implemented
 */

/**
 * ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count] 
 * Available since 2.2.0.
 * ! Is not implemented
 */

/**
 * ZREVRANK key member 
 * Available since 2.0.0.
 * Return value
 *  If member exists in the sorted set, Integer reply: the rank of member.
 *  If member does not exist in the sorted set or key does not exist, Bulk string reply: nil.
 */
public fun cmdZRevRank(key: String, member: String): Command = cmdZRevRank(key.toByteArray(Charsets.UTF_8), member.toByteArray(Charsets.UTF_8))
public fun cmdZRevRank(key: String, member: ByteArray): Command = cmdZRevRank(key.toByteArray(Charsets.UTF_8), member)
public fun cmdZRevRank(key: ByteArray, member: ByteArray): Command = varargParamCommand(Command.ZREVRANK, key, member)

/**
 * ZSCAN key cursor [MATCH pattern] [COUNT count] 
 * Available since 2.8.0.
 * ! Is not implemented
 */

/**
 * ZSCORE key member 
 * Available since 1.2.0.
 * Return value
 *  Bulk string reply: the score of member (a double precision floating point number), represented as string.
 */
public fun cmdZScore(key: String, member: String): Command = cmdZScore(key.toByteArray(Charsets.UTF_8), member.toByteArray(Charsets.UTF_8))
public fun cmdZScore(key: String, member: ByteArray): Command = cmdZScore(key.toByteArray(Charsets.UTF_8), member)
public fun cmdZScore(key: ByteArray, member: ByteArray): Command = varargParamCommand(Command.ZSCORE, key, member)

/**
 * ZUNIONSTORE destination numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX] 
 * Available since 2.0.0.
 * ! Is not implemented
 */

/**
 * Command class
 * 
 * User: sibircevs
 * Date: 04.01.2017
 * @param name
 * @param cmd
 */
class Command(val name: String, val cmd: ByteArray) {

    companion object Factory {
        val APPEND: String = "APPEND" // Append a value to a key; Available since 2.0.0.
        val AUTH: String = "AUTH" // Authenticate to the server
        val BGREWRITEAOF: String = "BGREWRITEAOF" // Asynchronously rewrite the append-only file
        val BGSAVE: String = "BGSAVE" // Asynchronously save the dataset to disk
        val BITCOUNT: String = "BITCOUNT" // Count set bits in a string
        val BITFIELD: String = "BITFIELD" // Perform arbitrary bitfield integer operations on strings
        val BITOP: String = "BITOP" // Perform bitwise operations between strings
        val BITPOS: String = "BITPOS" // Find first bit set or clear in a string
        val BLPOP: String = "BLPOP" // Remove and get the first element in a list, or block until one is available
        val BRPOP: String = "BRPOP" // Remove and get the last element in a list, or block until one is available
        val BRPOPLPUSH: String = "BRPOPLPUSH" // Pop a value from a list, push it to another list and return it; or block until one is available
        val CLIENT_KILL: String = "CLIENT_KILL" // Kill the connection of a client
        val CLIENT_LIST: String = "CLIENT_LIST" // Get the list of client connections
        val CLIENT_GETNAME: String = "CLIENT_GETNAME" // Get the current connection name
        val CLIENT_PAUSE: String = "CLIENT_PAUSE" // Stop processing commands from clients for some time
        val CLIENT_REPLY: String = "CLIENT_REPLY" // Instruct the server whether to reply to commands
        val CLIENT_SETNAME: String = "CLIENT_SETNAME" // Set the current connection name
        val CLUSTER_ADDSLOTS: String = "CLUSTER_ADDSLOTS" // Assign new hash slots to receiving node
        val CLUSTER_COUNT_FAILURE_REPORTS: String = "CLUSTER_COUNT_FAILURE_REPORTS" // Return the number of failure reports active for a given node
        val CLUSTER_COUNTKEYSINSLOT: String = "CLUSTER_COUNTKEYSINSLOT" // Return the number of local keys in the specified hash slot
        val CLUSTER_DELSLOTS: String = "CLUSTER_DELSLOTS" // Set hash slots as unbound in receiving node
        val CLUSTER_FAILOVER: String = "CLUSTER_FAILOVER" // Forces a slave to perform a manual failover of its master.
        val CLUSTER_FORGET: String = "CLUSTER_FORGET" // Remove a node from the nodes table
        val CLUSTER_GETKEYSINSLOT: String = "CLUSTER_GETKEYSINSLOT" // Return local key names in the specified hash slot
        val CLUSTER_INFO: String = "CLUSTER_INFO" // Provides info about Redis Cluster node state
        val CLUSTER_KEYSLOT: String = "CLUSTER_KEYSLOT" // Returns the hash slot of the specified key
        val CLUSTER_MEET: String = "CLUSTER_MEET" // Force a node cluster to handshake with another node
        val CLUSTER_NODES: String = "CLUSTER_NODES" // Get Cluster config for the node
        val CLUSTER_REPLICATE: String = "CLUSTER_REPLICATE" // Reconfigure a node as a slave of the specified master node
        val CLUSTER_RESET: String = "CLUSTER_RESET" // Reset a Redis Cluster node
        val CLUSTER_SAVECONFIG: String = "CLUSTER_SAVECONFIG" // Forces the node to save cluster state on disk
        val CLUSTER_SET_CONFIG_EPOCH: String = "CLUSTER_SET_CONFIG_EPOCH" // Set the configuration epoch in a new node
        val CLUSTER_SETSLOT: String = "CLUSTER_SETSLOT" // Bind a hash slot to a specific node
        val CLUSTER_SLAVES: String = "CLUSTER_SLAVES" // List slave nodes of the specified master node
        val CLUSTER_SLOTS: String = "CLUSTER_SLOTS" // Get array of Cluster slot to node mappings
        val COMMAND: String = "COMMAND" // Get array of Redis command details
        val COMMAND_COUNT: String = "COMMAND_COUNT" // Get total number of Redis commands
        val COMMAND_GETKEYS: String = "COMMAND_GETKEYS" // Extract keys given a full Redis command
        val COMMAND_INFO: String = "COMMAND_INFO" // Get array of specific Redis command details
        val CONFIG_GET: String = "CONFIG_GET" // Get the value of a configuration parameter
        val CONFIG_REWRITE: String = "CONFIG_REWRITE" // Rewrite the configuration file with the in memory configuration
        val CONFIG_SET: String = "CONFIG_SET" // Set a configuration parameter to the given value
        val CONFIG_RESETSTAT: String = "CONFIG_RESETSTAT" // Reset the stats returned by INFO
        val DBSIZE: String = "DBSIZE" // Return the number of keys in the selected database
        val DEBUG_OBJECT: String = "DEBUG_OBJECT" // Get debugging information about a key
        val DEBUG_SEGFAULT: String = "DEBUG_SEGFAULT" // Make the server crash
        val DECR: String = "DECR" // Decrement the integer value of a key by one
        val DECRBY: String = "DECRBY" // Decrement the integer value of a key by the given number
        val DEL: String = "DEL" // Delete a key
        val DISCARD: String = "DISCARD" // Discard all commands issued after MULTI
        val DUMP: String = "DUMP" // Return a serialized version of the value stored at the specified key.
        val ECHO: String = "ECHO" // Echo the given string
        val EVAL: String = "EVAL" // Execute a Lua script server side
        val EVALSHA: String = "EVALSHA" // Execute a Lua script server side
        val EXEC: String = "EXEC" // Execute all commands issued after MULTI
        val EXISTS: String = "EXISTS" // Determine if a key exists
        val EXPIRE: String = "EXPIRE" // Set a key's time to live in seconds
        val EXPIREAT: String = "EXPIREAT" // Set the expiration for a key as a UNIX timestamp
        val FLUSHALL: String = "FLUSHALL" // Remove all keys from all databases
        val FLUSHDB: String = "FLUSHDB" // Remove all keys from the current database
        val GEOADD: String = "GEOADD" // Add one or more geospatial items in the geospatial index represented using a sorted set
        val GEOHASH: String = "GEOHASH" // Returns members of a geospatial index as standard geohash strings
        val GEOPOS: String = "GEOPOS" // Returns longitude and latitude of members of a geospatial index
        val GEODIST: String = "GEODIST" // Returns the distance between two members of a geospatial index
        val GEORADIUS: String = "GEORADIUS" // Query a sorted set representing a geospatial index to fetch members matching a given maximum distance from a point
        val GEORADIUSBYMEMBER: String = "GEORADIUSBYMEMBER"     // Query a sorted set representing a geospatial index to fetch members matching a given maximum distance from a member
        val GET: String = "GET" // Get the value of a key
        val GETBIT: String = "GETBIT" // Returns the bit value at offset in the string value stored at key
        val GETRANGE: String = "GETRANGE" // Get a substring of the string stored at a key
        val GETSET: String = "GETSET" // Set the string value of a key and return its old value
        val HDEL: String = "HDEL" // Delete one or more hash fields
        val HEXISTS: String = "HEXISTS" // Determine if a hash field exists
        val HGET: String = "HGET" // Get the value of a hash field
        val HGETALL: String = "HGETALL" // Get all the fields and values in a hash
        val HINCRBY: String = "HINCRBY" // Increment the integer value of a hash field by the given number
        val HINCRBYFLOAT: String = "HINCRBYFLOAT" // Increment the float value of a hash field by the given amount
        val HKEYS: String = "HKEYS" // Get all the fields in a hash
        val HLEN: String = "HLEN" // Get the number of fields in a hash
        val HMGET: String = "HMGET" // Get the values of all the given hash fields
        val HMSET: String = "HMSET" // Set multiple hash fields to multiple values
        val HSET: String = "HSET" // Set the string value of a hash field
        val HSETNX: String = "HSETNX" // Set the value of a hash field, only if the field does not exist
        val HSTRLEN: String = "HSTRLEN" // Get the length of the value of a hash field
        val HVALS: String = "HVALS" // Get all the values in a hash
        val INCR: String = "INCR" // Increment the integer value of a key by one
        val INCRBY: String = "INCRBY" // increment Increment the integer value of a key by the given amount
        val INCRBYFLOAT: String = "INCRBYFLOAT" // increment Increment the float value of a key by the given amount
        val INFO: String = "INFO" // Get information and statistics about the server
        val KEYS: String = "KEYS" // Find all keys matching the given pattern
        val LASTSAVE: String = "LASTSAVE" // Get the UNIX time stamp of the last successful save to disk
        val LINDEX: String = "LINDEX" // Get an element from a list by its index
        val LINSERT: String = "LINSERT" // Insert an element before or after another element in a list
        val LLEN: String = "LLEN" // Get the length of a list
        val LPOP: String = "LPOP" // Remove and get the first element in a list
        val LPUSH: String = "LPUSH" // Prepend one or multiple values to a list
        val LPUSHX: String = "LPUSHX" // Prepend a value to a list, only if the list exists
        val LRANGE: String = "LRANGE" // Get a range of elements from a list
        val LREM: String = "LREM" // Remove elements from a list
        val LSET: String = "LSET" // Set the value of an element in a list by its index
        val LTRIM: String = "LTRIM" // Trim a list to the specified range
        val MGET: String = "MGET" // Get the values of all the given keys
        val MIGRATE: String = "MIGRATE" // Atomically transfer a key from a Redis instance to another one.
        val MONITOR: String = "MONITOR" // Listen for all requests received by the server in real time
        val MOVE: String = "MOVE" // Move a key to another database
        val MSET: String = "MSET" // Set multiple keys to multiple values
        val MSETNX: String = "MSETNX" // Set multiple keys to multiple values, only if none of the keys exist
        val MULTI: String = "MULTI" // Mark the start of a transaction block
        val OBJECT: String = "OBJECT" // Inspect the internals of Redis objects
        val PERSIST: String = "PERSIST" // Remove the expiration from a key
        val PEXPIRE: String = "PEXPIRE" // milliseconds Set a key's time to live in milliseconds
        val PEXPIREAT: String = "PEXPIREAT" // Set the expiration for a key as a UNIX timestamp specified in milliseconds
        val PFADD: String = "PFADD" // Adds the specified elements to the specified HyperLogLog.
        val PFCOUNT: String = "PFCOUNT" // Return the approximated cardinality of the set(s) observed by the HyperLogLog at key(s).
        val PFMERGE: String = "PFMERGE" // Merge N different HyperLogLogs into a single one.
        val PING: String = "PING" // Ping the server
        val PSETEX: String = "PSETEX" // Set the value and expiration in milliseconds of a key
        val PSUBSCRIBE: String = "PSUBSCRIBE" // Listen for messages published to channels matching the given patterns
        val PUBSUB: String = "PUBSUB" // Inspect the state of the Pub/Sub subsystem
        val PTTL: String = "PTTL" // Get the time to live for a key in milliseconds
        val PUBLISH: String = "PUBLISH" // Post a message to a channel
        val PUNSUBSCRIBE: String = "PUNSUBSCRIBE" // Stop listening for messages posted to channels matching the given patterns
        val QUIT: String = "QUIT" // Close the connection
        val RANDOMKEY: String = "RANDOMKEY" // Return a random key from the keyspace
        val READONLY: String = "READONLY" // Enables read queries for a connection to a cluster slave node
        val READWRITE: String = "READWRITE" // Disables read queries for a connection to a cluster slave node
        val RENAME: String = "RENAME" // Rename a key
        val RENAMENX: String = "RENAMENX" // Rename a key, only if the new key does not exist
        val RESTORE: String = "RESTORE" // Create a key using the provided serialized value, previously obtained using DUMP.
        val ROLE: String = "ROLE" // Return the role of the instance in the context of replication
        val RPOP: String = "RPOP" // Remove and get the last element in a list
        val RPOPLPUSH: String = "RPOPLPUSH" // Remove the last element in a list, prepend it to another list and return it
        val RPUSH: String = "RPUSH" // Append one or multiple values to a list
        val RPUSHX: String = "RPUSHX" // Append a value to a list, only if the list exists
        val SADD: String = "SADD" // Add one or more members to a set
        val SAVE: String = "SAVE" // Synchronously save the dataset to disk
        val SCARD: String = "SCARD" // Get the number of members in a set
        val SCRIPT_DEBUG: String = "SCRIPT_DEBUG"               // Set the debug mode for executed scripts.
        val SCRIPT_EXISTS: String = "SCRIPT_EXISTS"             // Check existence of scripts in the script cache.
        val SCRIPT_FLUSH: String = "SCRIPT_FLUSH"               // Remove all the scripts from the script cache.
        val SCRIPT_KILL: String = "SCRIPT_KILL"                 // Kill the script currently in execution.
        val SCRIPT_LOAD: String = "SCRIPT_LOAD"                 // script Load the specified Lua script into the script cache.
        val SDIFF: String = "SDIFF" // Subtract multiple sets
        val SDIFFSTORE: String = "SDIFFSTORE" // Subtract multiple sets and store the resulting set in a key
        val SELECT: String = "SELECT" // Change the selected database for the current connection
        val SET: String = "SET" // Set the string value of a key
        val SETBIT: String = "SETBIT" // Sets or clears the bit at offset in the string value stored at key
        val SETEX: String = "SETEX" // Set the value and expiration of a key
        val SETNX: String = "SETNX" // Set the value of a key, only if the key does not exist
        val SETRANGE: String = "SETRANGE" // Overwrite part of a string at key starting at the specified offset
        val SHUTDOWN: String = "SHUTDOWN" // Synchronously save the dataset to disk and then shut down the server
        val SINTER: String = "SINTER" // Intersect multiple sets
        val SINTERSTORE: String = "SINTERSTORE" // Intersect multiple sets and store the resulting set in a key
        val SISMEMBER: String = "SISMEMBER" // Determine if a given value is a member of a set
        val SLAVEOF: String = "SLAVEOF" // Make the server a slave of another instance, or promote it as master
        val SLOWLOG: String = "SLOWLOG" // Manages the Redis slow queries log
        val SMEMBERS: String = "SMEMBERS" // Get all the members in a set
        val SMOVE: String = "SMOVE" // Move a member from one set to another
        val SORT: String = "SORT" // Sort the elements in a list, set or sorted set
        val SPOP: String = "SPOP" // Remove and return one or multiple random members from a set
        val SRANDMEMBER: String = "SRANDMEMBER" // Get one or multiple random members from a set
        val SREM: String = "SREM" // Remove one or more members from a set
        val STRLEN: String = "STRLEN" // Get the length of the value stored in a key
        val SUBSCRIBE: String = "SUBSCRIBE" // Listen for messages published to the given channels
        val SUNION: String = "SUNION" // Add multiple sets
        val SUNIONSTORE: String = "SUNIONSTORE" // Add multiple sets and store the resulting set in a key
        val SWAPDB: String = "SWAPDB" // Swaps two Redis databases
        val TIME: String = "TIME" // Return the current server time
        val TOUCH: String = "TOUCH" // Alters the last access time of a key(s). Returns the number of existing keys specified.
        val TTL: String = "TTL" // Get the time to live for a key
        val TYPE: String = "TYPE" // Determine the type stored at key
        val UNSUBSCRIBE: String = "UNSUBSCRIBE" // Stop listening for messages posted to the given channels
        val UNLINK: String = "UNLINK" // Delete a key asynchronously in another thread. Otherwise it is just as DEL, but non blocking.
        val UNWATCH: String = "UNWATCH" // Forget about all watched keys
        val WAIT: String = "WAIT" // Wait for the synchronous replication of all the write commands sent in the context of the current connection
        val WATCH: String = "WATCH" // Watch the given keys to determine execution of the MULTI/EXEC block
        val ZADD: String = "ZADD" // Add one or more members to a sorted set, or update its score if it already exists
        val ZCARD: String = "ZCARD" // Get the number of members in a sorted set
        val ZCOUNT: String = "ZCOUNT" // Count the members in a sorted set with scores within the given values
        val ZINCRBY: String = "ZINCRBY" // Increment the score of a member in a sorted set
        val ZINTERSTORE: String = "ZINTERSTORE" // Intersect multiple sorted sets and store the resulting sorted set in a new key
        val ZLEXCOUNT: String = "ZLEXCOUNT" // Count the number of members in a sorted set between a given lexicographical range
        val ZRANGE: String = "ZRANGE" // Return a range of members in a sorted set, by index
        val ZRANGEBYLEX: String = "ZRANGEBYLEX" // Return a range of members in a sorted set, by lexicographical range
        val ZREVRANGEBYLEX: String = "ZREVRANGEBYLEX" // Return a range of members in a sorted set, by lexicographical range, ordered from higher to lower strings.
        val ZRANGEBYSCORE: String = "ZRANGEBYSCORE" // Return a range of members in a sorted set, by score
        val ZRANK: String = "ZRANK" // Determine the index of a member in a sorted set
        val ZREM: String = "ZREM" // Remove one or more members from a sorted set
        val ZREMRANGEBYLEX: String = "ZREMRANGEBYLEX" // Remove all members in a sorted set between the given lexicographical range
        val ZREMRANGEBYRANK: String = "ZREMRANGEBYRANK" // Remove all members in a sorted set within the given indexes
        val ZREMRANGEBYSCORE: String = "ZREMRANGEBYSCORE" // Remove all members in a sorted set within the given scores
        val ZREVRANGE: String = "ZREVRANGE" // Return a range of members in a sorted set, by index, with scores ordered from high to low
        val ZREVRANGEBYSCORE: String = "ZREVRANGEBYSCORE" // Return a range of members in a sorted set, by score, with scores ordered from high to low
        val ZREVRANK: String = "ZREVRANK" // Determine the index of a member in a sorted set, with scores ordered from high to low
        val ZSCORE: String = "ZSCORE" // Get the score associated with the given member in a sorted set
        val ZUNIONSTORE: String = "ZUNIONSTORE" // Add multiple sorted sets and store the resulting sorted set in a new key
        val SCAN: String = "SCAN" // Incrementally iterate the keys space
        val SSCAN: String = "SSCAN" // Incrementally iterate Set elements
        val HSCAN: String = "HSCAN" // Incrementally iterate hash fields and associated values
        val ZSCAN: String = "ZSCAN" // Incrementally iterate sorted sets elements and associated scores
    }
}

fun main(args: Array<String>) {
    println("Hello! Command TEST.")
    val tst1 = cmdLPush("tstpush", "TEST")
    println("Command line -> ${String(tst1.cmd)}")
    val tst2 = cmdLPop("tstpop")
    println("Command line -> ${String(tst2.cmd)}")
}