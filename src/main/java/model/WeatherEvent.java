package model;


public class WeatherEvent {

    private float atmosphere;
    private float visibility;
    private float temperature;
    private float humidity;
    private String timestamp;
    public WeatherEvent(){

    }
    public WeatherEvent(String timestamp,float temperature, float atmosphere,
                            float humidity, float visibility){
        this.timestamp=timestamp;
        this.atmosphere=atmosphere;
        this.temperature=temperature;
        this.humidity=humidity;
        this.visibility=visibility;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public float getAtmosphere() {
        return atmosphere;
    }

    public void setAtmosphere(float atmosphere) {
        this.atmosphere = atmosphere;
    }

    public float getVisibility() {
        return visibility;
    }

    public void setVisibility(float visibility) {
        this.visibility = visibility;
    }
}
