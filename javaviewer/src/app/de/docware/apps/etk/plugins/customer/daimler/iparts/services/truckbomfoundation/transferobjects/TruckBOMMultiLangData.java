/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationLanguageDefs;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.List;

/**
 * Enthält die Standard-Informationen für alle Daten aus TruckBOM.foundation, die mehrsprachige Texte enthalten
 */
public class TruckBOMMultiLangData implements RESTfulTransferObjectInterface {

    private List<TruckBOMText> nomenclature; // Benennungen
    private List<TruckBOMText> remark;       // Bemerkungen

    public TruckBOMMultiLangData() {
    }

    public List<TruckBOMText> getNomenclature() {
        return nomenclature;
    }

    public void setNomenclature(List<TruckBOMText> nomenclature) {
        this.nomenclature = nomenclature;
    }

    public List<TruckBOMText> getRemark() {
        return remark;
    }

    public void setRemark(List<TruckBOMText> remark) {
        this.remark = remark;
    }

    /**
     * Konvertiert die {@link TruckBOMText} Objekte in ein {@link EtkMultiSprache} Objekt
     *
     * @param texts
     * @return
     */
    @JsonIgnore
    public static EtkMultiSprache convertTextsToMultiLang(List<TruckBOMText> texts) {
        if (texts == null) {
            return null;
        }
        EtkMultiSprache multiLang = new EtkMultiSprache();
        texts.forEach(text -> {
            iPartsTruckBOMFoundationLanguageDefs languageDef = iPartsTruckBOMFoundationLanguageDefs.getType(text.getLanguage());

            if (languageDef != iPartsTruckBOMFoundationLanguageDefs.TBF_UNKNOWN) {
                String content = text.getContent();
                multiLang.setText(languageDef.getDbValue(), (content == null) ? "" : content.trim());
            }
        });
        return multiLang;
    }

    @JsonIgnore
    public EtkMultiSprache getNomenclatureAsMultiLangObject() {
        return convertTextsToMultiLang(getNomenclature());
    }

    @JsonIgnore
    public EtkMultiSprache getRemarkAsMultiLangObject() {
        return convertTextsToMultiLang(getRemark());
    }
}
