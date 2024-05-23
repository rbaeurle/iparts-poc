package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTextKind;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTextKindList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.dictionary.DictMultiLangEditForm;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiMultiLangEdit;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * UserControl für SA/SAA Stammdaten
 */
public class EditUserControlForSAorSAA extends EditUserControls implements iPartsConst {

    private static EnumSet<DictTextKindTypes> DEFAULT_TEXTKIND_TYPES = EnumSet.of(DictTextKindTypes.SA_NAME);

    private EnumSet<DictTextKindTypes> textKindTypes;
    private List<String> specialMultiLangFieldNameList;

    public EditUserControlForSAorSAA(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                     String tableName, IdWithType id, DBDataObjectAttributes attributes,
                                     EtkEditFields externalEditFields, EnumSet<DictTextKindTypes> searchTextKindTypes) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
        textKindTypes = searchTextKindTypes;
        if (this.textKindTypes == null) {
            this.textKindTypes = DEFAULT_TEXTKIND_TYPES;
        }
        this.specialMultiLangFieldNameList = new DwList<>();
    }

    public void setSpecialMultiLangFieldNames(String... fieldNames) {
        specialMultiLangFieldNameList.clear();
        specialMultiLangFieldNameList.addAll(StrUtils.toStringArrayList(fieldNames));
    }

    /**
     * Sollte vor showModal() aufgerufen werden
     *
     * @param fieldNames
     */
    public void handleSpecialMultiLangFields(String... fieldNames) {
        setSpecialMultiLangFieldNames(fieldNames);
        handleSpecialMultiLangFields();
    }

    /**
     * Sollte vor showModal() aufgerufen werden
     */
    public void handleSpecialMultiLangFields() {
        for (String fieldName : specialMultiLangFieldNameList) {
            EditControl eCtrl = getEditControlByFieldName(fieldName);
            GuiMultiLangEdit multiLangEdit = getMultiLangEditFromControl(eCtrl);
            EtkEditField field = editFields.getFeldByName(tableName, fieldName);
            if ((multiLangEdit != null) && (field != null)) {
                if (!this.isReadOnly()) {
                    if (!field.isReadOnly() && field.isEditierbar()) {
                        multiLangEdit.setWithButton(true);
                        multiLangEdit.setTextControlEditable(false);
                        multiLangEdit.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                            @Override
                            public void fire(Event event) {
                                doEditTextId(event, field, multiLangEdit);
                            }
                        });
                    }
                }
            }
        }
    }

    private void handleTextControlEditable() {
        for (String fieldName : specialMultiLangFieldNameList) {
            GuiMultiLangEdit multiLangEdit = getMultiLangEditFromName(fieldName);
            if (multiLangEdit != null) {
                if (multiLangEdit.isWithButton()) {
                    multiLangEdit.setTextControlEditable(false);
                }
            }
        }
    }

    private GuiMultiLangEdit getMultiLangEditFromName(String fieldName) {
        return getMultiLangEditFromControl(getEditControlByFieldName(fieldName));
    }

    private GuiMultiLangEdit getMultiLangEditFromControl(EditControl ctrl) {
        if (ctrl != null) {
            AbstractGuiControl control = ctrl.getAbstractGuiControl();
            if ((control != null) && (control instanceof GuiMultiLangEdit)) {
                return ((GuiMultiLangEdit)control);
            }
        }
        return null;
    }

    private void doEditTextId(Event event, EtkEditField field, GuiMultiLangEdit multiLangEdit) {
        Language lang = Language.findLanguage(getProject().getDBLanguage());
        DBDataObjectAttribute attrib = getAttributeFromKey(field);
        String initialSearchValue = attrib.getMultiLanguageText(lang.getCode(), null);
        DictTextKindTypes initialType = textKindTypes.iterator().next();
        Collection<iPartsDataDictTextKind> textKindList = iPartsDataDictTextKindList.loadSpecialTextKindListSortedByName(getProject(), textKindTypes);
        EtkMultiSprache multiLang = DictMultiLangEditForm.showDictMultiLangEdit(getConnector(), this,
                                                                                lang, initialSearchValue, initialType,
                                                                                textKindList, true, false, tableName);
        if (multiLang != null) {
            multiLangEdit.setMultiLanguage(multiLang.cloneMe());
            handleTextControlEditable();
            doEnableButtons(null);
        }
    }
}
