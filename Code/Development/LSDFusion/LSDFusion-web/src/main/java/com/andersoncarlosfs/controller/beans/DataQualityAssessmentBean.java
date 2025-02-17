/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.andersoncarlosfs.controller.beans;

import com.andersoncarlosfs.annotations.scopes.ApplicationScope;
import com.andersoncarlosfs.data.model.assessments.DataFusionAssessment;
import com.andersoncarlosfs.data.model.assessments.DataQualityAssessment;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Anderson Carlos Ferreira da Silva
 */
@ApplicationScope
public class DataQualityAssessmentBean {

    private DataFusionAssessment assessment;
    private Collection<Collection<RDFNode>> classes;
    private Collection<RDFNode> equivalenceClass;
    private Collection<RDFNode> equivalenceClassProperties;
    private RDFNode value;
    private DataQualityAssessment details;
    private StreamedContent file;

    public DataQualityAssessmentBean() {
    }

    /**
     *
     * @return the assessment
     */
    public DataFusionAssessment getAssessment() {
        return assessment;
    }

    /**
     *
     * @param assessment the assessment to set
     */
    public void setAssessment(DataFusionAssessment assessment) throws Exception {

        this.assessment = assessment;

        setClasses();

        setFile();

    }

    /**
     *
     */
    private void setClasses() {

        classes = new ArrayList<>();

        for (Collection<RDFNode> collection : assessment.getComputedDataQualityAssessment().keySet()) {
            if (collection.size() > 1) {
                classes.add(collection);
            }
        }

    }

    /**
     *
     */
    public Collection<Collection<RDFNode>> getClasses() {
        return classes;
    }

    /**
     *
     */
    public Collection<RDFNode> getEquivalenceClass() {
        return equivalenceClass;
    }

    /**
     *
     */
    public void setEquivalenceClass(Collection<RDFNode> equivalenceClass) {
        this.equivalenceClass = equivalenceClass;
    }

    /**
     *
     */
    public Collection<Collection<RDFNode>> getProperties() {
        return assessment.getComputedDataQualityAssessment().get(equivalenceClass).keySet();
    }

    /**
     *
     */
    public Collection<RDFNode> getEquivalenceClassProperties() {
        return equivalenceClassProperties;
    }

    /**
     *
     */
    public void setEquivalenceClassProperties(Collection<RDFNode> equivalenceClassProperties) {
        this.equivalenceClassProperties = equivalenceClassProperties;
    }

    /**
     *
     */
    public Set<Map.Entry<RDFNode, DataQualityAssessment>> getEntrySet() {
        return assessment.getComputedDataQualityAssessment().get(equivalenceClass).get(equivalenceClassProperties).entrySet();
    }

    /**
     *
     */
    public Collection<RDFNode> getValues() {
        return assessment.getComputedDataQualityAssessment().get(equivalenceClass).get(equivalenceClassProperties).keySet();
    }

    /**
     *
     */
    public RDFNode getValue() {
        return value;
    }

    /**
     *
     */
    public void setValue(RDFNode value) {

        this.value = value;

        this.details = assessment.getComputedDataQualityAssessment().get(equivalenceClass).get(equivalenceClassProperties).get(value);

    }

    /**
     *
     */
    public DataQualityAssessment getDetails() {
        return details;
    }

    /**
     *
     */
    private void setFile() throws Exception {

        final String root = "http://www.fusion.com/properties/";
        
        Model model = ModelFactory.createDefaultModel();

        Property equivalentPredicate = model.createProperty(root + "equivalentSubject");
        Property equivalentProperty = model.createProperty(root + "equivalentPredicate");
        Property valueProperty = model.createProperty(root + "value");
        Property frequencyProperty = model.createProperty(root + "frequency");
        Property homogeneityProperty = model.createProperty(root + "homogeneity");
        Property reliabilityProperty = model.createProperty(root + "reliability");
        Property freshnessProperty = model.createProperty(root + "freshness");
        //Property trustinessProperty = model.createProperty(root + "trustiness");
        Property scoreProperty = model.createProperty(root + "score");
        Property morePrecise = model.createProperty(root + "morePrecise");

        for (Map.Entry<Collection<RDFNode>, Map<Collection<RDFNode>, Map<RDFNode, DataQualityAssessment>>> classes : assessment.getComputedDataQualityAssessment().entrySet()) {

            Queue<RDFNode> classesNodes = new LinkedList<RDFNode>(classes.getKey());

            Resource representativeResource = (Resource) classesNodes.poll();

            for (RDFNode node : classesNodes) {
                model.add(representativeResource, equivalentPredicate, node);
            }

            for (Map.Entry<Collection<RDFNode>, Map<RDFNode, DataQualityAssessment>> properties : classes.getValue().entrySet()) {

                Queue<RDFNode> propertiesNodes = new LinkedList<RDFNode>(properties.getKey());

                Property representativeProperty = (Property) propertiesNodes.poll();

                for (RDFNode node : propertiesNodes) {
                    model.add(representativeProperty, equivalentProperty, node);
                }

                for (Map.Entry<RDFNode, DataQualityAssessment> values : properties.getValue().entrySet()) {

                    Resource representativeValue = model.createResource();

                    DataQualityAssessment assessment = values.getValue();

                    model.add(representativeResource, representativeProperty, representativeValue);
                    model.add(representativeValue, valueProperty, values.getKey());
                    model.add(representativeValue, frequencyProperty, assessment.getFrequency().toString());
                    model.add(representativeValue, homogeneityProperty, assessment.getHomogeneity().toString());
                    model.add(representativeValue, reliabilityProperty, assessment.getReliability().toString());
                    model.add(representativeValue, freshnessProperty, assessment.getFreshness().toString());
                    //model.add(representativeValue, trustinessProperty, assessment.getTrustiness().toString());
                    model.add(representativeValue, scoreProperty, assessment.getScore().toString());

                    for (RDFNode lessPreciseNode : assessment.getMorePrecise()) {

                        model.add(representativeValue, morePrecise, lessPreciseNode);

                    }

                }

            }

        }

        Path path = Files.createTempFile(null, ".nt");

        RDFDataMgr.write(Files.newOutputStream(path), model, Lang.NTRIPLES);

        InputStream stream = Files.newInputStream(path);

        file = new DefaultStreamedContent(stream, "application/n-triples", "fusion.nt");

    }

    /**
     *
     */
    public StreamedContent getFile() {
        return file;
    }

    /**
     *
     */
    public void reset() {

        assessment = null;
        equivalenceClass = null;
        equivalenceClassProperties = null;
        details = null;

    }

}
