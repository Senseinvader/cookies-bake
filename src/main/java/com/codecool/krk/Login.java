package com.codecool.krk;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.*;
import java.net.HttpCookie;
import java.net.URLDecoder;
import java.util.*;

public class Login implements HttpHandler {

    private DAO dao;
    private User currentUser;

    public Login() {
        this.dao = new DAO();
        addUserData();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();

        String cookieStr = parseSessionId(httpExchange);
        HttpCookie cookie;
        String response = "";

        if (method.equals("POST")) {
            InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "UTF8");
            BufferedReader br = new BufferedReader(isr);
            String inputs = br.readLine();

            Map<String, String> formData = parseInputs(inputs);

            if (findUserMatch(formData)) {
                System.out.println("loading logout");
                JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/logout.twig");
                JtwigModel model = JtwigModel.newModel();
                model.with("greeting", "Hello, " + currentUser.getLogin());
                response = template.render(model);
                String id = generateId();
                cookie = new HttpCookie("sessionId", id);
                currentUser.setSessionId(id);  // add cookie to user object
                httpExchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
                httpExchange.sendResponseHeaders(200, response.length());
            } else {
                System.out.println("Wrong login data!");
                JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/login.twig");
                JtwigModel model = JtwigModel.newModel();
                model.with("greeting", "Wrong login data!");
                response = template.render(model);
                httpExchange.sendResponseHeaders(200, response.length());
            }
        }
        if (method.equals("GET")) {
            System.out.println("we are here");
            System.out.println(cookieStr);
            if (getUserById(cookieStr) == null) {
                JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/login.twig");
                JtwigModel model = JtwigModel.newModel();
                response = template.render(model);
                httpExchange.sendResponseHeaders(200, response.length());
                System.out.println("reached this place");
            } else {
                User entry = getUserById(cookieStr);
                cookie = new HttpCookie("sessionId", cookieStr);
                JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/logout.twig");
                JtwigModel model = JtwigModel.newModel();
                model.with("greeting", "Hello, " + entry.getLogin());
                response = template.render(model);
                httpExchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
                httpExchange.sendResponseHeaders(200, response.length());
                System.out.println("reached another place");
            }
        }
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private User getUserById (String cookieStr) {
        for (User entry : dao.getUserData()) {
            System.out.println(entry.getSessionId());
            if (entry.getSessionId().equals(cookieStr)) {
                System.out.println("yes");
                return entry;
            }
        }
        System.out.println("no");
        return null;
    }

    private void addUserData() {
        dao.getUserData().add(new User("admin", "pass"));
        dao.getUserData().add(new User("user", "word"));
    }

    private boolean findUserMatch(Map<String, String> formData) {
        boolean match = false;
        for (User entry : dao.getUserData()) {
            if (formData.get("username").equals(entry.getLogin()) &&
                    (formData.get("password").equals(entry.getPass()))) {
                System.out.println("true true))");
                currentUser = entry;
                match = true;
            }
        }
        return match;
    }

    private String generateId() {
        Random random = new Random();
        String allSybmols = "!#$%&()*+-0123456789<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ^_abcdefghijklmnopqrstuvwxyz";
        char [] id = new char[10];
        for (int i=0; i<10; i++) {
            id[i] = allSybmols.charAt(random.nextInt(allSybmols.length()));
        }
        return new String(id);
    }

    private static Map<String, String> parseInputs(String inputs) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        String [] pairs = inputs.split("&");
        for (String element : pairs) {
            String [] keyValue = element.split("=");
            String value = URLDecoder.decode(keyValue[1], "UTF8");
            map.put(keyValue[0], value);
        }
        return map;
    }

    private String parseSessionId(HttpExchange httpExchange) {
        if (httpExchange.getRequestHeaders().getFirst("Cookie") != null) {
            String [] idPair = httpExchange.getRequestHeaders().getFirst("Cookie").split("=");
            return idPair[1].replaceAll("\"", "");
        }
        return null;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}

