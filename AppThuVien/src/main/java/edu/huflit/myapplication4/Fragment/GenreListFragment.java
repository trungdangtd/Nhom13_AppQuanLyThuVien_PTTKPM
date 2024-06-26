package edu.huflit.myapplication4.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import edu.huflit.myapplication4.Adapter.GenreAdapter;
import edu.huflit.myapplication4.BookstoreProjectDatabase;
import edu.huflit.myapplication4.MainActivity;
import edu.huflit.myapplication4.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GenreListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenreListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GenreListFragment() {
        BookstoreProjectDatabase.LoadGenre();
        MainActivity.instance.menuBNV.setVisibility(View.GONE);
        MainActivity.instance.menuBNV.setEnabled(false);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GenreListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GenreListFragment newInstance(String param1, String param2) {
        GenreListFragment fragment = new GenreListFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_genre_list, container, false);
    }
    RecyclerView genreListRV;
    ImageView backBtn;

    Button btnadd;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GetIDPalletes(view);
        SetPalletes();
        LoadBookList();
    }

    void GetIDPalletes(View view)
    {
        genreListRV = view.findViewById(R.id.GenreList);
        backBtn = view.findViewById(R.id.backBtn);
        btnadd = view.findViewById(R.id.btnAddGenre);
        btnadd.setVisibility(View.INVISIBLE);
        btnadd.setEnabled(false);
        if(MainActivity.instance.isLogin) {
            if (BookstoreProjectDatabase.accountInfo.getRole().equals("Sinh viên")) {
                btnadd.setVisibility(View.INVISIBLE);
                btnadd.setEnabled(false);
            } else if (BookstoreProjectDatabase.accountInfo.getRole().equals("Quản lý") || BookstoreProjectDatabase.accountInfo.getRole().equals("Thủ thư")) {
                btnadd.setVisibility(View.VISIBLE);
                btnadd.setEnabled(true);
                btnadd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity.instance.currentFragment = new AddGenreFragment();
                        MainActivity.instance.ReplaceFragment(-1);
                    }
                });
            }
        }

    }

    void SetPalletes()
    {
        backBtn.setOnClickListener(v -> BackToPage());
    }

    void LoadBookList()
    {
        genreListRV.setLayoutManager(new LinearLayoutManager(MainActivity.instance, RecyclerView.VERTICAL, false));
        genreListRV.setAdapter(new GenreAdapter(getActivity().getApplicationContext(), BookstoreProjectDatabase.genres));
    }


    void BackToPage()
    {
        getFragmentManager().popBackStack();
    }
}