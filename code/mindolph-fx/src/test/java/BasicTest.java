import javafx.scene.input.KeyCode;

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
    }
}
