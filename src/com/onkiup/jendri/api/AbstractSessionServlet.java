package com.onkiup.jendri.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onkiup.jendri.AbstractServlet;
import com.onkiup.jendri.session.Cookies;

public abstract class AbstractSessionServlet extends AbstractServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SessionQueryModel query = SerializationService.unserialize(null, SessionQueryModel.class, req.getReader());
        SessionDataModel response = new SessionDataModel();
        for (String field : query) {
            response.put(field, Cookies.get(field).toString());
        }
        SerializationService.serialize(null, response, resp.getWriter());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SessionDataModel query = SerializationService.unserialize(null, SessionDataModel.class, req.getReader());
        query.entrySet().stream().forEach(entry -> Cookies.set(entry.getKey(), entry.getValue()));
        SerializationService.serialize(null, query, resp.getWriter());
    }

    protected class SessionDataModel extends HashMap<String, String> {

    }

    protected class SessionQueryModel extends ArrayList<String> {

    }
}
