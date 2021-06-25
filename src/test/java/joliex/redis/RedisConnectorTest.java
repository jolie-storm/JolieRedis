package joliex.redis;

import jolie.runtime.CanUseJars;
import jolie.runtime.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@CanUseJars({"jedis-3.6.0.jar"})
public class RedisConnectorTest {
    private RedisConnector redisConnector = new RedisConnector("localhost",6379);
    @Test
    @Order(1)
    public void testWriteStringsOnCache() {
        Value request = Value.create();
        request.getFirstChild("key").setValue("testWriteStringsOnCache()");
        request.getFirstChild("value").setValue("I am testing writeStringOnCache");
        redisConnector.writeStringOnCache(request);
    }
    @Test
    @Order(2)
    public void testReadStringFromCache() {
        Value request = Value.create();
        request.getFirstChild("key").setValue("testWriteStringsOnCache()");
        Assertions.assertTrue (redisConnector.readStringFromCache(request).strValue().equals("I am testing writeStringOnCache"));

    }
    @Test
    @Order(3)
    public void testPushStringIntoList() {
        Value request = Value.create();
        request.getFirstChild("key").setValue("list1");
        request.getFirstChild("direction").setValue("L");
        request.getFirstChild("value").setValue("valueL0");
        redisConnector.pushStringIntoList(request);
        request.getFirstChild("value").setValue("valueL1");
        redisConnector.pushStringIntoList(request);
    }
    @Test
    @Order(4)
    public void testPopStringFromList() {
        Value request = Value.create();
        request.getFirstChild("key").setValue("list1");
        request.getFirstChild("direction").setValue("R");
        Assertions.assertTrue(redisConnector.popStringFromList(request).strValue().equals("valueL0"));
    }

    @Test
    @Order(5)
    void addToSet() {
        Value request = Value.create();
        request.getFirstChild("set").setValue("set1");
        request.getChildren("values").add(Value.create("value1"));
        request.getChildren("values").add(Value.create("value2"));
        request.getChildren("values").add(Value.create("value3"));
        redisConnector.addToSet(request);
    }

    @Test
    @Order(6)
    void getSet() {
        Value request = Value.create();
        request.getFirstChild("set").setValue("set1");
        Assertions.assertEquals(3 ,redisConnector.getSet(request).getChildren("values").size());
    }
}