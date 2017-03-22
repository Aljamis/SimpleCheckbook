package org.avr.simplecheckbook.db.master;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.avr.simplecheckbook.dataobjects.MasterCheckBook;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javafx.application.Platform;



/**
 * Query the MASTER db for all Checkbooks created locally.
 * @author Alfonso
 *
 */
public class SpringMasterDAO {
	
	private JdbcTemplate jdbcTmplt;
	
	
	public SpringMasterDAO() {
		this.connect("");
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
	 * Query MASTER db for all the checkbooks.  If the DB does not exist, create it.
	 * @return
	 */
	public List<MasterCheckBook> findAllCheckBooks() {
		try {
			List<MasterCheckBook> books = jdbcTmplt.query(
					"select dbname , dblocation , description from checkbooks "
					, new RowMapper<MasterCheckBook>() {
						public MasterCheckBook mapRow(ResultSet rs , int rowNum) throws SQLException {
							MasterCheckBook cb = new MasterCheckBook();
							cb.setDbName( rs.getString("dbname"));
							cb.setDbLocation( rs.getString("dblocation"));
							cb.setDescription( rs.getString("description"));
							return cb;
						}
					} );
			return books;
		} catch (CannotGetJdbcConnectionException sqlEx) {
			connect(";create=true");
			createTable();
			return findAllCheckBooks();
		}
	}
	
	
	
	
	/**
	 * Create a table.
	 */
	private void createTable() {
		StringBuffer str = new StringBuffer("create table checkbooks ( ");
		str.append("  dbName      varChar(20) ");
		str.append(", dbLocation  varChar(200) ");
		str.append(", description varChar(100) ");
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
		ins.append("insert into checkbooks ( dbname , dblocation , description ) ");
		ins.append(" values ( ? , ? , ? ) ");
		this.jdbcTmplt.update( 
				ins.toString()
				, cb.getDbName() , cb.getDbLocation() , cb.getDescription()  );
	}

}
