package org.avr.simplecheckbook.db.master;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.avr.simplecheckbook.dataobjects.MasterCheckBook;
import org.avr.simplecheckbook.utils.CheckBookVersion;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javafx.application.Platform;
import zorg.avr.derby.TempRow;



/**
 * Query the MASTER db for all Checkbooks created locally.
 * @author Alfonso
 *
 */
public class SpringMasterDAO {
	
	private JdbcTemplate jdbcTmplt;
	static final String SCHEMA = CheckBookVersion.getVersion();
	
	
	public SpringMasterDAO() {
		this.connect("");
		doesDBexist();
	}
	
	
	
	/**
	 * Setup connections to the database.
	 * @param suffix
	 */
	private void connect(String suffix) {
		DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:derby:ChkBookMaster"+ suffix);
		jdbcTmplt = new JdbcTemplate(dataSource);
	}
	
	
	
	/**
	 * Does this DB exist yet? Or should I create it?
	 */
	private void doesDBexist() {
		try {
			findAllCheckBooks();
		} catch (CannotGetJdbcConnectionException sqlEx) {
			connect(";create=true");
			createTable();
		}
	}
	
	
	
	
	/**
	 * Query MASTER db for all the checkbooks.
	 * @return
	 */
	public List<MasterCheckBook> findAllCheckBooks() {
		List<MasterCheckBook> books = jdbcTmplt.query(
				"select "+ MasterColumnNames.getAllColumnNames() +" from checkbooks "
				, new RowMapper<MasterCheckBook>() {
					public MasterCheckBook mapRow(ResultSet rs , int rowNum) throws SQLException {
						MasterCheckBook cb = new MasterCheckBook();
						cb.setDbName( rs.getString( MasterColumnNames.DBNAME.toString() ));
						cb.setDbLocation( rs.getString( MasterColumnNames.DBLOCATION.toString() ));
						cb.setDescription( rs.getString( MasterColumnNames.DESCRIPTION.toString() ));
						return cb;
					}
				} );
		return books;
	}
	
	
	
	
	/**
	 * Create a table.
	 */
	private void createTable() {
		StringBuffer str = new StringBuffer("create table checkbooks ( ");
		str.append("  ").append( MasterColumnNames.DBNAME ).append("      varChar(20) ");
		str.append(", ").append( MasterColumnNames.DBLOCATION ).append("  varChar(200) ");
		str.append(", ").append( MasterColumnNames.DESCRIPTION ).append(" varChar(100) ");
		str.append(" ) ");
		
		try {
			jdbcTmplt.execute( str.toString() );
		} catch (Exception ex) {
			ex.printStackTrace();
			Platform.exit();
		}
	}
	
	
	
	
	public void saveCheckbook(MasterCheckBook cb) {
		StringBuffer ins = new StringBuffer();
		ins.append("insert into checkbooks ( ").append( MasterColumnNames.getAllColumnNames() );
		ins.append(" )  values  ( ? , ? , ? ) ");
		this.jdbcTmplt.update( 
				ins.toString()
				, cb.getDbName() , cb.getDbLocation() , cb.getDescription()
		);
	}
}
