import javafx.scene.input.KeyCode;
import org.apache.commons.lang3.RegExUtils;

import java.awt.event.KeyEvent;

/**
 * @author mindolph.com@gmail.com
 */
public class BasicTest {

    public static void main(String[] args) {
        System.out.println((int)'\r');
        System.out.println((int)'\n');
        System.out.println(KeyCode.ENTER.getCode());

        System.out.println("awt");
        System.out.println(KeyEvent.VK_ENTER);

        System.out.println(RegExUtils.replaceAll("file.name.test.it", "[+=_`~&@,\\-\\<\\>\\.\\?\\^\\#\\$\\(\\)]+?", " "));
        System.out.println(RegExUtils.replaceAll("file.name.12.23.34", "[+=_`~&@,\\-\\<\\>\\.\\?\\^\\#\\$\\(\\)]+?", " "));
    }
}
