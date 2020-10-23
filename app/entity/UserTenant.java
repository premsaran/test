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
@Table(name="user_tenant")
@Access(AccessType.FIELD)
public class UserTenant implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    @Id
    @Column(name="user_tenant_id",nullable=false)
	String userTenantId;
	@ManyToOne
	@JoinColumn(name="tenant_id",referencedColumnName = "tenant_id")
	Tenant tenant;
	@ManyToOne
	@JoinColumn(name="user_id",referencedColumnName = "user_id")
	User userId;
	public String getUserTenantId() {
		return userTenantId;
	}
	public void setUserTenantId(String userTenantId) {
		this.userTenantId = userTenantId;
	}
	public Tenant getTenant() {
		return tenant;
	}
   public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}
	public User getUserId() {
		return userId;
	}
	public void setUserId(User userId) {
		this.userId = userId;
	}
}
