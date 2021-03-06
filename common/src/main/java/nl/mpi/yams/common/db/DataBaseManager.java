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

import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import nl.mpi.flap.kinnate.entityindexer.QueryException;
import nl.mpi.flap.model.DataField;
import nl.mpi.flap.model.DataNodeLink;
import nl.mpi.flap.model.DataNodeType;
import nl.mpi.flap.model.FieldGroup;
import nl.mpi.flap.model.ModelException;
import nl.mpi.flap.model.SerialisableDataNode;
import nl.mpi.flap.plugin.PluginException;
import nl.mpi.yams.common.data.DataNodeId;
import nl.mpi.yams.common.data.DatabaseLinks;
import nl.mpi.yams.common.data.DatabaseList;
import nl.mpi.yams.common.data.DatabaseStats;
import nl.mpi.yams.common.data.HighlightableDataNode;
import nl.mpi.yams.common.data.IconTable;
import nl.mpi.yams.common.data.IconTableBase64;
import nl.mpi.yams.common.data.MetadataFileType;
import nl.mpi.yams.common.data.NodeTypeImage;
import nl.mpi.yams.common.data.QueryDataStructures.CriterionJoinType;
import nl.mpi.yams.common.data.QueryDataStructures.SearchNegator;
import nl.mpi.yams.common.data.QueryDataStructures.SearchOption;
import nl.mpi.yams.common.data.QueryDataStructures.SearchType;
import nl.mpi.yams.common.data.SearchParameters;
import org.slf4j.LoggerFactory;

/**
 * Document : DataBaseManager Created on : Aug 6, 2012, 11:39:33 AM
 *
 * @param <D> Concrete class of DataNode that will be used in the jaxb
 * deserialising process
 * @param <F> Concrete class of DataField that will be used in the jaxb
 * deserialising process
 * @param <M> Concrete class of MetadataFileType that is used as query
 * parameters and in some cases query results via the jaxb deserialising process
 * @author Peter Withers
 */
public class DataBaseManager<D, F, M> {

    final private Class<D> dClass;
    final private Class<F> fClass;
    final private Class<M> mClass;
    final protected DbAdaptor dbAdaptor;
    final private String databaseName;
    final private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * these are two recommended database names, one for testing and the other
     * for production
     */
    final static public String defaultDataBase = "yams-data";
    final static public String testDataBase = "yams-test-data";
    final static public String facetsCollection = "Facets";
    final static public String dbStatsDocument = "DatabaseStats";
    final static private String linksDocument = "DatabaseLinks";
    final static public String iconTableDocument = "IconTable";
    final private String crawledDataCollection = "CrawledData";
//    final static public String guestUser = "guestdbuser";
//    final static public String guestUserPass = "minfc8u4ng6s";
    final static public String guestUser = "admin"; // todo: the user name and password for admin and guest users needs to be determined and set
    final static public String guestUserPass = "admin";

    /**
     *
     * @param dClass Concrete class of DataNode that will be used in the jaxb
     * deserialising process
     * @param fClass Concrete class of DataField that will be used in the jaxb
     * deserialising process
     * @param mClass Concrete class of MetadataFileType that is used as query
     * parameters and in some cases query results via the jaxb deserialising
     * process
     * @param dbAdaptor an implementation of DbAdaptor which interfaces to
     * either the REST DB or local DB via java bindings
     * @param databaseName the name of the database that will be connected to
     * @throws QueryException
     */
    public DataBaseManager(Class<D> dClass, Class<F> fClass, Class<M> mClass, DbAdaptor dbAdaptor, String databaseName) throws QueryException {
        this.dbAdaptor = dbAdaptor;
        this.dClass = dClass;
        this.fClass = fClass;
        this.mClass = mClass;
        this.databaseName = escapeBadChars(databaseName);
//        dbAdaptor.checkDbExists(databaseName);
    }

    /**
     * Verifies that the database exists and create a new empty database if it
     * does not
     *
     * @throws QueryException
     */
    public void checkDbExists() throws QueryException {
        dbAdaptor.checkDbExists(databaseName);
    }

    /**
     * Drop the entire database if it exists and create a new empty database
     *
     * @throws QueryException
     */
    public void dropAllRecords() throws QueryException {
        dbAdaptor.dropAndRecreateDb(databaseName);
//        dbAdaptor.deleteDocument(databaseName, crawledDataCollection);
//        dbAdaptor.deleteDocument(databaseName, dbStatsDocument);
//        dbAdaptor.deleteDocument(databaseName, iconTableDocument);
//        dbAdaptor.deleteDocument(databaseName, facetsCollection);
    }

    /**
     * Remove the database statistics document that is generated after a crawl
     * by getDatabaseStats()
     *
     * @throws QueryException
     */
    public void clearDatabaseStats() throws QueryException {
        dbAdaptor.deleteDocument(databaseName, dbStatsDocument);
    }

    /**
     * Causes the database to reindex all of its files which is required after
     * an add or delete for instance
     *
     * @throws QueryException
     */
    public void createIndexes() throws QueryException {
        long startTime = System.currentTimeMillis();
        dbAdaptor.createIndexes(databaseName);
//        logger.debug("queryResult: " + queryResult);
        long queryMils = System.currentTimeMillis() - startTime;
        String queryTimeString = "Create indexes time: " + queryMils + "ms";
        logger.debug(queryTimeString);
    }

    private String getCachedVersion(String cachedDocument, String queryString) throws QueryException {
        String statsCachedQuery = "for $statsDoc in collection(\"" + databaseName + "\")\n"
                + "where matches(document-uri($statsDoc), '" + cachedDocument + "')\n"
                + "return $statsDoc";
        String queryResult;
        queryResult = dbAdaptor.executeQuery(databaseName, statsCachedQuery);
        if (queryResult.length() < 2) {
            // calculate the stats
            queryResult = dbAdaptor.executeQuery(databaseName, queryString);
            String resultCacheFlagged = queryResult.replaceFirst("<Cached>false</Cached>", "<Cached>true</Cached>");
            // insert the stats as a document
            dbAdaptor.addDocument(databaseName, cachedDocument, resultCacheFlagged);
        }
//        logger.debug("queryResult: " + queryResult);
        return queryResult;
    }

    /**
     * Gets a list of available databases
     *
     * @return a list of database names
     * @throws QueryException
     */
    public String[] getDatabaseList() throws QueryException {
//        String queryResult = dbAdaptor.executeQuery(databaseName, "for $databaseName in db:list()\n"
//                + "return <String>{$databaseName}</String>");
        String queryResult = dbAdaptor.executeQuery(databaseName, "db:list()");
        logger.debug("databaseList: " + queryResult);
        return queryResult.split(" ");
    }

    public DatabaseList getDatabaseStatsList() throws QueryException {
        long startTime = System.currentTimeMillis();
        String databaseListQuery = "<DatabaseList>{\n"
                + "for $dbName in db:list() return <DatabaseInfo><DatabaseName>{$dbName}</DatabaseName>{"
                + "for $statsDoc in collection($dbName)/" + dbStatsDocument + "\n"
                + "return $statsDoc,\n"
                + "for $iconDoc in collection($dbName)/" + iconTableDocument + "\n"
                + "return $iconDoc\n"
                + "}</DatabaseInfo>}</DatabaseList>\n";
        String queryResult;
//        logger.info("databaseListQuery: " + databaseListQuery);
        queryResult = dbAdaptor.executeQuery(databaseName, databaseListQuery);
//        logger.info("databaseList: " + queryResult);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DatabaseList.class, DatabaseStats.class, DataNodeId.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            DatabaseList databaseList = (DatabaseList) unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), DatabaseList.class).getValue();
            long queryMils = System.currentTimeMillis() - startTime;
            databaseList.setQueryTimeMS(queryMils);
            return databaseList;
        } catch (JAXBException exception) {
            logger.debug(exception.getMessage());
            throw new QueryException("Error getting DatabaseStatsList", exception);
        }
    }

    /**
     * Creates a document in the database that holds information on the contents
     * of the database such as document count and root nodes URLs
     *
     * @return an object of type DatabaseStats that contains information on the
     * contents of the database
     * @throws QueryException
     */
    public DatabaseStats getDatabaseStats() throws QueryException {
        long startTime = System.currentTimeMillis();
        String statsQuery = "let $rootNodes := collection(\"" + databaseName + "\")/" + linksDocument + "/RootDocumentLinks/@ID[not(.=collection(\"" + databaseName + "\")/DataNode/ChildLink/@ID)]/string()\n"
                + "return <DatabaseStats>\n"
                //                + "<KnownDocuments>{count(collection(\"" + databaseName + "\")/" + crawledDataCollection + ")}</KnownDocuments>\n"
                + "<KnownDocuments>{count(collection(\"" + databaseName + "\")/DataNode)}</KnownDocuments>\n" // todo: shouldnt this data be in crawledDataCollection?
                + "<MissingDocuments>{count(collection(\"" + databaseName + "\")/" + linksDocument + "/MissingDocumentLinks)}</MissingDocuments>\n"
                //                + "<DuplicateDocuments>{$duplicateDocumentCount}</DuplicateDocuments>\n"
                + "<RootDocuments>{count($rootNodes)}</RootDocuments>\n"
                + "<Cached>false</Cached>\n"
                + "{for $rootDocId in $rootNodes return <RootDocumentID>{$rootDocId}</RootDocumentID>}\n"
                + "</DatabaseStats>\n";
        String queryResult = getCachedVersion(dbStatsDocument, statsQuery);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DatabaseStats.class, DataNodeId.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            DatabaseStats databaseStats = (DatabaseStats) unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), DatabaseStats.class).getValue();
            long queryMils = System.currentTimeMillis() - startTime;
//            String queryTimeString = "DatabaseStats Query time: " + queryMils + "ms";
            databaseStats.setQueryTimeMS(queryMils);
//            logger.debug(queryTimeString);
            return databaseStats;
        } catch (JAXBException exception) {
            logger.debug(exception.getMessage());
            throw new QueryException("Error getting DatabaseStats");
        }
    }

    /**
     * Searches the database for missing child nodes for use when crawling
     * missing documents
     *
     * @return the URLs of the first N missing documents
     * @throws PluginException
     * @throws QueryException
     */
    public String getHandlesOfMissing() throws PluginException, QueryException {
        long startTime = System.currentTimeMillis();
        String queryString = "let $childIds := collection(\"" + databaseName + "\")/DataNode/ChildLink\n"
                + "let $knownIds := collection(\"" + databaseName + "\")/DataNode/@ID\n"
                + "let $missingIds := distinct-values($childIds[not(@ID=$knownIds)]/@URI)"
                + "return $missingIds[matches(., '\\.[icIC][mM][dD][iI]$')][position() le 1000]\n"; // <DataNodeId> </DataNodeId>
        logger.debug("filtering by suffix on cmdi and imdi (this must be removed when a better solution is defined)");// todo: resolve this issue
//        logger.debug("getHandlesOfMissing: " + queryString);
        String queryResult = dbAdaptor.executeQuery(databaseName, queryString);
        long queryMils = System.currentTimeMillis() - startTime;
        String queryTimeString = "Query time: " + queryMils + "ms";
        final String sampleDateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());;
        String statsQuery = "let $childIds := collection(\"" + databaseName + "\")/DataNode/ChildLink\n"
                + "let $knownIds := collection(\"" + databaseName + "\")/DataNode/@ID\n"
                + "return\n"
                + "<CrawlerStats linkcount='{count($childIds)}' documentcount='{count($knownIds)}' queryms='" + queryMils + "' timestamp='" + sampleDateTime + "'/>";
        String statsDoc = dbAdaptor.executeQuery(databaseName, statsQuery);
        logger.debug("stats:" + statsDoc);
        // insert the stats document
        dbAdaptor.addDocument(databaseName, "CrawlerStats/" + sampleDateTime, statsDoc);
        logger.debug(queryTimeString);
        return queryResult; // the results here need to be split on " ", but the string can be very long so it should not be done by String.split().
    }

    /**
     * Uses the DatabaseLinks document to get the next batch of missing child
     * nodes for use when crawling missing documents
     *
     * @param databaseLinks from the crawler with will be merged and updated
     * with the DatabaseLinks document in the database
     * @param numberToGet maximum number of links to retrieve
     * @param selectionFilter if not null this string will be used to filter the
     * links so that only matching links will be returned
     * @return the DataNodeLinks of the first N missing documents
     * @throws PluginException
     * @throws QueryException
     */
    public Set<DataNodeLink> getHandlesOfMissing(DatabaseLinks databaseLinks, int numberToGet, String selectionFilter) throws PluginException, QueryException {
        long startTime = System.currentTimeMillis();
        DatabaseLinks updatedDatabaseLinks;
        try {
            String docTestQueryString = "if(fn:empty(collection(\"" + databaseName + "\")/" + linksDocument + ")) then (0)else(1)";
            String docTestResult = dbAdaptor.executeQuery(databaseName, docTestQueryString);

            JAXBContext jaxbContext = JAXBContext.newInstance(DatabaseLinks.class, DataNodeLink.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter stringWriter = new StringWriter();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(databaseLinks, stringWriter);
            if (docTestResult.equals("1")) {
                // update the document
                String insertString = "let $updatedLinks := "
                        + stringWriter.toString().replaceFirst("^\\<\\?[^\\?]*\\?\\>", "") // remove the xml header that xquery cant have in a variable
                        + "\n"
                        + "return (\n"
                        // delete any recently added documents from the missing list
                        + "delete node collection(\"" + databaseName + "\")/" + linksDocument + "/MissingDocumentLinks[@ID = $updatedLinks/RecentDocumentLinks/@ID]"
                        // we check that a root node is not already in the kown documents and that new root nodes do not exist in the crawled collection
                        + ",\ninsert node $updatedLinks/RootDocumentLinks[not(@ID=collection(\"" + databaseName + "\")/" + linksDocument + "/RootDocumentLinks/@ID)][not(@ID=collection(\"" + databaseName + "\")/DataNode/ChildLink)] into collection(\"" + databaseName + "\")/" + linksDocument
                        // update the list of missing documents
                        + ",\ninsert node $updatedLinks/MissingDocumentLinks[not(@ID=collection(\"" + databaseName + "\")/" + linksDocument + "/MissingDocumentLinks/@ID)] into collection(\"" + databaseName + "\")/" + linksDocument + ")";
                dbAdaptor.executeQuery(databaseName, insertString);
                // this seems be slow with the following stats: stats:<CrawlerStats linkcount="215516" documentcount="45955" queryms="8114061" timestamp="20130903132702"/> Query time: 8114061ms
                // instead we now delete via the list of newly added nodes and filter the inbound list of links
//                String deleteQuery = "delete node collection(\"" + databaseName + "\")/" + linksDocument + "/MissingDocumentLinks[@ID = collection(\"" + databaseName + "\")/DataNode/@ID]";
//                dbAdaptor.executeQuery(databaseName, deleteQuery);
            } else if (docTestResult.equals("0")) {
                // add the document
                dbAdaptor.addDocument(databaseName, linksDocument, stringWriter.toString());
            } else {
                throw new QueryException("unexpected state for DatabaseLinks document");
            }
            final String filterQuery;
            if (selectionFilter != null) {
                filterQuery = "[@URI contains text '" + selectionFilter + "']";
            } else {
                filterQuery = "";
            }
            final String queryString = "<DatabaseLinks>{collection(\"" + databaseName + "\")/" + linksDocument + "/MissingDocumentLinks" + filterQuery + "[position() le " + numberToGet + "]}</DatabaseLinks>";
            System.out.println(queryString);
            String queryResult = dbAdaptor.executeQuery(databaseName, queryString);
            logger.debug("updatedDatabaseLinks: " + queryResult);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            updatedDatabaseLinks = (DatabaseLinks) unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), DatabaseLinks.class).getValue();
        } catch (JAXBException exception) {
            System.err.println("jaxb error:" + exception.getMessage());
            throw new PluginException(exception);
        }

        long queryMils = System.currentTimeMillis() - startTime;
        String queryTimeString = "Query time: " + queryMils + "ms";
        final String sampleDateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());;
        final long totalMemory = Runtime.getRuntime().totalMemory();
        final long freeMemory = Runtime.getRuntime().freeMemory();
        final long maxMemory = Runtime.getRuntime().maxMemory();
        String statsQuery = "let $childIds := collection(\"" + databaseName + "\")/DataNode/ChildLink\n"
                + "let $knownIds := collection(\"" + databaseName + "\")/DataNode/@ID\n"
                + "return\n"
                + "<CrawlerStats linkcount='{count($childIds)}' documentcount='{count($knownIds)}' queryms='" + queryMils + "' timestamp='" + sampleDateTime + "' freebytes='" + freeMemory + "' totalbytes='" + totalMemory + "' maxMemory='" + maxMemory + "'/>";
        String statsDoc = dbAdaptor.executeQuery(databaseName, statsQuery);
        logger.debug("stats:" + statsDoc);
        // insert the stats document
        dbAdaptor.addDocument(databaseName, "CrawlerStats/" + sampleDateTime, statsDoc);
        logger.debug(queryTimeString);
        return updatedDatabaseLinks.getChildLinks(); // the results here need to be split on " ", but the string can be very long so it should not be done by String.split().
    }

    /**
     * Retrieves the document of all the known node types and the icons for each
     * type from the database in base 64 format
     *
     * @return IconTableBase64 a set of node types and their icons in base 64
     * format
     * @throws PluginException
     * @throws QueryException
     */
    public IconTableBase64 getNodeIconsBase64() throws PluginException, QueryException {
        String iconTableQuery = "for $statsDoc in collection(\"" + databaseName + "\")\n"
                + "where matches(document-uri($statsDoc), '" + iconTableDocument + "')\n"
                + "return $statsDoc";
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(IconTableBase64.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String queryResult;
            queryResult = dbAdaptor.executeQuery(databaseName, iconTableQuery);
//            logger.debug("queryResult: " + queryResult);
            return (IconTableBase64) unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), IconTableBase64.class).getValue();
        } catch (JAXBException exception) {
            throw new PluginException(exception);
        }
    }

    /**
     * Retrieves the document of all the known node types and the icons for each
     * type from the database
     *
     * @return IconTable a set of node types and their icons
     * @throws PluginException
     * @throws QueryException
     */
    public IconTable getNodeIcons() throws PluginException, QueryException {
        String iconTableQuery = "for $iconTable in collection(\"" + databaseName + "\")\n"
                + "where matches(document-uri($iconTable), '" + iconTableDocument + "')\n"
                + "return $iconTable";
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(IconTable.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String queryResult;
            queryResult = dbAdaptor.executeQuery(databaseName, iconTableQuery);
            //logger.debug("queryResult: " + queryResult);
            return (IconTable) unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), IconTable.class).getValue();
        } catch (JAXBException exception) {
            throw new PluginException(exception);
        }
    }

    /**
     * Inserts a document of all the known node types and the icons for each
     * type into the database
     *
     * @param iconTable a set of node types and their icons to be inserted into
     * the database
     * @throws PluginException
     * @throws QueryException
     */
    public IconTable insertNodeIconsIntoDatabase(IconTable iconTable) throws PluginException, QueryException {
        try {
            IconTable databaseIconTable = getNodeIcons();
            for (NodeTypeImage nodeTypeImage : databaseIconTable.getNodeTypeImageSet()) {
                // add the known types to the new set
                iconTable.addTypeIcon(nodeTypeImage);
            }
        } catch (PluginException exception) {
            // if there is not icon document here that can be normal if it is the first run
            logger.debug("Error getting existing IconTableDocument (this is normal on the first run).");
        }
        dbAdaptor.deleteDocument(databaseName, iconTableDocument);
        // use JAXB to serialise and insert the IconTable into the database
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(IconTable.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter stringWriter = new StringWriter();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(iconTable, stringWriter);
            //logger.debug("NodeIcons to be inserted:\n" + stringWriter.toString());
            dbAdaptor.addDocument(databaseName, iconTableDocument, stringWriter.toString());
            getNodeIcons(); // do we really need to be calling getNodeIcons at this point?
        } catch (JAXBException exception) {
            System.err.println("jaxb error:" + exception.getMessage());
            throw new PluginException(exception);
        }
        return iconTable;
    }

    /**
     * Returns a string representing the number of actual documents in the
     * database, expected documents, missing documents and root documents.
     *
     * @return String of counts: missing, crawled, actual document count
     * @throws PluginException
     * @throws QueryException
     */
    public String getDatabaseLinksCounts() throws PluginException, QueryException {
        String queryString = "count(collection(\"unit-test-database\")/DatabaseLinks/MissingDocumentLinks),\n"
                + "count(collection(\"unit-test-database\")/DatabaseLinks/RecentDocumentLinks),\n"
                + "count(collection(\"unit-test-database\")/DataNode)\n"; // <DataNodeId> </DataNodeId>
        logger.debug("getHandlesOfMissing: " + queryString);
        String queryResult = dbAdaptor.executeQuery(databaseName, queryString);
        return queryResult; // the results here could to be split on " " but a string comparison of the expected will do the job in the unit test for which this is intended
    }

    /*
     * Takes a node id and deletes it and all of its children from the database, starting with the leaves first.
     * When each node is deleted it's ID must be removed from the RecentDocumentLinks and added to the MissingDocumentLinks in the database.
     * Once this is run, the database can be updated with a standard append in the command line tool and the deleted nodes will be recrawled.    
     * @param nodeId the ID of the data node that will with all its child nodes be deleted from the database so that it can be recrawled
     * @returns affectedDocumentCount which is the number of deleted documents. 
     */
    public void deleteBranch(String nodeId) throws NumberFormatException, QueryException {
        logger.debug("Deleting branch {}", nodeId);
        String deleteBranchQuery = //"collection(\"" + databaseName + "\")/DataNode[@ID eq \"" + nodeId + "\"]//ChildLink";
                "declare function local:branchDelete($nodeId as xs:string)\n"
                + "{\n"
                + " for $childId in collection(\"" + databaseName + "\")/DataNode[@ID eq $nodeId]//ChildLink/@ID/string()\n"
                + " return local:branchDelete($childId)\n"
                + ",\n"
                + "collection(\"" + databaseName + "\")/DataNode[@ID eq $nodeId]/@ID/string()\n"
                + "};\n"
                + "let $deleteList := local:branchDelete(\"" + nodeId + "\")\n"
                // update the list of missing documents by moving the relevant IDs to MissingDocumentLinks from RecentDocumentLinks
                + "return (\n"
                + "for $n in collection(\"" + databaseName + "\")/DatabaseLinks/RecentDocumentLinks[@ID = $deleteList]\n"
                + "return rename node $n as 'MissingDocumentLinks',\n"
                // delete the actual documents from the database
                + "delete node collection(\"" + databaseName + "\")/DataNode[@ID = $deleteList]\n"
                + ")\n";
        logger.trace(deleteBranchQuery);
        dbAdaptor.executeQuery(databaseName, deleteBranchQuery);
    }

    /**
     * Inserts a document into the database and optionally checks for existing
     * documents that would constitute a duplicate
     *
     * @param dataNode the data node to be inserted into the database
     * @param testForDuplicates if true the database will be searched for the
     * document before inserting
     * @throws PluginException
     * @throws QueryException
     */
    public void insertIntoDatabase(SerialisableDataNode dataNode, boolean throwOnDuplicate) throws PluginException, QueryException, ModelException {
        // test for existing documents with the same ID and optionally throw if one is found
        String existingDocumentQuery = "let $countValue := count(collection(\"" + databaseName + "\")/DataNode[@ID = \"" + dataNode.getID() + "\"])\nreturn $countValue";
        String existingDocumentResult = dbAdaptor.executeQuery(databaseName, existingDocumentQuery);
        if (existingDocumentResult.equals("0")) {
            // use JAXB to serialise and insert the data node into the database
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(dClass, fClass, mClass);
                Marshaller marshaller = jaxbContext.createMarshaller();
                StringWriter stringWriter = new StringWriter();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                marshaller.marshal(dataNode, stringWriter);
//            logger.debug("Data to be inserted:\n" + stringWriter.toString());
                dbAdaptor.addDocument(databaseName, crawledDataCollection + "/" + dataNode.getID(), stringWriter.toString());
            } catch (JAXBException exception) {
                System.err.println("jaxb error:" + exception.getMessage());
                throw new PluginException(exception);
            }
        } else {
            if (throwOnDuplicate) {
                throw new QueryException("Existing document found, count: " + existingDocumentResult + " ID: " + dataNode.getID() + " URL: " + dataNode.getURI());
            }
            logger.debug("Existing document found, count: " + existingDocumentResult + " : " + dataNode.getURI());
        }
    }

    private String getTypeClause(MetadataFileType metadataFileType) {
        String typeClause = "";
        if (metadataFileType != null) {
            if (metadataFileType.getType() != null && !metadataFileType.getType().isEmpty()) {
                typeClause += "[/DataNode/Type/@Label = '" + escapeBadChars(metadataFileType.getType()) + "']";
            }
            if (metadataFileType.getPath() != null && !metadataFileType.getPath().isEmpty()) {
                typeClause += "//DataNode/FieldGroup[@Label = '" + escapeBadChars(metadataFileType.getPath()) + "']";
                if (metadataFileType.getValue() != null && !metadataFileType.getValue().isEmpty()) {
                    typeClause += "[FieldData/@FieldValue contains text '" + escapeBadChars(metadataFileType.getValue()) + "']";
                }
            } else if (metadataFileType.getValue() != null && !metadataFileType.getValue().isEmpty()) {
                typeClause += "//DataNode/FieldGroup[FieldData/@FieldValue contains text '" + escapeBadChars(metadataFileType.getValue()) + "']";
            }
        }
        return typeClause;
    }

    private String getTypeNodes(MetadataFileType metadataFileType) {
        String typeNodes = "";
        if (metadataFileType != null) {
            if (metadataFileType.getPath() != null && !metadataFileType.getPath().isEmpty()) {
                typeNodes += "<Path>" + escapeBadChars(metadataFileType.getPath()) + "</Path>";
            }
            if (metadataFileType.getType() != null && !metadataFileType.getType().isEmpty()) {
                typeNodes += "<Type>" + escapeBadChars(metadataFileType.getType()) + "</Type>";
            }
        }
        return typeNodes;
    }

    private String getDocumentName(MetadataFileType metadataFileType, String queryType) {
        if (metadataFileType == null) {
            return facetsCollection + "/" + queryType + "/all";
        }
        final String type = (metadataFileType.getType() == null) ? "all" : escapeBadChars(metadataFileType.getType());
        final String path = (metadataFileType.getPath() == null) ? "all" : escapeBadChars(metadataFileType.getPath());
        return facetsCollection + "/" + queryType + "/" + type + "/" + path;
    }

    private String getDocumentName(MetadataFileType[] metadataFileTypes, String queryType) {
        String documentName = facetsCollection + "/" + queryType;
        for (MetadataFileType metadataFileType : metadataFileTypes) {
            if (metadataFileType == null) {
                documentName += "/all/all";
            } else {
                final String type = (metadataFileType.getType() == null) ? "all" : escapeBadChars(metadataFileType.getType());
                final String path = (metadataFileType.getPath() == null) ? "all" : escapeBadChars(metadataFileType.getPath());
                documentName += "/" + type + "/" + path;
            }
        }
        logger.debug("documentName: " + documentName);
        return documentName;
    }
//    private String getFieldConstraint(MetadataFileType fieldType) {
//        String fieldConstraint = "";
//        if (fieldType != null) {
//            final String fieldNameString = fieldType.getFieldName();
//            if (fieldNameString != null) {
//                fieldConstraint = "FieldGroup/@Label = '" + fieldNameString + "' and ";
//            }
//        }
//        return fieldConstraint;
//    }

    private String getSearchTextConstraint(SearchType searchType, String searchString, String nodeString) {
        final String escapedSearchString = escapeBadChars(searchString);
        String returnString = "";
        switch (searchType) {
            case contains:
                if (escapedSearchString.isEmpty()) {
                    // when the user has not entered any string then return all, but allow the negator to still be used
                    returnString = "1=1";
                } else {
                    returnString = nodeString + "@FieldValue contains text '" + escapedSearchString + "'";
                }
                break;
            case equals:
                returnString = nodeString + "@FieldValue = '" + escapedSearchString + "'";
                break;
            case fuzzy:
                returnString = nodeString + "@FieldValue contains text '" + escapedSearchString + "' using fuzzy";
                break;
        }
        return returnString;
    }

    static String escapeBadChars(String inputString) {
        if (inputString == null) {
            return null;
        }
        // our queries use double quotes so single quotes are allowed
        // todo: could ; cause issues?
        return inputString.replace("&", "&amp;").replace("\"", "&quot;").replace("'", "&apos;");
    }
    /*
     * let $elementSet0 := for $nameString0 in collection('" + databaseName + "')//*:Address[count(*) = 0] order by $nameString0 return $nameString0
     let $elementSet1 := for $nameString0 in collection('" + databaseName + "')//*:Region[count(*) = 0] order by $nameString0 return $nameString0
     return
     <TreeNode><DisplayString>All</DisplayString>
     {
     for $nameString0 in distinct-values($elementSet0/text())
     return
     <TreeNode><DisplayString>Address: {$nameString0}</DisplayString>
     {
     let $intersectionSet0 := $elementSet1[root()//*:Address = $nameString0]
     for $nameString1 in distinct-values($intersectionSet0/text())
     return
     <TreeNode><DisplayString>Region: {$nameString1}</DisplayString>
     </TreeNode>
     }
     </TreeNode>
     }
     </TreeNode>
     * */

    private String getTreeSubQuery(List<MetadataFileType> treeBranchTypeList, String whereClause, String selectClause, String trailingSelectClause, int levelCount) {
        final int maxMetadataFileCount = 100;
        if (!treeBranchTypeList.isEmpty()) {
            String separatorString = "";
//            if (whereClause.length() > 0) {
//                separatorString = ",\n";
//            }
            MetadataFileType treeBranchType = treeBranchTypeList.remove(0);
            String currentFieldName = escapeBadChars(treeBranchType.getPath());
            String nextWhereClause = whereClause + "[//*:" + currentFieldName + " = $nameString" + levelCount + "]";
            String nextSelectClause = selectClause + "[*:" + currentFieldName + " = $nameString" + levelCount + "]";
            String nextTrailingSelectClause = "[*:" + currentFieldName + " = $nameString" + levelCount + "]";
            return "{\n"
                    + "for $nameString" + levelCount + " in distinct-values(collection('" + databaseName + "')" + whereClause + "//*:" + currentFieldName + "[count(*) = 0]\n"
                    //                + "return concat(base-uri($entityNode), path($entityNode))\n"
                    + ")\n"
                    + "order by $nameString" + levelCount + "\n"
                    + "return\n"
                    + "<TreeNode><DisplayString>" + currentFieldName + ": {$nameString" + levelCount + "}</DisplayString>\n"
                    + getTreeSubQuery(treeBranchTypeList, nextWhereClause, nextSelectClause, nextTrailingSelectClause, levelCount + 1)
                    + "</TreeNode>\n}\n";
        } else {
            return "{"
                    //                    + " if (count(collection('" + databaseName + "')" + whereClause + "//.[count(*) = 0][text() != '']" + trailingSelectClause + ") < " + maxMetadataFileCount + ") then\n"
                    + "for $matchingNode in collection('" + databaseName + "')" + whereClause + "//." + trailingSelectClause + "\n"
                    + "return\n"
                    + "<MetadataTreeNode>\n"
                    + "<FileUri>{base-uri($matchingNode)}</FileUri>\n"
                    + "<FileUriPath>{path($matchingNode)}</FileUriPath>\n"
                    + "</MetadataTreeNode>\n"
                    //                    + "else \n"
                    //                    + "<DisplayString>&gt;more than " + maxMetadataFileCount + " results, please add more facets&lt;</DisplayString>"
                    + "\n}\n";
        }
    }

    private String getRootNodesQuery() {
        return "<DataNode>\n"
                + "{for $dataNodeId in string(collection('" + databaseName + "')/DatabaseLinks/RootDocumentLinks/@ID)\n"
                + " return \n"
                + " collection('" + databaseName + "')/DataNode[@ID eq $dataNodeId]}</DataNode>";
    }

    private String getChildNodesOfAttributeQuery(final String attributeName, String id, int start, int end) {
        return "<DataNode>{for $childNodeId in collection('" + databaseName + "')/DataNode[@" + attributeName + " eq '" + escapeBadChars(id) + "']/ChildLink[position() gt " + start + " and position() le " + end + "]/@ID\n"
                + "return collection('" + databaseName + "')/DataNode[@ID eq $childNodeId]}</DataNode>";
    }

    private String getNodesByUrlQuery(final List<String> nodeIDs) {
        return getNodesByAttributeQuery("URI", nodeIDs);
    }

    private String getNodesByHdlQuery(final List<String> nodeIDs) {
        return getNodesByAttributeQuery("ArchiveHandle", nodeIDs);
    }

    private String getNodesByAttributeQuery(final String attributeName, final List<String> attributeValues) {
        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append("<DataNode>\n");
        queryStringBuilder.append("{for $dataNode in collection('");
        queryStringBuilder.append(databaseName);
        queryStringBuilder.append("')/DataNode where $dataNode/@");
        queryStringBuilder.append(attributeName);
        queryStringBuilder.append(" = (\n");
        boolean firstLoop = true;
        for (String value : attributeValues) {
            if (!firstLoop) {
                queryStringBuilder.append(",");
            }
            firstLoop = false;
            queryStringBuilder.append("'");
            queryStringBuilder.append(escapeBadChars(value));
            queryStringBuilder.append("'");
        }
        queryStringBuilder.append(") return $dataNode}");
        queryStringBuilder.append("</DataNode>");
        return queryStringBuilder.toString();
    }

    private String getNodesByIdQuery(final List<DataNodeId> nodeIDs) {
        //logger.info("getNodesByIdQuery");
        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append("<DataNode>\n");
        queryStringBuilder.append("{for $dataNode in collection('");
        queryStringBuilder.append(databaseName);
        queryStringBuilder.append("')/DataNode where $dataNode/@ID = (\n");
        boolean firstLoop = true;
        for (DataNodeId dataNodeId : nodeIDs) {
            //logger.info(dataNodeId.getIdString());
            if (!firstLoop) {
                queryStringBuilder.append(",");
            }
            firstLoop = false;
            queryStringBuilder.append("'");
            queryStringBuilder.append(escapeBadChars(dataNodeId.getIdString()));
            queryStringBuilder.append("'");
        }
        queryStringBuilder.append(") return $dataNode}");
        queryStringBuilder.append("</DataNode>");
        return queryStringBuilder.toString();
    }

//    private String getSearchFieldConstraint(SearchParameters searchParameters) {
//        String fieldConstraint = getTypeClause(searchParameters.getFieldType());
//        String searchTextConstraint = getSearchTextConstraint(searchParameters.getSearchType(), searchParameters.getSearchString(), "//FieldGroup/FieldData/");
//        return fieldConstraint + searchTextConstraint;
//    }
    private String getSearchConstraint(SearchParameters searchParameters) {
        String typeClause = "/DataNode";
        String pathClause = "";
        if (searchParameters.getFileType() != null) {
            if (searchParameters.getFileType().getType() != null) {
                typeClause += "[Type/@Label = '" + escapeBadChars(searchParameters.getFileType().getType()) + "']";
            }
            if (searchParameters.getFieldType().getPath() != null) {
                pathClause += "[@Label = '" + escapeBadChars(searchParameters.getFieldType().getPath()) + "']";
            }
        }
        String fieldsQuery = "";
        if (searchParameters.getSearchNegator() == SearchNegator.is) {
            fieldsQuery = "for $field in $foundNode"
                    + getSearchTextConstraint(searchParameters.getSearchType(), searchParameters.getSearchString(), "//FieldGroup/FieldData[")
                    + "]\n"
                    + "return \n"
                    + "<Highlight>{$nodeId, $field/@Path}</Highlight>\n";
        } else {
            fieldsQuery = "<Exclude>{$nodeId}</Exclude>\n";
        }
        return "for $foundNode in collection('" + databaseName + "/" + crawledDataCollection + "')" + typeClause + "[//DataNode/FieldGroup" + pathClause + "["
                + getSearchTextConstraint(searchParameters.getSearchType(), searchParameters.getSearchString(), "FieldData/")
                + "]]\n"
                + "let $nodeId := $foundNode/@ID\n"
                + "return\n"
                + "(\n"
                + fieldsQuery
                + ")";
    }

    private String getTreeFacetsQuery(MetadataFileType[] metadataFileTypes) {
        String typeClause = "";
        for (MetadataFileType type : metadataFileTypes) {
            typeClause += getTypeClause(type);
        }
        String typeNodes = getTypeNodes(metadataFileTypes[metadataFileTypes.length - 1]);
        return "let $fieldValues := collection('" + databaseName + "/" + crawledDataCollection + "')" + typeClause + "//FieldData/@FieldValue/string()\n"
                + "return <MetadataFileType>\n"
                + "{\n"
                + "for $label in distinct-values($fieldValues)\n"
                + "order by $label\n"
                + "return <MetadataFileType>"
                + "<Label>{$label}</Label>\n"
                + "<Value>{$label}</Value>\n"
                + typeNodes
                + "<Count>{count($fieldValues[. = $label])}</Count></MetadataFileType>\n"
                + "}</MetadataFileType>";
    }

    private String getMetadataFieldValuesQuery(MetadataFileType metadataFileType, int maxResults) {
        String typeClause = getTypeClause(metadataFileType);
        String typeNodes = getTypeNodes(metadataFileType);
        return "let $fieldValues := collection('" + databaseName + "/" + crawledDataCollection + "')" + typeClause + "//FieldData/@FieldValue/string()\n"
                + "return <MetadataFileType>\n"
                + "{\n"
                + "for $label in distinct-values($fieldValues)[position() le " + maxResults + "]\n"
                + "order by $label\n"
                + "return <MetadataFileType>"
                + "<Label>{$label}</Label>\n"
                + "<Value>{$label}</Value>\n"
                + typeNodes
                //                + "<Count>{count($fieldValues[. = $label])}</Count>"
                + "</MetadataFileType>\n"
                + "}</MetadataFileType>";
    }

    private String getMetadataTypes() {
//        return "for $xpathString in distinct-values(\n"
//                + "for $entityNode in collection('" + databaseName + "')/*\n"
//                + "return path($entityNode)\n"
//                + ")\n"
//                + "return"
//                + "$xpathString";
        return "<MetadataFileType>\n"
                + "<MetadataFileType>\n"
                + "<Label>All Types</Label>\n"
                + "<Count>{count(collection('" + databaseName + "/" + crawledDataCollection + "'))}</Count>\n"
                + "</MetadataFileType>\n"
                + "{\n"
                //                + "for $imdiType in distinct-values(collection('" + databaseName + "')/*:METATRANSCRIPT/*/name())\n"
                //                + "order by $imdiType\n"
                //                + "return\n"
                //                + "<MetadataFileType>\n"
                //                + "<ImdiType>{$imdiType}</ImdiType>\n"
                //                + "<RecordCount>{count(collection('" + databaseName + "')/*:METATRANSCRIPT/*[name()=$imdiType])}</RecordCount>\n"
                //                + "</MetadataFileType>\n"
                //                + "},{"
                //                + "for $profileString in distinct-values(collection('" + databaseName + "')/*:CMD/@*:schemaLocation)\n"
                //                //                + "order by $profileString\n"
                //                + "return\n"
                //                + "<MetadataFileType>\n"
                //                + "<profileString>{$profileString}</profileString>\n"
                //                + "<RecordCount>{count(collection('" + databaseName + "')/*:CMD[@*:schemaLocation = $profileString])}</RecordCount>"
                //                + "</MetadataFileType>\n"
                /*
                 * optimised this query 2012-10-17
                 * the query above takes:
                 * 5014.03 ms
                 * the query below takes:
                 * 11.8 ms (varies per run)
                 */
                //                + "for $profileInfo in index:facets('" + databaseName + "/" + crawledDataCollection + "')/document-node/element[@name='DataNode']/element[@name='Type']/attribute[@name='Format']/entry\n"
                //                + "return\n"
                //                + "<MetadataFileType>\n"
                //                + "<fieldName>{string($profileInfo)}</fieldName>\n"
                //                + "<RecordCount>{string($profileInfo/@count)}</RecordCount>\n"
                //                + "</MetadataFileType>"
                //                + "}{"
                //                + "for $profileInfo in index:facets('" + databaseName + "')/document-node/element[@name='DataNode']/element[@name='Type']/attribute[@name='Name']/entry\n"
                //                + "return\n"
                //                + "<MetadataFileType>\n"
                //                + "<fieldName>{string($profileInfo)}</fieldName>\n"
                //                + "<RecordCount>{string($profileInfo/@count)}</RecordCount>\n"
                //                //                + "<ValueCount>{count($profileInfo/entry)}</ValueCount>\n"
                //                + "</MetadataFileType>\n"

                + "let $allNodeTypes := collection('" + databaseName + "/" + crawledDataCollection + "')/DataNode/Type/@Label/string()\n"
                + "for $nodeType in distinct-values($allNodeTypes)\n"
                + "order by $nodeType\n"
                + "return\n"
                + "<MetadataFileType>\n"
                + "<Label>{$nodeType}</Label>\n"
                + "<Type>{$nodeType}</Type>\n"
                + "<Count>{count($allNodeTypes[. = $nodeType])}</Count>\n"
                + "</MetadataFileType>\n"
                + "}</MetadataFileType>";
    }

    private String getMetadataPathsQuery(MetadataFileType metadataFileType) {
        String typeClause = getTypeClause(metadataFileType);
        String typeNodes = getTypeNodes(metadataFileType);
        return "let $fieldLabels := collection('" + databaseName + "/" + crawledDataCollection + "')" + typeClause + "//FieldGroup[FieldData/@FieldValue != '']/@Label/string()\n"
                + "return <MetadataFileType>\n"
                + "<MetadataFileType><Label>All Paths</Label>"
                + typeNodes
                + "<Count>{count($fieldLabels)}</Count></MetadataFileType>\n"
                + "{\n"
                + "for $label in distinct-values($fieldLabels)\n"
                + "order by $label\n"
                + "return <MetadataFileType>"
                + "<Label>{$label}</Label>\n"
                + typeNodes
                + "<Path>{$label}</Path>\n"
                + "<Count>{count($fieldLabels[. = $label and $label != ''])}</Count></MetadataFileType>\n"
                + "}</MetadataFileType>";
    }

    /**
     * Searches the database
     *
     * @param criterionJoinType the type of join that the query will perform
     * @param searchParametersList the parameters of the search
     * @return A data node that the results as child nodes plus some query
     * information
     * @throws QueryException
     */
    public D getSearchResult(CriterionJoinType criterionJoinType, List<SearchParameters> searchParametersList) throws QueryException {
        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append("<DataNode ID=\"Search Results\" Label=\"Search Results: ");
        if (searchParametersList.size() > 1) {
            queryStringBuilder.append(criterionJoinType.name());
            queryStringBuilder.append(" ");
        }
        for (SearchParameters parameters : searchParametersList) {
            queryStringBuilder.append("(");
            final String type = escapeBadChars(parameters.getFileType().getType());
            if (type != null) {
                queryStringBuilder.append(type);
                queryStringBuilder.append(" ");
            }
            final String path = escapeBadChars(parameters.getFieldType().getPath());
            if (path != null) {
                queryStringBuilder.append(path);
                queryStringBuilder.append(" ");
            }
            for (SearchOption option : SearchOption.values()) {
                if (option.getSearchNegator() == parameters.getSearchNegator() && option.getSearchType() == parameters.getSearchType()) {
                    queryStringBuilder.append(option.toString());
                }
            }
            queryStringBuilder.append(" ");
            queryStringBuilder.append(escapeBadChars(parameters.getSearchString()));
            queryStringBuilder.append(") ");
        }
        queryStringBuilder.append("\">");
        queryStringBuilder.append("{\n");
        int parameterCounter = 0;
        int exclusionCounter = 0;
        for (SearchParameters searchParameters : searchParametersList) {
            switch (searchParameters.getSearchNegator()) {
                case is:
                    queryStringBuilder.append("let $documentSet");
                    queryStringBuilder.append(parameterCounter);
                    parameterCounter++;
                    break;
                case not:
                    queryStringBuilder.append("let $exclusionSet");
                    queryStringBuilder.append(exclusionCounter);
                    exclusionCounter++;
                    break;
            }
            queryStringBuilder.append(" := ");
            queryStringBuilder.append(getSearchConstraint(searchParameters));
        }
        queryStringBuilder.append("\nlet $highlightSet := $documentSet0");
        switch (criterionJoinType) {
            case intersect:
                for (int setCount = 1; setCount < parameterCounter; setCount++) {
                    queryStringBuilder.append("[@ID = $documentSet");
                    queryStringBuilder.append(setCount);
                    queryStringBuilder.append("/@ID]");
                }
                break;
            case union:
                for (int setCount = 1; setCount < parameterCounter; setCount++) {
                    queryStringBuilder.append(" ");
                    queryStringBuilder.append(criterionJoinType.name());
                    queryStringBuilder.append(" $documentSet");
                    queryStringBuilder.append(setCount);
                }
                break;
        }
        queryStringBuilder.append("\nlet $exclusionSet := (()");
        for (int setCount = 0; setCount < exclusionCounter; setCount++) {
            queryStringBuilder.append(",$exclusionSet");
            queryStringBuilder.append(setCount);
        }
        queryStringBuilder.append(")\n");
        queryStringBuilder.append("\n"
                + "let $nodeIdSet := for $nodeId in distinct-values($highlightSet[not (@ID = $exclusionSet/@ID)]/@ID) return <ChildLink ID='{$nodeId}'/>\n"
                //                + "for $documentNode in $returnSet\n"
                + "return\n"
                /*
                 * This query currently takes 18348.54 ms
                 * the loop over the return set takes 15000 ms or so
                 * With two search values and union it takes 13810.04ms
                 * With two search values and union and one field name specified it takes 9086.76ms
                 * 
                 */
                /*
                 * 15041
                 * <TreeNode>{
                 for $fieldNode in collection('" + databaseName + "')//.[(text() contains text 'pu6') or (name() = 'Name' and text() contains text 'pu8')]
                 let $documentFile := base-uri($fieldNode)
                 group by $documentFile
                 return
                 <MetadataTreeNode>
                 <FileUri>{$documentFile}</FileUri>
                 {
                 for $entityNode in $fieldNode
                 return <FileUriPath>{path($entityNode)}</FileUriPath>
                 }
                 </MetadataTreeNode>
                 }</TreeNode>
                 */
                // todo: add back in the set functions
                //                + "for $entityNode in $documentNode[");
                //        boolean firstConstraint = true;
                //        for (SearchParameters searchParameters : searchParametersList) {
                //            if (firstConstraint) {
                //                firstConstraint = false;
                //            } else {
                //                queryStringBuilder.append(" or ");
                //            }
                //            queryStringBuilder.append(getSearchFieldConstraint(searchParameters));
                //        }
                //        queryStringBuilder.append("]\n"
                //                + "return $entityNode\n"
                + "($highlightSet[not (@ID = $exclusionSet/@ID)], $nodeIdSet)"
                + "}</DataNode>\n");
        // todo: this would be better getting the nodes and doing an instersect on the nodes and only then extracting the fields to highlight
//         logger.debug("Query: " + queryStringBuilder);
        final D metadataTypesString = getDbTreeNode(queryStringBuilder.toString());
        return metadataTypesString;
    }

//    public DbTreeNode getSearchResultX(CriterionJoinType criterionJoinType, ArrayList<SearchParameters> searchParametersList) {
//        StringBuilder queryStringBuilder = new StringBuilder();
//        StringBuilder joinStringBuilder = new StringBuilder();
//        StringBuilder fieldStringBuilder = new StringBuilder();
//        int parameterCounter = 0;
//        for (SearchParameters searchParameters : searchParametersList) {
//            fieldStringBuilder.append(getSearchFieldConstraint(searchParameters));
//            if (queryStringBuilder.length() > 0) {
//                fieldStringBuilder.append(" or ");
//                joinStringBuilder.append(" ");
//                joinStringBuilder.append(criterionJoinType.name());
//                joinStringBuilder.append(" ");
//            } else {
//                joinStringBuilder.append("let $returnSet := ");
//            }
//            joinStringBuilder.append("$set");
//            joinStringBuilder.append(parameterCounter);
//            queryStringBuilder.append("let $set");
//            queryStringBuilder.append(parameterCounter);
//            queryStringBuilder.append(" := ");
//            parameterCounter++;
//            queryStringBuilder.append(getSearchConstraint(searchParameters));
//        }
//        queryStringBuilder.append(joinStringBuilder);
//        queryStringBuilder.append("return <TreeNode>{"
//                + "for $documentNode in $returnSet\n"
//                + "return\n"
//                + "<MetadataTreeNode>\n"
//                + "<FileUri>{base-uri($entityNode)}</FileUri>\n"
//                + "for $entityNode in $documentNode//*");
//        queryStringBuilder.append(fieldStringBuilder.toString());
//        queryStringBuilder.append("\n"
//                + "return <FileUriPath>{path($entityNode)}</FileUriPath>\n"
//                + "</MetadataTreeNode>\n"
//                + "}</TreeNode>");
//
//        final DbTreeNode metadataTypesString = getDbTreeNode(queryStringBuilder.toString());
//        return metadataTypesString;
//    }
    public M[] getMetadataPaths(MetadataFileType metadataFileType) throws QueryException {
        final String queryString = getMetadataPathsQuery(metadataFileType);
        return getMetadataTypes(queryString, getDocumentName(metadataFileType, "paths"), true);
    }

    public M[] getMetadataFieldValues(MetadataFileType metadataFileType, int maxResults) throws QueryException {
        final String queryString = getMetadataFieldValuesQuery(metadataFileType, maxResults);
        //logger.debug("getMetadataFieldValues: " + queryString);
        return getMetadataTypes(queryString, getDocumentName(metadataFileType, "values"), false);
    }

    public M[] getMetadataTypes(MetadataFileType metadataFileType) throws QueryException {
        final String queryString = getMetadataTypes();
        //logger.debug("getMetadataTypes: " + queryString);
        return getMetadataTypes(queryString, getDocumentName(metadataFileType, "types"), true);
    }

    public M[] getTreeFacetTypes(MetadataFileType[] metadataFileTypes) throws QueryException {
        for (MetadataFileType type : metadataFileTypes) {
            //logger.debug("Type: " + type); // todo: comment this out when done
        }
        final String queryString = getTreeFacetsQuery(metadataFileTypes);
        return getMetadataTypes(queryString, getDocumentName(metadataFileTypes, "tree"), true);
    }

//    public DbTreeNode getSearchTreeData() {
//        final String queryString = getTreeQuery(treeBranchTypeList);
//        return getDbTreeNode(queryString);
//    }
    public D getRootNodes() throws QueryException {
        final String queryString = getRootNodesQuery();
        //logger.debug("getRootNodes: " + queryString);
        return getDbTreeNode(queryString);
    }

    public D getChildNodesOfHdl(final String nodeIdentifier, int start, int end) throws QueryException {
        final String queryString = getChildNodesOfAttributeQuery("ArchiveHandle", nodeIdentifier, start, end);
        //logger.debug("getMetadataTypes: " + queryString);
        return getDbTreeNode(queryString);
    }

    public D getChildNodesOfId(final String nodeIdentifier, int start, int end) throws QueryException {
        final String queryString = getChildNodesOfAttributeQuery("ID", nodeIdentifier, start, end);
        //logger.debug("getMetadataTypes: " + queryString);
        return getDbTreeNode(queryString);
    }

    public D getChildNodesOfUrl(final String nodeIdentifier, int start, int end) throws QueryException {
        final String queryString = getChildNodesOfAttributeQuery("URI", nodeIdentifier, start, end);
        //logger.debug("getMetadataTypes: " + queryString);
        return getDbTreeNode(queryString);
    }

    public D getNodeDatasByHdls(final List<String> nodeHdls) throws QueryException {
        final String queryString = getNodesByHdlQuery(nodeHdls);
        //logger.debug("getMetadataTypes: " + queryString);
        return getDbTreeNode(queryString);
    }

    public D getNodeDatasByUrls(final List<String> nodeUrls) throws QueryException {
        final String queryString = getNodesByUrlQuery(nodeUrls);
        return getDbTreeNode(queryString);
    }

    public D getNodeDatasByIDs(final List<DataNodeId> nodeIDs) throws QueryException {
        final String queryString = getNodesByIdQuery(nodeIDs);
        //logger.info(queryString);
        //logger.info("getDbTreeNode");
        return getDbTreeNode(queryString);
    }

    private D getDbTreeNode(String queryString) throws QueryException {
//        long startTime = System.currentTimeMillis();
        try {
            //logger.info("JAXBContext.newInstance(dClass)--- stack trace verions");
            //logger.info(dClass.getName());

//            JAXBContext jaxbContext3 = JAXBContext.newInstance(DataField.class);
//            logger.info("jaxbContext.createUnmarshaller()DataField");
//            Unmarshaller unmarshaller3 = jaxbContext3.createUnmarshaller();
//
//            JAXBContext jaxbContext1 = JAXBContext.newInstance(FieldGroup.class);
//            logger.info("jaxbContext.createUnmarshaller()FieldGroup");
//            Unmarshaller unmarshaller1 = jaxbContext1.createUnmarshaller();
//
//            JAXBContext jaxbContext4 = JAXBContext.newInstance(DataNodeType.class);
//            logger.info("jaxbContext.createUnmarshaller()DataNodeType");
//            Unmarshaller unmarshaller4 = jaxbContext4.createUnmarshaller();
//
//            JAXBContext jaxbContext5 = JAXBContext.newInstance(SerialisableDataNode.class);
//            logger.info("jaxbContext.createUnmarshaller()SerialisableDataNode");
//            Unmarshaller unmarshaller5 = jaxbContext5.createUnmarshaller();
//
//            JAXBContext jaxbContext2 = JAXBContext.newInstance(HighlightableDataNode.class);
//            logger.info("jaxbContext.createUnmarshaller()HighlightableDataNode");
//            Unmarshaller unmarshaller2 = jaxbContext2.createUnmarshaller();
            String queryResult;
//            logger.debug("queryString: " + queryString);
            queryResult = dbAdaptor.executeQuery(databaseName, queryString);
            //logger.debug("queryResult: " + queryResult);
            JAXBContext jaxbContext = JAXBContext.newInstance(dClass);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            D rootTreeNode = (D) unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), dClass).getValue();
//            long queryMils = System.currentTimeMillis() - startTime;
//            int resultCount = 0;
//            if (rootTreeNode != null) {
//                resultCount = 1;
//            }
//            String queryTimeString = "Query time: " + queryMils + "ms for " + resultCount + " entities";
//            logger.debug(queryTimeString);
            //logger.debug("rootTreeNode");
            return rootTreeNode;
        } catch (JAXBException exception) {
            logger.debug(exception.getMessage());
//            exception.printStackTrace();
            throw new QueryException("Error getting search options");
        }
    }

    private M[] getMetadataTypes(final String queryString, String documentName, boolean allowCaching) throws QueryException {
        long startTime = System.currentTimeMillis();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(mClass);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            final String queryResult;
            if (allowCaching) {
                queryResult = getCachedVersion(documentName, queryString);
            } else {
                queryResult = dbAdaptor.executeQuery(databaseName, queryString);
            }
//            logger.debug("queryString: " + queryString);
//            queryResult = dbAdaptor.executeQuery(databaseName, queryString);
//            logger.debug("queryResult: " + queryResult);
            M foundEntities = (M) unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), MetadataFileType.class).getValue();
            long queryMils = System.currentTimeMillis() - startTime;
            final M[] entityDataArray = (M[]) ((MetadataFileType) foundEntities).getChildMetadataTypes();
            int resultCount = 0;
            if (entityDataArray != null) {
                resultCount = entityDataArray.length;
            }
            String queryTimeString = "Query time: " + queryMils + "ms for " + resultCount + " entities";
            logger.debug(queryTimeString);
//            selectedEntity.appendTempLabel(queryTimeString);
            return (M[]) ((MetadataFileType) foundEntities).getChildMetadataTypes();
        } catch (JAXBException exception) {
            logger.debug(exception.getMessage());
            throw new QueryException("Error getting search options");
        }
    }
}
