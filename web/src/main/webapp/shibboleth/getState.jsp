<%--

    Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

--%>
<%-- 
    Document   : getState
    Created on : May 8, 2014, 10:29:57 AM
    Author     : Peter Withers <peter.withers@mpi.nl>
--%>
<%@page contentType="application/json" pageEncoding="UTF-8"%>
<%
    response.setContentType("application/json");
    response.setHeader("Content-Disposition", "inline");
%>
{
"contextPath": "<%= request.getContextPath()%>",
"getRemoteUser": "<%= request.getRemoteUser()%>",
"getAttribute": "<%= session.getAttribute("userid")%>"
}