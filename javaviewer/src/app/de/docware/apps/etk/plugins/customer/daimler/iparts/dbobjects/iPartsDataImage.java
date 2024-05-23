/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImageList;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPool;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPoolVariants;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsDataPool;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLStringConvert;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Erweiterung von {@link EtkDataImage} um iParts-spezifische Methoden und Daten.
 */
public class iPartsDataImage extends EtkDataImage {

    private AssemblyId assemblyId;

    /**
     * Aktualisiert den Gültigkeitsbereich aller Zeichnungsvarianten der übergebenen Zeichnung basierend auf den jeweiligen
     * Verwendungen dieser Zeichnungen in Modulen und freien SAs bzw. deren Produkte und AS-Produktklassen. Die Änderungen
     * werden direkt in der DB gespeichert und zusätzlich mit {@link de.docware.framework.modules.db.serialization.SerializedDBDataObjectState#COMMITTED}
     * falls ein {@link de.docware.apps.etk.base.db.AbstractRevisionChangeSet} aktiv ist.
     *
     * @return Veränderte Zeichnungsvarianten dieser Zeichnung
     */
    public static void updateAndSaveValidityScopeForImages(List<EtkDataImage> imagesList, EtkProject project) {
        GenericEtkDataObjectList modifiedDataPoolList = new GenericEtkDataObjectList();
        for (EtkDataImage dataImage : imagesList) {
            if (dataImage instanceof iPartsDataImage) {
                modifiedDataPoolList.addAll(((iPartsDataImage)dataImage).updateValidityScope(), DBActionOrigin.FROM_EDIT);
            }
        }

        EtkRevisionsHelper revisionsHelper = project.getRevisionsHelper();
        if ((revisionsHelper != null) && revisionsHelper.isRevisionChangeSetActiveForEdit()) {
            revisionsHelper.addDataObjectListToActiveChangeSetForEditCommitted(modifiedDataPoolList);
        }
        modifiedDataPoolList.saveToDB(project);
    }

    public iPartsDataImage(EtkProject project, AssemblyId assemblyId, String iBlatt, String imagePoolNo, String imagePoolVer) {
        super(project, assemblyId, iBlatt, imagePoolNo, imagePoolVer);
        this.assemblyId = assemblyId;
    }

    public AssemblyId getAssemblyId() {
        return assemblyId;
    }

    /**
     * Aktualisiert den Gültigkeitsbereich aller Zeichnungsvarianten dieser Zeichnung basierend auf den Verwendungen dieser
     * Zeichnung in Modulen und freien SAs bzw. deren Produkte und AS-Produktklassen. Die Änderung wird nur in den {@link iPartsDataPool}-Objekten
     * vorgenommen, aber nicht direkt gespeichert.
     *
     * @return Bzgl. dem Gültigkeitsbereich veränderte Zeichnungsvarianten dieser Zeichnung
     */
    public List<iPartsDataPool> updateValidityScope() {
        // Gültigkeitsbereich aller Zeichnungsvarianten aktualisieren
        EtkDataPoolVariants dataPoolVariants = getUnfilteredPoolVariants();
        List<iPartsDataPool> dataPoolList = new DwList<>();
        for (EtkDataPool dataPool : dataPoolVariants) {
            if (dataPool instanceof iPartsDataPool) {
                iPartsDataPool iPartsPool = (iPartsDataPool)dataPool;
                iPartsPool.updateValidityScope();
                if (iPartsPool.isModified()) {
                    dataPoolList.add(iPartsPool);
                }
            }
        }
        return dataPoolList;
    }

    @Override
    public boolean loadVirtualField(String attributeName) {
        if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_IS_MULTIPLE_USED)) {
            // Wird die Zeichnung (OHNE Berücksichtigung der Zeichnungsversion) in mehreren Modulen verwendet?
            Set<String> moduleNumbers = getMultipleUseModuleNumbers(false);
            attributes.addField(attributeName, SQLStringConvert.booleanToPPString(moduleNumbers.size() > 1), true, DBActionOrigin.FROM_DB);
        }
        return false;
    }

    /**
     * Liefert alle Modulnummern zurück, die diese Zeichnung verwenden.
     *
     * @param withPSK true: PSK Module werden nicht aussortiert
     * @return
     */
    public Set<String> getMultipleUseModuleNumbers(boolean withPSK) {
        EtkDataImageList dataImageList = EtkDataObjectFactory.createDataImageList();
        dataImageList.loadImagesForImageNumber(getEtkProject(), getImagePoolNo(), DBDataObjectList.LoadType.ONLY_IDS);
        Set<String> moduleNumbers;
        if (withPSK) {
            moduleNumbers = dataImageList.getAsList().stream()
                    .map(dataImage -> dataImage.getAsId().getITiffName())
                    .collect(Collectors.toSet());
        } else {
            moduleNumbers = new TreeSet<>();
            // PSK-Module entfernen
            for (EtkDataImage dataImage : dataImageList) {
                String moduleNumber = dataImage.getAsId().getITiffName();
                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getEtkProject(), new AssemblyId(moduleNumber, ""));
                if (assembly instanceof iPartsDataAssembly) {
                    if (!((iPartsDataAssembly)assembly).isPSKAssembly()) {
                        moduleNumbers.add(moduleNumber);
                    }
                }
            }
        }
        return moduleNumbers;
    }
}
