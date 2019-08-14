package org.apache.camel.example.spring.niji;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class ReSendNiji implements Processor {
        private Logger log = Logger.getLogger(this.getClass().getSimpleName());
        
        private int maxReintento = 2;
        
        public ReSendNiji(){}

        public void process(Exchange exchange) throws Exception {
                String body = exchange.getIn().getBody(String.class);
        Message msg = exchange.getIn();
        int reintento=-1;

        try
                {
                String action = (String) msg.getHeader("action");
            if (msg.getHeader("reintento")!=null)
                        {
                    reintento=Integer.parseInt((String)msg.getHeader("reintento"));
                    
                    if (reintento >= maxReintento)
                        { msg.setHeader("status_niji", "ERROR_FINAL"); }
                        }
                        
                        log.error("[STATUS_BOSS:"+(String)msg.getHeader("status_niji")+"] [REINTENTO:" + reintento + "] [maxReintento:" + maxReintento + "] ");
                }
        catch(Exception e)
                {
                log.error("[STATUS_BOSS:"+(String)msg.getHeader("status_niji")+"] [REINTENTO:" + reintento + "] [RESPUESTA:"+e.toString()+"]");
                e.printStackTrace();
                }
        }
        }