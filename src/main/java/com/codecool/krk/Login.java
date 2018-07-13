package com.codecool.krk;

import com.codecool.krk.aux.MimeTypeResolver;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.*;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public class Login implements HttpHandler {

    private DAO dao;

    public Login() {
        this.dao = new DAO();
        addUserData();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws UnsupportedEncodingException, IOException {

        String method = httpExchange.getRequestMethod();
        String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");
        HttpCookie cookie;
        String response = "";

        if (method.equals("POST")) {

            InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "UTF8");
            BufferedReader br = new BufferedReader(isr);
            String inputs = br.readLine();

            Map<String, String> formData = parseInputs(inputs);
            System.out.println(formData.toString());

            for (User entry : dao.getUserData()) {
                if (formData.get("username").equals(entry.getLogin()) &&
                        (formData.get("password").equals(entry.getPass()))) {

                    JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/index.twig");
                    JtwigModel model = JtwigModel.newModel();
                    model.with("greeting", "Hello, " + formData.get("username"));
                    response = template.render(model);
                    String id = generateId();
                    cookie = new HttpCookie("sessionId", id);
                    httpExchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
                    httpExchange.sendResponseHeaders(200, response.length());
                } else {
                    System.out.println("Wrong login data!");
                }
            }

            httpExchange.sendResponseHeaders(200, response.length());
        }
        if (method.equals("GET")) {
            if (cookieStr == null) {
                URI uri = httpExchange.getRequestURI();
                String path = "." + uri.getPath();

                ClassLoader classLoader = getClass().getClassLoader();
                URL fileURL = classLoader.getResource(path);

                if (fileURL == null) {
                    send404(httpExchange);
                } else {
                    sendFile(httpExchange, fileURL);
                }
            } else {
                cookie = new HttpCookie("sessionId", generateId());
                httpExchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
            }
        }
    }

    private void addUserData() {
        dao.getUserData().add(new User("admin", "pass"));
        dao.getUserData().add(new User("user", "word"));
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

    private void send404(HttpExchange httpExchange) throws IOException {
        String response = "404, page not found";
        httpExchange.sendResponseHeaders(404, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private void sendFile(HttpExchange httpExchange, URL fileURL) throws IOException {
        File file = new File(fileURL.getFile());
        MimeTypeResolver resolver = new MimeTypeResolver(file);
        String mime = resolver.getMimeType();

        httpExchange.getResponseHeaders().set("Content-Type", mime);
        httpExchange.sendResponseHeaders(200, 0);

        OutputStream os = httpExchange.getResponseBody();
        FileInputStream fs = new FileInputStream(file);
        final byte[] buffer = new byte[0x10000];
        int count;
        while ((count = fs.read(buffer)) >= 0) {
            os.write(buffer,0,count);
        }
        os.close();
    }
}

