/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.mediaservice.partnumbers;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.mediaservice.MediaServiceWebserviceUtils;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.webservice.restful.CallWebserviceException;
import de.docware.framework.utils.JSONUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;

import java.io.IOException;
import java.util.Set;

/**
 * Service-Klasse für den Aufruf von Teilenummern mit Einzelteilbildern vom Mediaservice
 */
public class MediaServicePartNumbersService {

    public static final String WEBSERVICE_NAME = "partnumbers";
    private static Genson genson = JSONUtils.createGenson(true);

    /**
     * Teilenummern vom xentryAPI Mediaservice laden und als String Liste zurückgeben.
     *
     * @param project
     * @return {@code null} falls der Aufruf nicht erfolgreich war
     */
    public static Set<String> getPartNumbersFromMediaService(final EtkProject project) throws CallWebserviceException {
        // Die BasisURL ist gleichzeitig die Webservice URL
        String mediaServicePartNumbersUrl = iPartsPlugin.getWebservicesSinglePicPartsBaseURI();
        if (!mediaServicePartNumbersUrl.endsWith('/' + WEBSERVICE_NAME)) {
            mediaServicePartNumbersUrl += '/' + WEBSERVICE_NAME;
        }

        String partNumbersJson = null;

        // Simulation vom Webservice partnumbers
        if (mediaServicePartNumbersUrl.equals(iPartsConst.WEBSERVICE_URI_DATACARDS_SIM_BASE + '/' + WEBSERVICE_NAME)) {
            DWFile simulationFile = iPartsPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsPlugin.CONFIG_SQL_PERFORMANCE_TESTS_DIR).getChild("partnumbers_response.json");
            if (simulationFile.isFile()) {
                try {
                    String responseString = simulationFile.readTextFile(DWFileCoding.UTF8);
                    partNumbersJson = "{\"items\":" + responseString + "}";
                } catch (IOException e) {
                    throw new CallWebserviceException("Error reading " + WEBSERVICE_NAME + " web service simulation file "
                                                      + simulationFile.getAbsolutePath(), e);
                }
            }
        }

        if (partNumbersJson == null) {
            partNumbersJson = MediaServiceWebserviceUtils.getJsonFromPartNumbersWebservice(mediaServicePartNumbersUrl, WEBSERVICE_NAME,
                                                                                           project.getDBLanguage());
        }
        if (partNumbersJson != null) {
            MediaServicePartNumbersResponse partNumbersResponse = genson.deserialize(partNumbersJson, MediaServicePartNumbersResponse.class);
            if ((partNumbersResponse == null) || (partNumbersResponse.getItems() == null)) {
                throw new CallWebserviceException(TranslationHandler.translate("!!Der JSON-String ist semantisch ungültig: %2",
                                                                               TranslationHandler.translate("!!Deserialisierung nicht möglich")));
            }

            return partNumbersResponse.getItems();
        }

        return null;
    }
}