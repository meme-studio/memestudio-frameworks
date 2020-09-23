package dev.memestudio.framework.xxljob;

import brave.Tracer;
import com.xxl.job.core.log.XxlJobLogger;
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
        tracer.startScopedSpan("xxl-job-task");
        XxlJobLogger.log("trace: {}", tracer.currentSpan().context().toString().replace("/", ","));
    }

    @After("jobPointCut()")
    public void removeTrace() {
        tracer.currentSpan().finish();
    }

}
