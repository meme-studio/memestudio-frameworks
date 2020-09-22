package dev.memestudio.framework.xxljob;

import brave.Tracer;
import lombok.AllArgsConstructor;
import org.aspectj.lang.annotation.*;

@AllArgsConstructor
@Aspect
public class JobTracer {

    private final Tracer tracer;

    @Pointcut("@annotation(com.xxl.job.core.handler.annotation.XxlJob)")
    public void jobPointCut() {}

    @Before("@annotation(com.xxl.job.core.handler.annotation.XxlJob)")
    public void createTrace() {
        tracer.newTrace();
    }

    @AfterReturning(pointcut = "jobPointCut()")
    public void removeTraceAfterReturning() {
        tracer.currentSpan().finish();
    }

    @AfterThrowing(pointcut = "jobPointCut()")
    public void removeTraceAfterThrowing() {
        removeTraceAfterReturning();
    }

}
