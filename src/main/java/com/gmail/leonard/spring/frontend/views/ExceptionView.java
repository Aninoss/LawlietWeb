package com.gmail.leonard.spring.frontend.views;

import com.gmail.leonard.spring.backend.userdata.SessionData;
import com.gmail.leonard.spring.backend.userdata.UIData;
import com.gmail.leonard.spring.frontend.layouts.ErrorLayout;
import com.gmail.leonard.spring.frontend.layouts.MainLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "exception", layout = MainLayout.class)
public class ExceptionView extends ErrorLayout {

    public ExceptionView(@Autowired SessionData sessionData, @Autowired UIData uiData) {
        super(sessionData, uiData, "exception");
    }

}