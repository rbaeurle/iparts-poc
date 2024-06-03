package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects.iPartsWSgetVehicleDatacardResponse;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class VisService {

    private VisService() {
    }

    private static Map<String, String> finMap = null;

    public static void init() {
        finMap = new HashMap<>();

        try {
            GensonBuilder gensonBuilder = new GensonBuilder();
            Genson genson = gensonBuilder.create();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:de_docware_apps_etk_plugins_customer_daimler_iparts_webservice_iPartsWebservicePlugin_V1/DataCards/v2/vehicledatacards/*.json");
            for (Resource resource : resources) {
                try (InputStream inputStream = resource.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1))) {
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    String json = stringBuilder.toString();
                    iPartsWSgetVehicleDatacardResponse datacardResponse = genson.deserialize(json, iPartsWSgetVehicleDatacardResponse.class);
                    if (datacardResponse.getVehicleRestrictedInclMasterDataResponse() == null) {
                        continue;
                    }
                    final var fin = datacardResponse.getVehicleRestrictedInclMasterDataResponse().getVehicleInclMasterData().getFin();
                    finMap.put(fin, json);
                }
            }
        } catch (Exception e) {
            Logger.log(LogChannels.APPLICATION, LogType.ERROR, e.getMessage());
        }
    }


    static synchronized String getDataCard(String requestParameter) {
        if (finMap == null) {
            init();
        }

        return finMap.get(requestParameter);
    }

}
