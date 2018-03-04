package scse.sinaweibotest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * Created by HP233 on 2017/10/15.
 */

public class WebViewAcitvity extends Activity {
    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        webView = (WebView)findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new InJavaScriptLocalObj(WebViewAcitvity.this), "javaScriptMethod");
        webView.setWebViewClient(new MyWebViewClient());
        webView.loadUrl("javascript:showInfoFromJava(test)");
    }


    final class MyWebViewClient extends WebViewClient {

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d("WebView","onPageStarted");
            super.onPageStarted(view, url, favicon);
        }
        public void onPageFinished(WebView view, String url) {
            Log.d("WebView","onPageFinished ");
            view.loadUrl("javascript:window.local_obj.showSource(''+" +
                    "document.getElementsByTagName('html')[0].innerHTML+'');");
            super.onPageFinished(view, url);
        }
    }

    final class InJavaScriptLocalObj {

        private Context mcontext;

        public InJavaScriptLocalObj(Context mcontext){
            this.mcontext = mcontext;
        }

        @JavascriptInterface
        public void SSOController(){
            Toast.makeText(this.mcontext, "Test JavaScript", Toast.LENGTH_LONG);
        }
    }
}
