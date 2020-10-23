package controllers;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import play.Play;
import javax.persistence.Query;
import org.eclipse.persistence.sessions.Session;
import org.json.JSONArray;
import org.json.JSONObject;
import akka.io.Tcp.Bind;
import beans.PostRequest;
import beans.TransferReport;
import entity.AccountBalance;
import entity.ExchangeRates;
import entity.Report;
import entity.Transcation;
import entity.TranscationBalance;
import javafx.scene.input.DataFormat;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import services.ReportsGen;

public class ReportController extends Controller{
@SuppressWarnings({ "deprecation", "unchecked" })
	@Transactional
	public Result downloadReport() throws Exception{
		Form<?> bindform=Form.form(TransferReport.class);
		TransferReport report = (TransferReport) bindform.bindFromRequest().get();
		String reportType="pdf";
		
		Map<String, Object> reportParameters = new HashMap<String, Object>();
		String[] fieldNames = null;
		
		Query countQuery = JPA.em().createNativeQuery("select count (*) from report where report_id=:reportId and tenant_id=:tenantId");
		countQuery.setParameter("tenantId",session("tenantId"));
		countQuery.setParameter("reportId", report.getReportId().toString());
		String tenantId=session("tenantId");
		if(Long.valueOf(countQuery.getSingleResult().toString()) <= 0L) {
			tenantId=Play.application().configuration().getString("application.session.global.tenantId");
		}
		
		Query query = JPA.em().createNativeQuery("select report_name,url,fields from report where report_id=:reportId and tenant_id=:tenantId");
		query.setParameter("reportId", report.getReportId().toString());
		query.setParameter("tenantId",tenantId);
		List<Object[]> results = query.getResultList();
		
		for(Object[] objects : results){
			fieldNames = objects[2].toString().split(",");
			reportParameters.put("url", objects[1].toString());
			reportParameters.put("reportName",objects[0].toString());
		}
		DRDataSource drDataSource = new DRDataSource(fieldNames);
		if(report.getReportId().equals(Play.application().configuration().getString("application.report.vendor.id"))){
			downloadReportData(drDataSource,reportParameters,report);
		} else if(report.getReportId().equals(Play.application().configuration().getString("application.report.ledger.id"))){
			downloadReportLedgerData(drDataSource,reportParameters,report);
		} else if(report.getReportId().equals(Play.application().configuration().getString("application.report.Transfer.id"))){
			downloadReportTransferData(drDataSource,reportParameters,report.getTransferId());
		}else if(report.getReportId().equals(Play.application().configuration().getString("application.report.profitLoss.id"))){
			downloadReportProfitAndLoss(drDataSource,reportParameters,report);
		}else if(report.getReportId().equals(Play.application().configuration().getString("application.report.currencyProfitLoss.id"))) {
			downloadReportCurrencyProfitAndLoss(drDataSource,reportParameters,report);
		}
		else {
			downloadVendorCurrency(drDataSource,reportParameters,report);
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	JasperReportBuilder jasperReportBuilder =null;
    	jasperReportBuilder = ReportsGen.generate(drDataSource,reportParameters);
    	jasperReportBuilder.toPdf(outputStream);
    	response().setHeader("Content-Disposition","download; filename=\""+reportParameters.get("reportName").toString()+"."+reportType+"\"");
		return ok(outputStream.toByteArray()).as("application/"+reportType);
	}
	


	@SuppressWarnings({ "unchecked", "deprecation", "unused" })
	private void downloadReportCurrencyProfitAndLoss(DRDataSource drDataSource, Map<String, Object> reportParameters,
			TransferReport report) throws ParseException {
		  int month=0;
		 String currency=ctx().request().getQueryString("currency");
		 String reportType=ctx().request().getQueryString("reportType");
		 String enddate="",currencyName="",startdate="";
		Double crtotal=0d,drtotal=0d,profit=0d;
		
		Calendar fromCal = Calendar.getInstance();
	
		if(report.getStartDate()!=null && !report.getStartDate().isEmpty()){
			SimpleDateFormat formatterDateFormat = new SimpleDateFormat("dd/MM/yyyy");
			java.util.Date fromDate = formatterDateFormat.parse(report.getStartDate());
			fromCal.setTime(fromDate);
			month =fromDate.getMonth();
			fromCal.set(Calendar.HOUR_OF_DAY, 0);
			fromCal.set(Calendar.MINUTE, 0);
			fromCal.set(Calendar.SECOND, 0);
			fromCal.set(Calendar.MILLISECOND, 0);
			startdate=" and trans.transcation_date >=:startDate";
		}
		 String currencyQuery="sum(tb.fc_cr_amount) as fc_cr_amount,sum(tb.fc_dr_amount) as fc_dr_amount";
			if(currency.equals(session("currencyId"))){
				currencyQuery="sum(tb.cr_amount) as cr_amount,sum(tb.dr_amount) as dr_amount";	
			}
		 
		 Calendar toCal = Calendar.getInstance();
			SimpleDateFormat formatterDateFormat = new SimpleDateFormat("dd/MM/yyyy");
			java.util.Date endDate = formatterDateFormat.parse(report.getEndDate());
			toCal.setTime(endDate);
			month =endDate.getMonth();
			toCal.set(Calendar.HOUR_OF_DAY, 23);
			toCal.set(Calendar.MINUTE, 59);
			toCal.set(Calendar.SECOND, 59);
			toCal.set(Calendar.MILLISECOND, 999);
			
			Query query2 = JPA.em().createNativeQuery("SELECT sum(trans.input_amount) as inp_amount,concat(tacc.account_name,'-',tcur.currency_code) to_account,trans.exchange_rate_1," 
			 		+" trans.exchange_rate_2,"+currencyQuery+" from transcation_balance tb"
			 		+" join transcation trans on tb.transcation_id=trans.transcation_id join transcation_balance tbl on tb.transcation_id=tbl.transcation_id"
			 		+" join account facc on facc.account_id=trans.from_account_id join account tacc on tacc.account_id=trans.to_account_id"
			 		+" join currency fcur on fcur.currency_id=trans.from_currency_id join currency tcur on tcur.currency_id=trans.to_currency_id"
			 		+" join status st on st.status_id=trans.transcation_status_id where trans.tenant_id=:tenantId and trans.transcation_date <=:endDate "+startdate
			 		+" and tb.currency_id=:currencyId and tbl.currency_id not in(:currencyId)" 
			 		+" group by to_account,trans.exchange_rate_1,trans.exchange_rate_2");						
			
			 	query2.setParameter("tenantId",session("tenantId"));	
				query2.setParameter("endDate", toCal.getTime());
				
				if(report.getStartDate()!=null && !report.getStartDate().isEmpty()){
					query2.setParameter("startDate",fromCal.getTime());
				}
				if(currency !=null && ! currency.equals("")) {
					query2.setParameter("currencyId",currency.toString());
				}
				Query query1 = JPA.em().createNativeQuery("select currency_code from currency where currency_id=:currencyId");
				if(currency !=null && ! currency.equals("")) {
					query1.setParameter("currencyId",currency.toString());
				}
				List<Object[]> list1=query1.getResultList();
				List<Object[]> list=query2.getResultList();
				
				 if(list.size()>0) {
					for( Object[] object:list) {
						  drtotal=(Double) (drtotal+TransferController.removeExponentFromAmount(Long.valueOf(object[5].toString()),Long.parseLong(session("exponent"))));
							crtotal=(Double) (crtotal+TransferController.removeExponentFromAmount(Long.valueOf(object[4].toString()),Long.parseLong(session("exponent"))));
							
						drDataSource.add(""+object[1],
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(object[0].toString()),Long.parseLong(session("exponent"))),session("exponent")),
								""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(object[2].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(object[5].toString()),Long.parseLong(session("exponent"))),session("exponent")),
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(object[4].toString()),Long.parseLong(session("exponent"))),session("exponent")));
		        	
					}
				 }else {
					 drDataSource.add("","","No Data");
				 }
			String currencyname =list1.toString();
			
			profit=crtotal-drtotal;
			reportParameters.put("Dr_Total",""+ConvertDecimalFormat.convertDecimalFormat(drtotal.doubleValue(), session("exponent"))); 
			reportParameters.put("Cr_Total",""+ConvertDecimalFormat.convertDecimalFormat(crtotal.doubleValue(),session("exponent")));
			reportParameters.put("currencyName",currencyname);
			if(report.getStartDate()!=null && !report.getStartDate().isEmpty()){
				reportParameters.put("StartDateLable","StartDate :");
				reportParameters.put("StartDate",formatterDateFormat.format(fromCal.getTime()));
			}
			reportParameters.put("endDate",formatterDateFormat.format(toCal.getTime()));
			if(profit>=0) {
			reportParameters.put("Profits","Profit");
			reportParameters.put("Profit",""+ConvertDecimalFormat.convertDecimalFormat(profit.doubleValue(),session("exponent")));
			}else {
			reportParameters.put("Profits","Loss");
			 reportParameters.put("Profit",""+ConvertDecimalFormat.convertDecimalFormat(profit.doubleValue(),session("exponent")));
			}
	}
	


	@SuppressWarnings({ "unchecked", "deprecation", "unused" })
	private void downloadReportProfitAndLoss(DRDataSource drDataSource, Map<String, Object> reportParameters,
			TransferReport report) throws ParseException {
          int month=0;
          String flag=ctx().request().getQueryString("flag");
          String flags=ctx().request().getQueryString("flags");
          String currency=ctx().request().getQueryString("currency");
  	   	  String accName=ctx().request().getQueryString("accName");
          String reportType=ctx().request().getQueryString("reportType");
		  Long debitAmt=0l,creditAmt=0l,Loss=0l;
		  Double Profit=0d,total=0d;
		   String enddate="",startdate="",asOnDate="",currencyName="",accountName="";
			 if(report.getStartDate()!=null && !report.getStartDate().isEmpty()) {
			    asOnDate="creation_date >=:startDate and ";
			 }
			 if(report.getEndDate()!=null && !report.getEndDate().isEmpty()) {
				 enddate="creation_date <=:endDate";
			 }
			 if(currency!=null && !currency.isEmpty()) {
				 currencyName=" and ab.currency_id=:currencyId";
			 }
			if(accName!=null && !accName.isEmpty()) {
				accountName =" and ab.account_id=:fromAccountId";
			}
			String curQuery="fc_dr_amount as dr_amount,fc_cr_amount+f_opening_balance as bal";
			if(currency.equals(session("currencyId"))){
				curQuery="dr_amount,cr_amount+opening_balance as bal";	
			}
			     Calendar toCal = Calendar.getInstance();
			 if(report.getStartDate()!=null && !report.getStartDate().isEmpty()) {
			   	    SimpleDateFormat formatterDateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
						java.util.Date toDate1 = formatterDateFormat1.parse(report.getStartDate());
						toCal.setTime(toDate1);
						month =toDate1.getMonth();
						toCal.set(Calendar.HOUR_OF_DAY, 0);
						toCal.set(Calendar.MINUTE, 0);
						toCal.set(Calendar.SECOND, 0);
						toCal.set(Calendar.MILLISECOND, 0);
		   }
			Calendar fromCal = Calendar.getInstance();
			SimpleDateFormat formatterDateFormat = new SimpleDateFormat("dd/MM/yyyy");
			java.util.Date fromDate = formatterDateFormat.parse(report.getEndDate());
			fromCal.setTime(fromDate);
			month =fromDate.getMonth();
			fromCal.set(Calendar.HOUR_OF_DAY, 23);
			fromCal.set(Calendar.MINUTE, 59);
			fromCal.set(Calendar.SECOND, 59);
			fromCal.set(Calendar.MILLISECOND, 999);
		 
			 List<Object[]> object = new ArrayList<Object[]>();
			    Object[] result = new Object[3];	    
			 if(reportType.equals("1")) {
				 Query query=JPA.em().createNativeQuery( 
					 		"select cr_amount,dr_amount,fc_cr_amount,fc_dr_amount,cur.currency_code,acc.account_name,ab.f_opening_balance,ab.opening_balance " + 
					 		"from account_balance ab " + 
					 		"join currency cur on cur.currency_id=ab.currency_id " + 
					 		"join account acc on acc.account_id=ab.account_id and acc.account_code not in ('exp') where "+asOnDate+" "+enddate+"  "+accountName+" and ab.tenant_id=:tenantId");
				 	query.setParameter("tenantId",session("tenantId")); 
				 	if(report.getStartDate()!=null && !report.getStartDate().equals("")) {
						 query.setParameter("startDate", toCal.getTime());
					 }
					 query.setParameter("endDate", fromCal.getTime());	
					 if(accName !=null && ! accName.equals("")) {
						 query.setParameter("fromAccountId",accName.toString());
					 }
				    List<Object[]> list=query.getResultList();
				    long totalDr=0,totalCr=0,profit=0;
				    for(Object[] objects:list) {
				    	 long object0=objects[0]!=null?Long.valueOf(objects[0].toString()):0;
				    	 long object1=objects[1]!=null?Long.valueOf(objects[1].toString()):0;
				    	 long object2=objects[7]!=null?Long.valueOf(objects[7].toString()):0;
				    	 totalCr+=object0;
				    	 totalDr+=object1;
				    	 profit+=object2;
				    }
				    result[0]=""+(totalCr+profit);
					result[1]=""+totalDr;
					result[2]=""+((totalCr+profit)-totalDr);
					object.add(result);
			 }else if(reportType.equals("2")) {
				
	  if(currency.equals(session("currencyId"))){
			    	 String cashExpCode=Play.application().configuration().getString("application.cash.expense.id");
					 Query query=JPA.em().createNativeQuery( "select dr_amount,aa.bal,dr_amount-bal as bal2 from ( select "+curQuery+" from account_balance ab join currency cur on cur.currency_id=ab.currency_id join account acc on acc.account_id=ab.account_id "+
		                                                   "and LOWER(acc.account_code) not in (:cashExpCode) where "+asOnDate+" "+enddate+" "+currencyName+" and acc.tenant_id=:tenantId order by cr_amount asc ) as aa order by bal2 asc ");
			  query.setParameter("tenantId",session("tenantId")); 
			  if(report.getStartDate()!=null && !report.getStartDate().equals("")) {
					 query.setParameter("startDate", toCal.getTime());
				}
			 query.setParameter("endDate", fromCal.getTime());
			 query.setParameter("cashExpCode", Arrays.asList(cashExpCode.split(",")));
			 if(currency !=null && ! currency.equals("")) {
				 query.setParameter("currencyId",currency.toString());
			 }
				
			 List<Object[]> list=query.getResultList();
	    	 Query querys=JPA.em().createNativeQuery( "select sum(cr_amount) as cr_amount,sum(dr_amount)as dr_amount,sum(opening_balance)as opening_balance from (select cr_amount,(dr_amount+ab.opening_balance) as dr_amount,fc_cr_amount,fc_dr_amount,cur.currency_code,acc.account_name,ab.f_opening_balance,ab.opening_balance from account_balance ab join currency cur on cur.currency_id=ab.currency_id join account acc on acc.account_id=ab.account_id  where  LOWER(acc.account_code) in (:cashExpCode) "+currencyName+"  and ab.tenant_id=:tenantId) as aa");

	    	  querys.setParameter("tenantId",session("tenantId"));
	    	  querys.setParameter("cashExpCode", Arrays.asList(cashExpCode.split(",")));
	    	  if(currency !=null && ! currency.equals("")) {
	 			 querys.setParameter("currencyId",currency.toString());
	 		 }
	    	  List<Object[]> lists=querys.getResultList();
	    	long totalDr=0,totalCr=0,profitAmount=0,totalDr1=0,totalCr1=0,totalDrs=0,totalCrs=0;
	    	 for(Object[] objects:lists) {
	    		 	long object0=objects[0]!=null?Long.valueOf(objects[0].toString()):0;
			    	long object1=objects[1]!=null?Long.valueOf(objects[1].toString()):0;
			    	totalCrs=object0;
			    	totalDrs=object1;
			    	
			    }
		    for(Object[] objects:list) {
		    	 long totaldebit=0,totalcredit=0;
		    	 long object0=objects[0]!=null?Long.valueOf(objects[0].toString()):0;
		    	 long object1=objects[1]!=null?Long.valueOf(objects[1].toString()):0;   	 
		    	 if(object1>object0 ){
					 totaldebit=object1-object0; 
				 }
				 else if(object1==object0){
					 totalcredit=0;
					 totaldebit=0;
				 }
				 else{
					 totalcredit=object0-object1;	
				 }
				 totalDr=totalDr+totaldebit;
				 totalCr=totalCr+totalcredit;		 
		    }
		    totalDr=totalDr+totalDrs;
		    totalCr=totalCr+totalCrs;
			result[0]=""+totalDr;
			result[1]=""+totalCr;
			result[2]=""+(totalDr-totalCr);
			object.add(result);
			 
		 }else {
			 String cashExpCode=Play.application().configuration().getString("application.cash.expense.id");
			 Query query=JPA.em().createNativeQuery( "select dr_amount,aa.bal,dr_amount-bal as bal2 from ( select "+curQuery+" from account_balance ab join currency cur on cur.currency_id=ab.currency_id join account acc on acc.account_id=ab.account_id "+
                                                   " where "+asOnDate+" "+enddate+" "+currencyName+" and acc.tenant_id=:tenantId order by cr_amount asc ) as aa order by bal2 asc ");
			  query.setParameter("tenantId",session("tenantId")); 
			  if(report.getStartDate()!=null && !report.getStartDate().equals("")) {
					 query.setParameter("startDate", toCal.getTime());
				}
			 query.setParameter("endDate", fromCal.getTime());
			 if(currency !=null && ! currency.equals("")) {
				 query.setParameter("currencyId",currency.toString());
			 }
				
    List<Object[]> list=query.getResultList();
	    long totalDr=0,totalCr=0,profitAmount=0;
	    for(Object[] objects:list) {
	    	 long totaldebit=0,totalcredit=0;
	    	 long object0=objects[0]!=null?Long.valueOf(objects[0].toString()):0;
	    	 long object1=objects[1]!=null?Long.valueOf(objects[1].toString()):0;
	    	 long object2=objects[2]!=null?Long.valueOf(objects[2].toString()):0;    	 
	    	 if(object1>object0){
				 totaldebit=object1-object0;
			 } else if(object1==object0){
				 totalcredit=0;
				 totaldebit=0;
			 } else{
				 totalcredit=object0-object1;
			 }
			 totalDr=totalDr+totaldebit;
			 totalCr=totalCr+totalcredit;
	    }
		result[0]=""+totalDr;
		result[1]=""+totalCr;
		result[2]=""+(totalDr-totalCr);
		object.add(result);
	 }
		}else {
			Query query=JPA.em().createNativeQuery( 
			 		"select cr_amount,dr_amount,fc_cr_amount,fc_dr_amount,cur.currency_code,acc.account_name,ab.f_opening_balance,ab.opening_balance " + 
			 		"from account_balance ab " + 
			 		"join currency cur on cur.currency_id=ab.currency_id " + 
			 		"join account acc on acc.account_id=ab.account_id and acc.account_code not in ('exp') where "+asOnDate+" "+enddate+" and ab.tenant_id=:tenantId");
		   query.setParameter("tenantId",session("tenantId"));
		   if(report.getStartDate()!=null && !report.getStartDate().equals("")) {
				 query.setParameter("startDate", toCal.getTime());
			}
			 query.setParameter("endDate", fromCal.getTime());
			 List<Object[]> list=query.getResultList();
			 long totalDr=0,totalCr=0,profit=0;;
			 for(Object[] objects:list) {
		    	 long object0=objects[0]!=null?Long.valueOf(objects[0].toString()):0;
		    	 long object1=objects[1]!=null?Long.valueOf(objects[1].toString()):0;
		    	 long object2=objects[7]!=null?Long.valueOf(objects[7].toString()):0;
		    	 totalCr+=object0;
		    	 totalDr+=object1;
		    	 profit+=object2;	 
			 }	
			 	result[0]=""+(totalDr+profit);
				result[1]=""+totalCr;
				result[2]=""+((totalDr+profit)-totalCr);
				object.add(result);		
		}			
		    List<Object[]> list=object;
		   if(list.size()>0) {
		    for(Object[] objects:list) {
				    	debitAmt=Long.valueOf(objects[0].toString());
						creditAmt=Long.valueOf(objects[1].toString());
						Profit=TransferController.removeExponentFromAmount(Long.valueOf(objects[2].toString()),Long.parseLong(session("exponent")));
						total=TransferController.removeExponentFromAmount(Long.valueOf(objects[0].toString()),Long.parseLong(session("exponent")))-TransferController.removeExponentFromAmount(Long.valueOf(objects[1].toString()),Long.parseLong(session("exponent")));
						drDataSource.add(""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[0].toString()),Long.parseLong(session("exponent"))),session("exponent")),
							                  ""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[1].toString()),Long.parseLong(session("exponent"))),session("exponent")));
		  }
		}else {
		drDataSource.add("", "No Data");
		}
		   if(reportType.equals("1")) 
			   reportParameters.put("Profit_loss_Title","VENDOR WISE PROFIT AND LOSS"); 
		   else if(reportType.equals("2")) 
			   reportParameters.put("Profit_loss_Title","CUSTOMER WISE PROFIT AND LOSS"); 
		   else
			   reportParameters.put("Profit_loss_Title","PROFIT AND LOSS REPORT"); 
		   if(report.getStartDate()!=null && !report.getStartDate().equals("")) {
			   reportParameters.put("StartDatelabel","StartDate :");
			   reportParameters.put("StartDate",formatterDateFormat.format(toCal.getTime()));
		   }
		   reportParameters.put("endDate", formatterDateFormat.format(fromCal.getTime()));
		   if(flag.equals("0")) {
				  if(Profit>=0) {
						reportParameters.put("Profit_loss","Profit"); 
						reportParameters.put("Profit",""+ConvertDecimalFormat.convertDecimalFormat(Profit,session("exponent"))); 
					}else {
						reportParameters.put("Profit_loss","Loss");
						reportParameters.put("Profit",""+ConvertDecimalFormat.convertDecimalFormat(Profit,session("exponent")));
					}
		   }else {
			   reportParameters.put("Profit_loss","Total");
			   reportParameters.put("Profit",""+ConvertDecimalFormat.convertDecimalFormat(total,session("exponent")));
		   }
		  
	}
	@SuppressWarnings({ "unchecked", "deprecation", "unused" })
	private void downloadVendorCurrency(DRDataSource drDataSource, Map<String, Object> reportParameters,
			TransferReport report) {
		String currencyId="",cusId="";
		
		if(report.getFlag() == 2L && report.getCustomerId() != null && !report.getCustomerId().isEmpty()){
			cusId=" where ab.account_id=:accountId";
		}
		String expeseCode=Play.application().configuration().getString("application.expense.accountId");
		String currName="";
		Query query2 = JPA.em().createNativeQuery("select cr_amount,dr_amount,fc_dr_amount,fc_cr_amount,cur.currency_code,acc.account_name,ab.f_opening_balance,ab.opening_balance from account_balance ab"
				+" join currency cur on cur.currency_id=ab.currency_id"
				+" join account acc on acc.account_id=ab.account_id and acc.account_code not in ('"+expeseCode+"') "+cusId+" and ab.tenant_id=:tenantId");
		        query2.setParameter("tenantId",session("tenantId"));
			 if(report.getFlag() == 2L && report.getCustomerId() != null && !report.getCustomerId().isEmpty()){
				query2.setParameter("accountId", report.getCustomerId());
			    }
	List<Object[]> results2=query2.getResultList();
	int count=0;
	Double crTotal=0D, drTotal=0D,openingBalance=0D,fcrTotal=0D,fdrTotal=0D,fopeningBalance=0D,totalBalance=0D;
	for(Object[] objects :results2) {
		currName=objects[5].toString();
		crTotal += Double.valueOf(objects[1].toString());
		drTotal += Double.valueOf(objects[0].toString());
		openingBalance +=Double.valueOf(objects[7].toString());
		Double amt1= Double.valueOf(objects[1].toString());
		Double amt2= Double.valueOf(objects[0].toString());
		Double oBalance= Double.valueOf(objects[7].toString());
		
		fcrTotal += Double.valueOf(objects[3].toString());
		fdrTotal += Double.valueOf(objects[2].toString());
		fopeningBalance +=Double.valueOf(objects[6].toString());
		Double amt3= Double.valueOf(objects[3].toString());
		Double amt4= Double.valueOf(objects[2].toString());
		Double fcoBalance= Double.valueOf(objects[6].toString());
		
		if(report.getFlag() == 1L){
			drDataSource.add(""+(++count),""+objects[5],""+objects[4],""+ConvertDecimalFormat.convertDecimalFormat((amt1/100),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat((amt2/100),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat((amt3/100),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat((amt4/100),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat((fcoBalance/100),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat((oBalance/100),session("exponent")));
		} else {
			drDataSource.add(""+(++count),""+objects[5],""+objects[4],""+ConvertDecimalFormat.convertDecimalFormat((amt1/100),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat((amt2/100),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat((amt3/100),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat((amt4/100),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat((fcoBalance/100),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat((oBalance/100),session("exponent")));
		}
		
	}
	if(results2.isEmpty()){
		drDataSource.add("","","NO DATA AVAILABLE","","");
	}
	totalBalance = openingBalance+drTotal-crTotal;
	reportParameters.put("CR_TOT", ""+ConvertDecimalFormat.convertDecimalFormat((drTotal/100),session("exponent")));
	reportParameters.put("DR_TOT", ""+ConvertDecimalFormat.convertDecimalFormat((crTotal/100),session("exponent")));
	reportParameters.put("OPENING_BALANCE_TOT", ""+ConvertDecimalFormat.convertDecimalFormat((openingBalance/100),session("exponent")));
	
	reportParameters.put("F_DR_TOT", ""+ConvertDecimalFormat.convertDecimalFormat((fdrTotal/100),session("exponent")));
	reportParameters.put("F_CR_TOT", ""+ConvertDecimalFormat.convertDecimalFormat((fcrTotal/100),session("exponent")));
	reportParameters.put("F_OPENING_BALANCE_TOT", ""+ConvertDecimalFormat.convertDecimalFormat((fopeningBalance/100),session("exponent")));
	
	reportParameters.put("TOTAL_BALANCE", ""+ConvertDecimalFormat.convertDecimalFormat((totalBalance/100),session("exponent")));
	
		reportParameters.put("NAME_TEXT", "Vendor Name");
		reportParameters.put("CURRENCY_NAME", ""+currName);
		
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Transactional
	public static void downloadReportData(DRDataSource drDataSource, Map<String, Object> reportParameters, TransferReport report) throws Exception{
		String currencyId="",cusId="";
			if(report.getFlag() == 1L && report.getCurrencyId() != null && !report.getCurrencyId().isEmpty()){
				currencyId=" where ab.currency_id=:currencyId";
			}
			if(report.getFlag() == 2L && report.getCustomerId() != null && !report.getCustomerId().isEmpty()){
				cusId=" where ab.account_id=:accountId";
			}
			
			String currName="";
			String curQuery="fc_cr_amount,fc_dr_amount,";
			if(report.getCurrencyId().equals(session("currencyId"))){
				curQuery="cr_amount,dr_amount,";
			}
			String expeseCode=Play.application().configuration().getString("application.expense.accountId");
			Query query2 = JPA.em().createNativeQuery("select "+curQuery+" cur.currency_name,acc.account_name from account_balance ab"
					+" join currency cur on cur.currency_id=ab.currency_id"
					+" join account acc on acc.account_id=ab.account_id and acc.account_code not in ('"+expeseCode+"')"+currencyId+" "+cusId+" and acc.tenant_id=:tenantId");
			    query2.setParameter("tenantId",session("tenantId"));
				if(report.getFlag() == 1L && report.getCurrencyId() != null && !report.getCurrencyId().isEmpty()){
					query2.setParameter("currencyId", report.getCurrencyId());
				}
				if(report.getFlag() == 2L && report.getCustomerId() != null && !report.getCustomerId().isEmpty()){
					query2.setParameter("accountId", report.getCustomerId());
				}
				
		List<Object[]> results2=query2.getResultList();
		int count=0;
		Double crTotal=0D, drTotal=0D;
		for(Object[] objects :results2) {
			
			crTotal += Double.valueOf(objects[1].toString());
			drTotal += Double.valueOf(objects[0].toString());
			Double amt1= Double.valueOf(objects[1].toString());
			Double amt2= Double.valueOf(objects[0].toString());
			if(report.getFlag() == 1L){
				currName=objects[2].toString();
				drDataSource.add(""+(++count),""+objects[3],""+objects[2],""+ConvertDecimalFormat.convertDecimalFormat((amt1/100),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat((amt2/100),session("exponent")));
			} else {
				currName=objects[2].toString();
				drDataSource.add(""+(++count),""+objects[3],""+objects[2],""+ConvertDecimalFormat.convertDecimalFormat((amt1/100),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat((amt2/100),session("exponent")));
			}
			
		}
		if(results2.isEmpty()){
			drDataSource.add("","","NO DATA AVAILABLE","","");
		}
		
		reportParameters.put("CR_TOT", ""+(drTotal/100));
		reportParameters.put("DR_TOT", ""+(crTotal/100));
		
			reportParameters.put("NAME_TEXT", "Currency Name");
		
		if(!report.getCurrencyId().isEmpty()){
			reportParameters.put("CURRENCY_NAME", ""+currName);
		} else{
			reportParameters.put("CURRENCY_NAME","All");
		}
	}
	
	@SuppressWarnings({ "unchecked", "unused", "deprecation" })
	@Transactional
	public static void downloadReportLedgerData(DRDataSource drDataSource, Map<String, Object> reportParameters, TransferReport report) throws Exception{
		Calendar startDate,endDate;
		int month=0;
		String asOnDate="",fromAcc="",toAcc="",fromcurrency="",tocurrency="",statusId="";
		Calendar fromCal = Calendar.getInstance();
		String flag=ctx().request().getQueryString("date");
	   if(report.getStartDate()!=null && !report.getStartDate().isEmpty()){
		SimpleDateFormat formatterDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		java.util.Date fromDate = formatterDateFormat.parse(report.getStartDate());
		fromCal.setTime(fromDate);
		month =fromDate.getMonth();
		/*fromCal.add(Calendar.MONTH, month+1);*/
		fromCal.set(Calendar.HOUR_OF_DAY, 0);
		fromCal.set(Calendar.MINUTE, 0);
		fromCal.set(Calendar.SECOND, 0);
		fromCal.set(Calendar.MILLISECOND, 0);
			asOnDate=" and trans.transcation_date >=:startDate";
		}
	   String accountIds="",currencyIds="";
		if(report.getCurrencyId()!=null  && !report.getCurrencyId().isEmpty()){
			fromAcc=" and tb.currency_id=:currencyId ";
			currencyIds=report.getCurrencyId();
		}
		if(report.getFromAccountId()!=null  && !report.getFromAccountId().isEmpty()){
			fromcurrency=" and tb.account_id=:fromAccId ";
			accountIds=report.getFromAccountId();
		}
		if(report.getStatusId()!=null && !report.getStatusId().isEmpty()){
			statusId= " and st.status_id=:statusId ";
		}
		SimpleDateFormat formatterDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		java.util.Date toDate = formatterDateFormat.parse(report.getEndDate());
		Calendar toCal = Calendar.getInstance();
		toCal.setTime(toDate);
		month=toDate.getMonth();
		toCal.set(Calendar.HOUR_OF_DAY, 23);
		toCal.set(Calendar.MINUTE, 59);
		toCal.set(Calendar.SECOND, 59);
		toCal.set(Calendar.MILLISECOND, 999);
		Query query2 = JPA.em().createNativeQuery("SELECT trans.transcation_date,transcation_sequence_no,trans.comments,trans.input_amount,concat(facc.account_name,'-',fcur.currency_code) from_account, concat(tacc.account_name,'-',tcur.currency_code) to_account,trans.exchange_rate_1,trans.exchange_rate_2,tb.cr_amount,tb.dr_amount,tb.fc_dr_amount,tb.fc_cr_amount,tb.closing_balance,tb.fc_closing_balance,st.status_name, case when tbl.fc_dr_amount=0 then tbl.fc_cr_amount else tbl.fc_dr_amount end as amount,facc.account_id fAId,fcur.currency_id fCId,tacc.account_id tAcId, tcur.currency_id tCId from transcation_balance tb join transcation trans on tb.transcation_id=trans.transcation_id join transcation_balance tbl on tb.transcation_id=tbl.transcation_id join account facc on facc.account_id=trans.from_account_id join account tacc on tacc.account_id=trans.to_account_id join currency fcur on fcur.currency_id=trans.from_currency_id join currency tcur on tcur.currency_id=trans.to_currency_id join status st on st.status_id=trans.transcation_status_id where trans.transcation_date <=:endDate "+asOnDate+" "+fromAcc+" "+fromcurrency+" and (tbl.currency_id not in(:currencyId) or tbl.account_id not in(:fromAccId)) and trans.tenant_id=:tenantId order by trans.transcation_date asc");						
			query2.setParameter("tenantId",session("tenantId"));
			query2.setParameter("endDate", toCal.getTime());
			if(report.getStartDate()!=null && !report.getStartDate().isEmpty()){
				query2.setParameter("startDate",fromCal.getTime());
			}
			if(report.getCurrencyId()!=null && !report.getCurrencyId().isEmpty()){
				query2.setParameter("currencyId",report.getCurrencyId().toString());
			}
			if(report.getFromAccountId()!=null && !report.getFromAccountId().isEmpty()){
				query2.setParameter("fromAccId",report.getFromAccountId().toString());
			}
			if(report.getStatusId()!=null && !report.getStatusId().isEmpty()){
				query2.setParameter("statusId",report.getStatusId());
			}
			report.getCurrencyId();
			report.getFromAccountId();
			List<Object[]> results2=query2.getResultList();
			
			
			
			
			int count=0;
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			String fromcur="";
			Double crtotal=0d,balance=0d,fcdrtotal=0d,fccrtotal=0d,fcbalance=0d,bal=0d,drtotal=0d;
			String amount="";
			String cur1="";
			Query query = JPA.em().createNativeQuery("select f_opening_balance,opening_balance from account_balance where currency_id =:currencyId and account_id =:accountId and tenant_id=:tenantId");						
			query.setParameter("tenantId",session("tenantId"));
			if(report.getCurrencyId()!=null && !report.getCurrencyId().isEmpty()){
				query.setParameter("currencyId",report.getCurrencyId().toString());
			}
			if(report.getFromAccountId()!=null && !report.getFromAccountId().isEmpty()){
				query.setParameter("accountId",report.getFromAccountId().toString());
			}
			List<Object[]> results=query.getResultList();
			
			
			List<Object[]> object = new ArrayList<Object[]>();
			Long debitAmount = 0L,creditAmount=0L,fOpBal=0L,openingBalance=0L;
			if(report.getStartDate()!=null && !report.getStartDate().isEmpty()){
				String asOnDate2=" and trans.transcation_date <:startDate";
				Query balQuery = JPA.em().createNativeQuery("SELECT trans.transcation_date,transcation_sequence_no,trans.comments,trans.input_amount,concat(facc.account_name,'-',fcur.currency_code) from_account, concat(tacc.account_name,'-',tcur.currency_code) to_account,trans.exchange_rate_1,trans.exchange_rate_2,tb.cr_amount,tb.dr_amount,tb.fc_dr_amount,tb.fc_cr_amount,tb.closing_balance,tb.fc_closing_balance,st.status_name, case when tbl.fc_dr_amount=0 then tbl.fc_cr_amount else tbl.fc_dr_amount end as amount,facc.account_id fAId,fcur.currency_id fCId,tacc.account_id tAcId, tcur.currency_id tCId from transcation_balance tb join transcation trans on tb.transcation_id=trans.transcation_id join transcation_balance tbl on tb.transcation_id=tbl.transcation_id join account facc on facc.account_id=trans.from_account_id join account tacc on tacc.account_id=trans.to_account_id join currency fcur on fcur.currency_id=trans.from_currency_id join currency tcur on tcur.currency_id=trans.to_currency_id join status st on st.status_id=trans.transcation_status_id where trans.tenant_id=:tenantId "+asOnDate2+" "+fromAcc+" "+fromcurrency+" and (tbl.currency_id not in(:currencyId) or tbl.account_id not in(:fromAccId)) order by trans.transcation_date asc");						
				balQuery.setParameter("tenantId",session("tenantId"));
				if(report.getStartDate()!=null && !report.getStartDate().isEmpty()){
				  balQuery.setParameter("startDate",fromCal.getTime());
				}
				if(report.getCurrencyId()!=null && !report.getCurrencyId().isEmpty()){
					balQuery.setParameter("currencyId",report.getCurrencyId().toString());
				}
				if(report.getFromAccountId()!=null && !report.getFromAccountId().isEmpty()){
					balQuery.setParameter("fromAccId",report.getFromAccountId().toString());
				}
				if(report.getStatusId()!=null && !report.getStatusId().isEmpty()){
					balQuery.setParameter("statusId",report.getStatusId());
				}
				List<Object[]> balResults=balQuery.getResultList();
				
				
				for(Object[] objects : balResults) {
					if(report.getFlag() == 1L) {
						debitAmount +=Long.valueOf(objects[11].toString());
						creditAmount +=Long.valueOf(objects[10].toString());
						fOpBal = debitAmount-creditAmount;
					} else {
						debitAmount +=Long.valueOf(objects[8].toString());
						creditAmount +=Long.valueOf(objects[9].toString());
						fOpBal = creditAmount-debitAmount;
					}
				}
				
				for(Object[] objects : results) {
					if(report.getFlag() == 1L)
						debitAmount +=Long.valueOf(objects[0].toString());
					else
						debitAmount +=Long.valueOf(objects[1].toString());
				}
				Object[] result = new Object[2];
				result[0]=""+debitAmount;
				result[1]=""+creditAmount;
				object.add(result);
				
			}
			else {
				Query querys = JPA.em().createNativeQuery("select f_opening_balance,opening_balance from account_balance where currency_id =:currencyId and account_id =:accountId and tenant_id=:tenantId");						
				querys.setParameter("tenantId",session("tenantId"));
				if(report.getCurrencyId()!=null && !report.getCurrencyId().isEmpty()){
					querys.setParameter("currencyId",report.getCurrencyId().toString());
				}
				if(report.getFromAccountId()!=null && !report.getFromAccountId().isEmpty()){
					querys.setParameter("accountId",report.getFromAccountId().toString());
				}
				List<Object[]> result=query.getResultList();
				for(Object[] objects : result) {
					if(report.getFlag() == 1L)
						debitAmount +=Long.valueOf(objects[0].toString());
					else
						debitAmount +=Long.valueOf(objects[1].toString());
				}
				Object[] object1 = new Object[2];
				object1[0]=""+debitAmount;
				object1[1]=""+creditAmount;
				object.add(object1);
			}
			
			for(Object[] objects :object) {
				if(flag.equals("0")){   //local currency Opening balance
					if(report.getStartDate()!=null && !report.getStartDate().isEmpty()){
							if(Long.valueOf(objects[0].toString())>0) {
								drtotal=(Double) (fccrtotal+TransferController.removeExponentFromAmount(Long.valueOf(objects[0].toString())-Long.valueOf(objects[1].toString()),Long.parseLong(session("exponent"))));
								drDataSource.add("","","","Opening Balance","","","","",""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[0].toString())-Long.valueOf(objects[1].toString()),Long.parseLong(session("exponent"))),session("exponent")),"");
							}else {
								crtotal=(Double) (fcdrtotal+TransferController.removeExponentFromAmount(Long.valueOf(objects[0].toString())-Long.valueOf(objects[1].toString()),Long.parseLong(session("exponent"))));
								crtotal=(-1*crtotal);
								drDataSource.add("","","","Opening Balance","","","",""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[0].toString())-Long.valueOf(objects[1].toString()),Long.parseLong(session("exponent"))),session("exponent")),"","");
							}
					}else {
							if(Long.valueOf(objects[0].toString())>0) {
								drtotal=(Double) (fccrtotal+TransferController.removeExponentFromAmount(Long.valueOf(objects[0].toString()),Long.parseLong(session("exponent"))));
								drDataSource.add("","","","Opening Balance","","","","",""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[0].toString()),Long.parseLong(session("exponent"))),session("exponent")),"");
							}else {
								crtotal=(Double) (fcdrtotal+TransferController.removeExponentFromAmount(-1*Long.valueOf(objects[0].toString()),Long.parseLong(session("exponent"))));
								drDataSource.add("","","","Opening Balance","","","",""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[0].toString()),Long.parseLong(session("exponent"))),session("exponent")),"","");
							}
					}
					
				}else {   //Foreign Currency Opeining 
					if(report.getStartDate()!=null && !report.getStartDate().isEmpty()){
						if(Long.valueOf(objects[0].toString())>0) {
							fcdrtotal=(Double) (fcdrtotal+TransferController.removeExponentFromAmount(Long.valueOf(objects[0].toString())-Long.valueOf(objects[1].toString()),Long.parseLong(session("exponent"))));
							drDataSource.add("","","","Opening Balance","","","","",""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[0].toString())-Long.valueOf(objects[1].toString()),Long.parseLong(session("exponent"))),session("exponent")),"");
						}else {
							fccrtotal=(Double) (fccrtotal+TransferController.removeExponentFromAmount(Long.valueOf(objects[0].toString())-Long.valueOf(objects[1].toString()),Long.parseLong(session("exponent"))));
							fccrtotal=(-1*fccrtotal);
							drDataSource.add("","","","Opening Balance","","","",""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[0].toString())-Long.valueOf(objects[1].toString()),Long.parseLong(session("exponent"))),session("exponent")),"","");
						}
					}else {
						if(Long.valueOf(objects[0].toString())>0) {
							fcdrtotal=(Double) (fccrtotal+TransferController.removeExponentFromAmount(Long.valueOf(objects[0].toString()),Long.parseLong(session("exponent"))));
							drDataSource.add("","","","Opening Balance","","","","",""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[0].toString()),Long.parseLong(session("exponent"))),session("exponent")),"");
						}else {
							fccrtotal=(Double) (fcdrtotal+TransferController.removeExponentFromAmount(-1*Long.valueOf(objects[0].toString()),Long.parseLong(session("exponent"))));
							drDataSource.add("","","","Opening Balance","","","",""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[0].toString()),Long.parseLong(session("exponent"))),session("exponent")),"","");
						}
					}
					
				} 
			}
			Boolean displayFlag=false;
		    for(Object[] objects :results2) {
		    drtotal=(Double) (drtotal+TransferController.removeExponentFromAmount(Long.valueOf(objects[8].toString()),Long.parseLong(session("exponent"))));
			crtotal=(Double) (crtotal+TransferController.removeExponentFromAmount(Long.valueOf(objects[9].toString()),Long.parseLong(session("exponent"))));
			fcdrtotal=(Double) (fcdrtotal+TransferController.removeExponentFromAmount(Long.valueOf(objects[11].toString()),Long.parseLong(session("exponent"))));
			fccrtotal=(Double) (fccrtotal+TransferController.removeExponentFromAmount(Long.valueOf(objects[10].toString()),Long.parseLong(session("exponent"))));
			displayFlag=false;
			String comments="";		    
			if(objects[2] != null){
				comments=objects[2].toString();
			}
			if(flag.equals("0")){
				if((report.getAccountName()+'-'+report.getCurrencyCode()).equals(objects[5].toString())) {
					fromcur=(String) objects[5];
					String[] cur=objects[4].toString().split("-");
					if(cur[1].equals(session("currencyName"))) {
						if(objects[16].toString().equals(accountIds)) {
							displayFlag=true;
						}
						amount=objects[9].toString()+objects[8].toString();
						if(displayFlag) {
							drDataSource.add(""+dateFormat.format(objects[0]),""+objects[1],""+objects[4],""+comments,
							""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(amount),Long.parseLong(session("exponent"))),session("exponent")),
							""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[6].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
							""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[7].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
							""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[9].toString()),Long.parseLong(session("exponent"))),session("exponent")),
							""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[8].toString()),Long.parseLong(session("exponent"))),session("exponent")),
							""+ConvertDecimalFormat.convertDecimalFormat(drtotal.doubleValue()-crtotal.doubleValue(), session("exponent")));
						} else {
							drDataSource.add(""+dateFormat.format(objects[0]),""+objects[1],""+objects[4],""+comments,
									""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(amount),Long.parseLong(session("exponent"))),session("exponent")),
									"",
									"",
									""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[9].toString()),Long.parseLong(session("exponent"))),session("exponent")),
									""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[8].toString()),Long.parseLong(session("exponent"))),session("exponent")),
									""+ConvertDecimalFormat.convertDecimalFormat(drtotal.doubleValue()-crtotal.doubleValue(), session("exponent")));
						}
					}else {
						if(objects[16].toString().equals(accountIds)) {
							displayFlag=true;
						}
						if(displayFlag) {
							drDataSource.add(""+dateFormat.format(objects[0]),""+objects[1],""+objects[4],""+comments,
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[15].toString()),Long.parseLong(session("exponent"))),session("exponent")),
								""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[6].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
								""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[7].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[9].toString()),Long.parseLong(session("exponent"))),session("exponent")),
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[8].toString()),Long.parseLong(session("exponent"))),session("exponent")),
								""+ConvertDecimalFormat.convertDecimalFormat(drtotal.doubleValue()-crtotal.doubleValue(), session("exponent")));
						} else {
							drDataSource.add(""+dateFormat.format(objects[0]),""+objects[1],""+objects[4],""+comments,
									""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[15].toString()),Long.parseLong(session("exponent"))),session("exponent")),
									"",
									"",
									""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[9].toString()),Long.parseLong(session("exponent"))),session("exponent")),
									""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[8].toString()),Long.parseLong(session("exponent"))),session("exponent")),
									""+ConvertDecimalFormat.convertDecimalFormat(drtotal.doubleValue()-crtotal.doubleValue(), session("exponent")));
						}
					}
					}else if((report.getAccountName()+'-'+report.getCurrencyCode()).equals(objects[4].toString())) {
						fromcur=(String) objects[4];
						String[] cur=objects[5].toString().split("-");
						if(cur[1].equals(session("currencyName"))) {
							if(objects[18].toString().equals(accountIds)) {
								displayFlag=true;
							}
						amount=objects[8].toString()+objects[9].toString();
				         	if(displayFlag) {	
				         		drDataSource.add(""+dateFormat.format(objects[0]),""+objects[1],""+objects[5],""+comments,
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(amount),Long.parseLong(session("exponent"))),session("exponent")),
								""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[6].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
								""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[7].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[9].toString()),Long.parseLong(session("exponent"))),session("exponent")),
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[8].toString()),Long.parseLong(session("exponent"))),session("exponent")),
								""+ConvertDecimalFormat.convertDecimalFormat(drtotal.doubleValue()-crtotal.doubleValue(), session("exponent")));
				         	} else {
				         		drDataSource.add(""+dateFormat.format(objects[0]),""+objects[1],""+objects[5],""+comments,
										""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(amount),Long.parseLong(session("exponent"))),session("exponent")),
										"",
										"",
										""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[9].toString()),Long.parseLong(session("exponent"))),session("exponent")),
										""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[8].toString()),Long.parseLong(session("exponent"))),session("exponent")),
										""+ConvertDecimalFormat.convertDecimalFormat(drtotal.doubleValue()-crtotal.doubleValue(), session("exponent")));
				         	}
				         }else {
				        	 if(objects[16].toString().equals(accountIds)) {
									displayFlag=true;
								}
				        	 if(displayFlag) {
				        		 		drDataSource.add(""+dateFormat.format(objects[0]),""+objects[1],""+objects[4],""+comments,
										""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[15].toString()),Long.parseLong(session("exponent"))),session("exponent")),
										""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[6].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
										""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[7].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
										""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[9].toString()),Long.parseLong(session("exponent"))),session("exponent")),
										""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[8].toString()),Long.parseLong(session("exponent"))),session("exponent")),
										""+ConvertDecimalFormat.convertDecimalFormat(drtotal.doubleValue()-crtotal.doubleValue(), session("exponent")));
				        	 } else {
				        		 			drDataSource.add(""+dateFormat.format(objects[0]),""+objects[1],""+objects[4],""+comments,
											""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[15].toString()),Long.parseLong(session("exponent"))),session("exponent")),
											"",
											"",
											""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[9].toString()),Long.parseLong(session("exponent"))),session("exponent")),
											""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[8].toString()),Long.parseLong(session("exponent"))),session("exponent")),
											""+ConvertDecimalFormat.convertDecimalFormat(drtotal.doubleValue()-crtotal.doubleValue(), session("exponent")));
				        	 }
				         }
					}
			         }else {
			  if((report.getAccountName()+'-'+report.getCurrencyCode()).equals(objects[5].toString())) {
				fromcur=(String) objects[5];
				if(objects[16].toString().equals(accountIds)) {
					displayFlag=true;
				}
				String[] cur=objects[4].toString().split("-");
				if(cur[1].equals(session("currencyName"))) {
					 amount=objects[9].toString()+objects[8].toString();
					 if(displayFlag) {
					    	drDataSource.add(""+dateFormat.format(objects[0]),""+objects[1],""+objects[4],""+comments,
							""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(amount),Long.parseLong(session("exponent"))),session("exponent")),
							""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[6].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
							""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[7].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
							""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[10].toString()),Long.parseLong(session("exponent"))),session("exponent")),
							""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[11].toString()),Long.parseLong(session("exponent"))),session("exponent")),
							""+ConvertDecimalFormat.convertDecimalFormat(fcdrtotal.doubleValue()-fccrtotal.doubleValue(), session("exponent")));
					 } else {
							drDataSource.add(""+dateFormat.format(objects[0]),""+objects[1],""+objects[4],""+comments,
									""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(amount),Long.parseLong(session("exponent"))),session("exponent")),
									"",
									"",
									""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[10].toString()),Long.parseLong(session("exponent"))),session("exponent")),
									""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[11].toString()),Long.parseLong(session("exponent"))),session("exponent")),
									""+ConvertDecimalFormat.convertDecimalFormat(fcdrtotal.doubleValue()-fccrtotal.doubleValue(), session("exponent")));
					 }
				}else {
					if(displayFlag) {
								drDataSource.add(""+dateFormat.format(objects[0]),""+objects[1],""+objects[4],""+comments,
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[15].toString()),Long.parseLong(session("exponent"))),session("exponent")),
								""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[6].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
								""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[7].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[10].toString()),Long.parseLong(session("exponent"))),session("exponent")),
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[11].toString()),Long.parseLong(session("exponent"))),session("exponent")),
								""+ConvertDecimalFormat.convertDecimalFormat(fcdrtotal.doubleValue()-fccrtotal.doubleValue(), session("exponent")));
					} else {
						drDataSource.add(""+dateFormat.format(objects[0]),""+objects[1],""+objects[4],""+comments,
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[15].toString()),Long.parseLong(session("exponent"))),session("exponent")),
								"",
								"",
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[10].toString()),Long.parseLong(session("exponent"))),session("exponent")),
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[11].toString()),Long.parseLong(session("exponent"))),session("exponent")),
								""+ConvertDecimalFormat.convertDecimalFormat(fcdrtotal.doubleValue()-fccrtotal.doubleValue(), session("exponent")));
					}
					
				}
				}else if((report.getAccountName()+'-'+report.getCurrencyCode()).equals(objects[4].toString())) {
					fromcur=(String) objects[4];
					String[] cur=objects[5].toString().split("-");
					if(objects[18].toString().equals(accountIds)) {
						displayFlag=true;
					}
					if(cur[1].equals(session("currencyName"))) {
						amount=objects[8].toString()+objects[9].toString();
						if(displayFlag) {
							drDataSource.add(""+dateFormat.format(objects[0]),""+objects[1],""+objects[5],""+comments,
							""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(amount),Long.parseLong(session("exponent"))),session("exponent")),
							""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[6].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
							""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[7].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
							""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[10].toString()),Long.parseLong(session("exponent"))),session("exponent")),
							""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[11].toString()),Long.parseLong(session("exponent"))),session("exponent")),
							""+ConvertDecimalFormat.convertDecimalFormat(fcdrtotal.doubleValue()-fccrtotal.doubleValue(), session("exponent")));
						} else {
							drDataSource.add(""+dateFormat.format(objects[0]),""+objects[1],""+objects[5],""+comments,
									""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(amount),Long.parseLong(session("exponent"))),session("exponent")),
									"",
									"",
									""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[10].toString()),Long.parseLong(session("exponent"))),session("exponent")),
									""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[11].toString()),Long.parseLong(session("exponent"))),session("exponent")),
									""+ConvertDecimalFormat.convertDecimalFormat(fcdrtotal.doubleValue()-fccrtotal.doubleValue(), session("exponent")));
						}
					}else {
						if(displayFlag) {
								drDataSource.add(""+dateFormat.format(objects[0]),""+objects[1],""+objects[5],""+comments,
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[15].toString()),Long.parseLong(session("exponent"))),session("exponent")),
								""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[6].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
								""+Double.valueOf(ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[7].toString()),Long.parseLong(session("currencyExponent"))),session("currencyExponent"))),
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[10].toString()),Long.parseLong(session("exponent"))),session("exponent")),
								""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[11].toString()),Long.parseLong(session("exponent"))),session("exponent")),
								""+ConvertDecimalFormat.convertDecimalFormat(fcdrtotal.doubleValue()-fccrtotal.doubleValue(), session("exponent")));
						} else {
							drDataSource.add(""+dateFormat.format(objects[0]),""+objects[1],""+objects[5],""+comments,
									""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[15].toString()),Long.parseLong(session("exponent"))),session("exponent")),
									"",
									"",
									""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[10].toString()),Long.parseLong(session("exponent"))),session("exponent")),
									""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(Long.valueOf(objects[11].toString()),Long.parseLong(session("exponent"))),session("exponent")),
									""+ConvertDecimalFormat.convertDecimalFormat(fcdrtotal.doubleValue()-fccrtotal.doubleValue(), session("exponent")));
						}
					}
					
				}
				
			}
			}
		if(results2.isEmpty() && results.isEmpty()){
			drDataSource.add("","","","NO DATA","","","","","","");
		}
		
		if(flag.equals("0")){
		balance=drtotal-crtotal;
		reportParameters.put("Total","Total"); 
		reportParameters.put("drTotal",""+ConvertDecimalFormat.convertDecimalFormat(drtotal.doubleValue(), session("exponent"))); 
		reportParameters.put("crTotal",""+ConvertDecimalFormat.convertDecimalFormat(crtotal.doubleValue(),session("exponent")));
		reportParameters.put("Balance","Balance");
		if(balance>=0) {
		reportParameters.put("dr-cr",""+ConvertDecimalFormat.convertDecimalFormat(balance.doubleValue(),session("exponent")));
		}else {
		 reportParameters.put("dr-cr",""+ConvertDecimalFormat.convertDecimalFormat(balance.doubleValue(),session("exponent")));
		}
		if((report.getAccountName()+'-'+report.getCurrencyCode()).equals(fromcur)) {
			reportParameters.put("CurrencyName","CurrencyName");
			reportParameters.put("c_name",fromcur);
		}else {
			    reportParameters.put("CurrencyName","CurrencyName");
				reportParameters.put("c_name",fromcur);
		}
		}else {
			fcbalance=fcdrtotal-fccrtotal;
			reportParameters.put("Total","Total"); 
			reportParameters.put("drTotal",""+ConvertDecimalFormat.convertDecimalFormat(fcdrtotal.doubleValue(),session("exponent"))); 
			reportParameters.put("crTotal",""+ConvertDecimalFormat.convertDecimalFormat(fccrtotal.doubleValue(),session("exponent")));
			reportParameters.put("Balance","Balance");
			if(fcbalance>=0) {
			reportParameters.put("dr-cr",""+ConvertDecimalFormat.convertDecimalFormat(fcbalance.doubleValue(),session("exponent")));
			}else {
			  reportParameters.put("dr-cr",""+ConvertDecimalFormat.convertDecimalFormat(fcbalance.doubleValue(),session("exponent")));
		  }
		if((report.getAccountName()+'-'+report.getCurrencyCode()).equals(fromcur)){
				reportParameters.put("CurrencyName","CurrencyName");
				reportParameters.put("c_name",fromcur);
		}else {
				 reportParameters.put("CurrencyName","CurrencyName");
			     reportParameters.put("c_name",fromcur);
			}
		}
		}
	 
	@SuppressWarnings("unchecked")
	private void downloadReportTransferData(DRDataSource drDataSource, Map<String, Object> reportParameters,String transferId) {
		
    Query transactionQuery=JPA.em().createQuery("select X from TranscationBalance X where X.transcation.transcationId=\'"+transferId+"\' and tenant_id=:tenantId");
    transactionQuery.setParameter("tenantId",session("tenantId"));
		List<TranscationBalance> transcationList=transactionQuery.getResultList();
		for(TranscationBalance transcationBalance : transcationList){
			drDataSource.add("",transcationBalance.getAccount().getAccountName(),transcationBalance.getCurrency().getCurrencyCode(),""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(transcationBalance.getFcDrAmount(),Long.parseLong(session("exponent"))),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(transcationBalance.getFcCrAmount(),Long.parseLong(session("exponent"))),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(transcationBalance.getFcPrevBalance(),Long.parseLong(session("exponent"))),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(transcationBalance.getFcClosingBalance(),Long.parseLong(session("exponent"))),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(transcationBalance.getDrAmount(),Long.parseLong(session("exponent"))),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(transcationBalance.getCrAmount(),Long.parseLong(session("exponent"))),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(transcationBalance.getPrevBalance(),Long.parseLong(session("exponent"))),session("exponent")),""+ConvertDecimalFormat.convertDecimalFormat(TransferController.removeExponentFromAmount(transcationBalance.getClosingBalance(),Long.parseLong(session("exponent"))),session("exponent")));
		}
		
	}
	
	@SuppressWarnings({ "deprecation", "unused", "unchecked" })
	@Transactional
    public Result viewLedgerReport() throws ParseException {
		int month=0;
		Form<?> bindform=Form.form(TransferReport.class);
		TransferReport report = (TransferReport) bindform.bindFromRequest().get();
		Calendar startDate,endDate;
		String asOnDate="",fromAcc="",toAcc="",fromcurrency="",tocurrency="",statusId="";
		Calendar fromCal = Calendar.getInstance();
		if(report.getStartDate()!=null && !report.getStartDate().isEmpty()){
			
		SimpleDateFormat formatterDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		java.util.Date fromDate = formatterDateFormat.parse(report.getStartDate());
		fromCal.setTime(fromDate);
		fromCal.set(Calendar.HOUR_OF_DAY, 0);
        fromCal.set(Calendar.MINUTE, 0);
		fromCal.set(Calendar.SECOND, 0);
		fromCal.set(Calendar.MILLISECOND, 0);
		asOnDate=" and trans.transcation_date >=:startDate ";
		}
		if(report.getCurrencyId()!=null  && !report.getCurrencyId().isEmpty()){
			fromAcc=" and tb.currency_id=:currencyId ";
		}
		if(report.getFromAccountId()!=null  && !report.getFromAccountId().isEmpty()){
			fromcurrency=" and tb.account_id=:fromAccId ";
		}
		if(report.getStatusId()!=null && !report.getStatusId().isEmpty()){
			statusId= " and st.status_id=:statusId ";
		}
		SimpleDateFormat formatterDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		java.util.Date toDate = formatterDateFormat.parse(report.getEndDate());
		Calendar toCal = Calendar.getInstance();
		toCal.setTime(toDate);
		toCal.set(Calendar.HOUR_OF_DAY, 23);
		toCal.set(Calendar.MINUTE, 59);
		toCal.set(Calendar.SECOND, 59);
		toCal.set(Calendar.MILLISECOND, 999);
		Query query2 = JPA.em().createNativeQuery("SELECT trans.transcation_date,transcation_sequence_no,trans.comments,trans.input_amount,concat(facc.account_name,'-',fcur.currency_code) from_account, concat(tacc.account_name,'-',tcur.currency_code) to_account,trans.exchange_rate_1,trans.exchange_rate_2,tb.cr_amount,tb.dr_amount,tb.fc_dr_amount,tb.fc_cr_amount,tb.closing_balance,tb.fc_closing_balance,st.status_name, case when tbl.fc_dr_amount=0 then tbl.fc_cr_amount else tbl.fc_dr_amount end as amount from transcation_balance tb join transcation trans on tb.transcation_id=trans.transcation_id join transcation_balance tbl on tb.transcation_id=tbl.transcation_id join account facc on facc.account_id=trans.from_account_id join account tacc on tacc.account_id=trans.to_account_id join currency fcur on fcur.currency_id=trans.from_currency_id join currency tcur on tcur.currency_id=trans.to_currency_id join status st on st.status_id=trans.transcation_status_id where trans.transcation_date <=:endDate "+asOnDate+" "+fromAcc+" "+fromcurrency+" and (tbl.currency_id not in(:currencyId) or tbl.account_id not in(:fromAccId)) and trans.tenant_id=:tenantId order by trans.transcation_date asc");						
		query2.setParameter("tenantId",session("tenantId"));	
		if(report.getStartDate()!=null && !report.getStartDate().isEmpty()){
			query2.setParameter("startDate",fromCal.getTime());
		}
		query2.setParameter("endDate", toCal.getTime());
		if(report.getCurrencyId()!=null && !report.getCurrencyId().isEmpty()){
			query2.setParameter("currencyId",report.getCurrencyId().toString());
		}
		if(report.getFromAccountId()!=null && !report.getFromAccountId().isEmpty()){
			query2.setParameter("fromAccId",report.getFromAccountId().toString());
		}
		if(report.getStatusId()!=null && !report.getStatusId().isEmpty()){
			query2.setParameter("statusId",report.getStatusId());
		}
		List<Object[]> object = new ArrayList<Object[]>();
		if(report.getStartDate()!=null && !report.getStartDate().isEmpty()){
			String dateEntry=" and trans.transcation_date <:startDate ";
			Query balQuery = JPA.em().createNativeQuery("SELECT trans.transcation_date,transcation_sequence_no,trans.comments,trans.input_amount,concat(facc.account_name,'-',fcur.currency_code) from_account, concat(tacc.account_name,'-',tcur.currency_code) to_account,trans.exchange_rate_1,trans.exchange_rate_2,tb.cr_amount,tb.dr_amount,tb.fc_dr_amount,tb.fc_cr_amount,tb.closing_balance,tb.fc_closing_balance,st.status_name, case when tbl.fc_dr_amount=0 then tbl.fc_cr_amount else tbl.fc_dr_amount end as amount from transcation_balance tb join transcation trans on tb.transcation_id=trans.transcation_id join transcation_balance tbl on tb.transcation_id=tbl.transcation_id join account facc on facc.account_id=trans.from_account_id join account tacc on tacc.account_id=trans.to_account_id join currency fcur on fcur.currency_id=trans.from_currency_id join currency tcur on tcur.currency_id=trans.to_currency_id join status st on st.status_id=trans.transcation_status_id where trans.tenant_id=:tenantId "+dateEntry+" "+fromAcc+" "+fromcurrency+" and (tbl.currency_id not in(:currencyId) or tbl.account_id not in(:fromAccId)) order by trans.transcation_date asc");						
			balQuery.setParameter("tenantId",session("tenantId"));	
			if(report.getStartDate()!=null && !report.getStartDate().isEmpty()){
				balQuery.setParameter("startDate",fromCal.getTime());
			}
			if(report.getCurrencyId()!=null && !report.getCurrencyId().isEmpty()){
				balQuery.setParameter("currencyId",report.getCurrencyId().toString());
			}
			if(report.getFromAccountId()!=null && !report.getFromAccountId().isEmpty()){
				balQuery.setParameter("fromAccId",report.getFromAccountId().toString());
			}
			if(report.getStatusId()!=null && !report.getStatusId().isEmpty()){
				balQuery.setParameter("statusId",report.getStatusId());
			}
			Long debitAmount = 0L,fOpeningBalance = 0L,openningBalance = 0L,creditAmount=0L;
			List<Object[]> balRresults=balQuery.getResultList();
			for(Object[] objects : balRresults) {
				if(report.getFlag() == 1L) {
					debitAmount +=Long.valueOf(objects[11].toString());
					creditAmount +=Long.valueOf(objects[10].toString());
				} else {
					debitAmount +=Long.valueOf(objects[8].toString());
					creditAmount +=Long.valueOf(objects[9].toString());
				}
			}
			Query query5 = JPA.em().createNativeQuery("select f_opening_balance,opening_balance from account_balance where currency_id =:currencyId and account_id =:accountId and tenant_id=:tenantId");						
			query5.setParameter("tenantId",session("tenantId"));
			if(report.getCurrencyId()!=null && !report.getCurrencyId().isEmpty()){
				query5.setParameter("currencyId",report.getCurrencyId().toString());
			}
			if(report.getFromAccountId()!=null && !report.getFromAccountId().isEmpty()){
				query5.setParameter("accountId",report.getFromAccountId().toString());
			}
			List<Object[]> results6=query5.getResultList();
			for(Object[] objects : results6) {
				if(report.getFlag() == 1L)
					debitAmount +=Long.valueOf(objects[0].toString());
				else
					debitAmount +=Long.valueOf(objects[1].toString());
			}
			Object[] result = new Object[2];
			result[0]=""+debitAmount;
			result[1]=""+creditAmount;
			object.add(result);
			List<Object[]> results2=query2.getResultList();
			object.addAll(results2);	
		} else {
			List<Object[]> results2=query2.getResultList();
			Query query = JPA.em().createNativeQuery("select f_opening_balance,opening_balance from account_balance where currency_id =:currencyId and account_id =:accountId and tenant_id=:tenantId");						
			query.setParameter("tenantId",session("tenantId"));
			if(report.getCurrencyId()!=null && !report.getCurrencyId().isEmpty()){
				query.setParameter("currencyId",report.getCurrencyId().toString());
			}
			if(report.getFromAccountId()!=null && !report.getFromAccountId().isEmpty()){
				query.setParameter("accountId",report.getFromAccountId().toString());
			}
			List<Object[]> results=query.getResultList();
			Long debitAmount = 0L,fOpeningBalance = 0L,openningBalance = 0L,creditAmount=0L;
			for(Object[] objects : results) {
				if(report.getFlag() == 1L)
					debitAmount +=Long.valueOf(objects[0].toString());
				else
					debitAmount +=Long.valueOf(objects[1].toString());
			}
			Object[] result = new Object[2];
			result[0]=""+debitAmount;
			result[1]=""+creditAmount;
			object.add(result);
			object.addAll(results2);
		}
		return ok(new JSONArray(object).toString());	
    }
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	@Transactional
    public Result currencyReportView() {
		String currencyId="",cusId="";
		Form<?> bindform=Form.form(PostRequest.class);
		PostRequest report = (PostRequest) bindform.bindFromRequest().get();
		if(report.getPrimaryKey() != null && !report.getPrimaryKey().isEmpty()){
			currencyId=" where ab.currency_id=:currencyId";
		}
		if(report.getUrl() != null && !report.getUrl().isEmpty()){
			cusId=" where ab.account_id=:accountId";
		}
		String curQuery="fc_dr_amount dr_amount,fc_cr_amount+f_opening_balance as bal,";
		String curOrderBY="order by dr_amount asc";
		if(report.getPrimaryKey().equals(session("currencyId"))){
			curQuery="dr_amount,cr_amount+opening_balance as bal,";
			curOrderBY="order by dr_amount asc";
		}
		String cashExpCode=Play.application().configuration().getString("application.cash.expense.id");
		Query query2 = JPA.em().createNativeQuery("select dr_amount,aa.bal,currency_name,account_name,currency_code,dr_amount-bal as bal2 from ( select "+curQuery+" cur.currency_name,acc.account_name,cur.currency_code from account_balance ab"
				+" join currency cur on cur.currency_id=ab.currency_id"
				+" join account acc on acc.account_id=ab.account_id and LOWER(acc.account_code) not in (:cashExpCode)"+currencyId+" "+cusId+" and acc.tenant_id=:tenantId "+curOrderBY+" ) as aa order by bal2 desc ");
		query2.setParameter("tenantId",session("tenantId"));
		query2.setParameter("cashExpCode", Arrays.asList(cashExpCode.split(",")));
		if(report.getPrimaryKey() != null && !report.getPrimaryKey().isEmpty()){
				query2.setParameter("currencyId", report.getPrimaryKey());
			}
		if(report.getUrl() != null && !report.getUrl().isEmpty()){
				query2.setParameter("accountId", report.getUrl());
			}
		
		List<Object[]> results2=query2.getResultList();
		return ok(new JSONArray(results2).toString());
    }
	@SuppressWarnings({ "deprecation", "unchecked" })
	@Transactional
    public Result viewVendorCurrency() {
		String currencyId="",cusId="";
		Form<?> bindform=Form.form(PostRequest.class);
		PostRequest report = (PostRequest) bindform.bindFromRequest().get();
		
		if(report.getUrl() != null && !report.getUrl().isEmpty()){
			cusId=" where ab.account_id=:accountId";
		}
		String expeseCode=Play.application().configuration().getString("application.expense.accountId");
		Query query2 = JPA.em().createNativeQuery("select cr_amount,dr_amount,fc_cr_amount,fc_dr_amount,cur.currency_code,acc.account_name,ab.f_opening_balance,ab.opening_balance from account_balance ab"
				+" join currency cur on cur.currency_id=ab.currency_id"
				+" join account acc on acc.account_id=ab.account_id and acc.account_code not in ('"+expeseCode+"')"+currencyId+" "+cusId+" and ab.tenant_id=:tenantId");
		query2.setParameter("tenantId",session("tenantId"));
		if(report.getUrl() != null && !report.getUrl().isEmpty()){
				query2.setParameter("accountId", report.getUrl());
			}
		List<Object[]> results2=query2.getResultList();
		return ok(new JSONArray(results2).toString());
    }
	@Transactional
	public Result profitAndLossReport(){
	return ok(views.html.profitAndLoss.render());
	}
	@SuppressWarnings({ "unchecked", "unused" })
	@Transactional
	   public Result viewProfitAndLossReport() throws ParseException{
		int month=0;
		String flag=ctx().request().getQueryString("flag");
		String from=ctx().request().getQueryString("from");
		String to=ctx().request().getQueryString("to");
		String currency=ctx().request().getQueryString("currency");
		String accName=ctx().request().getQueryString("accName");
		String reportType=ctx().request().getQueryString("reportType");
		   String enddate="",startdate="",asOnDate="",currencyName="",accountName="",startdate1="";
			 if(from!=null && !from.isEmpty()) {
			    asOnDate="creation_date >=:startDate and ";
			    startdate1="and trans.transcation_date >=:startDate";
			 }
			 if(to!=null && !to.isEmpty()) {
				 enddate="creation_date <=:endDate";
			 }
			if(currency!=null && !currency.isEmpty()) {
				 currencyName=" and ab.currency_id=:currencyId";
			 }
			if(accName!=null && !accName.isEmpty()) {
				accountName =" and ab.account_id=:fromAccountId";
			}
			String curQuery="fc_dr_amount as dr_amount,fc_cr_amount+f_opening_balance as bal";
			if(currency.equals(session("currencyId"))){
				curQuery="dr_amount,cr_amount+opening_balance as bal";	
			}
			String currencyQuery="sum(tb.fc_cr_amount) as fc_cr_amount,sum(tb.fc_dr_amount) as fc_dr_amount";
			if(currency.equals(session("currencyId"))){
				currencyQuery="sum(tb.cr_amount) as cr_amount,sum(tb.dr_amount) as dr_amount";	
			}
			     Calendar toCal = Calendar.getInstance();
			 if(from !=null && ! from.isEmpty()) {
			   	    SimpleDateFormat formatterDateFormat1 = new SimpleDateFormat("dd-MM-yyyy");
						java.util.Date toDate1 = formatterDateFormat1.parse(from);
						toCal.setTime(toDate1);
						/*toCal.add(Calendar.MONTH, month+1);*/
						toCal.set(Calendar.HOUR_OF_DAY, 0);
						toCal.set(Calendar.MINUTE, 0);
						toCal.set(Calendar.SECOND, 0);
						toCal.set(Calendar.MILLISECOND, 0);
		   }
			Calendar fromCal = Calendar.getInstance();
			SimpleDateFormat formatterDateFormat = new SimpleDateFormat("dd-MM-yyyy");
			java.util.Date fromDate = formatterDateFormat.parse(to);
			fromCal.setTime(fromDate);
			fromCal.set(Calendar.HOUR_OF_DAY, 23);
			fromCal.set(Calendar.MINUTE, 59);
			fromCal.set(Calendar.SECOND, 59);
			fromCal.set(Calendar.MILLISECOND, 999);
			 List<Object[]> object = new ArrayList<Object[]>();
			    Object[] result = new Object[3];
			    
			 if(reportType.equals("1")) {
				 Query query=JPA.em().createNativeQuery( 
					 		"select cr_amount,dr_amount,fc_cr_amount,fc_dr_amount,cur.currency_code,acc.account_name,ab.f_opening_balance,ab.opening_balance "+ 
					 		"from account_balance ab " + 
					 		"join currency cur on cur.currency_id=ab.currency_id " + 
					 		"join account acc on acc.account_id=ab.account_id and acc.account_code not in ('exp') where "+asOnDate+" "+enddate+"  "+accountName+" and ab.tenant_id=:tenantId");
				 query.setParameter("tenantId",session("tenantId")); 
				 if(from !=null && ! from.equals("")) {
						 query.setParameter("startDate", toCal.getTime());
					 }
					 query.setParameter("endDate", fromCal.getTime());
					 if(accName !=null && ! accName.equals("")) {
						 query.setParameter("fromAccountId",accName.toString());
					 }
						
				    List<Object[]> list=query.getResultList();
				    long totalDr=0,totalCr=0,profit=0;
				    for(Object[] objects:list) {
				    	 long object0=objects[0]!=null?Long.valueOf(objects[0].toString()):0;
				    	 long object1=objects[1]!=null?Long.valueOf(objects[1].toString()):0;
				    	 long object2=objects[7]!=null?Long.valueOf(objects[7].toString()):0;
				    	 totalCr+=object0;
				    	 totalDr+=object1;
				    	 profit+=object2;
				    }
				    result[0]=""+(totalCr+profit);
					result[1]=""+totalDr;
					result[2]=""+((totalCr+profit)-totalDr);
					object.add(result);
			 }else if(reportType.equals("2")) {
				
				 Query query2 = JPA.em().createNativeQuery("SELECT sum(trans.input_amount) as input_amount,concat(tacc.account_name,'-',tcur.currency_code) to_account,trans.exchange_rate_1,"
				 		+" trans.exchange_rate_2,"+currencyQuery+" from transcation_balance tb join transcation trans on tb.transcation_id=trans.transcation_id"
				 		+" join transcation_balance tbl on tb.transcation_id=tbl.transcation_id join account facc on facc.account_id=trans.from_account_id join account tacc on tacc.account_id=trans.to_account_id"
				 		+" join currency fcur on fcur.currency_id=trans.from_currency_id join currency tcur on tcur.currency_id=trans.to_currency_id where trans.transcation_date <=:endDate "+startdate1
				 		+" and tb.currency_id=:currencyId and tbl.currency_id not in(:currencyId) and trans.tenant_id=:tenantId"
				 		+" group by to_account,trans.exchange_rate_1,trans.exchange_rate_2");						
				
				 	query2.setParameter("tenantId",session("tenantId"));	
					query2.setParameter("endDate", fromCal.getTime());
					
					 if(from!=null && !from.isEmpty()) {
						 query2.setParameter("startDate", toCal.getTime());
					 }
					 
					if(currency !=null && ! currency.equals("")) {
						query2.setParameter("currencyId",currency.toString());
					}
					List<Object[]> list=query2.getResultList();
					object.addAll(list);
		}
		else {
			 Query query=JPA.em().createNativeQuery( 
				 		"select cr_amount,dr_amount,fc_cr_amount,fc_dr_amount,cur.currency_code,acc.account_name,ab.f_opening_balance,ab.opening_balance " + 
				 		"from account_balance ab " + 
				 		"join currency cur on cur.currency_id=ab.currency_id " + 
				 		"join account acc on acc.account_id=ab.account_id and acc.account_code not in ('exp') where "+asOnDate+" "+enddate+" and ab.tenant_id=:tenantId");
			   query.setParameter("tenantId",session("tenantId"));
			   if(from !=null && ! from.equals("")) {
					 query.setParameter("startDate", toCal.getTime());
				 }
				 query.setParameter("endDate", fromCal.getTime());
				 List<Object[]> list=query.getResultList();
				 long totalDr=0,totalCr=0,profit=0;;
				 for(Object[] objects:list) {
			    	 long object0=objects[0]!=null?Long.valueOf(objects[0].toString()):0;
			    	 long object1=objects[1]!=null?Long.valueOf(objects[1].toString()):0;
			    	 long object2=objects[7]!=null?Long.valueOf(objects[7].toString()):0;
			    	 totalCr+=object0;
			    	 totalDr+=object1;
			    	 profit+=object2;	 
				 }	
				 	result[0]=""+(totalDr+profit);
					result[1]=""+totalCr;
					result[2]=""+((totalDr+profit)-totalCr);
					object.add(result);
		}			    
		return ok(new JSONArray(object).toString());	 
	}
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	@Transactional
    public Result customerNameValidate() {
		String cusName=ctx().request().getQueryString("cusName");
		String id=ctx().request().getQueryString("id");
		String idQuery="";
		if(!id.isEmpty())
			idQuery=" and customer_id not in(:id)";
		Query query=JPA.em().createNativeQuery("select customer_code,customer_name from customer where tenant_id=:tenantId and LOWER(customer_name)=:cusName "+idQuery+"");
		query.setParameter("tenantId",session("tenantId"));
		query.setParameter("cusName", cusName.toLowerCase());
		if(!id.isEmpty())
			query.setParameter("id", id);
	    List<Object[]> list=query.getResultList();
		return ok(new JSONArray(list).toString());
	}
}
