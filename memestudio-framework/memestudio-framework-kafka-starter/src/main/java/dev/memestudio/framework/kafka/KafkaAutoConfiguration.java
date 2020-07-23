package dev.memestudio.framework.kafka;

import brave.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.memestudio.framework.common.message.Sender;
import dev.memestudio.framework.common.message.SentMessage;
import dev.memestudio.framework.kafka.converter.KafkaEntityMessageConverter;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * @author meme
 * @since 2020/7/21
 */
@EnableKafka
@Configuration
public class KafkaAutoConfiguration {

    @Bean
    public KafkaSender defaultKafkaSender(KafkaTemplate<String, String> template, ObjectMapper mapper, Tracer tracer) {
        return new KafkaSender(template, mapper, tracer);
    }

    @Bean
    public Sender<SentMessage> kafkaSentMessageSender(KafkaSender sender) {
        return (channel, message) -> sender.send(channel, message.getTo(), message);
    }

    @Bean
    public KafkaEntityMessageConverter kafkaEntityMessageConverter(
            Tracer tracer,
            ObjectMapper objectMapper) {
        return new KafkaEntityMessageConverter(tracer, objectMapper);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory,
            KafkaEntityMessageConverter kafkaEntityMessageConverter) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setMessageConverter(kafkaEntityMessageConverter);
        configurer.configure(factory, kafkaConsumerFactory);
        return factory;
    }

    @Bean
    public KafkaTraceRemover kafkaTraceRemover(Tracer tracer) {
        return new KafkaTraceRemover(tracer);
    }


}
