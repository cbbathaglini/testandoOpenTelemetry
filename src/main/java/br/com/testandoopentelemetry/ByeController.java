package br.com.testandoopentelemetry;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

import static br.com.testandoopentelemetry.Constants.*;
import static java.lang.Runtime.getRuntime;

@RestController
public class ByeController {

    private static final Logger log =
            LoggerFactory.getLogger(ByeController.class);


    @Value("otel.traces.api.version")
    private String tracesApiVersion;

    @Value("otel.metrics.api.version")
    private String metricsApiVersion;

    private final Tracer tracer = GlobalOpenTelemetry.getTracer("io.opentelemetry.traces.bye",tracesApiVersion);
    private final Meter meter = GlobalOpenTelemetry
            .meterBuilder("io.opentelemetry.metrics.bye")
            .setInstrumentationVersion(metricsApiVersion)
            .build();

    private LongCounter numberOfExecutions;

    @PostConstruct
    public void createMetrics(){
        numberOfExecutions =
                meter
                        .counterBuilder(NUMBER_OF_EXEC_NAME)
                        .setDescription(NUMBER_OF_EXEC_DESCRIPTION)
                        .setUnit("int")
                        .build();

        meter
                .gaugeBuilder(HEAP_MEMORY_NAME)
                .setDescription(HEAP_MEMORY_DESCRIPTION)
                .setUnit("byte")
                .buildWithCallback(
                        r ->{
                            r.record(getRuntime().totalMemory() - getRuntime().freeMemory());
                        }
                );
    }

    @RequestMapping(method= RequestMethod.GET, value="/bye")
    public Response bye() {
        Response response = buildResponse();
        Span span = tracer.spanBuilder("byeSpan").startSpan();
        try(Scope scope = span.makeCurrent()){
            if (response.isValid()) {
                log.info("[bye] The response is valid.");
            }
            numberOfExecutions.add(1);
        }finally {
            span.end();
        }
        return response;
    }

    @WithSpan
    private Response buildResponse() {
        return new Response("Bye World");
    }

    private record Response (String message) {
        private Response {
            Objects.requireNonNull(message);
        }
        private boolean isValid() {
            return true;
        }
    }

}
