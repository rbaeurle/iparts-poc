README zu Apache ActiveMQ Libs
-------------------------------
Diese JARs stammen aus dem lib-Verzeichnis der binary Distribution von Apache ActiveMQ 5.10.0.
Zum aktuellen Zeitpunkt ist das stabile Release 5.11.1. Dieses erfordert aber Java 7 zur Laufzeit.
F�r Java 6 wird ActiveMQ <= 1.10.0 ben�tigt.

F�r Test gegen einen externen Message Broker (Apache Active MQ separat gestartet) sind diese JARs ausreichend:
activemq-client-xxx.jar
geronimo-j2ee-management_1.1_spec-xxx.jar
geronimo-jms_1.1_spec-xxx.jar
slf4j-api-xxx.jar (ist bereits bei JV Standard-Libs dabei)

F�r Test mit einem embedded Message Broker ZUS�TZLICH diese JARs n�tig.
activemq-broker-xxx.jar
activemq-console-xxx.jar
activemq-kahadb-store-xxx.jar (evtl. nur f�r persistent mode)
activemq-openwire-legacy-xxx.jar
activemq-protobuf-xxx.jar
hawtbuf-xxx.jar

Das lib-Verzeichnis der Distribution enth�lt noch weitere JARs, die bisher nicht ben�tigt werden.
Die Doku empfiehlt zur Vereinfachung die Verwendung von activemq-all.jar, die alle JARs beinhaltet (im Root-Verzeichnis der Distribution).
Da die slf4j-api-xxx.jar aber bereits in den JV Standard-Libs enthalten ist, habe ich das nicht gemacht.
