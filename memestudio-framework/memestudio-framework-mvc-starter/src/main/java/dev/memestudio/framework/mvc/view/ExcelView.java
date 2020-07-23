package dev.memestudio.framework.mvc.view;

import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

/**
 * Excel模版view，controller可以在@{@link org.springframework.web.servlet.ModelAndView#ModelAndView(View)}中返回此模版用以生成excel表格
 *
 * @author meme
 */
@Getter
@AllArgsConstructor(staticName = "of")
@Slf4j
public class ExcelView<DETAILS> extends AbstractXlsxView {

    private static final String EXCEL_TEMPLATE_PATH = "view/excel/template/";

    private final ExcelDetail<DETAILS> excelDetail;

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("开始生成报表[{}]", excelDetail.getExcelName());
        String fileName = new String(excelDetail.getExcelName().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        response.setHeader(CONTENT_DISPOSITION, String.format("attachment; filename=%s.xlsx", fileName));
        response.setContentType(getContentType());
        String templateName = String.format("%s%s.xlsx", EXCEL_TEMPLATE_PATH, excelDetail.getTemplateName());
        @Cleanup InputStream fis = new ClassPathResource(templateName).getInputStream();
        Context context = new Context(singletonMap("details", excelDetail.getDetails()));
        JxlsHelper.getInstance().processTemplate(fis, response.getOutputStream(), context);
        log.debug("成功生成报表[{}]", excelDetail.getExcelName());
    }

    @Override
    protected void renderWorkbook(Workbook workbook, HttpServletResponse response) {
    }

}
