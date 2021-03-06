package xyz.lawlietbot.spring.backend.userdata;

import bell.oauth.discord.main.OAuthBuilder;
import bell.oauth.discord.main.Response;
import xyz.lawlietbot.spring.backend.util.StringUtil;
import xyz.lawlietbot.spring.frontend.layouts.PageLayout;
import xyz.lawlietbot.spring.frontend.views.DiscordLogin;
import xyz.lawlietbot.spring.frontend.views.HomeView;
import com.vaadin.flow.server.*;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

@Component
@VaadinSessionScope
public class SessionData {

    private OAuthBuilder builder;
    private final String id;
    private DiscordUser discordUser = null;
    private Class<? extends PageLayout> currentTarget = HomeView.class;

    public SessionData() {
        id = StringUtil.getRandomString();
        setData();
    }

    private void setData() {
        builder = new OAuthBuilder(System.getenv("BOT_CLIENT_ID"), System.getenv("BOT_CLIENT_SECRET"))
                .setScopes(new String[]{"identify"})
                .setRedirectURI(getCurrentDomain() + PageLayout.getRouteStatic(DiscordLogin.class));
    }

    private String getCurrentDomain() {
        VaadinRequest vaadinRequest = VaadinService.getCurrentRequest();
        HttpServletRequest httpServletRequest = ((VaadinServletRequest)vaadinRequest).getHttpServletRequest();
        String requestUrl = httpServletRequest.getRequestURL().toString().replace("//", "|");
        return requestUrl.substring(0, requestUrl.indexOf('/')).replace("|", "//") + "/";
    }

    public String getLoginUrl() {
        return builder.getAuthorizationUrl(id) + "&prompt=none";
    }

    public boolean login(String code, String state, UIData uiData) {
        if (state.equals(id)) {
            Response response = builder.exchange(code);
            if (response != Response.ERROR) {
                discordUser = new DiscordUser(builder.getUser());
                uiData.login(discordUser.getId());
                return true;
            }
        }
        return false;
    }

    public void logout(UIData uiData) {
        if (isLoggedIn()) {
            uiData.logout();
            setData();
            discordUser = null;
        }
    }

    public Optional<DiscordUser> getDiscordUser() {
        return Optional.ofNullable(discordUser);
    }

    public boolean isLoggedIn() {
        return discordUser != null;
    }

    public void setCurrentTarget(Class<? extends PageLayout> c) {
        currentTarget = c;
    }

    public Class<? extends PageLayout> getCurrentTarget() {
        return currentTarget;
    }

}
