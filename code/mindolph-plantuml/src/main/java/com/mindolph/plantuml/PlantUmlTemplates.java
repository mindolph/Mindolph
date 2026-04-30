package com.mindolph.plantuml;

import com.mindolph.core.template.BaseFileTemplate;
import com.mindolph.core.template.Template;
import org.swiftboot.util.I18nHelper;

import java.util.Arrays;

/**
 * @author mindolph.com@gmail.com
 */
public class PlantUmlTemplates extends BaseFileTemplate {

    private static PlantUmlTemplates ins;

    public static PlantUmlTemplates getIns() {
        if (ins == null) {
            ins = new PlantUmlTemplates();
        }
        return ins;
    }

    private PlantUmlTemplates() {
        I18nHelper i18n = I18nHelper.getInstance();
        super.templates = Arrays.asList(
                new Template(i18n.get("plantuml.template.blank"), """
                        '%s
                        @startuml
                        title "%s"
                        
                        right footer "%s"
                        @enduml
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.default.title"), i18n.get("plantuml.template.generated.by"))),
                new Template(i18n.get("plantuml.template.sequence"), """
                        '%s
                        @startuml
                        title "%s"
                        
                        Alice -> Bob: Authentication Request
                        Bob --> Alice: Authentication Response
                        
                        Alice -> Bob: Another authentication Request
                        Alice <-- Bob: Another authentication Response
                        right footer "%s"
                        @enduml
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.default.title"), i18n.get("plantuml.template.generated.by"))),
                new Template(i18n.get("plantuml.template.usecase"), """
                        '%s
                        @startuml
                        title "%s"
                        
                        User -> (Start)
                        User --> (Use the application) : A small label
                        :Main Admin: ---> (Use the application) : This is\\nyet another\\nlabel
                        right footer "%s"
                        @enduml
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.default.title"), i18n.get("plantuml.template.generated.by"))),
                new Template(i18n.get("plantuml.template.class"), """
                        '%s
                        @startuml
                        title "%s"
                        
                        Object <|-- ArrayList
                        Object : equals()
                        ArrayList : Object[] elementData
                        ArrayList : size()
                        right footer "%s"
                        @enduml
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.default.title"), i18n.get("plantuml.template.generated.by"))),
                new Template(i18n.get("plantuml.template.activity"), """
                        '%s
                        @startuml
                        title "%s"
                        
                        start
                        :Hello world;
                        :This is defined on
                        several **lines**;
                        end
                        right footer "%s"
                        @enduml
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.default.title"), i18n.get("plantuml.template.generated.by"))),
                new Template(i18n.get("plantuml.template.component"), """
                        '%s
                        @startuml
                        title "%s"
                        
                        DataAccess - [First Component]
                        [First Component] ..> HTTP : use
                        right footer "%s"
                        @enduml
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.default.title"), i18n.get("plantuml.template.generated.by"))),
                new Template(i18n.get("plantuml.template.state"), """
                        '%s
                        @startuml
                        title "%s"
                        
                        [*] --> State1
                        State1 --> [*]
                        State1 : this is a string
                        State1 : this is another string
                        
                        State1 --> State2
                        State2 --> [*]
                        right footer "%s"
                        @enduml
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.default.title"), i18n.get("plantuml.template.generated.by"))),
                new Template(i18n.get("plantuml.template.object"), """
                        '%s
                        @startuml
                        title "%s"
                        
                        object London
                        object Washington
                        object Berlin
                        object NewYork
                        
                        map CapitalCity {
                          UK *-> London
                          USA *--> Washington
                          Germany *---> Berlin
                        }
                        
                        NewYork --> CapitalCity::USA
                        right footer "%s"
                        @enduml
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.default.title"), i18n.get("plantuml.template.generated.by"))),
                new Template(i18n.get("plantuml.template.deployment"), """
                        '%s
                        @startuml
                        title "%s"
                        
                        node Node1 as n1
                        node "Node 2" as n2
                        file f1 as "File 1"
                        cloud c1 as "this
                        is
                        a
                        cloud"
                        cloud c2 [this
                        is
                        another
                        cloud]
                        
                        n1 -> n2
                        n1 --> f1
                        f1 -> c1
                        c1 -> c2
                        right footer "%s"
                        @enduml
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.default.title"), i18n.get("plantuml.template.generated.by"))),
                new Template(i18n.get("plantuml.template.network"), """
                        '%s
                        @startuml
                        title "%s"
                        
                        nwdiag {
                          network dmz {
                            address = "210.x.x.x/24"
                        
                            web01 [address = "210.x.x.1"];
                            web02 [address = "210.x.x.2"];
                          }
                          network internal {
                            address = "172.x.x.x/24";
                        
                            web01 [address = "172.x.x.1"];
                            web02 [address = "172.x.x.2"];
                            db01;
                            db02;
                          }
                        }
                        right footer "%s"
                        @enduml
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.default.title"), i18n.get("plantuml.template.generated.by"))),
                new Template(i18n.get("plantuml.template.wireframe"), """
                        '%s
                        @startsalt
                        title "%s"
                        
                        {
                          Just plain text
                          [This is my button]
                          ()  Unchecked radio
                          (X) Checked radio
                          []  Unchecked box
                          [X] Checked box
                          "Enter text here   "
                          ^This is a droplist^
                        }
                        right footer "%s"
                        @endsalt
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.default.title"), i18n.get("plantuml.template.generated.by"))),
                new Template(i18n.get("plantuml.template.archimate"), """
                        '%s
                        @startuml
                        title "%s"
                        
                        archimate #Technology "VPN Server" as vpnServerA <<technology-device>>
                        
                        rectangle GO #lightgreen
                        rectangle STOP #red
                        rectangle WAIT #orange
                        right footer "%s"
                        @enduml
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.default.title"), i18n.get("plantuml.template.generated.by"))),
                new Template(i18n.get("plantuml.template.gantt"), """
                        '%s
                        @startgantt
                        title "%s"
                        
                        Project starts 2020-07-01
                        [Test prototype] lasts 10 days
                        [Prototype completed] happens 2020-07-10
                        [Setup assembly line] lasts 12 days
                        [Setup assembly line] starts at [Test prototype]'s end
                        right footer "%s"
                        @endgantt
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.default.title"), i18n.get("plantuml.template.generated.by"))),
                new Template(i18n.get("plantuml.template.wbs"), """
                        '%s
                        @startwbs
                        title "%s"
                        
                        * Business Process Modelling WBS
                        ** Launch the project
                        *** Complete Stakeholder Research
                        *** Initial Implementation Plan
                        ** Design phase
                        *** Model of AsIs Processes Completed
                        ****< Model of AsIs Processes Completed1
                        ****> Model of AsIs Processes Completed2
                        ***< Measure AsIs performance metrics
                        ***< Identify Quick Wins
                        right footer "%s"
                        @endwbs
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.default.title"), i18n.get("plantuml.template.generated.by"))),
                new Template(i18n.get("plantuml.template.er"), """
                        '%s
                        @startuml
                        title "%s"
                        
                        ' hide the spot
                        hide circle
                        
                        ' avoid problems with angled crows feet
                        skinparam linetype ortho
                        
                        entity "Entity01" as e01 {
                          *id : number <<generated>>
                          --
                          *name : varchar
                          description : varchar
                        }
                        
                        entity "Entity02" as e02 {
                          *id : number <<generated>>
                          --
                          *e1_id : number <<FK>>
                          other_details : varchar
                        }
                        
                        entity "Entity03" as e03 {
                          *id : varchar
                          --
                          e1_id : number <<FK>>
                          other_details : varchar
                        }
                        
                        e01 ||..o{ e02
                        e01 |o..o{ e03
                        
                        right footer "%s"
                        @enduml
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.default.title"), i18n.get("plantuml.template.generated.by"))),
                new Template(i18n.get("plantuml.template.json"), """
                        '%s
                        @startjson
                        {
                          "name": "JSON",
                          "content": {
                            "title": "%s"
                          }
                        }
                        @endjson
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.json.title"))),
                new Template(i18n.get("plantuml.template.yaml"), """
                        '%s
                        @startyaml
                        name: YAML
                        content:
                          title: %s
                        @endyaml
                        """.formatted(i18n.get("plantuml.template.created.by"), i18n.get("plantuml.template.yaml.title"))),
                new Template(i18n.get("plantuml.template.ebnf"), """
                        '%s
                        @startebnf
                        title Title
                        my_enbf = {"a", c , "a" (* Note on a *)}
                        | ? special ?
                        | "repetition", 4 * '2';
                        (* Global End Note *)
                        @endebnf
                        """.formatted(i18n.get("plantuml.template.created.by"))),
                new Template(i18n.get("plantuml.template.regex"), """
                        '%s
                        @startregex
                        title minimumRepetition
                        ab{1}c{1,}
                        @endregex
                        """.formatted(i18n.get("plantuml.template.created.by")))
        );
    }
}
