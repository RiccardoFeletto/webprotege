/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.bmir.protege.web.server.projectconvert;

import edu.stanford.bmir.protege.web.server.filedownload.DownloadFormat;
import edu.stanford.bmir.protege.web.server.owlapi.OWLAPIProjectDocumentStore;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.revision.RevisionNumber;


/**
 *
 * @author antonio
 */
public class OWLPIProjectConverter {
    
    private ProjectId projectId;
    
    private RevisionNumber revision;
    
    private DownloadFormat format;
    
    public OWLPIProjectConverter(ProjectId projectId, RevisionNumber revision, DownloadFormat format){
        this.projectId=projectId;
        this.revision=revision;
        this.format=format;
    }
//funzione per la conversione dell'ontologia in RDF/XML che provvede alla conversine passando il formato(che sarà nel nostro caso sempre RDF/XML) e il numero di revisione
    public String convertontology(){
        String convertedontology="";
        try{
            OWLAPIProjectDocumentStore documentstore = OWLAPIProjectDocumentStore.getProjectDocumentStore(projectId);
          
                if(revision.isHead()) {
                convertedontology=documentstore.convertproject(format,null); 
            }
            else {
                convertedontology=documentstore.convertproject(format,revision);
            }
          
        }
            catch(Exception ex)
                    {
                     ex.printStackTrace();
                    }
        return convertedontology;
    }
}
