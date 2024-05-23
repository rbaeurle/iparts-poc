package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBDataObjectAttributes;

import java.util.Collections;
import java.util.List;

/**
 * Cache-Objekt für den {@link iPartsPRIMUSReplacementsCache}.
 */
public class iPartsPRIMUSReplacementCacheObject {

    @JsonProperty
    private String predecessorPartNo;
    @JsonProperty
    private String successorPartNo;
    @JsonProperty
    private String codeForward;
    @JsonProperty
    private String codeBackward;
    @JsonProperty
    private String infoType;
    @JsonProperty
    protected List<iPartsPRIMUSIncludePartCacheObject> includeParts;

    public iPartsPRIMUSReplacementCacheObject() {
    }

    protected iPartsPRIMUSReplacementCacheObject(DBDataObjectAttributes primusReplacePartAttributes) {
        this.predecessorPartNo = primusReplacePartAttributes.getFieldValue(iPartsConst.FIELD_PRP_PART_NO);
        this.successorPartNo = primusReplacePartAttributes.getFieldValue(iPartsConst.FIELD_PRP_SUCCESSOR_PARTNO);
        this.codeForward = primusReplacePartAttributes.getFieldValue(iPartsConst.FIELD_PRP_PSS_CODE_FORWARD);
        this.codeBackward = primusReplacePartAttributes.getFieldValue(iPartsConst.FIELD_PRP_PSS_CODE_BACK);
        this.infoType = primusReplacePartAttributes.getFieldValue(iPartsConst.FIELD_PRP_PSS_INFO_TYPE);
    }

    public String getPredecessorPartNo() {
        return predecessorPartNo;
    }

    public String getSuccessorPartNo() {
        return successorPartNo;
    }

    public String getCodeForward() {
        return codeForward;
    }

    public String getCodeBackward() {
        return codeBackward;
    }

    public String getInfoType() {
        return infoType;
    }

    public List<iPartsPRIMUSIncludePartCacheObject> getIncludeParts() {
        if (includeParts != null) {
            return Collections.unmodifiableList(includeParts);
        } else {
            return null;
        }
    }
}