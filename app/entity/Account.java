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
@Table(name="account")
@Access(AccessType.FIELD)
public class Account implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="account_id",nullable=false)
	String accountId;
	
	@ManyToOne
	@JoinColumn(name="tenant_id",referencedColumnName = "tenant_id")
	Tenant tenant;
	
	@Column(name="account_name",nullable=false)
	String accountName;

	@Column(name="account_code",nullable=false)
	String accountCode;
	
	@ManyToOne
	@JoinColumn(name="customer_id",referencedColumnName = "customer_id")
	Customer customer;
	
	public Tenant getTenant() {
		return tenant;
	}
   public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}
    public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAccountCode() {
		return accountCode;
	}

	public void setAccountCode(String accountCode) {
		this.accountCode = accountCode;
	}
	public Customer getCustomer() {
		return customer;
	}
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	
}
