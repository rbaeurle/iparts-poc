package de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.AbstractiPartsNode;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

public class MBSStructureNode extends AbstractiPartsNode<MBSStructureNodes, MBSStructureNode, MBSStructureType> implements iPartsConst {

    private boolean textsLoaded;
    private boolean subTextsLoaded;

    public MBSStructureNode(MBSStructureType type, String number, MBSStructureNode parent) {
        super(type, number, parent, new MBSStructureNodes());
    }

    @Override
    public MBSStructureId getId() {
        if (getType() == MBSStructureType.CON_GROUP) {
            return new MBSStructureId(getParent().getNumber(), getNumber());
        } else {
            return new MBSStructureId(getNumber(), "");
        }
    }

    /**
     * Liefert die Benennung des Knotens. Sollte die Benennung noch nicht existieren, wird sie aus der DB geladen
     *
     * @param project
     * @return
     */
    public EtkMultiSprache getTitle(EtkProject project) {
        EtkMultiSprache title = getTitle();
        if (!isTextsLoaded() && title.isEmpty()) {
            // Wurde die Benennung noch nicht geladen, dann wird sie jetzt aus der DB geladen. Bei GS/SAA/Teilebennungen
            // müsste die Benennung schon existieren, weil sie beim Laden des Knotens als Batch geladen wird
            String mbsSnrNumber = getId().isConGroupNode() ? getId().getConGroup() : getId().getListNumber();
            if (StrUtils.isValid(mbsSnrNumber)) {
                EtkMultiSprache newTitle = null;
                if (mbsSnrNumber.startsWith(SAA_NUMBER_PREFIX) || mbsSnrNumber.startsWith(BASE_LIST_NUMBER_PREFIX)) {
                    iPartsDataSaa saaBaseListObject = new iPartsDataSaa(project, new iPartsSaaId(mbsSnrNumber));
                    if (saaBaseListObject.existsInDB()) {
                        newTitle = saaBaseListObject.getFieldValueAsMultiLanguage(FIELD_DS_CONST_DESC);
                    }
                } else {
                    EtkDataPart dataPart = EtkDataObjectFactory.createDataPart(project, new iPartsPartId(mbsSnrNumber, ""));
                    if (dataPart.existsInDB()) {
                        newTitle = dataPart.getFieldValueAsMultiLanguage(FIELD_M_TEXTNR);
                    }
                }
                setTextLoaded(true);
                if (newTitle != null) {
                    setTitle(newTitle);
                    return newTitle;
                }
            }
        }
        return title;
    }

    public void setTextLoaded(boolean textLoaded) {
        this.textsLoaded = textLoaded;
    }

    public boolean isTextsLoaded() {
        return textsLoaded;
    }

    public boolean isSubTextsLoaded() {
        return subTextsLoaded;
    }

    public void setSubTextsLoaded(boolean subTextsLoaded) {
        this.subTextsLoaded = subTextsLoaded;
    }
}
