package dev.memestudio.framework.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

/**
 * @author meme
 * @since 2020/8/10
 */
@Configuration
public class FrameworkJpaAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager){
        return new JPAQueryFactory(entityManager);
    }

}

