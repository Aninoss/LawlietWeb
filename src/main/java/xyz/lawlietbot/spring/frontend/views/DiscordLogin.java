package xyz.lawlietbot.spring.frontend.views;

import xyz.lawlietbot.spring.backend.userdata.SessionData;
import xyz.lawlietbot.spring.backend.userdata.UIData;
import xyz.lawlietbot.spring.frontend.components.CustomNotification;
import xyz.lawlietbot.spring.frontend.layouts.PageLayout;
import xyz.lawlietbot.spring.NoLiteAccess;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Route(value = "discordlogin")
@NoLiteAccess
public class DiscordLogin extends PageLayout implements HasUrlParameter<String> {

    public DiscordLogin(@Autowired SessionData sessionData, @Autowired UIData uiData) {
        super(sessionData, uiData);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        SessionData sessionData = getSessionData();
        UIData uiData = getUiData();

        Location location = event.getLocation();
        QueryParameters queryParameters = location
                .getQueryParameters();

        Map<String, List<String>> parametersMap =
                queryParameters.getParameters();

        if (parametersMap != null && parametersMap.containsKey("code") && parametersMap.containsKey("state")) {
            String code = parametersMap.get("code").get(0);
            String state = parametersMap.get("state").get(0);

            if (!sessionData.login(code, state, uiData)) {
                CustomNotification.showError(getTranslation("login.error"));
            }
        }

        Class<? extends Component> resumeClass = sessionData.isLoggedIn() ? (Class<? extends Component>) sessionData.getCurrentTarget() : HomeView.class;

        UI.getCurrent().navigate(resumeClass);
        event.rerouteTo(resumeClass);
    }

}
