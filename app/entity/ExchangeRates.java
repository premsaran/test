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
@Table(name="exchange_rate")
@Access(AccessType.FIELD)
public class ExchangeRates implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="exchange_rate_id",nullable=false)
	String exchangeRateId;
	@ManyToOne(optional = false)
	@JoinColumn(name = "from_currency_id", referencedColumnName = "currency_id")
	Currency fromCurrency;
	@ManyToOne(optional = false)
	@JoinColumn(name = "to_currency_id", referencedColumnName = "currency_id")
	Currency toCurrency;
    @Column(name="exchange_rate_multiply")
	Long exchangeRateMultiply;
	@Column(name="exchange_reverse_divided")
	Long exchangeRateDivided;
	@ManyToOne
	@JoinColumn(name="tenant_id",referencedColumnName = "tenant_id")
	Tenant tenant;
	public Tenant getTenant() {
		return tenant;
	}
   public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}
	public String getExchangeRateId() {
		return exchangeRateId;
	}
	public void setExchangeRateId(String exchangeRateId) {
		this.exchangeRateId = exchangeRateId;
	}
	public Currency getToCurrency() {
		return toCurrency;
	}
	public void setToCurrency(Currency toCurrency) {
		this.toCurrency = toCurrency;
	}
	public Currency getFromCurrency() {
		return fromCurrency;
	}
	public void setFromCurrency(Currency fromCurrency) {
		this.fromCurrency = fromCurrency;
	}
	public Long getExchangeRateMultiply() {
		return exchangeRateMultiply;
	}
	public void setExchangeRateMultiply(Long exchangeRateMultiply) {
		this.exchangeRateMultiply = exchangeRateMultiply;
	}
	public Long getExchangeRateDivided() {
		return exchangeRateDivided;
	}
	public void setExchangeRateDivided(Long exchangeRateDivided) {
		this.exchangeRateDivided = exchangeRateDivided;
	}
}
