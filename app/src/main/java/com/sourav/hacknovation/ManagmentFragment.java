package com.sourav.hacknovation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ManagmentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManagmentFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ManagmentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ManagmentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ManagmentFragment newInstance(String param1, String param2) {
        ManagmentFragment fragment = new ManagmentFragment();
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
        View view = inflater.inflate(R.layout.fragment_managment, container, false);


        setupActions(
                view.findViewById(R.id.btnCall),
                view.findViewById(R.id.btnMail),
                view.findViewById(R.id.btnLinkedin),
                "8249594589",
                "sriyapanigrahi361@gmail.com",
                "https://www.linkedin.com/in/sriya-panigrahi-767602296?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app"
        );

        setupActions(
                view.findViewById(R.id.btnCall1),
                view.findViewById(R.id.btnMail1),
                view.findViewById(R.id.btnLinkedin1),
                "8917563521",
                "rajababu777sahu@gmail.com",
                "https://www.linkedin.com/in/raja-babu-sahu-b3986b349?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app"

        );

        setupActions(
                view.findViewById(R.id.btnCall2),
                view.findViewById(R.id.btnMail2),
                view.findViewById(R.id.btnLinkedin2),

                "7684881661",
                "ayushbiswal2004@gmail.com",
                "https://www.linkedin.com/in/ayushkumarbiswal"
        );
        setupActions(
                view.findViewById(R.id.btnCall3),
                view.findViewById(R.id.btnMail3),
                view.findViewById(R.id.btnLinkedin3),

                "7903847241",
                "ankitkumarvv0702@gmail.com",
                "https://www.linkedin.com/in/ankit-kumar-1948532a1?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app"
        );
        setupActions(
                view.findViewById(R.id.btnCall4),
                view.findViewById(R.id.btnMail4),
                view.findViewById(R.id.btnLinkedin4),

                "9938317108",
                "ankitbiswal144@gmail.com",
                "https://www.linkedin.com/in/ankit-biswal-8b9656278?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app"
        );
        setupActions(
                view.findViewById(R.id.btnCall5),
                view.findViewById(R.id.btnMail5),
                view.findViewById(R.id.btnLinkedin5),

                "6370390842",
                "krishnamangaraj05@gmail.com",
                "https://www.linkedin.com/in/krishna-mangaraj-408aa0327?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app"
        );
        setupActions(
                view.findViewById(R.id.btnCall6),
                view.findViewById(R.id.btnMail6),
                view.findViewById(R.id.btnLinkedin6),

                "6299620074",
                "bhaskarpandey8908@gmail.com",
                ""
        );
        setupActions(
                view.findViewById(R.id.btnCall7),
                view.findViewById(R.id.btnMail7),
                view.findViewById(R.id.btnLinkedin7),

                "9090158121",
                "basundhara.jadav@gmail.com",
                "https://www.linkedin.com/in/basundhara-jadav-1957b9304?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app"
        );
        setupActions(
                view.findViewById(R.id.btnCall8),
                view.findViewById(R.id.btnMail8),
                view.findViewById(R.id.btnLinkedin8),

                "7848924713",
                "sindhujagudla155@gmail.com",
                "https://www.linkedin.com/in/sindhuja-gudla-274892341?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app"
        );


        return  view;
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