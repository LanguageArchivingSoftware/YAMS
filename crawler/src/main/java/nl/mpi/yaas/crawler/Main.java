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
package nl.mpi.yaas.crawler;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created on : Feb 6, 2013, 2:02:48 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class Main {

    static public void main(String[] args) {
        RemoteArchiveCrawler archiveCrawler = new RemoteArchiveCrawler();
        try {
            URI startURI = new URI("http://corpus1.mpi.nl/CGN/COREX6/data/meta/imdi_3.0_eaf/corpora/cgn.imdi");
            archiveCrawler.crawl(startURI);
        } catch (URISyntaxException exception) {
            System.out.println(exception.getMessage());
            System.exit(-1);
        }
    }
}
