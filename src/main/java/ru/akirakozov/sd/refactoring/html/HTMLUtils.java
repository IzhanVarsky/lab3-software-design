package ru.akirakozov.sd.refactoring.html;

import java.util.Arrays;

public class HTMLUtils {
    public static String wrapWithTag(String tag, String content) {
        return "<%s>%s</%s>".formatted(tag, content, tag);
    }

    public static String h1Wrap(String content) {
        return wrapWithTag("h1", content);
    }

    public static String pairToHTMLString(String a, Object b) {
        return a + "\t" + b + "</br>";
    }

    public static String constructHTMLResponse(Object... objects) {
        StringBuilder sj = new StringBuilder();
        Arrays.stream(objects).forEach(sj::append);
        return wrapWithTag("html", wrapWithTag("body", sj.toString()));
    }
}
