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

@Entity
@Table(name="users")
@Access(AccessType.FIELD)
public class User implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="user_id",nullable=false)
	String userId;
 
	@ManyToOne
	@JoinColumn(name="tenant_id",referencedColumnName = "tenant_id",nullable=false)
	Tenant tenant;

	@Column(name="login_id",nullable=false)
	String loginId;

	@Column(name="user_name",nullable=false)
	String userName;

	@Column(name="password",nullable=false)
	String password;

	@ManyToOne
	@JoinColumn(name="role_id",referencedColumnName = "role_id")
	Roles role;

	@ManyToOne
	@JoinColumn(name="status_id", referencedColumnName = "status_id")
	Status status;

	@Column(name="force_password_change",nullable=false)
	String forcePasswordChange;

	@Column(name="created_by",nullable=false)
	String createdBy;

	@Column(name="updated_by",nullable=false)
	String updatedBy;

	@Column(name="update_date",nullable=false)
	Date updateDate;

	public Tenant getTenant() {
		return tenant;
	}
	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Roles getRole() {
		return role;
	}

	public void setRole(Roles role) {
		this.role = role;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getForcePasswordChange() {
		return forcePasswordChange;
	}

	public void setForcePasswordChange(String forcePasswordChange) {
		this.forcePasswordChange = forcePasswordChange;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

}
