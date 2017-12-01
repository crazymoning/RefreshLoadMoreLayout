package cn.xzr.refreshloadmorelayout;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by xzr on 2017/12/1.
 */

public class TestAdatper extends RecyclerView.Adapter<TestAdatper.TestViewHolder> {

    @Override
    public TestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = new TextView(parent.getContext());
        v.setPadding(100,10,100,10);
        v.setBackgroundColor(Color.RED);
        return new TestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TestViewHolder holder, final int position) {
        holder.test.setText(position+"");
        holder.test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(),""+position,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return 30;
    }

    static class TestViewHolder extends RecyclerView.ViewHolder{

        TextView test;

        public TestViewHolder(View itemView) {
            super(itemView);
            test = (TextView) itemView;
        }
    }
}
