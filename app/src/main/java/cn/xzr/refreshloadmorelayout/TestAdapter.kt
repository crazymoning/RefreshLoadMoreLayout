package cn.xzr.refreshloadmorelayout

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

/**
 * Created by xzr on 2017/12/1.
 */

class TestAdapter : RecyclerView.Adapter<TestAdapter.TestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        return TestViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_test, parent, false))
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        holder.test.text = position.toString()
        holder.test.setOnClickListener { v -> Toast.makeText(v.context, "" + position, Toast.LENGTH_SHORT).show() }
    }

    override fun getItemCount(): Int {
        return 30
    }

    class TestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var test: TextView = itemView.findViewById(R.id.position)

    }
}
