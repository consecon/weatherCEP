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
import warning.ExtremeHotWarning;
import warning.StormWarning;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class StormPatternCEP {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");

        properties.setProperty("zookeeper.connect", "localhost:2181");
        properties.setProperty("group.id", "stormpattern");
        FlinkKafkaConsumer<WeatherEvent> consumer =
                new FlinkKafkaConsumer<>("weathersourcecsv", new FlinkWeatherEventDeserializerSchema(), properties);
        consumer.setStartFromEarliest();
        DataStream<WeatherEvent> stream = env.addSource(consumer);


        Pattern<WeatherEvent, ?> storm = Pattern.<WeatherEvent>begin("first")
                .where(new SimpleCondition<WeatherEvent>() {
                    @Override
                    public boolean filter(WeatherEvent event) {
                        return event.getAtmosphere() < 750;
                    }
                })
                //750
                .next("second")
                .where(new SimpleCondition<WeatherEvent>() {
                    @Override
                    public boolean filter(WeatherEvent event) {
                        return event.getTemperature() < 750;
                    }
                });
        PatternStream<WeatherEvent> patternStreamStorm = CEP.pattern(stream, storm);
        DataStream<StormWarning> stormWarning =
                patternStreamStorm.select(new PatternSelectFunction<WeatherEvent, StormWarning>() {
                    @Override
                    public StormWarning select(Map<String, List<WeatherEvent>> map) throws Exception {
                        return new StormWarning(map.get("first").get(0), map.get("second").get(0));
                    }
                });
        stormWarning.print();
        FlinkKafkaProducer<StormWarning> myProducerStormCEP = new FlinkKafkaProducer<>(
                "localhost:9092",
                "stormcep",
                new SerializationSchema<StormWarning>() {
                    @Override
                    public byte[] serialize(StormWarning stormWarning) {
                        ObjectMapper o = new ObjectMapper();
                        try {
                            return o.writeValueAsBytes(stormWarning);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            System.out.println("error storm when serial");
                            return null;
                        }
                    }
                }
        );
        stormWarning.addSink(myProducerStormCEP);

        System.out.println("end");
        env.execute("test");
    }
}
