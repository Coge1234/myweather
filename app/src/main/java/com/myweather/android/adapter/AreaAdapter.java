package com.myweather.android.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.myweather.android.R;
import com.myweather.android.db.AddCounty;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by Administrator on 2017/2/12.
 */

public class AreaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<String> dataList;
    private OnItemClickListener onItemClickListener;
    private boolean isvisiable = false;
    private List<AddCounty> addCounties;

    public boolean isIsvisiable() {
        return isvisiable;
    }

    public void setIsvisiable(boolean isvisiable) {
        this.isvisiable = isvisiable;
//        notifyDataSetChanged();
    }

    public AreaAdapter(Context context, List<String> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View view = LayoutInflater.from(context).inflate(R.layout.area_item, parent, false);
            return new AddCommand(view);
        } else if (viewType == 2) {
            View view = LayoutInflater.from(context).inflate(R.layout.area_item, parent, false);
            return new AddCountyList(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.area_item, parent, false);
            return new ItemHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AddCommand) {
            ((AddCommand) holder).addCommendTextView.setTextColor(Color.parseColor("#E74940"));
            ((AddCommand) holder).addCommendTextView.setText(dataList.get(position));
            ((AddCommand) holder).deleteBtn.setVisibility(View.GONE);
            ((AddCommand) holder).getItemposition(position);
        } else if (holder instanceof AddCountyList) {
            ((AddCountyList) holder).deleteBtn.setVisibility(View.VISIBLE);
            ((AddCountyList) holder).countyListNameTextView.setText(dataList.get(position));
            ((AddCountyList) holder).getItemposition(position);
            ((AddCountyList) holder).deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DataSupport.delete(AddCounty.class, position);
                    dataList.clear();
                    addCounties =DataSupport.findAll(AddCounty.class);
                    dataList.add("添加城市");
                    if (null != addCounties && addCounties.size() > 0) {
                        for (AddCounty addCounty : addCounties) {
                            dataList.add(addCounty.getCountyName());
                        }
                        notifyDataSetChanged();
                    }
                }
            });
        } else if (holder instanceof ItemHolder) {
            ((ItemHolder) holder).setData(dataList.get(position), position);
        }

    }

    @Override
    public int getItemCount() {
        return null != dataList ? dataList.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (isIsvisiable()) {
            return position == 0 ? 1 : 2;
        } else
            return super.getItemViewType(position);
    }

    public class AddCommand extends RecyclerView.ViewHolder {
        private TextView addCommendTextView;
        private Button deleteBtn;
        private int mPosition;

        public void getItemposition(int position) {
            this.mPosition = position;
        }

        public AddCommand(View itemView) {
            super(itemView);
            addCommendTextView = (TextView) itemView.findViewById(R.id.area_name);
            deleteBtn = (Button) itemView.findViewById(R.id.delete_btn);
            initEvent(itemView);
        }

        private void initEvent(View itemView) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(mPosition, v);
                }
            });
        }
    }

    public class AddCountyList extends RecyclerView.ViewHolder {
        private TextView countyListNameTextView;
        private Button deleteBtn;
        private int mPosition;

        public void getItemposition(int position) {
            this.mPosition = position;
        }

        public AddCountyList(View itemView) {
            super(itemView);
            countyListNameTextView = (TextView) itemView.findViewById(R.id.area_name);
            deleteBtn = (Button) itemView.findViewById(R.id.delete_btn);
        }
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
        void onClick(int position, View v);
    }
}
