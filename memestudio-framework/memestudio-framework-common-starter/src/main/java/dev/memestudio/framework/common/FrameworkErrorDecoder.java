package dev.memestudio.framework.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.memestudio.framework.common.error.BusinessException;
import dev.memestudio.framework.common.error.ErrorMessage;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.SneakyThrows;

import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * 异常解析器
 *
 * @author meme
 * @since 2019-01-23 10:38
 */
@AllArgsConstructor
public class FrameworkErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public Exception decode(String methodKey, Response response) {
        @Cleanup Reader body = response.body().asReader(StandardCharsets.UTF_8);
        ErrorMessage errorMessage = objectMapper.readValue(body, ErrorMessage.class);
        return new BusinessException(errorMessage);
    }
}
