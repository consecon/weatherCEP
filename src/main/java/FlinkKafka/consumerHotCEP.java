package FlinkKafka;

import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import warning.ExtremeHotWarning;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Properties;

import static FlinkKafka.KafkaProducer.pushDataToCEPIndex;

public class consumerHotCEP {
    public static void main(String[] args) throws IOException {
        Properties CEPConfig = new Properties();
        CEPConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        CEPConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "weatherHotCEP");
        CEPConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        CEPConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        CEPConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        CEPConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        //hot CEP
        KafkaConsumer<String, byte[]> consumerHotCEP =
                new KafkaConsumer<>(CEPConfig);
        String hotCEPTopic = "hotcep";
        consumerHotCEP.subscribe(Collections.singleton(hotCEPTopic));
        boolean hotCEP = true;
        while (true) {
            ConsumerRecords<String, byte[]> records = consumerHotCEP.poll(1000);
            if (hotCEP) {
                consumerHotCEP.seekToBeginning(consumerHotCEP.assignment());
                hotCEP = false;
            }
            for (ConsumerRecord<String, byte[]> record : records) {
                String string = new String(record.value(), StandardCharsets.UTF_8);
                Gson gsonCEP = new Gson();
                ExtremeHotWarning extremeHotWarning = gsonCEP.fromJson(string, ExtremeHotWarning.class);
                pushDataToCEPIndex(hotCEPTopic,
                        (extremeHotWarning.getEvent1().getTemperature()+extremeHotWarning.getEvent2().getTemperature())/2,
                        extremeHotWarning.getEvent1().getTimestamp(),
                        extremeHotWarning.getEvent2().getTimestamp());
            }
        }
    }

}
