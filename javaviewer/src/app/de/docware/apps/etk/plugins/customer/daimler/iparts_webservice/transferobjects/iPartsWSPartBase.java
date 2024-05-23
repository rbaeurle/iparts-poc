/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.VirtualMaterialType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsWWPartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSReplacementsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsColorTable;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * PartBase Data Transfer Object für die iParts Webservices
 */
public class iPartsWSPartBase implements RESTfulTransferObjectInterface {

    private String partNo;
    private String partNoFormatted;
    private boolean textOnly;
    private String name;
    private String nameRef;
    private String es1Key;
    private String es2Key;
    private iPartsWSPartContext partContext;
    private boolean dsr; // DSR Kenner (="Daimler sicherheitsrelevantes Teil bei der Reparatur")
    private boolean pictureAvailable; // Flag für Einzelzeilbilder
    private List<iPartsWSAdditionalPartInformation> additionalPartInformation;
    private iPartsWSReplacementInfo replacementChain;
    private boolean primusCode74Available;

    /**
     * Leerer Konstruktor (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSPartBase() {
    }

    /**
     * Überträgt alle Werte von dem übergebenen Quell-{@link iPartsWSPartBase} zu diesem {@link iPartsWSPartBase}.
     *
     * @param source
     */
    public void assign(iPartsWSPartBase source) {
        partNo = source.partNo;
        partNoFormatted = source.partNoFormatted;
        textOnly = source.textOnly;
        name = source.name;
        nameRef = source.nameRef;
        es1Key = source.es1Key;
        es2Key = source.es2Key;
        if (source.partContext != null) {
            partContext = new iPartsWSPartContext(source.partContext.getModuleId(), source.partContext.getSequenceId());
        } else {
            partContext = null;
        }
        dsr = source.dsr;
        pictureAvailable = source.pictureAvailable;
        if (source.additionalPartInformation != null) {
            additionalPartInformation = new ArrayList<>(source.additionalPartInformation);
        } else {
            additionalPartInformation = null;
        }
    }

    /**
     * Füllt die RMI-Informationen für dieses {@link iPartsWSPartBase}-Objekt.
     *
     * @param project            Zum Nachladen aus der DB
     * @param partListEntry      Der Stücklisteneintrag aus dem das Part DTO erstellt werden soll
     * @param includePartContext Soll der {@link iPartsWSPartContext} zu dem Teil hinzugefügt werden?
     * @param reducedInformation Sollen die Informationen auf ein Minimum reduziert werden?
     * @return (Gemappte Gleichteile -)Teilenummer
     */
    public String assignRMIValues(EtkProject project, iPartsDataPartListEntry partListEntry, boolean includePartContext, boolean reducedInformation) {
        EtkDataPart dataPart = partListEntry.getPart();
        String matNr = dataPart.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR);

        if (!reducedInformation) {
            String name = dataPart.getFieldValue(EtkDbConst.FIELD_M_TEXTNR, project.getDBLanguage(), true);
            setPartBaseValues(matNr, name, dataPart.getFieldValueAsBoolean(iPartsConst.FIELD_M_SECURITYSIGN_REPAIR));

            // ES2
            String uniqueColorNr = getUniqueColorId(partListEntry);
            if (uniqueColorNr != null) {
                this.setEs2Key(uniqueColorNr);
            }

            if (includePartContext && !partListEntry.getAsId().getKLfdnr().equals(iPartsWWPartsHelper.K_LFDNR_NOT_IN_MODULE)) {
                this.setPartContext(new iPartsWSPartContext(partListEntry));
            }
        } else {
            setPartBaseValuesReducedInformation(matNr);
        }

        // Sollen ES1 und ES2 Schlüssel separat ausgegeben werden, dann muss die Grundsachnummer verwendet werden
        if (iPartsPlugin.isWebservicesXMLExportSeparateES12Keys()) {
            setBaseMatNrAndSeparateES12Keys(project, dataPart, reducedInformation);
        }
        return matNr;
    }

    /**
     * Füllt die Nicht-RMI-Informationen für dieses {@link iPartsWSPartBase}-Objekt.
     *
     * @param project                  Zum Nachladen aus der DB
     * @param partListEntry            Der Stücklisteneintrag aus dem das Part DTO erstellt werden soll
     * @param withExtendedDescriptions Sollen erweiterte Beschreibungen wie z.B. Text-IDs ausgegeben werden?
     * @return (Gemappte Gleichteile -)Teilenummer
     */
    public String assignNonRMIValues(EtkProject project, iPartsDataPartListEntry partListEntry, boolean withExtendedDescriptions) {
        EtkDataPart dataPart = partListEntry.getPart();
        String matNr = dataPart.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR);

        this.setNameRef(getNameRefFromPart(dataPart, withExtendedDescriptions));

        if (iPartsNumberHelper.isPseudoPart(matNr)) {
            this.setPartNoFormatted("");
        } else {
            this.setPartNoFormatted(iPartsNumberHelper.formatPartNo(project, matNr, project.getDBLanguage()));
        }

        // V-Positionen, Zusatztexte, Zwischenüberschriften, die kein echtes Material sind speziell als "NUR TEXT" kennzeichnen.
        if (VirtualMaterialType.isPartListTextEntry(partListEntry)) {
            setTextOnly(true);
        }

        // ES1
        String es1 = dataPart.getFieldValue(iPartsConst.FIELD_M_AS_ES_1);
        if (!es1.isEmpty()) {
            this.setEs1Key(es1);
        }

        // Flag für Einzelteilbild
        this.setPictureAvailable(dataPart.getFieldValueAsBoolean(iPartsConst.FIELD_M_IMAGE_AVAILABLE));

        // Sollen ES1 und ES2 Schlüssel separat ausgegeben werden, dann muss die Grundsachnummer verwendet werden
        if (iPartsPlugin.isWebservicesXMLExportSeparateES12Keys()) {
            setBaseMatNrAndSeparateES12Keys(project, dataPart);
        }

        return matNr;
    }

    @JsonIgnore
    public void setPartBaseValues(EtkProject project, String partNo, boolean withExtendedDescriptions) {
        EtkDataPart dataPart = EtkDataObjectFactory.createDataPart(project, partNo, "");
        String name = dataPart.getFieldValue(EtkDbConst.FIELD_M_TEXTNR, project.getDBLanguage(), true);
        partNo = dataPart.getAsId().getMatNr();
        setPartBaseValues(partNo, name, dataPart.getFieldValueAsBoolean(iPartsConst.FIELD_M_SECURITYSIGN_REPAIR));

        this.setNameRef(getNameRefFromPart(dataPart, withExtendedDescriptions));

        if (iPartsNumberHelper.isPseudoPart(partNo)) {
            this.setPartNoFormatted("");
        } else {
            this.setPartNoFormatted(iPartsNumberHelper.formatPartNo(project, partNo, project.getDBLanguage()));
        }

        // Sollen ES1 und ES2 Schlüssel separat ausgegeben werden, dann muss die Grundsachnummer verwendet werden
        if (iPartsPlugin.isWebservicesXMLExportSeparateES12Keys()) {
            setBaseMatNrAndSeparateES12Keys(project, dataPart);
        }
    }

    protected void setPartBaseValues(String partNo, String name, boolean dsrValue) {
        // Es können Pseudo-Teile vorkommen, die nur eine Teilebenennung und keine Teilenummer haben. Hier darf die
        // Teilenummer nicht ausgegebene werden
        if (iPartsNumberHelper.isPseudoPart(partNo)) {
            this.setPartNo("");
        } else {
            // Ab DAIMLER-9716 werden QSL Sachnummer ohne SL ausgegeben
            this.setPartNo(iPartsNumberHelper.handleQSLPartNo(partNo));
        }
        this.setName(name);
        this.setEs1Key(null);
        this.setEs2Key(null);
        this.setPartContext(null);
        this.setDsr(dsrValue);
    }

    protected void setPartBaseValuesReducedInformation(String partNo) {
        // Es können Pseudo-Teile vorkommen, die nur eine Teilebenennung und keine Teilenummer haben. Hier darf die
        // Teilenummer nicht ausgegebene werden
        if (iPartsNumberHelper.isPseudoPart(partNo)) {
            this.setPartNo("");
        } else {
            // Ab DAIMLER-9716 werden QSL Sachnummer ohne SL ausgegeben
            this.setPartNo(iPartsNumberHelper.handleQSLPartNo(partNo));
        }
    }

    // Getter and Setter
    public boolean getTextOnly() {
        return textOnly;
    }

    public void setTextOnly(boolean textOnly) {
        this.textOnly = textOnly;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getPartNoFormatted() {
        return partNoFormatted;
    }

    public void setPartNoFormatted(String partNoFormatted) {
        this.partNoFormatted = partNoFormatted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEs1Key() {
        return es1Key;
    }

    public void setEs1Key(String es1Key) {
        this.es1Key = es1Key;
    }

    public String getEs2Key() {
        return es2Key;
    }

    public void setEs2Key(String es2Key) {
        this.es2Key = es2Key;
    }

    public iPartsWSPartContext getPartContext() {
        return partContext;
    }

    public void setPartContext(iPartsWSPartContext partContext) {
        this.partContext = partContext;
    }

    public boolean isDsr() {
        return dsr;
    }

    public void setDsr(boolean dsr) {
        this.dsr = dsr;
    }

    public String getNameRef() {
        return nameRef;
    }

    public void setNameRef(String nameRef) {
        this.nameRef = nameRef;
    }

    @JsonIgnore
    protected String getNameRefFromPart(EtkDataPart part, boolean withExtendedDescriptions) {
        if (withExtendedDescriptions) {
            EtkMultiSprache partName = part.getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);
            if (partName != null) {
                String textId = partName.getTextId();
                if (!textId.isEmpty()) {
                    return textId;
                }
            }
        }
        return null;
    }

    public boolean isPictureAvailable() {
        return pictureAvailable;
    }

    public void setPictureAvailable(boolean pictureAvailable) {
        this.pictureAvailable = pictureAvailable;
    }

    /**
     * Diese Methode setzt den ES1 und ES2 Schlüssel, falls vorhanden, in separate Attribute,
     * zudem wird die Grundsachnummer als formatierte und unformatierte Teilenummer verwendet.
     *
     * @param project
     * @param dataPart
     */
    protected void setBaseMatNrAndSeparateES12Keys(EtkProject project, EtkDataPart dataPart) {
        setBaseMatNrAndSeparateES12Keys(project, dataPart, false);
    }

    /**
     * Diese Methode setzt den ES1 und ES2 Schlüssel, falls vorhanden, in separate Attribute,
     * zudem wird die Grundsachnummer als formatierte und unformatierte Teilenummer verwendet.
     *
     * @param project
     * @param dataPart
     * @param reducedInformation Sollen die Informationen auf ein Minimum reduziert werden?
     */
    protected void setBaseMatNrAndSeparateES12Keys(EtkProject project, EtkDataPart dataPart, boolean reducedInformation) {
        // Grundsachnummer - unformatiert und formatiert - verwenden
        String basePartNumber = dataPart.getFieldValue(iPartsConst.FIELD_M_BASE_MATNR);
        if (StrUtils.isValid(basePartNumber)) {
            this.setPartNo(basePartNumber);
            if (!reducedInformation) {
                this.setPartNoFormatted(iPartsNumberHelper.formatPartNo(project, basePartNumber, project.getDBLanguage()));
            }
        }

        setES1ES2Keys(dataPart);
    }

    @JsonIgnore
    protected void setES1ES2Keys(EtkDataPart dataPart) {
        // ES1 Schlüssel
        String es1 = dataPart.getFieldValue(iPartsConst.FIELD_M_AS_ES_1);
        if (!es1.isEmpty()) {
            this.setEs1Key(es1);
        }

        // ES2 Schlüssel - falls nicht bereits aus ColorTable befüllt - aus Materialtabelle entnehmen - falls vorhanden
        String es2 = dataPart.getFieldValue(iPartsConst.FIELD_M_AS_ES_2);
        if (StrUtils.isEmpty(this.es2Key) && !es2.isEmpty()) {
            this.setEs2Key(es2);
        }
    }

    public List<iPartsWSAdditionalPartInformation> getAdditionalPartInformation() {
        return additionalPartInformation;
    }

    public void setAdditionalPartInformation(List<iPartsWSAdditionalPartInformation> additionalPartInformation) {
        this.additionalPartInformation = additionalPartInformation;
    }

    public iPartsWSReplacementInfo getReplacementChain() {
        return replacementChain;
    }

    public void setReplacementChain(iPartsWSReplacementInfo replacementChain) {
        this.replacementChain = replacementChain;
    }

    public void addReplacementsToChain(List<iPartsWSReplacementPart> replacementChain, boolean isSuccessorDirection) {
        if (!Utils.isValid(replacementChain)) {
            return;
        }
        if (this.replacementChain == null) {
            this.replacementChain = new iPartsWSReplacementInfo();
        }
        if (isSuccessorDirection) {
            this.replacementChain.addSuccessors(replacementChain);
        } else {
            this.replacementChain.addPredecessors(replacementChain);
        }
    }

    @JsonIgnore
    protected String getUniqueColorId(iPartsDataPartListEntry partListEntry) {
        iPartsColorTable allColorTable = partListEntry.getColorTableForRetail();
        // ES2 soll nur gefüllt werden, wenn es genau eine Variantentabelle mit einer eindeutigen Farbvariante gibt
        if ((allColorTable != null) && (allColorTable.getColorTablesMap().size() == 1)) {
            iPartsColorTable.ColorTable colorTable = allColorTable.getColorTablesMap().values().iterator().next();

            // Eindeutige Farbvariante bestimmen
            String uniqueColorNr = null;
            for (iPartsColorTable.ColorTableContent colorTableContent : colorTable.colorTableContents) {
                String colorNr = colorTableContent.colorNumber;
                if (uniqueColorNr == null) {
                    uniqueColorNr = colorNr;
                } else if (!uniqueColorNr.equals(colorNr)) {
                    uniqueColorNr = null;
                    break;
                }
            }

            return uniqueColorNr;
        }
        return null;
    }

    public boolean isPrimusCode74Available() {
        return primusCode74Available;
    }

    public void setPrimusCode74Available(boolean primusCode74Available) {
        this.primusCode74Available = primusCode74Available;
    }

    public void setPrimusCode74AvailableDependingOnPrimusRepCacheData(iPartsPRIMUSReplacementsCache primusReplacementsCache) {
        setPrimusCode74Available(primusReplacementsCache.hasForwardCode74ForPartNo(getPartNo()));
    }
}