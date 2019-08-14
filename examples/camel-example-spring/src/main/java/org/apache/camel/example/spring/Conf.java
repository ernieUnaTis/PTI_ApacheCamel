package org.apache.camel.example.spring;

public class Conf {
    
        /**URL NIJI  DESARROLLO**/
        //public static final String NIJI_IP = "10.253.126.111:8085";
        //public static final String NIJI_ERROR_IP = "10.253.126.111:8081";
        
        /**URL NIJI  PRODUCCION**/
        public static final String NIJI_IP = "mobile-aprov-mia.tpn.terra.com:8080";
        public static final String NIJI_ERROR_IP = "app.moviles.tpn.terra.com";
        
        /**URL HUB  DESARROLLO**/
        //public static final String HUB_IP = "10.253.126.111:8085";
        //public static final String HUB_ERROR_IP = "10.253.126.111:8081";
        
        /**URL HUB  PRODUCCION**/
        public static final String HUB_IP = "mobile-app01-mia.tpn.terra.com:8080";
        public static final String HUB_ERROR_IP = "app.moviles.tpn.terra.com";

	/* Parametros Globales */
	//desarrollo
	//static final String ROUTER_IP = "10.253.126.112" ;
	//produccion
	static final String ROUTER_IP = "sms-router.tpn.terra.com" ;
	static final int SENDMT_MAXCONNECTIONS_PERHOST = 30;
	static final int SENDMT_MAXTOTAL_CONNECTIONS = 30;

	//desarrollo
	//static final String TS_IP = "mobile-dev-smsst-mia.tpn.terra.com";
	//produccion
	static final String TS_IP = "smsst-be.terra.com";
        static final int ALTATS_MAXCONNECTIONS_PERHOST = 30;
        static final int ALTATS_MAXTOTAL_CONNECTIONS = 30;

	static final String TT_IP = "mcm.teletouch.com.mx";
        static final int ALTATT_MAXCONNECTIONS_PERHOST = 30;
        static final int ALTATT_MAXTOTAL_CONNECTIONS = 30;


        //static final String BILLING_IP = "10.253.126.120:8082" ;
        static final String BILLING_IP = "bundle-br.tpn.terra.com:8080";
        
        static final String BILLING_IP_LATAM = "bundle-br.tpn.terra.com:8080";

	/* Parametros de configuracion Movistar Mexico */
	static final int THROTTLE_MT_MOVISTAR_MEXICO = 25;
	static final int CONSUMERS_MT_MOVISTAR_MEXICO = 1;
        static final int THROTTLE_TS_MOVISTAR_MEXICO = 10;
        static final int CONSUMERS_TS_MOVISTAR_MEXICO = 1;
        static final int THROTTLE_TT_MOVISTAR_MEXICO = 8;
        static final int CONSUMERS_TT_MOVISTAR_MEXICO = 1;
	static final int DELAY_MT_RETRY_MOVISTAR_MEXICO = 3000; //delay para el reintento en msegundos
	static final int THROTTLE_MT_RETRY_MOVISTAR_MEXICO = 1;
	static final int CONSUMERS_MT_RETRY_MOVISTAR_MEXICO = 1;
}
