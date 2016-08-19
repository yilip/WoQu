package com.lip.woqu.utils.net;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.lip.woqu.ApplicationManager;
import com.lip.woqu.utils.MyPreferences;
import com.lip.woqu.utils.SysParams;
import com.lip.woqu.utils.UtilsManager;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NetManager {
    static private NetManager instance = null;

    /**
     * Context 无需再传20120601，传入null即可
     */
    static public NetManager getInstance() {
        if (instance == null) {
            instance = new NetManager();
        }
        return instance;
    }

    private NetManager() {
        userAgentStr = equipUserAgent(ApplicationManager.ctx);
        try {
            client = new OkHttpClient();
        }catch (Error error){
            isOKHttpCaseException = true;
        } catch (Exception e) {//OKHttp只支持2.3版本以上的系统！
            isOKHttpCaseException = true;
            e.printStackTrace();
        }
    }
    private String userAgentStr="";
    private boolean isOKHttpCaseException = false;
    private OkHttpClient client = null;
    public static final MediaType JSON = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    /**当前版本如果不支持OKHttp，则使用备用的NetManager来处理*/
    private StandbyNetManager standbyNetManager=null;

    /**
     * 上传文件到又拍云服务器
     * @param folder   policy signature 又拍云参数
     * @param filePath 文件路径
     * @throws java.io.IOException
     */
    public String updateFileToUpYun(String folder, String policy, String signature, String filePath, NetCustomMultiPartEntity.ProgressListener listener) throws IOException {
        StringBuffer sb = new StringBuffer();
        HttpClient httpClient = null;
        InputStream in = null;
        BufferedReader reader = null;
        try {
            httpClient = new DefaultHttpClient(); // getHttpClient();
            HttpPost httppost = new HttpPost(SysParams.uploadFileURL + folder + "/");
            NetCustomMultiPartEntity mpEntity = new NetCustomMultiPartEntity(listener);
            File file = new File(filePath);
            mpEntity.addPart("file", new FileBody(file));
            mpEntity.addPart("policy", new StringBody(policy));
            mpEntity.addPart("signature", new StringBody(signature));
            httppost.setEntity(mpEntity);

            HttpResponse response = null;
            response = httpClient.execute(httppost);
            HttpEntity resEntity = response.getEntity();

            in = resEntity.getContent();
            reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (ClientProtocolException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (in != null) {
                    in.close();
                }
                if (httpClient != null) {
                    httpClient.getConnectionManager().shutdown();
                }
            } catch (Exception e) {
            }
        }
        return sb.toString();
    }

    /**
     * 上传文件到又拍云服务器(免登陆)
     * @param filePath 文件路径
     * @throws java.io.IOException
     */
    public String updateFileToUpYun(String filePath, NetCustomMultiPartEntity.ProgressListener listener) throws IOException {
        StringBuffer sb = new StringBuffer();
        HttpClient httpClient = null;
        InputStream in = null;
        BufferedReader reader = null;
        try {
            httpClient = new DefaultHttpClient(); // getHttpClient();
            HttpPost httppost = new HttpPost(SysParams.uploadFileURL);
            UtilsManager.println("url=" + SysParams.uploadFileURL);
            NetCustomMultiPartEntity mpEntity = new NetCustomMultiPartEntity(listener);
            File file = new File(filePath);
            mpEntity.addPart("file", new FileBody(file));
            httppost.setEntity(mpEntity);

            HttpResponse response = null;
            response = httpClient.execute(httppost);
            HttpEntity resEntity = response.getEntity();
            in = resEntity.getContent();
            reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (ClientProtocolException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (in != null) {
                    in.close();
                }
                if (httpClient != null) {
                    httpClient.getConnectionManager().shutdown();
                }
            } catch (Exception e) {
            }
        }
        return sb.toString();
    }

    /**
     * 执行post返回String
     * 参数一定不要编码(方法内有编码处理)
     */
    public String doPostAsString(String url, Hashtable<String, String> tableData) {
        StringBuffer sb = new StringBuffer();
        try {
            if (isOKHttpCaseException||client==null){
                if (standbyNetManager==null){
                    standbyNetManager=new StandbyNetManager();
                }
                sb.append(standbyNetManager.doPostAsString(url,tableData));
            }else{
                NetParams params = new NetParams();
                if (tableData != null) {
                    Enumeration<String> enu = tableData.keys();
                    String key = "";
                    while (enu.hasMoreElements()) {
                        key = enu.nextElement();
                        String val = tableData.get(key);
                        params.addParam(key, val);
                    }
                }
                sb.append(post(url, params.getParamsAsString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return sb.toString();
        }
    }

    /**
     * get方法获取结果，并将结果转化为字符串
     * 参数一定不要编码(方法内有编码处理)
     */
    public String doGetAsString(String url, Hashtable<String, String> table) {
        NetParams params = new NetParams();
        if (table != null) {
            Enumeration<String> enu = table.keys();
            String key = "";
            while (enu.hasMoreElements()) {
                key = enu.nextElement();
                String val = table.get(key);
                params.addParam(key, val);
            }
        }
        return doGetAsString(url, params);
    }

    /**
     * get方法获取结果，并将结果转化为字符串,该方法可以保证参数顺序不变
     * 参数一定不要编码(方法内有编码处理)
     */
    public String doGetAsString(String url, NetParams params) {
        StringBuffer sb = new StringBuffer();
        try {
            String getUrl = url;
            if (params != null) {
                if (url.contains("?")) {
                    getUrl = url + params.getParamsAsString();
                } else {
                    getUrl = url + "?" + params.getParamsAsString();
                }
            }
            if (isOKHttpCaseException||client==null){
                if (standbyNetManager==null){
                    standbyNetManager=new StandbyNetManager();
                }
                sb.append(standbyNetManager.doGetAsString(getUrl));
            }else{
                sb.append(get(getUrl));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return sb.toString();
        }
    }
    /**
     * get方法获取结果，并将结果转化为字符串,该方法可以保证参数顺序不变
     * 参数一定不要编码(方法内有编码处理)
     * 无参数
     */
    public String doGetAsString(String url) {
        StringBuffer sb = new StringBuffer();
        try {
            String getUrl = url;
            if (isOKHttpCaseException||client==null){
                if (standbyNetManager==null){
                    standbyNetManager=new StandbyNetManager();
                }
                sb.append(standbyNetManager.doGetAsString(getUrl));
            }else{
                sb.append(get(getUrl));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return sb.toString();
        }
    }

    private String post(String url, String json) throws IOException {
        UtilsManager.println("url="+url+"\ndata="+json);
        RequestBody body = RequestBody.create(JSON, json);
        if(TextUtils.isEmpty(userAgentStr)){
            userAgentStr = equipUserAgent(ApplicationManager.ctx);
        }
        Request request = new Request.Builder()
                .url(url)
                .post(body).addHeader("user-agent",userAgentStr)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private String get(String url) throws IOException {
        UtilsManager.println("url="+url);
        Request request = new Request.Builder().addHeader("user-agent",userAgentStr)
                .url(url).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /**
     * 判断wifi是否可用
     */
    public static boolean isWiFiActive(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.getTypeName().equals("WIFI")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否有可用网络
     */
    public static boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }// end isNetworkAvailable


    /**该类主要处理2.3以下手机无法使用OKHTTP问题*/
    private class StandbyNetManager {

        /** post方法获取结果，并将结果转化为字符串,
         *  参数一定不要编码*/
        public String doPostAsString(String url,Hashtable<String, String> tableData) {
            StringBuffer result=new StringBuffer();
            HttpClient httpClient = getHttpClient();
            InputStream inputStream = null;
            BufferedReader reader = null;
            HttpPost httpRequest = new HttpPost(url);
            List<BasicNameValuePair> httpParams = new ArrayList<BasicNameValuePair>();
            if (tableData != null) {
                Enumeration<String> enu = tableData.keys();
                String key = "";
                while (enu.hasMoreElements()) {
                    key = enu.nextElement();
                    String val = tableData.get(key);
                    httpParams.add(new BasicNameValuePair(key, val));
                }
            }
            try {
                httpRequest.setEntity(new UrlEncodedFormEntity(httpParams, HTTP.UTF_8));
                HttpResponse httpResponse = null;
                httpResponse = httpClient.execute(httpRequest);
                /** 返回类型，是否是gzip压缩流 */
                String contentEncoding = null;
                Header[] contentEncodings = httpResponse.getHeaders("Content-Encoding");
                if (contentEncodings != null) {
                    for (Header h : contentEncodings) {
                        if (h.getValue() != null && !h.getValue().equals("")) {
                            contentEncoding = h.getValue();
                        }
                    }
                }// end contentEncodings!=null
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    try {
                        inputStream =httpResponse.getEntity().getContent();
                        if (contentEncoding != null&& contentEncoding.equals("gzip")) {
                            inputStream = new GZIPInputStream(inputStream);
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(inputStream!=null){
                        reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line = "";
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                }
            }
            return result.toString();
        }

        /** get方法获取结果，并将结果转化为字符串,
         *  参数一定要编码*/
        private String doGetAsString(String url) {
            StringBuffer result=new StringBuffer();
            HttpClient httpClient = getHttpClient();
            HttpGet httpRequest = null;
            InputStream inputStream = null;
            BufferedReader reader = null;
            HttpResponse httpResponse = null;
            try {
                httpRequest = new HttpGet(url);
                httpResponse = httpClient.execute(httpRequest);
                /** 返回类型，是否是gzip压缩流 */
                String contentEncoding = null;
                Header[] contentEncodings = httpResponse.getHeaders("Content-Encoding");
                if (contentEncodings != null) {
                    for (Header h : contentEncodings) {
                        if (h.getValue() != null && !h.getValue().equals("")) {
                            contentEncoding = h.getValue();
                        }
                    }
                }// end contentEncodings!=null
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    HttpEntity httpEntity = httpResponse.getEntity();
                    inputStream = httpEntity.getContent();
                    if (contentEncoding != null && contentEncoding.equals("gzip")) {
                        inputStream = new GZIPInputStream(inputStream);
                    }
                    if(inputStream!=null){
                        reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line = "";
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                }
            }
            return result.toString();
        }
        private synchronized HttpClient getHttpClient() {
            HttpClient httpClient = null;
            if (httpClient == null) {
                try {
                    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    trustStore.load(null, null);
                    SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
                    sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                    final HttpParams httpParams = new BasicHttpParams();
                    // timeout: connect to the server
                    ConnManagerParams.setTimeout(httpParams, 2000);
                    HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
                    // timeout: transfer data from server
                    HttpConnectionParams.setSoTimeout(httpParams, 20000);

                    HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
                    HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
                    //设置UserAgent
                    if(TextUtils.isEmpty(userAgentStr)){
                        userAgentStr = equipUserAgent(ApplicationManager.ctx);
                    }
                    HttpProtocolParams.setUserAgent(httpParams, userAgentStr);
                    HttpClientParams.setRedirecting(httpParams, true);
                    HttpConnectionParams.setTcpNoDelay(httpParams, true);

                    SchemeRegistry schemeRegistry = new SchemeRegistry();
                    schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                    schemeRegistry.register(new Scheme("https", sf, 443));

                    ClientConnectionManager manager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
                    httpClient = new DefaultHttpClient(manager, httpParams);
                } catch (Exception e) {
                    httpClient = new DefaultHttpClient();
                }
            }
            return httpClient;
        }
        /**Https问题*/
        public class SSLSocketFactoryEx extends SSLSocketFactory {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            public SSLSocketFactoryEx(KeyStore truststore)throws NoSuchAlgorithmException, KeyManagementException,
                    KeyStoreException, UnrecoverableKeyException {
                super(truststore);
                TrustManager tm = new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    @Override
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] chain,
                            String authType)
                            throws java.security.cert.CertificateException {
                    }
                    @Override
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] chain,
                            String authType)
                            throws java.security.cert.CertificateException {
                    }
                };
                sslContext.init(null, new TrustManager[] { tm }, null);
            }
            @Override
            public Socket createSocket(Socket socket, String host, int port,
                                       boolean autoClose) throws IOException, UnknownHostException {
                return sslContext.getSocketFactory().createSocket(socket, host,port, autoClose);
            }
            @Override
            public Socket createSocket() throws IOException {
                return sslContext.getSocketFactory().createSocket();
            }
        }

    }//end StandbyNetManager

    /**封装userAgent*/
    public static String equipUserAgent(Context ctx){
        StringBuilder sb =new StringBuilder();
        sb.append(" ssy={Android;ECalendar;");
        ApplicationInfo ai;
        try {
            ai = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
            PackageInfo info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            sb.append("V"+info.versionName+";");
            sb.append(String.valueOf(ai.metaData.get("UMENG_CHANNEL"))+";");
            MyPreferences myPreferences=MyPreferences.getInstance(ctx);
            JSONObject location= myPreferences.getLocation();
            sb.append(location.optString("cityKey2",""));
            ConnectivityManager connectivity = (ConnectivityManager) ctx
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo net_info = connectivity.getActiveNetworkInfo();
                if (net_info != null) {
                    sb.append(";"+net_info.getTypeName());
                }
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return sb.append("}").toString();
    }

}
