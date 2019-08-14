package org.apache.camel.example.spring;


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


public class ConsultaSegmentoAr implements Processor {
    
    private Logger log = Logger.getLogger(this.getClass().getSimpleName());
    
    public void process(Exchange exchange) throws Exception {
        String respuestaBoss = null;
        Message msg = exchange.getIn();
        BufferedReader reader = null;
        
           HttpClient      httpClient = null;
        HttpMethodBase  getMethod = null;
        int             statusHttp = 0;

        String mensaje = null;
        boolean notificarExitoReintento = false;
        String estadoBoss = "NOK";
        String replyevent = null;
        String partner = null;
        String trx_id = null;
        
        String operador = (String)msg.getHeader("CARRIER");
        String producto = (String)msg.getHeader("PRODUCTO");
        String msisdn = (String)msg.getHeader("MSISDN");
        String updateType = (String)msg.getHeader("UPDATETYPE");
        
        //String urlConsulta     = "http://mobile-seg-mia.tpn.terra.com:8080/SegmentoArMovistar/SegmentoMovAr?msisdn";
        String urlConsulta     = "http://10.253.126.111:8085/SegmentoArMovistar/SegmentoMovAr?msisdn";
        
        String urlParams ="msisdn="+msisdn;
        int reintento = 0;
        if (msg.getHeader("reintento") != null)
        { reintento=Integer.parseInt((String)msg.getHeader("reintento"))+1; }
        
        try{

            
            MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
            HttpConnectionManagerParams params = new HttpConnectionManagerParams();
            params.setDefaultMaxConnectionsPerHost(50);
            params.setMaxTotalConnections(50);
            multiThreadedHttpConnectionManager.setParams(params);
    
            httpClient = new HttpClient(multiThreadedHttpConnectionManager);
            getMethod = new GetMethod(urlConsulta);
            getMethod.setQueryString(urlParams);
            statusHttp = httpClient.executeMethod(getMethod);
            
            if(statusHttp== 200){
                reader = new BufferedReader(new InputStreamReader(getMethod.getResponseBodyAsStream()));
                String buf = null;
                StringBuffer xmlFull = new StringBuffer();
                while((buf = reader.readLine())!= null){
                    xmlFull.append(buf);
                    }
        
                respuestaBoss = xmlFull.toString();
                reader.close();
            }
            
            msg.setHeader("segmento", respuestaBoss);
            //msg.setHeader("statusHttp",String.valueOf(statusHttp));
            //msg.setHeader("reintento", String.valueOf(reintento));
            log.info("OPERADOR["+operador+"] RET["+ respuestaBoss +"] STATUSHTTP["+statusHttp+"]");
            
        }
        catch (Exception e)
        {
            msg.setHeader("status_app", "EXCEPTION");
            log.info("OPERADOR["+operador+"] URL Niji["+urlConsulta+urlParams+"] STATUSHTTP["+statusHttp+"] ERROR LLAMANDO A BOSS");
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