/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models;

import com.owlike.genson.Genson;
import com.owlike.genson.JsonBindingException;
import com.owlike.genson.stream.JsonStreamException;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.iPartsProValWebserviceUtils;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.webservice.restful.CallWebserviceException;
import de.docware.framework.utils.JSONUtils;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;

import java.io.IOException;
import java.util.List;

/**
 * ProVal Models Service
 */
public class iPartsProValModelsService {

    public static final String MODEL_WEBSERVICE_NAME = "models";
    private static final Genson genson = JSONUtils.createGenson(true);

    public static List<iPartsProValModelsServiceResponseObject> getSalesTitlesFromProValModelsWebservice(String lang, final EtkProject etkProject) throws CallWebserviceException {
        // Die Basis-URL ist gleichzeitig die Webservice URL - es wird aber zwingend ein Sprach-Parameter Attribut benötigt
        // Fallback auf DE
        if (!StrUtils.isValid(lang)) {
            lang = "de";
        } else {
            lang = lang.toLowerCase();
        }
        String proValModelsUrl = getBaseProValURL();
        String webserviceEndpoint = MODEL_WEBSERVICE_NAME + "?lang=" + lang;
        proValModelsUrl += webserviceEndpoint;
        Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.DEBUG, "Calling ProVal Webservice with URL: " + proValModelsUrl);

        String jsonData = retrieveResponseFromSimulationOrWebService(proValModelsUrl, webserviceEndpoint, "model_" + lang + ".json", lang);

        if (jsonData != null) {
            iPartsProValModelsServiceResponse proValModelsServiceResponse;
            try {
                proValModelsServiceResponse = genson.deserialize(jsonData, iPartsProValModelsServiceResponse.class);
            } catch (JsonStreamException | JsonBindingException e) {
                throw new CallWebserviceException(TranslationHandler.translate("!!Der JSON-String ist semantisch ungültig: %2",
                                                                               TranslationHandler.translate("!!Deserialisierung nicht möglich")));
            }
            return proValModelsServiceResponse.getModels();
        }
        return null;
    }

    private static String getBaseProValURL() {
        String proValModelsUrl = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_PROVAL_BASE_URI);
        proValModelsUrl = StrUtils.removeLastCharacterIfCharacterIs(proValModelsUrl, '/' + MODEL_WEBSERVICE_NAME);
        proValModelsUrl = StrUtils.addCharacterIfLastCharacterIsNot(proValModelsUrl, '/');
        return proValModelsUrl;
    }

    public static String getModelAggDataFromProValModelsWebservice(EtkProject etkProject) throws CallWebserviceException {

        String webserviceEndpoint = MODEL_WEBSERVICE_NAME + "?includeDesignNumbers=true";
        String proValModelsUrl = getBaseProValURL() + webserviceEndpoint;
        Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.DEBUG, "Calling ProVal Webservice with URL: "
                                                                   + proValModelsUrl);

        return retrieveResponseFromSimulationOrWebService(proValModelsUrl, webserviceEndpoint, "modelAggs" + ".json",
                                                          etkProject.getDBLanguage());
    }

    private static String retrieveResponseFromSimulationOrWebService(String proValModelsUrl, String webserviceEndpoint, String simFileName, String language) throws CallWebserviceException {
        if (proValModelsUrl.equals(iPartsConst.WEBSERVICE_URI_DATACARDS_SIM_BASE + '/' + webserviceEndpoint)) {
            DWFile simulationFile = iPartsPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsPlugin.CONFIG_SQL_PERFORMANCE_TESTS_DIR).getChild(simFileName);
            if (simulationFile.isFile()) {
                try {
                    return simulationFile.readTextFile(DWFileCoding.UTF8);
                } catch (IOException e) {
                    throw new CallWebserviceException("Error reading " + MODEL_WEBSERVICE_NAME + " web service simulation file "
                                                      + simulationFile.getAbsolutePath(), e);
                }
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.DEBUG, "ProVal simulation JSON file not found: " + simFileName);
                return null;
            }
        } else {
            return iPartsProValWebserviceUtils.getJsonFromWebservice(proValModelsUrl, MODEL_WEBSERVICE_NAME, language);
        }
    }
}
