package br.com.testandoopentelemetry;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.web.client.RestTemplate;

import static br.com.testandoopentelemetry.Constants.*;
import static java.lang.Runtime.getRuntime;

@RestController
public class EnderecoController {

    private static final Logger log =
            LoggerFactory.getLogger(HelloController.class);


    @Value("otel.traces.api.version")
    private String tracesApiVersion;

    @Value("otel.metrics.api.version")
    private String metricsApiVersion;

    private final Tracer tracer = GlobalOpenTelemetry.getTracer("io.opentelemetry.traces.hello",tracesApiVersion);
    private final Meter meter = GlobalOpenTelemetry
            .meterBuilder("io.opentelemetry.metrics.hello")
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

    @WithSpan("allmethodSpan")
    @RequestMapping(method= RequestMethod.GET, value="/consultacep")
    public String consulta() {

        String responseRest= "NOK";

        RestTemplate  restTemplate = new RestTemplate();

        Span span = tracer.spanBuilder("consultaCep").startSpan();
        ResponseEntity<?> response = restTemplate.exchange("https://viacep.com.br/ws/91920530/json/", HttpMethod.GET, null,String.class);
        try(Scope scope = span.makeCurrent()){
            if (response.getStatusCode().value() == 200) {
                responseRest = "OK";
                log.info("[hello] The response is valid.");
            }
            numberOfExecutions.add(1);
        }finally {
            span.end();
        }

        return responseRest;
    }

}
