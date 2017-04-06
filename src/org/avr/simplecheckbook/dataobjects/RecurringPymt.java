package org.avr.simplecheckbook.dataobjects;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.avr.simplecheckbook.utils.IncalculableDueDate;

/**
 * Object representation of RECURRING_PYMT table
 * @author Alfonso
 *
 */
public class RecurringPymt {
	
	private Integer id;
	private String payTo;
	private BigDecimal amount;
	private LocalDate effDt;
	private LocalDate termDt;
	private LocalDate inactiveDt;
	private LocalDate dateOfLastPymt;
	private RecurringTerm frequency;
	
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append( getPayTo() ).append(" ").append( frequency.getType() ).append(" ").append( getAmount() );
		return str.toString();
	}
	
	
	
	/**
	 * Calculate the next due date. 
	 * If a previous payment has been recorded, use that.  Otherwise calculate the
	 * first payment.
	 * 
	 * If the due date occurs AFTER termination date, return Todays date +1 year.
	 * 
	 * @return
	 */
	public LocalDate dueDate() throws IncalculableDueDate {
		LocalDate dueDate = null;
		
		if (dateOfLastPymt != null) {
			switch (frequency.getType()) {
			case MONTHLY :
				dueDate = dateOfLastPymt.plusMonths(frequency.getAlternate());
				if ( termDt != null && dueDate.isAfter( termDt ))
					return LocalDate.now().plusYears(1);
				return dueDate;
			case WEEKLY :
				dueDate = dateOfLastPymt.plusWeeks(frequency.getAlternate());
				if ( termDt != null && dueDate.isAfter( termDt ))
					return LocalDate.now().plusYears(1);
				return dueDate;
			}
		} else {
			switch (frequency.getType()) {
			case MONTHLY :
				return  LocalDate.of(effDt.getYear(), effDt.getMonth(), frequency.getOnThisDate() );
			case WEEKLY :
				LocalDate today = LocalDate.now();
				int x = frequency.getOnThisDayOfWeek() - today.getDayOfWeek().getValue();
				return today.plusDays( x );
			}
		}
		
		throw new IncalculableDueDate();
	}
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getPayTo() {
		return payTo;
	}
	public void setPayTo(String payTo) {
		this.payTo = payTo;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public LocalDate getEffDt() {
		return effDt;
	}
	public void setEffDt(LocalDate effDt) {
		this.effDt = effDt;
	}
	public LocalDate getTermDt() {
		return termDt;
	}
	public void setTermDt(LocalDate termDt) {
		this.termDt = termDt;
	}
	public LocalDate getInactiveDt() {
		return inactiveDt;
	}
	public void setInactiveDt(LocalDate inactiveDt) {
		this.inactiveDt = inactiveDt;
	}
	public LocalDate getDateOfLastPymt() {
		return dateOfLastPymt;
	}
	public void setDateOfLastPymt(LocalDate dateOfLastPymt) {
		this.dateOfLastPymt = dateOfLastPymt;
	}
	public RecurringTerm getFrequency() {
		return frequency;
	}
	public void setFrequency(RecurringTerm frequency) {
		this.frequency = frequency;
	}
	public String getFrequencyText() {
		return getFrequency().getDescription();
	}
	
}
