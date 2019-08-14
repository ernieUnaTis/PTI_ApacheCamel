package org.apache.camel.example.spring.niji;


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


public class ReintentoNiji implements Processor {
    private Logger log = Logger.getLogger(this.getClass().getSimpleName());
    
    public void process(Exchange exchange) throws Exception {
        HttpClient      httpClient = null;
        HttpMethodBase  getMethod = null;
        int             statusHttp = 0;
        
        String respuestaBoss = null;
        Message msg = exchange.getIn();
        BufferedReader reader = null;
        String mensaje = null;
        String parametrosUrl = null;
        
        String estadoBoss = "NOK";
        String replyevent = null;
        String partner = null;
        String trx_id = null;
        
        String urlNiji = "";
        
        try{
            int reintento = 0;
            if (msg.getHeader("reintento") != null)
            { reintento=Integer.parseInt((String)msg.getHeader("reintento"))+1; }
            
            mensaje = exchange.getIn().getBody(String.class);
            mensaje = mensaje.startsWith("?") ? mensaje.substring(1, mensaje.length()) : mensaje;
            mensaje = mensaje.replace("[", "%5B");
            mensaje = mensaje.replace("]", "%5D");
            mensaje = mensaje.replace("\"", "%22");
            urlNiji = new StringBuffer(mensaje).toString();
            
            Map<String, String> map_gateway = this.getQueryMap(mensaje);
            
            if(!map_gateway.get("operacion").equalsIgnoreCase("25") && !map_gateway.get("operacion").equalsIgnoreCase("26")){
                MultiThreadedHttpConnectionManager      multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
                HttpConnectionManagerParams params = new HttpConnectionManagerParams();
                params.setDefaultMaxConnectionsPerHost(50);
                params.setMaxTotalConnections(50);
                multiThreadedHttpConnectionManager.setParams(params);
    
                httpClient = new HttpClient(multiThreadedHttpConnectionManager);
                getMethod = new GetMethod(urlNiji);
                //getMethod.setQueryString(urlParams);
                statusHttp = httpClient.executeMethod(getMethod);
            
                if(statusHttp== 204){
                    respuestaBoss = "OK";
                }else{
                       reader = new BufferedReader(new InputStreamReader(getMethod.getResponseBodyAsStream()));
                       String buf = null;
                       StringBuffer xmlFull = new StringBuffer();
                       while((buf = reader.readLine())!= null){
                           xmlFull.append(buf);
                       }
    
                       respuestaBoss = xmlFull.toString();
                       reader.close();
            
                }     
            
            
            getMethod.releaseConnection();
            }else{
            	statusHttp = 200 ;
            	respuestaBoss="REINTENTO_ENVIO_MT";
            }
            
           
            if(statusHttp == 200 || statusHttp == 409 || statusHttp== 201 || statusHttp== 404 || statusHttp== 204)
            {
                log.info("OPERADOR[ar.movistar] URL Niji["+urlNiji+"] operacion["+map_gateway.get("operacion")+"]");
                
                urlNiji= urlNiji.replaceAll("&operacion=0", "&operacion=25");
                urlNiji= urlNiji.replaceAll("&operacion=3", "&operacion=26");
                msg.setHeader("urlMt",urlNiji);
                estadoBoss = "OK"; 
            }
            else
            { 
            	msg.setHeader("urlMt",urlNiji);
            	estadoBoss = "ERROR"; 
            }
            msg.setHeader("status_niji", estadoBoss);
            msg.setHeader("statusHttp",String.valueOf(statusHttp));
            msg.setHeader("reintento", String.valueOf(reintento));
            msg.setHeader("esReintento", "OK");
            log.info("OPERADOR[722200] URL Niji["+urlNiji+"] REINTENTO["+ reintento +"] RET["+ respuestaBoss +"] STATUSHTTP["+statusHttp+"]");
        } catch (Exception e)
        {
    msg.setHeader("status_niji", "EXCEPTION");
    log.info("OPERADOR[722200] URL Niji["+urlNiji+"] STATUSHTTP["+statusHttp+"] ERROR LLAMANDO A BOSS");
    e.printStackTrace();
        }
finally
        {
    if(reader != null)
        reader.close();
    if(getMethod != null)
        getMethod.releaseConnection();
        }
   
    }
    
    public Map<String, String> getQueryMap(String query)throws Exception
    {
            String[] params = query.split("&");
    Map<String, String> map = new HashMap<String, String>();
    String[] pair = null;
    for (String param : params){
            pair = param.split("=");
            if (pair.length > 1 && pair[1] != null)
                    { map.put(pair[0], pair[1]); }
            else
                    { map.put(pair[0], ""); }
            }
    return map;
    }
}