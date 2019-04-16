package com.bill.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bill.R;
import com.bill.bean.CategoryResBean;
import com.bill.util.GlobalUtil;

import java.util.LinkedList;

public class CategoryRecyclerAdapter extends RecyclerView.Adapter<CategoryViewHolder> {

    public Context mContext;
    private LayoutInflater mInflater;
    private LinkedList<CategoryResBean> celllist = GlobalUtil.getInstance().costRes;
    private String selected = "";
    private OnCategoryClickListener onCategoryClickListener;

    public CategoryRecyclerAdapter(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);
        selected = celllist.get(0).title;
    }

    public String getSelected() {
        return selected;
    }

    public void setOnCategoryClickListener(OnCategoryClickListener onCategoryClickListener) {
        this.onCategoryClickListener = onCategoryClickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.cell_category, viewGroup, false);
        CategoryViewHolder myViewHolder = new CategoryViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder categoryViewHolder, int i) {
        final CategoryResBean res = celllist.get(i);
        categoryViewHolder.imageView.setImageResource(res.resBlack);
        categoryViewHolder.textView.setText(res.title);

        categoryViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected = res.title;
                notifyDataSetChanged();

                if (onCategoryClickListener != null) {
                    onCategoryClickListener.onClick(res.title);
                }
            }
        });

        if (categoryViewHolder.textView.getText().toString().equals(selected)) {
            categoryViewHolder.background.setBackgroundResource(R.drawable.bg_edit_text);
        } else {
            categoryViewHolder.background.setBackgroundResource(R.color.colorGrey);
        }
    }

    public void changeType(int type) {
        if (type == 1) {
            celllist = GlobalUtil.getInstance().costRes;
        } else {
            celllist = GlobalUtil.getInstance().earnRes;
        }

        selected = celllist.get(0).title;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return celllist.size();
    }

    public interface OnCategoryClickListener {
        void onClick(String category);
    }
}

class CategoryViewHolder extends RecyclerView.ViewHolder {

    RelativeLayout background;
    ImageView imageView;
    TextView textView;

    public CategoryViewHolder(@NonNull View itemView) {
        super(itemView);
        background = itemView.findViewById(R.id.cell_background);
        imageView = itemView.findViewById(R.id.imageView_category);
        textView = itemView.findViewById(R.id.textView_category);
    }
}
