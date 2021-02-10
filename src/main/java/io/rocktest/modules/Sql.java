package io.rocktest.modules;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.rocktest.RockException;
import io.rocktest.modules.annotations.RockWord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.SerializationUtils;

import java.sql.Blob;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class Sql extends RockModule {

    private static Logger LOG = LoggerFactory.getLogger(Sql.class);

    @Getter
    @Setter
    @AllArgsConstructor
    private class Connection {
        private int retry;
        private int interval;
        private JdbcTemplate jdbcTemplate;
        private HikariDataSource dataSource;
    }

    private Map<String,Connection> connections = new HashMap<>();

    public Sql() {
    }

    @Override
    public void cleanup() {
        connections.forEach((key, connection) -> {
            LOG.debug("Close SQL connection {}",key);
            try {
                connection.dataSource.close();
            } catch (Exception e) {
            }
        });

    }


    @RockWord(keyword="sql.connect")
    public Map<String, Object> connect(Map<String, Object> params) {

        String url = getStringParam(params, "url");
        String username = getStringParam(params, "user", "sa");
        String password = getStringParam(params, "password", "sa");
        String name = getStringParam(params,"name","default");

        LOG.info("Connect SQL {}, datasource = {}",name,url);

        Integer retry = getIntParam(params, "retry", 0);
        Integer interval = getIntParam(params, "interval", 0);

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        HikariDataSource ds = new HikariDataSource(config);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

        connections.put(name,new Connection(retry,interval,jdbcTemplate,ds));

        return null;
    }


    public Map<String, Object> update(Map<String, Object> params) throws InterruptedException {

        String req = getStringParam(params, "request");

        String name = getStringParam(params,"name","default");
        Connection connection=connections.get(name);
        if(connection == null) {
            fail("SQL connection "+name+" does not exist");
        }

        connection.jdbcTemplate.update(req);
        return null;

    }


    @RockWord(keyword="sql.request")
    public Map<String, Object> request(Map<String, Object> params) throws InterruptedException {

        String req = getStringParam(params, "request");

        LOG.info(req);

        if(req.trim().toLowerCase().startsWith("select")) {
            return query(params);
        } else {
            return update(params);
        }

    }



    public Map<String, Object> query(Map<String, Object> params) throws InterruptedException {

        String name = getStringParam(params,"name","default");
        Connection connection=connections.get(name);
        if(connection == null) {
            fail("SQL connection "+name+" does not exist");
        }

        List<String> expect = getArrayParam(params, "expect", null);
        String req = getStringParam(params, "request");

        HashMap<String, Object> last = new HashMap<>();

        for (int iRetry = 1; iRetry <= connection.retry + 1; iRetry++) {

            List<String> data = connection.jdbcTemplate.query(req, (rs, n) -> {
                ResultSetMetaData rsmd = rs.getMetaData();
                int max = rsmd.getColumnCount();
                last.clear();
                String ret = "";


                for (int j = 1; j <= max; j++) {
                    String f = "";
                    int t = rsmd.getColumnType(j);

                    if (t == 2004) {
                        Blob blob = rs.getBlob(j);
                        byte[] bytes = blob.getBytes(1, (int) blob.length());

                        try {
                            f = SerializationUtils.deserialize(bytes).toString();
                        } catch (Exception e) {
                            f = "<<BLOB>>";
                            LOG.warn("Cannot deserialize: ", e);
                        }
                    } else {
                        f = rs.getString(j);
                    }

                    ret += f;
                    ret += ",";
                    last.put("" + j, f);
                    last.put(rsmd.getColumnName(j), f);
                }

                ret = ret.substring(0, ret.length() - 1);


                last.put("0", ret);

                LOG.info("{}", ret);
                return ret;
            });

            try {

                if (expect != null) {
                    if (data.size() != expect.size()) {
                        fail("Size does not match. Expected " + expect.size() + " elements but was " + data.size() + " elements");
                    }

                    int nb = 0;
                    for (Iterator<String> iterator = data.iterator(); iterator.hasNext(); nb++) {
                        String next = iterator.next();

                        boolean found = false;
                        for (int k = 0; k < expect.size(); k++) {
                            String expectItem = String.valueOf(expect.get(k));

                            LOG.debug("Check match {} against {}", expectItem, next);

                            Pattern p = Pattern.compile(expectItem);//. represents single character
                            Matcher m = p.matcher(next);
                            boolean b = m.find();

                            if (b) {
                                LOG.info("Match {}", next);
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            fail("Record " + nb + " : Record does not match any regex : " + next);
                        }

                    }
                    // Once there, all is ok => break the loop
                    break;
                } else {
                    // If nothing to check => OK
                    break;
                }
            } catch (RockException e) {
                // Au bout de checkRetry tentatives, on lance l'exception et le test échoue.
                if (iRetry == connection.retry + 1) {
                    throw e;
                }
                LOG.info("No match - retry #{}", iRetry);
                Thread.sleep(connection.interval * 1000);
            }
        }

        return last;
    }

}
