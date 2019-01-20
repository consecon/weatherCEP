package FlinkKafka;

import WeatherSerializer.FlinkWeatherEventDeserializerSchema;
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
import warning.RainWarning;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class RainPatternCEP {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env= StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties= new Properties();
        properties.setProperty("bootstrap.servers","localhost:9092");
        properties.setProperty("group.id","test");
        FlinkKafkaConsumer<WeatherEvent> consumer=
                new FlinkKafkaConsumer<WeatherEvent>("weathersourcecsv",
                        new FlinkWeatherEventDeserializerSchema(),
                        properties);
        consumer.setStartFromEarliest();
        DataStream<WeatherEvent> stream= env.addSource(consumer);

        Pattern<WeatherEvent,?> rainPattern= Pattern.<WeatherEvent>begin("first")
                .where(new SimpleCondition<WeatherEvent>() {
                    @Override
                    public boolean filter(WeatherEvent event) throws Exception {
                        return event.getHumidity()>95;
                    }
                })
                .next("second")
                .where(new SimpleCondition<WeatherEvent>() {
                    @Override
                    public boolean filter(WeatherEvent event) throws Exception {
                        return event.getHumidity()>95;
                    }
                });
        PatternStream<WeatherEvent> patternStreamRain= CEP.pattern(stream,rainPattern);
        DataStream<RainWarning> rainWarning= patternStreamRain.select(
                new PatternSelectFunction<WeatherEvent, RainWarning>() {
                    @Override
                    public RainWarning select(Map<String, List<WeatherEvent>> map) throws Exception {
                        return new RainWarning(map.get("first").get(0),map.get("second").get(0));
                    }
                }
        );
        rainWarning.print();
        FlinkKafkaProducer<RainWarning> rainWarningFlinkKafkaProducer=
                new FlinkKafkaProducer<RainWarning>(
                        "localhost:9092",
                        "raincep",
                        new SerializationSchema<RainWarning>() {
                            @Override
                            public byte[] serialize(RainWarning rainWarning) {
                                ObjectMapper objectMapper= new ObjectMapper();
                                try{
                                    return objectMapper.writeValueAsBytes(rainWarning);
                                } catch (Exception e){
                                    e.printStackTrace();
                                    return null;
                                }
                            }
                        }

                );
        rainWarning.addSink(rainWarningFlinkKafkaProducer);
        env.execute("rain pattern");
    }
}
