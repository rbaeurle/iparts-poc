/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated.DaiEngBomDbService;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated.DaiEngBomDbServiceService;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated.Language;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;
import de.docware.util.security.PasswordString;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.SOAPFaultException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Helper für die Eco...Services
 */
public class BomDBServiceHelper {

    private static final de.docware.framework.modules.config.common.Language DEFAULT_LANGUAGE =
            de.docware.framework.modules.config.common.Language.EN;


    public static String getBomDbUserName() {
        // Benutzername
        return iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_BOMDB_WEBSERVICE_USERNAME);
    }

    public static PasswordString getBomDbPassword() {
        // Passwort
        return iPartsEditPlugin.getPluginConfig().getConfigValueAsPassword(iPartsEditPlugin.CONFIG_BOMDB_WEBSERVICE_PASSWORD);
    }

    public static String getBomDbEndPointUri() {
        // Endpunkt-URI
        return iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_BOMDB_WEBSERVICE_ENDPOINT_URI);
    }

    public static String getBomDbAppToken() {
        // Application Token (AppToken)
        return iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_BOMDB_WEBSERVICE_APPTOKEN);
    }

    public static DaiEngBomDbService createAndInitBomDBService(BomDBDefaultServiceHandler serviceHandler) throws SOAPFaultException {
        // Endpunkt-URI
        String endpointUri = BomDBServiceHelper.getBomDbEndPointUri();
        // Erstellung einer Service Instanz
        // Explizite Aktivierung von WS-Addressing (Erläuterung siehe Confluence Artikel: https://confluence.docware.de/x/1oR0Bg
        DaiEngBomDbService bomDbService = new DaiEngBomDbServiceService().getDaiEngBomDbServicePort(new AddressingFeature(true, true));

        // Mittels BindingProvider lassen sich die Default-Parameter aus dem generierten Service anpassen, wie z.B. die
        // Endpunkt-URI oder Benutzername und Passwort.
        BindingProvider bp = (BindingProvider)bomDbService;

        // Setzen der Endpunkt-URI
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUri);

        // Erweiterung des SOAP Headers um obligatorisches (mandatory) Security-Tag
        Binding binding = bp.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();

        // Übergabe von Security-Parametern an Handler
        handlerChain.add(serviceHandler);
        binding.setHandlerChain(handlerChain);

        return bomDbService;
    }

    /**
     * Standard-Sprache: ENGLISCH; Wenn die GUI-Sprache Deutsch ist, soll die Abfrage-Sprache DEUTSCH sein.
     * Es gibt hier leider keine schönere Möglichkeit, die Abfrage-Sprache zu setzen, da man die "iParts"-ISO-Sprache
     * auf das generierte Sprachenobjekt mappen muss.
     *
     * @param language
     * @return
     */
    public static Language getEcoLanguage(String language) {
        de.docware.framework.modules.config.common.Language iPartsLanguage =
                de.docware.framework.modules.config.common.Language.findLanguage(language, DEFAULT_LANGUAGE.getCode());
        switch (iPartsLanguage) {
            case DE:
                return Language.GERMAN;
            case EN:
                return Language.ENGLISH;
            case ES:
                return Language.SPANISH;
            case PT:
                return Language.PORTUGUESE;
            case FR:
                return Language.FRENCH;
            case TR:
                return Language.TURKISH;
            case IT:
                return Language.ITALIAN;
            default:
                return Language.ENGLISH;
        }
    }

    /**
     * Konvertiert einen DB-DateTime-String nach XMLGregorianCalendar
     *
     * @param dbDateTime
     * @return
     */
    public static XMLGregorianCalendar convertDateTime(String dbDateTime) {
        XMLGregorianCalendar txnTimeXMLCalendar = null;
        try {
            Date txnTimeDate = DateUtils.toDate(dbDateTime, DateUtils.simpleTimeFormatyyyyMMddHHmmss);
            GregorianCalendar txnTimeCalendar = new GregorianCalendar();
            txnTimeCalendar.setTime(txnTimeDate);
            txnTimeXMLCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(txnTimeCalendar);
        } catch (DatatypeConfigurationException | DateException e) {
            Logger.logExceptionWithoutThrowing(iPartsEditPlugin.LOG_CHANNEL_BOM_DB_SOAP_WEBSERVICE, LogType.ERROR, e);
        }
        return txnTimeXMLCalendar;
    }


    /**
     * Konvertiert ein XMLGregorianCalendar-Datum nach DB-DateTime-String
     *
     * @param xmlDate
     * @return
     */
    public static String convertXMLCalendarToDbString(XMLGregorianCalendar xmlDate) {
        GregorianCalendar txnTimeCalendar = xmlDate.toGregorianCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.simpleTimeFormatyyyyMMddHHmmss);
        return sdf.format(txnTimeCalendar.getTime());
    }

}
