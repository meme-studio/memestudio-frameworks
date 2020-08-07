package dev.memestudio.framework.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 * @author meme
 * @since 2019-03-19 17:45
 */

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class KafkaEntity implements Serializable {

    private static final long serialVersionUID = 1076278890855115411L;

    private CurrentSpan currentSpan;

    private String content;

    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    @Data
    public static class CurrentSpan implements Serializable {

        private static final long serialVersionUID = -6014793661812486814L;

        private long spanId;

        private long traceId;

    }

}
