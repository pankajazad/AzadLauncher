package com.pankajazad.azadlauncher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pankajazad.azadlauncher.databinding.ItemAppBinding;

import java.util.ArrayList;
import java.util.List;

final class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {
    private final List<AppEntry> apps = new ArrayList<>();

    void submit(List<AppEntry> newApps) {
        apps.clear();
        apps.addAll(newApps);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAppBinding binding = ItemAppBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AppViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppEntry app = apps.get(position);
        holder.binding.appIcon.setImageDrawable(app.getIcon());
        holder.binding.appLabel.setText(app.getLabel());
        holder.binding.getRoot().setOnClickListener(view -> view.getContext().startActivity(app.getLaunchIntent()));
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static final class AppViewHolder extends RecyclerView.ViewHolder {
        private final ItemAppBinding binding;

        AppViewHolder(ItemAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
