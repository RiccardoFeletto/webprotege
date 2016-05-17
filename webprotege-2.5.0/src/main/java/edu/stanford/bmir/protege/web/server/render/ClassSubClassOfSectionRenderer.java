package edu.stanford.bmir.protege.web.server.render;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.*;

/**
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 24/02/2014
 */
public class ClassSubClassOfSectionRenderer extends AbstractOWLAxiomItemSectionRenderer<OWLClass, OWLSubClassOfAxiom, OWLClassExpression> {

    @Override
    public ManchesterOWLSyntax getSection() {
        return ManchesterOWLSyntax.SUBCLASS_OF;
    }

    @Override
    protected Set<OWLSubClassOfAxiom> getAxiomsInOntology(OWLClass subject, OWLOntology ontology) {
        return ontology.getSubClassAxiomsForSubClass(subject);
    }

    @Override
    public List<OWLClassExpression> getRenderablesForItem(OWLClass subject,
                                                          OWLSubClassOfAxiom item,
                                                          OWLOntology ontology) {
        return Arrays.asList(item.getSuperClass());
    }

    @Override
    public String getSeparatorAfter(int renderableIndex, List<OWLClassExpression> renderables) {
        return "";
    }
}
