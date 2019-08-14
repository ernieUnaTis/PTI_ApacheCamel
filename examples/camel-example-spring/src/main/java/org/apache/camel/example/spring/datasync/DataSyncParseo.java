package org.apache.camel.example.spring.datasync;


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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.util.List;

import java.util.Hashtable;
import java.io.File;
import org.apache.camel.example.spring.datasync.*;

public class DataSyncParseo implements Processor {
        private Logger log = Logger.getLogger(this.getClass().getSimpleName());
        
        public void process(Exchange exchange) throws Exception {
                String mensaje = null;
                String responseDataSync = null;
                 Message msg = exchange.getIn();
                try{
                        mensaje = exchange.getIn().getBody(String.class);
                        responseDataSync = new StringBuffer(mensaje).toString();
                        Hashtable hashParam = null;
                        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();  
                        DocumentBuilder builder = domFactory.newDocumentBuilder();
                        org.w3c.dom.Document doc = null;
                        
                        
                        
                        if (responseDataSync.indexOf("SyncSubscriptionData") != -1){
                            doc = builder.parse(new ByteArrayInputStream(responseDataSync.getBytes("UTF-8")));
                            if (responseDataSync.indexOf("Fault")!=-1 || responseDataSync.indexOf("fault")!=-1){
                                //Si se desea agregar un parametro, se hace aqui
                                String faultcode = valorNodo("faultcode", doc, "//*/");
                                String faultstring = valorNodo("faultstring", doc, "//*/");
                                                                    //String faultstring = nodes1.item(0).getChildNodes().item(0).getNodeValue() ;
                            } else if (responseDataSync.indexOf("updateType")!=-1) {
                                String updateType = valorNodo("updateType", doc, "//*/SyncSubscriptionData/");
                                String serviceId = valorNodo("serviceID", doc, "//*/SyncSubscriptionData/");
                                String productId = valorNodo("productID", doc, "//*/SyncSubscriptionData/");
                                String msisdn = valorNodo("MSISDN", doc, "//*/SyncSubscriptionData/");
                                String spid = valorNodo("SPID", doc, "//*/SyncSubscriptionData/");
                                String transId = valorNodo("transactionID", doc, "//*/SyncSubscriptionData/");
                                hashParam = Utils.obtenerInfoDataSync(serviceId, productId);
                                msg.setHeader("PLATAFORMA", (String)hashParam.get("plataforma"));
                                msg.setHeader("CARRIER", (String)hashParam.get("operador"));
                                msg.setHeader("PRODUCTO", (String)hashParam.get("producto"));
                                msg.setHeader("MSISDN", msisdn);
                                msg.setHeader("UPDATETYPE", updateType);
                                msg.setHeader("SERVICEID", serviceId);
                                msg.setHeader("PRODUCTID", productId);
                                msg.setHeader("TRANSID", transId);
                                log.info("PLATAFORMA["+(String)hashParam.get("plataforma")+"] CARRIER["+(String)hashParam.get("operador")+"] PRODUCTO["+(String)hashParam.get("producto")+"] MSISDN["+msisdn+"] UPDATETYPE["+updateType+"] SERVICEID["+serviceId+"]PRODUCTID["+productId+"]SPID["+spid+"]");
                                             
                             } else {
                                log.info("Utils - [processSDPResponseBaja] [BAD_SOAP_RSP:" +responseDataSync.toString()+"] ");
                             }
                           
                       }  
                        
                        
                        
                        
                        
                if (responseDataSync.indexOf("syncSubscriptionData") != -1){
                    doc = builder.parse(new ByteArrayInputStream(responseDataSync.getBytes("UTF-8")));
                    
                    
                    if (responseDataSync.indexOf("Fault")!=-1 || responseDataSync.indexOf("fault")!=-1){
                                //Si se desea agregar un parametro, se hace aqui
                                String faultcode = valorNodo("faultcode", doc, "//*/");
                                String faultstring = valorNodo("faultstring", doc, "//*/");
                                //String faultstring = nodes1.item(0).getChildNodes().item(0).getNodeValue() ;
                     } else if (responseDataSync.indexOf("updateType")!=-1) {
                                String updateType = valorNodo("updateType", doc, "//*/syncSubscriptionData/");
                                String serviceId = valorNodo("serviceId", doc, "//*/syncSubscriptionData/");
                                String productId = valorNodo("productId", doc, "//*/syncSubscriptionData/");
                                String msisdn = valorNodo("MSISDN", doc, "//*/syncSubscriptionData/");
                                hashParam = Utils.obtenerInfoDataSync(serviceId, productId);
                                msg.setHeader("PLATAFORMA", (String)hashParam.get("plataforma"));
                                msg.setHeader("CARRIER", (String)hashParam.get("operador"));
                                msg.setHeader("PRODUCTO", (String)hashParam.get("producto"));
                                msg.setHeader("MSISDN", msisdn);
                                msg.setHeader("UPDATETYPE", updateType);
                                
                                log.info("PLATAFORMA["+(String)hashParam.get("plataforma")+"] CARRIER["+(String)hashParam.get("operador")+"] PRODUCTO["+(String)hashParam.get("producto")+"] MSISDN["+msisdn+"] UPDATETYPE["+updateType+"]");
                     
                     } else {
                                log.info("Utils - [processSDPResponseBaja] [BAD_SOAP_RSP:" +responseDataSync.toString()+"] ");
                    }
                }
                        
                }
                catch (Exception e)
            {
        msg.setHeader("status_boss", "EXCEPTION");
        e.printStackTrace();
            }
        finally
            {

            }
        }
        
        public static String valorNodo(String sNodo, Document doc, String xpathexpr) throws XPathExpressionException{

        XPathFactory fac = XPathFactory.newInstance();
        XPath xpath = fac.newXPath();
        String sXPath = xpathexpr+sNodo;  
        XPathExpression exprId = xpath.compile(sXPath);
        Object ress = exprId.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) ress;
        String retorno = null;
        if(nodes.item(0) != null) retorno = nodes.item(0).getChildNodes().item(0).getNodeValue() ;
        return retorno;
    }
    

        
}