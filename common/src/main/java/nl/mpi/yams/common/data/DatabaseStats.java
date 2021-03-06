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
package nl.mpi.yams.common.data;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created on : Apr 2, 2013, 10:18:32 AM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
@XmlRootElement(name = "DatabaseStats")
public class DatabaseStats implements Serializable {

//    @XmlElement(name = "QueryTimeMS")
    private long queryTimeMS = -1;
//    @XmlElement(name = "DatabaseName")
//    private String databaseName = null;
    @XmlElement(name = "KnownDocuments")
    protected int knownDocumentsCount = -1;
    @XmlElement(name = "MissingDocuments")
    protected int misingDocumentsCount = -1;
    @XmlElement(name = "DuplicateDocuments")
    protected int duplicateDocumentsCount = -1;
    @XmlElement(name = "RootDocuments")
    protected int rootDocumentsCount = -1;
    @XmlElement(name = "RootDocumentID")
    protected DataNodeId[] rootDocumentsIDs = new DataNodeId[0];
    @XmlElement(name = "Cached")
    private boolean isCachedResults = false;

    public DatabaseStats() {
    }

    public DatabaseStats(int knownDocumentsCount, int misingDocumentsCount, int duplicateDocumentsCount, int rootDocumentsCount, DataNodeId[] rootDocumentsIDs) {
        this.knownDocumentsCount = knownDocumentsCount;
        this.misingDocumentsCount = misingDocumentsCount;
        this.duplicateDocumentsCount = duplicateDocumentsCount;
        this.rootDocumentsCount = rootDocumentsCount;
        this.rootDocumentsIDs = rootDocumentsIDs;
    }

    public boolean isIsCachedResults() {
        return isCachedResults;
    }

//    public void setIsCachedResults(boolean isCachedResults) {
//        this.isCachedResults = isCachedResults;
//    }
//    public String getDatabaseName() {
//        return databaseName;
//    }
//
//    public void setDatabaseName(String databaseName) {
//        this.databaseName = databaseName;
//    }
    public void setQueryTimeMS(long queryTimeMS) {
        this.queryTimeMS = queryTimeMS;
    }

    public long getQueryTimeMS() {
        return queryTimeMS;
    }

    public int getKnownDocumentsCount() {
        return knownDocumentsCount;
    }

    public int getMisingDocumentsCount() {
        return misingDocumentsCount;
    }

    public int getDuplicateDocumentsCount() {
        return duplicateDocumentsCount;
    }

    public int getRootDocumentsCount() {
        return rootDocumentsCount;
    }

    public DataNodeId[] getRootDocumentsIDs() {
        return rootDocumentsIDs;
    }
}
