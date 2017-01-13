package redis.client

import redis.protocol.RedisProtocol
import java.io.ByteArrayOutputStream

// https://redis.io/topics/data-types-intro
// !!! Redis keys are binary safe, this means that you can use any binary sequence as a key

enum class InsertType {
    BEFORE, AFTER
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

private fun singleCommand(cmdName: String): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 1 // komanda
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(cmdName)
        baos.toByteArray()
    }
    return Command(cmdName, cmd)
}

private fun oneParamCommand(cmdName: String, param: ByteArray): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 2 // komanda + param
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(cmdName)
        baos.writeAsBulkString(param)
        baos.toByteArray()
    }
    return Command(cmdName, cmd)
}

private fun twoParamCommand(cmdName: String, param1: ByteArray, param2: ByteArray): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 3 // komanda + param
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(cmdName)
        baos.writeAsBulkString(param1)
        baos.writeAsBulkString(param2)
        baos.toByteArray()
    }
    return Command(cmdName, cmd)
}

/**
 * APPEND key value
 * Available since 2.0.0.
 * Return value
 *  Integer reply: the length of the string after the append operation.
 */
public fun cmdAppend(key: String, value: String): Command = cmdAppend(key.toByteArray(Charsets.UTF_8), value.toByteArray(Charsets.UTF_8))
public fun cmdAppend(key: ByteArray, value: ByteArray): Command = twoParamCommand(Command.APPEND, key, value)

/**
 * AUTH password
 * Available since 1.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdAuth(password0: String): Command = oneParamCommand(Command.AUTH, password0.toByteArray(Charsets.UTF_8))

/**
 * DBSIZE
 * Available since 1.0.0.
 * Return value
 *  Integer reply
 */
public fun cmdDBSize(): Command = singleCommand(Command.DBSIZE)

/**
 * DEL key [key ...]
 * Available since 1.0.0.
 * Return value:
 *  Integer reply: The number of keys that were removed.
 */
public fun cmdDel(key: ByteArray): Command = oneParamCommand(Command.DEL, key)
public fun cmdDel(vararg keys: String): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 1 + keys.size // komanda + keys.size
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(Command.DEL)
        baos.writeAsBulkString(*keys)
        baos.toByteArray()
    }
    return Command(Command.DEL, cmd)
}

/**
 * ECHO message
 * Available since 1.0.0.
 * Return value
 *  Bulk string reply
 */
public fun cmdEcho(msg: String): Command = oneParamCommand(Command.ECHO, msg.toByteArray(Charsets.UTF_8))

/**
 * EXISTS key [key ...]
 * Available since 1.0.0.
 * Return value
 *  Integer reply, specifically:
 *  1 if the key exists.
 *  0 if the key does not exist.
 */
public fun cmdExists(key: ByteArray): Command = oneParamCommand(Command.EXISTS, key)
public fun cmdExists(vararg keys: String): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 1 + keys.size // komanda + keys.size
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(Command.EXISTS)
        baos.writeAsBulkString(*keys)
        baos.toByteArray()
    }
    return Command(Command.EXISTS, cmd)
}

/**
 * LINDEX key index
 * Available since 1.0.0.
 * Return value
 *  Bulk string reply: the requested element, or nil when index is out of range.
 */
public fun cmdLIndex(key: String, index: Int): Command = cmdLIndex(key.toByteArray(Charsets.UTF_8), index)
public fun cmdLIndex(key: ByteArray, index: Int): Command = twoParamCommand(Command.LINDEX, key, index.toString().toByteArray(Charsets.UTF_8))

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
public fun cmdLLen(key: ByteArray): Command = oneParamCommand(Command.LLEN, key)

/**
 * LPOP key
 * Available since 1.0.0.
 * Return value
 *  Bulk string reply
 */
public fun cmdLPop(key: String): Command = oneParamCommand(Command.LPOP, key.toByteArray(Charsets.UTF_8))
public fun cmdLPop(key: ByteArray): Command = oneParamCommand(Command.LPOP, key)

/**
 * LPUSH key value [value ...]
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the length of the list after the push operations.
 */
public fun cmdLPush(key: String, value: ByteArray): Command = cmdLPush(key.toByteArray(Charsets.UTF_8), value)
public fun cmdLPush(key: String, vararg values: String): Command = cmdLPush(key.toByteArray(Charsets.UTF_8), *values)
public fun cmdLPush(key: ByteArray, value: ByteArray): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 3 // komanda + key + value
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(Command.LPUSH)
        baos.writeAsBulkString(key)
        baos.writeAsBulkString(value)
        baos.toByteArray()
    }
    return Command(Command.LPUSH, cmd)
}
public fun cmdLPush(key: ByteArray, vararg values: String): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 2 + values.size // komanda + key + values.size
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(Command.LPUSH)
        baos.writeAsBulkString(key)
        baos.writeAsBulkString(*values)
        baos.toByteArray()
    }
    return Command(Command.LPUSH, cmd)
}

/**
 * LPUSHX key value
 * Available since 2.2.0.
 * Return value
 *  Integer reply: the length of the list after the push operation.
 */
public fun cmdLPushX(key: String, value: ByteArray): Command = cmdLPushX(key.toByteArray(Charsets.UTF_8), value)
public fun cmdLPushX(key: String, value: String): Command = cmdLPushX(key.toByteArray(Charsets.UTF_8), value.toByteArray(Charsets.UTF_8))
public fun cmdLPushX(key: ByteArray, value: String): Command = cmdLPushX(key, value.toByteArray(Charsets.UTF_8))
public fun cmdLPushX(key: ByteArray, value: ByteArray): Command = twoParamCommand(Command.LPUSHX, key, value)

/**
 * LRANGE key start stop 
 * Available since 1.0.0.
 * Return value
 *  Array reply: list of elements in the specified range.
 */
public fun cmdLRange(key: String, start: Int, stop: Int): Command = cmdLRange(key.toByteArray(Charsets.UTF_8), start, stop)
public fun cmdLRange(key: ByteArray, start: Int, stop: Int): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 4 // komanda + key + start + stop
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(Command.LRANGE)
        baos.writeAsBulkString(key)
        baos.writeAsBulkString(start)
        baos.writeAsBulkString(stop)
        baos.toByteArray()
    }
    return Command(Command.LRANGE, cmd)
}

/**
 * LREM key count value
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the number of removed elements.
 */
public fun cmdLRem(key: String, count: Int, value: String): Command = cmdLRem(key.toByteArray(Charsets.UTF_8), count, value.toByteArray(Charsets.UTF_8))
public fun cmdLRem(key: ByteArray, count: Int, value: String): Command = cmdLRem(key, count, value.toByteArray(Charsets.UTF_8))
public fun cmdLRem(key: ByteArray, count: Int, value: ByteArray): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 4 // komanda + key + count + value
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(Command.LREM)
        baos.writeAsBulkString(key)
        baos.writeAsBulkString(count)
        baos.writeAsBulkString(value)
        baos.toByteArray()
    }
    return Command(Command.LREM, cmd)
}

/**
 * LSET key index value 
 * Available since 1.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdLSet(key: String, index: Int, value: String): Command = cmdLSet(key.toByteArray(Charsets.UTF_8), index, value.toByteArray(Charsets.UTF_8))
public fun cmdLSet(key: ByteArray, index: Int, value: String): Command = cmdLSet(key, index, value.toByteArray(Charsets.UTF_8))
public fun cmdLSet(key: ByteArray, index: Int, value: ByteArray): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 4 // komanda + key + index + value
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(Command.LSET)
        baos.writeAsBulkString(key)
        baos.writeAsBulkString(index)
        baos.writeAsBulkString(value)
        baos.toByteArray()
    }
    return Command(Command.LSET, cmd)
}

/**
 * LTRIM key start stop 
 * Available since 1.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdLTrim(key: String, start: Int, stop: Int): Command = cmdLTrim(key.toByteArray(Charsets.UTF_8), start, stop)
public fun cmdLTrim(key: ByteArray, start: Int, stop: Int): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 4 // komanda + key + start + stop
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(Command.LTRIM)
        baos.writeAsBulkString(key)
        baos.writeAsBulkString(start)
        baos.writeAsBulkString(stop)
        baos.toByteArray()
    }
    return Command(Command.LTRIM, cmd)
}

/**
 * MOVE key db
 * Available since 1.0.0.
 * Return value
 *  Integer reply, specifically:
 *  1 if key was moved.
 *  0 if key was not moved.
 */
public fun cmdMove(key: ByteArray, db: Int): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 3 // komanda + key + db
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(Command.MOVE)
        baos.writeAsBulkString(key)
        baos.writeAsBulkString(db)
        baos.toByteArray()
    }
    return Command(Command.MOVE, cmd)
}

/**
 * PING [message]
 * Available since 1.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdPing(): Command = singleCommand(Command.PING)
public fun cmdPing(msg: String): Command = oneParamCommand(Command.PING, msg.toByteArray(Charsets.UTF_8))

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
 * RENAME key newkey
 * Available since 1.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdRename(key: String, newkey: String): Command = cmdRename(key.toByteArray(Charsets.UTF_8), newkey.toByteArray(Charsets.UTF_8))
public fun cmdRename(key: ByteArray, newkey: ByteArray): Command = twoParamCommand(Command.RENAME, key, newkey)

/**
 * RENAMENX key newkey 
 * Available since 1.0.0.
 * Return value
 *  Integer reply, specifically:
 *  1 if key was renamed to newkey.
 *  0 if newkey already exists.
 */
public fun cmdRenameNx(key: String, newkey: String): Command = cmdRenameNx(key.toByteArray(Charsets.UTF_8), newkey.toByteArray(Charsets.UTF_8))
public fun cmdRenameNx(key: ByteArray, newkey: ByteArray): Command = twoParamCommand(Command.RENAMENX, key, newkey)

/**
 * RPOP key 
 * Available since 1.0.0.
 * Return value
 *  Bulk string reply: the value of the last element, or nil when key does not exist.
 */
public fun cmdRPop(key: String): Command = oneParamCommand(Command.RPOP, key.toByteArray(Charsets.UTF_8))
public fun cmdRPop(key: ByteArray): Command = oneParamCommand(Command.RPOP, key)

/**
 * RPOPLPUSH source destination
 * Available since 1.2.0.
 * Return value
 *  Bulk string reply: the element being popped and pushed.
 */
public fun cmdRPopLPush(source: String, destination: String): Command = cmdRPopLPush(source.toByteArray(Charsets.UTF_8), destination.toByteArray(Charsets.UTF_8))
public fun cmdRPopLPush(source: ByteArray, destination: ByteArray): Command = twoParamCommand(Command.RPOPLPUSH, source, destination)

/**
 * RPUSH key value [value ...] 
 * Available since 1.0.0.
 * Return value
 *  Integer reply: the length of the list after the push operation.
 */
public fun cmdRPush(key: String, value: ByteArray): Command = cmdRPush(key.toByteArray(Charsets.UTF_8), value)
public fun cmdRPush(key: String, vararg values: String): Command = cmdRPush(key.toByteArray(Charsets.UTF_8), *values)
public fun cmdRPush(key: ByteArray, value: ByteArray): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 3 // komanda + key + value
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(Command.RPUSH)
        baos.writeAsBulkString(key)
        baos.writeAsBulkString(value)
        baos.toByteArray()
    }
    return Command(Command.RPUSH, cmd)
}
public fun cmdRPush(key: ByteArray, vararg values: String): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 2 + values.size // komanda + key + values.size
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(Command.RPUSH)
        baos.writeAsBulkString(key)
        baos.writeAsBulkString(*values)
        baos.toByteArray()
    }
    return Command(Command.RPUSH, cmd)
}

/**
 * RPUSHX key value 
 * Available since 2.2.0.
 * Return value
 *  Integer reply: the length of the list after the push operation.
 */
public fun cmdRPushX(key: String, value: ByteArray): Command = cmdRPushX(key.toByteArray(Charsets.UTF_8), value)
public fun cmdRPushX(key: String, value: String): Command = cmdRPushX(key.toByteArray(Charsets.UTF_8), value.toByteArray(Charsets.UTF_8))
public fun cmdRPushX(key: ByteArray, value: String): Command = cmdRPushX(key, value.toByteArray(Charsets.UTF_8))
public fun cmdRPushX(key: ByteArray, value: ByteArray): Command = twoParamCommand(Command.RPUSHX, key, value)
 
/**
 * SELECT index
 * Available since 1.0.0.
 * Return value
 *  Simple string reply
 */
public fun cmdSelect(index: Int): Command = oneParamCommand(Command.SELECT, index.toString().toByteArray(Charsets.UTF_8))

/**
 * STRLEN key 
 * Available since 2.2.0.
 * Return value
 *  Integer reply: the length of the string at key, or 0 when key does not exist.
 */
public fun cmdStrLen(key: String): Command = oneParamCommand(Command.STRLEN, key.toByteArray(Charsets.UTF_8))
public fun cmdStrLen(key: ByteArray): Command = oneParamCommand(Command.STRLEN, key)
 
/**
 * SWAPDB index index
 * Available since 4.0.0.
 * Return value
 *  Simple string reply: OK
 */
public fun cmdSwapDB(index1: Int, index2: Int): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 3 // komanda + index1 + index2
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(Command.SWAPDB)
        baos.writeAsBulkString(index1)
        baos.writeAsBulkString(index2)
        baos.toByteArray()
    }
    return Command(Command.SWAPDB, cmd)
}

/**
 * TOUCH key [key ...] 
 * Available since 3.2.1.
 * Return value
 *  Integer reply: The number of keys that were touched.
 */
public fun cmdTouch(key: ByteArray): Command = oneParamCommand(Command.TOUCH, key)
public fun cmdTouch(vararg keys: String): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 1 + keys.size // komanda + keys.size
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(Command.TOUCH)
        baos.writeAsBulkString(*keys)
        baos.toByteArray()
    }
    return Command(Command.TOUCH, cmd)
}

/**
 * UNLINK key [key ...] 
 * Available since 4.0.0.
 * Return value
 *  Integer reply: The number of keys that were unlinked.
 */
public fun cmdUnLink(key: ByteArray): Command = oneParamCommand(Command.UNLINK, key)
public fun cmdUnLink(vararg keys: String): Command {
    val baos = ByteArrayOutputStream()
    val cmd = baos.use {
        val size = 1 + keys.size // komanda + keys.size
        baos.writeAsArrayStart(size)
        baos.writeAsBulkString(Command.UNLINK)
        baos.writeAsBulkString(*keys)
        baos.toByteArray()
    }
    return Command(Command.UNLINK, cmd)
}

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