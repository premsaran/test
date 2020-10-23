package entity;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="tenant")
@Access(AccessType.FIELD)
public class Tenant implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="tenant_id",nullable=false)
	String tenantId;
	@Column(name="tenant_name",nullable=false)
	String tenantName;
	@Column(name="tenant_code",nullable=false)
	String tenantCode;
	@ManyToOne
	@JoinColumn(name="status_id",referencedColumnName = "status_id")
	Status statusId;
	@ManyToOne
	@JoinColumn(name="currency_id",referencedColumnName = "currency_id")
	Currency currencyId;
	
	@Column(name = "background_image", nullable = true)
	private byte[] backgroundImage;


	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public Currency getCurrencyId() {
		return currencyId;
	}
	public void setCurrencyId(Currency currencyId) {
		this.currencyId = currencyId;
	}
	public String getTenantName() {
		return tenantName;
	}
	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}
	public String getTenantCode() {
		return tenantCode;
	}
	public void setTenantCode(String tenantCode) {
		this.tenantCode = tenantCode;
	}
	public Status getStatusId() {
		return statusId;
	}
	public void setStatusId(Status statusId) {
		this.statusId = statusId;
	}
	public byte[] getBackgroundImage() {
		return backgroundImage;
	}
	public void setBackgroundImage(byte[] backgroundImage) {
		this.backgroundImage = backgroundImage;
	}
	



}
