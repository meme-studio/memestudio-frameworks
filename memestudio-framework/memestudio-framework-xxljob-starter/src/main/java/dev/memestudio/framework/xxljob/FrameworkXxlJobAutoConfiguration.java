package dev.memestudio.framework.xxljob;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * dev.memestudio.framework.xxljob.XxlJobAutoConfiguration
 *
 * @author meme
 * @since 2019-03-04 18:05
 */
@Slf4j
@Configuration
@ConditionalOnClass(XxlJobExecutor.class)
@EnableConfigurationProperties(FrameworkTaskProperties.class)
public class FrameworkXxlJobAutoConfiguration {

    @ConditionalOnProperty(prefix = FrameworkTaskProperties.PREFIX, name = "enabled", matchIfMissing = true)
    @ConditionalOnMissingBean
    @Bean(initMethod = "start", destroyMethod = "destroy")
    public XxlJobExecutor xxlJobExecutor(FrameworkTaskProperties properties){
        log.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobExecutor xxlJobExecutor = new XxlJobSpringExecutor();
        xxlJobExecutor.setAppname(properties.getAppName());
        xxlJobExecutor.setAdminAddresses(String.join(",", properties.getExecutors()));
        xxlJobExecutor.setAccessToken(properties.getAccessToken());
        xxlJobExecutor.setLogRetentionDays(properties.getLogRetentionDays());
        return xxlJobExecutor;
    }


}