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


public class SendNiji implements Processor {
    private Logger log = Logger.getLogger(this.getClass().getSimpleName());
    
    public void process(Exchange exchange) throws Exception {
        HttpClient      httpClient = null;
        HttpMethodBase  getMethod = null;
        int             statusHttp = 0;

        
        String respuestaBoss = null;
        Message msg = exchange.getIn();
        BufferedReader reader = null;

        String mensaje = null;
        boolean notificarExitoReintento = false;
        String estadoBoss = "NOK";
        String replyevent = null;
        String partner = null;
        String trx_id = null;
        String urlNiji     = "http://"+ org.apache.camel.example.spring.Conf.NIJI_IP +"/IntegracionNiji/Niji";
        String urlNijiBaja = "http://"+ org.apache.camel.example.spring.Conf.NIJI_ERROR_IP +"/Niji/nijiAlmacenamiento.php";
        String operador = (String)msg.getHeader("CARRIER");
        String producto = (String)msg.getHeader("PRODUCTO");
        String msisdn = (String)msg.getHeader("MSISDN");
        String updateType = (String)msg.getHeader("UPDATETYPE");
        
        if (msg.getHeader("esReintento") != null)
        {notificarExitoReintento = true;  }
        
        if(updateType.equalsIgnoreCase("0") || updateType.equalsIgnoreCase("3"))
        {
                String urlParams ="msisdn="+msisdn+"&operacion="+updateType+"&producto="+producto+"&operador=722200&origen=STORE";
                int reintento = 0;
                if (msg.getHeader("reintento") != null)
                { reintento=Integer.parseInt((String)msg.getHeader("reintento"))+1; }
                
                try{
                
                  if(reintento == 0){
                      callDeleteFrontRetry(urlNijiBaja, urlParams);
                  }
                    
                    
                    
                    
                MultiThreadedHttpConnectionManager      multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
                HttpConnectionManagerParams params = new HttpConnectionManagerParams();
                params.setDefaultMaxConnectionsPerHost(50);
                params.setMaxTotalConnections(50);
                multiThreadedHttpConnectionManager.setParams(params);
        
                httpClient = new HttpClient(multiThreadedHttpConnectionManager);
                getMethod = new GetMethod(urlNiji);
                getMethod.setQueryString(urlParams);
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
                
        
                if(statusHttp == 200 || statusHttp == 409 || statusHttp== 201 || statusHttp== 404 || statusHttp== 204)
                    {
                    if(notificarExitoReintento) {
                        callDeleteFrontRetry(urlNijiBaja, urlParams);
                    }
                    
                    if(respuestaBoss.equalsIgnoreCase("OK|ROLLBACK")){
                        estadoBoss = respuestaBoss; 
                    }else if(respuestaBoss.equalsIgnoreCase("OK|NO_SUSCRITO")){
                        estadoBoss = respuestaBoss; 
                    }
                    else{
                        estadoBoss = "OK"; 
                    }
                    
                    
                   }
                else
                    { 
                    estadoBoss = "ERROR"; 
                    }
        
                msg.setHeader("status_niji", estadoBoss);
                msg.setHeader("statusHttp",String.valueOf(statusHttp));
                msg.setHeader("reintento", String.valueOf(reintento));
                log.info("OPERADOR["+operador+"] URL Niji["+urlNiji+"] REINTENTO["+ reintento +"] RET["+ respuestaBoss +"] STATUSHTTP["+statusHttp+"]");
         
            }
    catch (Exception e)
            {
        msg.setHeader("status_niji", "EXCEPTION");
        log.info("OPERADOR["+operador+"] URL Niji["+urlNiji+urlParams+"] STATUSHTTP["+statusHttp+"] ERROR LLAMANDO A BOSS");
        e.printStackTrace();
            }
    finally
            {
        if(reader != null)
            reader.close();
        if(getMethod != null)
            getMethod.releaseConnection();
            }
        }else {
            msg.setHeader("status_niji", "OK|UPDATETYPE_NOAPLICA");
            msg.setHeader("statusHttp","200");
            msg.setHeader("reintento", "0");
            log.info("OPERADOR["+operador+"] URL Niji["+urlNiji+"] REINTENTO[0] RET[OK|UPDATETYPE_NOAPLICA] UPDATETYPE["+updateType+"]");
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
    
    public void callDeleteFrontRetry(String urlNijiBaja, String urlParams) throws Exception {
        HttpClient      httpClientBaja = null;
        HttpMethodBase  getMethodBaja = null;
        int             statusHttpBaja = 0;
        BufferedReader readerBaja = null;
        urlNijiBaja = urlNijiBaja + "?" + urlParams +"&bajaSDP=1&status=200";
        MultiThreadedHttpConnectionManager  multiThreadedHttpConnectionManagerBaja = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams paramsBaja = new HttpConnectionManagerParams();
        paramsBaja.setDefaultMaxConnectionsPerHost(50);
        paramsBaja.setMaxTotalConnections(50);
        multiThreadedHttpConnectionManagerBaja.setParams(paramsBaja);

        httpClientBaja = new HttpClient(multiThreadedHttpConnectionManagerBaja);
        getMethodBaja = new GetMethod(urlNijiBaja);
        statusHttpBaja = httpClientBaja.executeMethod(getMethodBaja);

      readerBaja = new BufferedReader(new InputStreamReader(getMethodBaja.getResponseBodyAsStream()));
      String bufBaja = null;
      StringBuffer xmlFullBaja = new StringBuffer();
      while((bufBaja = readerBaja.readLine())!= null){
          xmlFullBaja.append(bufBaja);
      }

      String respuestaBaja = xmlFullBaja.toString();
      getMethodBaja.releaseConnection();
      readerBaja.close();
      log.info("URL Baja ["+urlNijiBaja+"] RET["+ respuestaBaja +"] STATUSHTTP["+statusHttpBaja+"]");
    }
        
        
}
