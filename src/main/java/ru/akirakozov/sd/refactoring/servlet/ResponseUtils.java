package ru.akirakozov.sd.refactoring.servlet;

import javax.servlet.http.HttpServletResponse;

public class ResponseUtils {
    public static void setContentTypeAndOKStatus(HttpServletResponse response) {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
