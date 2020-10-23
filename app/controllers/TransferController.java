package controllers;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.Query;
import entity.Account;
import entity.AccountBalance;
import entity.Currency;
import entity.Transcation;
import entity.TranscationBalance;
import play.Play;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import services.SequenceGen;
import entity.Customer;
import entity.Status;
import entity.Tenant;
import entity.User;
import play.mvc.*;

import javax.persistence.TypedQuery;

import org.json.JSONArray;

import beans.PostRequest;
import views.html.*;

public class TransferController extends Controller{


	@SuppressWarnings({ "unchecked", "deprecation" })
	@Transactional
	public Result createTransfer() throws Exception{
		Form<?> bindform=Form.form(Transcation.class);
		Transcation transcation = (Transcation) bindform.bindFromRequest().get();
		transcation.setTranscationId(UUID.randomUUID().toString());
		transcation.setTransSeqNo(SequenceGen.generate(Play.application().configuration().getString("application.report.transactionSeq.name"),session("tenantId")));
		if(Long.valueOf(transcation.getFromLocalAmount().toString()) == 0L){
			transcation.setFromLocalAmount(transcation.getInputAmount());
			transcation.setToLocalAmount(transcation.getInputAmount());
		}  
		JPA.em().persist(transcation);
		accountEntry(transcation);
		flash("tranId",transcation.getTranscationId());
		return redirect(routes.HomeController.viewTransfer());
	}
	private void accountEntry(Transcation transcation) {
		Long fcCrAmount=0L,fcDrAmount=0L,drAmount=transcation.getFromLocalAmount(),crAmount=transcation.getToLocalAmount();
		boolean flag=true;
		if(transcation.getMultiplyDivideFlag().equals(1L)) {
		if(!transcation.getFromCurrency().getCurrencyId().equals(session("currencyId")) && !transcation.getToCurrency().getCurrencyId().equals(session("currencyId"))  && transcation.getCurrency() != null  && !transcation.getToCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId()) && transcation.getFromCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId())) {
			fcCrAmount = addExponentToAmount(removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) * removeExponentFromAmount(transcation.getExchangeRate1(),Long.parseLong(session("currencyExponent"))),session("exponent"));
			drAmount = addExponentToAmount(removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) * removeExponentFromAmount(transcation.getExchangeRate2(),Long.parseLong(session("currencyExponent"))),session("exponent"));
			crAmount=drAmount;
			fcDrAmount=transcation.getInputAmount();
			flag=false;
		}
		if(!transcation.getToCurrency().getCurrencyId().equals(session("currencyId")) && !transcation.getFromCurrency().getCurrencyId().equals(session("currencyId")) && transcation.getCurrency() != null && transcation.getToCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId()) && !transcation.getFromCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId())) {
			fcDrAmount = addExponentToAmount(removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) * removeExponentFromAmount(transcation.getExchangeRate1(),Long.parseLong(session("currencyExponent"))),session("exponent"));
			fcCrAmount=transcation.getInputAmount();
			flag=false;
		}
		if(flag && transcation.getFromCurrency().getCurrencyId().equals(session("currencyId")) && transcation.getCurrency() != null && !transcation.getToCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId())){
			fcCrAmount = addExponentToAmount(removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) * removeExponentFromAmount(transcation.getExchangeRate1(),Long.parseLong(session("currencyExponent"))),session("exponent"));
			drAmount = transcation.getInputAmount();
			crAmount=transcation.getInputAmount();
			flag=false;
		}
		if(flag && transcation.getToCurrency().getCurrencyId().equals(session("currencyId")) && transcation.getCurrency() != null && transcation.getToCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId())){
			fcDrAmount = addExponentToAmount(removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) * removeExponentFromAmount(transcation.getExchangeRate1(),Long.parseLong(session("currencyExponent"))),session("exponent"));
			drAmount = transcation.getInputAmount();
			crAmount=transcation.getInputAmount();
			flag=false;
		}
		}else {
			if(!transcation.getFromCurrency().getCurrencyId().equals(session("currencyId")) && !transcation.getToCurrency().getCurrencyId().equals(session("currencyId"))  && transcation.getCurrency() != null  && !transcation.getToCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId()) && transcation.getFromCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId())) {
				fcCrAmount = addExponentToAmount(removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) / removeExponentFromAmount(transcation.getExchangeRate1(),Long.parseLong(session("currencyExponent"))),session("exponent"));
				drAmount = addExponentToAmount(removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) / removeExponentFromAmount(transcation.getExchangeRate2(),Long.parseLong(session("currencyExponent"))),session("exponent"));
				crAmount=drAmount;
				fcDrAmount=transcation.getInputAmount();
				flag=false;
			}
			if(!transcation.getToCurrency().getCurrencyId().equals(session("currencyId")) && !transcation.getFromCurrency().getCurrencyId().equals(session("currencyId")) && transcation.getCurrency() != null && transcation.getToCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId()) && !transcation.getFromCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId())) {
				fcDrAmount = addExponentToAmount(removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) / removeExponentFromAmount(transcation.getExchangeRate1(),Long.parseLong(session("currencyExponent"))),session("exponent"));
				fcCrAmount=transcation.getInputAmount();
				flag=false;
			}
			if(flag && transcation.getFromCurrency().getCurrencyId().equals(session("currencyId")) && transcation.getCurrency() != null && !transcation.getToCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId())){
				fcCrAmount = addExponentToAmount(removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) / removeExponentFromAmount(transcation.getExchangeRate1(),Long.parseLong(session("currencyExponent"))),session("exponent"));
				drAmount = transcation.getInputAmount();
				crAmount=transcation.getInputAmount();
				flag=false;
			}
			if(flag && transcation.getToCurrency().getCurrencyId().equals(session("currencyId")) && transcation.getCurrency() != null && transcation.getToCurrency().getCurrencyId().equals(transcation.getCurrency().getCurrencyId())){
				fcDrAmount = addExponentToAmount(removeExponentFromAmount(transcation.getInputAmount(),Long.parseLong(session("exponent"))) / removeExponentFromAmount(transcation.getExchangeRate1(),Long.parseLong(session("currencyExponent"))),session("exponent"));
				drAmount = transcation.getInputAmount();
				crAmount=transcation.getInputAmount();
				flag=false;
			}
		}
		if(flag && !transcation.getToCurrency().getCurrencyId().equals(session("currencyId"))) 
			fcCrAmount=transcation.getInputAmount();
		if(flag && !transcation.getFromCurrency().getCurrencyId().equals(session("currencyId"))) 
			fcDrAmount=transcation.getInputAmount();
		createTransBalance(transcation, transcation.getFromAccount(), transcation.getFromCurrency(), 0L,drAmount, 0L, fcDrAmount,0L, fcDrAmount);
		createTransBalance(transcation, transcation.getToAccount(), transcation.getToCurrency(), crAmount, 0L,fcCrAmount, 0L,fcCrAmount,0L);
		createAccountBalance(transcation.getFromAccount(), transcation.getFromCurrency(), drAmount, 0L,fcDrAmount,0L);
		createAccountBalance(transcation.getToAccount(), transcation.getToCurrency(), 0L, crAmount,0L,fcCrAmount);
	}

	public static Double removeExponentFromAmount(Long amount,Long exponent)
	{
		return Double.valueOf(amount)*Math.pow(10,-(exponent));
	}
	public static Long addExponentToAmount(Double amount,String value)
	{
		Long exponent = Long.parseLong(value);
		return (long) (Math.round(amount*Math.pow(10,exponent)));
	}
	void createTransBalance(Transcation transcation, Account account, Currency currency, Long crAmount, Long drAmount, Long fcCrAmount, Long fcDrAmount, Long fccrAmount, Long fcdrAmount) {
		TypedQuery<AccountBalance> accountQuery = JPA.em().createQuery("select x from AccountBalance x where tenant_id=:tenantId and x.account.accountId=:accountId and x.currency.currencyId=:currencyId",AccountBalance.class);
		accountQuery.setParameter("accountId", account.getAccountId());
		accountQuery.setParameter("currencyId", currency.getCurrencyId());
		accountQuery.setParameter("tenantId",session("tenantId"));
		List<AccountBalance> results = accountQuery.getResultList();
		TranscationBalance transcationBalance = new TranscationBalance();
		transcationBalance.setTransactionBalanceId(UUID.randomUUID().toString());
		transcationBalance.setTranscation(transcation);
		transcationBalance.setAccount(account);
		transcationBalance.setCurrency(currency);	
		transcationBalance.setCrAmount(crAmount);
		transcationBalance.setDrAmount(drAmount);
		transcationBalance.setFcCrAmount(fcCrAmount);
		transcationBalance.setFcDrAmount(fcDrAmount);
		transcationBalance.setPrevBalance(0L);
		transcationBalance.setClosingBalance(crAmount-drAmount);
		transcationBalance.setFcPrevBalance(0L);
		transcationBalance.setFcClosingBalance(fccrAmount-fcdrAmount);
		transcationBalance.setTenant(JPA.em().find(Tenant.class,session("tenantId")));		
		for(AccountBalance objects : results){
			transcationBalance.setPrevBalance(objects.getOpeningBalance() + (objects.getCrAmount()-objects.getDrAmount()));
			transcationBalance.setClosingBalance(objects.getOpeningBalance() + (objects.getCrAmount()-objects.getDrAmount()+(crAmount-drAmount)));
			if(!objects.getCurrency().getCurrencyId().equals(session("currencyId"))) {
				transcationBalance.setFcPrevBalance(objects.getfOpeningBalance() + (objects.getFcCrAmount()-objects.getFcDrAmount()));
				transcationBalance.setFcClosingBalance(objects.getfOpeningBalance() + (objects.getFcCrAmount()-objects.getFcDrAmount())+(fccrAmount-fcdrAmount));
			}
		}
		JPA.em().persist(transcationBalance);

	}
	void createAccountBalance(Account account, Currency currency, Long drAmount,Long crAmount, Long fcdrAmount, Long fccrAmount){
		Query accountQuery = JPA.em().createNativeQuery("select account_id,cr_amount,dr_amount,fc_cr_amount,fc_dr_amount from account_balance where account_id=:accountId and currency_id=:currencyId and tenant_id=:tenantId");
		accountQuery.setParameter("tenantId",session("tenantId"));
		accountQuery.setParameter("accountId", account.getAccountId());
		accountQuery.setParameter("currencyId", currency.getCurrencyId());
		List<Object[]> results = accountQuery.getResultList();
		for(Object[] objects : results){
			Query updateAccountQuery = JPA.em().createNativeQuery("update account_balance set cr_amount=:crAmount,dr_amount=:drAmount,fc_cr_amount=:fccrAmount,fc_dr_amount=:fcdrAmount where account_id=:accountId and currency_id=:currencyId and tenant_id=:tenantId");
			updateAccountQuery.setParameter("tenantId",session("tenantId"));
			updateAccountQuery.setParameter("accountId", account.getAccountId());
			updateAccountQuery.setParameter("currencyId", currency.getCurrencyId());
			updateAccountQuery.setParameter("crAmount",Long.valueOf(objects[1].toString())+ crAmount);
			updateAccountQuery.setParameter("drAmount",Long.valueOf(objects[2].toString())+ drAmount);
			updateAccountQuery.setParameter("fccrAmount",Long.valueOf(objects[3].toString())+ fccrAmount);
			updateAccountQuery.setParameter("fcdrAmount",Long.valueOf(objects[4].toString())+ fcdrAmount);
			updateAccountQuery.executeUpdate();
		}
		if(results.isEmpty()){
			AccountBalance accountBalance = new AccountBalance();
			accountBalance.setAccountBalanceId(UUID.randomUUID().toString());
			accountBalance.setAccount(account);
			accountBalance.setCurrency(currency);
			accountBalance.setCrAmount(crAmount);
			accountBalance.setDrAmount(drAmount);
			accountBalance.setFcCrAmount(fccrAmount);
			accountBalance.setCreationDate(new Date());
			accountBalance.setOpeningBalance(0L);
			accountBalance.setfOpeningBalance(0L);
			accountBalance.setFcDrAmount(fcdrAmount);
			accountBalance.setTenant(JPA.em().find(Tenant.class,session("tenantId")));
			JPA.em().persist(accountBalance);
		}
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Transactional
	public Result viewTransBal() {
		Form<?> bindform=Form.form(PostRequest.class);
		PostRequest report = (PostRequest) bindform.bindFromRequest().get();
		Query transactionQuery=JPA.em().createQuery("select X from TranscationBalance X where X.transcation.transcationId=\'"+report.getPrimaryKey()+"\' and X.tenant.tenantId =:tenantId");
		transactionQuery.setParameter("tenantId",session("tenantId"));
		List<TranscationBalance> transcationList=transactionQuery.getResultList();
		org.json.JSONArray jsonArray = new org.json.JSONArray();
		for (TranscationBalance transcationbal : transcationList) {
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("accountName", transcationbal.getAccount().getAccountName());
			jsonObject.put("currencyCode", transcationbal.getCurrency().getCurrencyCode());
			jsonObject.put("fcDrAmount", transcationbal.getFcDrAmount());
			jsonObject.put("fcCrAmount", transcationbal.getFcCrAmount());
			jsonObject.put("fcPrevBalance",transcationbal.getFcPrevBalance());
			jsonObject.put("fcClosingBalance", transcationbal.getFcClosingBalance());
			jsonObject.put("drAmount", transcationbal.getDrAmount());
			jsonObject.put("crAmount", transcationbal.getCrAmount());
			jsonObject.put("prevBalance", transcationbal.getPrevBalance());
			jsonObject.put("closingBalance", transcationbal.getClosingBalance());
			jsonArray.put(jsonObject);
		}
		return ok(jsonArray.toString());
	}

	@Transactional
	public Result deleteRecallTrans() {
		String deleteStatus=Play.application().configuration().getString("application.status.delete.id");
		Query transactionQuery=JPA.em().createQuery("select X from Transcation X where X.transcationStatus.statusId='"+deleteStatus+"' and X.tenant.tenantId =:tenantId ORDER BY X.transcationDate DESC");
		transactionQuery.setParameter("tenantId",session("tenantId"));
		List<Transcation> transcationList=transactionQuery.getResultList();
		Query accountQuery=JPA.em().createQuery("select X from Account X where X.tenant.tenantId =:tenantId");
		accountQuery.setParameter("tenantId",session("tenantId"));
		List<Account> accountList=accountQuery.getResultList();
		Query currency=JPA.em().createQuery("select X from Currency X");
		List<Currency> currencyList=currency.getResultList();
		return ok(deleteRecallTrans.render(transcationList,accountList,currencyList));
	}

	@Transactional
	public Result deleteTransfer() {
		Form<?> bindform=Form.form(PostRequest.class);
		PostRequest trans = (PostRequest) bindform.bindFromRequest().get();
		Transcation transcation=JPA.em().find(Transcation.class, trans.getPrimaryKey());
		transcation.setDeleteUser(JPA.em().find(User.class, session("userId")));
		transcation.setDeleteDate(new Date());
		transcation.setDeleteComments(trans.getUrl());
		transcation.setTranscationStatus(JPA.em().find(Status.class, Play.application().configuration().getString("application.status.delete.id")));
		deleteAccountEntry(transcation);
		return redirect(routes.TransferController.deleteRecallTrans());
	}

	@Transactional
	public Result recallTransfer() {
		Form<?> bindform=Form.form(PostRequest.class);
		PostRequest trans = (PostRequest) bindform.bindFromRequest().get();
		Transcation transcation=JPA.em().find(Transcation.class, trans.getPrimaryKey());
		transcation.setRecallUser(JPA.em().find(User.class, session("userId")));
		transcation.setRecallDate(new Date());
		transcation.setRecallComments(trans.getUrl());
		transcation.setRecallFlag(1L);
		transcation.setTranscationStatus(JPA.em().find(Status.class, Play.application().configuration().getString("application.status.active.id")));
		accountEntry(transcation);
		flash("transId",transcation.getTranscationId());
		return redirect(routes.TransferController.deleteRecallTrans());
	}

	@Transactional
	public Result updateTransfer() throws Exception{
		Form<?> bindform=Form.form(PostRequest.class);
		PostRequest trans = (PostRequest) bindform.bindFromRequest().get();
		Transcation transcation=JPA.em().find(Transcation.class, trans.getPrimaryKey());
		SimpleDateFormat formatter1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		transcation.setTranscationDate(formatter1.parse(trans.getStartDate()));
		transcation.setComments(trans.getValue1());
		return redirect(routes.HomeController.viewTransfer());
	}

	private void deleteAccountEntry(Transcation transcation) {
		Query transactionQuery=JPA.em().createQuery("select X from TranscationBalance X where X.transcation.transcationId=\'"+transcation.getTranscationId()+"\' and X.tenant.tenantId =:tenantId");
		transactionQuery.setParameter("tenantId",session("tenantId"));
		List<TranscationBalance> transcationList=transactionQuery.getResultList();
		for(TranscationBalance transcationBalance:transcationList) {
			createAccountBalance(transcationBalance.getAccount(), transcationBalance.getCurrency(), transcationBalance.getDrAmount()*-1, transcationBalance.getCrAmount()*-1,transcationBalance.getFcDrAmount()*-1,transcationBalance.getFcCrAmount()*-1);
		}
		JPA.em().createQuery("DELETE FROM TranscationBalance X where X.transcation.transcationId=\'"+transcation.getTranscationId()+"\' and X.tenant.tenantId =\'"+session("tenantId")+"\'").executeUpdate();
		
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public Result editTransaction() throws Exception{
		Form<?> bindform=Form.form(Transcation.class);
		Transcation transactions = (Transcation) bindform.bindFromRequest().get();
		String check=ctx().request().getQueryString("flag"); 
		Transcation transcation=JPA.em().find(Transcation.class, transactions.getTranscationId());
		List<String> accountValues = Arrays.asList(session("tenantId"),session("globalTenant"));
		Query currency=JPA.em().createQuery("select X from Currency X");
		List<Currency> currencyList=currency.getResultList();
		Query account=JPA.em().createQuery("select X from Account X where X.tenant.tenantId=:tenantId");
		account.setParameter("tenantId", session("tenantId"));
		List<Account> accountList=account.getResultList();
		Query transactionQuery=JPA.em().createQuery("select X from AccountBalance X where X.account.accountCode='"+Play.application().configuration().getString("application.cash.accountId")+"' and X.currency.currencyId='"+session("currencyId")+"' and X.tenant.tenantId=:tenantId");
		transactionQuery.setParameter("tenantId",session("tenantId"));
		List<AccountBalance> balList=transactionQuery.getResultList();
		String bal="";
		for(AccountBalance accountBalance:balList) {
			bal = ""+TransferController.removeExponentFromAmount(accountBalance.getDrAmount()-accountBalance.getCrAmount()+accountBalance.getOpeningBalance(),Long.parseLong(session("exponent")));
		}

		return ok(home.render(currencyList,transcation,bal,check,accountList));
	}
}
