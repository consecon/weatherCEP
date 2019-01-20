package WeatherSerializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import warning.ExtremeHotWarning;

import java.io.IOException;

public class FlinkExtremeHotWarningSchema
        implements DeserializationSchema<ExtremeHotWarning>, SerializationSchema<ExtremeHotWarning> {

    @Override
    public ExtremeHotWarning deserialize(byte[] bytes) throws IOException {
        ObjectMapper o= new ObjectMapper();
        ExtremeHotWarning event;
        event= o.readValue(bytes, ExtremeHotWarning.class);
        return event;
    }

    @Override
    public boolean isEndOfStream(ExtremeHotWarning extremeHotWarning) {
        return false;
    }

    @Override
    public byte[] serialize(ExtremeHotWarning extremeHotWarning) {
        ObjectMapper objectMapper= new ObjectMapper();
        try {
            return objectMapper.writeValueAsBytes(extremeHotWarning);
        } catch (JsonProcessingException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public TypeInformation<ExtremeHotWarning> getProducedType() {
        return TypeExtractor.getForClass(ExtremeHotWarning.class);
    }
}
