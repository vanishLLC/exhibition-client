package exhibition.gui.console.components;

/**
 * Created by Arithmo on 9/22/2017 at 5:47 PM.
 */
public interface ISourceComponent {

    void mousePressed(float mouseX, float mouseY, int mouseID);

    void mouseReleased(float mouseX, float mouseY, int mouseID);

    void drawScreen(float mouseX, float mouseY);

    void keyboardTyped(int keyTyped);

}
