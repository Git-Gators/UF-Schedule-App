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

public class Week_Adapter extends RecyclerView.Adapter<Week_Adapter.CalendarViewHolder>{

    String data1[];
    String[][] data2;
    int colorVal[][];
    Context context;

    public Week_Adapter(Context ct, String s1[], String s2[][], int colors[][])
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
        View view = inflater.inflate(R.layout.row_week, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        holder.calendarText1.setText(data1[position]);
        holder.calendarText2.setText(data2[0][position]);
        holder.calendarText3.setText(data2[1][position]);
        holder.calendarText4.setText(data2[2][position]);
        holder.calendarText5.setText(data2[3][position]);
        holder.calendarText6.setText(data2[4][position]);
        holder.calendarText7.setText(data2[5][position]);
        holder.constraintLayoutM.setBackground(context.getResources().getDrawable(colorVal[0][position]));
        holder.constraintLayoutT.setBackground(context.getResources().getDrawable(colorVal[1][position]));
        holder.constraintLayoutW.setBackground(context.getResources().getDrawable(colorVal[2][position]));
        holder.constraintLayoutR.setBackground(context.getResources().getDrawable(colorVal[3][position]));
        holder.constraintLayoutF.setBackground(context.getResources().getDrawable(colorVal[4][position]));
        holder.constraintLayoutS.setBackground(context.getResources().getDrawable(colorVal[5][position]));
        //holder.calendarText2.setTextColor(context.getResources().getColor(R.color.white));
        //holder.calendarImage.setImageResource(images[position]);

    }

    @Override
    public int getItemCount() {
        return data1.length;
    }

    public class CalendarViewHolder extends RecyclerView.ViewHolder{

        public View row_linearlayout;
        public View constraintLayoutM, constraintLayoutT, constraintLayoutW, constraintLayoutR, constraintLayoutF, constraintLayoutS;
        TextView calendarText1, calendarText2, calendarText3, calendarText4, calendarText5, calendarText6, calendarText7;
        //ImageView calendarImage;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            calendarText1 = itemView.findViewById(R.id.period);
            calendarText2 = itemView.findViewById(R.id.monday);
            calendarText3 = itemView.findViewById(R.id.tuesday);
            calendarText4 = itemView.findViewById(R.id.wednesday);
            calendarText5 = itemView.findViewById(R.id.thursday);
            calendarText6 = itemView.findViewById(R.id.friday);
            calendarText7 = itemView.findViewById(R.id.saturday);
            row_linearlayout = (LinearLayout)itemView.findViewById(R.id.row_linearLayout);
            constraintLayoutM = (ConstraintLayout)itemView.findViewById(R.id.constraintLayout1);
            constraintLayoutT = (ConstraintLayout)itemView.findViewById(R.id.constraintLayout3);
            constraintLayoutW = (ConstraintLayout)itemView.findViewById(R.id.constraintLayout2);
            constraintLayoutR = (ConstraintLayout)itemView.findViewById(R.id.constraintLayout4);
            constraintLayoutF = (ConstraintLayout)itemView.findViewById(R.id.constraintLayout5);
            constraintLayoutS = (ConstraintLayout)itemView.findViewById(R.id.constraintLayout6);
            //calendarImage = itemView.findViewById(R.id.calendar_image_view);
        }
    }
}
