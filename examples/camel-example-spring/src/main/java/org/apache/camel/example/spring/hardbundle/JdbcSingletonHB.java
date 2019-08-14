package org.apache.camel.example.spring.hardbundle;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import javax.sql.DataSource;

public class JdbcSingletonHB {

        private static JdbcTemplate jdbcTemplate = null;

        private static JdbcSingletonHB jdbcSingleton= new JdbcSingletonHB();

        public static JdbcSingletonHB getInstance(){
                return jdbcSingleton;
        }

        private  JdbcSingletonHB(){
                ApplicationContext ac = new ClassPathXmlApplicationContext("classpath*:dbcontext.xml");
                DataSource dataSource = (DataSource) ac.getBean("dataSourceHB");

                JdbcSingletonHB.jdbcTemplate = new JdbcTemplate (dataSource);
        }

        public static JdbcTemplate getJdbcTemplate(){
                return JdbcSingletonHB.jdbcTemplate;
        }
}
