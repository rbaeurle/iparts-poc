/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.VinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.j2ee.EC;

import java.util.List;

/**
 * Automatische Filterung durch URL für Support
 */
public class SupportURLInterpreter {

    // Die Schlüsselwörter für das URL-Format Version [1]
    private static String PARAMETER_DELIMITER_V1 = "?";
    private static String SEARCH_ROOT_PATTERN_V1 = "ROOT=";
    private static String SCOPE_DELIMITER_V1 = ":";
    private static String NAVPART_PREFIX_V1 = "NAV=";
    private static String NAVPART_AGG_PREFIX_V1 = "AGG=";
    private static String AREA_DELIMITER_V1 = ",";
    private static String NAVPART_AS_PREFIX_V1 = "AS=";
    private static String NAVPART_CAT_PREFIX_V1 = "CAT=";
    private static String KGTU_DELIMITER_V1 = ",";

    // Die Schlüsselwörter für das URL-Format Version [2]
    private static String PARAMETER_DELIMITER_V2 = ";";
    private static String SCOPE_DELIMITER_V2 = ";";
    private static String SEARCH_ROOT_PATTERN_V2 = "bm=";       // bm=WDDWJ8DB1LF952949 || bm=205205 || bm=906991C0991501
    private static String CAT_PREFIX_V2 = "cat=";               // cat=C205_FC
    private static String NAVPART_PREFIX_V2 = "nav=";           // nav=88,075
    private static String NAVPART_AGG_PREFIX_V2 = "agg=";       // agg=D651921
    private static String NAVPART_AGGCAT_PREFIX_V2 = "aggcat="; // aggcat=69L

    public static boolean isKgValid(String kgValue) {
        if (StrUtils.isEmpty(kgValue)) {
            return true;
        }
        return (kgValue.length() == 2) && StrUtils.isDigit(kgValue);
    }

    public static boolean isTuValid(String tuValue) {
        if (StrUtils.isEmpty(tuValue)) {
            return true;
        }
        return (tuValue.length() == 3) && StrUtils.isDigit(tuValue);
    }

    public static boolean isTuSaaValid(String tuSaaValue) {
        boolean result = isTuValid(tuSaaValue);
        if (!result) {
            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
            result = numberHelper.isValidSa(numberHelper.unformatSaSaaForDB(tuSaaValue, false), true);
        }
        return result;
    }

    private enum ERRORS {
        PARSE_ERROR("!!Fehler beim Parsen: %1"),
        KEYWORD_MISSING("!!Schlüsselwort \"%1\" fehlt."),
        KEYWORDS_MISSING("!!Schlüsselwörter \"%1\" oder \"%2\" fehlen."),
        MISSING_FIN("!!Keine FIN/VIN/BM (\"%1\") angegeben."),
        MISSING_AGG_BM("!!Kein Aggregate-Baumuster gefunden oder fehlende Schlüsselwörter (\"%1\",\"%2\")"),
        WRONG_NAVPART("!!NAV-Part falsch aufgebaut"),
        KEYWORD_IS_DOUBLE("!!Doppeltes Schlüsselwort \"%1\".");

        private String errKey;

        ERRORS(String errKey) {
            this.errKey = errKey;
        }

        public String getErrKey() {
            return errKey;
        }
    }

    private String realURL;
    private boolean isValid;
    private boolean aggIsSet;
    private boolean navIsSet;
    private String finOrVinOrBm;
    private String kg;
    private String tu;
    private String as;
    private String product;
    private String aggBM;
    private String aggAs;
    private String aggProduct;
    private List<String> parseErrors = new DwList<>();

    public SupportURLInterpreter() {
        clear();
    }

    public SupportURLInterpreter(String url) {
        setRealURL(url);
    }

    private void clear() {
        realURL = "";
        isValid = false;
        aggIsSet = false;
        navIsSet = false;
        finOrVinOrBm = "";
        kg = "";
        tu = "";
        as = "";
        product = "";
        aggBM = "";
        aggAs = "";
        aggProduct = "";
        parseErrors.clear();
    }

    public String getRealURL() {
        return realURL;
    }

    public void setRealURL(String realURL) {
        clear();
        try {
            if (checkAndDecodeUrl(realURL)) {
                analyzeURL();
            }
        } catch (Exception e) {
            addError(ERRORS.PARSE_ERROR, "\n" + e.getMessage());
        }
    }

    private boolean checkAndDecodeUrl(String url) {
        boolean result = true;
        if (StrUtils.isValid(url)) {
            if (isUrlInVersion1Format(url)) {
                this.realURL = checkAndDecodeUrlVersion1(url);
            } else if (isUrlInVersion2Format(url)) {
                this.realURL = checkAndDecodeUrlVersion2(url);
            } else {
                // Da passt überhaupt nichts ==> Fehlermeldung und raus!
                addError(ERRORS.KEYWORDS_MISSING, SEARCH_ROOT_PATTERN_V1, SEARCH_ROOT_PATTERN_V2);
                result = false;
            }
        }
        return result;
    }

    private String checkAndDecodeUrlVersion1(String url) {
        return checkAndDecodeUrl(url, PARAMETER_DELIMITER_V1);
    }

    private String checkAndDecodeUrlVersion2(String url) {
        return checkAndDecodeUrl(url, PARAMETER_DELIMITER_V2);
    }

    /**
     * Entfernt erst falsche Leerzeichen aus der URL und decodiert dann den URL-Path.
     * URL-Decoding: Umwandlung von z.B.: "%20" in: " "
     * (V1):
     * - Der Eingabefehler (Leerzeichen in der URL) wird behoben,
     * - das Leerzeichen in Namen der freien SA bleibt.
     * "https: //retail.aftersales.i.daimler.com/wps/myportal/functions/partsinquiry-xsf/start?iparts.deep=ROOT=WDB96763610103076:NAV=42,Z M03.930"
     * <p>
     * (V2):
     * - Dieses Leerzeichen wird umgewandelt
     * |  da (%20)!
     * "https://xentry.daimler.com/xhpi/start;bm=C963406;cat=S01;nav=47,Z%20M02.580;img=drawing_SM0258000001"
     *
     * @param url
     * @param delimiter
     * @return
     */
    private String checkAndDecodeUrl(String url, String delimiter) {
        StringBuilder sb = new StringBuilder();
        if (url != null) {
            // Leerzeichen(Eingabefehler) im String ersetzen.
            String firstPart;
            String secondPart = "";
            int idx = url.indexOf(delimiter);
            // Der Aufruf V2 enthält keinen Trenner zwischen der URL und den Parametern.
            if (idx < 0) {
                firstPart = url;
            } else {
                firstPart = url.substring(0, idx);
                secondPart = url.substring(idx + 1, url.length());
            }

            // Im URL-Teil erst Leerzeichen entfernen und dann z.B.: "%20" decodieren.
            sb.append(EC.decodePath(firstPart.replaceAll(" ", ""), false));
            // Falls es mindestens 2 Teile gab, den/die Rest(e) wieder anhängen.
            // Die Parameter wieder anhängen und darin die encoded-Werte (z.B.: "%20") ebenfalls durch Leerzeichen ersetzen.
            if (secondPart.length() > 1) {
                sb.append(delimiter);
                sb.append(Utils.decodeAndConvertStringFromUTF8(secondPart));
            }
        }
        return sb.toString();
    }

    public boolean isValid() {
        return isValid;
    }

    public List<String> getParseErrors() {
        return parseErrors;
    }

    public boolean isAggSet() {
        return aggIsSet;
    }

    public boolean isNavSet() {
        return navIsSet;
    }

    public FinId getFinId() {
        return new FinId(getFinOrVinOrBm());
    }

    public VinId getVinId() {
        return new VinId(getFinOrVinOrBm());
    }

    public iPartsModelId getModelId(EtkProject project) {
        if (isModelIdValid()) {
            String modelNo = getFinOrVinOrBm();
            if (!iPartsModel.isVehicleModel(modelNo) && !iPartsModel.isAggregateModel(modelNo)) {
                iPartsDataModelList models = iPartsDataModelList.loadForModelNumberWithoutPrefix(project, modelNo);
                if (!models.isEmpty()) {
                    return models.get(0).getAsId();
                }
                return new iPartsModelId();
            }
            return new iPartsModelId(modelNo);
        } else {
            if (!getFinOrVinOrBm().isEmpty()) {
                if (getFinId().isModelNumberValid()) {
                    return new iPartsModelId(getFinId().getFullModelNumber());
                } else {
                    if (!getVinId().isValidId()) {
                        String modelNo = StrUtils.copySubString(getFinOrVinOrBm(), 3, 6);
                        if (!iPartsModel.isVehicleModel(modelNo) && !iPartsModel.isAggregateModel(modelNo)) {
                            modelNo = iPartsConst.MODEL_NUMBER_PREFIX_CAR + modelNo;
                        }
                        return new iPartsModelId(modelNo);
                    }
                }
            }
        }
        return new iPartsModelId("");
    }

    public boolean isFinOrVinIdValid() {
        return isFinIdValid() || getVinId().isValidId();
    }

    public boolean isFinIdValid() {
        return getFinId().isValidId();
    }

    public boolean isModelIdValid() {
        return iPartsModel.isModelNumberValid(getFinOrVinOrBm(), true);
    }

    public String getFinOrVinOrBm() {
        return finOrVinOrBm;
    }

    public String getKg() {
        return kg;
    }

    public String getTu() {
        return tu;
    }

    public String getAs() {
        return as;
    }

    public String getProduct() {
        return product;
    }

    public String getAggBM() {
        return aggBM;
    }

    public String getAggAs() {
        return aggAs;
    }

    public String getAggProduct() {
        return aggProduct;
    }

    /**
     * Unterscheidung in die (aktuell) zwei verschiedenen URL-Formate.
     */
    private void analyzeURL() {
        if (isUrlInVersion1Format(realURL)) {
            analyzeUrlVersion1();
        } else if (isUrlInVersion2Format(realURL)) {
            analyzeUrlVersion2();
        }
    }

    private boolean isUrlInVersion1Format(String url) {
        return url.contains(SEARCH_ROOT_PATTERN_V1);
    }

    private boolean isUrlInVersion2Format(String url) {
        return url.contains(SEARCH_ROOT_PATTERN_V2);
    }

    /**
     * Zerlegt die übergebenen URLs im NEUEN Format:
     * bm= und nav=
     * <p>
     * Fin/Vin:
     * https://xentry.daimler.com/xhpi/support/start;bm=WDDWJ8DB1LF952949;cat=C205_FC;nav=88,075;img=drawing_PV000.002.920.057_version_002
     * Baumuster:
     * https://xentry.daimler.com/xhpi/support/start;bm=205205;cat=C205_FS;agg=D651921;aggcat=69L;nav=07,013;img=drawing_B07013000032
     * Fin mit freier SA
     * https://xentry.daimler.com/xhpi/start;bm=C963406;cat=S01;nav=47,Z%20M02.580;img=drawing_SM0258000001
     * Nochmal Fin/Vin (Aggregateidentnummer für eine Motordatenkarte)
     * https://xentry.daimler.com/xhpi/start;bm=906991C0991501;cat=06V;nav=13,060;img=drawing_B13060000089
     */
    private void analyzeUrlVersion2() {
        // Erst die einzelnen Werte aus der URL holen ...
        String[] splittedString = realURL.split(SCOPE_DELIMITER_V2);
        String tmpStr;
        boolean bmKeyWordExists = false;
        for (String oneToken : splittedString) {
            if (oneToken.startsWith(SEARCH_ROOT_PATTERN_V2)) {
                bmKeyWordExists = true;
                // bm=WDDWJ8DB1LF952949 || bm=205205 || bm=C963406 || bm=906991C0991501
                finOrVinOrBm = oneToken.replace(SEARCH_ROOT_PATTERN_V2, "");
            } else if (oneToken.startsWith(CAT_PREFIX_V2)) {
                product = oneToken.replace(CAT_PREFIX_V2, "");
            } else if (oneToken.startsWith(NAVPART_PREFIX_V2)) {
                // nav=88,075
                tmpStr = oneToken.replace(NAVPART_PREFIX_V2, "");
                List<String> splitList = StrUtils.toStringList(tmpStr, KGTU_DELIMITER_V1, true);
                // Bei fehlenden Wert(en) in "nav" einen Fehler ausgeben.
                if (splitList.size() < 2) {
                    addError(ERRORS.WRONG_NAVPART);
                } else {
                    kg = splitList.get(0);
                    tu = splitList.get(1); // TU oder auch freie SA
                    navIsSet = true;
                }
            } else if (oneToken.startsWith(NAVPART_AGG_PREFIX_V2)) {
                aggBM = oneToken.replace(NAVPART_AGG_PREFIX_V2, "");
            } else if (oneToken.startsWith(NAVPART_AGGCAT_PREFIX_V2)) {
                aggProduct = oneToken.replace(NAVPART_AGGCAT_PREFIX_V2, "");
                aggIsSet = true;
            } else {
                // Mach' erschd a'mal nix!
            }
        }

        // Ein paar Checks, ob man mit den eingelesenen Daten etwas anfangen kann:
        // bm (=finOrVinOrBm) + nav (=kg+tu) muss immer da sein
        if (!StrUtils.isValid(finOrVinOrBm)) {
            if (!bmKeyWordExists) {
                addError(ERRORS.KEYWORD_MISSING, SEARCH_ROOT_PATTERN_V2);
            } else {
                addError(ERRORS.MISSING_FIN, "");
            }
        }
        isValid = (getErrorCount() < 1);
    }


    /**
     * Zerlegt die übergebenen URLs im ALTEN Format:
     * <p>
     * https://retail.aftersales.i.daimler.com/wps/myportal/functions/partsinquiry-xsf/start?iparts.deep=ROOT=WDC2539091F134418:NAV=41,015
     * https://retail.aftersales.i.daimler.com/wps/myportal/functions/partsinquiry-xsf/start?iparts.deep=ROOT=WDB2102261B011012,AS=P,CAT=65X:AGG=D613961,AS=PKW,CAT=65C:NAV=20,015
     * https://retail.aftersales.i.daimler.com/wps/myportal/functions/partsinquiry-xsf/start?iparts.deep=ROOT=259909:NAV=41,015
     * https://retail.aftersales.i.daimler.com/wps/myportal/functions/partsinquiry-xsf/start?iparts.deep=ROOT=WDB96763610103076:NAV=42,Z M03.930
     * https://retail.aftersales.i.daimler.com/wps/myportal/functions/partsinquiry-xsf/start?iparts.deep=ROOT=WD4PG2EE3G3138415:NAV=41,015     // VIN
     * https://retail.aftersales.i.daimler.com/wps/myportal/functions/partsinquiry-xsf/start?iparts.deep=ROOT=205140,AS=P,CAT=66E
     */
    private void analyzeUrlVersion1() {

        int index = realURL.indexOf(SEARCH_ROOT_PATTERN_V1);
        if (index != -1) {
            String searchStr = StrUtils.trimLeft(realURL.substring(index + SEARCH_ROOT_PATTERN_V1.length()));
            String finVinBM = "";
            String navPart = "";
            String navPartAgg = "";
            int state = 0;
            while (state < 99) {
                switch (state) {
                    case 0: // finVinBm, navPartAgg und navPath trennen
                        List<String> splitList = StrUtils.toStringList(searchStr, SCOPE_DELIMITER_V1, true);
                        if (splitList.size() < 1) { // nav ist optional
                            addError(ERRORS.KEYWORDS_MISSING, NAVPART_PREFIX_V1, NAVPART_AGG_PREFIX_V1);
                            state = 100;
                        } else {
                            finVinBM = splitList.get(0);
                            if (splitList.size() >= 3) {
                                navPartAgg = splitList.get(1);
                                navPart = splitList.get(2);
                            } else if (splitList.size() > 1) {
                                navPart = splitList.get(1);
                            }
                            state = 1;
                        }
                        break;
                    case 1:  // finVinBm aufschlüsseln
                        splitList = StrUtils.toStringList(finVinBM, AREA_DELIMITER_V1, true);
                        boolean found = false;
                        for (String str : splitList) {
                            if (str.startsWith(NAVPART_AS_PREFIX_V1)) {
                                as = str.substring(NAVPART_AS_PREFIX_V1.length());
                                found = true;
                            } else if (str.startsWith(NAVPART_CAT_PREFIX_V1)) {
                                product = str.substring(NAVPART_CAT_PREFIX_V1.length());
                                found = true;
                            } else {
                                if (finOrVinOrBm.isEmpty()) {
                                    finOrVinOrBm = str;
                                    found = true;
                                }
                            }
                        }
                        if (!found) {
                            addError(ERRORS.MISSING_FIN, finVinBM);
                            state = 100;
                            break;
                        }
                        if (!navPartAgg.isEmpty()) {
                            state = 2;
                        } else {
                            if (navPart.isEmpty()) {
                                navIsSet = false;
                                isValid = true;
                                state = 100;
                            } else {
                                state = 3;
                            }
                        }
                        break;
                    case 2: // navPart AGG aufschlüsseln
                        if (!navPartAgg.startsWith(NAVPART_AGG_PREFIX_V1)) {
                            addError(ERRORS.KEYWORD_MISSING, NAVPART_AGG_PREFIX_V1);
                            state = 100;
                            break;
                        }
                        aggIsSet = true;
                        navPartAgg = navPartAgg.substring(NAVPART_AGG_PREFIX_V1.length());
                        splitList = StrUtils.toStringList(navPartAgg, AREA_DELIMITER_V1, true);
                        found = false;
                        for (String str : splitList) {
                            if (str.startsWith(NAVPART_CAT_PREFIX_V1)) {
                                aggProduct = str.substring(NAVPART_CAT_PREFIX_V1.length());
                                found = true;
                            } else if (str.startsWith(NAVPART_AS_PREFIX_V1)) {
                                aggAs = str.substring(NAVPART_AS_PREFIX_V1.length());
                                found = true;
                            } else if (iPartsModel.isVehicleModel(str) || iPartsModel.isAggregateModel(str)) {
                                aggBM = str;
                                found = true;
                            }
                        }
                        if (!found) {
                            addError(ERRORS.MISSING_AGG_BM, NAVPART_CAT_PREFIX_V1, NAVPART_AS_PREFIX_V1);
                            state = 100;
                            break;
                        }
                        if (!navPart.isEmpty()) {
                            state = 3;
                        }
                        break;
                    case 3:  // navPart aufschlüsseln
                        if (!navPart.startsWith(NAVPART_PREFIX_V1)) {
                            addError(ERRORS.KEYWORD_MISSING, NAVPART_PREFIX_V1);
                            state = 100;
                            break;
                        }
                        navPart = navPart.substring(NAVPART_PREFIX_V1.length());
                        splitList = StrUtils.toStringList(navPart, KGTU_DELIMITER_V1, true);
                        if (splitList.size() < 2) {
                            addError(ERRORS.WRONG_NAVPART);
                            state = 100;
                        } else {
                            kg = splitList.get(0);
                            tu = splitList.get(1);
                            isValid = true;
                            navIsSet = true;
                            state = 100;
                        }
                        break;
                    case 100:
                        break;
                }
            }
        } else {
            addError(ERRORS.KEYWORD_MISSING, SEARCH_ROOT_PATTERN_V1);
        }
    }

    private void addError(ERRORS error, String... placeHolderTexts) {
        if (error != null) {
            parseErrors.add(TranslationHandler.translate(error.getErrKey(), placeHolderTexts));
        } else {
            parseErrors.add(TranslationHandler.translate("!!Unbekannter Fehler"));
        }
    }

    private int getErrorCount() {
        if (parseErrors != null) {
            return parseErrors.size();
        }
        return 0;
    }
}
