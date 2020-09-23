package dev.memestudio.framework.kafka;

import brave.Tracer;
import lombok.AllArgsConstructor;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@AllArgsConstructor
@Aspect
public class KafkaTraceRemover {

    private final Tracer tracer;

    @Pointcut("@annotation(org.springframework.kafka.annotation.KafkaListener) " +
            "|| @annotation(org.springframework.kafka.annotation.KafkaListeners)" +
            "|| @annotation(org.springframework.kafka.annotation.KafkaHandler)")
    public void kafkaConsumerPointCut() {}

    @After("kafkaConsumerPointCut()")
    public void removeTrace() {
        tracer.currentSpan().finish();
    }

}
