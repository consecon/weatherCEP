package FlinkKafka;

import WeatherSerializer.WeatherSerializer;
import com.google.gson.Gson;
import model.CEPEvent;
import model.WeatherEvent;


import org.apache.http.HttpEntity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import org.apache.http.util.EntityUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import warning.ExtremeColdWarning;
import warning.ExtremeHotWarning;
import warning.RainWarning;
import warning.StormWarning;

import java.io.FileReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class KafkaProducer {

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        createWeatherIndexES("weathersourcecsv");
        createCEPIndexES("hotcep");
        createCEPIndexES("coldcep");
        createCEPIndexES("stormcep");
        createCEPIndexES("raincep");
        producerFromCSV();
        producerRealTime();
    }

    private static void producerRealTime() throws IOException, ParseException, InterruptedException {
        String topic = "weathersourcecsv";
        String APIKEY = "cf2d0fd2db2364ab52eaccb920fac9e5";
        String BOOTSTRAP_SERVERS = "localhost:9092";
        Properties propsRealTime = new Properties();
        propsRealTime.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        propsRealTime.put(ProducerConfig.CLIENT_ID_CONFIG, "kafka.weather.realtime");
        propsRealTime.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        propsRealTime.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, WeatherSerializer.class.getName());

        Producer<String, WeatherEvent> producerRealtime =
                new org.apache.kafka.clients.producer.KafkaProducer<>(propsRealTime);

        while (true) {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet("http://api.openweathermap.org/data/2.5/weather?q=hanoi&APPID=" +
                    APIKEY);
            httpGet.setHeader("Content-type", "application/json");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(responseString);
            //format timestamp
            long time = (long) jsonObject.get("dt");
            Date date = new java.util.Date(time * 1000L);
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT-7"));
            String timestamp = sdf.format(date);
            //visibility
            long visibilityM = (long) jsonObject.get("visibility");
            float visibility = (float) visibilityM;
            JSONObject weatherStack = (JSONObject) jsonObject.get("main");
            //atmosphere
            long hPa = (long) weatherStack.get("pressure");
            float atmosphere = (float) Math.round((hPa * 0.7500616827 * 100) / 100);
            //temperature
            double temperatureKelvin = (double) weatherStack.get("temp");
            float temperature = (float) (temperatureKelvin - 273.15);
            //humidity
            long humidityLong = (long) weatherStack.get("humidity");
            float humidity = (float) humidityLong;
            WeatherEvent weatherEvent = new WeatherEvent(timestamp, temperature, atmosphere, humidity, visibility);
            Gson gson= new Gson();
            String weatherEventJsonString= gson.toJson(weatherEvent);
//        System.out.println(weatherEvent.getTimestamp());
            sendWeatherToES(new StringEntity(weatherEventJsonString), topic);
            Thread.sleep(10800000);
        }
    }

    private static void producerFromCSV() {
        String topic = "weathersourcecsv";
        String BOOTSTRAP_SERVERS = "localhost:9092";
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "kafka.weather");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, WeatherSerializer.class.getName());

        Producer<String, WeatherEvent> producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
        JSONParser parser = new JSONParser();
        try {
            JSONArray array = (JSONArray) parser
                    .parse(new FileReader("C:\\Users\\RUOI\\IdeaProjects\\flink-java\\12-2018.json"));
            for (int i = 0; i < array.size() - 1; i++) {
                JSONObject weather = (JSONObject) array.get(i);
                Gson gson = new Gson();
                WeatherEvent event = gson.fromJson(weather.toString(), WeatherEvent.class);

                producer.send(new ProducerRecord<>(topic, String.valueOf(i), event));
//                System.out.println("send completed " + event.getTimestamp());
                sendWeatherToES(new StringEntity(gson.toJson(weather)), topic);
//                System.out.println(weather.toString());
            }
            System.out.println("send complete");
//            producer.close();

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    private static void sendWeatherToES(StringEntity entity, String index) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost("http://localhost:9200/" + index + "/" + index + "-event");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(entity);
        HttpResponse response = httpClient.execute(httpPost);
        System.out.println(response);
    }

    private static void createWeatherIndexES(String indexWeatherName) throws IOException {
        HttpClient httpClientCreateWeatherIndex = HttpClientBuilder.create().build();
        HttpPut createWeatherIndex = new HttpPut("http://localhost:9200/" + indexWeatherName);
        HttpResponse createWeatherIndexResponse = httpClientCreateWeatherIndex.execute(createWeatherIndex);
        HttpPut createWeatherSchemaMapping =
                new HttpPut("http://localhost:9200/" + indexWeatherName
                        + "/_mapping/" + indexWeatherName + "-event");
        createWeatherSchemaMapping.setHeader("Content-type", "application/json");
        JSONObject entity = new JSONObject();
        JSONObject indexEvent = new JSONObject();
        JSONObject properties = new JSONObject();
        JSONObject temperature = new JSONObject();
        temperature.put("type", "float");
        JSONObject atmosphere = new JSONObject();
        atmosphere.put("type", "float");
        JSONObject humidity = new JSONObject();
        humidity.put("type", "float");
        JSONObject visibility = new JSONObject();
        visibility.put("type", "float");
        JSONObject timestamp = new JSONObject();
        timestamp.put("type", "date");
        timestamp.put("format", "DD.MM.YYYY HH:mm");
        properties.put("temperature", temperature);
        properties.put("atmosphere", atmosphere);
        properties.put("humidity", humidity);
        properties.put("visibility", visibility);
        properties.put("timestamp", timestamp);
        indexEvent.put("properties", properties);
        entity.put(indexWeatherName + "-event", indexEvent);
        createWeatherSchemaMapping.setEntity(new StringEntity(entity.toString()));
        HttpResponse httpResponse = httpClientCreateWeatherIndex.execute(createWeatherSchemaMapping);
        System.out.println("createWeatherSchemaMapping" + httpResponse.toString());
    }

    private static void createCEPIndexES(String indexName) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPut createIndex = new HttpPut("http://localhost:9200/" + indexName);
        HttpResponse httpResponse = httpClient.execute(createIndex);
        HttpPut createCEPSchemaMapping =
                new HttpPut("http://localhost:9200/" + indexName + "/_mapping/" + indexName + "-event");
        createCEPSchemaMapping.setHeader("Content-type", "application/json");
        JSONObject valueType = new JSONObject();
        valueType.put("type", "float");
        JSONObject fromTimeType = new JSONObject();
        fromTimeType.put("type", "date");
        fromTimeType.put("format", "DD.MM.YYYY HH:mm");
        JSONObject toTimeType = new JSONObject();
        toTimeType.put("type", "date");
        toTimeType.put("format", "DD.MM.YYYY HH:mm");
        JSONObject properties = new JSONObject();
        properties.put("value", valueType);
        properties.put("from", fromTimeType);
        properties.put("to", toTimeType);
        JSONObject indexNameEvent = new JSONObject();
        indexNameEvent.put("properties", properties);
        JSONObject entity = new JSONObject();
        entity.put(indexName + "-event", indexNameEvent);
        createCEPSchemaMapping.setEntity(new StringEntity(entity.toString()));
        HttpResponse httpResponse1 = httpClient.execute(createCEPSchemaMapping);
        System.out.println("createCEPSchemaMapping: " + httpResponse1.toString());
    }

    public static void pushDataToCEPIndex(String indexName, float value,
                                           String fromTime, String toTime) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost =
                new HttpPost("http://localhost:9200/" + indexName + "/" + indexName + "-event");
        httpPost.setHeader("Content-type", "application/json");
        CEPEvent event = new CEPEvent(value, fromTime, toTime);
        Gson gson= new Gson();
        String eventJsonString = gson.toJson(event);
        httpPost.setEntity(new StringEntity(eventJsonString));
        HttpResponse response = httpClient.execute(httpPost);
        System.out.println(response);
    }
}
