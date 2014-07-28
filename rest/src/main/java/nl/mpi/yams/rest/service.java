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
package nl.mpi.yams.rest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import nl.mpi.flap.kinnate.entityindexer.QueryException;
import nl.mpi.flap.model.DataField;
import nl.mpi.flap.model.SerialisableDataNode;
import nl.mpi.yams.common.data.DatabaseList;
import nl.mpi.yams.common.data.DatabaseStats;
import nl.mpi.yams.common.data.HighlightableDataNode;
import nl.mpi.yams.common.data.MetadataFileType;
import nl.mpi.yams.common.db.DataBaseManager;
import nl.mpi.yams.common.db.DbAdaptor;
import nl.mpi.yams.common.db.RestDbAdaptor;
import org.slf4j.LoggerFactory;

/**
 * REST Web Service
 *
 * @since Feb 17, 2014 3:41 PM (creation date)
 * @author Peter Withers <peter.withers@mpi.nl>
 */
@Path("")
public class service {

    final private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
    @Context
    private UriInfo context;
    @Context
    ServletConfig servletConfig;
    @Context
    ServletContext servletContext;

    /**
     * Creates a new instance of service
     */
    public service() {
    }

    @GET
    @Produces({MediaType.TEXT_HTML})
    public String getHtmlListing() throws QueryException {
        StringBuilder stringBuilder = new StringBuilder();
        DataBaseManager<HighlightableDataNode, DataField, MetadataFileType> yamsDatabase = getDatabase(DataBaseManager.defaultDataBase);
        stringBuilder.append("<a href=\"./dbinfo\">dbinfo</a><br>");
        for (String dbName : yamsDatabase.getDatabaseList()) {
            stringBuilder.append("<h3>");
            stringBuilder.append(dbName);
            stringBuilder.append("</h3><a href=\"./dbinfo/");
            stringBuilder.append(dbName);
            stringBuilder.append("\">dbinfo</a><br>");
            stringBuilder.append("<a href=\"./data/");
            stringBuilder.append(dbName);
            stringBuilder.append("\">root nodes</a><br>");
            stringBuilder.append("<a href=\"./types/");
            stringBuilder.append(dbName);
            stringBuilder.append("\">types</a><br>");
            stringBuilder.append("<a href=\"./paths/");
            stringBuilder.append(dbName);
            stringBuilder.append("?type=\">paths</a><br>");
            stringBuilder.append("<a href=\"./hints/");
            stringBuilder.append(dbName);
            stringBuilder.append("?type=&path=&text=");
            stringBuilder.append("\">hints</a><br>");
        }
        return stringBuilder.toString();
    }

    @GET
    @Path("/dbinfo")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDatabaseList() throws QueryException {
        DataBaseManager<HighlightableDataNode, DataField, MetadataFileType> yamsDatabase = getDatabase("");
        DatabaseList databaseList = yamsDatabase.getDatabaseStatsList();
        return Response.ok(databaseList).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/dbinfo/{dbname}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDatabaseInfo(@PathParam("dbname") String dbName) throws QueryException {
        DataBaseManager<HighlightableDataNode, DataField, MetadataFileType> yamsDatabase = getDatabase(dbName);
        DatabaseStats databaseStats = yamsDatabase.getDatabaseStats();
        return Response.ok(databaseStats).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/data/{dbname}")
    public Response getRootNode(@PathParam("dbname") String dbName) throws QueryException {
        DataBaseManager<HighlightableDataNode, DataField, MetadataFileType> yamsDatabase = getDatabase(dbName);
        final SerialisableDataNode rootNodes = yamsDatabase.getRootNodes();
        return Response.ok(rootNodes.getChildList()).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/data/{dbname}/linksof")
//    @Path("hdl{hdl}")
    public Response getChildDataNodes(@PathParam("dbname") String dbName, @QueryParam("url") final String identifier, @QueryParam("start") @DefaultValue("0") final int start, @QueryParam("end") @DefaultValue("30") final int end) throws QueryException {
        DataBaseManager<HighlightableDataNode, DataField, MetadataFileType> yamsDatabase = getDatabase(dbName);
        final SerialisableDataNode childNodes = yamsDatabase.getChildNodesOfHdl(identifier, start, end);
        return Response.ok(childNodes.getChildList()).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/types/{dbname}")
    public Response getTypeOptions(@PathParam("dbname") String dbName) throws QueryException {
        DataBaseManager<HighlightableDataNode, DataField, MetadataFileType> yamsDatabase = getDatabase(dbName);
        final MetadataFileType[] metadataTypes = yamsDatabase.getMetadataTypes(null);
        return Response.ok(metadataTypes).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/paths/{dbname}")
    public Response getPathOptions(@PathParam("dbname") String dbName, @QueryParam("type") final String type) throws QueryException {
        DataBaseManager<HighlightableDataNode, DataField, MetadataFileType> yamsDatabase = getDatabase(dbName);
        final MetadataFileType[] metadataTypes = yamsDatabase.getMetadataPaths(new MetadataFileType(type, null, null));
        return Response.ok(metadataTypes).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/hints/{dbname}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getHints(@PathParam("dbname") String dbName, @QueryParam("type") final String type, @QueryParam("path") final String path, @QueryParam("text") final String text, @QueryParam("max") @DefaultValue("5") final int max) throws QueryException {
        final MetadataFileType options = new MetadataFileType(type, path, text);
        DataBaseManager<HighlightableDataNode, DataField, MetadataFileType> yamsDatabase = getDatabase(dbName);
        MetadataFileType[] metadataFieldTypes = yamsDatabase.getMetadataFieldValues(options, max);
        return Response.ok(metadataFieldTypes).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/data/{dbname}/node")
//    @Path("hdl{hdl}")
    public Response getDataNode(@PathParam("dbname") String dbName, @QueryParam("url") final String identifier) throws QueryException {
        DataBaseManager<HighlightableDataNode, DataField, MetadataFileType> yamsDatabase = getDatabase(dbName);
        List<String> identifierList = new ArrayList<>();
        identifierList.add(identifier);
        final SerialisableDataNode childNodes = yamsDatabase.getNodeDatasByHdls(identifierList);
        return Response.ok(childNodes.getChildList()).header("Access-Control-Allow-Origin", "*").build();
    }

    private String getBasexRestUrl() {
        final String initParameterRestUrl = servletContext.getInitParameter("basexRestUrl");
        return initParameterRestUrl;
    }

    private String getBasexUser() {
        final String initParameterUser = servletContext.getInitParameter("basexUser");
        return initParameterUser;
    }

    private String getBasexPass() {
        final String initParameterPass = servletContext.getInitParameter("basexPass");
        return initParameterPass;
    }

    private DataBaseManager<HighlightableDataNode, DataField, MetadataFileType> getDatabase(String databaseName) throws QueryException {
        String basexRestUrl = getBasexRestUrl();
        try {
            final DbAdaptor dbAdaptor = new RestDbAdaptor(new URL(basexRestUrl), getBasexUser(), getBasexPass());
            return new DataBaseManager(HighlightableDataNode.class, DataField.class, MetadataFileType.class, dbAdaptor, databaseName);
        } catch (MalformedURLException exception) {
            throw new QueryException("Failed to open the database connection at: " + basexRestUrl + " " + exception.getMessage());
        }
    }
}
