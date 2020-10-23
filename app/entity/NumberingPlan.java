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
@Table(name="numbering_plan")
@Access(AccessType.FIELD)
public class NumberingPlan implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="numbering_plan_id",nullable=false)
	String numberingPlanId;
	
	@Column(name="prefix",nullable=false)
	String prefix;
	
	@Column(name="suffix",nullable=false)
	String suffix;

	@Column(name="increment",nullable=false)
	Long increment;
	
	@Column(name="last_sequence_number",nullable=false)
	Long lastSeqNo;
	
	@ManyToOne
	@JoinColumn(name="tenant_id",referencedColumnName = "tenant_id")
	Tenant tenant;
	
	@Column(name="sequence_name",nullable=false)
	String sequenceName;
	
	
	public String getSequenceName() {
		return sequenceName;
	}
	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}
	public Tenant getTenant() {
		return tenant;
	}
   public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}
	public String getNumberingPlanId() {
		return numberingPlanId;
	}

	public void setNumberingPlanId(String numberingPlanId) {
		this.numberingPlanId = numberingPlanId;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public Long getIncrement() {
		return increment;
	}

	public void setIncrement(Long increment) {
		this.increment = increment;
	}

	public Long getLastSeqNo() {
		return lastSeqNo;
	}

	public void setLastSeqNo(Long lastSeqNo) {
		this.lastSeqNo = lastSeqNo;
	}
}
