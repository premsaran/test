package controllers;

import java.util.List;
import java.util.UUID;
import javax.persistence.Query;

import beans.CheckDublicateBo;
import beans.CurrencyBo;
import beans.CustomerBo;
import beans.ExchangeRateBo;
import beans.PostRequest;
import entity.Account;
import entity.Currency;
import entity.Customer;
import entity.ExchangeRates;
import entity.Roles;
import entity.Tenant;
import play.data.Form;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import entity.Transcation;
import entity.TranscationBalance;
import entity.User;
import play.mvc.*;
import play.twirl.api.Format;
import javax.persistence.TypedQuery;
import org.json.JSONArray;
import views.html.*;

public class MasterController extends Controller{	


	@SuppressWarnings("deprecation")
	@Transactional
	public Result createCustomer() {
		Form<?> customerform=Form.form(CustomerBo.class);
		CustomerBo currencyBo = (CustomerBo) customerform.bindFromRequest().get();

		for(Customer customer:currencyBo.getCustomers()){
			customer.setCustomerId(UUID.randomUUID().toString());
			JPA.em().persist(customer);
			Account account=new Account();
			account.setAccountId(UUID.randomUUID().toString());
			account.setAccountCode(customer.getCustomerCode());
			account.setAccountName(customer.getCustomerName());
			account.setCustomer(customer);
			account.setTenant(JPA.em().find(Tenant.class,session("tenantId")));
			JPA.em().persist(account);
		}
		return redirect(routes.HomeController.viewCustomer());
	}


	@SuppressWarnings("deprecation")
	@Transactional
	public Result createCurrency() {
		Form<?> currencyform=Form.form(CurrencyBo.class);
		CurrencyBo currencyBo = (CurrencyBo) currencyform.bindFromRequest().get();
		for(Currency currency:currencyBo.getCurrency()){
			currency.setCurrencyId(UUID.randomUUID().toString());
			JPA.em().persist(currency);
		}
		return redirect(routes.HomeController.viewCurrency());
	}

	@SuppressWarnings("deprecation")
	@Transactional
	public Result updateCurrency() {
		Form<?> currencyform=Form.form(Currency.class);
		Currency currency = (Currency) currencyform.bindFromRequest().get();
		JPA.em().merge(currency);
		return redirect(routes.HomeController.viewCurrency());
	}

	@SuppressWarnings("deprecation")
	@Transactional
	public Result updateCustomer() {
		Form<?> currencyform=Form.form(Customer.class);
		Customer customer = (Customer) currencyform.bindFromRequest().get();
		String customerId=customer.getCustomerId();
		JPA.em().merge(customer);
		
		Customer customer2 = JPA.em().find(Customer.class,customerId);
		Query query=JPA.em().createNativeQuery("select account_id from account where tenant_id=:tenantId and customer_id=:customerId");
		query.setParameter("tenantId",session("tenantId"));
		query.setParameter("customerId",customerId);
		
		Account account = JPA.em().find(Account.class,query.getSingleResult().toString());
		Account account2 = new Account();
		account2.setAccountCode(customer2.getCustomerCode());
		account2.setAccountId(account.getAccountId());
		account2.setAccountName(customer2.getCustomerName());
		account2.setCustomer(customer2);
		account2.setTenant(customer2.getTenant());
		JPA.em().merge(account2);
		return redirect(routes.HomeController.viewCustomer());
	}

	@SuppressWarnings("deprecation")
	@Transactional
	public Result addExchange(){
		Form<?> form=Form.form(ExchangeRateBo.class);
		ExchangeRateBo list=(ExchangeRateBo) form.bindFromRequest().get();
		for(ExchangeRates exchangeRates :  list.getExchangerates()) {
			exchangeRates.setExchangeRateId(UUID.randomUUID().toString());
			JPA.em().persist(exchangeRates);
		}
		return redirect(routes.MasterController.viewExchange());
	}

	@SuppressWarnings("deprecation")
	@Transactional
	public Result updateExchange(){
		Form<?> form=Form.form(ExchangeRateBo.class);
		ExchangeRateBo list=(ExchangeRateBo) form.bindFromRequest().get();
		for(ExchangeRates exchangeRates :  list.getExchangerates()) {
			JPA.em().merge(exchangeRates);
		}
		return redirect(routes.MasterController.viewExchange());
	}

	@Transactional
	public Result createExchange(String viewType) {
		return ok(views.html.addExchange.render(viewType));
	}
	@SuppressWarnings("unchecked")
	@Transactional
	public Result viewExchange() {
		Query query=JPA.em().createQuery("select X from ExchangeRates X where X.tenant.tenantId=:tenantId");
		query.setParameter("tenantId",session("tenantId"));
		List <ExchangeRates> list=query.getResultList();
		return ok(views.html.viewExchange.render(list));
	}
	@SuppressWarnings("unchecked")
	@Transactional
	public Result searchTransNo(String transNo)
	{
		Query query=JPA.em().createQuery("select X from Transcation X where X.tenant.tenantId=:tenantId and lower(transSeqNo) like lower('%"+transNo+"%')");
		query.setParameter("tenantId",session("tenantId"));
		List<Transcation> trans=query.getResultList();
		org.json.JSONArray jsonArray = new org.json.JSONArray();
		for (Transcation tran : trans) {
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("data", tran.getTransSeqNo());
			jsonObject.put("value", tran.getTranscationId());
			jsonArray.put(jsonObject);
		}
		return ok(jsonArray.toString());
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public Result transAction(String transNo)
	{
		Query query=JPA.em().createQuery("select X from Transcation X where transcationId = '"+transNo+"' and X.tenant.tenantId=:tenantId");
		query.setParameter("tenantId",session("tenantId"));
		List<Transcation> trans=query.getResultList();
		org.json.JSONArray jsonArray = new org.json.JSONArray();
		for (Transcation transcation : trans) {
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("transcationId",transcation.getTranscationId());
			jsonObject.put("transcationStatus", transcation.getTranscationStatus().getStatusId());
			jsonObject.put("transcationStatusName", transcation.getTranscationStatus().getStatusName());
			if(transcation.getRecallUser()!=null && !transcation.getRecallUser().equals("")) {
				jsonObject.put("recallUser", transcation.getRecallUser().getUserName());
			}
			if(transcation.getRecallDate()!=null) {
				jsonObject.put("recallDate", transcation.getRecallDate());
			}
			if(transcation.getDeleteUser()!=null && !transcation.getDeleteUser().equals("")) {
				jsonObject.put("deleteUser",transcation.getDeleteUser().getUserName());
			}
			if(transcation.getDeleteDate()!=null) {
				jsonObject.put("deleteDate",transcation.getDeleteDate());
			}
			jsonObject.put("transcationDate",transcation.getTranscationDate());
			jsonObject.put("inputAmount", transcation.getInputAmount());
			jsonObject.put("fromAccount", transcation.getFromAccount().getAccountName());
			jsonObject.put("toAccount",transcation.getToAccount().getAccountName());
			jsonObject.put("user", transcation.getUser().getUserName());
			jsonObject.put("transcationDate", transcation.getTranscationDate());
			jsonObject.put("fromCurrency", transcation.getFromCurrency().getCurrencyId());
			jsonObject.put("toCurrency", transcation.getToCurrency().getCurrencyId());
			jsonObject.put("comments",transcation.getComments());
			if(transcation.getDeleteComments()!=null && !transcation.getDeleteComments().isEmpty()) {
				jsonObject.put("deleteComments",transcation.getDeleteComments());
			}
			if(transcation.getRecallComments()!=null && !transcation.getRecallComments().isEmpty()) {
				jsonObject.put("recallComments",transcation.getRecallComments());
			}
			jsonObject.put("fromLocalAmount",transcation.getFromLocalAmount());
			jsonObject.put("toLocalAmount",transcation.getToLocalAmount());
			jsonObject.put("exchangeRate1",transcation.getExchangeRate1());
			jsonObject.put("exchangeRate2",transcation.getExchangeRate2());
			if(transcation.getCurrency() != null){
				jsonObject.put("currencyId",transcation.getCurrency().getCurrencyId());
				jsonObject.put("currencyCode",transcation.getCurrency().getCurrencyCode());
			}
			jsonArray.put(jsonObject);
		}
		return ok(jsonArray.toString());
	}
	@SuppressWarnings({ "unchecked", "unused" })
	@Transactional
	public Result deleteRecallTransaction(String transID)
	{
		Query query=JPA.em().createQuery("select X from TranscationBalance X where X.transcation.transcationId = '"+transID+"' and X.tenant.tenantId=:tenantId");
		query.setParameter("tenantId",session("tenantId"));
		List<TranscationBalance> transb=query.getResultList();
		org.json.JSONArray jsonArray = new org.json.JSONArray();
		for (TranscationBalance transcation : transb) {
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("accountName",transcation.getAccount().getAccountName());
			jsonObject.put("currencyCode",transcation.getCurrency().getCurrencyCode());
			jsonObject.put("fcClosingBalance",transcation.getFcClosingBalance());
			jsonObject.put("fcPrevBalance",transcation.getFcPrevBalance());
			jsonObject.put("closingBalance",transcation.getClosingBalance());
			jsonObject.put("prevBalance",transcation.getPrevBalance());
			jsonArray.put(jsonObject);
		}
		return ok(jsonArray.toString());
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Transactional
	public Result setCurrencyValues() {
		Form<?> bindform=Form.form(PostRequest.class);
		PostRequest report = (PostRequest) bindform.bindFromRequest().get();
		Query query=null;
		if(report.getValue1() != null && !report.getValue1().equals("")) {
			query=JPA.em().createQuery("select x from ExchangeRates x where x.fromCurrency.currencyId=:fromCurrency and  X.tenant.tenantId=:tenantId");
			query.setParameter("tenantId",session("tenantId"));
			query.setParameter("fromCurrency", report.getValue1());
		}else {
			//			if(report.getValue2() != null && !report.getValue2().equals("")) 
			query=JPA.em().createQuery("select x from ExchangeRates x where x.toCurrency.currencyId=:toCurrency and X.tenant.tenantId=:tenantId");
			query.setParameter("tenantId",session("tenantId"));
			query.setParameter("toCurrency", report.getValue2());
		}
		List<ExchangeRates> list=query.getResultList();
		org.json.JSONArray jsonArray = new org.json.JSONArray();
		for (ExchangeRates exchangeRates : list) {
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("exchangeRateId", exchangeRates.getExchangeRateId());
			jsonObject.put("fromCurrencyId", exchangeRates.getFromCurrency().getCurrencyId());
			jsonObject.put("fromCurrencyName", exchangeRates.getFromCurrency().getCurrencyName());
			jsonObject.put("toCurrencyId", exchangeRates.getToCurrency().getCurrencyId());
			jsonObject.put("toCurrencyName", exchangeRates.getToCurrency().getCurrencyName());
			jsonObject.put("exchangeRateMultiply", exchangeRates.getExchangeRateMultiply());
			jsonObject.put("exchangeRateDivided", exchangeRates.getExchangeRateDivided());
			jsonArray.put(jsonObject);
		}
		return ok(jsonArray.toString());
	}

	@SuppressWarnings("deprecation")
	@Transactional
	public Result checkDublicate(){
		Form<?> bindform=Form.form(CheckDublicateBo.class);
		CheckDublicateBo checkDublicateBo = (CheckDublicateBo) bindform.bindFromRequest().get();
		Query query=JPA.em().createQuery("select * from "+checkDublicateBo.getTableName()+" where "+checkDublicateBo.getColumnName()+"='"+checkDublicateBo.getText()+"' and tenant_id=:tenantId");
		query.setParameter("tenantId",session("tenantId"));
		if(query.getResultList().size()>0)
			return ok("Given Name is Dublicate...");
		else
			return ok("");
	}
}