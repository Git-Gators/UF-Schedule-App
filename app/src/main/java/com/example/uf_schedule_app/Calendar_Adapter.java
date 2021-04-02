package com.example.uf_schedule_app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class Calendar_Adapter extends RecyclerView.Adapter<Calendar_Adapter.CalendarViewHolder>{

    String data1[], data2[];
    int colorVal[];
    Context context;

    public Calendar_Adapter(Context ct, String s1[], String s2[], int colors[])
    {
        context = ct;
        data1 = s1;
        data2 = s2;
        colorVal = colors;
        //images = img;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.calendar_row, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        holder.calendarText1.setText(data1[position]);
        holder.calendarText2.setText(data2[position]);
        holder.constraintLayout.setBackground(context.getResources().getDrawable(colorVal[position]));
        //holder.calendarText2.setTextColor(context.getResources().getColor(R.color.white));
        //holder.calendarImage.setImageResource(images[position]);

    }

    @Override
    public int getItemCount() {
        return data1.length;
    }

    public class CalendarViewHolder extends RecyclerView.ViewHolder{

        public View row_linearlayout;
        public View constraintLayout;
        TextView calendarText1, calendarText2;
        //ImageView calendarImage;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            calendarText1 = itemView.findViewById(R.id.period);
            calendarText2 = itemView.findViewById(R.id.course_field);
            row_linearlayout = (LinearLayout)itemView.findViewById(R.id.row_linearLayout);
            constraintLayout = (ConstraintLayout)itemView.findViewById(R.id.constraintLayout2);
            //calendarImage = itemView.findViewById(R.id.calendar_image_view);
        }
    }
}
