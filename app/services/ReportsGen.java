package services;

import static net.sf.dynamicreports.report.builder.DynamicReports.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import play.Play;
public class ReportsGen {
	@SuppressWarnings("deprecation")
	public static JasperReportBuilder generate(DRDataSource drDataSource, Map<String, Object> reportParameters) throws Exception {
		InputStream reportTemplateJrxml = new FileInputStream(new File(Play.application().configuration().getString("application.report.path.url")+reportParameters.get("url").toString()));
		JasperReportBuilder jasperReportBuilder = report()
				.setReportName("test")
				.setParameters(reportParameters)
				.setTemplateDesign(reportTemplateJrxml)
				.setDataSource(drDataSource);
		return jasperReportBuilder;
	}
   

}
