package entity;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import play.data.format.Formats.DateTime;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="transcation")
@Access(AccessType.FIELD)

public class Transcation implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="transcation_id",nullable=false)
	String transcationId;
	
	@Column(name="transcation_sequence_no",nullable=false)
	String transSeqNo;
	
	@ManyToOne
	@JoinColumn(name = "transcation_status_id", referencedColumnName = "status_id")
	Status transcationStatus;
	
	@ManyToOne
	@JoinColumn(name = "from_account_id", referencedColumnName = "account_id")
	Account fromAccount;
	
	@ManyToOne
	@JoinColumn(name = "to_account_id", referencedColumnName = "account_id")
	Account toAccount;
	
	@ManyToOne
	@JoinColumn(name = "from_currency_id", referencedColumnName = "currency_id")
	Currency fromCurrency;
	
	@ManyToOne
	@JoinColumn(name = "to_currency_id", referencedColumnName = "currency_id")
	Currency toCurrency;
	
	@Column(name="exchange_rate_1",nullable=true)
	Long exchangeRate1;
	
	@Column(name="exchange_rate_2",nullable=true)
	Long exchangeRate2;
	
	@Column(name="exponent",nullable=false)
	Long exponent;
	
	@Column(name="from_local_amount",nullable=true)
	Long fromLocalAmount;
	
	@DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	@Column(name="transcation_date",nullable=false)
	Date transcationDate;
	
	@Column(name="comments",nullable=true)
	String comments;
	
	@Column(name="input_amount",nullable=true)
	Long inputAmount;
	
	@ManyToOne
	@JoinColumn(name = "currency_id", referencedColumnName = "currency_id")
	Currency currency;

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "user_id")
	User user;

	@Column(name="multiply_divide_flag",nullable=false)
	Long multiplyDivideFlag;
	
	@Column(name="to_local_amount",nullable=true)
	Long toLocalAmount;
	
	@Column(name="delete_comments",nullable=true)
	String deleteComments;
	
	@DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	@Column(name="delete_date",nullable=true)
	Date deleteDate;
	
	@ManyToOne
	@JoinColumn(name = "delete_user_id", referencedColumnName = "user_id")
	User deleteUser;
	
	@Column(name="recall_comments",nullable=true)
	String recallComments;
	
	@DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	@Column(name="recall_date",nullable=true)
	Date recallDate;
	
	@ManyToOne
	@JoinColumn(name = "recall_user_id", referencedColumnName = "user_id")
	User recallUser;
	
	@Column(name="recall_flag",nullable=true)
	Long recallFlag;
	
	@ManyToOne
	@JoinColumn(name="tenant_id",referencedColumnName = "tenant_id")
	Tenant tenant;
	public Tenant getTenant() {
		return tenant;
	}
   public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}
	public String getTransSeqNo() {
		return transSeqNo;
	}

	public void setTransSeqNo(String transSeqNo) {
		this.transSeqNo = transSeqNo;
	}

	public Status getTranscationStatus() {
		return transcationStatus;
	}

	public void setTranscationStatus(Status transcationStatus) {
		this.transcationStatus = transcationStatus;
	}

	public String getDeleteComments() {
		return deleteComments;
	}

	public void setDeleteComments(String deleteComments) {
		this.deleteComments = deleteComments;
	}

	public Date getDeleteDate() {
		return deleteDate;
	}

	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}

	public User getDeleteUser() {
		return deleteUser;
	}

	public void setDeleteUser(User deleteUser) {
		this.deleteUser = deleteUser;
	}

	public String getRecallComments() {
		return recallComments;
	}

	public void setRecallComments(String recallComments) {
		this.recallComments = recallComments;
	}

	public Date getRecallDate() {
		return recallDate;
	}

	public void setRecallDate(Date recallDate) {
		this.recallDate = recallDate;
	}

	public User getRecallUser() {
		return recallUser;
	}

	public void setRecallUser(User recallUser) {
		this.recallUser = recallUser;
	}

	public Long getRecallFlag() {
		return recallFlag;
	}

	public void setRecallFlag(Long recallFlag) {
		this.recallFlag = recallFlag;
	}

	public String getTranscationId() {
		return transcationId;
	}

	public void setTranscationId(String transcationId) {
		this.transcationId = transcationId;
	}

	public Account getFromAccount() {
		return fromAccount;
	}

	public void setFromAccount(Account fromAccount) {
		this.fromAccount = fromAccount;
	}

	public Account getToAccount() {
		return toAccount;
	}

	public void setToAccount(Account toAccount) {
		this.toAccount = toAccount;
	}

	public Currency getFromCurrency() {
		return fromCurrency;
	}

	public void setFromCurrency(Currency fromCurrency) {
		this.fromCurrency = fromCurrency;
	}

	public Long getExchangeRate1() {
		return exchangeRate1;
	}

	public void setExchangeRate1(Long exchangeRate1) {
		this.exchangeRate1 = exchangeRate1;
	}

	public Long getExchangeRate2() {
		return exchangeRate2;
	}

	public void setExchangeRate2(Long exchangeRate2) {
		this.exchangeRate2 = exchangeRate2;
	}

	public Long getExponent() {
		return exponent;
	}

	public void setExponent(Long exponent) {
		this.exponent = exponent;
	}

	
	public Date getTranscationDate() {
		return transcationDate;
	}

	public void setTranscationDate(Date transcationDate) {
		this.transcationDate = transcationDate;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Currency getToCurrency() {
		return toCurrency;
	}

	public void setToCurrency(Currency toCurrency) {
		this.toCurrency = toCurrency;
	}

	public Long getMultiplyDivideFlag() {
		return multiplyDivideFlag;
	}

	public void setMultiplyDivideFlag(Long multiplyDivideFlag) {
		this.multiplyDivideFlag = multiplyDivideFlag;
	}

	public Long getFromLocalAmount() {
		return fromLocalAmount;
	}

	public void setFromLocalAmount(Long fromLocalAmount) {
		this.fromLocalAmount = fromLocalAmount;
	}

	public Long getToLocalAmount() {
		return toLocalAmount;
	}

	public void setToLocalAmount(Long toLocalAmount) {
		this.toLocalAmount = toLocalAmount;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getInputAmount() {
		return inputAmount;
	}

	public void setInputAmount(Long inputAmount) {
		this.inputAmount = inputAmount;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}
	
}
