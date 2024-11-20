package com.aischool.goodswap.service.board;

import org.springframework.stereotype.Service;

@Service
public class HtmlEscapeService {

    public String escapeHtml(String input) {
        if (input == null) return null;
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
