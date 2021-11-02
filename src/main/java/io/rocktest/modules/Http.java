package io.rocktest.modules;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jayway.jsonpath.JsonPath;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.rocktest.modules.annotations.NoExpand;
import io.rocktest.modules.annotations.RockWord;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import io.rocktest.Scenario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class Http extends RockModule {

    private static Logger LOG = LoggerFactory.getLogger(Http.class);

    private List<HttpServer> mocks = new ArrayList<HttpServer>();

    @Getter
    @Setter
    @AllArgsConstructor
    private class HttpReq {

        private String method;
        private String body;
        private String uri;
        private Map headers;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class HttpResp {
        private int code;
        private String body;
    }


    // Return false or throws an exception if a condition is false
    private boolean isConditionTrue(String var, String val, Http.HttpResp response, boolean throwErrorIfNotTrue) {
        if (var.equals("code")) {
            LOG.info("\tResponse code = {}", response.getCode());

            String actual = "" + response.getCode();

            Pattern p = Pattern.compile(val,Pattern.DOTALL);
            Matcher m = p.matcher(actual);

            if (!m.find()) {
                if (throwErrorIfNotTrue) {
                    fail("Status code does not match. Expected " + val + " but was " + actual);
                }
                return false;
            }

            LOG.info("OK");

        } else if (var.startsWith("response.json")) {

            String path = var.replaceFirst("response.json", "");

            if(response.getBody() == null || response.getBody().isEmpty()) {
                LOG.info("\tJSON body empty");
                if (!val.isEmpty()) {
                    if (throwErrorIfNotTrue) {
                        fail("Value JSON" + path + " does not match. Expected " + val + " but was empty");
                    }
                    return false;
                }
            }

            Object actualObject = JsonPath.parse(response.getBody()).read("$" + path);

            if (actualObject == null) {
                LOG.info("\tJSON body{} = NULL", path);

                if (!val.equals("null")) {
                    if (throwErrorIfNotTrue) {
                        fail("Value JSON" + path + " does not match. Expected " + val + " but was NULL");
                    }
                    return false;
                }

            } else {

                String actual = actualObject.toString();

                LOG.info("\tJSON body{} = {}", path, actual);

                Pattern p = Pattern.compile(val,Pattern.DOTALL);
                Matcher m = p.matcher(actual);

                if (!m.find()) {
                    if (throwErrorIfNotTrue) {
                        fail("JSON content at path " + path + " does not match REGEX. Expected " + val + " but was " + actual);
                    }
                    return false;
                }

            }
        } else {
            fail("Syntax error. Expect in HTTP clause \"" + var + " = " + val + "\".");
        }

        return true;
    }

    // TODO: multiple or in or does not work
    private boolean isSubConditionTrue(String curr, Http.HttpResp response) {
        curr = curr.substring(1, curr.length() - 1);
        if (curr.startsWith("or=")) {
            curr = curr.substring(4, curr.length() - 1);

            String subCondition = null;

            // Check if contains another sub condition and remove it from curr
            if (curr.contains("{")) {
                int startArray = curr.indexOf("{");
                int endArray = curr.lastIndexOf("}");

                subCondition = curr.substring(startArray, endArray + 1);
                curr = curr.replace(" " + subCondition + ",", "");
            }

            String[] orLinesString = curr.split(",");
            Map<String, List<String>> orLines = new HashMap<>();

            // Fold every val (that are not sub conditions) by the var checked
            for (String s : orLinesString) {
                Scenario.Variable v = scenario.extractVariable(s);
                String var = v.getVar();
                String val = v.getValue();

                if (!orLines.keySet().contains(var)) {
                    orLines.put(var, new ArrayList<>());
                }
                orLines.get(var).add(val);
            }

            // Check if one of the conditions (that are not sub conditions) is true
            boolean isOrTrue = false;
            for (String k : orLines.keySet()) {
                for (String s : orLines.get(k)) {
                    LOG.info("OR sub condition, checks whether {} = {}", k, s);
                    if (isConditionTrue(k, s, response, false)) {
                        isOrTrue = true;
                        break;
                    }
                }
                if (isOrTrue) {
                    break;
                }
            }
            // Check if we find a condition or a sub condition true
            if (!isOrTrue) {
                if (subCondition != null && isSubConditionTrue(subCondition, response)) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }


    public void find(Object expr, String regex) {
        Pattern p = Pattern.compile(regex,Pattern.DOTALL);
        Matcher m = p.matcher(String.valueOf(expr));
        if(! m.find()) {
            fail("Expression does not patch. Expected "+regex+" but was "+expr);
        }
    }


    public void httpCheck(Map<String,Object> expect, Http.HttpResp response) {
        if (expect == null) {
            return;
        }

        String code=getStringParam(expect,"code",null);
        if(code != null) {
            find(response.getCode(),code);
        }

        Map expr=getMapParam(expect,"body.json",null);
        if(expr!=null) {
            JSon module = new JSon();
            module.check(response.getBody(), null, expr);
        }

        List listToCkeck=getArrayParam(expect,"body.matches",null);
        if(listToCkeck!=null) {
            for (Object o: listToCkeck) {
                LOG.debug("Checks whether body matches {}",String.valueOf(o));
                find(response.getBody(),String.valueOf(o));
            }
        }

    }

    public void httpCheck(List<Object> expect, Http.HttpResp response) {
        if (expect == null) {
            return;
        }

        for (int i = 0; i < expect.size(); i++) {
            String curr = expect.get(i).toString();

            if (curr.startsWith("{")) {
                if (!isSubConditionTrue(curr, response)) {
                    fail("Sub condition returns false");
                }
            } else {
                Scenario.Variable v = scenario.extractVariable(curr);
                String var = v.getVar();
                String val = v.getValue();

                LOG.info("Checks whether {} = {}", var, val);

                isConditionTrue(var, val, response, true);
            }
        }
    }


    private String readBody(HttpExchange t) throws IOException {
        InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);

        int b;
        StringBuilder buf = new StringBuilder();
        while ((b = br.read()) != -1) {
            buf.append((char) b);
        }

        br.close();
        isr.close();
        return buf.toString();
    }


    @Getter
    @Setter
    @AllArgsConstructor
    // Handler for the Mock
    private class MyHandler implements HttpHandler {

        private List conditions;
        private Map headers;
        private String name;

        boolean matchCondition(HttpReq r, Map condition) throws URISyntaxException, IOException {
            String methodWanted = getStringParam(condition, "method");
            String uriWanted = getStringParam(condition, "uri");

            String method = r.getMethod();
            String uri = r.getUri();
            String body = r.getBody();

            LOG.debug("Check condition : method = {} - URI = {}", methodWanted, uriWanted);

            Pattern methodPattern = Pattern.compile(methodWanted, Pattern.CASE_INSENSITIVE);
            Matcher methodMatcher = methodPattern.matcher(method);
            boolean methodMatches = methodMatcher.matches();

            boolean uriMatches = false;
            if (methodMatches) {
                Pattern uriPattern = Pattern.compile(uriWanted);
                Matcher uriMatcher = uriPattern.matcher(uri);
                uriMatches = uriMatcher.matches();

                // If the URI matches, extract the groups and put them to variables
                if (uriMatches) {

                    // Clean previously defined variables which are numeric
                    ArrayList<String> tokill = new ArrayList<>();
                    scenario.getLocalContext().keySet().forEach((k) -> {
                        if (StringUtils.isNumeric((String) k)) tokill.add(k);
                    });
                    for (String s : tokill) {
                        scenario.getLocalContext().remove(s);
                    }

                    for (int i = 1; i <= uriMatcher.groupCount(); i++) {
                        String gr = uriMatcher.group(i);
                        LOG.debug("  group({})={}", i, gr);
                        if (gr != null)
                            scenario.getLocalContext().put("" + i, gr);
                    }

                    scenario.getLocalContext().put("uri", uri);
                    scenario.getLocalContext().put("method", method);
                    scenario.getLocalContext().put("body", body);

                    return true;
                }

            }

            return false;
        }

        Map findCondition(HttpReq r) throws URISyntaxException, IOException {

            for (int i = 0; i < conditions.size(); i++) {

                Map condition = (Map) conditions.get(i);
                if (matchCondition(r, condition)) {
                    LOG.info("Condition match");
                    return condition;
                }

            }
            LOG.info("No condition match");
            return null;
        }

        public void sendResponse(HttpExchange t, int code, String body, Map headers) throws IOException {

            if (headers != null) {
                for (Object k : headers.keySet()) {
                    t.getResponseHeaders().set((String) k, (String) headers.get(k));
                }
            }

            if (body == null) {
                t.sendResponseHeaders(code, 0);
                t.getResponseBody().close();
            } else {
                t.sendResponseHeaders(code, body.getBytes().length);
                OutputStream os = t.getResponseBody();
                os.write(body.getBytes());
                os.close();
            }

        }


        void call(Map<String, Object> paramsIn) throws IOException, InterruptedException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            String mod = getStringParam(paramsIn, "value");
            Map params = (Map) paramsIn.get("params");
            scenario.call(mod, params);
        }


        public void handle(HttpExchange t) throws IOException {

            MDC.put("position", name);
            LOG.info("======================================");

            String method = t.getRequestMethod().toLowerCase();
            URI uri = t.getRequestURI();
            String body = readBody(t);

            HttpReq r = new HttpReq(method, body, uri.toString(), t.getRequestHeaders());

            LOG.info("Receive request {} - URI = {}", method, uri.toString());

            logContent(body,t.getRequestHeaders().getFirst("Content-Type"));

            scenario.getLocalContext().put("body", body);
            scenario.getLocalContext().put("uri", uri.toString());

            try {
                Map condition = findCondition(r);

                if (condition == null) {
                    sendResponse(t, 404, "No match for URI", null);
                } else {
                    Map c = (Map) condition.get("call");
                    if (c != null) {
                        call(scenario.expand(c));
                        MDC.put("position", name);
                    }

                    Map condresp = (Map) condition.get("response");

                    if (condresp == null) {
                        fail("Condition must have a reponse field");
                    }

                    Map resp = scenario.expand(condresp);
                    int code = getIntParam(resp, "code", 200);

                    String bodyToSend = scenario.expand(getStringParam(resp, "body", null));
                    Map headers = (Map) resp.get("headers");

                    Map<String, String> mergeHeaders = new HashMap<>();
                    if (this.headers != null) {
                        this.headers.forEach((k, v) -> mergeHeaders.put((String) k, (String) v));
                    }
                    if (headers != null) {
                        headers.forEach((k, v) -> mergeHeaders.put((String) k, (String) v));
                    }

                    sendResponse(t, code, bodyToSend, mergeHeaders);

                }

            } catch (Exception e) {
                LOG.error("Error processing request", e);
                sendResponse(t, 500, e.getMessage(), null);
            }

            LOG.info("======================================");

        }
    }


    private HttpResp httpGet(String url,Map<String,Object> headers) throws IOException {

        HttpGet httpGet = new HttpGet(url);

        if(headers!=null) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpGet.setHeader(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        LOG.info("Sent Get request : " + url);

        return httpRequest(httpGet);

    }


    private HttpResp httpDelete(String url,Map<String,Object> headers) throws IOException {

        HttpDelete httpDelete = new HttpDelete(url);

        if(headers!=null) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpDelete.setHeader(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        LOG.info("Sent Delete request : " + url);

        return httpRequest(httpDelete);

    }


    String formatXML(String xml) {
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(xml);
            StringWriter sw = new StringWriter();
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter xw = new XMLWriter(sw, format);
            xw.write(doc);
            return sw.toString();
        } catch (DocumentException | IOException e) {
            LOG.warn("Cannot parse XML: {}",e.getMessage());
            return xml;
        }
    }



    String formatJSON(String json) {
        try {

            if (json == null) {
                return "null";
            } else if (json.isEmpty()) {
                return "<empty>";
            } else if (json.trim().startsWith("{")) {
                JSONObject jsonObj = new JSONObject(json);
                return jsonObj.toString(4);
            } else if (json.trim().startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                return jsonArray.toString(4);
            } else {
                return json;
            }

        } catch(Exception e) {
            LOG.warn("Cannot parse JSON: {}",e.getMessage());
            return json;
        }
    }


    void logBody(String method,String body,String url,Map<String,Object> headers) {

        String contentType=null;

        if(headers!=null) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                if (entry.getKey().toLowerCase().equals("content-type")) {
                    contentType = String.valueOf(entry.getValue());
                }
            }
        }

        LOG.info("Sent {} request : {}",method,url);
        logContent(body,contentType);
    }


    void logContent(String body,String contentType) {

        if(body==null)
            return;

        try {
            if (contentType==null) {
                LOG.info("body:\n{}", body);
            } else if (contentType.toLowerCase().contains("json")) {
                LOG.info("body {}:\n{}", contentType,formatJSON(body));
            } else if (contentType.toLowerCase().contains("xml")) {
                LOG.info("body {}:\n{}", contentType,formatXML(body));
            } else {
                LOG.info("body {}:\n{}", contentType,body);
            }
        } catch(Exception e) {
            LOG.warn("Cannot parse body: {}",e.getMessage());
            LOG.info("body {}:\n{}", (contentType==null?"":contentType),body);
        }
    }


    private HttpResp httpPost(String url, String body, Map<String,Object> headers) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(url);

        if(headers!=null) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        if(body!=null) {
            StringEntity entity = new StringEntity(body, "UTF-8");
            httpPost.setEntity(entity);
        }

         logBody("post",body,url,headers);

        return httpRequest(httpPost);

    }

    private HttpResp httpPatch(String url, String body, Map<String,Object> headers) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();

        HttpPatch httpPatch = new HttpPatch(url);

        if(headers!=null) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPatch.setHeader(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        if(body!=null) {
            StringEntity entity = new StringEntity(body, "UTF-8");
            httpPatch.setEntity(entity);
        }

         logBody("patch",body,url,headers);

        return httpRequest(httpPatch);

    }

    private HttpResp httpPut(String url, String body,Map<String,Object> headers) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();

        HttpPut httpPut = new HttpPut(url);

        if(headers!=null) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPut.setHeader(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        logBody("put",body,url,headers);


        if(body!=null) {
            StringEntity entity = new StringEntity(body, "UTF-8");
            httpPut.setEntity(entity);
        }


        return httpRequest(httpPut);

    }

    private HttpResp httpRequest(HttpUriRequest req) throws IOException {

        CloseableHttpClient client = HttpClients.createDefault();
        try {
            CloseableHttpResponse response = client.execute(req);

            Header h = response.getFirstHeader("content-type");
            String contentType = (h != null ? h.getValue() : "");
            int code = response.getStatusLine().getStatusCode();

            LOG.info("HTTP response code: {}", code);

            String content = "";

            if (response.getEntity() != null) {
                content = EntityUtils.toString(response.getEntity());

                logContent(content,contentType);

            } else {
                LOG.info("HTTP response body empty");
            }

            return new HttpResp(response.getStatusLine().getStatusCode(), content);

        } catch (IOException e) {
            throw e;
        } finally {
            try {
                client.close();
            } catch (IOException e) {
            }
        }
    }


    void check(Map<String, Object> params,HttpResp resp) {

        // Either expect is a list, or a map
        Object o = params.get("expect");
        if(o instanceof List) {
            List<Object> expect=(List<Object>)params.get("expect");
            httpCheck(expect,resp);
        } else {
            Map<String,Object> expect=(Map<String,Object>)params.get("expect");
            httpCheck(expect,resp);
        }

    }



    // Functions exposed as modules


    @RockWord(keyword="http.get")
    public Map<String, Object> get(Map<String, Object> params) throws IOException {

        String url = getStringParam(params, "url");
        Map<String,Object> headers = getMapParam(params,"headers",null);

        LOG.info("Get {}", url);

        HashMap<String, Object> ret = new HashMap<>();

        HttpResp resp = httpGet(url,headers);
        check(params,resp);

        ret.put("code", resp.getCode());
        ret.put("body", resp.getBody());

        return ret;
    }

    @RockWord(keyword="http.delete")
    public Map<String, Object> delete(Map<String, Object> params) throws IOException {

        String url = getStringParam(params, "url");
        Map<String,Object> headers = getMapParam(params,"headers",null);

        LOG.info("Get {}", url);

        HashMap<String, Object> ret = new HashMap<>();

        HttpResp resp = httpDelete(url,headers);
        check(params,resp);

        ret.put("code", resp.getCode());
        ret.put("body", resp.getBody());

        return ret;
    }

    @RockWord(keyword="http.post")
    public Map<String, Object> post(Map<String, Object> params) throws IOException {

        String url = getStringParam(params, "url");
        String body = getStringParam(params, "body", null);
        Map<String,Object> headers = getMapParam(params,"headers",null);

        LOG.info("Post {}", url);

        HashMap<String, Object> ret = new HashMap<>();

        HttpResp resp = httpPost(url, body,headers);
        check(params,resp);

        ret.put("code", resp.getCode());
        ret.put("body", resp.getBody());

        return ret;
    }

    @RockWord(keyword="http.patch")
    public Map<String, Object> patch(Map<String, Object> params) throws IOException {

        String url = getStringParam(params, "url");
        String body = getStringParam(params, "body", null);
        Map<String,Object> headers = getMapParam(params,"headers",null);

        LOG.info("Patch {}", url);

        HashMap<String, Object> ret = new HashMap<>();

        HttpResp resp = httpPatch(url, body,headers);
        check(params,resp);

        ret.put("code", resp.getCode());
        ret.put("body", resp.getBody());

        return ret;
    }

    @RockWord(keyword="http.put")
    public Map<String, Object> put(Map<String, Object> params) throws IOException {

        String url = getStringParam(params, "url");
        String body = getStringParam(params, "body", null);
        Map<String,Object> headers = getMapParam(params,"headers",null);

        LOG.info("Put {}", url);

        HashMap<String, Object> ret = new HashMap<>();

        HttpResp resp = httpPut(url, body, headers);
        check(params,resp);

        ret.put("code", resp.getCode());
        ret.put("body", resp.getBody());

        return ret;
    }


    @NoExpand
    @RockWord(keyword="http.mock")
    public Map<String, Object> mock(Map<String, Object> paramsNotExpanded) throws IOException {

        Map params = expand(paramsNotExpanded);

        int port = getIntParam(params, "port", 8080);
        List conditions = getArrayParam(paramsNotExpanded, "when");
        Map headers = (Map) paramsNotExpanded.get("headers");
        String name = getStringParam(params, "name", "MOCK");

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 10);

        server.createContext("/", new MyHandler(conditions, headers, name));
        server.setExecutor(null); // creates a default executor
        server.start();

        mocks.add(server);

        LOG.info("Started HTTP mock on port {}", port);

        return null;
    }



    @Override
    public void cleanup() {

        for (HttpServer mock : mocks) {
            try {
                mock.stop(0);
            } catch(Exception e) {
            }
        }
    }
}