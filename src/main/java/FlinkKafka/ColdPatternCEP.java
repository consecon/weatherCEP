package FlinkKafka;

import WeatherSerializer.FlinkWeatherEventDeserializerSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.WeatherEvent;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import warning.ExtremeColdWarning;


import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ColdPatternCEP {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");

        properties.setProperty("zookeeper.connect", "localhost:2181");
        properties.setProperty("group.id", "coldpattern");
        FlinkKafkaConsumer<WeatherEvent> consumer =
                new FlinkKafkaConsumer<>("weathersourcecsv", new FlinkWeatherEventDeserializerSchema(), properties);
        consumer.setStartFromEarliest();
        DataStream<WeatherEvent> stream = env.addSource(consumer);
        Pattern<WeatherEvent, ?> extremeCold = Pattern.<WeatherEvent>begin("first")
                .where(new SimpleCondition<WeatherEvent>() {
                    @Override
                    public boolean filter(WeatherEvent event) {
                        return event.getTemperature() <15;
                    }
                })
                .next("second")
                .where(new SimpleCondition<WeatherEvent>() {
                    @Override
                    public boolean filter(WeatherEvent event) {
                        return event.getTemperature() <25;
                    }
                });
        PatternStream<WeatherEvent> patternStream = CEP.pattern(stream, extremeCold);
        System.out.println("CEP pattern");
        DataStream<ExtremeColdWarning> coldWarningDataStream= patternStream.select(
                new PatternSelectFunction<WeatherEvent, ExtremeColdWarning>() {
                    @Override
                    public ExtremeColdWarning select(Map<String, List<WeatherEvent>> map) throws Exception {
                        return new ExtremeColdWarning(map.get("first").get(0), map.get("second").get(0));
                    }
                }
        );
        FlinkKafkaProducer<ExtremeColdWarning> myProducerColdCEP = new FlinkKafkaProducer<>(
                "localhost:9092",
                "coldcep",
                new SerializationSchema<ExtremeColdWarning>() {
                    @Override
                    public byte[] serialize(ExtremeColdWarning extremeColdWarning) {
                        ObjectMapper o = new ObjectMapper();
                        try {
                            return o.writeValueAsBytes(extremeColdWarning);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            System.out.println("error rain when serial");
                            return null;
                        }
                    }
                }
        );
        coldWarningDataStream.addSink(myProducerColdCEP);


        System.out.println("end");
        env.execute("rain pattern");
    }
}
