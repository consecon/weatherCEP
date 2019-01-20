package WeatherSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import warning.ExtremeHotWarning;

import java.util.Map;

public class HotCEPSerializer implements Serializer<ExtremeHotWarning> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(String s, ExtremeHotWarning extremeHotWarning) {
        byte[] result = null;
        ObjectMapper objectMapper= new ObjectMapper();
        try {
            result= objectMapper.writeValueAsBytes(extremeHotWarning);
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void close() {

    }
}
