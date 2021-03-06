/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.client.inventory.jaxrs.handlers;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hawkular.inventory.api.model.Relationship;
import org.hawkular.inventory.api.model.Tenant;

/**
 * Tenant Information API
 * http://www.hawkular.org/docs/rest/rest-inventory.html#_tenant_information
 */
@Path("/hawkular/inventory/tenant")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface TenantHandler {

    @GET
    @Path("/")
    Response getTenant(@QueryParam("at") String at);

    @PUT
    @Path("/")
    Response createTenant(@QueryParam("at") String at, Tenant.Update update);

    @POST
    @Path("/relationship")
    Response createRelationship(@QueryParam("at") String at, List<Relationship.Blueprint> blueprints);

    @GET
    @Path("/relationships/{path}")
    Response getRelationships(@Encoded @PathParam("path") String path, @QueryParam("at") String at);
}
