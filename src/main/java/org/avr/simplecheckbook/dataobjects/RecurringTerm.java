package org.avr.simplecheckbook.dataobjects;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Object representation of TERM_R table
 * @author Alfonso
 *
 */
public class RecurringTerm {
	
	private Integer id;
	private String description;
	private short onThisDate;
	private DayOfWeek onThisDayOfWeek;
	private TermType type;
	private short alternate;
	
	public String toString() { return description; }
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public short getOnThisDate() {
		return onThisDate;
	}
	public void setOnThisDate(short onThisDate) {
		this.onThisDate = onThisDate;
	}
	public DayOfWeek getOnThisDayOfWeek() {
		return onThisDayOfWeek;
	}
	public void setOnThisDayOfWeek(DayOfWeek onThisDayOfWeek) {
		this.onThisDayOfWeek = onThisDayOfWeek;
	}
	public TermType getType() {
		return type;
	}
	public void setType(TermType term) {
		this.type = term;
	}
	public short getAlternate() {
		return alternate;
	}
	public void setAlternate(short alternate) {
		this.alternate = alternate;
	}
	
	/**
	 * Return text value of Day of week.
	 * @return
	 */
	public String getDayOfWeekText() {
		if ( onThisDayOfWeek == null)
			return "";
		return onThisDayOfWeek.toString();
	}
	
	public String getDayOfMonth() {
		if (onThisDate == 0) return "";
		return ""+ onThisDate;
	}
	
	public LocalDate getDayOfMonthAsDate() {
		return LocalDate.now().withDayOfMonth( onThisDate );
	}
}