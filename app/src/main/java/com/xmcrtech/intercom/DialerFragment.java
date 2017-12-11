package com.xmcrtech.intercom;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.xmcrtech.intercom.avchat.activity.AVChatActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DialerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DialerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DialerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int MY_PERMISSIONS_REQUEST = 1;

    EditText inputedit;
    //TextView tv_number;

    Button b_1, b_2, b_3, b_4, b_5, b_6, b_7, b_8, b_9, b_0, b_clear, b_call;

    String number = "";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public static DialerFragment newInstance() {
        return new DialerFragment();
    }

    public DialerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DialerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DialerFragment newInstance(String param1, String param2) {
        DialerFragment fragment = new DialerFragment();
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
        if(ContextCompat.checkSelfPermission(DialerFragment.this.getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(DialerFragment.this.getActivity(),Manifest.permission.CALL_PHONE)){
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.CALL_PHONE},MY_PERMISSIONS_REQUEST);
            }else{
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.CALL_PHONE},MY_PERMISSIONS_REQUEST);
            }
        }


    }

    /**
     * 呼出
     */
    public void outgoing(String number){
        //默认视频通话
        AVChatActivity.outgoing(DialerFragment.this.getContext(),"techxmcr", AVChatType.VIDEO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this.getContext(),Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED ){
                        Toast.makeText(DialerFragment.this.getContext(),"权限允许",Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(DialerFragment.this.getContext(),"权限拒绝",Toast.LENGTH_LONG).show();
                    DialerFragment.this.getActivity().finish();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_dialer, container, false);
        inputedit = (EditText) view.findViewById(R.id.inputedit);
        //tv_number = (TextView) view.findViewById(R.id.tv_number);
        b_1 = (Button) view.findViewById(R.id.b_1);
        b_2 = (Button) view.findViewById(R.id.b_2);
        b_3 = (Button) view.findViewById(R.id.b_3);
        b_4 = (Button) view.findViewById(R.id.b_4);
        b_5 = (Button) view.findViewById(R.id.b_5);
        b_6 = (Button) view.findViewById(R.id.b_6);
        b_7 = (Button) view.findViewById(R.id.b_7);
        b_8 = (Button) view.findViewById(R.id.b_8);
        b_9 = (Button) view.findViewById(R.id.b_9);
        b_0 = (Button) view.findViewById(R.id.b_0);
        b_clear = (Button) view.findViewById(R.id.b_clear);
        b_call = (Button) view.findViewById(R.id.b_call);

        b_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = number + "1";
                //tv_number.setText(number);
                inputedit.setText(number);
            }
        });
        b_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = number + "2";
                //tv_number.setText(number);
                inputedit.setText(number);
            }
        });
        b_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = number + "3";
                //tv_number.setText(number);
                inputedit.setText(number);
            }
        });
        b_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = number + "4";
                //tv_number.setText(number);
                inputedit.setText(number);
            }
        });
        b_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = number + "5";
                //tv_number.setText(number);
                inputedit.setText(number);
            }
        });
        b_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = number + "6";
                //tv_number.setText(number);
                inputedit.setText(number);
            }
        });
        b_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = number + "7";
                //tv_number.setText(number);
                inputedit.setText(number);
            }
        });
        b_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = number + "8";
                //tv_number.setText(number);
                inputedit.setText(number);
            }
        });
        b_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = number + "9";
                //tv_number.setText(number);
                inputedit.setText(number);
            }
        });
        b_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = number + "0";
                //tv_number.setText(number);
                inputedit.setText(number);
            }
        });
        b_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = "";
                //tv_number.setText(number);
                inputedit.setText(number);
            }
        });
        b_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tNumber = number;
                number = "";
                //tv_number.setText(number);
                inputedit.setText(number);
                outgoing(tNumber);
            }
        });

        inputedit.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


                //text  输入框中改变前的字符串信息
                //start 输入框中改变前的字符串的起始位置
                //count 输入框中改变前后的字符串改变数量一般为0
                //after 输入框中改变后的字符串与起始位置的偏移量
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //text  输入框中改变后的字符串信息
                //start 输入框中改变后的字符串的起始位置
                //before 输入框中改变前的字符串的位置 默认为0
                //count 输入框中改变后的一共输入字符串的数量
                //判断最后一位
                if(charSequence.length() > 0){
                    String c = charSequence.toString().substring(charSequence.toString().length() - 1).substring(0,1);
                    //Toast.makeText(DialerFragment.this.getContext(),c,Toast.LENGTH_SHORT).show();
                    if(c.equals("*")){
                        inputedit.setText("");
                    }
                    if(c.equals("#")){
//                        inputedit.setEnabled(false);
                        outgoing(inputedit.getText().toString());
                        inputedit.setText("");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

                //edit  输入结束呈现在输入框中的信息
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
