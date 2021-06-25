package joliex.redis;


import jolie.runtime.JavaService;
import jolie.runtime.Value;
import jolie.runtime.embedding.RequestResponse;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.Set;


public class RedisConnector  extends JavaService {

    private static Jedis jedis;
    private static JedisCluster jedisCluster;
    private static boolean hasCluster = false;

    public RedisConnector (String host , int port){
        jedis = new Jedis(new HostAndPort(host , port));
    }
    public RedisConnector(){
        super();
    }

    @RequestResponse
    public Value connect(Value request) {
        Value response = Value.create();
        if (request.getFirstChild("cluster").boolValue()) {
            Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();

            request.getChildren("locations").forEach(
                    value -> {
                        jedisClusterNodes.add(new HostAndPort(value.getFirstChild("host").strValue(), value.getFirstChild("port").intValue()));
                    }
            );
            jedisCluster = new JedisCluster(jedisClusterNodes);

        } else {
            jedis = new Jedis(new HostAndPort(request.getFirstChild("locations").getFirstChild("host").strValue(), request.getFirstChild("locations").getFirstChild("port").intValue()));
        }
        hasCluster = request.getFirstChild("cluster").boolValue();
        return response;
    }

    @RequestResponse
    public Value writeStringOnCache(Value request) {
        Value response = Value.create();
        Transaction transaction;
        if (hasCluster) {

            jedisCluster.set(request.getFirstChild("key").strValue(), request.getFirstChild("value").strValue());
        } else {
            transaction = jedis.multi();
            transaction.set(request.getFirstChild("key").strValue(), request.getFirstChild("value").strValue());
            transaction.exec();
            transaction.close();
        }
        return response;
    }

    @RequestResponse
    public Value readStringFromCache(Value request) {
        Value response = Value.create();
        if (hasCluster) {
            response.setValue(jedisCluster.get(request.getFirstChild("key").strValue()));

        } else {
            response.setValue(jedis.get(request.getFirstChild("key").strValue()));
        }
        return response;
    }

    @RequestResponse
    public Value pushStringIntoList(Value request) {
        Value response = Value.create();


        if (hasCluster) {
            if (request.getFirstChild("direction").strValue().equalsIgnoreCase("R")){
                response.setValue(jedisCluster.rpush(request.getFirstChild("key").strValue(), request.getFirstChild("value").strValue()).longValue());
            }
            if (request.getFirstChild("direction").strValue().equalsIgnoreCase("L")) {
                response.setValue(jedisCluster.lpush(request.getFirstChild("key").strValue(), request.getFirstChild("value").strValue()).longValue());
            }

        } else {

            if (request.getFirstChild("direction").strValue().equalsIgnoreCase("R")) {
                response.setValue(jedis.rpush(request.getFirstChild("key").strValue(), request.getFirstChild("value").strValue()).longValue());
            }
            if (request.getFirstChild("direction").strValue().equalsIgnoreCase("L")) {
                response.setValue(jedis.lpush(request.getFirstChild("key").strValue(), request.getFirstChild("value").strValue()).longValue());
            }


        }
        return response;
    }


    @RequestResponse
    public Value popStringFromList(Value request) {
        Value response = Value.create();
        if (hasCluster) {
            if (request.getFirstChild("direction").strValue().equalsIgnoreCase("R")) {
                response.setValue(jedisCluster.rpop(request.getFirstChild("key").strValue()));
            }
            if (request.getFirstChild("direction").strValue().equalsIgnoreCase("L")) {
                response.setValue(jedisCluster.lpop(request.getFirstChild("key").strValue()));
            }

        } else {
            if (request.getFirstChild("direction").strValue().equalsIgnoreCase("R")) {
                response.setValue(jedis.rpop(request.getFirstChild("key").strValue()));
            }

            if (request.getFirstChild("direction").strValue().equalsIgnoreCase("L")) {
                response.setValue(jedis.lpop(request.getFirstChild("key").strValue()));
            }
        }

        return response;
    }

    @RequestResponse
    public Value addToSet(Value request){
         Value response = Value.create();
        if (hasCluster) {
            request.getChildren("values").forEach(value -> {
                jedisCluster.sadd(request.getFirstChild("set").strValue(),
                        value.strValue());
            });

        }else{

            if (request.hasChildren("transaction") & request.getFirstChild("transaction").boolValue() ){
                Transaction t ;
                t = jedis.multi();
                request.getChildren("values").forEach(value -> {
                    t.sadd(request.getFirstChild("set").strValue(),
                           value.strValue());
                });
                t.exec();
            }else{
                request.getChildren("values").forEach(value -> {
                    jedis.sadd(request.getFirstChild("set").strValue(),
                            value.strValue());
                });
            }

        }
      return response;
    }

    @RequestResponse
    public Value getSet(Value request) {
        Value response = Value.create();

        if (hasCluster) {

            Set<String> set = jedisCluster.smembers(request.getFirstChild("set").strValue());
            set.forEach(s -> {
                response.getChildren("values").add(Value.create(s));
            });


        } else {

            if (request.hasChildren("transaction") & request.getFirstChild("transaction").boolValue()) {
                Transaction t;
                t = jedis.multi();
                Response<Set<String>> responseSet = t.smembers(request.getFirstChild("set").strValue());
                responseSet.get().forEach(s -> {
                    response.getChildren("values").add(Value.create(s));
                });
                t.exec();
            } else {
                Set<String> set = jedis.smembers(request.getFirstChild("set").strValue());
                set.forEach(s -> {
                    response.getChildren("values").add(Value.create(s));
                });
            }

        }
        return response;

    }

    @RequestResponse
    public Value setHash (Value request){
        Value response = Value.create();
        if (hasCluster) {
            request.getChildren("values").forEach(value -> {
                jedisCluster.hset(request.getFirstChild("hash").strValue(),
                                   value.getFirstChild("key").strValue(),
                                   value.getFirstChild("value").strValue() );
            });

        }else{

            if (request.hasChildren("transaction") & request.getFirstChild("transaction").boolValue() ){
                Transaction t ;
                t = jedis.multi();
                request.getChildren("values").forEach(value -> {
                    t.hset(request.getFirstChild("hash").strValue(),
                            value.getFirstChild("key").strValue(),
                            value.getFirstChild("value").strValue() );
                });
                t.exec();
            }else{
                request.getChildren("values").forEach(value -> {
                    jedis.hset(request.getFirstChild("hash").strValue(),
                            value.getFirstChild("key").strValue(),
                            value.getFirstChild("value").strValue() );
                });
            }

        }
        return response;


    }


    @RequestResponse
    public Value getHash (Value request){
        Value response = Value.create();
        if (hasCluster) {
            if (request.hasChildren("key")){
                jedisCluster
            }
            jedisCluster.hget()
            request.getChildren("values").forEach(value -> {
                jedisCluster.hset(request.getFirstChild("hash").strValue(),
                        value.getFirstChild("key").strValue(),
                        value.getFirstChild("value").strValue() );
            });

        }else{

            if (request.hasChildren("transaction") & request.getFirstChild("transaction").boolValue() ){
                Transaction t ;
                t = jedis.multi();
                request.getChildren("values").forEach(value -> {
                    t.hset(request.getFirstChild("hash").strValue(),
                            value.getFirstChild("key").strValue(),
                            value.getFirstChild("value").strValue() );
                });
                t.exec();
            }else{
                request.getChildren("values").forEach(value -> {
                    jedis.hset(request.getFirstChild("hash").strValue(),
                            value.getFirstChild("key").strValue(),
                            value.getFirstChild("value").strValue() );
                });
            }

        }
        return response;


    }









}
