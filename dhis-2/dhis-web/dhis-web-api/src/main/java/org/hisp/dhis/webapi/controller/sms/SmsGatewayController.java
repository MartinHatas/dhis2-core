package org.hisp.dhis.webapi.controller.sms;

/*
 * Copyright (c) 2004-2018, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import com.google.common.collect.ImmutableMap;
import org.hisp.dhis.dxf2.webmessage.WebMessageException;
import org.hisp.dhis.dxf2.webmessage.WebMessageUtils;
import org.hisp.dhis.render.RenderService;
import org.hisp.dhis.sms.config.BulkSmsGatewayConfig;
import org.hisp.dhis.sms.config.ClickatellGatewayConfig;
import org.hisp.dhis.sms.config.GatewayAdministrationService;
import org.hisp.dhis.sms.config.GenericHttpGatewayConfig;
import org.hisp.dhis.sms.config.SMPPGatewayConfig;
import org.hisp.dhis.sms.config.SmsGatewayConfig;
import org.hisp.dhis.webapi.mvc.annotation.ApiVersion;
import org.hisp.dhis.common.DhisApiVersion;
import org.hisp.dhis.webapi.service.WebMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Zubair <rajazubair.asghar@gmail.com>
 */

@RestController
@RequestMapping( value = "/gateways" )
@ApiVersion( { DhisApiVersion.DEFAULT, DhisApiVersion.ALL } )
public class SmsGatewayController
{
    private static final ImmutableMap<String, Class<? extends SmsGatewayConfig>> TYPE_MAPPER = new
        ImmutableMap.Builder<String, Class<? extends SmsGatewayConfig>>()
        .put( "http", GenericHttpGatewayConfig.class  )
        .put( "bulksms", BulkSmsGatewayConfig.class  )
        .put( "clickatell", ClickatellGatewayConfig.class  )
        .put( "smpp", SMPPGatewayConfig.class  )
        .build();
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private WebMessageService webMessageService;

    @Autowired
    private RenderService renderService;

    @Autowired
    private GatewayAdministrationService gatewayAdminService;

    // -------------------------------------------------------------------------
    // GET
    // -------------------------------------------------------------------------

    @PreAuthorize( "hasRole('ALL') or hasRole('F_MOBILE_SENDSMS')" )
    @RequestMapping( method = RequestMethod.GET, produces = { "application/json" } )
    public void getGateways( HttpServletRequest request, HttpServletResponse response )
        throws WebMessageException, IOException
    {
        renderService.toJson( response.getOutputStream(), gatewayAdminService.listGateways() );
    }

    @PreAuthorize( "hasRole('ALL') or hasRole('F_MOBILE_SENDSMS')" )
    @RequestMapping( value = "/default", method = RequestMethod.GET )
    public void getDefault( HttpServletRequest request, HttpServletResponse response )
        throws WebMessageException, IOException
    {
        SmsGatewayConfig defaultGateway = gatewayAdminService.getDefaultGateway();

        if ( defaultGateway == null )
        {
            throw new WebMessageException( WebMessageUtils.notFound( "No default gateway found" ) );
        }

        renderService.toJson( response.getOutputStream(), defaultGateway );
    }

    @PreAuthorize( "hasRole('ALL') or hasRole('F_MOBILE_SENDSMS')" )
    @RequestMapping( value = "/{uid}", method = RequestMethod.GET, produces = "application/json" )
    public void getGatewayConfiguration( @PathVariable String uid, HttpServletRequest request,
        HttpServletResponse response )
        throws WebMessageException, IOException
    {
        SmsGatewayConfig gateway = gatewayAdminService.getByUid( uid );

        if ( gateway == null )
        {
            throw new WebMessageException( WebMessageUtils.notFound( "No gateway found" ) );
        }

        renderService.toJson( response.getOutputStream(), gateway );
    }

    // -------------------------------------------------------------------------
    // PUT,POST
    // -------------------------------------------------------------------------

    @PreAuthorize( "hasRole('ALL') or hasRole('F_MOBILE_SENDSMS')" )
    @RequestMapping( value = "/clickatell", method = { RequestMethod.POST, RequestMethod.PUT }, produces = "application/json" )
    public void addOrUpdateClickatellConfiguration( HttpServletRequest request, HttpServletResponse response )
        throws IOException
    {
        SmsGatewayConfig payLoad = renderService.fromJson( request.getInputStream(), ClickatellGatewayConfig.class );

        if ( gatewayAdminService.addOrUpdateGateway( payLoad, ClickatellGatewayConfig.class ) )
        {
            webMessageService.send( WebMessageUtils.ok( "SAVED" ), response, request );
        }
        else
        {
            webMessageService.send( WebMessageUtils.error( "FAILED" ), response, request );
        }
    }

    @PreAuthorize( "hasRole('ALL') or hasRole('F_MOBILE_SENDSMS')" )
    @RequestMapping( value = "/bulksms", method = { RequestMethod.POST, RequestMethod.PUT }, produces = "application/json" )
    public void addOrUpdatebulksmsConfiguration( HttpServletRequest request, HttpServletResponse response )
        throws IOException
    {
        BulkSmsGatewayConfig payLoad = renderService.fromJson( request.getInputStream(), BulkSmsGatewayConfig.class );

        if ( gatewayAdminService.addOrUpdateGateway( payLoad, BulkSmsGatewayConfig.class ) )
        {
            webMessageService.send( WebMessageUtils.ok( "SAVED" ), response, request );
        }
        else
        {
            webMessageService.send( WebMessageUtils.error( "FAILED" ), response, request );
        }
    }

    @PreAuthorize( "hasRole('ALL') or hasRole('F_MOBILE_SENDSMS')" )
    @RequestMapping( value = "/generichttp", method = { RequestMethod.POST, RequestMethod.PUT }, produces = "application/json" )
    public void addOrUpdateGenericConfiguration( HttpServletRequest request, HttpServletResponse response )
        throws IOException
    {
        GenericHttpGatewayConfig payLoad = renderService.fromJson( request.getInputStream(),
            GenericHttpGatewayConfig.class );

        if ( gatewayAdminService.addOrUpdateGateway( payLoad, GenericHttpGatewayConfig.class ) )
        {
            webMessageService.send( WebMessageUtils.ok( "SAVED" ), response, request );
        }
        else
        {
            webMessageService.send( WebMessageUtils.error( "FAILED" ), response, request );
        }
    }

    @PreAuthorize( "hasRole('ALL') or hasRole('F_MOBILE_SENDSMS')" )
    @RequestMapping( value = "/default/{uid}", method = RequestMethod.PUT )
    public void setDefault( @PathVariable String uid, HttpServletRequest request, HttpServletResponse response )
        throws WebMessageException
    {
        SmsGatewayConfig gateway = gatewayAdminService.getByUid( uid );

        if ( gateway == null )
        {
            throw new WebMessageException( WebMessageUtils.notFound( "No gateway found" ) );
        }

        gatewayAdminService.setDefaultGateway( gateway );

        webMessageService.send( WebMessageUtils.ok( gateway.getName() + " is set to default" ), response, request );
    }

    @PreAuthorize( "hasRole('ALL') or hasRole('F_MOBILE_SENDSMS')" )
    @RequestMapping( value = "/{uid}", method = RequestMethod.PUT )
    public void updateGateway( @PathVariable String uid, HttpServletRequest request, HttpServletResponse response )
            throws WebMessageException, IOException
    {
        SmsGatewayConfig config = gatewayAdminService.getByUid( uid );

        if ( config == null )
        {
            throw new WebMessageException( WebMessageUtils.notFound( "No gateway found" ) );
        }

        Class<? extends SmsGatewayConfig> gatewayType = gatewayAdminService.getGatewayType( config );

        SmsGatewayConfig updatedConfig = renderService.fromJson( request.getInputStream(), gatewayType );

        if ( gatewayAdminService.hasDefaultGateway() && updatedConfig.isDefault() )
        {
            throw new WebMessageException( WebMessageUtils.conflict( "Default gateway already exists" ) );
        }

        gatewayAdminService.updateGateway( config, updatedConfig  );

        webMessageService.send( WebMessageUtils.ok( String.format( "Gateway with uid: %s has been updated", uid ) ), response, request );
    }

    @PreAuthorize( "hasRole('ALL') or hasRole('F_MOBILE_SENDSMS')" )
    @RequestMapping( method = RequestMethod.POST )
    public void addGateway( @RequestParam String type, HttpServletRequest request, HttpServletResponse response )
        throws IOException, WebMessageException
    {
        Class<? extends SmsGatewayConfig> gatewayType = TYPE_MAPPER.getOrDefault( type, null );

        if ( gatewayType == null )
        {
            throw new WebMessageException(WebMessageUtils.notFound( String.format( "No gateway of type: %s is supported ", type ) ) );
        }

        SmsGatewayConfig config = renderService.fromJson( request.getInputStream(),  gatewayType );

        gatewayAdminService.addGateway( config );

        webMessageService.send( WebMessageUtils.ok( "Gateway configuration added" ), response, request );
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @PreAuthorize( "hasRole('ALL') or hasRole('F_MOBILE_SENDSMS')" )
    @RequestMapping( value = "/{uid}", method = RequestMethod.DELETE )
    public void removeGateway( @PathVariable String uid, HttpServletRequest request, HttpServletResponse response )
        throws WebMessageException
    {
        SmsGatewayConfig gateway = gatewayAdminService.getByUid( uid );

        if ( gateway == null )
        {
            throw new WebMessageException( WebMessageUtils.notFound( "No gateway found with id: " + uid ) );
        }

        gatewayAdminService.removeGatewayByUid( uid );

        webMessageService.send( WebMessageUtils.ok( "Gateway removed successfully" ), response, request );
    }
}
