package entity;
import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="status")
@Access(AccessType.FIELD)
public class Status implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;	@Id
	@Column(name="status_id",nullable=false)
	String statusId;
	
	@Column(name="status_name",nullable=false)
	String statusName;

	public String getStatusId() {
		return statusId;
	}

	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}
	
}
