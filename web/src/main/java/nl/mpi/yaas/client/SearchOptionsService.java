/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.yaas.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Created on : Jan 30, 2013, 5:21:01 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
@RemoteServiceRelativePath("searchoptions")
public interface SearchOptionsService extends RemoteService {

    String[] getSearchOptions();
}
