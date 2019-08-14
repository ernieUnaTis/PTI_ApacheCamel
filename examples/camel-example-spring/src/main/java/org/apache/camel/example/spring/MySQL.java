package org.apache.camel.example.spring;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.Main;
import org.apache.camel.Processor;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.example.spring.JdbcSingleton;
import org.apache.camel.Message;
import java.util.*;
import java.io.*;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import java.sql.Types;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;

public class MySQL {

	private Logger log = Logger.getLogger(this.getClass().getSimpleName());
	protected final static long DAYSms = 86400000L; // 24h * 60min * 60seg * 1000ms
	private static String uri;
	private static Map<String,Object> rowMap;
	private static Timestamp ottimestamp;

	public static void registraEventoUpdateType(JdbcTemplate jdbc,String carrier,String movil, String serviceId,String productId,String ut,String ot,String svt,String sai,String cf) {

		jdbc.update ("INSERT INTO eventos_UpdateType (evut_carrier, evut_movil, evut_serviceid, evut_productid, evut_updatetype, evut_operationtime, evut_subscriptionvalidtime, evut_subscriptionaddtionalinfo, evut_chargedfee, evut_fecha) VALUES (?,?,?,?,?,?,?,?,?,now())",
				new Object[]{carrier,movil,serviceId,productId,ut,ot,svt,sai,cf});
	}

	public static Map<String, Object> getRow (JdbcTemplate jdbc, String serviceId, String productId ) {

		try {
			return rowMap = jdbc.queryForMap ("SELECT keywordalta, keywordbaja, CONCAT(nc, '') AS nc, plataforma, producto, operador, precio, CONCAT(susc_id, '') AS susc_id, service_id_navegar, CONCAT(pfree, '') AS pfree, CONCAT(poda_cacre_id, '') AS poda_cacre_id FROM politicas_datasync WHERE service_id = ? AND product_id = ?",
				new Object[]{serviceId, productId});

		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

}
