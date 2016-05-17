/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.bmir.protege.web.client.ui.projectmanager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.revision.RevisionNumber;
/**
 *
 * @author antonio
 */
public class ConvertAndReturnRequestHandlerImpl implements ConvertAndReturnRequestHandler{
    @Override
    public void handleProjectConvertRequest(final ProjectId projectId){
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
            }

            @Override
            public void onSuccess() {
               
                        doConversion(projectId);   //provvede ad effettuare la conversione passando l'ID del progetto
                        
                    }
                });
            }
       
    private void doConversion(ProjectId projectId){
        RevisionNumber head=RevisionNumber.getHeadRevisionNumber();
        ProjectRevisionConverter converter=new ProjectRevisionConverter(projectId,head);
        converter.convert();
    }
}

     