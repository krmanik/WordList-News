package com.example.wordlistnews.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordlistnews.R;
import com.example.wordlistnews.dbhelper.WordDBHelper;
import com.example.wordlistnews.model.WordDataModel;

import java.util.List;

import static com.example.wordlistnews.MainActivity.getScreenWidth;

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.WordListViewHolder> {

    List<WordDataModel> wordDataModelList;
    WordDBHelper wordDbHelper;
    Context mContext;

    public WordListAdapter (List<WordDataModel> wordDataModelList) {
        this.wordDataModelList = wordDataModelList;
    }

    @NonNull
    @Override
    public WordListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.word_model, parent, false);
        return new WordListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordListViewHolder holder, int position) {

        wordDbHelper = new WordDBHelper(mContext);

        WordDataModel wordDataModel = wordDataModelList.get(position);

        String word = wordDataModel.getWord();
        String meanings = wordDataModel.getMeanings();

        holder.word.setText(word);
        holder.meanings.setText(meanings);

        Dialog editWordDialog = new Dialog(mContext);
        editWordDialog.setContentView(R.layout.edit_word_dialog);
        EditText editWordText = editWordDialog.findViewById(R.id.edit_word_text);
        EditText editMeaningsText = editWordDialog.findViewById(R.id.edit_meanings_text);
        Button cancelEditWord = editWordDialog.findViewById(R.id.cancel_edit_word);
        Button updateEditWord = editWordDialog.findViewById(R.id.confirm_edit_word);

        cancelEditWord.setOnClickListener(v -> {
            editWordDialog.dismiss();
        });

        updateEditWord.setOnClickListener(v -> {
            String word1 = editWordText.getText().toString();
            String meanings1 = editMeaningsText.getText().toString();
            boolean updated = wordDbHelper.updateWord(word1, meanings1);
            if (updated) {
                Toast.makeText(mContext, "Updated", Toast.LENGTH_SHORT).show();
                editWordDialog.dismiss();

                WordDataModel model = new WordDataModel(word1, meanings1);

                wordDataModelList.set(position, model);
                notifyItemChanged(position);
                notifyDataSetChanged();

            } else {
                Toast.makeText(mContext, "Error occurred, Try again", Toast.LENGTH_SHORT).show();
            }
        });

        holder.editWordBtn.setOnClickListener(v -> {
            editWordText.setText(word);
            editMeaningsText.setText(meanings);

            Window window = editWordDialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.width = getScreenWidth();
            window.setAttributes(wlp);

            editWordDialog.show();
        });

        holder.deleteWordBtn.setOnClickListener(v -> {

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
            alertBuilder.setTitle("Do you want to remove this word ?");
            alertBuilder.setMessage(word + "\n" + meanings);
            alertBuilder.setCancelable(true);

            alertBuilder.setPositiveButton(
                    "YES",
                    (dialog, id) -> {
                        boolean deleted = wordDbHelper.deleteWord(word);
                        if (deleted) {
                            Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show();

                            wordDataModelList.remove(position);
                            notifyItemRangeChanged(position, wordDataModelList.size());
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
        return wordDataModelList.size();
    }

    public class WordListViewHolder extends RecyclerView.ViewHolder {
        TextView word;
        TextView meanings;
        Button editWordBtn, deleteWordBtn;
        public WordListViewHolder(View itemView) {
            super(itemView);

            word = itemView.findViewById(R.id.word);
            meanings = itemView.findViewById(R.id.meanings);

            editWordBtn = itemView.findViewById(R.id.edit_word_meanings_btn);
            deleteWordBtn = itemView.findViewById(R.id.delete_word_btn);

        }
    }
}
