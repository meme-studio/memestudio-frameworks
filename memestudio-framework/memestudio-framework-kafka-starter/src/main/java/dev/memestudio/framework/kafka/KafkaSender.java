package dev.memestudio.framework.kafka;

import brave.Span;
import brave.Tracer;
import brave.propagation.TraceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;

/**
 * KafkaSender
 *
 * @author meme
 * @since 2019-03-19 18:27
 */
@Slf4j
@AllArgsConstructor
public class KafkaSender {

    private final KafkaTemplate<String, String> template;

    private final ObjectMapper mapper;

    private final Tracer tracer;

    private final Random random = new SecureRandom();

    public <T> void send(String topic, String key, T value) {
        String data = generateDataMessage(value);
        template.send(topic, key, data)
                .addCallback(
                        successCallback -> log.debug("Message [topic:{}, key:{}, value:{}] sent successfully :)", topic, key, data),
                        failureCallback -> {
                            log.warn("Message [topic:{}, key:{}, value:{}] sending failed :(", topic, key, data);
                            log.warn(failureCallback.getMessage(), failureCallback);
                        });
    }

    public <T> void send(String topic, T value) {
        String data = generateDataMessage(value);
        template.send(topic, data)
                .addCallback(
                        successCallback -> log.debug("Message [topic:{}, value:{}] sent successfully :)", topic, data),
                        failureCallback -> {
                            log.warn("Message [topic:{}, value:{}] sending failed :(", topic, data);
                            log.warn(failureCallback.getMessage(), failureCallback);
                        });
    }

    @SneakyThrows
    private String generateDataMessage(Object value) {
        TraceContext span = Optional.ofNullable(tracer.currentSpan())
                                    .map(Span::context)
                                    .orElseGet(() -> {
                                        long id = random.nextLong();
                                        return TraceContext.newBuilder()
                                                           .traceId(id)
                                                           .spanId(id)
                                                           .build();
                                    });
        return mapper.writeValueAsString(KafkaEntity.of(KafkaEntity.CurrentSpan.of(span.spanId(), span.traceId()), mapper.writeValueAsString(value)));
    }

}
