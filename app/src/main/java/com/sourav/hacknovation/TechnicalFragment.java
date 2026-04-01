package com.sourav.hacknovation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class TechnicalFragment extends Fragment {

    private CircleImageView souravImg, supriyoImg, subrataImg;
    private DatabaseReference membersRef;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_technical, container, false);

        souravImg = view.findViewById(R.id.souravimgProfile);
        supriyoImg = view.findViewById(R.id.supriyoimgProfile);

        setupActions(
                view.findViewById(R.id.btnCall),
                view.findViewById(R.id.btnMail),
                view.findViewById(R.id.btnLinkedin),
                "9556775778",
                "souravpati452@gmail.com",
                "https://www.linkedin.com/in/sourav-kumar-pati-aa0833297/?originalSubdomain=in"
        );

        setupActions(
                view.findViewById(R.id.btnCall1),
                view.findViewById(R.id.btnMail1),
                view.findViewById(R.id.btnLinkedin1),
                "8116814546",
                "dhibarsubrata18@gmail.com",
                "https://www.linkedin.com/in/subrata-dhibar/"

        );

        setupActions(
                view.findViewById(R.id.btnCall2),
                view.findViewById(R.id.btnMail2),
                view.findViewById(R.id.btnLinkedin2),

                "7992365863",
                "supriyodawn50@gmail.com",
                "https://www.linkedin.com/in/supriyo-dawn"
        );

        membersRef = FirebaseDatabase.getInstance()
                .getReference("members");

        loadMemberImages();

        return view;
    }

    private void loadMemberImages() {

        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    String souravUrl =
                            snapshot.child("sourav").child("image").getValue(String.class);
                    String supriyoUrl =
                            snapshot.child("supriyo").child("image").getValue(String.class);


                    if (souravUrl != null)
                        Glide.with(requireContext())
                                .load(souravUrl)
                                .placeholder(R.drawable.ic_profile)
                                .into(souravImg);

                    if (supriyoUrl != null)
                        Glide.with(requireContext())
                                .load(supriyoUrl)
                                .placeholder(R.drawable.ic_profile)
                                .into(supriyoImg);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void setupActions(ImageView callBtn,
                              ImageView mailBtn,
                              ImageView linkedinBtn,
                              String phone,
                              String email,
                              String linkedinUrl) {


        callBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        });
        mailBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + email));
            startActivity(intent);
        });

        linkedinBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(linkedinUrl));
            startActivity(intent);
        });
    }

}
