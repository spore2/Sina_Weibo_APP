package scse.sinaweibotest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by HP233 on 2017/10/10.
 */

public class HttpUtil {

    public static String logIn(String strUrlPath, String account, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "login");
        params.put("response_type", "code");
        params.put("client_id", Constants.APP_KEY);
        params.put("isLoginSina", "0");
        params.put("redirect_uri", Constants.REDIRECT_URL);
        params.put("state", "");
        params.put("withOfficalAccount", "0");
        params.put("ticket", "ST-NTUxODUzMDkyMA%3D%3D-1508003589-gz-EAE0770F6AD65478387B86C8A9F1D5C8-1");
        params.put("userId", account);
        params.put("passwd", password);

        //"https://api.weibo.com/oauth2/authorize?client_id="+clientId+"&redirect_uri="+redirectURI+"&from=sina&response_type=code"

        byte [] data = HttpUtil.getRequestData(params, "UTF-8").toString().getBytes();
        HttpURLConnection conn = null;

        try{
            URL url = new URL(strUrlPath);
            conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("Referer", "https://open.weibo.cn/oauth2/authorize?client_id=92981932&redirect_uri=https://api.weibo.com/oauth2/default.html&from=sina&response_type=code");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("Host", "api.weibo.com");
            conn.setRequestProperty("user-agent",
                    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
            //conn.setInstanceFollowRedirects(false);
            //HttpURLConnection.setFollowRedirects(false);
            //conn.setRequestProperty("action");
            OutputStream os = conn.getOutputStream();
            os.write(data);
            os.flush();
            os.close();
            int response = conn.getResponseCode();
            if(response == conn.HTTP_OK || response == conn.HTTP_MOVED_PERM || response == conn.HTTP_MOVED_TEMP){
                InputStream is = conn.getInputStream();
                //Map<String, List<String>> head = conn.getHeaderFields();
                if(response != conn.HTTP_OK) System.out.println(conn.getHeaderField("Location"));
                return HttpUtil.dealResponseResult(is);
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            conn.disconnect();
        }
        return "-1";
    }

    public static String submitPostData(String strUrlPath, Map<String, String> params, String encode){
        byte [] data = HttpUtil.getRequestData(params, encode).toString().getBytes();
        HttpURLConnection conn = null;

        try{
            URL url = new URL(strUrlPath);

            conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            OutputStream os = conn.getOutputStream();
            os.write(data);
            os.flush();
            os.close();
            //PrintWriter pw = new PrintWriter(conn.getOutputStream());
            //pw.print();
            int response = conn.getResponseCode();
            if(response == conn.HTTP_OK){
                InputStream is = conn.getInputStream();
                System.out.println("Response Head:" + response);
                return HttpUtil.dealResponseResult(is);
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            conn.disconnect();
        }
        return "-1";
    }

    public static StringBuffer getRequestData(Map<String, String> params, String encode){
        StringBuffer sb = new StringBuffer();
        try{
            for(Map.Entry<String, String> entry : params.entrySet()){
                sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), encode)).append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
        }catch(Exception e){
            e.printStackTrace();
        }
        return sb;
    }

    public static String dealResponseResult(InputStream is){
        /*String resultData = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte [] data = new byte [1024];
        int len = 0;
        try{
            while((len = is.read()) != -1){
                bos.write(data, 0, len);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        resultData = new String(bos.toByteArray());
        /*try{
            resultData = URLDecoder.decode(resultData, "UTF-8");
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }*/
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        StringBuilder resultData = new StringBuilder();
        try{
            while((line = br.readLine()) != null){
                resultData.append(line + "\n");
            }
            br.close();
            is.close();
        }catch(IOException e){
            e.printStackTrace();
        }

        return resultData.toString();
    }
}
