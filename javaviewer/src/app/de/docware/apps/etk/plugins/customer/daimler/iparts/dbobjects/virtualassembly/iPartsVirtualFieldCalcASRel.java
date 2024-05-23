/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.sql.SQLStringConvert;

import java.util.List;
import java.util.Set;

/**
 * Klasse zur Berechnung des Feldes DD_CALCULATED-AS_RELEVANT
 * Die Berechnungen basieren auf der Berechnung der Dokurelevanz aus ({@link iPartsVirtualCalcFieldDocuRel})
 */
public class iPartsVirtualFieldCalcASRel extends iPartsDataVirtualFieldsDefinition implements iPartsConst {

    private iPartsVirtualCalcFieldDocuRel docuRel;

    public iPartsVirtualFieldCalcASRel(EtkProject project, EtkDataPartListEntry partListEntry) {
        this.docuRel = new iPartsVirtualCalcFieldDocuRel(project, partListEntry);
    }

    public EtkProject getProject() {
        return docuRel.getProject();
    }

    public EtkDataPartListEntry getPartListEntry() {
        return docuRel.getPartListEntry();
    }


    public void calculateAndSetASRelevant() {
        if (getPartListEntry() != null) {
            boolean specialCalc = docuRel.isAlternativeCalculation();
            String asRelevant = SQLStringConvert.booleanToPPString(calculateASRelevant(specialCalc));
            getPartListEntry().getAttributes().addField(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_AS_RELEVANT, asRelevant, true, DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Berechnet die AS-Relevanz abhängig von den Daten des aktuellen Stücklisteneintrags.
     * Viele der Schritte sind analog zur Berechnung der Dokurelevanz (siehe {@link iPartsVirtualCalcFieldDocuRel}).
     *
     * @param specialCalc
     * @return
     */
    private boolean calculateASRelevant(boolean specialCalc) {
        // Wurde der DokuRelevanz Status manuell gesetzt, ist die Position AS relevant
        if (docuRel.getDocuRelevant() == iPartsDocuRelevant.DOCU_RELEVANT_YES) {
            return true;
        }
        if (docuRel.isUsedInAS()) {
            return true;
        }
        // Im Unterschied zur Berechnung der Dokurelevanz wird hier nur geprüft, ob die Teileposition permanent gültige Bad-Code enthält.
        if (docuRel.hasEveryConjunctionPermanentBadCodes()) {
            return false;
        }
        Set<String> productFactories = docuRel.getProductFactories();
        if (productFactories.isEmpty()) {
            return false;
        }
        // Alle Werksdaten mit PEM bis Termin unendlich oder gültig zum Ablaufdatum und AA der Baureihe
        List<iPartsFactoryData.DataForFactory> factoryData = docuRel.getFactoryData();
        iPartsVirtualCalcFieldDocuRel.KEMToDueDateResult kemToDueDateResult = docuRel.isKEMToBeforeSeriesKEMDueDate(factoryData);
        if (kemToDueDateResult.isNotRelevant()) {
            return false;
        }
        if (docuRel.isDDFEDequalZB()) {
            return true;
        }
        if (docuRel.hasAAForSeriesWithoutFactoryDataCheck()) {
            return true;
        }
        if (docuRel.hasAccOrAsStatusCodes()) {
            return true;
        }
        if (isAdditionalCheckNeeded()) {
            if (!docuRel.calcDocuRelFromFactoryData(specialCalc, factoryData)) {
                return false;
            }
            if (docuRel.doCheckAllPEMToDatesBeforeSOP(kemToDueDateResult, factoryData)) {
                return false;
            }
            if (docuRel.hasFutureFactoryData(factoryData)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check des PEM ab Termins, soll im Gegensatz zur Berechnung der Dokurelevanz hier nur für Teilepositionen mit einer
     * Strukturstufe kleiner/gleich der Strukturstufe der Baureihe durchgeführt werden.
     * <p>
     * Ist an der Baureihe keine Strukturstufe hinterlegt, dann wird die Teilprüfung nicht durchgeführt.
     *
     * @return
     */
    private boolean isAdditionalCheckNeeded() {
        iPartsDialogSeries dialogSeries = iPartsDialogSeries.getInstance(getProject(), new iPartsSeriesId(docuRel.getDDSeries()));
        int partListEntryHierarchy = StrUtils.strToIntDef(docuRel.getDDHierarchy(), -1);
        int seriesHierarchy = StrUtils.strToIntDef(dialogSeries.getHierarchyForCalcASRel(), -1);
        return (seriesHierarchy > 0) && (partListEntryHierarchy <= seriesHierarchy);
    }

}
