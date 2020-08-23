package dev.memestudio.framework.webflux;

import dev.memestudio.framework.common.error.BusinessException;
import dev.memestudio.framework.common.error.RemoteException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import static org.springframework.boot.web.error.ErrorAttributeOptions.defaults;
import static org.springframework.web.reactive.function.server.RequestPredicates.all;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author meme
 * @since 2020/8/4
 */
public class CommonErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {


    private final ErrorProperties errorProperties;

    /**
     * Create a new {@code DefaultErrorWebExceptionHandler} instance.
     * @param errorAttributes the error attributes
     * @param resourceProperties the resources configuration properties
     * @param errorProperties the error configuration properties
     * @param applicationContext the current application context
     */
    public CommonErrorWebExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
                                          ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, applicationContext);
        this.errorProperties = errorProperties;
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return route(all(), this::renderErrorResponse);
    }

    /**
     * Render the error information as a JSON payload.
     * @param request the current request
     * @return a {@code Publisher} of the HTTP response
     */
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> error = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        return ServerResponse.status(getHttpStatus(request)).contentType(MediaType.APPLICATION_JSON)
                             .body(BodyInserters.fromValue(error));
    }

    protected ErrorAttributeOptions getErrorAttributeOptions(ServerRequest request, MediaType mediaType) {
        ErrorAttributeOptions options = defaults();
        if (this.errorProperties.isIncludeException()) {
            options = options.including(Include.EXCEPTION);
        }
        if (isIncludeStackTrace(request, mediaType)) {
            options = options.including(Include.STACK_TRACE);
        }
        if (isIncludeMessage(request, mediaType)) {
            options = options.including(Include.MESSAGE);
        }
        if (isIncludeBindingErrors(request, mediaType)) {
            options = options.including(Include.BINDING_ERRORS);
        }
        return options;
    }

    /**
     * Determine if the stacktrace attribute should be included.
     * @param request the source request
     * @param produces the media type produced (or {@code MediaType.ALL})
     * @return if the stacktrace attribute should be included
     */
    @SuppressWarnings("deprecation")
    protected boolean isIncludeStackTrace(ServerRequest request, MediaType produces) {
        switch (this.errorProperties.getIncludeStacktrace()) {
            case ALWAYS:
                return true;
            case ON_PARAM:
            case ON_TRACE_PARAM:
                return isTraceEnabled(request);
            default:
                return false;
        }
    }

    /**
     * Determine if the message attribute should be included.
     * @param request the source request
     * @param produces the media type produced (or {@code MediaType.ALL})
     * @return if the message attribute should be included
     */
    protected boolean isIncludeMessage(ServerRequest request, MediaType produces) {
        switch (this.errorProperties.getIncludeMessage()) {
            case ALWAYS:
                return true;
            case ON_PARAM:
                return isMessageEnabled(request);
            default:
                return false;
        }
    }

    /**
     * Determine if the errors attribute should be included.
     * @param request the source request
     * @param produces the media type produced (or {@code MediaType.ALL})
     * @return if the errors attribute should be included
     */
    protected boolean isIncludeBindingErrors(ServerRequest request, MediaType produces) {
        switch (this.errorProperties.getIncludeBindingErrors()) {
            case ALWAYS:
                return true;
            case ON_PARAM:
                return isBindingErrorsEnabled(request);
            default:
                return false;
        }
    }

    /**
     * Get the HTTP error status information from the error map.
     * @param request the current error information
     * @return the error HTTP status
     */
    protected int getHttpStatus(ServerRequest request) {
        Throwable error = getError(request);
        MergedAnnotation<ResponseStatus> responseStatusAnnotation = MergedAnnotations
                .from(error.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(ResponseStatus.class);
        return determineHttpStatus(error, responseStatusAnnotation).value();
    }

    private HttpStatus determineHttpStatus(Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
        if (error instanceof ResponseStatusException) {
            return ((ResponseStatusException) error).getStatus();
        } else if (error instanceof BusinessException
                || error instanceof MethodArgumentNotValidException
                || error instanceof TypeMismatchException) {
            return HttpStatus.BAD_REQUEST;
        } else if (error instanceof RemoteException) {
            return HttpStatus.valueOf(((RemoteException) error).getStatus());
        }
        return responseStatusAnnotation.getValue("code", HttpStatus.class).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public Throwable getError(ServerRequest request) {
        return (Throwable) request.attribute(CommonErrorAttributes.ERROR_ATTRIBUTE)
                                  .orElseThrow(() -> new IllegalStateException("Missing exception attribute in ServerWebExchange"));
    }
}
