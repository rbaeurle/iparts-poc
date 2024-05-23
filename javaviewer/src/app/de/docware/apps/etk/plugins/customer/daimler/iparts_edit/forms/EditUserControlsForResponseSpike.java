/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseSpike;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseSpikeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsResponseSpikeId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiIdentTextField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsResponseSpikesDataForm;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;

/**
 * UserControl zum Bearbeiten von Ausreißern (DAIMLER-4871)
 *
 * Regeln:
 * Übernommene Felder aus den Rückmeldedaten: Baureihe, AA, Fahrzeugidentnummer, PEM, Lenkung
 * Pflichtangaben: Werk, Ident-Ausreißer (entweder einzeln oder bei einem Wertebereich Ident-Ausreißer von und bis)
 * Optionale Felder: BMAA, Gültigkeit
 * => aktuell hauptsächlich über Konfiguration in WB gelöst
 */
public class EditUserControlsForResponseSpike extends EditUserControlForCreate implements iPartsConst {

    // Diese Felder kommen von den Rückmeldedaten und dürfen deshalb nicht bearbeitet werden (hiermit wird die WS Konfig überschrieben)
    private static final String[] readOnlyFieldNames = new String[]{ FIELD_DRS_FACTORY, FIELD_DRS_SERIES_NO, FIELD_DRS_AA, FIELD_DRS_IDENT,
                                                                     FIELD_DRS_ADAT, FIELD_DRS_PEM, FIELD_DRS_STEERING, FIELD_DRS_SOURCE,
                                                                     FIELD_DRS_AS_DATA, FIELD_DRS_STATUS };
    private static final String[] mustHaveValueFieldNames = new String[]{ FIELD_DRS_PEM, FIELD_DRS_IDENT, FIELD_DRS_SPIKE_IDENT };
    private static final String[] invisibleFieldNames = new String[]{};
    private static final String[] eldasInvibleFieldNames = new String[]{ FIELD_DRS_FACTORY, FIELD_DRS_SERIES_NO, FIELD_DRS_AA };
    private static final String[] allowedEmptyPKFields = new String[]{ FIELD_DRS_SERIES_NO, FIELD_DRS_AA, FIELD_DRS_BMAA };

    private static final String FIELD_DRS_SPIKE_IDENT_FROM = VirtualFieldsUtils.addVirtualFieldMask("DRS_SPIKE_IDENT_FROM");
    private static final String FIELD_DRS_SPIKE_IDENT_TO = VirtualFieldsUtils.addVirtualFieldMask("DRS_SPIKE_IDENT_TO");

    private static final int MAX_IDENT_RANGE = 100;

    public static iPartsDataResponseSpikeList showEditCreateResponseSpike(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                                          iPartsResponseSpikeId externResponseSpikeId, String factory,
                                                                          boolean identRange, boolean isNewForm, boolean isEldasPartlist) {

        String tableName = TABLE_DA_RESPONSE_SPIKES;
        EtkProject project = dataConnector.getProject();

        iPartsDataResponseSpike responseSpike = null;
        if (externResponseSpikeId != null) {
            responseSpike = new iPartsDataResponseSpike(project, externResponseSpikeId);
            if (!responseSpike.existsInDB()) {
                responseSpike = null;
            }
        }
        String text;
        boolean useExistingData = false;
        if (responseSpike == null) {
            responseSpike = new iPartsDataResponseSpike(project, externResponseSpikeId);
            responseSpike.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            isNewForm = true;
            text = "!!Vorläufer / Nachzügler erstellen";
        } else {
            useExistingData = responseSpike.getSource() == iPartsImportDataOrigin.IPARTS;
            text = "!!Vorläufer / Nachzügler bearbeiten";

        }
        prepareForEdit(responseSpike, factory, isNewForm);
        EtkEditFields editFields = modifyEditFields(project, tableName, factory, isEldasPartlist);
        EditUserControlsForResponseSpike eCtrl = new EditUserControlsForResponseSpike(dataConnector, parentForm, tableName,
                                                                                      responseSpike.getAsId(),
                                                                                      responseSpike.getAttributes(),
                                                                                      editFields, identRange, isNewForm, factory);
        eCtrl.setMainTitle(text);
        eCtrl.setTitle("PEM: " + responseSpike.getAsId().getPem() + "\n" + TranslationHandler.translate("!!Fahrzeugidentnummer:")
                       + " " + responseSpike.getAsId().getIdent());
        ModalResult modalResult = eCtrl.showModal();
        iPartsDataResponseSpikeList result = null;
        if (modalResult == ModalResult.OK) {
            result = new iPartsDataResponseSpikeList();
            if (useExistingData) {
                // Weil das EditControl nur auf den Attributes arbeitet und wir die Beziehung zur alten ID nicht verlieren wollen,
                // muss hier die ID aus den bearbeiteten Attributes heraus synchronisiert werden.
                responseSpike.updateIdFromPrimaryKeys();
                result.add(responseSpike, DBActionOrigin.FROM_EDIT);
            } else {
                for (String spikeIdent : eCtrl.getSpikeIdents()) {
                    iPartsResponseSpikeId responseSpikeId = responseSpike.getAsId();
                    iPartsResponseSpikeId newResponseSpikeId = new iPartsResponseSpikeId(responseSpikeId.getFactory(), responseSpikeId.getSeriesNo(),
                                                                                         responseSpikeId.getAusfuehrungsArt(),
                                                                                         responseSpikeId.getBmaa(), responseSpikeId.getIdent(),
                                                                                         spikeIdent, responseSpikeId.getPem(),
                                                                                         responseSpikeId.getAdatAttribute(),
                                                                                         responseSpikeId.getAsData());
                    iPartsDataResponseSpike newDataResponseSpike = new iPartsDataResponseSpike(project, newResponseSpikeId);
                    DBDataObjectAttributes newAttributes = eCtrl.getAttributes().cloneMe(DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                    newAttributes.addField(FIELD_DRS_SPIKE_IDENT, spikeIdent, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                    newDataResponseSpike.setAttributes(newAttributes, DBActionOrigin.FROM_EDIT);
                    result.add(newDataResponseSpike, DBActionOrigin.FROM_EDIT);
                }
            }
        }
        return result;
    }

    private static EtkEditFields modifyEditFields(EtkProject project, String tableName, String factory, boolean isEldasPartlist) {
        String[] mustHaveValueFieldNamesForEdit = mustHaveValueFieldNames;
        String[] allowedEmptyPKFieldsForEdit = allowedEmptyPKFields;
        String[] invisibleFieldNamesForEdit = invisibleFieldNames;
        if (isEldasPartlist) {
            invisibleFieldNamesForEdit = eldasInvibleFieldNames;
        }
        if (StrUtils.isValid(factory)) { // Werk muss befüllt sein
            mustHaveValueFieldNamesForEdit = StrUtils.mergeArrays(mustHaveValueFieldNamesForEdit, new String[]{ FIELD_DRS_FACTORY });
        } else { // Werk darf im Primärschlüssel fehlen
            allowedEmptyPKFieldsForEdit = StrUtils.mergeArrays(allowedEmptyPKFieldsForEdit, new String[]{ FIELD_DRS_FACTORY });
        }

        return modifyEditFields(project, iPartsResponseSpikesDataForm.CONFIG_KEY_RESPONSE_SPIKES_TOP, tableName, mustHaveValueFieldNamesForEdit,
                                allowedEmptyPKFieldsForEdit, invisibleFieldNamesForEdit, readOnlyFieldNames);
    }

    private static EtkEditFields addIdentRangeFields(DBDataObjectAttributes attributes, EtkEditFields externalEditFields,
                                                     boolean identRange, EtkProject project) {
        if (identRange) {
            String identFieldText = "!!Ausreißer Fahrzeugidentnummer";
            int index = 0;
            EtkEditField spikeIdentField = externalEditFields.getFeldByName(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_SPIKE_IDENT);
            if (spikeIdentField != null) {
                identFieldText = spikeIdentField.getText().getTextByNearestLanguage(project.getViewerLanguage(), project.getDataBaseFallbackLanguages());
                index = externalEditFields.getIndexOfFeld(spikeIdentField);
                if (index >= 0) {
                    externalEditFields.deleteFeld(index);
                } else {
                    index = 0;
                }
            }

            // Virtuelle Felder für den IdentRange hinzufügen
            attributes.addField(FIELD_DRS_SPIKE_IDENT_FROM, "", true, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            EtkEditField spikeIdentFromField = new EtkEditField(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_SPIKE_IDENT_FROM, false);
            spikeIdentFromField.setText(new EtkMultiSprache("!!Ab %1", new String[]{ project.getViewerLanguage() }, identFieldText));
            spikeIdentFromField.setDefaultText(false);
            spikeIdentFromField.setMussFeld(true);
            externalEditFields.addFeld(index, spikeIdentFromField);

            attributes.addField(FIELD_DRS_SPIKE_IDENT_TO, "", true, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            EtkEditField spikeIdentToField = new EtkEditField(TABLE_DA_RESPONSE_SPIKES, FIELD_DRS_SPIKE_IDENT_TO, false);
            spikeIdentToField.setText(new EtkMultiSprache("!!Bis %1", new String[]{ project.getViewerLanguage() }, identFieldText));
            spikeIdentToField.setDefaultText(false);
            spikeIdentToField.setMussFeld(true);
            externalEditFields.addFeld(index + 1, spikeIdentToField);
        }

        return externalEditFields;
    }

    private static void prepareForEdit(iPartsDataResponseSpike responseSpike, String factory, boolean isNew) {
        responseSpike.setFieldValue(FIELD_DRS_ADAT, DateUtils.toyyyyMMddHHmmss_Calendar(Calendar.getInstance()), DBActionOrigin.FROM_EDIT);
        if (!isNew && (responseSpike.getSource() == iPartsImportDataOrigin.IPARTS)) {
            return;
        }

        // Werk aus den Werkseinsatzdaten verwenden und AS-Flag setzen, damit erkannt werden kann, dass der Datensatz für After-Sales angelegt wurde
        responseSpike.setFieldValue(FIELD_DRS_FACTORY, factory, DBActionOrigin.FROM_EDIT);
        responseSpike.setFieldValueAsBoolean(FIELD_DRS_AS_DATA, true, DBActionOrigin.FROM_EDIT);

        responseSpike.setFieldValue(FIELD_DRS_SOURCE, iPartsImportDataOrigin.IPARTS.getOrigin(), DBActionOrigin.FROM_EDIT);
        if (isNew) {
            responseSpike.setFieldValue(FIELD_DRS_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
        }
    }


    private boolean identRange;
    private boolean isNewForm;

    protected EditUserControlsForResponseSpike(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                               IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields,
                                               boolean identRange, boolean isNewForm, String factory) {
        super(dataConnector, parentForm, tableName, id, attributes, addIdentRangeFields(attributes, externalEditFields, identRange,
                                                                                        dataConnector.getProject()));
        this.identRange = identRange;
        this.isNewForm = isNewForm;
        setWindowName("responseSpikeEdit");
    }

    @Override
    protected boolean isVirtualFieldEditable(String tableName, String fieldName) {
        if (super.isVirtualFieldEditable(tableName, fieldName)) {
            return true;
        }

        // Virtuelle Felder für den IdentRange editierbar machen
        return (tableName.equals(TABLE_DA_RESPONSE_SPIKES) && (fieldName.equals(FIELD_DRS_SPIKE_IDENT_FROM) || fieldName.equals(FIELD_DRS_SPIKE_IDENT_TO)));
    }

    private iPartsGuiIdentTextField getSpikeIdentTextField() {
        iPartsGuiIdentTextField identTextField = null;
        EditControl editControl = getEditControlByFieldName(FIELD_DRS_SPIKE_IDENT);
        if (editControl != null) {
            if (editControl.getAbstractGuiControl() instanceof iPartsGuiIdentTextField) {
                identTextField = (iPartsGuiIdentTextField)editControl.getAbstractGuiControl();
            }
        }
        return identTextField;
    }

    private String getFieldDescription(String fieldName, String defaultText) {
        EditControl editControl = getEditControlByFieldName(fieldName);
        if (editControl != null) {
            return editControl.getLabel().getText();
        }
        return TranslationHandler.translate(defaultText);
    }

    private String getSpikeIdentTextDescription() {
        return getFieldDescription(FIELD_DRS_SPIKE_IDENT, "!!Ausreißer Fahrzeugidentnummer");
    }

    private iPartsGuiIdentTextField getSpikeIdentToTextField() {
        iPartsGuiIdentTextField identTextField = null;
        EditControl editControl = getEditControlByFieldName(FIELD_DRS_SPIKE_IDENT_TO);
        if (editControl != null) {
            if (editControl.getAbstractGuiControl() instanceof iPartsGuiIdentTextField) {
                identTextField = (iPartsGuiIdentTextField)editControl.getAbstractGuiControl();
            }
        }
        return identTextField;
    }

    private iPartsGuiIdentTextField getSpikeIdentFromTextField() {
        iPartsGuiIdentTextField identTextField = null;
        EditControl editControl = getEditControlByFieldName(FIELD_DRS_SPIKE_IDENT_FROM);
        if (editControl != null) {
            if (editControl.getAbstractGuiControl() instanceof iPartsGuiIdentTextField) {
                identTextField = (iPartsGuiIdentTextField)editControl.getAbstractGuiControl();
            }
        }
        return identTextField;
    }

    @Override
    protected boolean checkValues() {
        // zuerst das IdentField überprüfen, falls vorhanden
        iPartsGuiIdentTextField identField = getSpikeIdentTextField();
        if (identField != null) {
            if (!identField.isIdentValid()) {
                String msg = TranslationHandler.translate("!!Die eingegebene \"%1\" \"%2\" ist ungültig!",
                                                          getSpikeIdentTextDescription(), identField.getText());

                MessageDialog.showError(msg);
                return false;
            }
        }

        // jetzt SpikeIdentFrom und SpikeItemTo überprüfen
        DBDataObjectAttributes currentAttributes = getCurrentAttributes();
        if ((currentAttributes != null) && identRange) {
            String spikeIdentFrom = attributes.getFieldValue(FIELD_DRS_SPIKE_IDENT_FROM);
            String spikeIdentTo = attributes.getFieldValue(FIELD_DRS_SPIKE_IDENT_TO);
            if (spikeIdentFrom.isEmpty() || spikeIdentTo.isEmpty()) {
                // einer der beiden Werte ist leer => keine Fehlermeldung, da eigentlich der OK-Button nicht enabled sein dürfte
                return false;
            }
            String spikeIdentFromFieldText = getFieldDescription(FIELD_DRS_SPIKE_IDENT_FROM, "!!Ab Ausreißer Fahrzeugidentnummer");
            String spikeIdentToFieldText = getFieldDescription(FIELD_DRS_SPIKE_IDENT_TO, "!!Bis Ausreißer Fahrzeugidentnummer");

            // Überprüfung der eigentlichen Idents
            StringBuilder errorMsgs = new StringBuilder();
            identField = getSpikeIdentFromTextField();
            if (identField != null) {
                if (!identField.isIdentValid()) {
                    String msg = TranslationHandler.translate("!!Die eingegebene \"%1\" \"%2\" ist ungültig!",
                                                              spikeIdentFromFieldText, identField.getText());
                    errorMsgs.append(msg);
                }
            }
            identField = getSpikeIdentToTextField();
            if ((identField != null) && (!identField.isIdentValid())) {
                String msg = TranslationHandler.translate("!!Die eingegebene \"%1\" \"%2\" ist ungültig!",
                                                          spikeIdentToFieldText, identField.getText());
                if (errorMsgs.length() > 0) {
                    errorMsgs.append("\n");
                }
                errorMsgs.append(msg);
            }
            if (errorMsgs.length() > 0) {
                MessageDialog.showError(errorMsgs.toString());
                return false;
            }

            // beide Idents sind valid => weitere Prüfungen
            boolean identFromStartsWithBU = !Character.isDigit(spikeIdentFrom.charAt(0));
            boolean identToStartsWithBU = !Character.isDigit(spikeIdentTo.charAt(0));
            String spikeIdentFromNumber = "";
            String spikeIdentToNumber = "";

            if (identFromStartsWithBU && identToStartsWithBU) {
                // beide Idents beginnen mit einem Buchstaben => beide müssen gleich sein
                if (spikeIdentFrom.charAt(0) != spikeIdentTo.charAt(0)) {
                    String msg = TranslationHandler.translate("!!Der erste Buchstabe von \"%1\" und \"%2\" muss übereinstimmen.",
                                                              spikeIdentFromFieldText, spikeIdentToFieldText);
                    errorMsgs.append(msg);
                }
                spikeIdentFromNumber = spikeIdentFrom.substring(1);
                spikeIdentToNumber = spikeIdentTo.substring(1);
            } else {
                if ((identFromStartsWithBU && !identToStartsWithBU) || (!identFromStartsWithBU && identToStartsWithBU)) {
                    String msg = TranslationHandler.translate("!!Das erste Zeichen von \"%1\" und \"%2\" muss übereinstimmen.",
                                                              spikeIdentFromFieldText, spikeIdentToFieldText);
                    errorMsgs.append(msg);
                } else {
                    spikeIdentFromNumber = spikeIdentFrom;
                    spikeIdentToNumber = spikeIdentTo;
                }
            }
            if (errorMsgs.length() > 0) {
                MessageDialog.showError(errorMsgs.toString());
                return false;
            }

            // jetzt noch Längen- und Range-Überprüfung der Idents
            if (spikeIdentFromNumber.length() != spikeIdentToNumber.length()) {
                String msg = TranslationHandler.translate("!!Die Länge von \"%1\" und \"%2\" muss übereinstimmen.",
                                                          spikeIdentFromFieldText, spikeIdentToFieldText);
                errorMsgs.append(msg);
            }

            int diff = StrUtils.strToIntDef(spikeIdentToNumber, 0) - StrUtils.strToIntDef(spikeIdentFromNumber, 0) + 1;
            if (diff > MAX_IDENT_RANGE) {
                String msg = TranslationHandler.translate("!!Der Ident-Bereich zwischen \"%1\" und \"%2\" ist mit %3 größer als das erlaubte Maximum von %4.",
                                                          spikeIdentFromFieldText, spikeIdentToFieldText, String.valueOf(diff),
                                                          String.valueOf(MAX_IDENT_RANGE));
                if (errorMsgs.length() > 0) {
                    errorMsgs.append("\n");
                }
                errorMsgs.append(msg);
            }
            if (errorMsgs.length() > 0) {
                MessageDialog.showError(errorMsgs.toString());
                return false;
            }
        }

        return true;
    }

    @Override
    protected void doEnableButtons(Event event) {
        boolean enabled = !checkMustFieldsHaveValues();
        if (!isNewForm && enabled) {
            enabled = checkForModified();
        }
        enableOKButton(readOnly || enabled);
    }

    private String getSeriesNo() {
        String seriesNo = null;
        if (id instanceof iPartsResponseSpikeId) {
            seriesNo = ((iPartsResponseSpikeId)id).getSeriesNo();
        }
        return seriesNo;
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {

        if (field.getKey().getName().equals(TableAndFieldName.make(tableName, FIELD_DRS_SPIKE_IDENT)) ||
            field.getKey().getName().equals(TableAndFieldName.make(tableName, FIELD_DRS_SPIKE_IDENT_FROM)) ||
            field.getKey().getName().equals(TableAndFieldName.make(tableName, FIELD_DRS_SPIKE_IDENT_TO))) {
            iPartsGuiIdentTextField identField = new iPartsGuiIdentTextField();
            identField.setText(initialValue);
            identField.setSeriesNo(getSeriesNo());
            ctrl.getEditControl().setControl(identField);
        }
    }

    public Set<String> getSpikeIdents() {
        Set<String> spikeIdents = new TreeSet<String>();
        if (identRange) {
            // Es ist sichergestellt, dass spikeIdentFrom und spikeIdentTo nicht leer sind, der erste Buchstabe identisch
            // und der Ident-Bereich nicht zu groß ist
            String spikeIdentFrom = attributes.getFieldValue(FIELD_DRS_SPIKE_IDENT_FROM);
            char firstChar = spikeIdentFrom.charAt(0);
            String spikeIdentFromNumber = spikeIdentFrom.substring(1);
            int spikeIdentFromInt = StrUtils.strToIntDef(spikeIdentFromNumber, 0);

            String spikeIdentTo = attributes.getFieldValue(FIELD_DRS_SPIKE_IDENT_TO);
            int spikeIdentToInt = StrUtils.strToIntDef(spikeIdentTo.substring(1), 0);

            // Ident-Bereich erzeugen und mit Nullen vorne auffüllen bis die Länge des ursprünglichen Idents erreicht ist
            // (sonst würde A012345 bis A012350 zu A12345 bis A12350 werden)
            for (int i = spikeIdentFromInt; i <= spikeIdentToInt; i++) {
                spikeIdents.add(firstChar + StrUtils.prefixStringWithCharsUpToLength(String.valueOf(i), '0', spikeIdentFromNumber.length()));
            }

        } else {
            spikeIdents.add(attributes.getFieldValue(FIELD_DRS_SPIKE_IDENT));
        }
        return spikeIdents;
    }
}