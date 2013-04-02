/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.yaas.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.ArrayList;
import nl.mpi.yaas.common.data.DatabaseStats;
import nl.mpi.yaas.common.data.MetadataFileType;
import nl.mpi.yaas.common.data.QueryDataStructures;
import nl.mpi.yaas.common.data.SearchParameters;
import nl.mpi.yaas.shared.WebQueryException;
import nl.mpi.yaas.shared.YaasDataNode;

/**
 * Created on : Jan 30, 2013, 5:21:01 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
@RemoteServiceRelativePath("searchoptions")
public interface SearchOptionsService extends RemoteService {

    DatabaseStats getDatabaseStats() throws WebQueryException;

    MetadataFileType[] getTypeOptions() throws WebQueryException;

    MetadataFileType[] getFieldOptions() throws WebQueryException;

    YaasDataNode performSearch(QueryDataStructures.CriterionJoinType criterionJoinType, ArrayList<SearchParameters> searchParametersList) throws WebQueryException;
}
