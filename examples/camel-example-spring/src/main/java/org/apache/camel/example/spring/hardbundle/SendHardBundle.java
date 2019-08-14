package org.apache.camel.example.spring.hardbundle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.Logger;
import org.apache.camel.example.spring.hardbundle.JdbcSingletonHB;

public class SendHardBundle implements Processor {
    private Logger log = Logger.getLogger(this.getClass().getSimpleName());
    
    public void process(Exchange exchange) throws Exception {
    	
    	
    	
    	String respuestaBoss = null;
        Message msg = exchange.getIn();
        String mensaje = null;
        boolean notificarExitoReintento = false;
        String operador = (String)msg.getHeader("CARRIER");
        String producto = (String)msg.getHeader("PRODUCTO");
        String msisdn = (String)msg.getHeader("MSISDN");
        String transId = (String)msg.getHeader("TRANSID");
        String updateType = (String)msg.getHeader("UPDATETYPE");
        String plataforma = (String)msg.getHeader("PLATAFORMA");
        String productId = (String)msg.getHeader("PRODUCTID");
        String serviceId = (String)msg.getHeader("SERVICEID");
        String eventName = "";
        String action = "";
        
        if (msg.getHeader("esReintento") != null)
        {notificarExitoReintento = true;  }
        
        if(updateType.equalsIgnoreCase("0") || updateType.equalsIgnoreCase("3"))
        {

        //NewMap
        Map<String,String> newMap = new HashMap<String,String>();
        
        if(plataforma.equalsIgnoreCase("CLOUD_COLOMBIA") || plataforma.equalsIgnoreCase("CLOUD")) {
        	/*log.info("La plataforma es CLOUD");*/
        
        	/*Obtener los datos del servicio */
        	 Map<String, Object> map = HardBundleQueries.getServicio(JdbcSingletonHB.getJdbcTemplate(), serviceId,productId);
        	  if(map!=null){
            	for (Map.Entry<String, Object> entry : map.entrySet()) {
            		//if(entry.getValue() instanceof String){
            			newMap.put(entry.getKey(), entry.getValue().toString());
            			//log.info(entry.getKey() + " : " + entry.getValue().toString());
            		//} 
            	}
            	
            	if(updateType.equalsIgnoreCase("0")){
            		eventName = "Status Updated";
            		action = "Activation";
            	}else{
            	        eventName = "Status Updated";
            		action = "Deactivation";
            	}
            	
            	
            	String colaAMQPRe = "co"+"_"+newMap.get("id_bill_prcn");
            	if(colaAMQPRe.length() >  6) 
            	{
            	    colaAMQPRe = colaAMQPRe.replace("_","");
            	}
            	
            	HardBundleQueries.registraEventoHBCloud(JdbcSingletonHB.getJdbcTemplate(),serviceId,msisdn,newMap.get("product_id_prcn"),updateType,transId,eventName,action,newMap.get("id_bill_prcn"),newMap.get("tipo_prcn"),newMap.get("nc_prcn"),newMap.get("id_grupo_prcn"),newMap.get("producto_bill_prcn"),colaAMQPRe);
            	msg.setHeader("status_hardbundle", "OK");
            	respuestaBoss = "OK";
            	//log.info(" SE_REGISTRA_EVENTO_EN_BD");
        	 }
        	 
        }else {

            Map<String, Object> map = HardBundleQueries.getServicio(JdbcSingletonHB.getJdbcTemplate(), serviceId,productId);

            if(map!=null){
            	for (Map.Entry<String, Object> entry : map.entrySet()) {
            	    newMap.put(entry.getKey(), entry.getValue().toString());
            	}
            	
            	if(updateType.equalsIgnoreCase("0")){
            	        eventName = "Status Updated";
            		action = "Activation";
            	}else{
            	        eventName = "Status Updated";
            		action = "Deactivation";
            	}
            	
            	HardBundleQueries.registraEventoHB(JdbcSingletonHB.getJdbcTemplate(),serviceId,msisdn,newMap.get("product_id_prcn"),updateType,transId,eventName,action,newMap.get("id_bill_prcn"),newMap.get("tipo_prcn"),newMap.get("nc_prcn"),newMap.get("id_grupo_prcn"),newMap.get("producto_bill_prcn"));
            	msg.setHeader("status_hardbundle", "OK");
            	respuestaBoss = "OK";
            	//log.info(" SE_REGISTRA_EVENTO_EN_BD");

            }
        }
        }else {
            
            msg.setHeader("status_hardbundle", "OK|UPDATETYPE_NOAPLICA");
            msg.setHeader("statusHttp","200");
            msg.setHeader("reintento", "0");
            log.info("OPERADOR["+operador+"]REINTENTO[0] RET[OK|UPDATETYPE_NOAPLICA] UPDATETYPE["+updateType+"]");
        }
        
        log.info("OPERADOR["+operador+"] MOVIL["+msisdn+"] ESTATUS_HB["+respuestaBoss+"]");
    }
    
    /**
	 * Genera map de la query
	 * @return
	 */
	public static Map<String, String> getQueryMap(String query) {
		Map<String, String> map = new HashMap<String, String>();
		
		return map;
	}
}
