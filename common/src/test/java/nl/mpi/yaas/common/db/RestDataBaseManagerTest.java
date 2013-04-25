/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.yaas.common.db;

import java.io.IOException;
import java.net.URL;
import javax.xml.bind.JAXBException;
import nl.mpi.flap.kinnate.entityindexer.QueryException;
import nl.mpi.flap.plugin.PluginException;

/**
 * Document : RestDataBaseManagerTest Created on : April 24, 2013, 17:52 PM
 *
 * @author Peter Withers
 */
public class RestDataBaseManagerTest extends DataBaseManagerTest {

    @Override
    DbAdaptor getDbAdaptor() throws IOException, QueryException {
        return new RestDbAdaptor(new URL("http://192.168.56.101:8080/BaseX76/rest/"), "admin", "admin");
    }

    @Override
    public void testSampleData() throws JAXBException, PluginException, QueryException, IOException {
        super.testSampleData();
    }

    @Override
    public void testGetDatabaseStats() throws JAXBException, PluginException, QueryException, IOException {
        super.testGetDatabaseStats();
    }

    @Override
    public void testGetNodeDatasByIDs() throws QueryException, IOException, JAXBException, PluginException {
        super.testGetNodeDatasByIDs();
    }

    @Override
    public void testGetMetadataTypes() throws Exception {
        super.testGetMetadataTypes();
    }
}
