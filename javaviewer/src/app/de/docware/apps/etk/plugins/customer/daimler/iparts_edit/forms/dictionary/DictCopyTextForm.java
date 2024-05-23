/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.dictionary;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dialog zum Kopieren eines Lexikon-Eintrags in eine andre Textart
 */
public class DictCopyTextForm extends DictCreateTextIdForm {

    public static void copyDictText(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                    Set<DictTextKindTypes> allowedTextKindTypes, iPartsDictTextKindId textKindId,
                                    iPartsDataDictMeta dataDictMeta, boolean editAllowed,
                                    boolean statusChangeAllowed, boolean useExactCopy) {
        DictCopyTextForm dlg = new DictCopyTextForm(dataConnector, parentForm, textKindId, allowedTextKindTypes,
                                                    dataDictMeta, editAllowed, statusChangeAllowed, useExactCopy);
        dlg.setTitle("!!Text kopieren");
        if (dlg.showModal() == ModalResult.OK) {
            MessageDialog.show(TranslationHandler.translate("!!Text wurde in die Textart \"%1\" kopiert.", dlg.getSelectedTextKindText(dataConnector.getProject().getViewerLanguage())), "!!Text kopieren");
        }
    }

    private iPartsDataDictMeta savedDataDictMeta;
    private boolean useExactCopy;

    /**
     * Erzeugt eine Instanz von DictCreateTextId.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param textKindId
     * @param allowedTextKindTypes
     * @param dataDictMeta
     * @param editAllowed
     * @param statusChangeAllowed
     */
    public DictCopyTextForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                            iPartsDictTextKindId textKindId, Set<DictTextKindTypes> allowedTextKindTypes,
                            iPartsDataDictMeta dataDictMeta, boolean editAllowed, boolean statusChangeAllowed, boolean useExactCopy) {
        super(dataConnector, parentForm, textKindId, allowedTextKindTypes, dataDictMeta, false, editAllowed, statusChangeAllowed);
        this.savedDataDictMeta = null;
        this.useExactCopy = useExactCopy;
    }

    @Override
    protected void fillFields(boolean statusChangeAllowed, boolean isSingleTextKind, boolean editForMigrationAllowed) {
        super.fillFields(statusChangeAllowed, isSingleTextKind, false);
        if (isSingleTextKind) {
            setTextKindTitleAndEnabled("!!Textart", false);
        } else {
            setTextKindTitleAndEnabled("!!Ziel-Textart Auswahl", true);
        }
        setMultiLangReadOnly(true);
    }

    @Override
    protected boolean isAllowedTextKindToAdd(Map.Entry<String, iPartsDataDictTextKind> textKind,
                                             Set<DictTextKindTypes> allowedTextKindTypesList, iPartsDictTextKindId textKindId) {
        if (allowedTextKindTypesList == null) {
            return true;
        }
        if (!allowedTextKindTypesList.contains(textKind.getValue().getForeignTextKindType())) {
            return false;
        }
        if (textKindId == null) {
            return true;
        }
        // die aktuelle Textart nicht übernehmen
        return !textKind.getValue().getAsId().getTextKindId().equals(textKindId.getTextKindId());
    }

    @Override
    protected boolean checkData(List<String> warnings) {
        // damit checkData() die richtigen Abfragen macht
        if (!useExactCopy) {
            isCreate = true;
        }
        boolean result = super.checkData(warnings);
        isCreate = false;

        if (result && !isReadOnly()) {
            iPartsDataDictTextKind selectedDataTextKind = getSelectedTextKind();
            if (selectedDataTextKind == null) {
                addWarning(warnings, "!!Es wurde keine Textart ausgewählt.");
                return false;
            }

            iPartsDictMetaId dictMetaId = new iPartsDictMetaId(selectedDataTextKind.getAsId().getTextKindId(), getEditTextId());
            iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
            if (dataDictMeta.existsInDB()) {
                addWarning(warnings, "!!Der Text (TextID \"%1\") existiert bereits in der Textart \"%2\".",
                           dictMetaId.getTextId(), getSelectedTextKindText(getProject().getViewerLanguage()));
                return false;
            }
        }
        return result;
    }

    @Override
    protected void saveToDB() {
        // damit saveToDB() die richtigen Aktionen macht
        savedDataDictMeta = dataDictMeta;
        dataDictMeta = null;
        isCreate = true;

        super.saveToDB();

        isCreate = false;
        dataDictMeta = savedDataDictMeta;
        savedDataDictMeta = null;
    }

    @Override
    protected iPartsDataDictMeta createDataMetaKind(iPartsDataDictTextKind dataTextKind) {
        iPartsDataDictMeta dataDictMeta = super.createDataMetaKind(dataTextKind);
        if ((savedDataDictMeta != null) && !useExactCopy) {
            // alle FremdIds weitergeben
            String foreignId = savedDataDictMeta.getELDASId();
            if (StrUtils.isValid(foreignId)) {
                dataDictMeta.setELDASId(foreignId, DBActionOrigin.FROM_EDIT);
            }
            foreignId = savedDataDictMeta.getDIALOGId();
            if (StrUtils.isValid(foreignId)) {
                dataDictMeta.setDIALOGId(foreignId, DBActionOrigin.FROM_EDIT);
            }
        }
        dataDictMeta.setSaveAllUsages(true);
        return dataDictMeta;
    }

    @Override
    protected EtkDataObjectList getEtkDataObjectList(iPartsDataDictMeta saveDataDictMeta, EtkMultiSprache multi) {
        EtkDataObjectList dataObjectList = new GenericEtkDataObjectList();
        dataObjectList.add(saveDataDictMeta, DBActionOrigin.FROM_EDIT);

        iPartsDataDictTextKindUsageList usages = DictTxtKindIdByMADId.getInstance(getProject()).getUsagesByTextKindId(new iPartsDictTextKindId(saveDataDictMeta.getAsId().getTextKindId()),
                                                                                                                      getProject());
        if (usages != null) {
            for (iPartsDataDictTextKindUsage textKindUsage : usages) {
                String tableDotFieldName = textKindUsage.getFeld();
                addTextEntries(tableDotFieldName, multi, dataObjectList);
            }
        } else {
            addTextEntries("", multi, dataObjectList);
        }

        return dataObjectList;
    }
}
