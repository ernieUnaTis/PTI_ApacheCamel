/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.example.spring;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.Main;
import org.apache.camel.Processor;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.Message;
import org.apache.camel.Predicate;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.PredicateBuilder;

/**
 * A simple example router from a file system to an ActiveMQ queue and then to a file system
 *
 * @version
 */
public class MyRouteBuilder extends RouteBuilder {

    /**
     * Allow this route to be run as an application
     */
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    public void configure() {
	errorHandler(loggingErrorHandler("mylogger.name").level(LoggingLevel.INFO));

	/*************Predicados (Niji)************/
        Predicate plataformaNiji = header("PLATAFORMA").isEqualTo("NIJI");
        Predicate nijiAlta = header("UPDATETYPE").isEqualTo("0");
        Predicate nijiBaja = header("UPDATETYPE").isEqualTo("3");
        Predicate condNijiAlta = PredicateBuilder.and(plataformaNiji, nijiAlta);
        Predicate condNijiBaja = PredicateBuilder.and(plataformaNiji, nijiBaja);

        /*************Predicados (HardBundle)************/
        Predicate plataformaHB = header("PLATAFORMA").isEqualTo("HARDBUNDLE");
        Predicate plataformaHBCloud = header("PLATAFORMA").isEqualTo("CLOUD");

        /*************Predicados (ConsumerHub)************/
        /*************Predicados (ConsumerHub)************/
        Predicate plataformaCH = header("PLATAFORMA").isEqualTo("CONSUMERHUB");
        Predicate consumerHubBaja = header("UPDATETYPE").isEqualTo("3");
        Predicate productoCHDowngrade = header("PRODUCTO").isEqualTo("CLUBTERABOXUNLIMITED");
        Predicate productoCHDowngradeLegacy = header("PRODUCTO").isEqualTo("CLUBTERABOXUNLIMITEDLEGACY");
        Predicate productoCHBajaDown = PredicateBuilder.and(consumerHubBaja, productoCHDowngrade);
        Predicate productoCHBajaDownLegacy = PredicateBuilder.and(consumerHubBaja, productoCHDowngradeLegacy);


	/********************************************************************************/
        /********************************Movistar Argentina*************************************/
        /********************************************************************************/
        /***
        PARSEO DE DATASYNC
        ***/
        from("jms:ar.movistar.reciclaje?concurrentConsumers=1&maxMessagesPerTask=-1").to("jms:terra.datasync.parseo");
        from("jms:terra.datasync.parseo?concurrentConsumers=1&maxMessagesPerTask=-1").throttle(10).processRef("DataSyncParseoProcess").
        choice().
                when(condNijiBaja).to("jms:niji_integration.SendNijiBaja").
                when(plataformaNiji).to("jms:niji_integration.SendNiji").
                when(plataformaHB).to("jms:hb_integration.SendHB").
                when(plataformaHBCloud).to("jms:hb_integration.SendHB").
                when(plataformaHBCloud).to("jms:hb_integration.SendHB").
                //when(productoCHBajaDown).to("jms:consumerhub_integration.SendConsultaSegmentoAr").
                //when(productoCHBajaDownLegacy).to("jms:consumerhub_integration.SendConsultaSegmentoAr").
                when(plataformaCH).to("jms:consumerhub_integration.SendConsumerHub").
                otherwise().to("jms:terra.datasync.error");

        from("jms:terra.datasync.error").stop();

        /**
         * CONSUMER HUB


        from("jms:consumerhub_integration.SendConsultaSegmentoAr?concurrentConsumers=1&maxMessagesPerTask=-1").delay(3000).throttle(10).processRef("SendConsultaSegmentoArProcess").
        choice().
                when(header("segmento").isEqualTo("OK|CONTROL|INDIVIDUO|ACTIVO")).setHeader("producto",simple("CLUBTERABOX20GBBUNDLE")).setHeader("UPDATETYPE",simple("0")).to("jms:consumerhub_integration.SendConsumerHubCP").
                when(header("segmento").isEqualTo("OK|POSPAGO|INDIVIDUO|ACTIVO")).setHeader("producto",simple("CLUBTERABOX100GBBUNDLE")).setHeader("UPDATETYPE",simple("0")).to("jms:consumerhub_integration.SendConsumerHubCP").
                otherwise().to("jms:consumerhub_integration.SendConsumerHub");

       from("jms:consumerhub_integration.SendConsumerHubCP?concurrentConsumers=1&maxMessagesPerTask=-1").throttle(10).processRef("SendConsumerHubProcess").
        choice().
                        when(header("status_app").isEqualTo("OK")).to("jms:consumerhub_integration.SendConsumerHub.final.ok").
                when(header("status_app").isEqualTo("ERROR")).to("jms:consumerhub_integration.ReSendConsumerHub");

        from("jms:consumerhub_integration.SendConsumerHub?concurrentConsumers=1&maxMessagesPerTask=-1").throttle(10).processRef("SendConsumerHubProcess").
        choice().
                when(header("status_app").isEqualTo("OK")).to("jms:consumerhub_integration.SendMTConsumerHub").
                when(header("status_app").isEqualTo("ERROR")).to("jms:consumerhub_integration.ReSendConsumerHub");

        from("jms:consumerhub_integration.ReSendConsumerHub?concurrentConsumers=1&maxMessagesPerTask=-1").delay(3000).throttle(1).timePeriodMillis(100).processRef("ReSendConsumerHubProcess").
        choice().
                        when(header("status_app").isEqualTo("ERROR_FINAL")).to("jms:consumerhub_integration.SendConsumerHubError").
                        otherwise().to("jms:consumerhub_integration.SendConsumerHub");

        from("jms:consumerhub_integration.SendMTConsumerHub?concurrentConsumers=1&maxMessagesPerTask=-1").delay(15000).throttle(10).timePeriodMillis(100).processRef("SendMTConsumerHubProcess")
        .to("jms:consumerhub_integration.SendConsumerHub.final.ok");

        from("jms:consumerhub_integration.SendConsumerHubError?concurrentConsumers=1&maxMessagesPerTask=-1").delay(3000).throttle(10).timePeriodMillis(100)
        .processRef("SendErrorConsumerHubProcess").to("jms:consumerhub_integration.SendConsumerHub.final.error");

                from("jms:consumerhub_integration.SendConsumerHub.final.ok").stop();
                from("jms:consumerhub_integration.SendConsumerHub.final.error").stop();
        */
        /**
         * NIJI LATAM

        from("jms:ar.movistar.niji.queue?concurrentConsumers=1&maxMessagesPerTask=-1").to("jms:niji_integration.SendNiji");

        from("jms:niji_integration.SendNijiBaja?concurrentConsumers=1&maxMessagesPerTask=-1").delay(6000).to("jms:niji_integration.SendNiji");

        from("jms:niji_integration.SendNiji?concurrentConsumers=1&maxMessagesPerTask=-1").throttle(10).processRef("SendNijiProcess").
        choice().
                 when(header("status_niji").isEqualTo("ERROR")).to("jms:niji_integration.ReSendNiji").
                 when(header("status_niji").isEqualTo("OK|UPDATETYPE_NOAPLICA")).to("jms:niji_integration.Sendniji.final.ok").
                 when(header("status_niji").isEqualTo("OK|ROLLBACK")).to("jms:niji_integration.Sendniji.final.ok").
                 when(header("status_niji").isEqualTo("OK")).to("jms:niji_integration.SendMTNiji").
                 otherwise().to("jms:niji_integration.Sendniji.final.ok");

        from("jms:niji_integration.ReSendNiji?concurrentConsumers=1&maxMessagesPerTask=-1").delay(3000).throttle(1).timePeriodMillis(100).processRef("ReSendNijiProcess").
        choice().
                        when(header("status_niji").isEqualTo("ERROR_FINAL")).to("jms:niji_integration.SendNijiError").
                        otherwise().to("jms:niji_integration.SendNiji");

           from("jms:niji_integration.ReSendMTNiji?concurrentConsumers=1&maxMessagesPerTask=-1").delay(3000).throttle(1).timePeriodMillis(100).processRef("ReSendNijiProcess").
            choice().
                        when(header("status_niji").isEqualTo("ERROR_FINAL")).to("jms:niji_integration.SendNijiError").
                        otherwise().to("jms:niji_integration.SendMTNiji");

         from("jms:niji_integration.SendMTNiji?concurrentConsumers=1&maxMessagesPerTask=-1").delay(3000).throttle(10).timePeriodMillis(100).processRef("SendMTNijiProcess").
         choice().when(header("status_niji").isEqualTo("ERROR")).to("jms:niji_integration.ReSendMTNiji").
         otherwise().to("jms:niji_integration.Sendniji.final.ok");

         from("jms:niji_integration.SendNijiError?concurrentConsumers=1&maxMessagesPerTask=-1").delay(3000).throttle(10).timePeriodMillis(100)
                                                .processRef("SendErrorNijiProcess").to("jms:niji_integration.Sendniji.final.error");



        from("jms:niji_integration.Sendniji.final.ok").stop();
        from("jms:niji_integration.Sendniji.final.error").stop();
        */


        /**
         * Reintentos Niji


        from("jms:niji_integration.ReSendReintentoNiji?concurrentConsumers=1&maxMessagesPerTask=-1").delay(3000).throttle(1).timePeriodMillis(100).processRef("ReSendNijiProcess").
        choice().
                    when(header("status_niji").isEqualTo("ERROR_FINAL")).to("jms:niji_integration.SendNijiError").
                    otherwise().to("jms:niji_integration.ReintentoNiji");

        from("jms:niji_integration.ReintentoNiji?concurrentConsumers=1&maxMessagesPerTask=-1").throttle(10).processRef("ReintentoNijiProcess").
        choice().
        when(header("status_niji").isEqualTo("ERROR")).to("jms:niji_integration.ReSendReintentoNiji").
        when(header("status_niji").isEqualTo("OK")).to("jms:niji_integration.SendMTNiji").
        otherwise().to("jms:niji_integration.Sendniji.final.ok");

         */
        /********************************************************************************/
       /********************************Movistar Colombia (HB)*************************************/
       /******************************************************************************
               from("jms:hb_integration.SendHB?concurrentConsumers=1&maxMessagesPerTask=-1").throttle(10).processRef("SendHardBundleProcess").
               choice().
                when(header("status_hardbundle").isEqualTo("ERROR")).to("jms:hb_integration.ReSendHB").
                when(header("status_hardbundle").isEqualTo("OK|UPDATETYPE_NOAPLICA")).to("jms:hb_integration.SendHB.final.ok").
                when(header("status_hardbundle").isEqualTo("OK")).to("jms:hb_integration.SendHB.final.ok").
                otherwise().to("jms:hb_integration.SendHB.final.ok");

              from("jms:hb_integration.ReSendHB?concurrentConsumers=1&maxMessagesPerTask=-1").delay(3000).throttle(1).timePeriodMillis(100).processRef("ReSendHardBundleProcess").
              choice().
                       when(header("status_niji").isEqualTo("ERROR_FINAL")).to("jms:niji_integration.SendNijiError").
                       otherwise().to("jms:niji_integration.SendNiji");

       from("jms:hb_integration.SendHB.final.ok").stop();
       from("jms:hb_integration.SendHB.final.error").stop();
        **/


    }

    public static class SomeBean {

        public void someMethod(String body) {
            System.out.println("Received: " + body);
        }
    }

}
