package com.sourav.hacknovation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.checkbox.MaterialCheckBox;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.VH> {

    private List<AttendanceModel> list;
    private List<AttendanceModel> fullList;
    private boolean isAdmin;

    public AttendanceAdapter(List<AttendanceModel> list, boolean isAdmin) {
        this.list = new ArrayList<>(list);
        this.fullList = new ArrayList<>(list);

        this.isAdmin = isAdmin;
    }
    public List<AttendanceModel> getCurrentList() {
        return list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AttendanceModel m = list.get(position);

        h.name.setText(m.name);
        h.roll.setText(m.roll);

        if (m.profileImage != null && !m.profileImage.isEmpty()) {
            Glide.with(h.img.getContext())
                    .load(m.profileImage)
                    .placeholder(R.drawable.hackimg3bg)
                    .error(R.drawable.hackimg3bg)
                    .circleCrop()
                    .into(h.img);
        } else {
            h.img.setImageResource(R.drawable.hackimg3bg);
        }

        h.present.setOnCheckedChangeListener(null);
        h.present.setChecked(m.present);

        h.present.setEnabled(isAdmin);

        h.present.setOnCheckedChangeListener((buttonView, isChecked) -> {
            m.present = isChecked;
            if (listener != null) {
                listener.onChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, roll;
        MaterialCheckBox present;
        ImageView img;

        VH(View v) {
            super(v);
            name = v.findViewById(R.id.txtName);
            roll = v.findViewById(R.id.txtRoll);
            present = v.findViewById(R.id.cbPresent);
            img = v.findViewById(R.id.profileImg);
        }
    }

    public void filter(String query) {
        list.clear();

        if (query.isEmpty()) {
            list.addAll(fullList);
        } else {
            String q = query.toLowerCase();
            for (AttendanceModel m : fullList) {
                if ((m.name != null && m.name.toLowerCase().contains(q)) ||
                        (m.roll != null && m.roll.toLowerCase().contains(q))) {
                    list.add(m);
                }
            }
        }
        notifyDataSetChanged();
    }


    public void updateList(List<AttendanceModel> newData) {
        fullList.clear();
        fullList.addAll(newData);

        list.clear();
        list.addAll(newData);

        notifyDataSetChanged();
    }


    public interface OnAttendanceChangeListener {
        void onChanged();
    }

    private OnAttendanceChangeListener listener;

    public void setOnAttendanceChangeListener(OnAttendanceChangeListener listener) {
        this.listener = listener;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
        notifyDataSetChanged();
    }

}
