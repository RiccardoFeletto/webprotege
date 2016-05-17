/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.bmir.protege.web.client.ui.projectmanager;

import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.revision.RevisionNumber;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.URL;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;

/**
 *
 * @author antonio
 */
public class ProjectRevisionConverter {
    private final ProjectId projectid;
    
    private final RevisionNumber revisionnumber;
    
    
    public ProjectRevisionConverter(ProjectId projectid, RevisionNumber revisionnumber){
        this.projectid = checkNotNull(projectid);
        this.revisionnumber= checkNotNull(revisionnumber);
    }
    
    public void convert(){
        String encodedProjectName = URL.encode(projectid.getId());
        String requestData="";
        String baseURL = GWT.getHostPageBaseURL();
        String convertURL = baseURL + "convert?ontology=" + encodedProjectName + "&revision=" + revisionnumber.getValue();
        boolean opened=openedPage();
        //controllo se la pagina è stata aperta da un'altra pagina, in modo da avere un riferimento a cui inviare l'ontologia in RDF/XML
        if(opened==true)
        { //invio la richiesta alla servlet per ottenere l'ontologia convertita!!!
        RequestBuilder request= new RequestBuilder(RequestBuilder.GET,convertURL);
        try
        {
            request.sendRequest(requestData.toString(), new RequestCallback()   //requestData vuoti poichè i parametri di interesse sono già specificati nell'URL
            {
                 public void onError(Request request, Throwable e) 
                {
                    Window.alert(e.getMessage());
                }
                public void onResponseReceived(Request request, Response response)
            {
                    if (200 == response.getStatusCode())            //controllo che la response sia stata ricevuta in maniera corretta (stato 200)
                    {
                        String convertedProject=new String(response.getText());
                        returnTOtrill(convertedProject);   //funzione javascript che invia la stringa ottenuta a TRILL on SWISH
                    } else {
                        Window.alert("Received HTTP status code other than 200 : "+ response.getStatusText());
                    }
            }
            });
            }
        catch (RequestException e) {
            // Couldn't connect to server
        Window.alert(e.getMessage());
        }
        }
    }
    
    /* Funzioni javascript utilizzate, scritte seguendo la sintassi di JSNI*/
    
    
    private native boolean openedPage()/*-{ 
             var Windowreference=$wnd.opener;
             if(Windowreference==null)
            {
            $wnd.alert("devi aprire la pagina da http://localhost:3020/trill_on_swish per utilizzare questa funzionalità");
            return false;
            }
            return true;
             }-*/;

  private native void returnTOtrill(String converted)/*-{
      $wnd.opener.postMessage(converted,"http://localhost:3020/trill_on_swish/");
      $wnd.alert("messaggio inviato");
}-*/;
}