package com.example.wordlistnews.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordlistnews.model.BookmarkDataModel;
import com.example.wordlistnews.dbhelper.BookmarksDBHelper;
import com.example.wordlistnews.R;

import java.util.ArrayList;
import java.util.List;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder> {

    List<BookmarkDataModel> bookmarkDataModels = new ArrayList<BookmarkDataModel>();
    BookmarksDBHelper bookmarksDBHelper;
    Context mContext;
    private OnBookmarkClickListener mOnBookmarkClickListener;
    public BookmarksAdapter (List<BookmarkDataModel> bookmarkDataModels, OnBookmarkClickListener mOnBookmarkClickListener) {
        this.bookmarkDataModels = bookmarkDataModels;
        this.mOnBookmarkClickListener = mOnBookmarkClickListener;
    }

    @NonNull
    @Override
    public BookmarksAdapter.BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.bookmark_model, parent, false);
        return new BookmarksAdapter.BookmarkViewHolder(view, mOnBookmarkClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarksAdapter.BookmarkViewHolder holder, int position) {

        bookmarksDBHelper = new BookmarksDBHelper(mContext);

        BookmarkDataModel bookmarkDataModel = bookmarkDataModels.get(position);

        String title = bookmarkDataModel.getTitle();
        String url = bookmarkDataModel.getUrl();

        holder.title.setText(title);
        holder.url.setText(url);

        holder.deleteBookmarkBtn.setOnClickListener(v -> {

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
            alertBuilder.setTitle("Do you want to remove this url ?");
            alertBuilder.setMessage(title + "\n" + url);
            alertBuilder.setCancelable(true);

            alertBuilder.setPositiveButton(
                    "YES",
                    (dialog, id) -> {
                        boolean deleted = bookmarksDBHelper.deleteBookmark(url);
                        if (deleted) {
                            Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show();

                            bookmarkDataModels.remove(position);
                            notifyItemRangeChanged(position, bookmarkDataModels.size());
                            notifyDataSetChanged();

                        }
                    });

            alertBuilder.setNegativeButton("NO", (dialog, id) -> dialog.cancel());

            AlertDialog deleteAlert = alertBuilder.create();
            deleteAlert.show();
        });

    }

    @Override
    public int getItemCount() {
        return bookmarkDataModels.size();
    }

    public class BookmarkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView url;
        Button deleteBookmarkBtn;
        OnBookmarkClickListener onBookmarkClickListener;
        public BookmarkViewHolder(View itemView, OnBookmarkClickListener onBookmarkClickListener) {
            super(itemView);

            title = itemView.findViewById(R.id.bookmark_title);
            url = itemView.findViewById(R.id.bookmark_url);
            deleteBookmarkBtn = itemView.findViewById(R.id.delete_bookmark_btn);
            this.onBookmarkClickListener = onBookmarkClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onBookmarkClickListener.onBookmarkClick(getAdapterPosition());
        }
    }

    public interface OnBookmarkClickListener{
        void onBookmarkClick(int position);
    }
}
