package com.mindolph.plantuml;

import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author mindolph.com@gmail.com
 */
public class PlantUmlTest {
    static String code = """
            @startuml
            Alice->Alice: This is a signal to self.\\nIt also demonstrates\\nmultiline \\ntext
            @enduml
            """;

    static String codeWithError = """
            @startuml
            Alice->Alice: This is a signal to self.\\nIt also demonstrates\\nmultiline \\ntext
            x
            @enduml
            """;

    public static void main(String[] args) {
        try {
            while (true) {
                int read = System.in.read();
                if ('1' == read) {
                    generate(code);
                }
                else if ('2' == read) {
                    generate(codeWithError);
                }
                else if ('0' == read) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generate(String text) {
        System.out.println("Generate: \n" + text);
        SourceStringReader reader = new SourceStringReader(text);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            DiagramDescription diagramDescription = reader.outputImage(os, 0);
            System.out.println(diagramDescription);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
