package com.nedap.archie.archetypevalidator;

import com.nedap.archie.adlparser.ADLParser;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.rminfo.ArchieRMInfoLookup;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by pieter.bos on 05/04/2017.
 */
public class ArchetypeValidatorTest {

    private ADLParser parser;
    private Archetype archetype;

    @Before
    public void setup() {
        parser = new ADLParser();
    }

    @Test
    public void validArchetype() throws Exception {
        archetype = parse("/basic.adl");
        List<ValidationMessage> messages = new ArchetypeValidator(ArchieRMInfoLookup.getInstance()).validate(archetype);
        assertEquals(0, messages.size());
    }

    @Test
    public void VCARMNonExistantType() throws Exception {
        archetype = parse("/adl2-tests/validity/rm_checking/openEHR-EHR-EVALUATION.VCARM_rm_non_existent_attribute.v1.0.0.adls");
        List<ValidationMessage> messages = new ArchetypeValidator(ArchieRMInfoLookup.getInstance()).validate(archetype);
        System.out.println(messages);
        assertEquals(1, messages.size());
        assertEquals(ErrorType.VCARM, messages.get(0).getType());
    }

    @Test
    public void VCORMTNonConformingType2() throws Exception {
        archetype = parse("/adl2-tests/validity/rm_checking/openEHR-EHR-OBSERVATION.VCORMT_rm_non_conforming_type2.v1.0.0.adls");
        List<ValidationMessage> messages = new ArchetypeValidator(ArchieRMInfoLookup.getInstance()).validate(archetype);
        System.out.println(messages);
        assertEquals(messages.toString(), 1, messages.size());
        assertEquals(ErrorType.VCORMT, messages.get(0).getType());
    }

    @Test
    public void VACDFidCodeNotPresent() throws Exception {
        archetype = parse("/adl2-tests/validity/consistency/openEHR-TEST_PKG-ENTRY.VACDF_ac_code_in_definition_not_in_terminology.v1.0.0.adls");
        List<ValidationMessage> messages = new ArchetypeValidator(ArchieRMInfoLookup.getInstance()).validate(archetype);
        System.out.println(messages);
        assertEquals(2, messages.size());
        assertEquals(ErrorType.VACDF, messages.get(0).getType());
        assertEquals(ErrorType.VCARM, messages.get(1).getType());

    }

    @Test
    public void VATDFatCodeNotPresent() throws Exception {
        archetype = parse("/adl2-tests/validity/consistency/openEHR-TEST_PKG-ENTRY.VATDF_at_code_in_ordinal_not_in_terminology.v1.0.0.adls");
        List<ValidationMessage> messages = new ArchetypeValidator(ArchieRMInfoLookup.getInstance()).validate(archetype);
        System.out.println(messages);
        assertEquals(2, messages.size());
        assertEquals(ErrorType.VATDF, messages.get(0).getType());
        assertEquals(ErrorType.VCARM, messages.get(1).getType());

    }


    private Archetype parse(String filename) throws IOException {
        archetype = parser.parse(ArchetypeValidatorTest.class.getResourceAsStream(filename));
        assertTrue(parser.getErrors().toString(), parser.getErrors().hasNoErrors());
        return archetype;
    }

}
