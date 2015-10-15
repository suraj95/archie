package com.nedap.archie.adlparser;

import com.nedap.archie.adlparser.antlr.adlParser.*;
import com.nedap.archie.adlparser.antlr.*;
import com.nedap.archie.aom.*;
import com.nedap.archie.aom.primitives.*;
import com.nedap.archie.base.MultiplicityInterval;
import org.antlr.v4.runtime.tree.TerminalNode;

import static com.nedap.archie.adlparser.PrimitivesConstraintParser.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pieter.bos on 15/10/15.
 */
public class ADLTreeWalker {


    public Archetype parse(AdlContext tree) {
        Archetype archetype = null;

        if (tree.archetype() != null) {
            archetype = parseArchetype(tree.archetype());
        }
        return archetype;
    }

    private Archetype parseArchetype(ArchetypeContext archetypeContext) {
        Archetype archetype = new Archetype();
        archetype.setArchetypeId(new ArchetypeHRID(archetypeContext.ARCHETYPE_HRID().getText()));
        if (archetypeContext.definition_section() != null) {
            archetype.setDefinition(parseComplexObject(archetypeContext.definition_section().c_complex_object()));
        }
        return archetype;
    }

    private CComplexObject parseComplexObject(C_complex_objectContext context) {
        CComplexObject object = new CComplexObject();
        if(context.type_id() != null) {
            object.setRmTypeName(context.type_id().getText());
        }
        if(context.ID_CODE() != null) {
            object.setNodeId(context.ID_CODE().getText());
        } else if (context.ROOT_ID_CODE() != null) {
            object.setNodeId(context.ROOT_ID_CODE().getText());
        }
        //TODO: object.setDeprecated(context.);
        if (context.c_occurrences() != null) {
            object.setOccurences(parseMultiplicityInterval(context.c_occurrences()));
        }
        for (C_attribute_defContext attribute : context.c_attribute_def()) {
            object.addAttribute(parseAttribute(attribute));
        }
        return object;
    }

    private CAttribute parseAttribute(C_attribute_defContext attributeDefContext) {
        CAttribute attribute = null;
        if (attributeDefContext.c_attribute() != null) {
            attribute = new CAttribute();
            C_attributeContext attributeContext = attributeDefContext.c_attribute();
            attribute.setRmAttributeName(attributeContext.attribute_id().getText());
            if (attributeContext.c_existence() != null) {
                attribute.setExistence(parseMultiplicityInterval(attributeContext.c_existence()));
            }

            //TODO: attributeContext.adl_dir()
            if (attributeContext.c_cardinality() != null) {
                attributeContext.c_cardinality();//TODO!
            }
            if (attributeContext.c_objects() != null) {
                attribute.setChildren(parseCObjects(attributeContext.c_objects()));
            }
        } else if (attributeDefContext.c_attribute_tuple() != null) {
            C_attribute_tupleContext tupleContext = attributeDefContext.c_attribute_tuple();
            //TODO: i have no clue

        }
        return attribute;

    }

    private List<CObject> parseCObjects(C_objectsContext objectsContext) {
        ArrayList<CObject> result = new ArrayList<>();

        if (objectsContext.c_primitive_object() != null) {
            result.add(parsePrimitiveObject(objectsContext.c_primitive_object()));
        } else if (objectsContext.c_non_primitive_object() != null) {
            //TODO: sibling order!
            List<C_non_primitive_objectContext> objects = objectsContext.c_non_primitive_object();
            for (C_non_primitive_objectContext object : objects) {
                result.add(parseNonPrimitiveObject(object));
            }
        }
        return result;
    }

    private CObject parseNonPrimitiveObject(C_non_primitive_objectContext objectContext) {
        /*
          c_complex_object
        | c_archetype_root
        | c_complex_object_proxy
        | archetype_slot
        */
        if (objectContext.c_complex_object() != null) {
            return parseComplexObject(objectContext.c_complex_object());
        } else if (objectContext.c_archetype_root() != null) {
            return null;//TODO
        } else if (objectContext.c_complex_object_proxy() != null) {
            return null;//TODO
        } else if (objectContext.archetype_slot() != null) {
            return parseArchetypeSlot(objectContext.archetype_slot());

        }
        return null;
    }

    private ArchetypeSlot parseArchetypeSlot(Archetype_slotContext slotContext) {
        ArchetypeSlot slot = new ArchetypeSlot();
        C_archetype_slot_headContext headContext = slotContext.c_archetype_slot_head();
        slot.setNodeId(headContext.c_archetype_slot_id().ID_CODE().getText());
        slot.setRmTypeName(headContext.c_archetype_slot_id().type_id().getText());
        if(headContext.c_occurrences() != null) {
            slot.setOccurences(parseMultiplicityInterval(headContext.c_occurrences()));
        }
        if(slotContext.c_excludes() != null) {
            for(AssertionContext assertionContext:slotContext.c_excludes().assertion()) {
                slot.getExcludes().add(new Assertion(assertionContext.getText()));
            }
        }
        if(slotContext.c_includes() != null) {
            for(AssertionContext assertionContext:slotContext.c_includes().assertion()) {
                slot.getIncludes().add(new Assertion(assertionContext.getText()));
            }
        }
        return slot;
    }

    private CObject parsePrimitiveObject(C_primitive_objectContext objectContext) {
        /*c_integer
                | c_real
                | c_date
                | c_time
                | c_date_time
                | c_duration
                | c_string
                | c_terminology_code
                | c_boolean*/
        if(objectContext.c_integer() != null) {
            return parseCInteger(objectContext.c_integer());
        } else if (objectContext.c_real() != null) {
            return parseCReal(objectContext.c_real());
        } else if (objectContext.c_date() != null) {
            return parseCDate(objectContext.c_date());
        } else if (objectContext.c_time() != null) {
            return parseCTime(objectContext.c_time());
        } else if (objectContext.c_date_time() != null) {
            return parseCDateTime(objectContext.c_date_time());
        } else if (objectContext.c_duration() != null) {
            return parseCDuration(objectContext.c_duration());
        } else if (objectContext.c_string() != null) {
            return parseCString(objectContext.c_string());
        } else if (objectContext.c_terminology_code() != null) {
            return parseCTerminologyCode(objectContext.c_terminology_code());
        } else if (objectContext.c_boolean() != null) {
            return parseCBoolean(objectContext.c_boolean());
        }
        return null;
    }

    private MultiplicityInterval parseMultiplicityInterval(C_existenceContext existenceContext) {
        MultiplicityInterval interval = new MultiplicityInterval();
        List<TerminalNode> integers = existenceContext.existence().INTEGER();
        if(integers.size() == 1) {
            interval.setLower(Integer.parseInt(integers.get(0).getText()));
            interval.setUpper(interval.getLower());
        } else if (integers.size() == 2) {
            interval.setLower(Integer.parseInt(integers.get(0).getText()));
            interval.setUpper(Integer.parseInt(integers.get(1).getText()));
        }
        return interval;
    }

    private MultiplicityInterval parseMultiplicityInterval(C_occurrencesContext occurrencesContext) {
        MultiplicityInterval interval = new MultiplicityInterval();
        List<TerminalNode> integers = occurrencesContext.multiplicity().INTEGER();

        if(occurrencesContext.multiplicity().SYM_INTERVAL_SEP() != null) {
            //two bounds. Only the last one can be *, according to the grammar. Doesn't seem right, does it?
            //one integer or *
            if(occurrencesContext.multiplicity().getText().contains("*")) {
                interval.setLower(Integer.parseInt(integers.get(0).getText()));
                interval.setUpperUnbounded(true);
            } else {
                interval.setLower(Integer.parseInt(integers.get(0).getText()));
                interval.setLower(Integer.parseInt(integers.get(1).getText()));
            }
        } else {
            //one integer or *
            if(occurrencesContext.multiplicity().getText().contains("*")) {
                interval.setLowerUnbounded(true);
                interval.setUpperUnbounded(true);
            } else {
                interval.setLower(Integer.parseInt(integers.get(0).getText()));
                interval.setUpper(interval.getLower());
            }
        }
        return interval;
    }
}
