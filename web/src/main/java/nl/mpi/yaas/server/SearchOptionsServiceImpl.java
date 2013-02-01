/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.yaas.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.io.File;
import java.util.ArrayList;
import nl.mpi.flap.kinnate.entityindexer.QueryException;
import nl.mpi.flap.plugin.PluginSessionStorage;
import nl.mpi.yaas.client.SearchOptionsService;
import nl.mpi.yaas.common.data.MetadataFileType;
import nl.mpi.yaas.common.db.ArbilDatabase;
import nl.mpi.yaas.common.db.ArbilDatabase.SearchOption;
import nl.mpi.yaas.shared.DataNode;
import nl.mpi.yaas.shared.WebQueryException;

/**
 * Created on : Jan 30, 2013, 5:23:13 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
@SuppressWarnings("serial")
public class SearchOptionsServiceImpl extends RemoteServiceServlet implements SearchOptionsService {

    public String[] getTypeOptions() throws WebQueryException {
        // todo: this version of the Arbil database is not intended to multi entry and will be replaced by a rest version when it is written
        final PluginSessionStorage pluginSessionStorage = new PluginSessionStorage() {
            public File getApplicationSettingsDirectory() {
                return new File("/Users/petwit2/.arbil/");
            }

            public File getProjectDirectory() {
                return new File("/Users/petwit2/.arbil/");
            }

            public File getProjectWorkingDirectory() {
                return new File("/Users/petwit2/.arbil/ArbilWorkingFiles/");
            }
        };
        try {
            ArbilDatabase<DataNode> arbilDatabase = new ArbilDatabase<DataNode>(DataNode.class, pluginSessionStorage);
            MetadataFileType[] metadataPathTypes = arbilDatabase.getMetadataTypes(null);
            ArrayList<String> returnList = new ArrayList<String>();
            for (MetadataFileType metadataFileType : metadataPathTypes) {
                returnList.add(metadataFileType.getFieldName());
            };
            return returnList.toArray(new String[0]);
        } catch (QueryException exception) {
            throw new WebQueryException(exception.getMessage());
        }
    }

    public String[] getSearchOptions() {
        ArrayList<String> returnList = new ArrayList<String>();
        for (SearchOption searchOption : SearchOption.values()) {
            returnList.add(searchOption.toString());
        };
        return returnList.toArray(new String[0]);
    }
}