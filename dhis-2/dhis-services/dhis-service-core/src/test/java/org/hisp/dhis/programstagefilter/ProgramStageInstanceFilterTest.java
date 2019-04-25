package org.hisp.dhis.programstagefilter;
/*
 * Copyright (c) 2004-2019, University of Oslo
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProgramStageInstanceFilterTest extends DhisSpringTest
{

    @Autowired
    private ProgramStageInstanceFilterService psiFilterService;

    @Autowired
    private ProgramService programService;


    private Program programA;

    private Program programB;

    @Override
    public void setUpTest()
    {
        programA = createProgram( 'A' );
        programB = createProgram( 'B' );

        programService.addProgram( programA );
        programService.addProgram( programB );

    }


    @Test
    public void testValidatenvalidEventFilterWithMissingProgram()
    {
        ProgramStageInstanceFilter psiFilter = createProgramStageInstanceFilter( '1', null, null );
        List<String> errors = psiFilterService.validate( psiFilter );
        
        assertNotNull( errors );
        assertEquals( 1 , errors.size() );
    }

    @Test
    public void testValidateInvalidEventFilterWithInvalidProgram()
    {
        ProgramStageInstanceFilter psiFilter = createProgramStageInstanceFilter( '1', "ABCDEF12345", null );
        List<String> errors = psiFilterService.validate( psiFilter );
        assertNotNull( errors );
        assertEquals( 1 , errors.size() );
    }

    private static ProgramStageInstanceFilter createProgramStageInstanceFilter( char uniqueCharacter, String program, String programStage )
    {
        ProgramStageInstanceFilter psiFilter = new ProgramStageInstanceFilter();
        psiFilter.setAutoFields();

        psiFilter.setName( "eventFilterName" + uniqueCharacter );
        psiFilter.setCode( "eventFilterCode" + uniqueCharacter );
        psiFilter.setDescription( "eventFilterDescription" + uniqueCharacter );

        if ( program != null )
        {
            psiFilter.setProgram( program );
        }

        if ( programStage != null )
        {
            psiFilter.setProgramStage( programStage );
        }
        return psiFilter;
    }

}
