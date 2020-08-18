package dev.memestudio.framework.jpa.slice;

import com.querydsl.core.QueryResults;
import dev.memestudio.framework.common.slice.ScrollingResponse;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;

/**
 * @author meme
 * @since 2020/8/7
 */
@UtilityClass
public class JpaScrollingResponses {

    public <O> ScrollingResponse<O> from(QueryResults<O> results) {
        return ScrollingResponse.<O>builder()
                .next(results.getTotal() > results.getLimit() * (results.getOffset() + 1))
                .nextOffset(String.valueOf(results.getOffset() + results.getLimit()))
                .result(results.getResults())
                .build();
    }

    public <O> ScrollingResponse<O> from(Page<O> results) {
        return ScrollingResponse.<O>builder()
                .next(results.hasNext())
                .nextOffset(String.valueOf(results.getPageable().getOffset() + results.getPageable().getPageSize()))
                .result(results.getContent())
                .build();
    }

}
