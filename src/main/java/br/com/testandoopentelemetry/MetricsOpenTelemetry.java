package br.com.testandoopentelemetry;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static br.com.testandoopentelemetry.Constants.*;
import static br.com.testandoopentelemetry.Constants.HEAP_MEMORY_DESCRIPTION;
import static java.lang.Runtime.getRuntime;

public class MetricsOpenTelemetry {

    @Value("otel.metrics.api.version")
    private String metricsApiVersion;

    private Meter meter;

    private HashMap<String,LongCounter> staticMetrics;

    public MetricsOpenTelemetry(String meterName) {
        this.meter = GlobalOpenTelemetry
                .meterBuilder(meterName)
                .setInstrumentationVersion(metricsApiVersion)
                .build();
        staticMetrics = new HashMap<>();
    }



    @PostConstruct
    public void createStaticMetrics(String name, String counter, String description, String unit){
        LongCounter value=
                    meter
                            .counterBuilder(counter)
                            .setDescription(description)
                            .setUnit(unit)
                            .build();
        this.staticMetrics.put(name,value);

    }

    public LongCounter getStaticMetrics(String name) {
        return this.staticMetrics.get(name);
    }
    public void removeStaticMetrics(String name) {
        this.staticMetrics.remove(name);
    }


    @PostConstruct
    public void createAutomaticMetrics(String name, String description, String unit){
        meter
                .gaugeBuilder(name)
                .setDescription(description)
                .setUnit("byte")
                .buildWithCallback(
                        r ->{
                            r.record(getRuntime().totalMemory() - getRuntime().freeMemory());
                        }
                );


    }

}
