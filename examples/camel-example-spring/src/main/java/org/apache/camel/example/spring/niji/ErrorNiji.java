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


public class ErrorNiji implements Processor {
    private Logger log = Logger.getLogger(this.getClass().getSimpleName());
    
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
        String urlMt = null;
        String urlNijiError = "http://"+ org.apache.camel.example.spring.Conf.NIJI_ERROR_IP +"/Niji/nijiAlmacenamiento.php";
       
                String operador = (String)msg.getHeader("CARRIER");
        String producto = (String)msg.getHeader("PRODUCTO");
        String msisdn = (String)msg.getHeader("MSISDN");
        String updateType = (String)msg.getHeader("UPDATETYPE");
        String status = (String) msg.getHeader("statusHttp");
          String urlParams ="msisdn="+msisdn+"&operacion="+updateType+"&producto="+producto+"&operador=722200&origen=STORE";
      boolean notificarErrorReintento = false;
          
        int reintento = 0;
        if (msg.getHeader("reintento") != null)
        { reintento=Integer.parseInt((String)msg.getHeader("reintento"))+1; }
        
        if (msg.getHeader("esReintento") != null)
        {notificarErrorReintento = true;  }
    
        	 if (msg.getHeader("urlMt") != null)
        {urlMt = (String)msg.getHeader("urlMt"); }
        
        try{
        	if(urlMt!="" && urlMt!=null){
        		urlMt = urlMt.replace("http://"+ org.apache.camel.example.spring.Conf.NIJI_IP +"/IntegracionNiji/Niji","http://"+ org.apache.camel.example.spring.Conf.NIJI_ERROR_IP +"/Niji/nijiAlmacenamiento.php");
               urlNijiError = urlMt+ "&status="+status;
               operador = "ar.movistar";
            }else{
            	 urlNijiError = urlNijiError + "?"+ urlParams + "&status="+status;
            }
            
            if(notificarErrorReintento) {
                urlNijiError = urlNijiError + "&reintento=1";
            }
       
        MultiThreadedHttpConnectionManager      multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setDefaultMaxConnectionsPerHost(50);
        params.setMaxTotalConnections(50);
        multiThreadedHttpConnectionManager.setParams(params);

        httpClient = new HttpClient(multiThreadedHttpConnectionManager);
        getMethod = new GetMethod(urlNijiError);
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
            if(respuestaBoss.equalsIgnoreCase("OK"))
                    { estadoBoss = "OK"; }
            else
                    { estadoBoss = "ERROR"; }
            }
        else
            { estadoBoss = "ERROR"; }

        msg.setHeader("status_niji", estadoBoss);
        
        msg.setHeader("reintento", String.valueOf(reintento));
        log.info("OPERADOR["+operador+"] URL Niji["+urlNijiError+"] REINTENTO["+ reintento +"] RET["+ respuestaBoss +"] STATUSHTTP["+statusHttp+"]");
            }
    catch (Exception e)
            {
        msg.setHeader("status_boss", "EXCEPTION");
        log.info("OPERADOR["+operador+"] URL Niji["+urlNijiError+"]ERROR LLAMANDO A BOSS");
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
