package dev.memestudio.framework.security.context;

import java.util.List;

/**
 * @author meme
 * @since 2020/8/9
 */
public interface ResourceAccess<A extends AccessType> {

    List<String> listResourceIds(A type);

}
