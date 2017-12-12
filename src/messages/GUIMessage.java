package messages;

import java.io.Serializable;

public class GUIMessage extends Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public GUIMessage() {

    }

    @Execute
    public void popGUI() {
        new GUIClient();
    }
}