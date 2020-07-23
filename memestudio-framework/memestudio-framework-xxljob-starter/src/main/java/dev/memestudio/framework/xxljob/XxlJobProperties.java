package dev.memestudio.framework.xxljob;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * dev.memestudio.framework.xxljob.XxlJobProperties
 *
 * @author meme
 * @since 2019-03-04 18:05
 */
@Getter
@Setter
@ConfigurationProperties(prefix = XxlJobProperties.PREFIX)
class XxlJobProperties {

    public static final String PREFIX = "xxl.job";

    private Boolean enabled = true;

    /**
     * 访问令牌[选填]，非空则进行匹配校验
     */
    private String accessToken;

    private Admin admin = new Admin();

    private Executor executor = new Executor();

    @Getter
    @Setter
    static class Admin {

        /**
         * 执行器注册中心地址[选填]，为空则关闭自动注册
         */
        private List<String> addresses = new ArrayList<>();

    }

    @Getter
    @Setter
    static class Executor {

        /**
         * 执行器AppName[选填]，为空则关闭自动注册
         */
        private String appname;

        /**
         * 执行器IP[选填]，为空则自动获取
         */
        private String ip;

        /**
         * 行器端口号[选填]，小于等于0则自动获取
         */
        private int port;

        /**
         * 执行器日志路径[选填]，为空则使用默认路径
         */
        private String logPath;

        /**
         * 志保存天数[选填]，值大于3时生效
         */
        private int logRetentionDays;
    }


}
