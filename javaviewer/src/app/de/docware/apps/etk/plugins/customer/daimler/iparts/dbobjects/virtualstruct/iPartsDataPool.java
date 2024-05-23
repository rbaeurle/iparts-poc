/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImageList;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPool;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsValidityScope;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSA;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashSet;
import java.util.Set;

/**
 * Erweiterung von {@link EtkDataPool} um iParts-spezifische Methoden und Daten.
 */
public class iPartsDataPool extends EtkDataPool implements iPartsConst {

    public iPartsDataPool() {
        super();
    }

    /**
     * Liefert das iParts-spezifische Vorschaubild als {@link FrameworkImage} (Erweiterung von {@link EtkDataPool}).
     *
     * @return
     */
    public FrameworkImage getPreviewImage() {
        byte[] previewImageContent = getPreviewImageContent();
        if (previewImageContent != null) {
            return FrameworkImage.getFromByteArray(previewImageContent);
        } else {
            return null;
        }
    }

    /**
     * Liefert den Inhalt des iParts-spezifischen Vorschaubilds als byte-Array (Erweiterung von {@link EtkDataPool}).
     *
     * @return
     */
    public byte[] getPreviewImageContent() {
        return getFieldValueAsBlob(iPartsConst.FIELD_P_PREVIEW_DATA);
    }

    /**
     * Liefert den Bildtyp (Dateiendung) des iParts-spezifischen Vorschaubilds (Erweiterung von {@link EtkDataPool}).
     *
     * @return
     */
    public String getPreviewImageType() {
        return getFieldValue(iPartsConst.FIELD_P_PREVIEW_IMGTYPE);
    }

    /**
     * Liefert den Gültigkeitsbereich des Bildes bzgl. der Benutzer-Eigenschaften für PKW/Van und Truck/Bus (Erweiterung
     * von {@link EtkDataPool}).
     *
     * @return
     */
    public iPartsValidityScope getValidityScope() {
        return iPartsValidityScope.getValidityScope(getFieldValue(iPartsConst.FIELD_P_VALIDITY_SCOPE));
    }

    /**
     * Setzt den Gültigkeitsbereich des Bildes bzgl. der Benutzer-Eigenschaften für PKW/Van und Truck/Bus (Erweiterung
     * von {@link EtkDataPool}).
     *
     * @param scope
     */
    public void setValidityScope(iPartsValidityScope scope) {
        setFieldValue(iPartsConst.FIELD_P_VALIDITY_SCOPE, scope.getScopeKey(), DBActionOrigin.FROM_EDIT);
    }

    /**
     * Aktualisiert den Gültigkeitsbereich dieser Zeichnungsvariante basierend auf den Verwendungen dieser Zeichnung in Modulen
     * und freien SAs bzw. deren Produkte und AS-Produktklassen. Die Änderung wird nur in diesem {@link iPartsDataPool}-Objekt
     * vorgenommen, aber nicht direkt gespeichert.
     *
     * @return Berechneter Gültigkeitsbereich
     */
    public iPartsValidityScope updateValidityScope() {
        iPartsValidityScope oldValidityScope = getValidityScope();
        boolean isCarAndVan = oldValidityScope.isCarAndVan();
        boolean isTruckAndBus = oldValidityScope.isTruckAndBus();
        if (isCarAndVan && isTruckAndBus) { // Gültigkeitsbereich nie verkleinern -> PKW/Van UND Truck/Bus bleibt
            return oldValidityScope;
        }

        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.addFeld(new EtkDisplayField(TABLE_IMAGES, FIELD_I_TIFFNAME, false, false));
        displayFields.addFeld(new EtkDisplayField(TABLE_DA_SA_MODULES, FIELD_DSM_SA_NO, false, false));

        EtkProject project = getEtkProject();
        EtkDataImageList dataImageList = EtkDataObjectFactory.createDataImageList();
        dataImageList.searchSortAndFillWithJoin(project, null, displayFields,
                                                new String[]{ TableAndFieldName.make(TABLE_IMAGES, FIELD_I_IMAGES),
                                                              TableAndFieldName.make(TABLE_IMAGES, FIELD_I_PVER) },
                                                new String[]{ getFieldValue(FIELD_P_IMAGES), getFieldValue(FIELD_P_VER) },
                                                false, null, false, null,
                                                new EtkDataObjectList.JoinData(TABLE_DA_SA_MODULES,
                                                                               new String[]{ FIELD_I_TIFFNAME },
                                                                               new String[]{ FIELD_DSM_MODULE_NO },
                                                                               true, false));

        // Module und SAs von dieser Zeichnung ermitteln
        Set<String> modulesSet = new HashSet<>();
        Set<String> sasSet = new HashSet<>();
        for (EtkDataImage dataImage : dataImageList) {
            String saNumber = dataImage.getFieldValue(FIELD_DSM_SA_NO);
            if (saNumber.isEmpty()) {
                modulesSet.add(dataImage.getFieldValue(FIELD_I_TIFFNAME));
            } else {
                sasSet.add(saNumber);
            }
        }

        iPartsValidityScope validityScope = null;

        // Module und deren Produkte bzgl. Gültigkeitsbereich überprüfen
        for (String module : modulesSet) {
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, new AssemblyId(module, ""));
            if (assembly instanceof iPartsDataAssembly) {
                iPartsProductId productId = ((iPartsDataAssembly)assembly).getProductIdFromModuleUsage();
                if (productId != null) {
                    iPartsProduct product = iPartsProduct.getInstance(project, productId);
                    isCarAndVan |= product.isCarAndVanProduct();
                    isTruckAndBus |= product.isTruckAndBusProduct();
                    if (isCarAndVan && isTruckAndBus) {
                        validityScope = iPartsValidityScope.IPARTS;
                        break;
                    }
                }
            }
        }

        // SAs bzgl. Gültigkeitsbereich überprüfen
        if (validityScope == null) {
            for (String saNumber : sasSet) {
                iPartsSA sa = iPartsSA.getInstance(project, new iPartsSAId(saNumber));
                isCarAndVan |= sa.isCarAndVanSA(project);
                isTruckAndBus |= sa.isTruckAndBusSA(project);
                if (isCarAndVan && isTruckAndBus) {
                    validityScope = iPartsValidityScope.IPARTS;
                    break;
                }
            }
        }

        if (validityScope == null) { // Gültigkeitsbereich für PKW/Van UND Truck/Bus wird oben in den Schleifen bereits berücksichtigt
            if (isCarAndVan) {
                validityScope = iPartsValidityScope.IPARTS_MB;
            } else if (isTruckAndBus) {
                validityScope = iPartsValidityScope.IPARTS_TRUCK;
            } else {
                validityScope = iPartsValidityScope.UNUSED;
            }
        }

        // Neuen Gültigkeitsbereich setzen (da isCarAndVan und isTruckAndBus mit dem aktuellen Gültigkeitsbereich initialisiert
        // werden, kann sich der Gültigkeitsbereich nie verkleinern; Sonderfall für noch nie gesetzten Gültigkeitsbereich,
        // weil dieser als Fallback auch UNUSED zurückliefert)
        if ((oldValidityScope != validityScope) || getFieldValue(iPartsConst.FIELD_P_VALIDITY_SCOPE).isEmpty()) {
            setFieldValue(FIELD_P_VALIDITY_SCOPE, validityScope.getScopeKey(), DBActionOrigin.FROM_EDIT);
        }

        return validityScope;
    }
}