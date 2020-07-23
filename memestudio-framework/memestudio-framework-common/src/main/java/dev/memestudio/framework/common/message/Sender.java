package dev.memestudio.framework.common.message;

/**
 * 消息发送器，可以通过实现各种的@{@link Channel}类型，实现各种消息发送，
 * 默认提供了@{@link MessageChannel#MAIL}以及@{@link MessageChannel#SMS}两种预定义消息类型
 *
 * @author meme
 * @since 2019-03-27
 */
public interface Sender<MESSAGE> {

    default <CHANNEL extends Channel>void send(CHANNEL channel, MESSAGE message) {
        send(channel.toString(), message);
    }

    void send(String channel, MESSAGE message);

    interface Channel {}
}
