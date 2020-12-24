package io.rocktest.modules;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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

public class Http extends RockModule {

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

        boolean matchCondition(HttpReq r,Map condition) throws URISyntaxException, IOException {
            String methodWanted=getStringParam(condition,"method");
            String uriWanted=getStringParam(condition,"uri");

            String method=r.getMethod();
            String uri=r.getUri();
            String body=r.getBody();

            LOG.debug("Check condition : method = {} - URI = {}",methodWanted,uriWanted);

            Pattern methodPattern = Pattern.compile(methodWanted,Pattern.CASE_INSENSITIVE);
            Matcher methodMatcher = methodPattern.matcher(method);
            boolean methodMatches = methodMatcher.matches();

            boolean uriMatches=false;
            if(methodMatches) {
                Pattern uriPattern = Pattern.compile(uriWanted);
                Matcher uriMatcher = uriPattern.matcher(uri);
                uriMatches = uriMatcher.matches();

                // If the URI matches, extract the groups and put them to variables
                if(uriMatches) {

                    // Clean previously defined variables which are numeric
                    ArrayList<String> tokill=new ArrayList<>();
                    scenario.getLocalContext().keySet().forEach((k) -> {if(StringUtils.isNumeric((String)k)) tokill.add(k); });
                    for (String s : tokill) {
                        scenario.getLocalContext().remove(s);
                    }

                    for (int i = 1; i <= uriMatcher.groupCount(); i++) {
                        String gr=uriMatcher.group(i);
                        LOG.debug("  group({})={}",i,gr);
                        if(gr != null)
                            scenario.getLocalContext().put("" + i, gr);
                    }

                    scenario.getLocalContext().put("uri",uri);
                    scenario.getLocalContext().put("method",method);
                    scenario.getLocalContext().put("body",body);

                    return true;
                }

            }

            return false;
        }

        Map findCondition(HttpReq r) throws URISyntaxException, IOException {

            for (int i = 0; i < conditions.size(); i++) {

                Map condition=(Map)conditions.get(i);
                if(matchCondition(r,condition)) {
                    LOG.info("Condition match");
                    return condition;
                }

            }
            LOG.info("No condition match");
            return null;
        }

        public void sendResponse(HttpExchange t,int code,String body,Map headers) throws IOException {

            if(headers!=null) {
                for (Object k : headers.keySet()) {
                    t.getResponseHeaders().set((String)k,(String)headers.get(k));
                }
            }

            if(body==null) {
                t.sendResponseHeaders(code, 0);
                t.getResponseBody().close();
            } else {
                t.sendResponseHeaders(code, body.length());
                OutputStream os = t.getResponseBody();
                os.write(body.getBytes());
                os.close();
            }

        }


        void call(Map<String,Object> paramsIn) throws IOException, InterruptedException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            String mod=getStringParam(paramsIn,"value");
            Map params=(Map)paramsIn.get("params");
            scenario.call(mod,params);
        }


        public void handle(HttpExchange t) throws IOException {

            MDC.put("position",name);
            LOG.info("======================================");

            String method=t.getRequestMethod().toLowerCase();
            URI uri=t.getRequestURI();
            String body=readBody(t);

            HttpReq r=new HttpReq(method,body,uri.toString(),t.getRequestHeaders());

            LOG.info("Receive request {} - URI = {}",method,uri.toString());
            logJson("Body:",body);

            scenario.getLocalContext().put("body",body);
            scenario.getLocalContext().put("uri",uri.toString());

            try {
                Map condition = findCondition(r);

                if (condition == null) {
                    sendResponse(t, 404, "No match for URI",null);
                }

                Map c=(Map)condition.get("call");
                if(c!=null) {
                    call(scenario.expand(c));
                }

                Map resp = scenario.expand((Map) condition.get("response"));
                int code=getIntParam(resp,"code",200);

                String bodyToSend=scenario.expand(getStringParam(resp,"body",null));
                Map headers = (Map) resp.get("headers");

                Map<String,String> mergeHeaders=new HashMap<>();
                if(this.headers!=null) {
                    this.headers.forEach((k,v) -> mergeHeaders.put((String)k,(String)v));
                }
                if(headers!=null) {
                    headers.forEach((k,v) -> mergeHeaders.put((String)k,(String)v));
                }

                sendResponse(t,code,bodyToSend,mergeHeaders);

            } catch(Exception e) {
                LOG.error("Error processing request",e);
                sendResponse(t, 500, e.getMessage(),null);
            }

            LOG.info("======================================");

        }
    }

    private static Logger LOG = LoggerFactory.getLogger(Http.class);


    private HttpResp httpGet(String url) throws IOException {

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("Content-type", "application/json");

        LOG.info("Sent Get request : "+url);

        return httpRequest(httpGet);

    }


    private HttpResp httpDelete(String url) throws IOException {

        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setHeader("Accept", "application/json");
        httpDelete.setHeader("Content-type", "application/json");

        LOG.info("Sent Delete request : "+url);

        return httpRequest(httpDelete);

    }


    private HttpResp httpPost(String url,String body) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json;charset=UTF-8");
        StringEntity entity = new StringEntity(body,"UTF-8");
        httpPost.setEntity(entity);

        LOG.info("Sent Post request : {}, body :\n{}",url,new JSONObject(body).toString(4));

        return httpRequest(httpPost);

    }

    private HttpResp httpPut(String url,String body) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();

        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Accept", "application/json");
        httpPut.setHeader("Content-type", "application/json;charset=UTF-8");
        StringEntity entity = new StringEntity(body,"UTF-8");
        httpPut.setEntity(entity);

        LOG.info("Sent Put request : {}, body :\n{}",url,new JSONObject(body).toString(4));

        return httpRequest(httpPut);

    }

    private HttpResp httpRequest(HttpUriRequest req) throws IOException {

        CloseableHttpClient client = HttpClients.createDefault();
        try {
            CloseableHttpResponse response = client.execute(req);

            Header h=response.getFirstHeader("content-type");
            String contentType=(h!=null?h.getValue():"");
            int code = response.getStatusLine().getStatusCode();

            LOG.info("HTTP response code: {}",code);

            String content="";

            if(response.getEntity()!=null) {
                content= EntityUtils.toString(response.getEntity());

                if(contentType.startsWith("application/json")) {
                    logJson("HTTP response body :"+contentType,content);
                } else {
                    LOG.info("HTTP response body : {}\n{}", contentType, content);
                }
            } else {
                LOG.info("HTTP response body empty");
            }

            return new HttpResp(response.getStatusLine().getStatusCode(),content);

        } catch (IOException e) {
            throw e;
        } finally {
            try {
                client.close();
            } catch (IOException e) {
            }
        }
    }

    public Map<String,Object> get(Map<String,Object> paramsNotExpanded) throws IOException {

        Map params=scenario.expand(paramsNotExpanded);

        String url = getStringParam(params,"url");

        LOG.info("Get {}",url);

        HashMap<String,Object> ret=new HashMap<>();

        HttpResp resp = httpGet(url);

        ret.put("code",resp.getCode());
        ret.put("body",resp.getBody());

        return ret;
    }

    public Map<String,Object> delete(Map<String,Object> paramsNotExpanded) throws IOException {
        Map params=scenario.expand(paramsNotExpanded);

        String url = getStringParam(params,"url");

        LOG.info("Get {}",url);

        HashMap<String,Object> ret=new HashMap<>();

        HttpResp resp = httpDelete(url);

        ret.put("code",resp.getCode());
        ret.put("body",resp.getBody());

        return ret;
    }

    public Map<String,Object> post(Map<String,Object> paramsNotExpanded) throws IOException {
        Map params=scenario.expand(paramsNotExpanded);

        String url = getStringParam(params,"url");
        String body = getStringParam(params,"body",null);

        LOG.info("Post {}",url);
        logJson("Body:",body);

        HashMap<String,Object> ret=new HashMap<>();

        HttpResp resp = httpPost(url,body);

        ret.put("code",resp.getCode());
        ret.put("body",resp.getBody());

        return ret;
    }

    public Map<String,Object> put(Map<String,Object> paramsNotExpanded) throws IOException {
        Map params=scenario.expand(paramsNotExpanded);

        String url = getStringParam(params,"url");
        String body = getStringParam(params,"body",null);

        LOG.info("Put {}",url);

        HashMap<String,Object> ret=new HashMap<>();

        HttpResp resp = httpPut(url,body);

        ret.put("code",resp.getCode());
        ret.put("body",resp.getBody());

        return ret;
    }


    public Map<String,Object> mock(Map<String,Object> params) throws IOException {

        int port=getIntParam(params,"port",8080);
        List conditions=getArrayParam(params,"when");
        Map headers=(Map)params.get("headers");
        String name=getStringParam(params,"name","MOCK");

        HttpServer server = HttpServer.create(new InetSocketAddress(port),10);
        server.createContext("/", new MyHandler(conditions,headers,name));
        server.setExecutor(null); // creates a default executor
        server.start();

        LOG.info("Started HTTP mock on port {}",port);

        return null;
    }

}
