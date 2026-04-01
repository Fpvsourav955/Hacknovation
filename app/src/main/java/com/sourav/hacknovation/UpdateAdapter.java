package com.sourav.hacknovation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateAdapter extends RecyclerView.Adapter<UpdateAdapter.VH> {

    List<UpdateModel> list;

    public UpdateAdapter(List<UpdateModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_update, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        UpdateModel m = list.get(position);

        h.sender.setText(m.sender);
        h.message.setText(m.message);
        h.time.setText(android.text.format.DateFormat.format(
                "dd MMM hh:mm a", m.timestamp));

        if (m.photoUrl != null && !m.photoUrl.isEmpty()) {
            Glide.with(h.profile.getContext())
                    .load(m.photoUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(h.profile);
        } else {
            h.profile.setImageResource(R.drawable.ic_profile);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView sender, message, time;

        CircleImageView profile;
        VH(View v) {
            super(v);
            sender = v.findViewById(R.id.txtSender);
            message = v.findViewById(R.id.txtMessage);
            time = v.findViewById(R.id.txtTime);
            profile = v.findViewById(R.id.imgProfile);
        }
    }
}
