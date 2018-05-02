package com.snail.labaffinity.adapter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.snail.labaffinity.R;

/**
 * Author: lishang
 * Data: 17/1/5 下午2:10
 * Des:
 * version:
 */

public class MyFragment extends Fragment {


    public static Fragment newInstance(String msg) {
        Fragment fragment = new MyFragment();
        Bundle bundle = new Bundle();
        bundle.putString("msg", msg);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_main, container, false);
        TextView textView = (TextView) root.findViewById(R.id.first);
        textView.setText("Pos " + getArguments().getString("msg"));
        return root;
    }
}
