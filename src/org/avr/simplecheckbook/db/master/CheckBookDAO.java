package org.avr.simplecheckbook.db.master;

import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

import org.avr.simplecheckbook.dataobjects.Balance;
import org.avr.simplecheckbook.dataobjects.RecurringPymt;
import org.avr.simplecheckbook.dataobjects.RecurringTerm;
import org.avr.simplecheckbook.dataobjects.TermType;
import org.avr.simplecheckbook.dataobjects.Transaction;
import org.avr.simplecheckbook.utils.CheckBookException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javafx.application.Platform;

public class CheckBookDAO {
	
	private JdbcTemplate jdbcTmplt;
	
	public CheckBookDAO(String dbName) {
		this.connect(dbName);
	}
	
	
	/**
	 * Setup connections to the database.
	 * @param suffix
	 */
	private void connect(String dbName) {
		DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:derby:"+ dbName +";create=true");
		jdbcTmplt = new JdbcTemplate(dataSource);
	}
	
	
	
	/**
	 * ONly call this method when the application is shutting down.  This
	 * will write all cached memory to disk.  In other words .. all the
	 * updated SEQUENCES will be saved - preventing incrementing by 100.
	 */
	public void shutDown() {
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true;");
		} catch (SQLException sqlEx) {
			if ("XJ015".equalsIgnoreCase( sqlEx.getSQLState() ))
				return;
			sqlEx.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * Create Daily_balance & checkbook tables.
	 */
	public void createTables() throws CheckBookException {
		createDailyBalanceTable();
		createCheckBook();
		createRecurringRef();
		createRecurringPymt();
	}
	
	
	private void createDailyBalanceTable() {
//		String createBalance = "CREATE TABLE daily_balance ( date date , amount decimal(9,2) ) ";
		StringBuffer createBalance = new StringBuffer();
		createBalance.append("CREATE TABLE daily_balance ( ");
		createBalance.append("   ").append( DailyBalanceColumnNames.DATE ).append("   date ");
		createBalance.append(" , ").append( DailyBalanceColumnNames.AMOUNT ).append(" decimal(9,2) ");
		createBalance.append(" ) ");
		
		try {
			jdbcTmplt.execute( createBalance.toString() );		
		} catch (Exception ex) {
			ex.printStackTrace();
			Platform.exit();
		}	
	}
	private void createCheckBook() {
		StringBuffer createBook = new StringBuffer();
		createBook.append("CREATE TABLE checkbook ( ");
		createBook.append(" trans_id integer not null generated always as identity (start with 1 , increment by 1) ");
		createBook.append(" , tx_date timestamp ");
		createBook.append(" , checkNum integer");
		createBook.append(" , payee varchar(50) ");
		createBook.append(" , memo varchar(70) ");
		createBook.append(" , recur_id integer ");
		createBook.append(" , cleared boolean ");
		createBook.append(" , debit decimal(9,2) ");
		createBook.append(" , credit decimal(9,2) ");
		createBook.append(" , lgcl_dlt_dt timestamp )");
		createBook.append(" )");

		try {
			jdbcTmplt.execute( createBook.toString() );
			jdbcTmplt.execute("create index date_idx on checkbook (tx_date) ");
		} catch (Exception ex) {
			ex.printStackTrace();
			Platform.exit();
		}
	}
	private void createRecurringRef() {
		StringBuffer createBook = new StringBuffer();
		createBook.append("CREATE TABLE TERM_R ( ");
		createBook.append("   ").append( TermColumnNames.ID ).append(" integer not null generated always as identity (start with 1 , increment by 1)");
		createBook.append(" , ").append( TermColumnNames.DESCRIPTION ).append(" varchar(50) not null");
		createBook.append(" , ").append( TermColumnNames.ON_THIS_DATE ).append("     smallint");
		createBook.append(" , ").append( TermColumnNames.ON_THIS_DAY_OF_WEEK).append("  smallint");
		createBook.append(" , ").append( TermColumnNames.TYPE ).append("    varchar(8) not null");
		createBook.append(" , ").append( TermColumnNames.ALTERNATE ).append("        smallint  default 1");
		createBook.append(" )");

		try {
			jdbcTmplt.execute( createBook.toString() );
			populateTermRef();
		} catch (Exception ex) {
			ex.printStackTrace();
			Platform.exit();
		}
	}
	private void createRecurringPymt() {
		StringBuffer createBook = new StringBuffer();
		createBook.append("CREATE TABLE Recurring_pymt ( ");   
		createBook.append("   ").append( RecurringPymtColumnNames.ID ).append(" integer not null generated always as identity (start with 1 , increment by 1)");
		createBook.append(" , ").append( RecurringPymtColumnNames.PAY_TO ).append("  varchar(50) not null");
		createBook.append(" , ").append( RecurringPymtColumnNames.AMOUNT ).append("  decimal(9,2) not null");
		createBook.append(" , ").append( RecurringPymtColumnNames.EFF_DT ).append("  date not null");
		createBook.append(" , ").append( RecurringPymtColumnNames.TERM_DT ).append(" date");
		createBook.append(" , ").append( RecurringPymtColumnNames.INACTIVE_DT ).append("   date");
		createBook.append(" , ").append( RecurringPymtColumnNames.DATE_OF_LAST_PYMT ).append("   date");
		createBook.append(" , ").append( RecurringPymtColumnNames.FREQUENCY ).append("    integer  not null ");
		createBook.append(" ) ");

		try {
			jdbcTmplt.execute( createBook.toString() );
		} catch (Exception ex) {
			ex.printStackTrace();
			Platform.exit();
		}
	}
	
	
	
	/**
	 * Insert common recurring terms (Monthly, Weekly, bi-monthly)
	 */
	private void populateTermRef() {
		RecurringTerm r = new RecurringTerm();
		r.setDescription("First of the Month");
		r.setOnThisDate( (short)1);
		r.setType(TermType.MONTHLY);
		r.setAlternate((short)1);
		insertTermRef(r);
		
		RecurringTerm b = new RecurringTerm();
		b.setDescription("Every other month");
		b.setOnThisDate( (short)1);
		b.setType(TermType.MONTHLY);
		b.setAlternate((short)2);
		insertTermRef(b);
		
		RecurringTerm c = new RecurringTerm();
		c.setDescription("Midmonth");
		c.setOnThisDate( (short)15);
		c.setType(TermType.MONTHLY);
		c.setAlternate((short)1);
		insertTermRef(c);
		
		RecurringTerm w = new RecurringTerm();
		w.setDescription("Weekly-M");
//		w.setOnThisDayOfWeek( (short)Calendar.MONDAY );
		w.setOnThisDayOfWeek( DayOfWeek.MONDAY );
		w.setType(TermType.WEEKLY);
		w.setAlternate((short)1);
		insertTermRef(w);
	}
	
	private void insertTermRef(RecurringTerm a) {
		StringBuffer ins = new StringBuffer();
		ins.append("insert into term_r ( ").append( TermColumnNames.getInsertColumns() ).append(" ) ");
		ins.append(" values ( ? , ? , ? , ? , ? ) ");
		this.jdbcTmplt.update( 
				ins.toString()
				, a.getDescription() , a.getOnThisDate() , a.getOnThisDayOfWeek() 
				, a.getType().toString() , a.getAlternate()  );
	}
	
	
	
	public void updateLastPayment(RecurringPymt pymt) {
		StringBuffer ins = new StringBuffer();
		ins.append("update Recurring_pymt ");
		ins.append(" set ").append( RecurringPymtColumnNames.DATE_OF_LAST_PYMT ).append(" = ? where id = ?");
		this.jdbcTmplt.update( 
				ins.toString()
				, Date.valueOf( pymt.getDateOfLastPymt() ), pymt.getId()  );
	}
	
	
	
	
	/**
	 * Select all the transactions from the Checkbook.
	 * @return
	 */
	public List<Transaction> getTransactionsAll() {
		String qry = "select trans_id , tx_date, checknum, payee, memo, cleared, debit , credit from checkbook order by tx_date asc";
		return getTransactions(qry.toString());
	}
	
	
	
	
	/**
	 * 
	 * @param numOfDays
	 * @return
	 */
	public List<Transaction> getTransactionsAfter(int numOfDays) {
		Balance b = getBalance(numOfDays);
		return getTransactionsAfter(b);
	}
	
	public List<Transaction> getTransactionsAfter(Balance bal) {
		StringBuffer qry = new StringBuffer();
		qry.append("select trans_id , tx_date, checknum, payee, memo, cleared, debit , credit from checkbook ");
		qry.append("where cast ( tx_date as date ) > '").append( bal.getDate() ).append("' ");
		qry.append(" and  lgcl_dlt_dt is null ");
		qry.append(" order by tx_date asc , checknum asc ");
		
		return getTransactions(qry.toString());
	}
	
	
	
	
	/**
	 * Get the posted transactions for this recurring payment occurring after
	 * the Due Date.
	 * @param dueDate
	 * @param id
	 * @return
	 */
	public List<Transaction> getTransactionsForRecurring(LocalDate dueDate , int id) {
		StringBuffer qry = new StringBuffer();
		qry.append("select trans_id , tx_date, checknum, payee, memo, cleared, debit , credit from checkbook ");
		qry.append("where cast ( tx_date as date ) > '").append( dueDate ).append("' ");
		qry.append(" and recur_id=").append( id);
		qry.append(" and  lgcl_dlt_dt is null ");
		qry.append(" order by tx_date asc , checknum asc ");
		
		return getTransactions(qry.toString());
	}
	
	
	
	/**
	 * Return the transactions on this specific transaction date.
	 * @param tx
	 * @return
	 */
	public List<Transaction> getTransactionsOn(Transaction tx) {
		StringBuffer qry = new StringBuffer();
		qry.append("select trans_id , tx_date, checknum, payee, memo, cleared, debit , credit from checkbook ");
		qry.append("where cast ( tx_date as date ) = '").append( tx.getDateOnly() ).append("' ");
		qry.append(" and  lgcl_dlt_dt is null ");
		qry.append(" order by tx_date asc , checknum asc ");
		
		return getTransactions( qry.toString() );
	}
	
	
	
	
	/**
	 * Common method to build a collection of Transactions.
	 * @param query
	 * @return
	 */
	private List<Transaction> getTransactions(String query) {
		List<Transaction> trans = jdbcTmplt.query(query
				, new RowMapper<Transaction>() {
					public Transaction mapRow(ResultSet rs , int rowNum) throws SQLException {
						Transaction t = new Transaction();
						t.setTransID( rs.getInt("trans_id") ); 
						t.setTxDate( rs.getTimestamp("tx_date"));
						t.setCheckNumber( rs.getBigDecimal("checknum"));
						t.setPayee( rs.getString("payee"));
						t.setMemo( rs.getString("memo"));
						t.setCleared( rs.getBoolean("cleared"));
						t.setDebit( rs.getBigDecimal("debit"));
						t.setCredit( rs.getBigDecimal("credit"));
						return t;
					}
		});
		return trans;
	}
	
	
	
	
	/**
	 * 
	 * @param numOfDays
	 * @return
	 */
	public Balance getBalance(int numOfDays) {
		StringBuffer qry = new StringBuffer();
		qry.append("select * from daily_balance where date > ");
		qry.append("cast (( select {fn timestampadd(SQL_TSI_DAY , -").append(numOfDays).append(" , current_timestamp ) } ");
		qry.append("from sysibm.sysdummy1 ) as date ) order by date asc");
		
		List<Balance> balances = getBalance( qry.toString() );
		
		if (balances.isEmpty()) {
			/* There isn't a balance in the past numOfDays ... so
			 * I'll look for the latest one */
			qry.setLength(0);
			qry.append("select * from daily_balance order by date  ");
			balances = getBalance( qry.toString() );
			if ( balances.isEmpty() )
				return null;
		}
		
		return balances.get(0);
	}
	
	
	/**
	 * Perform the SQL
	 * @param qry
	 * @return
	 */
	private List<Balance> getBalance(String qry) {
		List<Balance> balances = jdbcTmplt.query( qry.toString() 
				, new RowMapper<Balance>() {
					public Balance mapRow(ResultSet rs , int rowNum) throws SQLException {
						Balance b = new Balance();
						b.setBalance( rs.getBigDecimal("amount"));
						b.setDate( rs.getDate("date").toLocalDate());
						return b;
					}
		});
		return balances;
	}
	
	
	private static final String ON = "=";
	private static final String BEFORE = "<";
	
	/**
	 * Look for a balance prior to txDate
	 * @param txDate
	 * @return
	 */
	public Balance getPreviousBalance(Date txDate) {
		return getBalance(txDate , CheckBookDAO.BEFORE );
	}
	public Balance getBalanceOn(Date txDate) {
		return getBalance(txDate , CheckBookDAO.ON );
	}
	
	
	private Balance getBalance( Date txDate , String onOrbefore ) {
		StringBuffer qry = new StringBuffer();
		qry.append("select * from daily_balance where date ").append( onOrbefore );
		qry.append(" '").append( txDate );
		qry.append("' order by date desc fetch first row only");
		
		try {
			Balance bal = jdbcTmplt.queryForObject( qry.toString() , new RowMapper<Balance>() {
				public Balance mapRow(ResultSet rs , int rowNum) throws SQLException {
					Balance b = new Balance();
					b.setBalance( rs.getBigDecimal("amount"));
					b.setDate( rs.getDate("date").toLocalDate());
					return b;
				}
			} );
			return bal;
		} catch (EmptyResultDataAccessException emptyEx) {
			return null;
		}
	}
	
	
	/**
	 * Insert a transaction into the checkbook table
	 * @param t
	 */
	public void insertTransaction(Transaction t) {
		StringBuffer ins = new StringBuffer();
		ins.append("insert into checkbook ( tx_date, checknum, payee, recur_id , memo, cleared, debit , credit ) ");
		ins.append(" values ( ?, ? , ? , ? , ? , ? , ? , ? ) ");
		
		jdbcTmplt.update(
				ins.toString()
				, t.getTxDate() , t.getCheckNumber() , t.getPayee() , t.getRecurId() 
				, t.getMemo() , t.getCleared() , t.getDebit() , t.getCredit() 
				);
	}
	
	
	
	/**
	 * Update the existing Transaction
	 * @param t
	 */
	public void updateTransaction(Transaction t) {
		StringBuffer upd = new StringBuffer();
		upd.append("update checkbook set ");
		upd.append("tx_date = ? ");
		upd.append(", checknum = ? ");
		upd.append(", payee = ? ");
		upd.append(", memo = ? ");
		upd.append(", cleared = ? ");
		upd.append(", debit = ?  ");
		upd.append(", credit = ?  ");
		upd.append(" where trans_id = ? ");
		
		jdbcTmplt.update( upd.toString()
				, t.getTxDate() , t.getCheckNumber() , t.getPayee() , t.getMemo() , t.getCleared() , t.getDebit() , t.getCredit() 
				, t.getTransID() );
	}
	
	
	
	
	/**
	 * Insert balance into 
	 * @param bal
	 */
	public void saveBalance(Balance bal) {
		StringBuffer ins = new StringBuffer();
		ins.append("update daily_balance set amount = ? ");
		ins.append(" where date = ? ");
		
		int i = jdbcTmplt.update( 
				ins.toString()
				, bal.getBalance() , Date.valueOf( bal.getDate() ) 
				);
		if (i == 0 )
			insertBalance(bal);
	}
	/**
	 * Insert balance into 
	 * @param bal
	 */
	private void insertBalance(Balance bal) {
		StringBuffer ins = new StringBuffer();
		ins.append("insert into daily_balance ( date, amount ) ");
		ins.append(" values ( ?, ? ) ");
		
		jdbcTmplt.update(
				ins.toString()
				, Date.valueOf( bal.getDate() ) , bal.getBalance() 
				);
	}
	
	
	
	/**
	 * NOT an actual delete, but an update of lgcl_dlt_dt
	 * @param trans
	 */
	public void deleteTrans(Transaction trans) {
		StringBuffer upd = new StringBuffer();
		upd.append("update checkbook set lgcl_dlt_dt = CURRENT_timestamp ");
		upd.append(" where trans_id = ? ");
		
		jdbcTmplt.update( upd.toString() , trans.getTransID() );
	}
	
	
	
	
	public List<RecurringPymt> getAllRecurringPymts() {
		return getRecurringPymts("");
	}
	public List<RecurringPymt> getActiveRecurringPymts() {
		StringBuffer where = new StringBuffer();
		where.append(" where ").append( RecurringPymtColumnNames.INACTIVE_DT ).append(" is null ");
		where.append(" and ").append( RecurringPymtColumnNames.EFF_DT ).append(" <= current_date ");
		where.append(" and ( ").append( RecurringPymtColumnNames.TERM_DT ).append(" is null ");
		where.append("             or   ");
		where.append("        ( ").append( RecurringPymtColumnNames.TERM_DT ).append(" >= current_date   or   ").append( RecurringPymtColumnNames.DATE_OF_LAST_PYMT ).append(" < ").append( RecurringPymtColumnNames.TERM_DT ).append(" )");
		where.append("             or   ");
		where.append("        ( ").append( RecurringPymtColumnNames.TERM_DT ).append(" <= current_date  and   ").append( RecurringPymtColumnNames.DATE_OF_LAST_PYMT ).append(" is null ) )");
		return getRecurringPymts( where.toString() );
	}
	private List<RecurringPymt> getRecurringPymts(String whereClause) {
		StringBuffer qry = new StringBuffer();
		qry.append("select ").append( RecurringPymtColumnNames.getAllColumnNames() ).append(" ");
		qry.append(" from recurring_pymt ");
		qry.append( whereClause );
		
		List<RecurringPymt> trans = jdbcTmplt.query( qry.toString()
				, new RowMapper<RecurringPymt>() {
					public RecurringPymt mapRow(ResultSet rs , int rowNum) throws SQLException {
						RecurringPymt t = new RecurringPymt();
						t.setId( rs.getInt( RecurringPymtColumnNames.ID.toString() ) ); 
						t.setPayTo( rs.getString( RecurringPymtColumnNames.PAY_TO.toString() ));
						t.setAmount( rs.getBigDecimal( RecurringPymtColumnNames.AMOUNT.toString() ));
						if (  rs.getDate( RecurringPymtColumnNames.DATE_OF_LAST_PYMT.toString() ) != null )
							t.setDateOfLastPymt( rs.getDate( RecurringPymtColumnNames.DATE_OF_LAST_PYMT.toString() ).toLocalDate() );
						t.setFrequency( getTermR( rs.getInt( RecurringPymtColumnNames.FREQUENCY.toString() )) );
						t.setEffDt( rs.getDate( RecurringPymtColumnNames.EFF_DT.toString() ).toLocalDate() );
						if ( rs.getDate( RecurringPymtColumnNames.TERM_DT.toString() ) != null )
							t.setTermDt( rs.getDate( RecurringPymtColumnNames.TERM_DT.toString() ).toLocalDate() );
						if ( rs.getDate( RecurringPymtColumnNames.INACTIVE_DT.toString() ) != null )
							t.setInactiveDt( rs.getDate( RecurringPymtColumnNames.INACTIVE_DT.toString() ).toLocalDate() );
						return t;
					}
		});
		return trans;
	}
	
	
	/**
	 * Query for a specific TERM_R
	 * @param id
	 * @return
	 */
	private RecurringTerm getTermR(int id) {
		StringBuffer qry = new StringBuffer();
		qry.append("select ").append( TermColumnNames.getAllColumns() ).append(" ");
		qry.append(" from term_r where ").append( TermColumnNames.ID ).append(" =").append( id );
		
		RecurringTerm trans = jdbcTmplt.queryForObject( qry.toString()
				, new RowMapper<RecurringTerm>() {
					public RecurringTerm mapRow(ResultSet rs , int rowNum) throws SQLException {
						RecurringTerm t = new RecurringTerm();
						t.setId( rs.getInt( TermColumnNames.ID.toString() ) ); 
						t.setDescription( rs.getString( TermColumnNames.DESCRIPTION.toString() ) );
						t.setOnThisDate( rs.getShort( TermColumnNames.ON_THIS_DATE.toString() ) );
//						t.setOnThisDayOfWeek( rs.getShort( TermColumnNames.ON_THIS_DAY_OF_WEEK.toString() ) );
						if ( rs.getShort( TermColumnNames.ON_THIS_DAY_OF_WEEK.toString() ) > 0 )
							t.setOnThisDayOfWeek( DayOfWeek.of( rs.getShort( TermColumnNames.ON_THIS_DAY_OF_WEEK.toString() ) ) );
						t.setAlternate( rs.getShort( TermColumnNames.ALTERNATE.toString()) );
						t.setType( TermType.valueOf( rs.getString( TermColumnNames.TYPE.toString()) ) );
						return t;
					}
		});
		return trans;
	
	}
	
	
	
	
	public List<RecurringTerm> getAllTermR() {
		StringBuffer qry = new StringBuffer();
		qry.append("select ").append( TermColumnNames.getAllColumns() ).append(" ");
		qry.append(" from term_r ");
		
		List<RecurringTerm> trans = jdbcTmplt.query( qry.toString()
				, new RowMapper<RecurringTerm>() {
					public RecurringTerm mapRow(ResultSet rs , int rowNum) throws SQLException {
						RecurringTerm t = new RecurringTerm();
						t.setId( rs.getInt( TermColumnNames.ID.toString() ) ); 
						t.setDescription( rs.getString( TermColumnNames.DESCRIPTION.toString() ) );
						t.setOnThisDate( rs.getShort( TermColumnNames.ON_THIS_DATE.toString() ) );
//						t.setOnThisDayOfWeek( rs.getShort( TermColumnNames.ON_THIS_DAY_OF_WEEK.toString() ) );
						if ( rs.getShort( TermColumnNames.ON_THIS_DAY_OF_WEEK.toString() ) > 0 )
							t.setOnThisDayOfWeek( DayOfWeek.of( rs.getShort( TermColumnNames.ON_THIS_DAY_OF_WEEK.toString() ) ) );
						t.setAlternate( rs.getShort( TermColumnNames.ALTERNATE.toString() ) );
						t.setType( TermType.valueOf( rs.getString( TermColumnNames.TYPE.toString() ) ) );
						return t;
					}
		});
		return trans;
	
	}
	
	
	
	
	
	public void insertRecurringPayment(RecurringPymt pymt) {
		StringBuffer ins = new StringBuffer();
		ins.append("insert into recurring_pymt ( ").append( RecurringPymtColumnNames.getInsertColumns() ).append(" ");
		if ( pymt.getTermDt() != null )
			ins.append(" , ").append( RecurringPymtColumnNames.TERM_DT ).append(" ");
		ins.append(" )  values  ( ?, ?, ?, ? ");
		if ( pymt.getTermDt() != null )
			ins.append(" , ? ");
		ins.append(" ) ");
		
		if ( pymt.getTermDt() != null ) {
			jdbcTmplt.update(
					ins.toString()
					, pymt.getPayTo() , pymt.getAmount() , Date.valueOf( pymt.getEffDt() ) 
					, +pymt.getFrequency().getId() , Date.valueOf( pymt.getTermDt() ) 
					);
		} else {
			jdbcTmplt.update(
					ins.toString()
					, pymt.getPayTo() , pymt.getAmount() , Date.valueOf( pymt.getEffDt() ) 
					, pymt.getFrequency().getId() 
					);
		}
	}
	public void updateRecurringPayment(RecurringPymt pymt) {
		StringBuffer ins = new StringBuffer();
		ins.append("update recurring_pymt set ").append( RecurringPymtColumnNames.PAY_TO ).append(" = ? ");
		ins.append(" , ").append( RecurringPymtColumnNames.AMOUNT ).append("=? ");
		ins.append(" , ").append( RecurringPymtColumnNames.EFF_DT ).append("=? ");
		ins.append(" , ").append( RecurringPymtColumnNames.FREQUENCY ).append("=? ");
		if ( pymt.getTermDt() != null )
			ins.append(" , ").append( RecurringPymtColumnNames.TERM_DT ).append("=? ");
		else
			ins.append(" , ").append( RecurringPymtColumnNames.TERM_DT ).append(" = null ");
		ins.append(" where ").append( RecurringPymtColumnNames.ID ).append(" = ? ");
		
		if ( pymt.getTermDt() != null ) {
			jdbcTmplt.update(
					ins.toString()
					, pymt.getPayTo() , pymt.getAmount() , Date.valueOf( pymt.getEffDt() ) 
					, pymt.getFrequency().getId() , Date.valueOf( pymt.getTermDt() ) 
					, pymt.getId()
					);
		} else {
			jdbcTmplt.update(
					ins.toString()
					, pymt.getPayTo() , pymt.getAmount() , Date.valueOf( pymt.getEffDt() ) 
					, pymt.getFrequency().getId() 
					, pymt.getId()
					);
		}
	}
}
