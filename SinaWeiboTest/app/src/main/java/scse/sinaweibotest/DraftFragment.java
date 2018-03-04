package scse.sinaweibotest;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by HP233 on 2018/1/5.
 */

public class DraftFragment extends Fragment {
    private ListView mListView;
    private WeiboDBHelper dbHelper;
    //private View.OnClickListener deleteDrafts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View res = inflater.inflate(R.layout.fragment_draft, container, false);
        initListView(res);

        return res;
    }

    @Override
    public void onResume(){
        super.onResume();
        //每一次重新切换到该片段（不论是重新生成还是切换 都重新读取数据库内容）
        initListView(getView());
    }

    private void initListView(View view){
        dbHelper = new WeiboDBHelper(this.getActivity());
        ArrayList<HashMap<String, Object>> contents = new ArrayList<>();
        SharedPreferences pref = getActivity().getSharedPreferences("com_weibo_sdk_android", Context.MODE_APPEND);
        String userId = pref.getString("uid", null);
        if(userId == null) return;
        SparseArray<String> saContents = dbHelper.inquireDraft(userId);
        for(int i = 0; i < saContents.size(); ++i){
            //contents.add(saContents.valueAt(i));
            HashMap<String, Object> hm = new HashMap<>();
            hm.put("draftId", saContents.keyAt(i));
            hm.put("content", saContents.valueAt(i));
            contents.add(hm);
        }
        //ArrayAdapter arrayAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_expandable_list_item_1, contents);
        SimpleAdapter simpleAdapter = new SimpleAdapter(this.getActivity(),
                contents,
                R.layout.fragment_draft_list_item,
                new String[]{"content"},
                new int[]{R.id.draftContent});

        mListView = view.findViewById(R.id.drafts);
        mListView.setAdapter(simpleAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ListView listView = (ListView)adapterView;
                HashMap<String, Object> hm = (HashMap<String, Object>)listView.getItemAtPosition(i);
                //String content = (String) listView.getItemAtPosition(i);
                int draftId = (int)hm.get("draftId");
                String content = (String)hm.get("content");
                Intent intent = new Intent(DraftFragment.this.getActivity(), SendWeiboActivity.class);
                intent.putExtra("draftContent", content);
                System.out.println("draft content: " + content);
                dbHelper.deleteDraft(draftId);
                startActivity(intent);
            }
        });
    }

    private void deleteAllDraft(){
        ListAdapter adapter = this.mListView.getAdapter();
        if(adapter == null) return;
        for(int i = 0; i < adapter.getCount(); ++i){
            HashMap<String, Object> hm = (HashMap<String, Object>)adapter.getItem(i);
            //this.mListView.removeViewAt(i);
            dbHelper.deleteDraft((int)hm.get("draftId"));
        }
    }
}
