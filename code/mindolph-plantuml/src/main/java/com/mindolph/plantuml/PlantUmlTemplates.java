package com.mindolph.plantuml;

import com.mindolph.core.template.BaseFileTemplate;
import com.mindolph.core.template.Template;

import java.util.Arrays;

/**
 * @author mindolph.com@gmail.com
 */
public class PlantUmlTemplates extends BaseFileTemplate {

    private static final PlantUmlTemplates ins = new PlantUmlTemplates();

    public static PlantUmlTemplates getIns() {
        return ins;
    }

    private PlantUmlTemplates() {
        super.templates = Arrays.asList(
                new Template("Blank", """
                        @startuml
                        title "%s"
                        
                        @enduml
                        """),
                new Template("Sequence Diagram", """
                        @startuml
                        title "%s"
                        
                        Alice -> Bob: Authentication Request
                        Bob --> Alice: Authentication Response
                                                
                        Alice -> Bob: Another authentication Request
                        Alice <-- Bob: Another authentication Response
                        @enduml
                        """),
                new Template("Use Case Diagram", """
                        @startuml
                        title "%s"
                        
                        User -> (Start)
                        User --> (Use the application) : A small label
                        :Main Admin: ---> (Use the application) : This is\\nyet another\\nlabel
                        @enduml
                        """),
                new Template("Class Diagram", """
                        @startuml
                        title "%s"
                        
                        Object <|-- ArrayList
                        Object : equals()
                        ArrayList : Object[] elementData
                        ArrayList : size()
                        @enduml
                        """),
                new Template("Activity Diagram", """
                        @startuml
                        title "%s"
                        
                        :Hello world;
                        :This is defined on
                        several **lines**;
                        @enduml
                        """),
                new Template("Component Diagram", """
                        @startuml
                        title "%s"
                        
                        DataAccess - [First Component]
                        [First Component] ..> HTTP : use
                        @enduml
                        """),
                new Template("State Diagram", """
                        @startuml
                        title "%s"
                        
                        [*] --> State1
                        State1 --> [*]
                        State1 : this is a string
                        State1 : this is another string
                        
                        State1 -> State2
                        State2 --> [*]
                        @enduml
                        """),
                new Template("Object Diagram", """
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
                        @enduml
                        """),
                new Template("Deployment Diagram", """
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
                        @enduml
                        """),
                new Template("Network Diagram", """
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
                        @enduml
                        """),
                new Template("Wireframe", """
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
                        @endsalt
                        """),
                new Template("Archimate Diagram", """
                        @startuml
                        title "%s"
                        
                        archimate #Technology "VPN Server" as vpnServerA <<technology-device>>
                                                
                        rectangle GO #lightgreen
                        rectangle STOP #red
                        rectangle WAIT #orange
                        @enduml
                        """),
                new Template("Gantt Diagram", """
                        @startgantt
                        title "%s"
                        
                        Project starts 2020-07-01
                        [Test prototype] lasts 10 days
                        [Prototype completed] happens 2020-07-10
                        [Setup assembly line] lasts 12 days
                        [Setup assembly line] starts at [Test prototype]'s end
                        @endgantt
                        """),
                new Template("WBS Diagram", """
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
                        @endwbs
                        """),
                new Template("ER Diagram", """
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
                                                
                        @enduml
                        """),
                new Template("JSON", """
                        @startjson
                        {
                            "name": "JSON",
                            "content": {
                                "title": "This is a JSON script"
                             }
                        }
                        @endjson
                        """),
                new Template("YAML", """
                        @startyaml
                        name: YAML
                        content:
                            title: This is a YAML script
                        @endyaml
                        """)
        );
    }
}