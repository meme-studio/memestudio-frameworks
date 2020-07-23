package dev.memestudio.framework.kafka.converter;

import brave.Tracer;
import brave.propagation.TraceContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import dev.memestudio.framework.kafka.KafkaEntity;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

import java.lang.reflect.Type;

/**
 * KafkaEntityMessageConverter
 *
 * @author meme
 * @since 2019-03-22 19:10
 */
@AllArgsConstructor
public class KafkaEntityMessageConverter extends StringJsonMessageConverter {

    private final Tracer tracer;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    protected Object extractAndConvertValue(ConsumerRecord<?, ?> record, Type type) {
        Object value = record.value();
        KafkaEntity entity = objectMapper.readValue(value.toString(), KafkaEntity.class);
        trace(entity.getCurrentSpan());
        JavaType javaType = TypeFactory.defaultInstance().constructType(type);
        return objectMapper.readValue(entity.getContent(), javaType);
    }

    private void trace(KafkaEntity.CurrentSpan parent) {
        tracer.joinSpan(TraceContext.newBuilder()
                                    .parentId(parent.getSpanId())
                                    .traceId(parent.getTraceId())
                                    .build());
    }


}
