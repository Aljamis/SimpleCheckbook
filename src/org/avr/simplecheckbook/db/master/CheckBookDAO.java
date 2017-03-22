package org.avr.simplecheckbook.db.master;

import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.avr.simplecheckbook.dataobjects.Balance;
import org.avr.simplecheckbook.dataobjects.Transaction;
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
	public void createTables() {
		createDailyBalanceTable();
		createCheckBook();		
	}
	
	
	private void createDailyBalanceTable() {
		String createBalance = "CREATE TABLE daily_balance ( date date , amount decimal(9,2) ) ";
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
		createBook.append(" , cleared boolean ");
		createBook.append(" , debit decimal(9,2) ");
		createBook.append(" , credit decimal(9,2) ");
		createBook.append(" , lgcl_dlt_dt timestamp )");

		try {
			jdbcTmplt.execute( createBook.toString() );
			jdbcTmplt.execute("create index date_idx on checkbook (tx_date) ");
		} catch (Exception ex) {
			ex.printStackTrace();
			Platform.exit();
		}
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
			/*
			 * Now this return a collection .. not just a single object
			 */
			Balance bal = jdbcTmplt.queryForObject( qry.toString() 				
					, new RowMapper<Balance>() {
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
		ins.append("insert into checkbook ( tx_date, checknum, payee, memo, cleared, debit , credit ) ");
		ins.append(" values ( ?, ? , ? , ? , ? , ? , ?) ");
		
		jdbcTmplt.update(
				ins.toString()
				, t.getTxDate() , t.getCheckNumber() , t.getPayee() , t.getMemo() , t.getCleared() , t.getDebit() , t.getCredit() 
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
}
