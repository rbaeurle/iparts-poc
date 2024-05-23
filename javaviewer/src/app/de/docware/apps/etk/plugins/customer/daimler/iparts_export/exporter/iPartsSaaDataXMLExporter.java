/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.common.EtkDataObjectArrayList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSAModules;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSAModulesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSA;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.iPartsExportPlugin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFileCoding;
import de.docware.util.file.DWWriter;
import de.docware.util.sql.TableAndFieldName;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.*;

/**
 * Exporter für die SA - SAA Benennungen samt KGs
 */
public class iPartsSaaDataXMLExporter extends AbstractXMLExporter implements iPartsXMLSaaDataExporterTags {

    private static final String EXPORT_NAME = "SAA_KG_DESCRIPTION";
    private static final String FAKE_XML_ELEMENT = "XML";
    private static final int SAA_LENGTH = 8;

    public iPartsSaaDataXMLExporter(EtkProject project) {
        super(project, EXPORT_NAME);
    }

    /**
     * Startet den Exporter mit Fortschrittsanzeige
     */
    public void exportWithMessageLogForm() {
        startExportWithoutChangeSets(() -> {
            // 1. Log-Datei setzen
            setLogFile();
            // 2. MessageLogForm erzeugen
            EtkMessageLogForm messageLogForm = initMessageLogForm("!!Saa Daten Export", getExportTitle());
            messageLogForm.showModal(new FrameworkRunnable() {
                @Override
                public void run(FrameworkThread thread) {
                    // 3. Export-Datei anlegen
                    createExportFile();
                    // 4. Daten exportieren
                    exportXMLData();
                    // 5. Export zum Downloaden anbieten
                    if (isRunning()) {
                        // Letzte Informationen ins Log schreiben
                        finishExportAndLogFile();
                        downloadExportFile();
                    }
                }
            });
            // 6. Temp-Verzeichnis löschen
            deleteExportFileDirectory();
        });
    }

    /**
     * Startet den Exporter ohne Fortschrittsanzeige
     */
    public void exportWithoutMessageLogForm() {
        if (iPartsExportPlugin.isSaaDataExportActive()) {
            startExportWithoutChangeSets(() -> {
                // 1. Log-Datei setzen
                setLogFile();
                // 2. Export-Datei anlegen
                createExportFile();
                // 3. Daten exportieren
                exportXMLData();
                // 4. Export-datei in das eingestellte Verzeichnis verschieben
                moveExportFileToExportDirectory(iPartsExportPlugin.getDirForSaaDataExport());
                // 5. Exporter und Log schließen
                if (isRunning()) {
                    // Letzte Informationen ins Log schreiben
                    finishExportAndLogFile();
                }
                // 6. Temp-Verzeichnis löschen
                deleteExportFileDirectory();
            });
        }
    }

    @Override
    protected String getExportTitle() {
        return translateForLog("!!Exportiere SA - SAA Benennungen inkl. KGs");
    }

    /**
     * Exportiert die SAA Daten in die Export-Datei
     */
    private void exportXMLData() {
        String currentSa = null;
        String currentSaa = null;
        // Ein BufferedWriter zum Schreiben der Strings in eine Datei
        try (DWWriter fileWriter = getExportFile().getWriter(DWFileCoding.UTF8, true)) {
            fireMessageWithTimeStamp(translateForLog("!!Lade alle SAs aus der Datenbank..."));
            // Alle SAs laden
            iPartsDataSaList saList = iPartsDataSaList.loadAllSAs(getProject());  // Helper die KGs zu den SAAs zu bestimmen
            int maxSaCount = saList.size();
            fireMessageWithTimeStamp(translateForLog("!!%1 SAs aus der Datenbank geladen", String.valueOf(maxSaCount)));
            // XML Writer initialisieren und das Fake.Element "<XML>" schreiben
            initWriterForStringDocument();
            Map<String, String> attributes = new LinkedHashMap<>();
            int saCount = 0;
            int saaCount = 0;
            boolean writeLineSeparator = false;
            fireMessageWithTimeStamp("!!Exportiere SA - SAA Benennungen inkl. KGs...");
            // Alle SAs durchlaufen
            for (iPartsDataSa saObject : saList) {
                if (!isRunning()) {
                    break;
                }
                currentSa = saObject.getAsId().getSaNumber();
                saCount++;

                // Die KGs für die SA bestimmen via DA_PRODUCT_SAS
                Set<String> kgsForSa = getKGsForSa(currentSa);

                // Die Module für die SA (falls es eine freie SA ist und es auch mindestens eine KG gibt)
                Set<String> saModulesSet = new HashSet<>();
                if (!kgsForSa.isEmpty()) {
                    iPartsDataSAModulesList saModulesList = iPartsDataSAModulesList.loadDataForSA(getProject(), new iPartsSAId(currentSa));
                    for (iPartsDataSAModules dataSAModule : saModulesList) {
                        saModulesSet.add(dataSAModule.getFieldValue(FIELD_DSM_MODULE_NO));
                    }
                }

                // Alle SAAs zur SA laden
                iPartsDataSaaList saaList = iPartsDataSaaList.loadAllSaasForSa(getProject(), currentSa);
                // Die SA Benennungen, die später benötigt wird
                EtkMultiSprache saTextObject = saObject.getFieldValueAsMultiLanguage(FIELD_DS_DESC);
                // Die Texte sollen in alle in iParts vorhandenen Sprachen ausgegeben werden
                Set<String> allSaTextLanguages = new HashSet<>(saTextObject.getSprachen());
                // Alle SAAs durchlaufen
                for (iPartsDataSaa saaObject : saaList) {
                    saaCount++;
                    if (!isRunning()) {
                        break;
                    }
                    attributes.clear();
                    currentSaa = saaObject.getAsId().getSaaNumber();
                    // KGs zu den SAAs bestimmen (via SAA-Gültigkeit in TUs von Produkten bzw. freien SAs)
                    Set<String> kgsForSaa = getKGsForSaa(currentSaa, saModulesSet, kgsForSa);
                    // Sind keine KGs vorhanden, dann taucht diese SAA in AS nicht auf. Solche SAAs sollen nicht ausgegeben
                    // werden
                    if (kgsForSaa.isEmpty()) {
                        continue;
                    }
                    // Neue Zeile pro SAA
                    if (writeLineSeparator) {
                        getXmlWriter().writeCharacters("\n");
                    } else {
                        writeLineSeparator = true;
                    }
                    // SAA in das gewünschte Ausgabeformat formatieren
                    String formattedSaa = formatSaa(currentSaa);
                    // Nun die XML Elemente erzeugen und befüllen
                    attributes.put(ATTRIBUTE_ID, formattedSaa);
                    // Da SAAs ohne KGs nicht exportiert werden, kann hier - im Moment -  nur "false" stehen
                    attributes.put(ATTRIBUTE_NO_DESIGN_GROUP_ASSIGNMENT, kgsForSaa.isEmpty() ? "true" : "false");
                    startElement(ELEMENT_SAA_DESIGNATION, attributes);
                    for (String kg : kgsForSaa) {
                        writeEnclosedElement(ELEMENT_DESIGN_GROUP, kg, null, true);
                    }
                    // SA - SAA Benennungen schreiben
                    writeSaSaaDescription(saaObject, saTextObject, allSaTextLanguages);
                    endElement(ELEMENT_SAA_DESIGNATION);
                }
                checkExportCurrentData(fileWriter, saCount, saaCount);
                fireProgress(saCount, maxSaCount, "", true, true);
            }
            writeDataBlockToFile(fileWriter);
        } catch (Exception e) {
            Logger.getLogger().handleRuntimeException(e);
            stopExport(translateForLog("!!Fehler beim Schreiben der Export-Datei \"" + getExportFile().getName()
                                       + "\" für die SA \"%1\" und SAA \"%2\"", currentSa, currentSaa));
        } finally {
            try {
                closeXMLWriter();
                closeStringWriter();
            } catch (Exception e) {
                Logger.getLogger().handleRuntimeException(e);
            }
        }
    }

    /**
     * Prüft, ob die bestehenden XML Daten in die Datei geschrieben werden sollen
     *
     * @param fileWriter
     * @param saCount
     * @param saaCount
     * @throws IOException
     * @throws XMLStreamException
     */
    private void checkExportCurrentData(DWWriter fileWriter, int saCount, int saaCount) throws IOException, XMLStreamException {
        // Ab 100 SAs wird der Text in die Datei geschrieben
        if ((saCount % 100) == 0) {
            writeDataBlockToFile(fileWriter);
            if ((saCount % 1000) == 0) {
                fireMessageWithTimeStamp(translateForLog("!!%1 SAs und %2 SAAs exportiert.", String.valueOf(saCount), String.valueOf(saaCount)));
            }
            initWriterForStringDocument();
        }
    }

    /**
     * Schreibt die SA - SAA Benennung in die XML Informationen
     *
     * @param saaObject
     * @param saTextObject
     * @param allSaTextLanguages
     * @throws XMLStreamException
     */
    private void writeSaSaaDescription(iPartsDataSaa saaObject, EtkMultiSprache saTextObject, Set<String> allSaTextLanguages) throws XMLStreamException {
        // Die SA - SAA Benennung bestimmen
        boolean hasText = false;
        // SAA Benennung bestimmen
        EtkMultiSprache saaTextObject = saaObject.getFieldValueAsMultiLanguage(FIELD_DS_DESC);
        // Die Sprachen der SA Benennunge mit den Sprachen der SAA Benennug zusammenlegen, damit wir alle
        // verfügbaren Sprachen haben
        allSaTextLanguages.addAll(saaTextObject.getSprachen());
        Map<String, String> attributes = new LinkedHashMap<>();
        // Alle Sprachen durchlaufen und den Text erzeugen
        for (String language : allSaTextLanguages) {
            String text = "";
            String saText = saTextObject.getText(language);
            if (StrUtils.isValid(saText)) {
                text = saText;
            }
            String saaText = saaTextObject.getText(language);
            if (StrUtils.isValid(saaText) && !text.equals(saaText)) {
                if (StrUtils.isValid(text)) {
                    text += " - ";
                }
                text += saaText;
            }

            // Konnte ein Text zusammengebaut werden, wird er hier gesetzt
            if (StrUtils.isValid(text)) {
                if (!hasText) {
                    startElement(ELEMENT_DESCRIPTION);
                    hasText = true;
                }
                attributes.put(ATTRIBUTE_LANGUAGE, language.toLowerCase());
                attributes.put(ATTRIBUTE_SHORT_DESCRIPTION, text);
                writeEnclosedElement(ELEMENT_INTERNATIONAL_DESCRIPTION, null, attributes, true);
            }

            attributes.clear();
        }
        if (hasText) {
            endElement(ELEMENT_DESCRIPTION);
        }
    }

    /**
     * Formatiert die SAA so wie es VeDoc erwartet
     *
     * @param currentSaa
     * @return
     */
    private String formatSaa(String currentSaa) {
        // SAA in das gewünschte Ausgabeformat formatieren
        String formattedSaa = currentSaa;
        if (formattedSaa.startsWith("Z")) {
            formattedSaa = StrUtils.removeFirstCharacter(formattedSaa);
        }
        formattedSaa = formattedSaa.trim();
        if (formattedSaa.length() < SAA_LENGTH) {
            formattedSaa = StrUtils.leftFill(formattedSaa, SAA_LENGTH, '0');
        }
        return formattedSaa;
    }

    /**
     * Liefert die KGs zur übergebenen SAA auf Basis der SAA-Gültigkeit in Stücklistenpositionen
     *
     * @param currentSaa
     * @param saModulesSet
     * @param kgsForSa
     * @return
     */
    private Set<String> getKGsForSaa(String currentSaa, Set<String> saModulesSet, Set<String> kgsForSa) {
        Set<String> kgsForSaa = new TreeSet<>();
        if (!StrUtils.isValid(currentSaa)) {
            return kgsForSaa;
        }

        VarParam<Boolean> saaValidityInSaModuleFound = new VarParam<>(false);
        EtkDataObjectArrayList list = new EtkDataObjectArrayList();

        // Callback um die KGs zu bestimmen
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {

            @Override
            public boolean createDataObjects() {
                return false;
            }

            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String moduleNumber = attributes.getFieldValue(FIELD_K_VARI);
                if (moduleNumber.startsWith(SA_MODULE_PREFIX)) {
                    if (saModulesSet.contains(moduleNumber)) {
                        saaValidityInSaModuleFound.setValue(true);
                    }
                    return false;
                }
                String kg = attributes.getFieldValue(FIELD_DME_SOURCE_KG);
                if (StrUtils.isValid(kg)) {
                    kgsForSaa.add(kg);
                }
                return false;
            }
        };

        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_KG, false, false));

        String[] whereTableAndFields = new String[]{ FIELD_DWA_FELD, FIELD_DWA_TOKEN };
        String[] whereValues = new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_SA_VALIDITY), currentSaa };

        // Bei leerem saModulesSet gibt es kein Modul für die SA -> es ist also keine freie SA und demzufolge kann der Join
        // mit DA_MODULES_EINPAS als Inner Join erfolgen. Bei vorhandenem Modul ist es aber eine freie SA, wo es demzufolge
        // nicht zwangsweise (bzw. in der Regel sogar keine) Einträge in DA_MODULES_EINPAS gibt, weswegen wir einen Left
        // Outer Join verwenden müssen.
        list.searchSortAndFillWithJoin(getProject(), getProject().getDBLanguage(),
                                       selectFields,
                                       whereTableAndFields,
                                       whereValues,
                                       false,
                                       new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI) },
                                       false, false, true,
                                       foundAttributesCallback,
                                       new EtkDataObjectList.JoinData(TABLE_KATALOG,
                                                                      new String[]{ FIELD_DWA_ARRAYID },
                                                                      new String[]{ FIELD_K_SA_VALIDITY },
                                                                      false, false),
                                       new EtkDataObjectList.JoinData(TABLE_DA_MODULES_EINPAS,
                                                                      new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI) },
                                                                      new String[]{ FIELD_DME_MODULE_NO },
                                                                      !saModulesSet.isEmpty(), false));

        // SAA-Gültigkeit in einem SA-TU (freie SA) gefunden? Falls ja, dann die KGs für die SA hinzufügen
        if (saaValidityInSaModuleFound.getValue()) {
            kgsForSaa.addAll(kgsForSa);
        }

        return kgsForSaa;
    }

    /**
     * Lifert die KGs zur übergebenen SA auf Basis der Tabelle DA_PRODUCT_SAS
     *
     * @param currentSa
     * @return
     */
    private Set<String> getKGsForSa(String currentSa) {
        Set<String> kgsForSa = new TreeSet<>();
        EtkProject project = getProject();
        Map<iPartsProductId, Set<String>> productIdsToKGsMap = iPartsSA.getInstance(project, new iPartsSAId(currentSa)).getProductIdsToKGsMap(project);
        for (Set<String> kgs : productIdsToKGsMap.values()) {
            kgsForSa.addAll(kgs);
        }
        return kgsForSa;
    }

    /**
     * Schreibt den XML Inhalt in die Zieldatei. Das Fake Element "<XML>" wird vorher entfernt. Alle Writer/Streams werden
     * neu angelegt.
     *
     * @param writer
     * @throws XMLStreamException
     * @throws IOException
     */
    private void writeDataBlockToFile(DWWriter writer) throws XMLStreamException, IOException {
        endElement(FAKE_XML_ELEMENT);
        closeRemainingElements();
        getXmlWriter().flush();
        getStringWriter().flush();
        String text = getStringWriter().toString();
        text = StrUtils.removeFirstCharacterIfCharacterIs(text, "<" + FAKE_XML_ELEMENT + ">");
        text = StrUtils.removeLastCharacterIfCharacterIs(text, "</" + FAKE_XML_ELEMENT + ">");
        writer.append(text);
    }

    /**
     * Initialisiert den Writer und schreibt das Fake Element "<XML>"
     *
     * @throws XMLStreamException
     * @throws IOException
     */
    private void initWriterForStringDocument() throws XMLStreamException, IOException {
        initWriterForString(false);
        startElement(FAKE_XML_ELEMENT);
    }

    @Override
    protected String getExportFileName() {
        return EXPORT_NAME + "_" + DateUtils.getCurrentDateFormatted(DATEFORMAT_EXPORT_FILE) + "." + MimeTypes.EXTENSION_XML;
    }
}
