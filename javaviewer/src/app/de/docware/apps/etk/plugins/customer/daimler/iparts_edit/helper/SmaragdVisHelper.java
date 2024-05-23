/*
 * Copyright (c) 2019 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.HTMLUtils;
import de.docware.util.StrUtils;
import de.docware.util.java1_1.Java1_1_Utils;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.Charset;
import java.util.Set;

/**
 * Hilfsklasse für die Visualisierung in Smaragd
 * Dazu wird im Hintergrund eine URL aufgerufen. Bei Daimler ist für diese URL ein Protokoll Handler aktiv der
 * dann lokal auf dem PC Smaragd öffnet.
 */
public class SmaragdVisHelper {

    private static final String URL_PART = "part/";
    private static final String URL_PARAMS = "?doexpand=y&target=insert&visualize2d=n&background=n&visualize=y";

    /**
     * Pro Teilenummer im übergebenen Set wird eine URL gemäß den Einstellungen zusammengebaut und im Hintergrund
     * aufgerufen.
     *
     * @param partNumbersOrSAAs
     */
    public static void openSmaragdURLSilent(final Set<String> partNumbersOrSAAs) {
        if (partNumbersOrSAAs.isEmpty()) {
            return;
        }

        Session session = Session.get();
        session.startChildThread(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                int delay = iPartsEditPlugin.getPluginConfig().getConfigValueAsInteger(iPartsEditPlugin.CONFIG_SMARAGD_DELAY);
                String baseUrl = iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_SMARAGD_URI);
                baseUrl = baseUrl.trim();
                baseUrl = StrUtils.addCharacterIfLastCharacterIsNot(baseUrl, '/');
                for (String partNumberOrSAA : partNumbersOrSAAs) {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }

                    String url = baseUrl + createParameterString(partNumberOrSAA, true);
                    String rawUrl = baseUrl + createParameterString(partNumberOrSAA, false);
                    Logger.log(iPartsEditPlugin.LOG_CHANNEL_SMARAGD, LogType.DEBUG, "SMARAGD URL raw: " + rawUrl + " - encoded: " + url);

                    session.invokeThreadSafe(() -> HTMLUtils.openUrl(url, null, HttpConstants.METHOD_POST));
                    Java1_1_Utils.sleep(delay);
                }
            }
        });
    }

    private static String createParameterString(String partNumberOrSAA, boolean encode) {
        if (partNumberOrSAA.startsWith(iPartsConst.SAA_NUMBER_PREFIX)) { // Spezialbehandlungen für SAAs
            try {
                iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                partNumberOrSAA = numberHelper.unformatSaaForDB(partNumberOrSAA);

                // "Z *" -> "Z0*" und "ZW* -> "M_W*"
                if (partNumberOrSAA.startsWith(iPartsConst.SAA_NUMBER_PREFIX + " ")) {
                    partNumberOrSAA = iPartsConst.SAA_NUMBER_PREFIX + "0" + partNumberOrSAA.substring(2);
                } else if (partNumberOrSAA.startsWith(iPartsConst.SAA_NUMBER_PREFIX + "W")) {
                    partNumberOrSAA = "M_W" + partNumberOrSAA.substring(2);
                }
            } catch (Exception e) {
                // Ist doch keine korrekte SAA -> als Teilenummer behandeln
            }
        }

        String result = URL_PART + partNumberOrSAA + URL_PARAMS;

        if (encode) {
            return Base64.encodeBase64String(result.getBytes(Charset.forName("UTF-8")));
        } else {
            return result;
        }
    }

}
