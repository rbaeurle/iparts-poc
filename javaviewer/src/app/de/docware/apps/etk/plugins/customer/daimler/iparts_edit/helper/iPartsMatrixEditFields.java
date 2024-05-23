/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.config.EtkConfig;
import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.AbstractEtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldKeyNormal;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.ArrayList;
import java.util.List;

/**
 * Normale MatrixEditlistenfelder-Liste
 */
public class iPartsMatrixEditFields extends AbstractEtkDisplayFields<iPartsMatrixEditField> implements EtkDbConst {

    private List<iPartsMatrixEditField> visibleFields = new DwList<>(); // todo: Angeleht an EtkEditFields, dort wird die List auch nich genutzt

    /**
     * Konstruktor Basisklasse, damit nicht vergessen wird den Klassentyp der Feldeinträge zu setzen.
     */
    public iPartsMatrixEditFields() {
        super(iPartsMatrixEditField.class);
    }

    public void load(EtkConfig etkConfig, String rootKey) {
        super.load(etkConfig, rootKey);
    }

    /**
     * Liste aller sichtbaren und editierbaren Felder zurück liefern
     *
     * @return
     */
    public List<iPartsMatrixEditField> getVisibleEditFields() {
        ArrayList visibleFields = new ArrayList(fields.size());
        for (iPartsMatrixEditField field : fields) {
            if (field.isVisible() /*&& field.isEditierbar()*/) {
                visibleFields.add(field);
            }
        }
        return visibleFields;
    }


    public void loadStandards(EtkConfig etkConfig) {
        for (int y = 0; y < getFields().size(); y++) {
            iPartsMatrixEditField ebenenFeld = getFeld(y);
            EtkDatabaseField feld = etkConfig.getFieldDescription(ebenenFeld.getKey().getName());
            // Zusatzfelder (Pseudofelder) sind nicht in DB-Desc enthalten (feld == null)
            if (feld != null) {
                ebenenFeld.setMultiLanguage(feld.isMultiLanguage());
                ebenenFeld.setArray(feld.isArray());
            }
        }
    }


    public void addField(iPartsMatrixEditField value) {
        if (!value.getKey().getName().isEmpty()) {
            addFeld(value);
            if (value.isVisible()) {
                visibleFields.add(value);
            }
        }
    }

    private void init(iPartsMatrixEditFields editFields) {
        clear();
        for (int lfdNr = 0; lfdNr < editFields.size(); lfdNr++) {
            iPartsMatrixEditField field = new iPartsMatrixEditField();
            field.assign(editFields.getFeld(lfdNr));
            addField(field);
        }
        //todo zukünftig keyFields fehlen noch
        //keyField = editFields.getKeyField();
        //keyField3D = editFields.getKeyField3D();
    }

    private void removeNotExistingFields(EtkProject project) {
        for (int lfdNr = fields.size() - 1; lfdNr >= 0; lfdNr--) {
            if (!fields.get(lfdNr).existsInConfig(project.getConfig()) ||
                !fields.get(lfdNr).existsInDB(project.getEtkDbs())) {
                deleteFeld(lfdNr);
            }
        }
    }

    private boolean containsFieldMultiple(String tableName, String fieldName) {
        EtkDisplayFieldKeyNormal key = new EtkDisplayFieldKeyNormal(tableName, fieldName);
        return containsFieldMultiple(key);
    }

    private boolean containsFieldMultiple(EtkDisplayFieldKeyNormal key) {
        int counter = 0;
        for (int lfdNr = 0; lfdNr < size(); lfdNr++) {
            if (fields.get(lfdNr).getKey().equalContent(key)) {
                counter++;
                if (counter >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getIndexOfTableAndFeldNameAndSprache(EtkDisplayFieldKeyNormal key, String language) {
        for (int lfdNr = 0; lfdNr < size(); lfdNr++) {
            if (fields.get(lfdNr).getKey().equalContent(key) && fields.get(lfdNr).getLanguage().equals(language)) {
                return lfdNr;
            }
        }
        return -1;
    }

    public int getIndexOfTableAndFeldName(EtkDisplayFieldKeyNormal key) {
        for (int lfdNr = 0; lfdNr < size(); lfdNr++) {
            if (fields.get(lfdNr).getKey().equalContent(key)) {
                return lfdNr;
            }
        }
        return -1;
    }

    private void setLanguageForMultiLangFields() {
        //  wenn ein Multisprachfeld nur einmal konfiguriert ist, soll es entsprechend der
        // ActDatabase/ActDokusprache dargestellt werden. Ist es mehrmals konfiguriert,
        // wird es mit der Konfigurierten Sprache dargestellt
        for (int lfdNr = 0; lfdNr < fields.size(); lfdNr++) {
            if (fields.get(lfdNr).isMultiLanguage()) {
                fields.get(lfdNr).setUseActDatabaseLanguage(!containsFieldMultiple(fields.get(lfdNr).getKey()) || fields.get(lfdNr).getLanguage().isEmpty());
            } else {
                fields.get(lfdNr).setUseActDatabaseLanguage(true);
            }
        }
    }

    private boolean isOnlyTextIdAllowedAll(EtkDisplayFieldKeyNormal key) {
        boolean result = true;
        for (int lfdNr = 0; lfdNr < size(); lfdNr++) {
            if (fields.get(lfdNr).getKey().equalContent(key)) {
                result = result && fields.get(lfdNr).isEditTextIdAllowed();
            }
        }
        return result;
    }

    private void setOnlyTextIdAllowedAll(EtkDisplayFieldKeyNormal key, boolean isEditTextIdAlloewd) {
        for (int lfdNr = 0; lfdNr < size(); lfdNr++) {
            if (fields.get(lfdNr).getKey().equalContent(key)) {
                fields.get(lfdNr).setEditTextIdAllowed(isEditTextIdAlloewd);
            }
        }
    }

    private void setTextIdFlagsForMultiLangFields() {
        //Sammeln betroffener Felder, d.h. der Multisprachfelder, die mehrfach vorkommen
        List<String> msFelder = new ArrayList<String>();
        for (int lfdNr = 0; lfdNr < size(); lfdNr++) {
            if (fields.get(lfdNr).isMultiLanguage() && containsFieldMultiple(fields.get(lfdNr).getKey())) {
                if (msFelder.indexOf(fields.get(lfdNr).getKey().getName()) == -1) {
                    msFelder.add(fields.get(lfdNr).getKey().getName());
                }
            }
        }
        // in msFelder sind nun die Mehrsprachigen Felder, die mehrfach vorkommen

        //IsOnlyTextIdAllowed:
        // Ist ein Multisprachfeld mehrfach vorhanden, bekommen alle Vorkommen das Flag=True nur dann,  wenn
        // alle das Flag=True haben. Hat eins davon Flag=False, bekommen alle das Flag=False
        for (int lfdNr = 0; lfdNr < msFelder.size(); lfdNr++) {
            EtkDisplayFieldKeyNormal key = new EtkDisplayFieldKeyNormal(TableAndFieldName.getTableName(msFelder.get(lfdNr)),
                                                                        TableAndFieldName.getFieldName(msFelder.get(lfdNr)));
            boolean effectiveIsOnlyTextIdAllowed = isOnlyTextIdAllowedAll(key);
            setOnlyTextIdAllowedAll(key, effectiveIsOnlyTextIdAllowed);
        }

        //IsEditTextIdAllowed:
        // Ist ein Multisprachfeld mehrfach vorhanden, bekommen alle Vorkommen das Flag=True nur dann,  wenn
        // alle das Flag=True haben. Hat eins davon Flag=False, bekommen alle das Flag=False
        for (int lfdNr = 0; lfdNr < msFelder.size(); lfdNr++) {
            EtkDisplayFieldKeyNormal key = new EtkDisplayFieldKeyNormal(TableAndFieldName.getTableName(msFelder.get(lfdNr)),
                                                                        TableAndFieldName.getFieldName(msFelder.get(lfdNr)));
            boolean effectiveIsOnlyTextIdAllowed = isOnlyTextIdAllowedAll(key);
            for (int j = 0; j < size(); j++) {
                if (this.getFeld(j).getKey().equalContent(key)) {
                    this.getFeld(j).setEditTextIdAllowed(effectiveIsOnlyTextIdAllowed);
                }
            }
        }
    }

    private void handleMultiLangFields() {
        setLanguageForMultiLangFields();
        setTextIdFlagsForMultiLangFields();
    }

}
