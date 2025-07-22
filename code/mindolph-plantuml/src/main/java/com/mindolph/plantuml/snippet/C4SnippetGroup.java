package com.mindolph.plantuml.snippet;

import com.mindolph.core.model.Snippet;

import java.util.Arrays;

/**
 * @since 1.12
 */
public class C4SnippetGroup extends BasePlantUmlSnippetGroup {

    @Override
    public String getTitle() {
        return "C4 Model";
    }

    @Override
    public void init() {
        super.snippets.addAll(
                Arrays.asList(
                        new Snippet().title("Import System Context").code("!include <C4/C4_Context>"),
                        new Snippet().title("Import Container").code("!include <C4/C4_Container>"),
                        new Snippet().title("Import Component").code("!include <C4/C4_Component>"),
                        new Snippet().title("Import Dynamic").code("!include <C4/C4_Dynamic>"),
                        new Snippet().title("Import Deployment").code("!include <C4/C4_Deployment>"),
//                        new Snippet().title("Import Sequence").code("!include <C4/C4_Sequence>"),


                        // System Context
                        new Snippet().title("Person").code("""
                                Person("person", "Person", $descr="Person", $sprite="person", $tags="", $link="", $type="")"""),
                        new Snippet().title("Person_Ext").code("""
                                Person_Ext("person_ext", "Person_Ext", $descr="Person_Ext", $sprite="person2", $tags="", $link="", $type="")"""),
                        new Snippet().title("System").code("""
                                System("system", "System", $descr="System", $sprite="", $tags="", $link="", $type="", $baseShape="")"""),
                        new Snippet().title("System_Ext").code("""
                                System_Ext("system_ext", "System_Ext", $descr="System_Ext", $sprite="", $tags="", $link="", $type="", $baseShape="")"""),
                        new Snippet().title("SystemDb").code("""
                                SystemDb("systemdb", "SystemDb", $descr="SystemDb", $sprite="", $tags="", $link="", $type="")"""),
                        new Snippet().title("SystemQueue").code("""
                                SystemQueue("systemqueue", "SystemQueue", $descr="SystemQueue", $sprite="", $tags="", $link="", $type="")"""),
                        new Snippet().title("SystemDb_Ext").code("""
                                SystemDb_Ext("systemdb_ext", "SystemDb_Ext", $descr="SystemDb_Ext", $sprite="", $tags="", $link="", $type="")"""),
                        new Snippet().title("SystemQueue_Ext").code("""
                                SystemQueue_Ext("systemqueue_ext", "SystemQueue_Ext", $descr="SystemQueue_Ext", $sprite="", $tags="", $link="", $type="")"""),
                        new Snippet().title("Boundary").code("""
                                Boundary("boundary", "Boundary", $type="", $tags="", $link="", $descr="Boundary"){
                                ⨁
                                }
                                """),
                        new Snippet().title("Enterprise_Boundary").code("""
                                Enterprise_Boundary("enterprise_boundary", "Enterprise_Boundary", $tags="", $link="", $descr="Enterprise_Boundary"){
                                ⨁
                                }
                                """),
                        new Snippet().title("System_Boundary").code("""
                                System_Boundary("system_boundary", "System_Boundary", $tags="", $link="", $descr="System_Boundary"){
                                ⨁
                                }
                                """),

                        // Container
                        new Snippet().title("Container").code(
                                """
                                Container("container", "Container", $techn="", $descr="Container", $sprite="", $tags="", $link="", $baseShape="")
                                """),
                        new Snippet().title("ContainerDb").code(
                                """
                                ContainerDb("containerdb", "ContainerDb", $techn="", $descr="ContainerDb", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("ContainerQueue").code(
                                """
                                ContainerQueue("containerqueue", "ContainerQueue", $techn="", $descr="ContainerQueue", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("Container_Ext").code(
                                """
                                Container_Ext("container_ext", "Container_Ext", $techn="", $descr="Container_Ext", $sprite="", $tags="", $link="", $baseShape="")
                                """),
                        new Snippet().title("ContainerDb_Ext").code(
                                """
                                ContainerDb_Ext("containerdb_ext", "ContainerDb_Ext", $techn="", $descr="ContainerDb_Ext", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("ContainerQueue_Ext").code(
                                """
                                ContainerQueue_Ext("containerqueue_ext", "ContainerQueue_Ext", $techn="", $descr="ContainerQueue_Ext", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("Container_Boundary").code(
                                """
                                Container_Boundary("container_boundary", "Container_Boundary", $tags="", $link="", $descr="Container_Boundary")
                                """),


                        // Component
                        new Snippet().title("Component").code(
                                """
                                Component("component", "Component", $techn="", $descr="Component", $sprite="", $tags="", $link="", $baseShape="")
                                """),
                        new Snippet().title("ComponentDb").code(
                                """
                                ComponentDb("componentdb", "ComponentDb", $techn="", $descr="ComponentDb", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("ComponentQueue").code(
                                """
                                ComponentQueue("componentqueue", "ComponentQueue", $techn="", $descr="ComponentQueue", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("Component_Ext").code(
                                """
                                Component_Ext("component_ext", "Component_Ext", $techn="", $descr="Component_Ext", $sprite="", $tags="", $link="", $baseShape="")
                                """),
                        new Snippet().title("ComponentDb_Ext").code(
                                """
                                ComponentDb_Ext("componentdb_ext", "ComponentDb_Ext", $techn="", $descr="ComponentDb_Ext", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("ComponentQueue_Ext").code(
                                """
                                ComponentQueue_Ext("componentqueue_ext", "ComponentQueue_Ext", $techn="", $descr="ComponentQueue_Ext", $sprite="", $tags="", $link="")
                                """),


                        // Deployment diagram
                        // https://github.com/plantuml-stdlib/C4-PlantUML?tab=readme-ov-file#deployment-diagram
                        new Snippet().title("Deployment_Node").code(
                                """
                                Deployment_Node("deployment_node", "Deployment_Node", $type="", $descr="Deployment_Node", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("Node").code(
                                """
                                Node("node", "Node", $type="", $descr="Node", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("Node_L").code(
                                """
                                Node_L("node_l", "Node_L", $type="", $descr="Node_L", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("Node_R").code(
                                """
                                Node_R("node_r", "Node_R", $type="", $descr="Node_R", $sprite="", $tags="", $link="")
                                """),

                        // Relationship Types
                        new Snippet().title("Rel").code(
                                """
                                Rel(from, to, "Rel", $techn="", $descr="", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("BiRel").code(
                                """
                                BiRel(from, to, "BiRel", $techn="", $descr="", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("Rel_U").code(
                                """
                                Rel_U(from, to, "Rel_U", $techn="", $descr="", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("Rel_Up").code(
                                """
                                Rel_Up(from, to, "Rel_Up", $techn="", $descr="", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("Rel_D").code(
                                """
                                Rel_D(from, to, "Rel_D", $techn="", $descr="", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("Rel_Down").code(
                                """
                                Rel_Down(from, to, "Rel_Down", $techn="", $descr="", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("Rel_L").code(
                                """
                                Rel_L(from, to, "Rel_L", $techn="", $descr="", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("Rel_Left").code(
                                """
                                Rel_Left(from, to, "Rel_Left", $techn="", $descr="", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("Rel_R").code(
                                """
                                Rel_R(from, to, "Rel_R", $techn="", $descr="", $sprite="", $tags="", $link="")
                                """),
                        new Snippet().title("Rel_Right").code(
                                """
                                Rel_Right(from, to, "Rel_Right", $techn="", $descr="", $sprite="", $tags="", $link="")
                                """),

                        // Layout (arrange) elements (without relationships)
                        new Snippet().title("Lay_U").code("Lay_U(from, to)"),
                        new Snippet().title("Lay_Up").code("Lay_Up(from, to)"),
                        new Snippet().title("Lay_D").code("Lay_D(from, to)"),
                        new Snippet().title("Lay_Down").code("Lay_Down(from, to)"),
                        new Snippet().title("Lay_L").code("Lay_L(from, to)"),
                        new Snippet().title("Lay_Left").code("Lay_Left(from, to)"),
                        new Snippet().title("Lay_R").code("Lay_R(from, to)"),
                        new Snippet().title("Lay_Right").code("Lay_Right(from, to)"),

                        // Tags & Styles
                        // https://github.com/plantuml-stdlib/C4-PlantUML?tab=readme-ov-file#custom-tagsstereotypes-support-and-skinparam-updates
                        new Snippet().title("AddElementTag").code("AddElementTag(tagStereo, $bgColor=\"\", $fontColor=\"\", $borderColor=\"\", $shadowing=\"\", $shape=\"\", $sprite=\"\", $techn=\"\", $legendText=\"\", $legendSprite=\"\", $borderStyle=\"\", $borderThickness=\"\")").description("Introduces a new element tag. The styles of the tagged elements are updated and the tag is displayed in the calculated legend."),
                        new Snippet().title("AddRelTag").code("AddRelTag(tagStereo, $textColor=\"\", $lineColor=\"\", $lineStyle=\"\", $sprite=\"\", $techn=\"\", $legendText=\"\", $legendSprite=\"\", $lineThickness=\"\")").description("Introduces a new Relationship tag. The styles of the tagged relationships are updated and the tag is displayed in the calculated legend."),
                        new Snippet().title("AddBoundaryTag").code("AddBoundaryTag(tagStereo, $bgColor=\"\", $fontColor=\"\", $borderColor=\"\", $shadowing=\"\", $shape=\"\", $type=\"\", $legendText=\"\", $borderStyle=\"\", $borderThickness=\"\", $borderStyle=\"\", $borderThickness=\"\", $sprite=\"\", $legendSprite=\"\")").description("Introduces a new Boundary tag. The styles of the tagged boundaries are updated and the tag is displayed in the calculated legend."),
                        new Snippet().title("AddPersonTag").code("AddPersonTag(tagStereo, $bgColor=\"\", $fontColor=\"\", $borderColor=\"\", $shadowing=\"\", $shape=\"\", $sprite=\"\", $legendText=\"\", $legendSprite=\"\", $type=\"\", $borderStyle=\"\", $borderThickness=\"\")"),
                        new Snippet().title("AddExternalPersonTag").code("AddExternalPersonTag(tagStereo, $bgColor=\"\", $fontColor=\"\", $borderColor=\"\", $shadowing=\"\", $shape=\"\", $sprite=\"\", $legendText=\"\", $legendSprite=\"\", $type=\"\", $borderStyle=\"\", $borderThickness=\"\")"),
                        new Snippet().title("AddSystemTag").code("AddSystemTag(tagStereo, $bgColor=\"\", $fontColor=\"\", $borderColor=\"\", $shadowing=\"\", $shape=\"\", $sprite=\"\", $legendText=\"\", $legendSprite=\"\", $type=\"\", $borderStyle=\"\", $borderThickness=\"\")"),
                        new Snippet().title("AddExternalSystemTag").code("AddExternalSystemTag(tagStereo, $bgColor=\"\", $fontColor=\"\", $borderColor=\"\", $shadowing=\"\", $shape=\"\", $sprite=\"\", $legendText=\"\", $legendSprite=\"\", $type=\"\", $borderStyle=\"\", $borderThickness=\"\")"),
                        new Snippet().title("AddComponentTag").code("AddComponentTag(tagStereo, $bgColor=\"\", $fontColor=\"\", $borderColor=\"\", $shadowing=\"\", $shape=\"\", $sprite=\"\", $techn=\"\", $legendText=\"\", $legendSprite=\"\", $borderStyle=\"\", $borderThickness=\"\")"),
                        new Snippet().title("AddExternalComponentTag").code("AddExternalComponentTag(tagStereo, $bgColor=\"\", $fontColor=\"\", $borderColor=\"\", $shadowing=\"\", $shape=\"\", $sprite=\"\", $techn=\"\", $legendText=\"\", $legendSprite=\"\", $borderStyle=\"\", $borderThickness=\"\")"),
                        new Snippet().title("AddContainerTag").code("AddContainerTag(tagStereo, $bgColor=\"\", $fontColor=\"\", $borderColor=\"\", $shadowing=\"\", $shape=\"\", $sprite=\"\", $techn=\"\", $legendText=\"\", $legendSprite=\"\", $borderStyle=\"\", $borderThickness=\"\")"),
                        new Snippet().title("AddExternalContainerTag").code("AddExternalContainerTag(tagStereo, $bgColor=\"\", $fontColor=\"\", $borderColor=\"\", $shadowing=\"\", $shape=\"\", $techn=\"\", $sprite=\"\", $legendText=\"\", $legendSprite=\"\", $borderStyle=\"\", $borderThickness=\"\")"),
                        new Snippet().title("AddNodeTag").code("AddNodeTag(tagStereo, $bgColor=\"\", $fontColor=\"\", $borderColor=\"\", $shadowing=\"\", $shape=\"\", $sprite=\"\", $techn=\"\", $legendText=\"\", $legendSprite=\"\", $borderStyle=\"\", $borderThickness=\"\"").description("node specific: $type reuses $techn definition of $tags"),
                        new Snippet().title("UpdateElementStyle").code("UpdateElementStyle(elementName, $bgColor=\"\", $fontColor=\"\", $borderColor=\"\", $shadowing=\"\", $shape=\"\", $sprite=\"\", $techn=\"\", $legendText=\"\", $legendSprite=\"\", $borderStyle=\"\", $borderThickness=\"\")").description("This call updates the default style of the elements (component, ...) and creates no additional legend entry."),
                        new Snippet().title("UpdateRelStyle").code("UpdateRelStyle(textColor, lineColor)").description("This call updates the default relationship colors and creates no additional legend entry."),
                        new Snippet().title("UpdateBoundaryStyle").code("UpdateBoundaryStyle($elementName=\"\", $bgColor=\"\", $fontColor=\"\", $borderColor=\"\", $shadowing=\"\", $shape=\"\", $type=\"\", $legendText=\"\", $borderStyle=\"\", $borderThickness=\"\", $sprite=\"\", $legendSprite=\"\")").description("This call updates the default style of the existing boundaries and creates no additional legend entry. If the element name is \"\" then it updates generic, enterprise, system and container boundary style in on call."),
                        new Snippet().title("UpdateContainerBoundaryStyle").code("UpdateContainerBoundaryStyle($bgColor=\"\", $fontColor=\"\", $borderColor=\"\", $shadowing=\"\", $shape=\"\", $type=\"\", $legendText=\"\", $borderStyle=\"\", $borderThickness=\"\", $sprite=\"\", $legendSprite=\"\")"),
                        new Snippet().title("UpdateSystemBoundaryStyle").code("UpdateSystemBoundaryStyle($bgColor=\"\", $fontColor=\"\", $borderColor=\"\", $shadowing=\"\", $shape=\"\", $type=\"\", $legendText=\"\", $borderStyle=\"\", $borderThickness=\"\", $sprite=\"\", $legendSprite=\"\")"),
                        new Snippet().title("UpdateEnterpriseBoundaryStyle").code("UpdateEnterpriseBoundaryStyle($bgColor=\"\", $fontColor=\"\", $borderColor=\"\", $shadowing=\"\", $shape=\"\", $type=\"\", $legendText=\"\", $borderStyle=\"\", $borderThickness=\"\", $sprite=\"\", $legendSprite=\"\")"),
                        new Snippet().title("UpdateLegendTitle").code("UpdateLegendTitle(newTitle)"),
                        new Snippet().title("RoundedBoxShape").code("RoundedBoxShape()").description("This call returns the name of the rounded box shape and can be used as $shape=\"\" argument."),
                        new Snippet().title("EightSidedShape").code("EightSidedShape()").description("This call returns the name of the eight sided shape and can be used as $shape=\"\" argument."),
                        new Snippet().title("DashedLine").code("DashedLine()").description("This call returns the name of the dashed line and can be used as $lineStyle=\"\" or $borderStyle=\"\" argument."),
                        new Snippet().title("DottedLine").code("DottedLine()").description("This call returns the name of the dotted line and can be used as $lineStyle=\"\" or $borderStyle=\"\" argument."),
                        new Snippet().title("BoldLine").code("BoldLine()").description("This call returns the name of the bold line and can be used as $lineStyle=\"\" or $borderStyle=\"\" argument."),
                        new Snippet().title("SolidLine").code("SolidLine()").description("This call returns the name of the solid line and can be used as $lineStyle=\"\" or $borderStyle=\"\" argument (enables e.g. a reset of dashed boundaries)."),

                        // Element and Relationship properties
                        // https://github.com/plantuml-stdlib/C4-PlantUML?tab=readme-ov-file#element-and-relationship-properties
                        new Snippet().title("SetPropertyHeader").code("SetPropertyHeader(col1Name, $col2Name=\"\", $col3Name=\"\", $col4Name=\"\")").description("The properties table can have up to 4 columns. The default header uses the column names \"Name\", \"Description\"."),
                        new Snippet().title("WithoutPropertyHeader").code("WithoutPropertyHeader()").description("If no header is used, then the second column is bold."),
                        new Snippet().title("AddProperty").code("AddProperty(col1, $col2=\"\", $col3=\"\", $col4=\"\")").description("(All columns of) a property which will be added to the next element."),

                        // Layout Options
                        // https://github.com/plantuml-stdlib/C4-PlantUML/blob/master/LayoutOptions.md#show_floating_legendalias-hidestereotype-details-and-legend
                        new Snippet().title("OPTION: LAYOUT_TOP_DOWN").code("LAYOUT_TOP_DOWN()"),
                        new Snippet().title("OPTION: LAYOUT_LEFT_RIGHT").code("LAYOUT_LEFT_RIGHT()").description("rotates the flow visualization to from Left to Right and directed relations like Rel_Left(), Rel_Right(), Rel_Up() and Rel_Down() are rotated too."),
                        new Snippet().title("OPTION: LAYOUT_LANDSCAPE").code("LAYOUT_LANDSCAPE()").description("rotates the default flow visualization to from Left to Right like LAYOUT_LEFT_RIGHT() additional directed relations like Rel_Left(), Rel_Right(), Rel_Up() and Rel_Down() are not rotated anymore."),
                        new Snippet().title("OPTION: LAYOUT_WITH_LEGEND").code("LAYOUT_WITH_LEGEND()"),
                        new Snippet().title("OPTION: SHOW_LEGEND").code("SHOW_LEGEND($hideStereotype(\"\"), $details(\"\"))"),
                        new Snippet().title("OPTION: SHOW_FLOATING_LEGEND").code("SHOW_FLOATING_LEGEND($alias(\"\"), $hideStereotype(\"\"), $details(\"\"))"),
                        new Snippet().title("OPTION: LEGEND").code("LEGEND()"),
                        new Snippet().title("OPTION: LAYOUT_AS_SKETCH").code("LAYOUT_AS_SKETCH()"),
                        new Snippet().title("OPTION: SET_SKETCH_STYLE").code("SET_SKETCH_STYLE($bgColor(\"\"), $fontColor(\"\"), $warningColor(\"\"), $fontName(\"\"), $footerWarning(\"\"), $footerText(\"\"))"),
                        new Snippet().title("OPTION: HIDE_STEREOTYPE").code("HIDE_STEREOTYPE()").description("To enable a layout without <<stereotypes>> and legend."),
                        new Snippet().title("OPTION: HIDE_PERSON_SPRITE").code("HIDE_PERSON_SPRITE()"),
                        new Snippet().title("OPTION: SHOW_PERSON_SPRITE").code("SHOW_PERSON_SPRITE($sprite(\"\"))"),
                        new Snippet().title("OPTION: SHOW_PERSON_PORTRAIT").code("SHOW_PERSON_PORTRAIT()"),
                        new Snippet().title("OPTION: SHOW_PERSON_OUTLINE").code("SHOW_PERSON_OUTLINE()")
                        // this is for sequence diagram which is not supported for now, add to KEYWORD list either if enabled
//                        new Snippet().title("OPTION: SHOW_ELEMENT_DESCRIPTIONS").code("SHOW_ELEMENT_DESCRIPTIONS($show(\"\"))"),
//                        new Snippet().title("OPTION: SHOW_FOOT_BOXES").code("SHOW_FOOT_BOXES($show(\"\"))"),
//                        new Snippet().title("OPTION: SHOW_INDEX").code("SHOW_INDEX($show(\"\"))")

                        )

        );
    }
}
