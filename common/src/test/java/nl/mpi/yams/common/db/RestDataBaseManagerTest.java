/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.yams.common.db;

import java.io.IOException;
import java.net.URL;
import javax.xml.bind.JAXBException;
import nl.mpi.flap.kinnate.entityindexer.QueryException;
import nl.mpi.flap.model.ModelException;
import nl.mpi.flap.plugin.PluginException;
import org.basex.BaseXHTTP;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Document : RestDataBaseManagerTest Created on : April 24, 2013, 17:52 PM
 *
 * @author Peter Withers
 */
public class RestDataBaseManagerTest extends DataBaseManagerTest {

    private static BaseXHTTP baseXHTTP;

    @BeforeClass
    public static void setUpClass() throws Exception {
        baseXHTTP = DataBaseManagerTest.startDb(baseXHTTP);
    }

    @AfterClass
    public static void cleanUpClass() throws Exception {
        baseXHTTP = DataBaseManagerTest.stopDb(baseXHTTP);
    }

    @Override
    DbAdaptor getDbAdaptor() throws IOException, QueryException {
        return new RestDbAdaptor(new URL(DataBaseManagerTest.restUrl), DataBaseManagerTest.restUser, DataBaseManagerTest.restPass);
    }

    @Override
    public void testSampleData() throws JAXBException, PluginException, QueryException, IOException, ModelException {
        super.testSampleData();
    }

    @Override
    public void testGetDatabaseStats() throws JAXBException, PluginException, QueryException, IOException, ModelException {
        super.testGetDatabaseStats();
    }

    @Override
    public void testGetNodeDatasByIDs() throws QueryException, IOException, JAXBException, PluginException, ModelException {
        super.testGetNodeDatasByIDs();
    }

    @Override
    public void testGetMetadataTypes() throws Exception {
        super.testGetMetadataTypes();
    }
}
