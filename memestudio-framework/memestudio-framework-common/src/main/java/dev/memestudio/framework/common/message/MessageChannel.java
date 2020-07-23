package dev.memestudio.framework.common.message;

/**
 * 消息类型
 */
public enum MessageChannel implements Sender.Channel {
    /**
     * 邮件类型
     */
    MAIL,
    /**
     * 短信类型
     */
    SMS;
}