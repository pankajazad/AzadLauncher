package com.pankajazad.azadlauncher;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pankajazad.azadlauncher.databinding.ItemHiddenAppBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class HiddenAppListAdapter extends RecyclerView.Adapter<HiddenAppListAdapter.HiddenAppViewHolder> {
    interface Listener {
        void onHiddenChanged(AppEntry app, boolean hidden);
    }

    private final List<AppEntry> apps = new ArrayList<>();
    private final Set<String> hiddenAppIds = new HashSet<>();
    private final Listener listener;

    HiddenAppListAdapter(Listener listener) {
        this.listener = listener;
    }

    void submit(List<AppEntry> newApps, Set<String> hiddenIds) {
        apps.clear();
        apps.addAll(newApps);
        hiddenAppIds.clear();
        hiddenAppIds.addAll(hiddenIds);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HiddenAppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHiddenAppBinding binding = ItemHiddenAppBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HiddenAppViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HiddenAppViewHolder holder, int position) {
        AppEntry app = apps.get(position);
        holder.binding.hiddenAppIcon.setImageDrawable(app.getIcon());
        holder.binding.hiddenAppLabel.setText(app.getLabel());
        holder.binding.hiddenAppCheck.setOnCheckedChangeListener(null);
        holder.binding.hiddenAppCheck.setChecked(hiddenAppIds.contains(app.getId()));
        holder.binding.hiddenAppCheck.setOnCheckedChangeListener((button, hidden) -> {
            if (hidden) {
                hiddenAppIds.add(app.getId());
            } else {
                hiddenAppIds.remove(app.getId());
            }
            listener.onHiddenChanged(app, hidden);
        });
        holder.binding.getRoot().setOnClickListener(view -> holder.binding.hiddenAppCheck.toggle());
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static final class HiddenAppViewHolder extends RecyclerView.ViewHolder {
        private final ItemHiddenAppBinding binding;

        HiddenAppViewHolder(ItemHiddenAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
