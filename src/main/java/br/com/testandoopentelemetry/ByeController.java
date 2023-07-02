package br.com.testandoopentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class ByeController {

    private static final TracesOpenTelemetry general = new TracesOpenTelemetry("tracer.bye");
    private static final MetricsOpenTelemetry metrics = new MetricsOpenTelemetry("metrics.ByeController");

    private static final Logger log =
            LoggerFactory.getLogger(ByeController.class);


    @RequestMapping(method= RequestMethod.GET, value="/bye")
    public Response bye() {
        metrics.createStaticMetrics("numero execucoes","number.of.exec","Count the number of executions.","int");
        Response response = buildResponse();
        Span spanByeResponse = general.startSpan("[bye()] responseTime ");
        try(Scope scope = spanByeResponse.makeCurrent()){
            if (response.isValid()) {
                log.info("[bye] The response is valid.");
            }
            metrics.getStaticMetrics("numero execucoes").add(1);
        }finally {
            general.endSpan(spanByeResponse);
        }
        return response;
    }

    @RequestMapping(method= RequestMethod.GET, value="/byebye")
    public Response byebye() {
        metrics.createStaticMetrics("numero execucoes sucesso","number.of.exec.ok","Count the number of executions ok.","int");
        Response response = buildResponse();
        Span spanByeByeResponse = general.startSpan("[byebye()] responseTime ");
        try(Scope scope = spanByeByeResponse.makeCurrent()){
            if (response.isValid()) {
                log.info("[byebye] The response is valid.");
            }
            metrics.getStaticMetrics("numero execucoes sucesso").add(1);
        }finally {
            general.endSpan(spanByeByeResponse);
        }
        return response;
    }

    @RequestMapping(method= RequestMethod.GET, value="/notokby")
    public Response notokby() {
        metrics.createStaticMetrics("numero execucoes erro","number.of.exec.nok","Count the number of executions nok.","int");
        Response response = buildResponse();
        Span spanByeByeResponse = general.startSpan("[notokby()] responseTime ");
        try(Scope scope = spanByeByeResponse.makeCurrent()){
            if (response.isValid()) {
                log.info("[notokby] The response is valid.");
            }
            metrics.getStaticMetrics("numero execucoes erro").add(1);
        }finally {
            general.endSpan(spanByeByeResponse);
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
