package WeatherSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import warning.ExtremeHotWarning;

import java.util.Map;

public class HotCEPDeserializer implements Deserializer<ExtremeHotWarning> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public ExtremeHotWarning deserialize(String s, byte[] bytes) {
        ObjectMapper objectMapper= new ObjectMapper();
        ExtremeHotWarning extremeHotWarning= null;
        try {
            extremeHotWarning= objectMapper.readValue(bytes, ExtremeHotWarning.class);
        } catch (Exception e){
            e.printStackTrace();
        }
        return extremeHotWarning;
    }

    @Override
    public void close() {

    }
}
