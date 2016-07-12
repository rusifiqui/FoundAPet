package com.jvilam.foundapet.listTools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jvilam.foundapet.R;

import java.util.ArrayList;

/**
 * Created by jvilam on 23/06/2016.
 *
 */
public class CommentAdapter extends ArrayAdapter<CommentItem> {

    private final Context context;
    private final ArrayList<CommentItem> commentArrayList;

    public CommentAdapter(Context context, ArrayList<CommentItem> commentArrayList){
        super(context, R.layout.list_layout, commentArrayList);
        this.context = context;
        this.commentArrayList = commentArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.list_layout, parent, false);

        // 3. Get the two text view from the rowView
        TextView userData = (TextView) rowView.findViewById(R.id.userData);
        TextView comment = (TextView) rowView.findViewById(R.id.comment);

        // 4. Set the text for textView
        userData.setText(commentArrayList.get(position).getTitle());
        comment.setText(commentArrayList.get(position).getComment());

        // 5. retrn rowView
        return rowView;
    }
}