package pl.krystianzak.to_do_list.ui.done;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pl.krystianzak.to_do_list.JobData;
import pl.krystianzak.to_do_list.MainActivity;
import pl.krystianzak.to_do_list.R;
import pl.krystianzak.to_do_list.databinding.FragmentDoneBinding;
import pl.krystianzak.to_do_list.databinding.FragmentDoneBinding;
import pl.krystianzak.to_do_list.ui.MainAdapter;
import pl.krystianzak.to_do_list.ui.RoomDB;
import pl.krystianzak.to_do_list.ui.done.DoneFragment;


public class DoneFragment extends Fragment {

    private FragmentDoneBinding binding;
    MainActivity main;
    Context context = null;
    RecyclerView recyclerView;

    LinearLayoutManager linearLayoutManager;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            context = getActivity();
            main = (MainActivity) getActivity();
        } catch (Exception e) {
            throw new IllegalStateException("MainActivity must implement callbacks");
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDoneBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = (RecyclerView) root.findViewById(R.id.RecyclerViewDoneJobs);
        main.setTypeOfJobList(1);
        main.updateJobList();
        //jobsList = main.getJobsList();
        //Initialize linear layot manager
        linearLayoutManager = new LinearLayoutManager(context);
        //Set layout manager
        recyclerView.setLayoutManager(linearLayoutManager);
        //Initialize adapter
        main.setMainAdapter(new MainAdapter(main, main.getJobsList()));
        //Set adapter
        recyclerView.setAdapter(main.getMainAdapter());

        swipeRefreshLayout = root.findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            main.refreshListAndAdapter();
            Toast.makeText(context, getResources().getString(R.string.list_refreshed), Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        });

        return root;
    }

    public static DoneFragment newInstance(String strArg) {
        DoneFragment fragment = new DoneFragment();
        Bundle args = new Bundle();
        args.putString("strArg1", strArg);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}