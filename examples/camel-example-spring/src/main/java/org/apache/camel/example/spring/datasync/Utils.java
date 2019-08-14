
package org.apache.camel.example.spring.datasync;

import java.io.File;
import java.io.IOException;
import org.jdom.Element;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import java.util.List;
import java.util.Hashtable;
import java.io.File;

public class Utils  {



    public static Hashtable obtenerInfoDataSync(String serviceId, String productId){
        Hashtable retorno = new Hashtable();
        try{
        SAXBuilder builder = new SAXBuilder();
                String file = "/usr/local/bd/politicas_datasync.xml";
                File xmlFile = new File(file);
        
                String poda_id = null;
                String operador = null;
                String service_id = null;
                String product_id = null;
                String keywordalta = null;
                String keywordbaja = null;
                String nc = null;
                String plataforma = null;
                String producto = null;
                String precio = null;
                String susc_id = null;
                String service_id_navegar = null;
                String pfree = null;
                String poda_cacre_id = null;
                String poda_fecha = null;
                Document document = (Document) builder.build(xmlFile); //Se crea el documento a traves del archivo
                                Element rootNode = document.getRootElement(); //Se obtiene la raiz 'Tabla_Parametros'
                                
                                //System.out.println("rootNode: "+rootNode);
                                // System.out.println("leyo el archivo");
                                
                                List list = rootNode.getChildren( "tabla" ); //Se obtiene la lista de hijos de la raiz 'tabla'
                                for ( int i = 0; i < list.size(); i++ ) //Se recorre la lista de hijos de 'parametro'
                                {
                                        Element tabla = (Element) list.get(i); //Se obtiene el elemento 'parametro'

                                        List lista_campos = tabla.getChildren(); //Se obtiene la lista de hijos del tag 'parametro'
                                        for ( int j = 0; j < lista_campos.size(); j++ ) //Se recorre la lista de campos
                                        {
                                                Element campo = (Element)lista_campos.get(j);  
                                                service_id = campo.getChildTextTrim("service_id"); //Se obtiene el valor que esta entre los tags '<service_id></service_id>'
                                                product_id = campo.getChildTextTrim("product_id"); //Se obtiene el valor que esta entre los tags '<product_id></product_id>'
                                                if (service_id.toUpperCase().equalsIgnoreCase(serviceId.toUpperCase()) && product_id.toUpperCase().equalsIgnoreCase(productId.toUpperCase())) {
                                                        retorno.put("poda_id", campo.getChildTextTrim("poda_id"));
                                                        retorno.put("operador", campo.getChildTextTrim("operador"));
                                                        retorno.put("keywordalta", campo.getChildTextTrim("keywordalta"));
                                                        retorno.put("keywordbaja", campo.getChildTextTrim("keywordbaja"));
                                                        retorno.put("nc", campo.getChildTextTrim("nc"));
                                                        retorno.put("plataforma", campo.getChildTextTrim("plataforma"));
                                                        retorno.put("producto", campo.getChildTextTrim("producto"));
                                                        retorno.put("precio", campo.getChildTextTrim("precio"));
                                                        retorno.put("susc_id", campo.getChildTextTrim("susc_id"));
                                                        retorno.put("service_id_navegar", campo.getChildTextTrim("service_id_navegar"));
                                                        retorno.put("pfree", campo.getChildTextTrim("pfree"));
                                                        retorno.put("poda_cacre_id", campo.getChildTextTrim("poda_cacre_id"));
                                                        retorno.put("poda_fecha", campo.getChildTextTrim("poda_fecha"));
                                                        
                                                        return retorno;
                                                }
                                                        
                                        }
                                }
                }
                catch ( IOException io ) { //logger.info("io.getMessage(): " + io.getMessage() ); 
                                System.out.println( io.getMessage() ); }
                        catch ( JDOMException jdomex ) { //logger.info("jdomex.getMessage(): " + jdomex.getMessage() ); 
                                System.out.println( jdomex.getMessage() ); 
                        }       
                return retorno;         
    }
}