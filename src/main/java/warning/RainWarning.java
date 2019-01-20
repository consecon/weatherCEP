package warning;

import model.WeatherEvent;

public class RainWarning {
    private WeatherEvent event1;
    private WeatherEvent event2;

    public RainWarning(){

    }
    public RainWarning(WeatherEvent event1,WeatherEvent event2){
        this.event1=event1;
        this.event2=event2;
    }

    public void setEvent1(WeatherEvent event1) {
        this.event1 = event1;
    }

    public void setEvent2(WeatherEvent event2) {
        this.event2 = event2;
    }

    public WeatherEvent getEvent1() {
        return event1;
    }

    public WeatherEvent getEvent2() {
        return event2;
    }
    public float getAvg(){
        return (this.getEvent1().getHumidity()+this.getEvent2().getHumidity())/2;
    }
    @Override
    public String toString(){
        return String.format("Rain warining (at: %s)",this.getEvent2().getTimestamp());
    }
}
