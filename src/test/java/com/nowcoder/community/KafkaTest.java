package com.nowcoder.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTest {
    @Autowired
    private KafaKaProducer kafaKaProducer;

    @Test
    public void testKafka() {
        kafaKaProducer.send("test", "你好");
        kafaKaProducer.send("test", "我是kafka");
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}

@Component
class KafaKaProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void send(String topic, String content) {
        kafkaTemplate.send(topic, content);
    }

}

@Component
class KafakaConsumer {
    @KafkaListener(topics = "test")
    public void handleMessage(ConsumerRecord record) {
        System.out.println(record.value());
    }
}