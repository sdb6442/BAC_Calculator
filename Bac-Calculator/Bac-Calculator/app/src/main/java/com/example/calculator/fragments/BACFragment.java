package com.example.calculator.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.calculator.Profile;
import com.example.bac.calculator.R;
import com.example.bac.calculator.databinding.DrinkRowItemBinding;
import com.example.bac.calculator.databinding.FragmentBacBinding;
import com.example.calculator.db.AppDatabase;
import com.example.calculator.db.Drink;

import java.util.ArrayList;

public class BACFragment extends Fragment {
    public BACFragment() {
        // Required empty public constructor
    }

    private Profile mProfile;
    FragmentBacBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBacBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    ArrayList<Drink> mDrinks = new ArrayList<>();
    DrinksAdapter adapter;
    AppDatabase db;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("BAC Calculator");
        setHasOptionsMenu(true);

        mProfile = mListener.getProfile();

        binding.buttonSetProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoSetProfile();
            }
        });

        adapter = new DrinksAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(adapter);

        db= Room.databaseBuilder(getActivity(), AppDatabase.class, "drinks-db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
        getDrinksAndNotifyAdapter();
        displayBacAndSetupUI();


    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_add){
            if(mProfile == null){
                Toast.makeText(getActivity(), "Please set profile first !!", Toast.LENGTH_SHORT).show();
            } else {
                mListener.gotoAddDrink();
            }
            return true;
        } else if(item.getItemId() == R.id.action_reset){
            //Clearing the profile
            mProfile = null;
            mListener.clearProfile();

            //Deletes all the drinks
            db.drinkDAO().deleteALL();

            getDrinksAndNotifyAdapter();
            displayBacAndSetupUI();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getDrinksAndNotifyAdapter(){

        mDrinks.clear();
        mDrinks.addAll(db.drinkDAO().getAll());
        adapter.notifyDataSetChanged();
    }

    private void displayBacAndSetupUI(){
        if(mProfile == null){
            binding.textViewWeightGender.setText("N/A");
            binding.textViewStatus.setText("You're safe");
            binding.viewStatus.setBackgroundResource(R.color.safe_color);
            binding.textViewBACLevel.setText("BAC Level: 0.000");
            binding.textViewNoDrinks.setText("# Drinks: 0");
        } else {
            binding.textViewWeightGender.setText(mProfile.getWeight() + " (" + mProfile.getGender() + ")");
            binding.textViewNoDrinks.setText("# Drinks: " + mDrinks.size());
            double value_r = 0.66;
            if(mProfile.getGender().equals("Male")){
                value_r = 0.73;
            }
            double value_a = 0.0;

            for (Drink drink: mDrinks) {
                value_a = value_a + drink.getAlcohol() * drink.getSize()/100.00;
            }

            double bac = value_a * 5.14 /(mProfile.getWeight() * value_r);
            binding.textViewBACLevel.setText("BAC Level: " + String.format("%.3f", bac));

            if(bac <= 0.08){
                binding.textViewStatus.setText("You're safe");
                binding.viewStatus.setBackgroundResource(R.color.safe_color);
            } else if(bac <= 0.2){
                binding.textViewStatus.setText("Be careful. You're over the limit, so don't drive!");
                binding.viewStatus.setBackgroundResource(R.color.becareful_color);
            } else {
                binding.textViewStatus.setText("You're Cut Off!");
                binding.viewStatus.setBackgroundResource(R.color.overlimit_color);
            }
        }
    }

    class DrinksAdapter extends RecyclerView.Adapter<DrinksAdapter.DrinkViewHolder>{
        @NonNull
        @Override
        public DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            DrinkRowItemBinding binding = DrinkRowItemBinding.inflate(getLayoutInflater(), parent, false);
            return new DrinkViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull DrinkViewHolder holder, int position) {
            Drink drink = mDrinks.get(position);
            holder.setupUI(drink);
        }

        @Override
        public int getItemCount() {
            return mDrinks.size();
        }

        class DrinkViewHolder extends RecyclerView.ViewHolder{
            DrinkRowItemBinding mBinding;
            Drink mDrink;
            public DrinkViewHolder(DrinkRowItemBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
            }

            public void setupUI(Drink drink){
                mDrink = drink;
                mBinding.textViewAlcoholPercentage.setText(String.valueOf(drink.getAlcohol()) + "% Alcohol");
                mBinding.textViewAlcoholSize.setText(String.valueOf(drink.getSize()) + "oz");
                mBinding.textViewDateAdded.setText("Added " + String.valueOf(drink.getAddedOnAsDate().toString()));

                if(mDrink.getType().equals("BEER")){
                    mBinding.imageView.setImageResource(R.drawable.ic_beer);
                } else if(mDrink.getType().equals("WINE")){
                    mBinding.imageView.setImageResource(R.drawable.ic_wine);
                } else if(mDrink.getType().equals("SHOT")){
                    mBinding.imageView.setImageResource(R.drawable.ic_shot);
                }

                mBinding.imageViewDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.drinkDAO().delete(mDrink);

                        getDrinksAndNotifyAdapter();
                        displayBacAndSetupUI();
                    }
                });

                mBinding.imageViewEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.gotoUpdateDrink(mDrink);
                    }
                });
            }
        }
    }

    BACFragmentListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (BACFragmentListener) context;
    }

    public interface BACFragmentListener{
        void gotoSetProfile();
        Profile getProfile();
        void clearProfile();
        void gotoAddDrink();
        void gotoUpdateDrink(Drink drink);
    }
}