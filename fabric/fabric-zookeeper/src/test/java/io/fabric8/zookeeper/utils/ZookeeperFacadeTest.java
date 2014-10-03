package io.fabric8.zookeeper.utils;

import io.fabric8.common.util.json.JsonWriter;
import org.apache.curator.framework.CuratorFramework;

import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.zookeeper.data.Stat;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Test;

public class ZookeeperFacadeTest {

    private CuratorFramework curator;

    @Test
    public void thatArrayOfMapsIsReturned() throws Exception {
        curator = mock(CuratorFramework.class);
        ExistsBuilder existsBuilder = mock(ExistsBuilder.class);
        GetChildrenBuilder childrenBuilder = mock(GetChildrenBuilder.class);
        GetDataBuilder dataBuilder = mock(GetDataBuilder.class);
        ZooKeeperFacade zookeeperFacade = new ZooKeeperFacade(curator);


        ArrayList<String> services = new ArrayList<>();
        services.add("tcp://${zk:brokers/ip}:54391");
        services.add("mqtt://${zk:brokers/ip}:60023");
        services.add("amqp://${zk:brokers/ip}:58377");
        services.add("stomp://${zk:brokers/ip}:55701");

        Map<String,Object> slave = new HashMap<>();

        slave.put("id", "mybroker");
        slave.put("container", "brokers2");
        slave.put("services", Collections.emptyList());

        Map<String,Object> master = new HashMap<>();
        master.put("id","mybroker");
        master.put("container", "brokers");
        master.put("services", services);

        when(curator.checkExists()).thenReturn(existsBuilder);
        when(existsBuilder.forPath(anyString())).thenReturn(new Stat());
        when(curator.getChildren()).thenReturn(childrenBuilder);
        when(curator.getData()).thenReturn(dataBuilder);
//        when(childrenBuilder.forPath(anyString()))
//                .thenReturn(Collections.<String>emptyList());
        when(childrenBuilder.forPath("/fabric/registry/cluster/amq/default/00000"))
                .thenReturn(Collections.<String>emptyList());
        when(childrenBuilder.forPath("/fabric/registry/cluster/amq/default/00001"))
                .thenReturn(Collections.<String>emptyList());
        when(childrenBuilder.forPath("/fabric/registry/cluster/amq/default"))
                .thenReturn(Arrays.asList("00000", "00001"));


        StringWriter slaveWriter = new StringWriter();
        JsonWriter.write(slaveWriter,slave);

        StringWriter masterWriter = new StringWriter();
        JsonWriter.write(masterWriter,master);

        when(dataBuilder.forPath("/fabric/registry/cluster/amq/default/00000"))
                .thenReturn(slaveWriter.toString().getBytes());

        when(dataBuilder.forPath("/fabric/registry/cluster/amq/default/00001"))
                .thenReturn(masterWriter.toString().getBytes());



        ArrayList<Map<String,Object>> descendants = new ArrayList<>();
        descendants.add(slave);
        descendants.add(master);

        Object result =
                zookeeperFacade.matchingDescendantStringDataAsObject(
                        "/fabric/registry/cluster/amq/default");


        assertEquals(descendants,result);
    }


    @Test
    public void thatArrayOfServicesIsReturned() throws Exception {
        curator = mock(CuratorFramework.class);
        ExistsBuilder existsBuilder = mock(ExistsBuilder.class);
        GetChildrenBuilder childrenBuilder = mock(GetChildrenBuilder.class);
        GetDataBuilder dataBuilder = mock(GetDataBuilder.class);
        ZooKeeperFacade zookeeperFacade = new ZooKeeperFacade(curator);


        ArrayList<String> services = new ArrayList<>();
        services.add("tcp://${zk:brokers/ip}:54391");
        services.add("mqtt://${zk:brokers/ip}:60023");
        services.add("amqp://${zk:brokers/ip}:58377");
        services.add("stomp://${zk:brokers/ip}:55701");

        Map<String,Object> slave = new HashMap<>();

        slave.put("id", "mybroker");
        slave.put("container", "brokers2");
        slave.put("services", Collections.emptyList());

        Map<String,Object> master = new HashMap<>();
        master.put("id","mybroker");
        master.put("container", "brokers");
        master.put("services", services);

        when(curator.checkExists()).thenReturn(existsBuilder);
        when(existsBuilder.forPath(anyString())).thenReturn(new Stat());
        when(curator.getChildren()).thenReturn(childrenBuilder);
        when(curator.getData()).thenReturn(dataBuilder);
//        when(childrenBuilder.forPath(anyString()))
//                .thenReturn(Collections.<String>emptyList());
        when(childrenBuilder.forPath("/fabric/registry/cluster/amq/default/00000"))
                .thenReturn(Collections.<String>emptyList());
        when(childrenBuilder.forPath("/fabric/registry/cluster/amq/default/00001"))
                .thenReturn(Collections.<String>emptyList());
        when(childrenBuilder.forPath("/fabric/registry/cluster/amq/default"))
                .thenReturn(Arrays.asList("00000", "00001"));


        StringWriter slaveWriter = new StringWriter();
        JsonWriter.write(slaveWriter,slave);

        StringWriter masterWriter = new StringWriter();
        JsonWriter.write(masterWriter,master);

        when(dataBuilder.forPath("/fabric/registry/cluster/amq/default/00000"))
                .thenReturn(slaveWriter.toString().getBytes());

        when(dataBuilder.forPath("/fabric/registry/cluster/amq/default/00001"))
                .thenReturn(masterWriter.toString().getBytes());



        ArrayList<Map<String,Object>> descendants = new ArrayList<>();
        descendants.add(slave);
        descendants.add(master);

        Object result =
                zookeeperFacade.extractDescendantStringDataAsObject(
                        "/fabric/registry/cluster/amq/default","$..services[*]");
        System.out.println(result);
        assertEquals(services,result);
    }

}
