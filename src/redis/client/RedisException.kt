package redis.client

/**
 * RedisException class
 * 
 * User: sibircevs
 * Date: 04.01.2017
 */
class RedisException : RuntimeException {
    constructor(message: String, ex: Exception?) : super(message, ex) {}
    constructor(message: String) : super(message) {}
    constructor(ex: Exception) : super(ex) {}
}