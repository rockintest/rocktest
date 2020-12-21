package io.rocktest.modules;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
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
import java.util.regex.Pattern;

@Getter
public class Sql extends RockModule {

    private static Logger LOG = LoggerFactory.getLogger(Sql.class);

    private HikariConfig config = new HikariConfig();
    private HikariDataSource ds;
    private JdbcTemplate jdbcTemplate;

    private int retry=0;
    private int interval=1;

    public Sql() {
    }

    public Map<String,Object> connect(Map<String,Object> params) {

        String url = getStringParam(params,"url",true);
        String username = getStringParam(params,"user",true);
        String password = getStringParam(params,"password",true);

        Integer retry=getIntParam(params,"retry",false);
        if(retry!=null)
            this.retry=retry;

        Integer interval=getIntParam(params,"interval",false);
        if(interval!=null)
            this.interval=interval;

        config.setJdbcUrl( url );
        config.setUsername( username );
        config.setPassword( password );
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        ds = new HikariDataSource( config );

        jdbcTemplate = new JdbcTemplate(ds);

        return null;
    }

    public Map<String,Object> request(Map<String,Object> params) throws InterruptedException {

        List<String> expect = getArrayParam(params,"expect",false);
        String req = getStringParam(params,"request",true);

        LOG.info(req);

        HashMap<String,Object> last=new HashMap<>();

        for(int iRetry=1;iRetry<=retry+1;iRetry++) {
            try {

                List<String> data = jdbcTemplate.query(req, (rs, n) -> {
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
                        last.put(rsmd.getColumnName(j),f);
                    }

                    ret = ret.substring(0, ret.length() - 1);
                    last.put("0", ret);

                    LOG.info("{}", ret);
                    return ret;
                });

                if (expect != null) {
                    if (data.size() != expect.size()) {
                        throw new RuntimeException("Size does not match. Expected " + expect.size() + " elements but was " + data.size() + " elements");
                    }

                    int nb=0;
                    for (Iterator<String> iterator = data.iterator(); iterator.hasNext(); nb++) {
                        String next = iterator.next();

                        boolean found=false;
                        for(int k=0;k<expect.size();k++){
                            String expectItem = String.valueOf(expect.get(k));

                            LOG.debug("Check match {} against {}",expectItem,next);
                            if (Pattern.matches(expectItem, next)) {
                                LOG.info("Match {}", next);
                                found=true;
                                break;
                            }
                        }

                        if(!found) {
                            throw new RuntimeException("Record " + nb + " : Record does not match any regex : " + next);
                        }

                    }
                    // Once there, all is ok => break the loop
                    break;
                } else {
                    // If nothing to check => OK
                    break;
                }
            } catch (RuntimeException e) {
                // Au bout de checkRetry tentatives, on lance l'exception et le test échoue.
                if (iRetry == retry + 1) {
                    throw e;
                }
                LOG.info("No match - retry #{}", iRetry);
                Thread.sleep(interval * 1000);
            }
        }

        return last;
    }

}
