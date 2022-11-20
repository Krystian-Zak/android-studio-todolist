package pl.krystianzak.to_do_list.ui;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import pl.krystianzak.to_do_list.R;

public class ViewHolder extends RecyclerView.ViewHolder {
    //Initialize variable
    TextView name;
    TextView endDateTime;
    LinearLayout blockPriority;
    ImageView imageViewJobEdit, imageViewJobDelete;
    CheckBox checkBoxJobDone;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        //Assign variable
        name = itemView.findViewById(R.id.textViewName);
        endDateTime = itemView.findViewById(R.id.textViewEndDateTime);
        blockPriority = itemView.findViewById(R.id.blockPriority);
        imageViewJobEdit = itemView.findViewById(R.id.imageViewJobEdit);
        imageViewJobDelete = itemView.findViewById(R.id.imageViewJobDelete);
        checkBoxJobDone = itemView.findViewById(R.id.checkBoxJobDone);
    }
}
