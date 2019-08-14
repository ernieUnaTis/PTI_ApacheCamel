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

public class SendToBillingEndpoint implements Processor {

	private Logger log = Logger.getLogger(this.getClass().getSimpleName());

	public SendToBillingEndpoint(){
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
		String carrier = null;
		String operador = null;
		String producto = null;
		String keywordalta = null;
		String keywordbaja = null;
		String plataforma = null;
		String susc_id = null;
		String pfree = null;
		String precio = null;
		String service_id_navegar = null;
		String nc = null;
		String poda_cacre_id = null;
		String accion = null;
		String id = null;
		String spId = null;
		String spIdVendor = null;
		String productId = null;
		String serviceId = null;
		String ut = null;
		int utInt;
		String ot = null;
		String svt = null;
		String sai = null;
		String cf = null;
		String urlBill = null;
		String[] data = null;
		int status = -1;
		String metodo = null;
		String result = null;

		long timerIni = (new Date()).getTime();
		long timerFin = 0;
		long timerIniXml = 0;
		long timerFinXml = 0;
                long timerIniBd = 0;
		long timerFinBd = 0;
                long timerIniHttp = 0;
		long timerFinHttp = 0;

		mensaje = exchange.getIn().getBody(String.class);

		carrier = (String) msg.getHeader("CARRIER");
		id = (String) msg.getHeader("ID_MENSAJE");
		movil = (String) msg.getHeader("MSISDN");
		operador = "br.vivo";
		utInt = (int) msg.getHeader("UPDATETYPE");
		ut = Integer.toString(utInt);
		spId = (String) msg.getHeader("SPID");
		spIdVendor = (String) msg.getHeader("SPIDVENDOR");
		serviceId = (String) msg.getHeader("SERVICEID");
		productId = (String) msg.getHeader("PRODUCTID");
		//ip = "10.253.126.120:8082";
		ip = Conf.BILLING_IP;
		ot = (String) msg.getHeader("OT");
		svt = (String) msg.getHeader("SVT");
		sai = (String) msg.getHeader("SAI");
		cf = (String) msg.getHeader("CF");


		if(ut.equalsIgnoreCase("5")){
			if(cf.equalsIgnoreCase("-1")){
				cf = "0";
			}
			if(sai.equalsIgnoreCase("")){
				sai = null;
			}
			MySQL.registraEventoUpdateType(JdbcSingleton.getJdbcTemplate(),operador, movil, serviceId, productId, ut, ot, svt, sai, cf);
			log.info("[carrier:"+carrier+"] [operador:"+operador+"] [spId:"+spId+"] [spIdVendor:"+spIdVendor+"] [serviceId:"+serviceId+"] [productId:"+productId+"] [ut:"+ut+"] [ot:"+ot+"] [svt:"+svt+"] [sai:"+sai+"] [cf:"+cf+"] [movil:"+movil+"] [id:"+id+"] SE_REGISTRA_UT5_EN_BD");
			return;
		}

		Hashtable hashParam = null;

		timerIniXml = (new Date()).getTime();
		hashParam = Utils.obtenerInfoXml(serviceId, productId);
		timerFinXml = (new Date()).getTime();

		Map<String,String> newMap = new HashMap<String,String>();
	
		if(!hashParam.isEmpty()) {
			keywordalta = (String)hashParam.get("keywordalta");
			keywordbaja = (String)hashParam.get("keywordbaja");
			nc = (String)hashParam.get("nc");
			plataforma = (String)hashParam.get("plataforma");
			producto = (String)hashParam.get("producto");
			operador = (String)hashParam.get("operador");
			precio = (String)hashParam.get("precio");
			//Si no hay dato o es null en la tabla politicas_datasync se setea el precio a cero - emartine - 20171004
			if(null==precio) {precio = "0";}
			susc_id = String.valueOf(hashParam.get("susc_id"));
			service_id_navegar = (String)hashParam.get("service_id_navegar");
			pfree = (String)hashParam.get("pfree");
			poda_cacre_id = (String)hashParam.get("poda_cacre_id");
			metodo = "ARCHIVOXML";
			//log.info("[keywordalta:"+keywordalta+"] [keywordbaja:"+keywordbaja+"] [nc:"+nc+"] [plataforma:"+plataforma+"] [producto:"+producto+"] [operador:"+operador+"] [precio:"+precio+"] [susc_id:"+susc_id+"] [service_id_navegar:"+service_id_navegar+"] [pfree:"+pfree+"] [poda_cacre_id:"+poda_cacre_id+"] [spId:"+spId+"] [spIdVendor:"+spIdVendor+"] [serviceId:"+serviceId+"] [productId:"+productId+"] [ut:"+ut+"] [urlBill:"+urlBill+"] [urlParams:"+urlParams+"] [id:"+id+"] SE_OBTUVO_REGISTRO_DEL_ARCHIVO_XML");
		
		} else {
			timerIniBd = (new Date()).getTime();
			Map<String, Object> map = MySQL.getRow(JdbcSingleton.getJdbcTemplate(), serviceId, productId);
			timerFinBd = (new Date()).getTime();

			if(map!=null){
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					if(entry.getValue() instanceof String){
						newMap.put(entry.getKey(), (String) entry.getValue());
						//log.info(entry.getKey() + " : " + entry.getValue());
					} 
				}
			}

			keywordalta = newMap.get("keywordalta");
			keywordbaja = newMap.get("keywordbaja");
			nc = newMap.get("nc");
			plataforma = newMap.get("plataforma");
			producto = newMap.get("producto");
			operador = newMap.get("operador");
			precio = newMap.get("precio");
			if(null==precio) {precio = "0";}
			susc_id = newMap.get("susc_id");
			service_id_navegar = newMap.get("service_id_navegar");
			pfree = newMap.get("pfree");
			poda_cacre_id = newMap.get("poda_cacre_id");
                        metodo = "TABLABD";
			
			//log.info("[keywordalta:"+keywordalta+"] [keywordbaja:"+keywordbaja+"] [nc:"+nc+"] [plataforma:"+plataforma+"] [producto:"+producto+"] [operador:"+operador+"] [precio:"+precio+"] [susc_id:"+susc_id+"] [service_id_navegar:"+service_id_navegar+"] [pfree:"+pfree+"] [poda_cacre_id:"+poda_cacre_id+"] [spId:"+spId+"] [spIdVendor:"+spIdVendor+"] [serviceId:"+serviceId+"] [productId:"+productId+"] [ut:"+ut+"] [urlBill:"+urlBill+"] [urlParams:"+urlParams+"] [id:"+id+"] SE_OBTUVO_REGISTRO_DE_BD");
		

			if(newMap.isEmpty()) {
				log.info("[mensaje:"+mensaje+"] [movil:"+movil+"] [operador:"+operador+"] [producto:"+producto+"] [ut:"+ut+"] [urlBill:"+urlBill+"] [urlParams:"+urlParams+"] [id:"+id+"] SERVICIO_NO_CONFIGURADO");
				return;
			}

		}

		//log.info("keywordalta: "+keywordalta+" keywordbaja:"+keywordbaja+" plataforma:"+plataforma+" producto: "+producto+" operador:"+operador+" precio:"+precio+" service_id_navegar:"+service_id_navegar+" pfree:"+pfree+" poda_cacre_id:"+poda_cacre_id);

		Header estadoRouter = null;
		String estadoRouterValue = null;
		String respuesta = null;

		//log.info("[mensaje:"+mensaje+"] [movil:"+movil+"] [operador:"+operador+"] [producto:"+producto+"] [spId:"+spId+"] [spIdVendor:"+spIdVendor+"] [serviceId:"+serviceId+"] [productId:"+productId+"] [ut:"+ut+"] [urlBill:"+urlBill+"] [urlParams:"+urlParams+"] [id:"+id+"]");

		if(cf.equalsIgnoreCase("-1")){
			cf = precio;
		}

		if(ut.equalsIgnoreCase("0")){
			urlBill = "http://"+ip+"/DaVinci/servlet/club/RenovacionNotificacion";
			urlParams = "movil="+movil+"&operador="+carrier+"&producto="+producto+"&origen=BUNDLE&keyword=BUNDLE&estado=0&origen=BUNDLE&fee=0&esoferta=2";
		} else if(ut.equalsIgnoreCase("3")){
			urlBill = "http://"+ip+"/DaVinci/servlet/club/doBaja";
			urlParams = "movil="+movil+"&operador="+carrier+"&producto="+producto+"&origen=BUNDLE";
		} else if(ut.equalsIgnoreCase("1") || ut.equalsIgnoreCase("4")){
			urlBill = "http://"+ip+"/DaVinci/servlet/club/DoSuspension";
			urlParams = "movil="+movil+"&operador="+carrier+"&producto="+producto+"";
		} else if(ut.equalsIgnoreCase("2")){
			urlBill = "http://"+ip+"/DaVinci/servlet/club/DoActivacion";
			urlParams = "movil="+movil+"&operador="+carrier+"&producto="+producto+"";
		} else {
			log.info("[mensaje:"+mensaje+"] [movil:"+movil+"] [operador:"+operador+"] [carrier:"+carrier+"] [producto:"+producto+"] [ut:"+ut+"] [urlBill:"+urlBill+"] [urlParams:"+urlParams+"] [id:"+id+"] UPDATETYPE_NO_CONFIGURADO");
			return;
		}

		httpClient = new HttpClient();
		getMethod = new GetMethod(urlBill);

		getMethod.setQueryString(urlParams);

		timerIniHttp = (new Date()).getTime();
		statusHttp = httpClient.executeMethod(getMethod);
		timerFinHttp = (new Date()).getTime();

		//log.info("[mensaje:"+mensaje+"] [movil:"+movil+"] [operador:"+operador+"] [producto:"+producto+"] [ut:"+ut+"] [urlBill:"+urlBill+"] [urlParams:"+urlParams+"] [id:"+id+"]");

		result = getMethod.getResponseBodyAsString();

		if(statusHttp == HttpStatus.SC_OK){
			//result = getMethod.getResponseBodyAsString();
			log.info("[result:"+result+"]");

			if (result.indexOf("|") != -1){	
				data = result.split("\\|");

				if(data[1].equalsIgnoreCase("OK")){
					status = 0;
					//log.info("[ INFO ] procesarSuscClub [Type:sms_datasync] La suscripcion fue procesada correctamente");
				} else {
					status = 1;
					//log.info("[ ERROR ] procesarSuscClub [Type:sms_datasync] La suscripcion NO fue procesada correctamente");
				}
				getMethod.releaseConnection();

			} else {

				if(result.equalsIgnoreCase("OK")){
					status = 0;
					//log.info("[ INFO ] procesarSuscClub [Type:sms_datasync] La suscripcion fue procesada correctamente");
				} else {
					status = 1;
					//log.info("[ ERROR ] procesarSuscClub [Type:sms_datasync] La suscripcion NO fue procesada correctamente");
				}
				getMethod.releaseConnection();
			}
		} else {
			//log.info("[ ERROR ] procesarSuscClub [Type:sms_datasync] Ocurrio un error o la pagina no fue encontrada");
		}

		timerFin = (new Date()).getTime();

		log.info("[keywordalta:"+keywordalta+"] [keywordbaja:"+keywordbaja+"] [nc:"+nc+"] [plataforma:"+plataforma+"] [producto:"+producto+"] [operador:"+operador+"] [carrier:"+carrier+"] [precio:"+precio+"] [susc_id:"+susc_id+"] [service_id_navegar:"+service_id_navegar+"] [pfree:"+pfree+"] [poda_cacre_id:"+poda_cacre_id+"] [spId:"+spId+"] [spIdVendor:"+spIdVendor+"] [serviceId:"+serviceId+"] [productId:"+productId+"] [ut:"+ut+"] [ot:"+ot+"] [svt:"+svt+"] [sai:"+sai+"] [cf:"+cf+"] [urlBill:"+urlBill+"] [urlParams:"+urlParams+"] [id:"+id+"] [metodo:"+metodo+"] [result:"+result+"] [status:"+status+"] [ExecTimeXml:"+(timerFinXml - timerIniXml)+" ms] [ExecTimeBd:"+(timerFinBd - timerIniBd)+" ms] [ExecTimeHttp:"+(timerFinHttp - timerIniHttp)+" ms] [ExecTime:"+(timerFin - timerIni)+" ms]");


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
