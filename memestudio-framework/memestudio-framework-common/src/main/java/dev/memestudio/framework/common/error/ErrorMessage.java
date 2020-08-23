package dev.memestudio.framework.common.error;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 服务间异常信息封装实体
 *
 * code: 错误代码（3位业务代码-3位具体代码） [string]
 * note: 用户错误信息（用于客户端显示）[string]
 * detail: 具体出错信息（用于客户端与服务端排查问题，客户端可不解析，或者记录在日志里面）[string]
 * timestamp: 发生错误时的时间戳（用于客户端与服务端排查问题，客户端可不解析，或者记录在日志里面）[long]
 * path: 请求路径（用于客户端与服务端排查问题，客户端可不解析，或者记录在日志里面）[string]
 * trace: 链路信息（用于客户端与服务端排查问题，客户端可不解析，或者记录在日志里面）[string]
 * errorStacks: 错误堆栈（只会在测试环境返回，用于客户端与服务端排查问题，客户端可不解析，或者记录在日志里面）[array]
 * from: 项目来源（用于客户端与服务端排查问题，客户端可不解析，或者记录在日志里面）[string]
 *
 * @author meme
 * @since 2019-03-05 17:31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessage implements ErrorCode {

    private String code;

    private String note;

    private Object errorParam;

    private String detail;

    private long timestamp;

    private String path;

    private String trace;

    private String from;

    private List<String> errorStacks;


}

