package FlinkKafka;


import WeatherSerializer.FlinkExtremeHotWarningSchema;
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


import java.util.List;
import java.util.Map;
import java.util.Properties;

public class HotPatternCEP {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");

        properties.setProperty("zookeeper.connect", "localhost:2181");
        properties.setProperty("group.id", "hotpattern");
        FlinkKafkaConsumer<WeatherEvent> consumer =
                new FlinkKafkaConsumer<>("weathersourcecsv", new FlinkWeatherEventDeserializerSchema(), properties);
        consumer.setStartFromEarliest();
        DataStream<WeatherEvent> stream = env.addSource(consumer);
        Pattern<WeatherEvent, ?> extremeHot = Pattern.<WeatherEvent>begin("first")
                .where(new SimpleCondition<WeatherEvent>() {
                    @Override
                    public boolean filter(WeatherEvent event) {
                        return event.getTemperature() > 35;
                    }
                })
                .next("second")
                .where(new SimpleCondition<WeatherEvent>() {
                    @Override
                    public boolean filter(WeatherEvent event) {
                        return event.getTemperature() > 35;
                    }
                });
        PatternStream<WeatherEvent> patternStream = CEP.pattern(stream, extremeHot);
        System.out.println("CEP pattern");
        DataStream<ExtremeHotWarning> warnings = patternStream.select(
                new PatternSelectFunction<WeatherEvent, ExtremeHotWarning>() {
                    @Override
                    public ExtremeHotWarning select(Map<String, List<WeatherEvent>> map) throws Exception {
                        return new ExtremeHotWarning(map.get("first").get(0), map.get("second").get(0));
                    }
                }
        );
        FlinkKafkaProducer<ExtremeHotWarning> myProducerHotCEP = new FlinkKafkaProducer<>(
                "localhost:9092",
                "hotcep",
                new SerializationSchema<ExtremeHotWarning>() {
                    @Override
                    public byte[] serialize(ExtremeHotWarning extremeHotWarning) {
                        ObjectMapper o = new ObjectMapper();
                        try {
                            return o.writeValueAsBytes(extremeHotWarning);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            System.out.println("error rain when serial");
                            return null;
                        }
                    }
                }
        );
        warnings.addSink(myProducerHotCEP);


        System.out.println("end");
        env.execute("test");
    }


}
