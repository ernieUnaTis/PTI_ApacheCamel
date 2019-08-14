package org.apache.camel.example.spring.consumerhub;


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


public class SendMTConsumerHub implements Processor {
    private Logger log = Logger.getLogger(this.getClass().getSimpleName());
    
            public SendMTConsumerHub(){
        }
    
    public void process(Exchange exchange) throws Exception {
        HttpClient      httpClient = null;
        HttpMethodBase  getMethod = null;
        int             statusHttp = 0;
        String respuestaBoss = null;
        Message msg = exchange.getIn();
        BufferedReader reader = null;
        String mensaje = null;
  
        String estadoBoss = "NOK";
        String replyevent = null;
        String partner = null;
        String trx_id = null;
        String urlNiji     = "http://"+ org.apache.camel.example.spring.Conf.HUB_IP +"/SecurityHub/ValidarSuscripcion";
        
       
        String operador = (String)msg.getHeader("CARRIER");
        String producto = (String)msg.getHeader("PRODUCTO");
        String msisdn = (String)msg.getHeader("MSISDN");
        String updateType = (String)msg.getHeader("UPDATETYPE");
        
        urlNiji = urlNiji + "?msisdn="+msisdn+"&operacion="+updateType+"&producto="+producto+"&operador=722200&origen=STORE";
       String urlMt = "";
        int reintento = 0;
        if (msg.getHeader("reintento") != null)
        { reintento=Integer.parseInt((String)msg.getHeader("reintento"))+1; }
    
        
        try{

        MultiThreadedHttpConnectionManager      multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setDefaultMaxConnectionsPerHost(50);
        params.setMaxTotalConnections(50);
        multiThreadedHttpConnectionManager.setParams(params);

        httpClient = new HttpClient(multiThreadedHttpConnectionManager);
        getMethod = new GetMethod(urlNiji);
        //getMethod.setQueryString(urlParams);
        statusHttp = httpClient.executeMethod(getMethod);
        reader = new BufferedReader(new InputStreamReader(getMethod.getResponseBodyAsStream()));
        String buf = null;
        StringBuffer xmlFull = new StringBuffer();
        while((buf = reader.readLine())!= null){
            xmlFull.append(buf);
            }

        respuestaBoss = xmlFull.toString();
        getMethod.releaseConnection();
        reader.close();
        if(statusHttp == 200)
            {
            if(respuestaBoss.equalsIgnoreCase("OK") || respuestaBoss.equalsIgnoreCase("NOK") || respuestaBoss.equalsIgnoreCase("OK|ROLLBACK"))
                    { estadoBoss = "OK"; }
            else
                    { estadoBoss = "ERROR"; }
            }
        else
            { 
      
            estadoBoss = "ERROR"; 
            }

        msg.setHeader("status_app", estadoBoss);
        msg.setHeader("statusHttp",String.valueOf(statusHttp));
        msg.setHeader("reintento", String.valueOf(reintento));
        log.info("OPERADOR["+operador+"] URL Niji["+urlNiji+"] REINTENTO["+ reintento +"] RET["+ respuestaBoss +"] STATUSHTTP["+statusHttp+"]");
            }
    catch (Exception e)
            {
        msg.setHeader("status_boss", "EXCEPTION");
        log.info("OPERADOR["+operador+"] URL Niji["+urlNiji+"]ERROR LLAMANDO A BOSS");
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
    

        
        
}
