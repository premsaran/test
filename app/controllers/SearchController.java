package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.json.JSONArray;
import com.google.common.base.Objects;
import beans.TransferReport;
import entity.Account;
import entity.Currency;
import entity.Roles;
import entity.Tenant;
import entity.Transcation;
import entity.UserTenant;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

@SuppressWarnings({ "unused", "deprecation" })
public class SearchController extends Controller{

	@SuppressWarnings("unchecked")
	@Transactional
	public Result searchCurrencyCode(String value)
	{
		Query query=JPA.em().createQuery("select X from Currency X where lower(X.currencyCode) like :value ",Currency.class);
		query.setParameter("value", "%"+value.toLowerCase()+"%");
		List<Currency> currency=query.getResultList();
		org.json.JSONArray jsonArray = new org.json.JSONArray();
		for (Currency currency2 : currency) {
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("value", currency2.getCurrencyId());
			jsonObject.put("data", currency2.getCurrencyCode());
			jsonArray.put(jsonObject);
		}
		return ok(jsonArray.toString());
	}
	
	@SuppressWarnings({ "unchecked" })
	@Transactional
	public Result searchAccountName(String value)
	{
		String expeseCode=Play.application().configuration().getString("application.expense.accountId");
		List<String> accountValues = Arrays.asList(session("tenantId"),session("globalTenant"));
		Query query=JPA.em().createQuery("select X from Account X where X.tenant.tenantId IN (:accountValues) and lower(X.accountName) like :value and X.accountCode NOT IN ('"+expeseCode+"') ",Account.class);
		query.setParameter("value", "%"+value.toLowerCase()+"%");
		query.setParameter("accountValues", accountValues);
		List<Account> account=query.getResultList();
		org.json.JSONArray jsonArray = new org.json.JSONArray();
		for (Account account2 : account) {
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("value", account2.getAccountId());
			jsonObject.put("data", account2.getAccountName());
			jsonArray.put(jsonObject);
		}
		
		return ok(jsonArray.toString());
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	public Result searchAccountNameWithExp(String value)
	{
		List<String> accountValues = Arrays.asList(session("tenantId"),session("globalTenant"));
		Query query=JPA.em().createQuery("select X from Account X where X.tenant.tenantId IN (:accountValues) and lower(X.accountName) like :value ",Account.class);
		query.setParameter("value", "%"+value.toLowerCase()+"%");
		query.setParameter("accountValues", accountValues);
		List<Account> account=query.getResultList();
		org.json.JSONArray jsonArray = new org.json.JSONArray();
		for (Account account2 : account) {
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("value", account2.getAccountId());
			jsonObject.put("data", account2.getAccountName());
			jsonArray.put(jsonObject);
		}
		
		return ok(jsonArray.toString());
	}
	
	@SuppressWarnings({ "unchecked"})
	@Transactional
	public Result searchAccountNameWithoutCashandExp(String value)
	{
		List<String> accountValues = Arrays.asList(session("tenantId"),session("globalTenant"));
		String cashExpCode=Play.application().configuration().getString("application.cash.expense.id");
		Query query=JPA.em().createQuery("select X from Account X where X.tenant.tenantId IN (:accountValues) and lower(X.accountName) like :value and X.accountCode NOT IN (:accountId) ",Account.class);
		query.setParameter("value", "%"+value.toLowerCase()+"%");
		query.setParameter("accountId",Arrays.asList(cashExpCode.split(",")));
		query.setParameter("accountValues", accountValues);
		List<Account> account=query.getResultList();
		org.json.JSONArray jsonArray = new org.json.JSONArray();
		for (Account account2 : account) {
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("value", account2.getAccountId());
			jsonObject.put("data", account2.getAccountName());
			jsonArray.put(jsonObject);
		}
		
		return ok(jsonArray.toString());
	}
	
	@SuppressWarnings({ "unchecked" })
	@Transactional
	public Result fromToDateTransactionDetails() throws ParseException{
		String enddate="",startdate="";
		String fromDate=ctx().request().getQueryString("from");
	    Calendar fromCal = Calendar.getInstance();
		SimpleDateFormat formatterDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		java.util.Date froMDate = formatterDateFormat.parse(fromDate);
		fromCal.setTime(froMDate);
		fromCal.set(Calendar.HOUR_OF_DAY, 0);
		fromCal.set(Calendar.MINUTE, 0);
		fromCal.set(Calendar.SECOND, 0);
		fromCal.set(Calendar.MILLISECOND,0);
		startdate=" X.transcationDate >=\'"+fromCal.getTime()+"\' and ";
		
		String toDate=ctx().request().getQueryString("to");
		SimpleDateFormat formatterDateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
		java.util.Date tODate = formatterDateFormat1.parse(toDate);
		Calendar toCal1 = Calendar.getInstance();
		toCal1.setTime(tODate);
		toCal1.set(Calendar.HOUR_OF_DAY, 23);
		toCal1.set(Calendar.MINUTE, 59);
		toCal1.set(Calendar.SECOND, 59);
		toCal1.set(Calendar.MILLISECOND, 999);
		enddate="X.transcationDate <=\'"+toCal1.getTime()+"\'";
	    String statusActiveId=Play.application().configuration().getString("application.status.active.id");
		Query query=JPA.em().createQuery("select X from Transcation X where X.tenant.tenantId =:tenantId and "+startdate+" "+enddate+" and X.transcationStatus.statusId='"+statusActiveId+"' ORDER BY X.transcationDate DESC");
		query.setParameter("tenantId",session("tenantId"));
		List<Transcation> trans=query.getResultList();
		org.json.JSONArray jsonArray = new org.json.JSONArray();
		for (Transcation transcation : trans) {
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("transcationDate", transcation.getTranscationDate());
			jsonObject.put("transSeqNo", transcation.getTransSeqNo());
			jsonObject.put("transcationId",transcation.getTranscationId());
			jsonObject.put("userName", transcation.getUser().getUserName());
			jsonObject.put("fromAccount", transcation.getFromAccount().getAccountName());
			jsonObject.put("toAccount", transcation.getToAccount().getAccountName());
			jsonObject.put("fromCurrency", transcation.getFromCurrency().getCurrencyCode());
			jsonObject.put("toCurrency", transcation.getToCurrency().getCurrencyCode());
			jsonObject.put("inputAmount",transcation.getInputAmount());
			jsonObject.put("transcationStatus", transcation.getTranscationStatus().getStatusName());
			jsonArray.put(jsonObject);
		}
		return ok(jsonArray.toString());
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	public Result searchRoleName(String value)
	{
		Query query=JPA.em().createQuery("select X from Roles X where lower(X.roleName) like :value ",Roles.class);
		query.setParameter("value", "%"+value.toLowerCase()+"%");
		List<Roles> roles=query.getResultList();
		org.json.JSONArray jsonArray = new org.json.JSONArray();
		for (Roles roles2 : roles) {
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("value", roles2.getRoleId());
			jsonObject.put("data", roles2.getRoleName());
			jsonArray.put(jsonObject);
		}
		
		return ok(jsonArray.toString());
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	public Result searchTenantName(String value)
	{
		Query query=JPA.em().createQuery("select X from Tenant X where lower(X.tenantName) like :value ",Tenant.class);
		query.setParameter("value", "%"+value.toLowerCase()+"%");
		List<Tenant> tenant=query.getResultList();
		org.json.JSONArray jsonArray = new org.json.JSONArray();
		for (Tenant tenant2 : tenant) {
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("value",tenant2.getTenantId());
			jsonObject.put("data",tenant2.getTenantName());
			jsonArray.put(jsonObject);
		}
		return ok(jsonArray.toString());
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	public Result getUserTenant(){
		Query query=JPA.em().createQuery("select X from UserTenant X where X.userId.userId=:userId");
		query.setParameter("userId", ctx().request().getQueryString("userId"));
		List<UserTenant> userTenant=query.getResultList();
		return ok(new JSONArray(userTenant).toString());
	}
}
