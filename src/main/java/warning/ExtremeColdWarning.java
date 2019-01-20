package warning;

import model.WeatherEvent;

public class ExtremeColdWarning {
    private WeatherEvent event1;
    private WeatherEvent event2;

    public ExtremeColdWarning(){

    }
    public ExtremeColdWarning(WeatherEvent event1,WeatherEvent event2){
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
        return (this.getEvent1().getTemperature()+this.getEvent2().getTemperature())/2;
    }
    @Override
    public String toString(){
        return String.format("Extreme cold (Avg temperature: %f, at: %s)",
                (getEvent1().getTemperature()+getEvent2().getTemperature())/2,
                getEvent2().getTimestamp());
    }
}
