/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

import java.util.List;

/**
 * DTO eines Arbeitsauftrags aus NutzDok, für KEMs oder SAAs.
 */
public class iPartsWSWorkBasketItem extends WSRequestTransferObject {

    public enum TYPE {KEM, SAA}

    // Gemeinsame Werte für KEM und SAA
    private String id;
    private String type;
    private String groupId;
    private boolean toFrom;
    private boolean flash;
    private boolean evo;
    private boolean priority;
    private boolean tc;
    private String ver;
    private String aus;
    private String ets;
    private String lastUser;
    private String docStartTs;
    private String manualStartTs;
    private List<iPartsWSWorkBasketItemAnnotation> annotation; // Bemerkungstexte

    // KEM spezifisch
    private String docUser;
    private String docTeam;
    private String marker;
    private boolean simplified;
    private boolean paper;
    private String pemNo;
    private String pemDate;
    private String pemStatus;

    // SAA spezifisch
    private Integer planNumber;
    private String beginUsageTs;

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribValid(path, "id", id);
        checkAttribEnumValid(path, "type", type, TYPE.class);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return null;  // Der NutzDok Webservice hat keinen JSON-Response-Cache
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    @JsonIgnore
    public TYPE getTypeAsEnum() {
        return TYPE.valueOf(type);
    }

    public void setType(String type) {
        this.type = type.toUpperCase();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isToFrom() {
        return toFrom;
    }

    public void setToFrom(boolean toFrom) {
        this.toFrom = toFrom;
    }

    public boolean isFlash() {
        return flash;
    }

    public void setFlash(boolean flash) {
        this.flash = flash;
    }

    public boolean isEvo() {
        return evo;
    }

    public void setEvo(boolean evo) {
        this.evo = evo;
    }

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public boolean isTc() {
        return tc;
    }

    public void setTc(boolean tc) {
        this.tc = tc;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getAus() {
        return aus;
    }

    public void setAus(String aus) {
        this.aus = aus;
    }

    public String getEts() {
        return ets;
    }

    public void setEts(String ets) {
        this.ets = ets;
    }

    public String getLastUser() {
        return lastUser;
    }

    public void setLastUser(String lastUser) {
        this.lastUser = lastUser;
    }

    public String getDocStartTs() {
        return docStartTs;
    }

    public void setDocStartTs(String docStartTs) {
        this.docStartTs = docStartTs;
    }

    public String getManualStartTs() {
        return manualStartTs;
    }

    public void setManualStartTs(String manualStartTs) {
        this.manualStartTs = manualStartTs;
    }

    public String getDocUser() {
        return docUser;
    }

    public void setDocUser(String docUser) {
        this.docUser = docUser;
    }

    public String getDocTeam() {
        return docTeam;
    }

    public void setDocTeam(String docTeam) {
        this.docTeam = docTeam;
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public boolean isSimplified() {
        return simplified;
    }

    public void setSimplified(boolean simplified) {
        this.simplified = simplified;
    }

    public boolean isPaper() {
        return paper;
    }

    public void setPaper(boolean paper) {
        this.paper = paper;
    }

    public String getPemNo() {
        return pemNo;
    }

    public void setPemNo(String pemNo) {
        this.pemNo = pemNo;
    }

    public String getPemDate() {
        return pemDate;
    }

    public void setPemDate(String pemDate) {
        this.pemDate = pemDate;
    }

    public String getPemStatus() {
        return pemStatus;
    }

    public void setPemStatus(String pemStatus) {
        this.pemStatus = pemStatus;
    }

    public Integer getPlanNumber() {
        return planNumber;
    }

    public void setPlanNumber(Integer planNumber) {
        this.planNumber = planNumber;
    }

    public String getBeginUsageTs() {
        return beginUsageTs;
    }

    public void setBeginUsageTs(String beginUsageTs) {
        this.beginUsageTs = beginUsageTs;
    }

    public List<iPartsWSWorkBasketItemAnnotation> getAnnotation() {
        return annotation;
    }

    public void setAnnotation(List<iPartsWSWorkBasketItemAnnotation> annotation) {
        this.annotation = annotation;
    }
}
