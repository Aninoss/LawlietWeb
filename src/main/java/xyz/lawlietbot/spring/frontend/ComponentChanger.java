package xyz.lawlietbot.spring.frontend;

import com.vaadin.flow.component.HtmlComponent;

public class ComponentChanger {

    public static void setNotInteractive(HtmlComponent... components) {
        for(HtmlComponent component: components) {
            component.addClassName("unselectable");
            component.getElement()
                    .setAttribute("ondragstart", "return false;")
                    .setAttribute("ondrop", "return false;")
                    .setAttribute("onmousedown", "return false;");
        }
    }

}
