package com.nutsmobi.ads.log.processor;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;

import java.util.Properties;

/**
 * KafkaSink
 * 自定义kafka sink,连接kafka所用
 * Kafka Topic: log-topic
 * <p/>
 * Author: Noprom <tyee.noprom@qq.com>
 * Date: 16/3/11 下午9:16.
 */
public class KafkaSink extends AbstractSink implements Configurable {
    private static final Log logger = LogFactory.getLog(KafkaSink.class);

    private String topic;
    private String brokerList;
    private String zookeeperServer;
    private Producer<String, String> producer;

    public void configure(Context context) {
        //topic = "log-topic";
        //"hk01:9092,hk02:9092,hk03:9092,hk04:9092"
        //"hk01:2181,hk02:2181,hk03:2181,hk04:2181/kafka"
        topic = context.getString("topic", "log-topic");
        brokerList = context.getString("brokerList", "localhost:9092, localhost:9093, localhost:9094");
        zookeeperServer = context.getString("zookeeperServer", "localhost:2181");

        Properties props = new Properties();
        props.setProperty("metadata.broker.list", brokerList);
        props.setProperty("serializer.class", "kafka.serializer.StringEncoder");
        //props.put("partitioner.class", "idoall.cloud.kafka.Partitionertest");
        //props.setProperty("num.partitions", "4"); //这里需要自定义partition
        //props.put("zookeeper.connect", "hk01:2181,hk02:2181,hk03:2181,hk04:2181/kafka");
        props.put("zookeeper.connect", zookeeperServer);
        //props.put("advertised.host.name", "hk01");
        props.put("request.required.acks", "1");
        ProducerConfig config = new ProducerConfig(props);
        producer = new Producer<String, String>(config);
        logger.info("KafkaSink init finished.");
    }

    public Status process() throws EventDeliveryException {
        Channel channel = getChannel();
        Transaction tx = channel.getTransaction();
        try {
            tx.begin();
            Event e = channel.take();
            if (e == null) {
                tx.rollback();
                return Status.BACKOFF;
            }

            KeyedMessage<String, String> data = new KeyedMessage<String, String>(topic, new String(e.getBody()));
            producer.send(data);
            logger.info("flume send data to kafka: " + new String(e.getBody()));
            tx.commit();
            return Status.READY;
        } catch (Exception e) {
            logger.error("Flume KafkaSinkException:", e);
            tx.rollback();
            return Status.BACKOFF;
        } finally {
            tx.close();
        }
    }
}