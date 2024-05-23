/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Teil-Index eines ElasticSearch-Index
 */
public class ElasticSearchIndex implements RESTfulTransferObjectInterface {

    private String branch;
    private String series;
    private String modelid;
    private String aggtype;
    private String cg;
    private String cgtext;
    private String cgtext_de;
    private String csg;
    private String csgtext;
    private String csgtext_de;
    private String prod;
    private String partno;
    private String calloutid;
    private long pos;
    private String partname;
    private String partname_de;
    private String partdesc;
    private String partdesc_de;
    private String matdesc;
    private String matdesc_de;
    private List<ElasticSearchCodes> codes;
    private Set<String> saacodes;
    private List<String> assemblylocations;
    private String steering;
    private Set<String> einpas;
    private List<String> spk;
    // DAIMLER-16208
    private List<String> successor;
    private String bomkey;

    public ElasticSearchIndex() {
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getModelid() {
        return modelid;
    }

    public void setModelid(String modelid) {
        this.modelid = modelid;
    }

    public String getAggtype() {
        return aggtype;
    }

    public void setAggtype(String aggtype) {
        this.aggtype = aggtype;
    }

    public String getCg() {
        return cg;
    }

    public void setCg(String cg) {
        this.cg = cg;
    }

    public String getCgtext() {
        return cgtext;
    }

    public void setCgtext(String cgtext) {
        this.cgtext = cgtext;
    }

    public String getCgtext_de() {
        return cgtext_de;
    }

    public void setCgtext_de(String cgtext_de) {
        this.cgtext_de = cgtext_de;
    }

    public String getCsg() {
        return csg;
    }

    public void setCsg(String csg) {
        this.csg = csg;
    }

    public String getCsgtext() {
        return csgtext;
    }

    public void setCsgtext(String csgtext) {
        this.csgtext = csgtext;
    }

    public String getCsgtext_de() {
        return csgtext_de;
    }

    public void setCsgtext_de(String csgtext_de) {
        this.csgtext_de = csgtext_de;
    }

    public String getProd() {
        return prod;
    }

    public void setProd(String prod) {
        this.prod = prod;
    }

    public String getPartno() {
        return partno;
    }

    public void setPartno(String partno) {
        this.partno = partno;
    }

    public String getCalloutid() {
        return calloutid;
    }

    public void setCalloutid(String calloutid) {
        this.calloutid = calloutid;
    }

    public long getPos() {
        return pos;
    }

    public void setPos(long pos) {
        this.pos = pos;
    }

    public String getPartname() {
        return partname;
    }

    public void setPartname(String partname) {
        this.partname = partname;
    }

    public String getPartname_de() {
        return partname_de;
    }

    public void setPartname_de(String partname_de) {
        this.partname_de = partname_de;
    }

    public String getPartdesc() {
        return partdesc;
    }

    public void setPartdesc(String partdesc) {
        this.partdesc = partdesc;
    }

    public String getPartdesc_de() {
        return partdesc_de;
    }

    public void setPartdesc_de(String partdesc_de) {
        this.partdesc_de = partdesc_de;
    }

    public String getMatdesc() {
        return matdesc;
    }

    public void setMatdesc(String matdesc) {
        this.matdesc = matdesc;
    }

    public String getMatdesc_de() {
        return matdesc_de;
    }

    public void setMatdesc_de(String matdesc_de) {
        this.matdesc_de = matdesc_de;
    }

    public List<ElasticSearchCodes> getCodes() {
        return codes;
    }

    public void setCodes(List<ElasticSearchCodes> codes) {
        this.codes = codes;
    }

    public List<String> getAssemblylocations() {
        return assemblylocations;
    }

    public void setAssemblylocations(List<String> assemblylocations) {
        this.assemblylocations = assemblylocations;
    }

    public Set<String> getEinpas() {
        return einpas;
    }

    public void setEinpas(Set<String> einpas) {
        this.einpas = einpas;
    }

    public Set<String> getSaacodes() {
        return saacodes;
    }

    public void setSaacodes(Set<String> saacodes) {
        this.saacodes = saacodes;
    }

    public String getSteering() {
        return steering;
    }

    public void setSteering(String steering) {
        this.steering = steering;
    }

    public List<String> getSpk() {
        return spk;
    }

    public void setSpk(List<String> spk) {
        this.spk = spk;
    }

    public List<String> getSuccessor() {
        return successor;
    }

    public void setSuccessor(List<String> successor) {
        this.successor = successor;
    }

    public String getBomkey() {
        return bomkey;
    }

    public void setBomkey(String bomkey) {
        this.bomkey = bomkey;
    }

    @JsonIgnore
    public void createEmptyCodes() {
        codes = new ArrayList<>();
        ElasticSearchCodes elasticSearchCodes = new ElasticSearchCodes();
        elasticSearchCodes.setMust(new HashSet<>());
        elasticSearchCodes.setNot(new HashSet<>());
        codes.add(elasticSearchCodes);
    }
}
