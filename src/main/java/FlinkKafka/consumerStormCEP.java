package FlinkKafka;

import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import warning.StormWarning;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Properties;

import static FlinkKafka.KafkaProducer.pushDataToCEPIndex;

public class consumerStormCEP {
    public static void main(String[] args) throws IOException {
        Properties CEPConfig = new Properties();
        CEPConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        CEPConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "weatherStormCEP");
        CEPConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        CEPConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        CEPConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        CEPConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        KafkaConsumer<String, byte[]> consumer =
                new KafkaConsumer<>(CEPConfig);
        String topic = "stormcep";
        consumer.subscribe(Collections.singleton(topic));
        boolean CEP = true;
        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(1000);
            if (CEP) {
                consumer.seekToBeginning(consumer.assignment());
                CEP = false;
            }
            for (ConsumerRecord<String, byte[]> record : records) {
                String string = new String(record.value(), StandardCharsets.UTF_8);
                Gson gsonCEP = new Gson();
                StormWarning warning = gsonCEP.fromJson(string, StormWarning.class);
                pushDataToCEPIndex(topic, warning.getAvg(),
                        warning.getEvent1().getTimestamp(),
                        warning.getEvent2().getTimestamp());
            }
        }
    }
}
