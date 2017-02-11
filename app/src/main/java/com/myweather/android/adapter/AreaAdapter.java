package com.myweather.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myweather.android.R;

import java.util.List;

/**
 * Created by Administrator on 2017/2/12.
 */

public class AreaAdapter extends RecyclerView.Adapter<AreaAdapter.ItemHolder> {

    private Context context;
    private List<String> dataList;
    private OnItemClickListener onItemClickListener;

    public AreaAdapter(Context context, List<String> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.area_item, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        String areaname = dataList.get(position);
        holder.setData(areaname, position);
    }

    @Override
    public int getItemCount() {
        return null != dataList ? dataList.size() : 0;
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        public View areaView;
        private TextView areaText;
        private int mPostion;

        public ItemHolder(View itemView) {
            super(itemView);
            areaView = itemView;
            areaText = (TextView) itemView.findViewById(R.id.area_name);
            initEvent(itemView);
        }

        private void initEvent(View itemView) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(mContext, "被點擊位置==" + mPostion, Toast.LENGTH_SHORT).show();
                    onItemClickListener.onClick(mPostion, v);
                }
            });

        }

        public void setData(String name, int position) {
            areaText.setText(name);
            this.mPostion = position;
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    //创建一个接口
    public interface OnItemClickListener {
        void onClick(int position,View v);
    }
}
