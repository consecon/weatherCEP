package model;

public class CEPEvent {
    float value;
    String from;
    String to;
    public CEPEvent(){

    }
    public CEPEvent(float value, String from,String to){
        this.value=value;
        this.from=from;
        this.to=to;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTo() {
        return to;
    }
}
