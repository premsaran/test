package controllers;

import play.db.jpa.Transactional;
import entity.Account;
import entity.AccountBalance;
import entity.Currency;
import entity.Customer;
import entity.ExchangeRates;
import entity.Transcation;
import entity.TranscationBalance;
import entity.User;
import play.Play;
import play.data.Form;
import play.db.jpa.JPA;
import play.mvc.*;
import play.twirl.api.Format;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.json.JSONArray;
import beans.AccountBalanceBo;
import beans.PostRequest;
import views.html.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class FinanceController extends Controller{

	@SuppressWarnings("unchecked")
	@Transactional
    public Result viewAccountBalanceList() {
		Query query=JPA.em().createQuery("select X from AccountBalance X where X.tenant.tenantId=:tenantId  ORDER BY X.creationDate DESC");
		query.setParameter("tenantId",session("tenantId"));
		List<AccountBalance> accountBalanceList=query.getResultList();
        return ok(openingBalanceList.render(accountBalanceList));
    }
	
	@SuppressWarnings("unchecked")
	@Transactional
    public Result createAccountBalance() {
		Query query=JPA.em().createQuery("select X from Account X where X.tenant.tenantId=:tenantId");
		query.setParameter("tenantId",session("tenantId"));
		List<Account> accountList=query.getResultList();
		Query query2=JPA.em().createQuery("select X from Currency X");
		List<Currency> currencylist=query2.getResultList();
		Query accountbal=JPA.em().createQuery("select X from AccountBalance X where X.tenant.tenantId=:tenantId");
		accountbal.setParameter("tenantId",session("tenantId"));
		List<AccountBalance> accountListbal=accountbal.getResultList();
        return ok(createOpeningBalance.render(accountList,currencylist,accountListbal));
    }
	
	@SuppressWarnings("deprecation")
	@Transactional
	public Result saveAccountBalance() {
		Form<?> balanceform=Form.form(AccountBalanceBo.class);
		AccountBalanceBo accountBalanceBo = (AccountBalanceBo) balanceform.bindFromRequest().get();
		for(AccountBalance accountBalance:accountBalanceBo.getAccountBalance()){
			accountBalance.setAccountBalanceId(UUID.randomUUID().toString());
			accountBalance.setDrAmount(0L);
			accountBalance.setCrAmount(0L);
			accountBalance.setFcCrAmount(0L);
			accountBalance.setFcDrAmount(0L);
			accountBalance.setCreationDate(new Date());
			JPA.em().persist(accountBalance);
		}
		return redirect(routes.FinanceController.viewAccountBalanceList());
	}
	
	@SuppressWarnings("deprecation")
	@Transactional
	public Result updateAccountBalance() {
		Form<?> accountBalanceform=Form.form(AccountBalance.class);
		AccountBalance accountBalance = (AccountBalance) accountBalanceform.bindFromRequest().get();
		JPA.em().merge(accountBalance);
		return redirect(routes.FinanceController.viewAccountBalanceList());
	}
	
	@SuppressWarnings("unchecked")
	public static Long getBalance(String accountId, String currencyId) {
		Long balance=0L;
		
		Query query=JPA.em().createQuery("select X from AccountBalance X Where X.account.accountId=:accountId and X.currency.currencyId=:currencyId and X.tenant.tenantId=:tenantId");
		query.setParameter("accountId", accountId);
		query.setParameter("currencyId", currencyId);
		query.setParameter("tenantId",session("tenantId"));
		List<AccountBalance> accountBalanceList=query.getResultList();
		if(accountBalanceList.size() > 0){
			if(accountBalanceList.get(0).getCurrency().getCurrencyId().equals(session("currencyId"))){
				balance = accountBalanceList.get(0).getOpeningBalance()+(accountBalanceList.get(0).getCrAmount() - accountBalanceList.get(0).getDrAmount());
			}else {
				balance = accountBalanceList.get(0).getfOpeningBalance()+(accountBalanceList.get(0).getFcCrAmount() - accountBalanceList.get(0).getFcDrAmount());
			}
		}
		return balance;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	@Transactional
    public Result getExchangeRate() {
		Form<?> bindform=Form.form(PostRequest.class);
		PostRequest report = (PostRequest) bindform.bindFromRequest().get();
		List<ExchangeRates> list=null;
		Boolean flag=true;
		Query query = null;
			query=JPA.em().createQuery("select x from ExchangeRates x where x.fromCurrency.currencyId=:fromCurrency and x.toCurrency.currencyId=:toCurrency and x.tenant.tenantId=:tenantId");
			query.setParameter("fromCurrency", report.getValue1());
			query.setParameter("toCurrency", report.getValue2());
			query.setParameter("tenantId",session("tenantId"));
			list=query.getResultList();
			if(list.size()==0){
				flag=false;
				query=JPA.em().createQuery("select x from ExchangeRates x where x.fromCurrency.currencyId=:fromCurrency and x.toCurrency.currencyId=:toCurrency and x.tenant.tenantId=:tenantId");
				query.setParameter("fromCurrency", report.getValue2());
				query.setParameter("toCurrency", report.getValue1());
				query.setParameter("tenantId",session("tenantId"));
				list=query.getResultList();
			}
			
		
		org.json.JSONArray jsonArray = new org.json.JSONArray();
		for (ExchangeRates exchangeRates : list) {
			org.json.JSONObject jsonObject = new org.json.JSONObject();
				if(flag) {
					jsonObject.put("exchangeRateMultiply", exchangeRates.getExchangeRateDivided());
					jsonObject.put("exchangeRateDivided", exchangeRates.getExchangeRateMultiply());
				}else {
					jsonObject.put("exchangeRateMultiply", exchangeRates.getExchangeRateMultiply());
					jsonObject.put("exchangeRateDivided", exchangeRates.getExchangeRateDivided());
					}
				jsonArray.put(jsonObject);
		}
		return ok(jsonArray.toString());
		
		
    }
	
	@SuppressWarnings("deprecation")
	@Transactional
    public Result getAccountBalance() {
		Form<?> bindform=Form.form(PostRequest.class);
		PostRequest formValues = (PostRequest) bindform.bindFromRequest().get();
		Long balance=getBalance(formValues.getValue1(), formValues.getValue2());
		
		Long updateBal=0L;
		if(formValues.getDivideFlag() != null && !formValues.getDivideFlag().isEmpty()){
			String divideFlag=formValues.getDivideFlag();
			String amt=formValues.getValue3();
			String toCurrencyId=formValues.getToCurrencyId();
			String fromCurrencyId=formValues.getFromCurrencyId();
			String fromAccountId=formValues.getFromAccountId();
			String toAccountId=formValues.getToAccountId();
			String inputAmt=formValues.getValue4();
			String exRate1=formValues.getExRate1();
			String exRate2=formValues.getExRate2();
			String currencyId=formValues.getCurrencyId();
			String type=formValues.getType();
			Transcation transcation = new Transcation();
			transcation.setFromCurrency(JPA.em().find(Currency.class,fromCurrencyId));
			transcation.setToCurrency(JPA.em().find(Currency.class,toCurrencyId));
			transcation.setFromAccount(JPA.em().find(Account.class,fromAccountId));
			transcation.setToAccount(JPA.em().find(Account.class,toAccountId));
			transcation.setInputAmount(Long.valueOf(inputAmt));
			transcation.setMultiplyDivideFlag(Long.valueOf(divideFlag));
			transcation.setFromLocalAmount(Long.valueOf(amt));
			transcation.setToLocalAmount(Long.valueOf(amt));
			transcation.setExchangeRate1(Long.valueOf(exRate1));
			transcation.setExchangeRate2(Long.valueOf(exRate2));
			transcation.setCurrency(JPA.em().find(Currency.class,currencyId));
			
			if(Long.valueOf(transcation.getFromLocalAmount().toString()) == 0L){
				transcation.setFromLocalAmount(Long.valueOf(inputAmt));
				transcation.setToLocalAmount(Long.valueOf(inputAmt));
			} 
			
			Long fcCrAmount=0L,fcDrAmount=0L,drAmount=0L,crAmount=0L;
			if(formValues.getType().equals("1")){
				drAmount=Long.valueOf(amt);
			} else{
				crAmount=Long.valueOf(amt);
			}
			boolean flag=true;
			
			if(transcation.getMultiplyDivideFlag().equals(1L)) {
				if(!transcation.getFromCurrency().getCurrencyId().equals(session("currencyId")) && !transcation.getToCurrency().getCurrencyId().equals(session("currencyId"))  && transcation.getCurrency() != null  && !transcation.getToCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId()) && transcation.getFromCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId())) {
					fcCrAmount = TransferController.addExponentToAmount(TransferController.removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) * TransferController.removeExponentFromAmount(transcation.getExchangeRate1(),Long.parseLong(session("currencyExponent"))),session("exponent"));
					drAmount = TransferController.addExponentToAmount(TransferController.removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) * TransferController.removeExponentFromAmount(transcation.getExchangeRate2(),Long.parseLong(session("currencyExponent"))),session("exponent"));
					crAmount=drAmount;
					fcDrAmount=transcation.getInputAmount();
					flag=false;
				}
				if(!transcation.getToCurrency().getCurrencyId().equals(session("currencyId")) && !transcation.getFromCurrency().getCurrencyId().equals(session("currencyId")) && transcation.getCurrency() != null && transcation.getToCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId()) && !transcation.getFromCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId())) {
					fcDrAmount = TransferController.addExponentToAmount(TransferController.removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) * TransferController.removeExponentFromAmount(transcation.getExchangeRate1(),Long.parseLong(session("currencyExponent"))),session("exponent"));
					fcCrAmount=transcation.getInputAmount();
					flag=false;
				}
				if(flag && transcation.getFromCurrency().getCurrencyId().equals(session("currencyId")) && transcation.getCurrency() != null && !transcation.getToCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId())){
					fcCrAmount = TransferController.addExponentToAmount(TransferController.removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) * TransferController.removeExponentFromAmount(transcation.getExchangeRate1(),Long.parseLong(session("currencyExponent"))),session("exponent"));
					drAmount = transcation.getInputAmount();
					crAmount=transcation.getInputAmount();
					flag=false;
				}
				if(flag && transcation.getToCurrency().getCurrencyId().equals(session("currencyId")) && transcation.getCurrency() != null && transcation.getToCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId())){
					fcDrAmount = TransferController.addExponentToAmount(TransferController.removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) * TransferController.removeExponentFromAmount(transcation.getExchangeRate1(),Long.parseLong(session("currencyExponent"))),session("exponent"));
					drAmount = transcation.getInputAmount();
					crAmount=transcation.getInputAmount();
					flag=false;
				}
				}else {
					if(!transcation.getFromCurrency().getCurrencyId().equals(session("currencyId")) && !transcation.getToCurrency().getCurrencyId().equals(session("currencyId"))  && transcation.getCurrency() != null  && !transcation.getToCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId()) && transcation.getFromCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId())) {
						fcCrAmount = TransferController.addExponentToAmount(TransferController.removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) / TransferController.removeExponentFromAmount(transcation.getExchangeRate1(),Long.parseLong(session("currencyExponent"))),session("exponent"));
						drAmount = TransferController.addExponentToAmount(TransferController.removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) / TransferController.removeExponentFromAmount(transcation.getExchangeRate2(),Long.parseLong(session("currencyExponent"))),session("exponent"));
						crAmount=drAmount;
						fcDrAmount=transcation.getInputAmount();
						flag=false;
					}
					if(!transcation.getToCurrency().getCurrencyId().equals(session("currencyId")) && !transcation.getFromCurrency().getCurrencyId().equals(session("currencyId")) && transcation.getCurrency() != null && transcation.getToCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId()) && !transcation.getFromCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId())) {
						fcDrAmount = TransferController.addExponentToAmount(TransferController.removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) / TransferController.removeExponentFromAmount(transcation.getExchangeRate1(),Long.parseLong(session("currencyExponent"))),session("exponent"));
						fcCrAmount=transcation.getInputAmount();
						flag=false;
					}
					if(flag && transcation.getFromCurrency().getCurrencyId().equals(session("currencyId")) && transcation.getCurrency() != null && !transcation.getToCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId())){
						fcCrAmount = TransferController.addExponentToAmount(TransferController.removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) / TransferController.removeExponentFromAmount(transcation.getExchangeRate1(),Long.parseLong(session("currencyExponent"))),session("exponent"));
						drAmount = transcation.getInputAmount();
						crAmount=transcation.getInputAmount();
						flag=false;
					}
					if(flag && transcation.getToCurrency().getCurrencyId().equals(session("currencyId")) && transcation.getCurrency() != null && transcation.getToCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId())){
						fcDrAmount = TransferController.addExponentToAmount(TransferController.removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) / TransferController.removeExponentFromAmount(transcation.getExchangeRate1(),Long.parseLong(session("currencyExponent"))),session("exponent"));
						drAmount = transcation.getInputAmount();
						crAmount=transcation.getInputAmount();
						flag=false;
					}
				}
				if(flag && !transcation.getToCurrency().getCurrencyId().equals(session("currencyId"))) 
					fcCrAmount=transcation.getInputAmount();
				if(flag && !transcation.getFromCurrency().getCurrencyId().equals(session("currencyId"))) 
					fcDrAmount=transcation.getInputAmount();
				if(type.equals("1")){
					fcCrAmount=0L;
					crAmount=0L;
				} else{
					fcDrAmount=0L;
					drAmount=0L;
				}
				TypedQuery<AccountBalance> accountQuery = JPA.em().createQuery("select x from AccountBalance x where tenant_id=:tenantId and x.account.accountId=:accountId and x.currency.currencyId=:currencyId",AccountBalance.class);
				if(type.equals("1")){
					accountQuery.setParameter("accountId", fromAccountId);
					accountQuery.setParameter("currencyId", transcation.getFromCurrency().getCurrencyId());
				} else {
					accountQuery.setParameter("accountId", toAccountId);
					accountQuery.setParameter("currencyId", transcation.getToCurrency().getCurrencyId());
				}
				
				accountQuery.setParameter("tenantId",session("tenantId"));
				List<AccountBalance> results = accountQuery.getResultList();
				TranscationBalance transcationBalance = new TranscationBalance();
				
				transcationBalance.setClosingBalance(crAmount-drAmount);
				transcationBalance.setFcClosingBalance(fcCrAmount-fcDrAmount);
				for(AccountBalance objects : results){
					transcationBalance.setPrevBalance(objects.getOpeningBalance() + (objects.getCrAmount()-objects.getDrAmount()));
					transcationBalance.setClosingBalance(objects.getOpeningBalance() + (objects.getCrAmount()-objects.getDrAmount()+(crAmount-drAmount)));
					if(!objects.getCurrency().getCurrencyId().equals(session("currencyId"))) {
						transcationBalance.setFcPrevBalance(objects.getfOpeningBalance() + (objects.getFcCrAmount()-objects.getFcDrAmount()));
						transcationBalance.setFcClosingBalance(objects.getfOpeningBalance() + (objects.getFcCrAmount()-objects.getFcDrAmount())+(fcCrAmount-fcDrAmount));
					}
				}
				
				if(type.equals("1")){
					if(!transcation.getFromCurrency().getCurrencyId().equals(session("currencyId"))){
						updateBal=transcationBalance.getFcClosingBalance();
					} else {
						updateBal=transcationBalance.getClosingBalance();
					}
				} else {
					if(!transcation.getToCurrency().getCurrencyId().equals(session("currencyId"))){
						updateBal=transcationBalance.getFcClosingBalance();
					} else {
						updateBal=transcationBalance.getClosingBalance();
					}
				
				}
		}
		org.json.JSONArray jsonArray = new org.json.JSONArray();
		jsonArray.put(balance);
		jsonArray.put(updateBal);
		return ok(jsonArray.toString());
    }
	
	
	
}
