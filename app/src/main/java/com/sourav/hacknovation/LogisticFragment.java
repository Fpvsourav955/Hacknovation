package com.sourav.hacknovation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LogisticFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LogisticFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LogisticFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LogisticFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LogisticFragment newInstance(String param1, String param2) {
        LogisticFragment fragment = new LogisticFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logistic, container, false);

        setupActions(
                view.findViewById(R.id.btnCall),
                view.findViewById(R.id.btnMail),
                view.findViewById(R.id.btnLinkedin),
                "7683904039",
                "ambikesh22maharana@gmail.com",
                "https://www.linkedin.com/in/ambikesh-maharana-155000260?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app"
        );

        setupActions(
                view.findViewById(R.id.btnCall1),
                view.findViewById(R.id.btnMail1),
                view.findViewById(R.id.btnLinkedin1),
                "7682072212",
                "chandrakantjena32@gmail.com",
                "https://www.linkedin.com/in/chandrakantajenaa/"

        );

        setupActions(
                view.findViewById(R.id.btnCall2),
                view.findViewById(R.id.btnMail2),
                view.findViewById(R.id.btnLinkedin2),

                "8926389549",
                "bijaylaxmisamal81@gmail.com",
                "https://www.linkedin.com/in/bijaylaxmi-samal-432a0726a/"
        );
        return view;

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