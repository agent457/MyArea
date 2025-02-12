package com.example.myarea;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditorFragment extends Fragment {

    private EditText editName, editDes, editLon, editLat, searchID;
    private Button addPOI, current;
    private ImageButton deleteID;
    private TextView title;
    private ListView listView;
    private DBHandler db;
    private ArrayList<POI> POIs;
    PoiAdapter adapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EditorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditorFragment newInstance(String param1, String param2) {
        EditorFragment fragment = new EditorFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_editor, container, false);
        init(view);

        addPOI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editName.getText() == null || editDes.getText() == null || editLon.getText() == null || editLat.getText() == null) {
                    Toast.makeText(requireContext(), "Please enter all the data", Toast.LENGTH_SHORT).show();
                    return;
                }
                db.addPOI(String.valueOf(editName.getText()), String.valueOf(editDes.getText()), Double.parseDouble(String.valueOf(editLon.getText())), Double.parseDouble(String.valueOf(editLat.getText())));
                POIs.add(db.getLast());
                adapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Added POI", Toast.LENGTH_SHORT).show();
            }
        });
        deleteID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = Integer.parseInt(String.valueOf(searchID.getText()));
                db.deletePOI(id);
                POIs = removeByID(id, POIs);
                adapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Deleted POI", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
    public void init(View view) {
        editName = view.findViewById(R.id.editName);
        editDes = view.findViewById(R.id.editDescription);
        editLon = view.findViewById(R.id.editLon);
        editLat = view.findViewById(R.id.editLat);
        searchID = view.findViewById(R.id.list);
        addPOI = view.findViewById(R.id.addPOI);
        current = view.findViewById(R.id.current);
        deleteID = view.findViewById(R.id.deleteID);
        title = view.findViewById(R.id.title);
        listView = view.findViewById(R.id.scroll);

        String text = getString(R.string.name,"");
        editName.setHint(text);
        text = getString(R.string.id,"");
        searchID.setHint(text);
        text = getString(R.string.longitude,"");
        editLon.setHint(text);
        text = getString(R.string.latitude,"");
        editLat.setHint(text);
        text = getString(R.string.description,"");
        editDes.setHint(text);

        db = new DBHandler(requireContext(), "Yoana");
        POIs = db.loadDB();
        adapter = new PoiAdapter(requireContext(), POIs);
        listView.setAdapter(adapter);
    }
    public ArrayList<POI> removeByID(int id, ArrayList<POI> list){
        int i = 0;
        try {
            while(list.get(i)!=null){
                if(list.get(i).getId()==id){
                    list.remove(i);
                }
                i++;
            }
        } catch (Exception e) {

        }
        return list;
    }
}