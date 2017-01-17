package com.onkiup.jendri.api;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onkiup.jendri.AbstractServlet;
import com.onkiup.jendri.injection.Inject;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class AbstractLogServer extends AbstractServlet {


    private static SimpleDateFormat sdf;

    protected abstract String getDatePattern();

    public AbstractLogServer() {
        sdf = new SimpleDateFormat(getDatePattern());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ErrorList errorList = SerializationService.unserialize(null, ErrorList.class, req.getReader());
        handleErrors(errorList);
        resp.setStatus(200);
        resp.getWriter().write("Got it.");
    }

    protected void handleErrors(ErrorList list) {
        Logger logger = LogManager.getLogger(getClass());
        list.forEach(error -> logger.error(error));
    }

    public static class ErrorList extends ArrayList<ClientException> {

    }

    public static class TraceList extends ArrayList<ClientStackTraceElement> {

    }

    public static class ClientException {
        private String message;
        private Long timestamp;
        private TraceList trace;

        @Override
        public String toString() {
            String date = timestamp != null ? timestamp.toString() : "";
            if (sdf != null) {
                if (timestamp != null) {
                    date = sdf.format(new Date(timestamp));
                }
            }
            String result = date + " — " + message + "\n\t";
            if (trace != null) {
                result += trace.stream().map(Object::toString).collect(Collectors.joining("\n\t"));
            }
            return result;
        }
    }

    public static class ClientStackTraceElement {
        private String file;
        private String function;
        private Integer line;
        private Integer column;

        @Override
        public String toString() {
            return "at " + (function != null ? function : "unknown")
                    + " (" + (file != null ? file : "")
                    + (line != null ? ":" + line : "")
                    + (column != null ? ":" + column : "") + ")";
        }
    }

}
