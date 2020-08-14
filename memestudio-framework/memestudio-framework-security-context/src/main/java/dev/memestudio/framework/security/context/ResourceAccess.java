package dev.memestudio.framework.security.context;

import java.util.List;

/**
 * @author meme
 * @since 2020/8/9
 */
public interface ResourceAccess {

    /**
     * 获取当前资源ID集合
     * @param type 资源类型
     * @return 资源ID集合，null: 拥有该类型所有资源权限，[]: 无权限
     */
    List<String> listResourceIds(AccessType type);

}
