package io.ekstrai.poc.ingestservice;

import com.fasterxml.jackson.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

public class MachineEvent {

    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-ddTHH:mm:ss.SSSZ")
    private final String timeStamp;
    private final String machine;
    private final int counter;
    private final UUID session;
    private final String parameter;
    private final String value;

    @JsonCreator
    public MachineEvent(
            @JsonProperty("timeStamp") String timeStamp,
            @JsonProperty("machine") String machine,
            @JsonProperty("counter") int counter,
            @JsonProperty("session") UUID session,
            @JsonProperty("parameter") String parameter,
            @JsonProperty("value") String value) {

        //this.timeStamp = OffsetDateTime.parse(timeStamp);
        this.timeStamp = timeStamp;
        this.machine = machine;
        this.counter = counter < 0 ?  0 : counter;
        this.session = session;
        this.parameter = parameter;
        this.value = value;
    }

    public MachineEvent(String machine, int counter, String parameter, String value) {
        this.timeStamp = OffsetDateTime.now().toString();
        this.machine = machine;
        this.counter = counter < 0 ?  0 : counter;
        this.session = UUID.randomUUID();
        this.parameter = parameter;
        this.value = value;
    }

    @JsonRawValue
    public String getTimeStamp() {
        return timeStamp;
    }


    public String getMachine() {
        return machine;
    }


    public int getCounter() {
        return counter;
    }


    public UUID getSession() {
        return session;
    }


    public String getParameter() {
        return parameter;
    }


    public String getValue() {
        return value;
    }

}
