/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractSourceKey;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;

import java.util.Date;

/**
 * Repräsentiert das "SourceKeyDialog" Element in der Transfer XML
 */
public class iPartsXMLSourceKeyDialog extends AbstractSourceKey {

    private iPartsSeriesId seriesId;
    private String hm;
    private String m;
    private String sm;
    private String pos;
    private String posV;
    // Neu ab DAIMLER-15068
    private String ww;          // ("optionalPartIndicator"),
    private String etz;         // ("partsCounter"),
    private String aa;          // Ausführungsart ("productVersion")
    private Date kemDateFrom;   // ("ecoValidFrom")

    public iPartsXMLSourceKeyDialog(iPartsDialogBCTEPrimaryKey bctePrimaryKey) {
        this.seriesId = bctePrimaryKey.getHmMSmId().getSeriesId();
        this.hm = bctePrimaryKey.getHmMSmId().getHm();
        this.m = bctePrimaryKey.getHmMSmId().getM();
        this.sm = bctePrimaryKey.getHmMSmId().getSm();
        this.pos = bctePrimaryKey.getPosE();
        this.posV = bctePrimaryKey.getPosV();
        this.ww = bctePrimaryKey.getWW();
        this.etz = bctePrimaryKey.getET();
        this.aa = bctePrimaryKey.getAA();
        this.kemDateFrom = XMLImportExportDateHelper.getDateFromDBDateTime(bctePrimaryKey.getSData());
    }

    public iPartsXMLSourceKeyDialog(DwXmlNode node) {
        super(node);
    }

    public iPartsSeriesId getSeriesId() {
        return seriesId;
    }

    public String getHm() {
        return hm;
    }

    public String getM() {
        return m;
    }

    public String getSm() {
        return sm;
    }

    public String getPos() {
        return pos;
    }

    public String getPosV() {
        return posV;
    }

    public HmMSmId getAsHmMSmId() {
        return new HmMSmId(getSeriesId().getSeriesNumber(), hm, m, sm);
    }

    public String getWw() {
        return ww;
    }

    public String getEtz() {
        return etz;
    }

    public String getAa() {
        return aa;
    }

    public Date getKemDateFrom() {
        return kemDateFrom;
    }

    @Override
    protected void fillSourceKeyNode(DwXmlNode sourceKeyNode) {
        addIfValid(sourceKeyNode, CMO_MODEL_SERIES, getSeriesId().getSeriesNumber());
        addIfValid(sourceKeyNode, CMO_MAIN_MODULE, getHm());
        addIfValid(sourceKeyNode, CMO_MODULE, getM());
        addIfValid(sourceKeyNode, CMO_SUBMODULE, getSm());
        addIfValid(sourceKeyNode, CMO_POS, getPos());
        addIfValid(sourceKeyNode, CMO_POS_VARIANT, getPosV());
        addIfValid(sourceKeyNode, CMO_OPT_PART_INDICATOR, getWw());
        addIfValid(sourceKeyNode, CMO_PARTS_COUNTER, getEtz());
        addIfValid(sourceKeyNode, CMO_PRODUCT_VERSION, getAa());
        addIfValid(sourceKeyNode, CMO_KEM_DATE_FROM, XMLImportExportDateHelper.getISOFormattedDateTimeAsString(getKemDateFrom()));
    }

    @Override
    protected iPartsTransferNodeTypes getSourceKeyNodeType() {
        return iPartsTransferNodeTypes.SOURCE_KEY_DIALOG;
    }

    private void addIfValid(DwXmlNode sourceKeyNode, String attributeName, String value) {
        if (StrUtils.isValid(value)) {
            sourceKeyNode.setAttribute(attributeName, value);
        }
    }

    @Override
    protected void fillObjectFromNode(DwXmlNode node) {
        seriesId = new iPartsSeriesId(node.getAttribute(iPartsTransferConst.CMO_MODEL_SERIES));
        hm = node.getAttribute(CMO_MAIN_MODULE);
        m = node.getAttribute(CMO_MODULE);
        sm = node.getAttribute(CMO_SUBMODULE);
        pos = node.getAttribute(CMO_POS);
        posV = node.getAttribute(CMO_POS_VARIANT);
        ww = node.getAttribute(CMO_OPT_PART_INDICATOR);
        etz = node.getAttribute(CMO_PARTS_COUNTER);
        aa = node.getAttribute(CMO_PRODUCT_VERSION);
        kemDateFrom = XMLImportExportDateHelper.getISOFormattedDateTimeAsDate(node.getAttribute(CMO_KEM_DATE_FROM));
    }
}
