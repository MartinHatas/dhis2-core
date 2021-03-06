package org.hisp.dhis.dxf2.sync;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dxf2.metadata.jobs.MetadataSyncJob;
import org.hisp.dhis.dxf2.synch.AvailabilityStatus;
import org.hisp.dhis.dxf2.synch.SynchronizationManager;
import org.hisp.dhis.feedback.ErrorCode;
import org.hisp.dhis.feedback.ErrorReport;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.scheduling.AbstractJob;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.scheduling.parameters.EventProgramsDataSynchronizationJobParameters;
import org.hisp.dhis.system.notification.Notifier;

/**
 * @author David Katuscak
 */
public class EventProgramsDataSynchronizationJob extends AbstractJob
{
    private static final Log log = LogFactory.getLog( EventProgramsDataSynchronizationJob.class );

    private final Notifier notifier;
    private final MessageService messageService;
    private final EventSynchronization eventSync;
    private final SynchronizationManager synchronizationManager;

    public EventProgramsDataSynchronizationJob( Notifier notifier, MessageService messageService,
        EventSynchronization eventSync, SynchronizationManager synchronizationManager )
    {
        this.notifier = notifier;
        this.messageService = messageService;
        this.eventSync = eventSync;
        this.synchronizationManager = synchronizationManager;
    }

    @Override
    public JobType getJobType()
    {
        return JobType.EVENT_PROGRAMS_DATA_SYNC;
    }

    @Override
    public void execute( JobConfiguration jobConfiguration ) throws Exception
    {
        try
        {
            EventProgramsDataSynchronizationJobParameters jobParameters =
                (EventProgramsDataSynchronizationJobParameters) jobConfiguration.getJobParameters();
            eventSync.syncEventProgramData( jobParameters.getPageSize() );
            notifier.notify( jobConfiguration, "Event programs data sync successful" );
        }
        catch ( Exception e )
        {
            log.error( "Event programs data sync failed.", e );
            notifier.notify( jobConfiguration, "Event programs data sync failed: " + e.getMessage() );
            messageService.sendSystemErrorNotification( "Event programs data sync failed", e );
        }
    }

    @Override
    public ErrorReport validate()
    {
        AvailabilityStatus isRemoteServerAvailable = synchronizationManager.isRemoteServerAvailable();

        if ( !isRemoteServerAvailable.isAvailable() )
        {
            return new ErrorReport( MetadataSyncJob.class, ErrorCode.E7010, isRemoteServerAvailable.getMessage() );
        }

        return super.validate();
    }
}
