package dev.memestudio.framework.xxljob;

import brave.Tracer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;

@Slf4j
@AllArgsConstructor
@Aspect
public class JobTracer {

    private final Tracer tracer;

    @Pointcut("@annotation(com.xxl.job.core.handler.annotation.XxlJob)")
    public void jobPointCut() {}

    @Before("jobPointCut()")
    public void createTrace() {
        log.info("createTrace");
        tracer.newTrace();
    }

    @After("jobPointCut()")
    public void removeTrace() {
        log.info("removeTrace");
        tracer.currentSpan().finish();
    }

}
