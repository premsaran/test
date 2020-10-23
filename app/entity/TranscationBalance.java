package entity;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="transcation_balance")
@Access(AccessType.FIELD)
public class TranscationBalance implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="transaction_balance_id",nullable=false)
	String transactionBalanceId;
	
	@ManyToOne
	@JoinColumn(name = "transcation_id", referencedColumnName = "transcation_id")
	Transcation transcation;
	
	@ManyToOne
	@JoinColumn(name = "account_id", referencedColumnName = "account_id")
	Account account;
	
	@ManyToOne
	@JoinColumn(name = "currency_id", referencedColumnName = "currency_id")
	Currency currency;
	
	@Column(name="prev_balance",nullable=false)
	Long prevBalance;
	
	@Column(name="closing_balance",nullable=false)
	Long closingBalance;
	
	@Column(name="cr_amount",nullable=false)
	Long crAmount;
	
	@Column(name="dr_amount",nullable=false)
	Long drAmount;
	
	@Column(name="fc_cr_amount",nullable=false)
	Long fcCrAmount;

	@Column(name="fc_dr_amount",nullable=false)
	Long fcDrAmount;

	@Column(name="fc_prev_balance")
	Long fcPrevBalance;
	
	@Column(name="fc_closing_balance")
	Long fcClosingBalance;
	@ManyToOne
	@JoinColumn(name="tenant_id",referencedColumnName = "tenant_id")
	Tenant tenant;
	public Tenant getTenant() {
		return tenant;
	}
   public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}
	public Long getFcPrevBalance() {
		return fcPrevBalance;
	}

	public void setFcPrevBalance(Long fcPrevBalance) {
		this.fcPrevBalance = fcPrevBalance;
	}

	public Long getFcClosingBalance() {
		return fcClosingBalance;
	}

	public void setFcClosingBalance(Long fcClosingBalance) {
		this.fcClosingBalance = fcClosingBalance;
	}

	public String getTransactionBalanceId() {
		return transactionBalanceId;
	}

	public void setTransactionBalanceId(String transactionBalanceId) {
		this.transactionBalanceId = transactionBalanceId;
	}

	public Transcation getTranscation() {
		return transcation;
	}

	public void setTranscation(Transcation transcation) {
		this.transcation = transcation;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public Long getPrevBalance() {
		return prevBalance;
	}

	public void setPrevBalance(Long prevBalance) {
		this.prevBalance = prevBalance;
	}

	public Long getClosingBalance() {
		return closingBalance;
	}

	public void setClosingBalance(Long closingBalance) {
		this.closingBalance = closingBalance;
	}

	public Long getCrAmount() {
		return crAmount;
	}

	public void setCrAmount(Long crAmount) {
		this.crAmount = crAmount;
	}

	public Long getDrAmount() {
		return drAmount;
	}

	public void setDrAmount(Long drAmount) {
		this.drAmount = drAmount;
	}

	public Long getFcCrAmount() {
		return fcCrAmount;
	}

	public void setFcCrAmount(Long fcCrAmount) {
		this.fcCrAmount = fcCrAmount;
	}

	public Long getFcDrAmount() {
		return fcDrAmount;
	}

	public void setFcDrAmount(Long fcDrAmount) {
		this.fcDrAmount = fcDrAmount;
	}
	
}
