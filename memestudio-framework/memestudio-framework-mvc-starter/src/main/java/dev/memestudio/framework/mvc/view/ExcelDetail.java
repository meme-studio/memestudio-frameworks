package dev.memestudio.framework.mvc.view;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * excel文件内容,实现此类与xls模版文件进行关联
 *
 * @author meme
 */
@Getter
@AllArgsConstructor
public abstract class ExcelDetail<DETAILS> implements Serializable {

    private static final long serialVersionUID = -7855561213528484941L;
    /**
     * 文件名称
     */
    protected final String excelName;

    /**
     * 详细数据
     */
    protected final DETAILS details;

    /**
     * 获取模版名称
     */
    public abstract String getTemplateName();

}
