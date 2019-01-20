package WeatherSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.WeatherEvent;
import org.apache.kafka.common.serialization.Serializer;

import java.io.*;
import java.util.Map;

public class WeatherSerializer implements Serializer<WeatherEvent> {
    @Override
    public void configure(Map<String,?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, WeatherEvent event) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            retVal = objectMapper.writeValueAsBytes(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    @Override
    public void close() {

    }
}