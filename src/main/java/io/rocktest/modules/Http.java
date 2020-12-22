package io.rocktest.modules;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Http extends RockModule {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class HttpResp {
        private int code;
        private String body;
    }

    // Handler for the Mock
    private class MyHandler implements HttpHandler {

        private List conditions;

        public MyHandler(List conditions) {
            this.conditions = conditions;
        }

        boolean matchCondition(HttpExchange t,Map condition) throws URISyntaxException {
            String methodWanted=getStringParam(condition,"method").toLowerCase();
            URI uriWanted=new URI(getStringParam(condition,"uri"));

            String method=t.getRequestMethod().toLowerCase();
            URI uri=t.getRequestURI();

            LOG.debug("Check condition : method = {} - URI = {}",methodWanted,uriWanted.toString());

            return methodWanted.equals(methodWanted) && uri.equals(uriWanted);
        }

        Map findCondition(HttpExchange t) throws URISyntaxException {

            for (int i = 0; i < conditions.size(); i++) {

                Map condition=(Map)conditions.get(i);
                if(matchCondition(t,condition)) {
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


        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();

            String method=t.getRequestMethod().toLowerCase();
            URI uri=t.getRequestURI();

            LOG.info("Receive request {} - URI = {}",method,uri.toString());

            try {
                Map condition = findCondition(t);

                if (condition == null) {
                    sendResponse(t, 404, "No match for URI",null);
                }

                Map resp = (Map) condition.get("response");
                int code=getIntParam(resp,"code",200);
                String body=getStringParam(resp,"body",null);
                Map headers = (Map) resp.get("headers");

                sendResponse(t,code,body,headers);

            } catch(Exception e) {
                sendResponse(t, 500, e.getMessage(),null);
            }
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

    public Map<String,Object> get(Map<String,Object> params) throws IOException {
        String url = getStringParam(params,"url");

        LOG.info("Get {}",url);

        HashMap<String,Object> ret=new HashMap<>();

        HttpResp resp = httpGet(url);

        ret.put("code",resp.getCode());
        ret.put("body",resp.getBody());

        return ret;
    }

    public Map<String,Object> delete(Map<String,Object> params) throws IOException {
        String url = getStringParam(params,"url");

        LOG.info("Get {}",url);

        HashMap<String,Object> ret=new HashMap<>();

        HttpResp resp = httpDelete(url);

        ret.put("code",resp.getCode());
        ret.put("body",resp.getBody());

        return ret;
    }

    public Map<String,Object> post(Map<String,Object> params) throws IOException {
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

    public Map<String,Object> put(Map<String,Object> params) throws IOException {
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

        HttpServer server = HttpServer.create(new InetSocketAddress(port),10);
        server.createContext("/", new MyHandler(conditions));
        server.setExecutor(null); // creates a default executor
        server.start();

        LOG.info("Started HTTP mock on port {}",port);


/*
        String url = getStringParam(params,"url",true);
        String body = getStringParam(params,"body",true);


        HashMap<String,Object> ret=new HashMap<>();

        HttpResp resp = httpPut(url,body);

        ret.put("code",resp.getCode());
        ret.put("body",resp.getBody());
*/
        return null;
    }

}
