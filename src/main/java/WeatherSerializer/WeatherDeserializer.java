package WeatherSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.WeatherEvent;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class WeatherDeserializer implements Deserializer<WeatherEvent> {


    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public WeatherEvent deserialize(String topic, byte[] data) {
        ObjectMapper objectMapper= new ObjectMapper();
        WeatherEvent event=null;
        try {
            event= objectMapper.readValue(data, WeatherEvent.class);
        } catch (Exception e){
            e.printStackTrace();
        }
        return event;
    }

    @Override
    public void close() {

    }
}
