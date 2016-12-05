package com.example.ysg.mylistview;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    private ListView lv;
    private List<News> news;//声明存储新闻标题与内容的List
     private int total=1;//计数器(设置默认从1开始)用于集合内数据初始化
     MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main);
        lv= (ListView) findViewById(R.id.lv);
        //为当前ListView设置OnScrollListener实现分页刷新
        lv.setOnScrollListener(this);
        //将login_item(下拉刷新效果的item)通过布局 填充器声明
        View v = getLayoutInflater().inflate(R.layout.login_item,null);
        //将login_item设置到ListView页脚
        lv.addFooterView(v);
        //实例化存储内容资源的List
        news = new ArrayList<>();
        //调用初始化List的方法
        initList();
        adapter = new MyAdapter();
        //设置单击item的事件
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                show(view);//事件处理为调用show方法（显示AlertDialog对话框）
            }
        });
        lv.setAdapter(adapter);
    }


    public void show(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        TextView title = (TextView) v.findViewById(R.id.textView);
        TextView message = (TextView) v.findViewById(R.id.textView2);
        builder.setTitle(title.getText().toString());
        builder.setMessage(message.getText().toString());
        builder.setPositiveButton("已经浏览完毕", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }


    //初始化List内的元素，模拟每次可刷新10条信息
    private void initList() {
        for(int i=1;i<=10;i++){
            News n = new News();
            //加total是因为total在刷新页面后不会继续从一开始
            n.title = "Title--"+total;
            n.message="Message"+total;
            news.add(n);
            total++;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {

        /*判断是否刷新页面之前，解释一下scrollState的三种状态
        * 1.scrollState = SCROLL_STATE_TOUCH_SCROLL为手指按住屏幕滚动（未脱离屏幕）；
        * 2.scrollState = SCROLL_STATE_FLING可以理解为手指离开屏幕前，用力滑了一下，
        *       手指离开后，页面已然保持滚动；
        * 3.scrollState = SCROLL_STATE_IDLE手指未接触屏幕，且屏幕页面保持静止
        * 开启刷新页面的线程前，确保ListView已经到最后一行（Item）并且屏幕页面保持静止
        * */
        if(isLastRow&&scrollState==SCROLL_STATE_IDLE){
            new Thread(new MyThread()).start();
        }

    }

    boolean isLastRow=false;//判断是否到ListView的最后一个item
    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {



        //firstVisibleItem位可见页面的第一条在Arraylist中的下标，visibleItemCount为当前页面item数
//        currenVisibleItemCount = firstVisibleItem+visibleItemCount-1=totalItemCount;（演示用）
        if(firstVisibleItem+visibleItemCount==totalItemCount&&totalItemCount>0){
            isLastRow=true;//判断已经到最后一个item（即为footerView）
        }

    }



    //创建分页刷新线程（模拟刷新）
    class MyThread implements Runnable{

        @Override
        public void run() {
            try {
                Thread.sleep(500);//设置线程休眠时间为500毫秒刷新一次
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            initList();//重新初始化List
            //线程内调用Handler执行页面刷新（后面会写文对handler进行详细剖析）
            handler.sendEmptyMessage(1);
        }
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    //强制调用适配器的getView来刷新每个Item的内容。
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    //自定义适配器
    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return news.size();
        }

        @Override
        public Object getItem(int position) {
            return news.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item, null);
                vh = new ViewHolder();
                vh.message = (TextView) convertView.findViewById(R.id.textView2);
                vh.title = (TextView) convertView.findViewById(R.id.textView);
                convertView.setTag(vh);
            }
            vh = (ViewHolder) convertView.getTag();
            vh.title.setText(news.get(position).title);
            vh.message.setText(news.get(position).message);
            return convertView;
        }

        class ViewHolder {
            TextView title;
            TextView message;
        }

    }
}
