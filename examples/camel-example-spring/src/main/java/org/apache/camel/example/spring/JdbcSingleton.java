package org.apache.camel.example.spring;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import javax.sql.DataSource;

public class JdbcSingleton {

	private static JdbcTemplate jdbcTemplate = null;

	private static JdbcSingleton jdbcSingleton= new JdbcSingleton();

	public static JdbcSingleton getInstance(){
		return jdbcSingleton;
	}

	private  JdbcSingleton(){
		ApplicationContext ac = new ClassPathXmlApplicationContext("classpath*:dbcontext.xml");
		DataSource dataSource = (DataSource) ac.getBean("dataSource");

		JdbcSingleton.jdbcTemplate = new JdbcTemplate (dataSource);
	}

	public static JdbcTemplate getJdbcTemplate(){
		return JdbcSingleton.jdbcTemplate;
	}
}
