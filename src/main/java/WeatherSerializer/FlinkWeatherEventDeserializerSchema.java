package WeatherSerializer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.WeatherEvent;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;

import java.io.IOException;

public class FlinkWeatherEventDeserializerSchema
        implements DeserializationSchema<WeatherEvent>, SerializationSchema<WeatherEvent> {

    @Override
    public WeatherEvent deserialize(byte[] data) throws IOException {
        ObjectMapper o= new ObjectMapper();
        WeatherEvent event;
        event= o.readValue(data, WeatherEvent.class);
        return event;
    }

    @Override
    public boolean isEndOfStream(WeatherEvent event) {
        return false;
    }

    @Override
    public byte[] serialize(WeatherEvent event) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsBytes(event);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public TypeInformation<WeatherEvent> getProducedType() {
        return TypeExtractor.getForClass(WeatherEvent.class);
    }
}
