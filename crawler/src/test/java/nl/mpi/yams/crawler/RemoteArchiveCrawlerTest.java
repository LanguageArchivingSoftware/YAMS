/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
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
package nl.mpi.yams.crawler;

import java.net.URI;
import java.net.URISyntaxException;
import junit.framework.TestCase;
import nl.mpi.flap.kinnate.entityindexer.QueryException;

/**
 * Created on : Feb 11, 2013, 21:02 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class RemoteArchiveCrawlerTest extends TestCase {

    public RemoteArchiveCrawlerTest(String testName) {
        super(testName);
    }

    /**
     * Test of crawl method, of class RemoteArchiveCrawler.
     */
    public void testCrawl() throws QueryException, URISyntaxException {
//        RemoteArchiveCrawler archiveCrawler = new RemoteArchiveCrawler(RemoteArchiveCrawler.DbType.TestDB, 3, restUrl, restUser, restPass);
//        //            URI startURI = new URI("http://corpus1.mpi.nl/CGN/COREX6/data/meta/imdi_3.0_eaf/corpora/cgn.imdi");
//        URI startURI = new URI("http://corpus1.mpi.nl/qfs1/media-archive/silang_data/Corpusstructure/1.imdi");
//        archiveCrawler.crawl(startURI);
    }
}
