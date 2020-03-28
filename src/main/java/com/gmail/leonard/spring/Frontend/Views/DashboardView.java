package com.gmail.leonard.spring.Frontend.Views;

import com.gmail.leonard.spring.Backend.Redirector;
import com.gmail.leonard.spring.Backend.UserData.DiscordServerData;
import com.gmail.leonard.spring.Backend.UserData.ServerListData;
import com.gmail.leonard.spring.Backend.UserData.SessionData;
import com.gmail.leonard.spring.Backend.WebCommunicationClient.WebComClient;
import com.gmail.leonard.spring.Frontend.Components.Dashboard.DashboardServerListLayout;
import com.gmail.leonard.spring.Frontend.Components.Dashboard.DashboardTitle;
import com.gmail.leonard.spring.Frontend.Components.Dashboard.DashboardTitleArea;
import com.gmail.leonard.spring.Frontend.Components.IconLabel;
import com.gmail.leonard.spring.Frontend.Layouts.MainLayout;
import com.gmail.leonard.spring.Frontend.Layouts.PageLayout;
import com.gmail.leonard.spring.Frontend.Styles;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route(value = "dashboard", layout = MainLayout.class)
@CssImport("./styles/dashboard.css")
public class DashboardView extends PageLayout implements BeforeEnterObserver, HasUrlParameter<Long> {

    private SessionData sessionData;
    private VerticalLayout mainContent = new VerticalLayout();

    public DashboardView(@Autowired SessionData sessionData) {
        this.sessionData = sessionData;

        setWidthFull();
        mainContent.setPadding(false);

        if (!sessionData.isLoggedIn()) {
            VerticalLayout usedContent = generateUsedContent();
            usedContent.add(new DashboardTitle(this));
            usedContent.add(getTranslation("dashboard.redirect"));
            mainContent.add(usedContent);
        }

        add(mainContent);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionData.isLoggedIn())
            new Redirector().redirect(sessionData.getLoginUrl());
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter Long serverId) {
        if (!sessionData.isLoggedIn()) return;
        mainContent.getChildren().forEach(mainContent::remove);

        ServerListData serverListData = WebComClient.getInstance().getServerListData(sessionData).join();
        Optional<DiscordServerData> optionalServerListData;

        if (serverId == null || !(optionalServerListData = serverListData.find(serverId)).isPresent()) {
            VerticalLayout usedContent = generateUsedContent();
            usedContent.add(new DashboardTitle(this));

            if (serverListData.size() == 0) {
                Paragraph p = new Paragraph(getTranslation("dashboard.noserver"));
                p.getStyle().set("color", "var(--lumo-error-text-color)");
                usedContent.add(p);
            } else {
                usedContent.add(new DashboardServerListLayout(this, serverListData));
            }

            Hr hr = new Hr();
            hr.getStyle()
                    .set("margin-top", "32px")
                    .set("margin-bottom", "-8px");
            usedContent.add(hr);
            usedContent.add(new IconLabel(VaadinIcon.WARNING.create(), getTranslation("dashboard.admin")));
            mainContent.add(usedContent);
            return;
        }

        DiscordServerData discordServerData = optionalServerListData.get();
        VerticalLayout usedContent = generateUsedContent();

        Paragraph p = new Paragraph(getTranslation("dashboard.notavailable"));
        p.getStyle().set("color", "var(--lumo-error-text-color)");
        usedContent.add(p);

        mainContent.add(new DashboardTitleArea(sessionData, this, discordServerData));
        mainContent.add(usedContent);
    }

    public void setServer(long serverId) {
        if (serverId == 0) UI.getCurrent().navigate(DashboardView.class);
        else UI.getCurrent().navigate(DashboardView.class, serverId);
    }

    private VerticalLayout generateUsedContent() {
        VerticalLayout usedContent = new VerticalLayout();
        usedContent.addClassName(Styles.APP_WIDTH);
        usedContent.setPadding(true);
        return usedContent;
    }

}