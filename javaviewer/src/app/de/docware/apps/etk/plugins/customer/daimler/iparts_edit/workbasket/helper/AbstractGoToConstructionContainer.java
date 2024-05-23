/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.events.FilterChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.SessionKeyHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsGotoHelper;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Hilfsklasse für alle Informationen um aus dem MBS-SAA- oder MBS-KEM-Arbeitsvorrat in die MBS Konstruktion zu springen
 */
public abstract class AbstractGoToConstructionContainer {

    private final EtkProject project;
    private final String modelNo;
    private final String saaBkNo;
    private final String conGroup;
    private AssemblyId gotoAssemblyId;  // AssemblyId, die angesprungen wird
    private PartListEntryId gotoPartListEntryId;  // welcher Eintrag soll selektiert werden, wenn einen Ebene höher gesprungen wird

    public AbstractGoToConstructionContainer(EtkProject project, String modelNo, String saaBkNo, String conGroup) {
        this.project = project;
        this.modelNo = modelNo;
        this.saaBkNo = saaBkNo;
        this.conGroup = conGroup;
        prepare();
    }

    /**
     * Den eigentlichen Sprung durchführen und ggf die TUs im Editor laden
     * Falls das BM nicht mehr in MBS geladen: den Vorgang im Fortschritts-Dialog durchführen
     *
     * @param withOwnDialog
     * @param withLoadInEdit
     * @return
     */
    public boolean gotoMBSConstruction(boolean withOwnDialog, boolean withLoadInEdit) {
        if (!isInitForGoto()) {
            return false;
        }
        // zur Sicherheit nochmals das MBS-BM abfragen
        if (SessionKeyHelper.addMBSConstructionModelToFilter(getModelNo())) {
            final VarParam<Boolean> gotoResult = new VarParam<>(false);
            EtkMessageLogForm logForm = new EtkMessageLogForm("!!MBS Konstruktionsbaumuster", "!!Lade MBS Baumuster...", null);
            logForm.disableButtons(true);
            logForm.getGui().setSize(600, 250);
            logForm.showModal(new FrameworkRunnable() {

                @Override
                public void run(FrameworkThread thread) {
                    logForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade Baumuster \"%1\"...", getModelNo()));
                    project.fireProjectEvent(new FilterChangedEvent());
                    gotoResult.setValue(doGotoMBSConstruction(withOwnDialog, withLoadInEdit));
                }
            });
            return gotoResult.getValue();
        }
        return doGotoMBSConstruction(withOwnDialog, withLoadInEdit);
    }

    /**
     * Liefert zurück, ob die Assembly angesprungen werden kann
     *
     * @param connector
     * @param parentForm
     * @param withOwnDialog
     * @return
     */
    protected boolean findGoToPath(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm, boolean withOwnDialog) {
        NavigationPath path = new NavigationPath();
        path.addAssembly(getGotoAssemblyId());
        boolean foundPath = iPartsGotoHelper.gotoPath(connector, parentForm, withOwnDialog, false, path,
                                                      getGotoAssemblyId(), getGotoPartListEntryId());
        if (!foundPath) {
            showMessageIfModelNotValid();
        }
        return foundPath;
    }

    /**
     * Zeigt eine Warnung, wenn die Konstruktionsdaten zum Baumuster nicht gefunden wurden
     */
    protected void showMessageIfModelNotValid() {
        String msg;
        String visSaaBkNo = iPartsNumberHelper.formatPartNo(project, getSaaBkNumber());
        if (StrUtils.isValid(getModelNo())) {
            msg = TranslationHandler.translate("!!Keine Konstruktionsdaten für \"%1\" BM: \"%2\" gefunden!", visSaaBkNo, getModelNo());
        } else {
            msg = TranslationHandler.translate("!!Keine Konstruktionsdaten für \"%1\" gefunden!", visSaaBkNo);
        }
        MessageDialog.showWarning(msg);
    }

    /**
     * Bereitet den Sprung vor (Laden des Baumusters und Berechnung der Ziele
     */
    protected void prepare() {
        if (StrUtils.isValid(getSaaBkNumber(), getModelNo())) {
            // Pfad für den Sprung in die Konstruktions zusammenbauen inkl. setzen des BM für den MBS-BM-Filter
            SessionKeyHelper.getSelectedMBSModelMapWithUserSettingsCheck(project);
            if (SessionKeyHelper.addMBSConstructionModelToFilter(getModelNo())) {
                EtkMessageLogForm logForm = new EtkMessageLogForm("!!MBS Konstruktionsbaumuster", "!!Lade MBS Baumuster...", null);
                logForm.disableButtons(true);
                logForm.getGui().setSize(600, 250);
                logForm.showModal(new FrameworkRunnable() {

                    @Override
                    public void run(FrameworkThread thread) {
                        logForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade Baumuster \"%1\"...", getModelNo()));
                        project.fireProjectEvent(new FilterChangedEvent());
                        setGoToAssemblyIdFromConGroup();
                    }
                });
            } else {
                setGoToAssemblyIdFromConGroup();
            }
        }
    }

    /**
     * Erzeugt eine {@link AssemblyId} für das aktuelle Baumuster, SAA/BK und Konstruktionsgruppe
     *
     * @return
     */
    protected iPartsAssemblyId createAssemblyIdForConGroup() {
        List<iPartsVirtualNode> nodes = iPartsGotoHelper.createVirtualNodePathForMBSConstruction(getModelNo(), getSaaBkNumber(), conGroup);
        return new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(ArrayUtil.toArray(nodes)), "");
    }

    /**
     * Setzt die Ziel-Assembly für das aktuelle Baumuster, SAA/BK und Konstruktionsgruppe
     */
    protected void setGoToAssemblyIdFromConGroup() {
        iPartsAssemblyId assemblyIdForConGroup = createAssemblyIdForConGroup();
        EtkDataAssembly etkAssembly = EtkDataObjectFactory.createDataAssembly(project, assemblyIdForConGroup);
        if (etkAssembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly assembly = (iPartsDataAssembly)etkAssembly;
            AssemblyId parentAssemblyId = assembly.getFirstParentAssemblyIdFromParentEntries();
            EtkDataAssembly etkParentAssembly = EtkDataObjectFactory.createDataAssembly(project, parentAssemblyId);
            List<EtkDataPartListEntry> partList = etkParentAssembly.getPartList(etkParentAssembly.getEbene());
            if (partList.size() > 1) {
                for (EtkDataPartListEntry partListEntry : partList) {
                    if (partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_SUB_SNR).equals(conGroup)) {
                        setGotoPartListEntryId(partListEntry.getAsId());
                        break;
                    }
                }
                List<iPartsVirtualNode> nodes = iPartsVirtualNode.parseVirtualIds(parentAssemblyId);
                setGotoAssemblyId(new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(ArrayUtil.toArray(nodes)), ""));
            } else {
                setGotoAssemblyId(assemblyIdForConGroup);
            }
        }
    }

    /**
     * Alles für Sprung berechnet?
     *
     * @return
     */
    protected boolean isInitForGoto() {
        return (gotoAssemblyId != null);
    }

    protected String getModelNo() {
        return modelNo;
    }

    protected String getSaaBkNumber() {
        return saaBkNo;
    }

    protected AssemblyId getGotoAssemblyId() {
        return gotoAssemblyId;
    }

    protected PartListEntryId getGotoPartListEntryId() {
        return gotoPartListEntryId;
    }

    protected void setGotoAssemblyId(AssemblyId gotoAssemblyId) {
        this.gotoAssemblyId = gotoAssemblyId;
    }

    protected void setGotoPartListEntryId(PartListEntryId gotoPartListEntryId) {
        this.gotoPartListEntryId = gotoPartListEntryId;
    }

    /**
     * Sprung ausführen und ggf TUs im Editor laden
     * sowohl direkt oder aus dem Fortschritts-Dialog heraus
     *
     * @param withOwnDialog
     * @param withLoadInEdit
     * @return
     */
    protected abstract boolean doGotoMBSConstruction(boolean withOwnDialog, boolean withLoadInEdit);
}
