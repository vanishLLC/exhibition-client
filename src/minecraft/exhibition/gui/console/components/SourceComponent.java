package exhibition.gui.console.components;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arithmo on 9/22/2017 at 5:47 PM.
 */
public abstract class SourceComponent implements ISourceComponent {

    private List<SourceComponent> components = new ArrayList<>();

    @Override
    public void mousePressed(float mouseX, float mouseY, int mouseID) {

    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, int mouseID) {

    }

    @Override
    public void drawScreen(float mouseX, float mouseY) {

    }

    @Override
    public void keyboardTyped(int keyTyped) {

    }

    public void addComponent(SourceComponent sourceComponent) {
        this.components.add(sourceComponent);
    }

    public List<SourceComponent> getComponents() {
        return this.components;
    }

}
