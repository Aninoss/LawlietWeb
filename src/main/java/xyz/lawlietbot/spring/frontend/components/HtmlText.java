package xyz.lawlietbot.spring.frontend.components;

import com.vaadin.flow.component.html.Div;
import org.apache.commons.lang3.StringEscapeUtils;

public class HtmlText extends Div {

    public HtmlText(String html) {
        getElement().setProperty("innerHTML", StringEscapeUtils.escapeHtml4(html).replace("\n", "<br>"));
    }

}
