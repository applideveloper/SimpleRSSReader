package jp.wishmatch.simplerssreader;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class RssParserTask extends AsyncTask<String, Integer, RssListAdapter>{

    private MainActivity mActivity;
    private RssListAdapter mAdapter;
    private ProgressDialog mProgressDialog;

    public RssParserTask(MainActivity mActivity, RssListAdapter mAdapter) {
        this.mActivity = mActivity;
        this.mAdapter = mAdapter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // ブログレスバーを表示する
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setMessage("Now Loading...");
        mProgressDialog.show();
    }

    @Override
    protected void onPostExecute(RssListAdapter rssListAdapter) {
        super.onPostExecute(rssListAdapter);
        mProgressDialog.dismiss();
        mActivity.setListAdapter(rssListAdapter);
    }

    // バックグラウンドにおける処理を担う。タスク実行時に渡された値を引数とする
    @Override
    protected RssListAdapter doInBackground(String... params) {
        RssListAdapter result = null;

        try {
            // HTTP経由でアクセスして、InputStreamを取得する
            URL url = new URL(params[0]);
            InputStream inputStream = url.openConnection().getInputStream();
            result = parseXml(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // XMLをパースする
    public RssListAdapter parseXml(InputStream inputStream) throws IOException, XmlPullParserException {
        XmlPullParser xmlPullParser = Xml.newPullParser();
        try {
            xmlPullParser.setInput(inputStream, null);
            int eventType = xmlPullParser.getEventType();
            Item currentItem = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tag = null;
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = xmlPullParser.getName();
                        if (tag.equals("item")) {
                            currentItem = new Item();
                        } else if (currentItem != null) {
                            if (tag.equals("title")) {
                                currentItem.setTitle(xmlPullParser.nextText());
                            } else if(tag.equals("description")) {
                                currentItem.setDescription(xmlPullParser.nextText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tag =xmlPullParser.getName();
                        if (tag.equals("item")) {
                            mAdapter.add(currentItem);
                        }
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mAdapter;
    }
}