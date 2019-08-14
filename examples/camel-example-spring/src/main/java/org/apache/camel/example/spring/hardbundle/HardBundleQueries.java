package org.apache.camel.example.spring.hardbundle;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import java.sql.Types;
import java.sql.Timestamp;
import java.util.Date;
import java.util.*;
import java.io.*;


import org.apache.log4j.Logger;

public class HardBundleQueries {
    
    private Logger log = Logger.getLogger(this.getClass().getSimpleName());
    private static String uri;
    private static Map<String,Object> rowMap;
    private static Timestamp ottimestamp;
    
    
    /***
     * Consultas de Servicios HardBundle que no son CLOUD
     */
    public static Map<String, Object> getServicio (JdbcTemplate jdbc, String serviceId, String productId ) {

        try {
                return rowMap = jdbc.queryForMap ("SELECT product_id_prcn,id_bill_prcn,tipo_prcn,id_grupo_prcn,producto_bill_prcn,nc_prcn FROM conmov_productos_configuracion WHERE service_id_prcn = ? AND product_id_prcn = ?",
                        new Object[]{serviceId,productId});

        } catch (EmptyResultDataAccessException e) {
                return null;
        }
    }
    
    public  static void registraEventoHB(JdbcTemplate jdbc,String serviceId,String movil,String productId,String eventId,String transId,String eventName,String accion,String idBill,String tipo_prcn,String jsnNummt,String grupoId,String producto_bill_prcn) {
        jdbc.update ("INSERT INTO integrador_concentrador_movistar (conmov_serviceId, conmov_idJson, conmov_movil, conmov_eventId, conmov_eventName, conmov_action, conmov_reason, conmov_productId, conmov_statusJson, conmov_idBill, conmov_nmBill, " + 
            "conmov_idOper, conmov_nmOper, conmov_idGrupo, conmov_tipoServ, conmov_mtNuco) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                     new Object[]{serviceId,"SDPNotifyEventID",movil,"Act0002",eventName,accion,"",productId,"on",idBill,producto_bill_prcn,"732200","co.movistar",grupoId,tipo_prcn,jsnNummt});
    }
    
    
    public  static void registraEventoHBCloud(JdbcTemplate jdbc,String serviceId,String movil,String productId,String eventId,String transId,String eventName,String accion,String idBill,String tipo_prcn,String jsnNummt,String grupoId,String producto_bill_prcn,String colaAMQPRe) {
        jdbc.update ("INSERT INTO integrador_cloud (cloud_service_id, cloud_id_json, cloud_movil, cloud_event_id, cloud_event_name, cloud_action, cloud_reason, cloud_product_id,cloud_status_json, cloud_club_id, cloud_club_name, cloud_oper_id, cloud_oper_id_real, cloud_oper_name, cloud_status, cloud_intentos, cloud_fecha) " + 
            " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,0,0,NOW())",
                     new Object[]{serviceId,"SDPNotifyEventID",movil,"Act0002",eventName,accion,"",productId,"on",idBill,producto_bill_prcn,colaAMQPRe,"732200","co.movistar"});
    }
    
}