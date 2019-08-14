package org.apache.camel.example.spring;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.Main;
import org.apache.camel.Processor;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.Message;
import java.util.*;
import java.io.*;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.sql.DataSource;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity; 
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

public class SendToBilling implements Processor {

	private Logger log = Logger.getLogger(this.getClass().getSimpleName());

	public SendToBilling(){
	}

	public void process(Exchange exchange) throws Exception {

		HttpClient httpClient = null;
		HttpMethodBase getMethod = null;
		int statusHttp = 0;
		Message msg = exchange.getIn();
		BufferedReader reader = null;
		SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String mensaje = null;
		String urlParams = null;
		String ip =  null;
		String movil = null;
		String operador = null;
		String producto = null;
		String accion = null;
		String id = null;
		String ut = null;
		String sai = null;
		String urlBill = null;
		String[] data = null;
		int status = -1;

		Header estadoRouter = null;
		String estadoRouterValue = null;
		String respuesta = null;

		mensaje = exchange.getIn().getBody(String.class);

		mensaje = mensaje.startsWith("?") ? mensaje.substring(1, mensaje.length()) : mensaje;

		Map<String, String> map_gateway = this.getQueryMap(mensaje);
		
		ip =  map_gateway.get("ip");
		movil = map_gateway.get("movil");
		operador =  map_gateway.get("operador");
		producto =  map_gateway.get("producto");
		ut =  map_gateway.get("ut");
		sai =  map_gateway.get("sai");
		accion = (String) msg.getHeader("ACCION");
		id = (String) msg.getHeader("ID_MENSAJE");

		if(accion.equalsIgnoreCase("doAlta")){
			urlBill = "http://"+ip+"/DaVinci/servlet/club/RenovacionNotificacion";
			urlParams = "movil="+movil+"&operador="+operador+"&producto="+producto+"&origen=BUNDLE&keyword=BUNDLE&estado=0&origen=BUNDLE&fee=0&esoferta=2";
		} else if(accion.equalsIgnoreCase("doBaja")){
			urlBill = "http://"+ip+"/DaVinci/servlet/club/doBaja";
			urlParams = "movil="+movil+"&operador="+operador+"&producto="+producto+"&origen=BUNDLE";
		} else if(accion.equalsIgnoreCase("DoSuspension")){
			urlBill = "http://"+ip+"/DaVinci/servlet/club/DoSuspension";
			urlParams = "movil="+movil+"&operador="+operador+"&producto="+producto+"";
		} else if(accion.equalsIgnoreCase("DoActivacion")){
			urlBill = "http://"+ip+"/DaVinci/servlet/club/DoActivacion";
			urlParams = "movil="+movil+"&operador="+operador+"&producto="+producto+"";
		} else {
			log.info("[mensaje:"+mensaje+"] [movil:"+movil+"] [operador:"+operador+"] [producto:"+producto+"] [ut:"+ut+"] [sai:"+sai+"] [urlBill:"+urlBill+"] [urlParams:"+urlParams+"] [accion:"+accion+"] [id:"+id+"] ACCION_NO_CONFIGURADA");
			return;
		}

		httpClient = new HttpClient();
		getMethod = new GetMethod(urlBill);

		getMethod.setQueryString(urlParams);
		statusHttp = httpClient.executeMethod(getMethod);

		log.info("[mensaje:"+mensaje+"] [movil:"+movil+"] [operador:"+operador+"] [producto:"+producto+"] [ut:"+ut+"] [sai:"+sai+"] [urlBill:"+urlBill+"] [urlParams:"+urlParams+"] [accion:"+accion+"] [id:"+id+"]");
		
		if(statusHttp == HttpStatus.SC_OK){
			String result = getMethod.getResponseBodyAsString();
			log.info("[result:"+result+"]");
	
			if (result.indexOf("|") != -1){	
				data = result.split("\\|");

				if(data[1].equalsIgnoreCase("OK")){
					status = 0;
					log.info("[ INFO ] procesarSuscClub [Type:sms_datasync] La suscripcion fue procesada correctamente");
				} else {
					status = 1;
					log.info("[ ERROR ] procesarSuscClub [Type:sms_datasync] La suscripcion NO fue procesada correctamente");
				}
				getMethod.releaseConnection();

			} else {

                                if(result.equalsIgnoreCase("OK")){
                                        status = 0;
                                        log.info("[ INFO ] procesarSuscClub [Type:sms_datasync] La suscripcion fue procesada correctamente");
                                } else {
                                        status = 1;
                                        log.info("[ ERROR ] procesarSuscClub [Type:sms_datasync] La suscripcion NO fue procesada correctamente");
                                }
                                getMethod.releaseConnection();
			}
		} else {
			log.info("[ ERROR ] procesarSuscClub [Type:sms_datasync] Ocurrio un error o la pagina no fue encontrada");
		}
	
	}

	/**
	 * Genera map de la query
	 * emartine 09-09-2014
	 * @return
	 */
	public static Map<String, String> getQueryMap(String query)
			throws UnsupportedEncodingException {
		Map<String, String> map = new HashMap<String, String>();
		for (String param : query.split("&")) {
			String pair[] = param.split("=");
			String key = URLDecoder.decode(pair[0], "UTF-8");
			String value = "";
			if (pair.length > 1) {
				value = URLDecoder.decode(pair[1], "UTF-8");
			}
			map.put(new String(key), new String(value));
		}
		return map;
	}
}
