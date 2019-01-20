package warning;

import model.WeatherEvent;

public class StormWarning {
    private WeatherEvent event1;
    private WeatherEvent event2;

    public StormWarning(){

    }
    public StormWarning(WeatherEvent event1,WeatherEvent event2){
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
        return (this.getEvent1().getAtmosphere()+this.getEvent2().getAtmosphere())/2;
    }
    @Override
    public String toString(){
        return String.format("Storm warning (at: %s)",this.getEvent2().getTimestamp());
    }
}
