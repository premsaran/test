package entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.data.format.Formats.DateTime;

@Entity
@Table(name="account_balance")
@Access(AccessType.FIELD)
public class AccountBalance implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="account_balance_id",nullable=false)
	String accountBalanceId;

	@ManyToOne
	@JoinColumn(name = "account_id", referencedColumnName = "account_id")
	Account account;
	
	@ManyToOne
	@JoinColumn(name = "currency_id", referencedColumnName = "currency_id")
	Currency currency;
	@ManyToOne
	@JoinColumn(name="tenant_id",referencedColumnName = "tenant_id")
	Tenant tenant;
	
	@Column(name="dr_amount",nullable=false)
	Long drAmount;

	@Column(name="cr_amount",nullable=false)
	Long crAmount;
	
	@Column(name="fc_dr_amount")
	Long fcDrAmount;

	@Column(name="fc_cr_amount")
	Long fcCrAmount;
	
	@Column(name="opening_balance")
	Long openingBalance;

	@Column(name="f_opening_balance")
	Long fOpeningBalance;
	
	@DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	@Column(name="creation_date",nullable=false)
	Date creationDate;
	
	public Tenant getTenant() {
		return tenant;
	}
   public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}
	
	public Long getFcDrAmount() {
		return fcDrAmount;
	}

	public void setFcDrAmount(Long fcDrAmount) {
		this.fcDrAmount = fcDrAmount;
	}

	public Long getFcCrAmount() {
		return fcCrAmount;
	}

	public void setFcCrAmount(Long fcCrAmount) {
		this.fcCrAmount = fcCrAmount;
	}

	public String getAccountBalanceId() {
		return accountBalanceId;
	}

	public void setAccountBalanceId(String accountBalanceId) {
		this.accountBalanceId = accountBalanceId;
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

	public Long getDrAmount() {
		return drAmount;
	}

	public void setDrAmount(Long drAmount) {
		this.drAmount = drAmount;
	}

	public Long getCrAmount() {
		return crAmount;
	}

	public void setCrAmount(Long crAmount) {
		this.crAmount = crAmount;
	}

	public Long getOpeningBalance() {
		return openingBalance;
	}

	public void setOpeningBalance(Long openingBalance) {
		this.openingBalance = openingBalance;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Long getfOpeningBalance() {
		return fOpeningBalance;
	}

	public void setfOpeningBalance(Long fOpeningBalance) {
		this.fOpeningBalance = fOpeningBalance;
	}
	
}
