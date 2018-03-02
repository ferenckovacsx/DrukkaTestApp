package com.example.drukkatestapp;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.drukkatestapp.pojo.FilePOJO;

import java.util.List;

/**
 * Created by ferenckovacsx on 2018-02-28.
 */

public class DocumentsListAdapter extends RecyclerView.Adapter<DocumentsListAdapter.MyViewHolder> {

    private List<FilePOJO> listOfFiles;
    private int selected_position = -1;
    private OnItemSelectedListener onItemSelectedListener;


    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView nameTextView, sizeTextView;

        MyViewHolder(View view) {
            super(view);
            itemView.setOnClickListener(this);
            nameTextView = view.findViewById(R.id.document_name);
            sizeTextView = view.findViewById(R.id.document_size);
        }

        @Override
        public void onClick(View v) {

            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

            notifyItemChanged(selected_position);
            selected_position = getAdapterPosition();
            notifyItemChanged(selected_position);

            onItemSelectedListener.onItemSelected(getAdapterPosition(), listOfFiles.get(getAdapterPosition()).getUuid());
        }
    }


    public DocumentsListAdapter(List<FilePOJO> listOfFiles) {
        this.listOfFiles = listOfFiles;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        FilePOJO document = listOfFiles.get(position);
        holder.nameTextView.setText(document.getFilename());
        holder.sizeTextView.setText(Utilities.formatFileSize(document.getSize()));
        holder.itemView.setBackgroundColor(selected_position == position ? Color.parseColor("#20000000") : Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return listOfFiles.size();
    }

    //listener for selected item
    public interface OnItemSelectedListener {
        void onItemSelected(int position, String uuid);

    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }
}
