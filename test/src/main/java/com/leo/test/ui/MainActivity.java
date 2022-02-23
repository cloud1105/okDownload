package com.leo.test.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.okdownload.DownloadCallback;
import com.leo.okdownload.DownloadManager;
import com.leo.okdownload.DownloadWatcher;
import com.leo.okdownload.model.DownloadEntry;
import com.leo.okdownload.util.LogUtls;
import com.leo.test.R;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "leo";
    private DownloadListAdapter adapter;
    private DownloadCallback callback;
    private List<DownloadEntry> downloadEntryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtls.debug("onCreate");
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtls.debug("onResume");
        RecyclerView recyclerView = findViewById(R.id.downloadList);
        downloadEntryList = getDownloadList();
        LogUtls.debug("get start");
        for (int i = 0; i < downloadEntryList.size(); i++) {
            DownloadEntry realEntry = DownloadManager.getInstance(this).queryDownloadEntry(
                    downloadEntryList.get(i).getTaskId());
            if (realEntry != null) {
                downloadEntryList.remove(i);
                downloadEntryList.add(i, realEntry);
            }
        }
        LogUtls.debug("get over");
        adapter = new DownloadListAdapter(this, downloadEntryList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        callback = new DownloadCallback() {
            @Override
            public void refreshUi(DownloadEntry entry) {
                int index = downloadEntryList.indexOf(entry);
                if (index <= -1) {
                    LogUtls.debug("no entry match");
                    return;
                }
                downloadEntryList.remove(index);
                downloadEntryList.add(index, entry);
                adapter.notifyDataSetChanged();
            }
        };
        DownloadWatcher.getInstance(this).registerCallback(callback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DownloadWatcher.getInstance(this).unregisterCallback(callback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            if (item.getTitle().equals("pause all")) {
                item.setTitle("recover all");
                DownloadManager.getInstance(this).pauseAll();
            } else {
                item.setTitle("pause all");
                DownloadManager.getInstance(this).recoverAll();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private List getDownloadList() {
        // todo 从数据库或者网络获取数据
        List list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            DownloadEntry entry = new DownloadEntry("www.baidu.com","File"+i);
            list.add(entry);
        }
        return list;
    }

    private class DownloadListAdapter extends RecyclerView.Adapter<DownloadListAdapter.ViewHolder> {
        private List<DownloadEntry> list;
        private Context context;

        public DownloadListAdapter(Context context, List list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.download_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DownloadEntry entry = list.get(position);
            DownloadEntry.Status status = entry.getStatus();
            holder.imgAppIcon.setImageResource(R.mipmap.ic_launcher);
            holder.txvFileName.setText(MessageFormat.format("{0}:状态是{1} 下载进度：{2}/{3}",
                    entry.getFileName(), entry.getStatus().toString(), entry.getCurrentSize(), entry.getTotalSize()));
            if (status == DownloadEntry.Status.COMPLETED) {
                holder.btnDownload.setText("已完成");
            }
            switch (status) {
                case DOWNLOADING:
                case WAIT:
                    holder.btnDownload.setText("暂停");
                    break;
                case PAUSED:
                    holder.btnDownload.setText("继续");
                    break;
                case COMPLETED:
                    holder.btnDownload.setVisibility(View.GONE);
                    break;
                case IDLE:
                case CANCELED:
                default:
                    holder.btnDownload.setText("下载");
                    break;
            }
            holder.btnDownload.setOnClickListener(v -> {
                switch (status) {
                    case DOWNLOADING:
                    case WAIT:
                        DownloadManager.getInstance(MainActivity.this).pauseDownload(entry);
                        break;
                    case PAUSED:
                        DownloadManager.getInstance(MainActivity.this).resumeDownload(entry);
                        break;
                    case IDLE:
                    default:
                        DownloadManager.getInstance(MainActivity.this).startDownload(entry);
                        break;
                }
            });
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView txvFileName;
            public ImageView imgAppIcon;
            public Button btnDownload;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                txvFileName = itemView.findViewById(R.id.txv_file_name);
                imgAppIcon = itemView.findViewById(R.id.imv_icon);
                btnDownload = itemView.findViewById(R.id.btn_download);
            }
        }
    }


}